package com.empresa.invoice_service.service.worker; // <-- AJUSTA: debe ser el mismo paquete donde está tu TimbradoWorker

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.empresa.invoice_service.mocks.FakePacApi;
import com.empresa.invoice_service.models.dto.TimbradoJobRabbit;
import com.empresa.invoice_service.models.dto.request.PacRequest;
import com.empresa.invoice_service.models.dto.response.PacResponse;
import com.empresa.invoice_service.models.entity.Factura;
import com.empresa.invoice_service.models.entity.TimbradoFactura;
import com.empresa.invoice_service.repository.FacturaRepository;
import com.empresa.invoice_service.service.PersistanceService;

@ExtendWith(MockitoExtension.class)
class TimbradoWorkerTest {

    // Dependencias falsas
    @Mock private FakePacApi fackepack;
    @Mock private PersistanceService persistanceService;
    @Mock private FacturaRepository facturaRepository;

    // Se contruye e inyecta el worker
    @InjectMocks private TimbradoWorker worker;

    private Factura factura;
    private TimbradoFactura timbradoFactura;
    private TimbradoJobRabbit job;

    @BeforeEach
    void setUp() {
        factura = new Factura();
        factura.setId(42L);
        factura.setEstatus("proceso");

        timbradoFactura = new TimbradoFactura();
        timbradoFactura.setInvoiceId(42L);
        timbradoFactura.setExternalId("req-1");
        timbradoFactura.setAmount(new BigDecimal("1500.50"));
        timbradoFactura.setFecha_envio_timbrado(LocalDateTime.now());

        PacRequest pacRequest = new PacRequest("req-1", 42L, new BigDecimal("1500.50"), null);

        job = new TimbradoJobRabbit(pacRequest, factura, timbradoFactura);
    }

    @Test
    void pacExitoso_marcaFacturaTimbrada_yGuardaElUuid() throws Exception {
        PacResponse pac = new PacResponse();
        pac.setSuccess(true);
        pac.setHttpCode(HttpStatus.CREATED);
        pac.setUuidFiscal("uuid-123");
        when(fackepack.timbrado(any())).thenReturn(pac);

        worker.procesarTimbrado(job);

        assertEquals("timbrada", factura.getEstatus());
        assertEquals("uuid-123", timbradoFactura.getUuidFiscal());
        verify(persistanceService, times(1)).guardar(timbradoFactura);
        verify(facturaRepository, times(1)).save(factura);
    }

    @Test
    void pacFallido_marcaFacturaFallida_yNoGuardaUuid() throws Exception {
        PacResponse pac = new PacResponse();
        pac.setSuccess(false);
        pac.setHttpCode(HttpStatus.UNPROCESSABLE_CONTENT);
        pac.setError("timeout");
        when(fackepack.timbrado(any())).thenReturn(pac);

        worker.procesarTimbrado(job);

        assertEquals("fallida", factura.getEstatus());
        assertNull(timbradoFactura.getUuidFiscal());
        verify(persistanceService, times(1)).guardar(timbradoFactura);
        verify(facturaRepository, times(1)).save(factura);
    }

    @Test
    void pacLanzaExcepcion_noSePropaga_marcaFallida_yAunAsiGuarda() throws Exception {
        when(fackepack.timbrado(any())).thenThrow(new RuntimeException("conexion perdida"));

        assertDoesNotThrow(() -> worker.procesarTimbrado(job));

        assertEquals("fallida", factura.getEstatus());
        assertNull(timbradoFactura.getUuidFiscal());
        
        verify(persistanceService, times(1)).guardar(timbradoFactura);
        verify(facturaRepository, times(1)).save(factura);
    }
}