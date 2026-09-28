package com.empresa.invoice_service.service.worker;

import com.empresa.invoice_service.config.RabbitMQConfig;
import com.empresa.invoice_service.models.dto.TimbradoJobRabbit;
import com.empresa.invoice_service.repository.FacturaRepository;
import com.empresa.invoice_service.mocks.FakePacApi;
import com.empresa.invoice_service.service.PersistanceService;

import lombok.AllArgsConstructor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor 
public class TimbradoWorker {

    private final FakePacApi fackepack;
    private final PersistanceService persistanceService;
    private final FacturaRepository facturaRepository;
    

    @RabbitListener(queues = RabbitMQConfig.COLA_TIMBRADO)
    public void procesarTimbrado(TimbradoJobRabbit job) {

        System.out.println("Procesando timbrado external_id=" + job.getRequest().getExternalId());

        try {
            var response = fackepack.timbrado(job.getRequest());
            job.getFactura().setEstatus(response.isSuccess()?"timbrada":"fallida");
            job.getTimbradoFactura().setUuidFiscal(response.isSuccess()?response.getUuidFiscal():null);
            
            System.out.println("Timbrado exitoso, UUID=" + response.getUuidFiscal());
            // aquí guardarías en BD el resultado exitoso (lo vemos en el siguiente paso)

        } catch (Exception ex) {
            job.getFactura().setEstatus("fallida");
            
            System.out.println("Error general: " + ex.getMessage());
        } finally{
            persistanceService.guardar( job.getTimbradoFactura());
            facturaRepository.save(job.getFactura());
        }
    }
}