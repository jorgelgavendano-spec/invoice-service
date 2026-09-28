package com.empresa.invoice_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;

import com.empresa.invoice_service.config.RabbitMQConfig;
import com.empresa.invoice_service.mocks.FakePacApi;
import com.empresa.invoice_service.models.dto.TimbradoJobRabbit;
import com.empresa.invoice_service.models.dto.request.TimbradoRequest;
import com.empresa.invoice_service.models.dto.response.PacResponse;
import com.empresa.invoice_service.models.dto.response.TimbradoResponse;
import com.empresa.invoice_service.models.entity.Factura;
import com.empresa.invoice_service.models.entity.TimbradoFactura;
import com.empresa.invoice_service.repository.FacturaRepository;

@ExtendWith(MockitoExtension.class)
class TimbradoServiceImplTest {

    @Mock private FakePacApi fakePacApi;
    @Mock private PersistanceServiceImpl persistanceService;
    @Mock private FacturaRepository facturaRepository;
    @Mock private RabbitTemplate template;

    //Se levanta el servicio de timbrado
    @InjectMocks private TimbradoServiceImpl service;

    //Helpers para los tests
    private TimbradoRequest request(String externalId) {
        TimbradoRequest r = new TimbradoRequest();
        r.setExternalId(externalId);
        r.setInvoiceId(42L);
        r.setAmount(new BigDecimal("1500.50"));
        return r;
    }

    private Factura factura(String estatus) {
        Factura f = new Factura();
        f.setId(42L);
        f.setEstatus(estatus);
        return f;
    }

    private TimbradoFactura timbradoPrevio(String externalId, LocalDateTime fechaEnvio, String uuid) {
        TimbradoFactura t = new TimbradoFactura();
        t.setInvoiceId(42L);
        t.setExternalId(externalId);
        t.setAmount(new BigDecimal("1500.50"));
        t.setUuidFiscal(uuid);
        t.setFecha_envio_timbrado(fechaEnvio);
        return t;
    }

    private PacResponse pacExito() {
        PacResponse p = new PacResponse();
        p.setSuccess(true);
        p.setHttpCode(HttpStatus.CREATED);
        p.setUuidFiscal("uuid-123");
        return p;
    }

    private PacResponse pacFalla(String error) {
        PacResponse p = new PacResponse();
        p.setSuccess(false);
        p.setHttpCode(HttpStatus.UNPROCESSABLE_CONTENT);
        p.setError(error);
        return p;
    }

// flujo de timbrado convencional timbrar() (POST /api/timbrados)\

