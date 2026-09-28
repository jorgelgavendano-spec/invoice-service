package com.empresa.invoice_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.invoice_service.models.dto.request.TimbradoRequest;
import com.empresa.invoice_service.models.dto.response.TimbradoResponse;
import com.empresa.invoice_service.service.TimbradoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api")
public class TimbradoController {

    private final TimbradoService timbradoService;

    TimbradoController(TimbradoService timbradoService) {
            this.timbradoService = timbradoService;
        }   

    @PostMapping("/timbrados")
    public ResponseEntity<TimbradoResponse> timbrados(@Valid @RequestBody final TimbradoRequest invoiceRequest) throws InterruptedException {
        TimbradoResponse response = timbradoService.timbrar(invoiceRequest);
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }
    @PostMapping("/timbradosCola")
    public ResponseEntity<TimbradoResponse> timbradosCola(@Valid @RequestBody final TimbradoRequest invoiceRequest) throws InterruptedException {
        TimbradoResponse response = timbradoService.timbrarPorCola(invoiceRequest);
        return ResponseEntity.status(response.getHttpCode()).body(response);
    }
    
    
}
