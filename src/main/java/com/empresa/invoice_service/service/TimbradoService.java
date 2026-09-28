package com.empresa.invoice_service.service;


import com.empresa.invoice_service.models.dto.request.TimbradoRequest;
import com.empresa.invoice_service.models.dto.response.TimbradoResponse;

public interface TimbradoService {
    public TimbradoResponse timbrar(TimbradoRequest request) throws InterruptedException;
    public TimbradoResponse timbrarPorCola(TimbradoRequest request) throws InterruptedException;
    
}