    @Test
    void timbrar_facturaInexistente_regresa404_yNoLlamaAlPac() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.empty());

        TimbradoResponse response = service.timbrar(request("req-1"));

        assertEquals(HttpStatus.NOT_FOUND, response.getHttpCode());
        assertNotNull(response.getError());
        verify(fakePacApi, never()).timbrado(any());
        verify(persistanceService, never()).guardar(any());
    }

    @Test
    void timbrar_primerIntentoConPacExitoso_regresa201() throws Exception {
        Factura factura = factura("");
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura));
        // buscarFacturaInvoiceId no se stubbea: el mock devuelve null = "nunca se intentó"
        when(fakePacApi.timbrado(any())).thenReturn(pacExito());

        TimbradoResponse response = service.timbrar(request("req-1"));

        assertEquals(HttpStatus.CREATED, response.getHttpCode());
        assertEquals("timbrada", response.getStatus());
        assertEquals("req-1", response.getExternalId());
        assertEquals("uuid-123", response.getUuidFiscal());
        assertNull(response.getError());
        assertEquals("timbrada", factura.getEstatus());

        // Se guarda dos veces: una en "proceso" y otra con el resultado final
        verify(persistanceService, times(2)).guardar(any());
        verify(facturaRepository, times(2)).save(factura);
    }

    @Test
    void timbrar_primerIntentoConPacFallido_regresa422ConError() throws Exception {
        Factura factura = factura("");
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura));
        when(fakePacApi.timbrado(any())).thenReturn(pacFalla("timeout"));

        TimbradoResponse response = service.timbrar(request("req-1"));

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getHttpCode());
        assertEquals("fallida", response.getStatus());
        assertEquals("timeout", response.getError());
        assertNull(response.getUuidFiscal());
        assertEquals("fallida", factura.getEstatus());
    }

    @Test
    void timbrar_mismoExternalIdYaTimbrado_regresa200ConElMismoResultado() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura("timbrada")));
        when(persistanceService.buscarFacturaInvoiceId(42L))
                .thenReturn(timbradoPrevio("req-1", LocalDateTime.now().minusMinutes(1), "uuid-original"));

        TimbradoResponse response = service.timbrar(request("req-1"));

        assertEquals(HttpStatus.OK, response.getHttpCode());
        assertEquals("timbrada", response.getStatus());
        assertEquals("req-1", response.getExternalId());
        assertEquals("uuid-original", response.getUuidFiscal());
        // Lo más importante de la idempotencia: NO se vuelve a timbrar
        verify(fakePacApi, never()).timbrado(any());
        verify(persistanceService, never()).guardar(any());
    }

    @Test
    void timbrar_facturaYaTimbradaConOtroExternalId_regresa409() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura("timbrada")));
        when(persistanceService.buscarFacturaInvoiceId(42L))
                .thenReturn(timbradoPrevio("req-1", LocalDateTime.now().minusMinutes(1), "uuid-original"));

        TimbradoResponse response = service.timbrar(request("req-DISTINTO"));

        assertEquals(HttpStatus.CONFLICT, response.getHttpCode());
        assertNotNull(response.getError());
        verify(fakePacApi, never()).timbrado(any());
    }

    @Test
    void timbrar_enProcesoHaceMenosDe5Segundos_regresa202SinTimbrarDeNuevo() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura("proceso")));
        when(persistanceService.buscarFacturaInvoiceId(42L))
                .thenReturn(timbradoPrevio("req-1", LocalDateTime.now(), null));

        TimbradoResponse response = service.timbrar(request("req-1"));

        assertEquals(HttpStatus.ACCEPTED, response.getHttpCode());
        assertEquals("proceso", response.getStatus());
        assertEquals("Factura en proceso de timbrado", response.getMensaje());
        verify(fakePacApi, never()).timbrado(any());
    }

    @Test
    void timbrar_enProcesoHaceMasDe5Segundos_seConsideraCaidaYReintenta() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura("proceso")));
        when(persistanceService.buscarFacturaInvoiceId(42L))
                .thenReturn(timbradoPrevio("req-1", LocalDateTime.now().minusSeconds(30), null));
        when(fakePacApi.timbrado(any())).thenReturn(pacExito());

        TimbradoResponse response = service.timbrar(request("req-1"));

        assertEquals(HttpStatus.CREATED, response.getHttpCode());
        verify(fakePacApi, times(1)).timbrado(any());
    }

    @Test
    void timbrar_intentoPreviofallidoConOtroExternalId_permiteReintentar() throws Exception {
        Factura factura = factura("fallida");
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura));
        when(persistanceService.buscarFacturaInvoiceId(42L))
                .thenReturn(timbradoPrevio("req-viejo", LocalDateTime.now().minusMinutes(5), null));
        when(fakePacApi.timbrado(any())).thenReturn(pacExito());

        TimbradoResponse response = service.timbrar(request("req-nuevo"));

        assertEquals(HttpStatus.CREATED, response.getHttpCode());
        assertEquals("req-nuevo", response.getExternalId());

        // Verifica que lo guardado ya trae el external_id nuevo
        ArgumentCaptor<TimbradoFactura> captor = ArgumentCaptor.forClass(TimbradoFactura.class);
        verify(persistanceService, times(2)).guardar(captor.capture());
        assertEquals("req-nuevo", captor.getValue().getExternalId());
    }

    @Test
    void timbrar_intentoPreviofallidoConMismoExternalId_reintentaContraElPac() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura("fallida")));
        when(persistanceService.buscarFacturaInvoiceId(42L))
                .thenReturn(timbradoPrevio("req-1", LocalDateTime.now().minusMinutes(5), null));
        when(fakePacApi.timbrado(any())).thenReturn(pacExito());

        TimbradoResponse response = service.timbrar(request("req-1"));

        assertEquals(HttpStatus.CREATED, response.getHttpCode());
        verify(fakePacApi, times(1)).timbrado(any());
    }

// flujo de timbrado a trvez de cola (POST /api/timbradosCola)

    @Test
    void timbrarPorCola_facturaInexistente_regresa404_yNoPublicaMensaje() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.empty());

        TimbradoResponse response = service.timbrarPorCola(request("req-1"));

        assertEquals(HttpStatus.NOT_FOUND, response.getHttpCode());
        verifyNoInteractions(template);
    }

    @Test
    void timbrarPorCola_primerIntento_regresa202_yPublicaEnLaCola() throws Exception {
        Factura factura = factura("");
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura));

        TimbradoResponse response = service.timbrarPorCola(request("req-1"));

        assertEquals(HttpStatus.ACCEPTED, response.getHttpCode());
        assertEquals("proceso", response.getStatus());
        assertEquals("req-1", response.getExternalId());
        assertNull(response.getUuidFiscal());

        // Se publicó exactamente un mensaje, al exchange y routing key correctos
        verify(template, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_TIMBRADO),
                eq(RabbitMQConfig.ROUTING_KEY_TIMBRADO),
                any(TimbradoJobRabbit.class));
        // El service NO llama al PAC en este flujo: eso lo hace el worker
        verify(fakePacApi, never()).timbrado(any());
    }

    @Test
    void timbrarPorCola_facturaEnProcesoConOtroExternalId_regresa409_yNoPublica() throws Exception {
        when(facturaRepository.findById(42L)).thenReturn(Optional.of(factura("proceso")));
        when(persistanceService.buscarFacturaInvoiceId(42L))
                .thenReturn(timbradoPrevio("req-1", LocalDateTime.now(), null));

        TimbradoResponse response = service.timbrarPorCola(request("req-DISTINTO"));

        assertEquals(HttpStatus.CONFLICT, response.getHttpCode());
        verifyNoInteractions(template);
    }
}