package com.empresa.invoice_service.mocks;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.empresa.invoice_service.models.dto.request.PacRequest;
import com.empresa.invoice_service.models.dto.response.PacResponse;

@Component 
public class FakePacApi {
    public PacResponse timbrado(PacRequest request) throws InterruptedException{
        PacResponse response = new PacResponse();
        boolean tiempo = Math.random() < 0.5; 
        boolean fallo = Math.random() < 0.333; 
         if (tiempo)
             Thread.sleep(3000);

        if (fallo) {
            boolean bifurcarFallo = Math.random() < 0.5; 
            response.setHttpCode(HttpStatus.UNPROCESSABLE_CONTENT);
            response.setSuccess(false);
            response.setError(bifurcarFallo ? "timeout" : "Error al timbrar la factura, inconsistencia de datos");

        } else {
            response.setHttpCode(HttpStatus.CREATED);
            response.setSuccess(true);
            response.setUuidFiscal(UUID.randomUUID().toString());
        }
        return response;
    }
}
