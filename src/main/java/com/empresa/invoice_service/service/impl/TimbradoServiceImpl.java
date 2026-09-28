package com.empresa.invoice_service.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.empresa.invoice_service.config.RabbitMQConfig;
import com.empresa.invoice_service.mocks.FakePacApi;
import com.empresa.invoice_service.models.dto.request.TimbradoRequest;
import com.empresa.invoice_service.models.dto.TimbradoJobRabbit;
import com.empresa.invoice_service.models.dto.request.PacRequest;
import com.empresa.invoice_service.models.dto.response.TimbradoResponse;
import com.empresa.invoice_service.models.dto.response.PacResponse;
import com.empresa.invoice_service.models.entity.Factura;
import com.empresa.invoice_service.models.entity.TimbradoFactura;
import com.empresa.invoice_service.repository.FacturaRepository;
import com.empresa.invoice_service.service.TimbradoService;
import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor 
public class TimbradoServiceImpl implements TimbradoService {
    
        private final FakePacApi fakePacApi;
        private final PersistanceServiceImpl persistanceService;
        private final FacturaRepository facturaRepository;
        private final RabbitTemplate template;

        @Value("${tiempoLimiteProceso}")
        private Integer tiempoLimiteProceso;

        @Override 
        public TimbradoResponse timbrar(TimbradoRequest request) throws InterruptedException {

                TimbradoResponse response = new TimbradoResponse();

                //Si la factura no existe no se timbra
               Optional<Factura> factura = facturaRepository.findById(request.getInvoiceId());
                if(!factura.isPresent()){
                        response.setHttpCode(HttpStatus.NOT_FOUND);
                        response.setError("No se encontro la factura con el invoice_id especificado");
                        return response;
                }

                //Se consulta si la factura fue intentada timbrar 
                TimbradoFactura timbradoFactura = persistanceService.buscarFacturaInvoiceId(request.getInvoiceId());
        
                //Se verifica si la factura tuvo intento de timbrado
                if (timbradoFactura != null) {
                        //Si el intento de timbrado fue con distinto externalId notificamos que ya esta o timbrada o en proceso, si acaso es fallida continuamos con el nuevo externalID

                        if (!timbradoFactura.getExternalId().equals(request.getExternalId()) &&
                        ("timbrada".equals(factura.get().getEstatus() ) || "proceso".equals(factura.get().getEstatus() ))){
                                response.setHttpCode(HttpStatus.CONFLICT);
                                response.setError("La factura ya fue timbrada o se encuentra en proceso con otro external_id");
                                return response;
                        }
                        // Si fue timbrada con exito retorna el 200
                        if ("timbrada".equals(factura.get().getEstatus())){
                                response.setHttpCode(HttpStatus.OK);
                                response.setStatus(factura.get().getEstatus());
                                response.setExternalId(timbradoFactura.getExternalId());
                                response.setUuidFiscal(timbradoFactura.getUuidFiscal());
                                return response; 
                        }
                        // Si hubo un fallo y se quedo en proceso por mas de 5 segundos se considera fallida y continua para volver a intentar timbrar
                        // si el proceso es menos se retorna una respuesta de en proceso
                        if("proceso".equals(factura.get().getEstatus())
                                && Duration.between(timbradoFactura.getFecha_envio_timbrado(), LocalDateTime.now()).getSeconds()<this.tiempoLimiteProceso){
                                response.setHttpCode(HttpStatus.ACCEPTED);
                                response.setStatus(factura.get().getEstatus());
                                response.setExternalId(timbradoFactura.getExternalId());
                                response.setUuidFiscal(null);
                                response.setMensaje("Factura en proceso de timbrado");
                                return response;
                        } 
                }
                
                //Se asigna una fecha nueva de envio y se cambia estatus en proceso 

                timbradoFactura = request.toTimbradoFacturasEntity();
                factura.get().setEstatus("proceso");
                timbradoFactura.setFecha_envio_timbrado(LocalDateTime.now());

                //Se guarda el timbradoFActura y el estado de la factura
                persistanceService.guardar(timbradoFactura);
                facturaRepository.save(factura.get());
                

                //Se manda el timbrado
                PacResponse responsePac = timbrarFactura(timbradoFactura, factura.get().getCfdiXml());
                
                //Se guardan los estados despues de timbrar
                factura.get().setEstatus(responsePac.isSuccess()?"timbrada":"fallida");
                timbradoFactura.setUuidFiscal(responsePac.isSuccess()?responsePac.getUuidFiscal():null);
                persistanceService.guardar(timbradoFactura);
                facturaRepository.save(factura.get());

                //Se arma el response para retornar la respuesta 
                response.setHttpCode(responsePac.isSuccess()?HttpStatus.CREATED:responsePac.getHttpCode());
                response.setStatus(factura.get().getEstatus());
                response.setExternalId(timbradoFactura.getExternalId());
                response.setUuidFiscal(timbradoFactura.getUuidFiscal());
                response.setError(!responsePac.isSuccess()?responsePac.getError():null);
                return response;
        }


