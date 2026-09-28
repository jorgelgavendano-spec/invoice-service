package com.empresa.invoice_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest; // Spring Boot 4
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // reemplaza a @MockBean
import org.springframework.test.web.servlet.MockMvc;

import com.empresa.invoice_service.models.dto.request.TimbradoRequest;
import com.empresa.invoice_service.models.dto.response.TimbradoResponse;
import com.empresa.invoice_service.service.TimbradoService;

// Levanta SOLO la capa web (controller + validación + JSON). Sin BD, sin RabbitMQ.
@WebMvcTest(TimbradoController.class)
class TimbradoControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @MockitoBean
    private TimbradoService timbradoService;

    private static final String JSON_VALIDO = """
            {"external_id": "req-abc-123", "invoice_id": 42, "amount": 1500.50}
            """;

    private TimbradoResponse respuesta(HttpStatus http, String status, String uuid, String error) {
        TimbradoResponse r = new TimbradoResponse();
        r.setHttpCode(http);
        r.setStatus(status);
        r.setExternalId("req-abc-123");
        r.setUuidFiscal(uuid);
        r.setError(error);
        return r;
    }

    // POST /api/timbrados

    @Test
    void primerIntentoExitoso_regresa201ConUuid() throws Exception {
        when(timbradoService.timbrar(any(TimbradoRequest.class)))
                .thenReturn(respuesta(HttpStatus.CREATED, "timbrada", "uuid-123", null));

        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("timbrada"))
                .andExpect(jsonPath("$.external_id").value("req-abc-123"))
                .andExpect(jsonPath("$.uuid_fiscal").value("uuid-123"))
                // @JsonInclude(NON_NULL): un éxito no trae "error"
                .andExpect(jsonPath("$.error").doesNotExist())
                // @JsonIgnore: el httpCode es interno, no debe viajar al cliente
                .andExpect(jsonPath("$.httpCode").doesNotExist());

        // Verifica que el JSON (external_id, invoice_id) se mapeó bien al objeto Java
        ArgumentCaptor<TimbradoRequest> captor = ArgumentCaptor.forClass(TimbradoRequest.class);
        verify(timbradoService).timbrar(captor.capture());
        assertEquals("req-abc-123", captor.getValue().getExternalId());
        assertEquals(42L, captor.getValue().getInvoiceId());
        assertEquals(0, new BigDecimal("1500.50").compareTo(captor.getValue().getAmount()));
    }

    @Test
    void externalIdDuplicado_regresa200() throws Exception {
        when(timbradoService.timbrar(any(TimbradoRequest.class)))
                .thenReturn(respuesta(HttpStatus.OK, "timbrada", "uuid-original", null));

        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid_fiscal").value("uuid-original"));
    }

    @Test
    void pacFalla_regresa422ConError() throws Exception {
        when(timbradoService.timbrar(any(TimbradoRequest.class)))
                .thenReturn(respuesta(HttpStatus.UNPROCESSABLE_CONTENT, "fallida", null, "timeout"));

        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value("fallida"))
                .andExpect(jsonPath("$.error").value("timeout"))
                .andExpect(jsonPath("$.uuid_fiscal").doesNotExist());
    }

    @Test
    void facturaInexistente_regresa404ConMensaje() throws Exception {
        TimbradoResponse r = new TimbradoResponse();
        r.setHttpCode(HttpStatus.NOT_FOUND);
        r.setError("No se encontro la factura con el invoice_id especificado");
        when(timbradoService.timbrar(any(TimbradoRequest.class))).thenReturn(r);

        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No se encontro la factura con el invoice_id especificado"));
    }

    @Test
    void facturaYaTimbradaConOtroExternalId_regresa409() throws Exception {
        TimbradoResponse r = new TimbradoResponse();
        r.setHttpCode(HttpStatus.CONFLICT);
        r.setError("La factura ya fue timbrada o se encuentra en proceso con otro external_id");
        when(timbradoService.timbrar(any(TimbradoRequest.class))).thenReturn(r);

        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void faltaExternalId_regresa400() throws Exception {
        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"invoice_id": 42, "amount": 1500.50}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(timbradoService);
    }

    @Test
    void externalIdVacio_regresa400() throws Exception {
        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"external_id": "", "invoice_id": 42, "amount": 1500.50}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(timbradoService);
    }

    @Test
    void faltaInvoiceId_regresa400() throws Exception {
        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"external_id": "req-1", "amount": 1500.50}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(timbradoService);
    }

    @Test
    void amountNoNumerico_regresa400() throws Exception {
        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"external_id": "req-1", "invoice_id": 42, "amount": "abc"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(timbradoService);
    }

    @Test
    void amountVacio_regresa400() throws Exception {
        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"external_id": "req-1", "invoice_id": 42, "amount": ""}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(timbradoService);
    }

    @Test
    void jsonMalFormado_regresa400() throws Exception {
        mockMvc.perform(post("/api/timbrados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(timbradoService);
    }

    // POST /api/timbradosCola

    @Test
    void timbradosCola_regresa202ConMensajeDeEnProceso() throws Exception {
        TimbradoResponse r = new TimbradoResponse();
        r.setHttpCode(HttpStatus.ACCEPTED);
        r.setStatus("proceso");
        r.setExternalId("req-abc-123");
        r.setMensaje("Factura en proceso de timbrado");
        when(timbradoService.timbrarPorCola(any(TimbradoRequest.class))).thenReturn(r);

        mockMvc.perform(post("/api/timbradosCola")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("proceso"))
                .andExpect(jsonPath("$.mensaje").value("Factura en proceso de timbrado"))
                .andExpect(jsonPath("$.uuid_fiscal").doesNotExist());
    }

    @Test
    void timbradosCola_payloadInvalido_regresa400() throws Exception {
        mockMvc.perform(post("/api/timbradosCola")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"invoice_id": 42, "amount": 1500.50}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(timbradoService);
    }
}