         public TimbradoResponse timbrarPorCola(TimbradoRequest request) throws InterruptedException {

                TimbradoResponse response = new TimbradoResponse();

                //Si la factura no existe no se timbra
               Optional<Factura> factura = facturaRepository.findById(request.getInvoiceId());
                if(!factura.isPresent()){
                        response.setHttpCode(HttpStatus.NOT_FOUND);
                        response.setError("No se encontro la factura con el invoice_id especificado");
                        return response;
                }

                //Se consulta si la factura fue intentada timbrar 
                TimbradoFactura timbradoFactura = persistanceService.buscarFacturaInvoiceId(request.getInvoiceId());

                //Se verifica si hubo un intento de timbrado con el external id.
                // TimbradoFactura timbradoExternalId = persistanceService.buscarFacturaExternalId(request.getExternalId());
        
                //Se verifica si la factura tuvo intento de timbrado
                if (timbradoFactura != null) {
                        //Si el intento de timbrado fue con distinto externalId notificamos que ya esta o timbrada o en proceso, si acaso es fallida continuamos con el nuevo externalID
                        if (!timbradoFactura.getExternalId().equals(request.getExternalId()) && List.of("timbrada","proceso").contains(factura.get().getEstatus())){
                                response.setHttpCode(HttpStatus.CONFLICT);
                                response.setError("La factura ya fue timbrada o se encuentra en proceso con otro external_id");
                                return response;
                        }
                        // Si fue timbrada con exito retorna el 200
                        if ("timbrada".equals(factura.get().getEstatus())){
                                response.setHttpCode(HttpStatus.OK);
                                response.setStatus(factura.get().getEstatus());
                                response.setExternalId(timbradoFactura.getExternalId());
                                response.setUuidFiscal(timbradoFactura.getUuidFiscal());
                                return response; 
                        }
                        // Si hubo un fallo y se quedo en proceso por mas de 5 segundos se considera fallida y continua para volver a intentar timbrar
                        // si el proceso es menos se retorna una respuesta de en proceso
                        if("proceso".equals(factura.get().getEstatus())
                                && Duration.between(timbradoFactura.getFecha_envio_timbrado(), LocalDateTime.now()).getSeconds()<5){
                                response.setHttpCode(HttpStatus.ACCEPTED);
                                response.setStatus(factura.get().getEstatus());
                                response.setExternalId(timbradoFactura.getExternalId());
                                response.setUuidFiscal(null);
                                response.setMensaje("Factura en proceso de timbrado");
                                return response;
                        } 
                }
                
                //Se asigna una fecha nueva de envio y se cambia estatus en proceso 

                timbradoFactura = request.toTimbradoFacturasEntity();
                factura.get().setEstatus("proceso");
                timbradoFactura.setFecha_envio_timbrado(LocalDateTime.now());

                //Se guarda el timbradoFActura y el estado de la factura
                persistanceService.guardar(timbradoFactura);
                facturaRepository.save(factura.get());
                

                 TimbradoJobRabbit job = new TimbradoJobRabbit(new PacRequest(
                        timbradoFactura.getExternalId(),
                        timbradoFactura.getInvoiceId(),
                        timbradoFactura.getAmount(),
                        factura.get().getCfdiXml()
                        ),factura.get(), timbradoFactura);
                template.convertAndSend(
                        RabbitMQConfig.EXCHANGE_TIMBRADO,
                        RabbitMQConfig.ROUTING_KEY_TIMBRADO,
                        job);
                
                // //Se guardan los estados despues de timbrar
                // factura.get().setEstatus(responsePac.isSuccess()?"timbrada":"fallida");
                // timbradoFactura.setUuidFiscal(responsePac.isSuccess()?responsePac.getUuidFiscal():null);
                // persistanceService.guardar(timbradoFactura);
                // facturaRepository.save(factura.get());

                //Se arma el response para retornar la respuesta 
                response.setHttpCode(HttpStatus.ACCEPTED);
                response.setStatus(factura.get().getEstatus());
                response.setExternalId(timbradoFactura.getExternalId());
                response.setUuidFiscal(null);
                response.setMensaje("Factura en proceso de timbrado");
                return response;
        }

        private PacResponse timbrarFactura(TimbradoFactura timbrado, String cfdiXml) throws InterruptedException{
                final PacRequest pacRequest = new PacRequest(
                timbrado.getExternalId(),
                timbrado.getInvoiceId(),
                timbrado.getAmount(),
                cfdiXml
                );
                return fakePacApi.timbrado(pacRequest); 
        }
}
