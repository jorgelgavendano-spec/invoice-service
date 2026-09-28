package com.empresa.invoice_service.models.dto;

import java.io.Serializable;

import com.empresa.invoice_service.models.dto.request.PacRequest;
import com.empresa.invoice_service.models.entity.Factura;
import com.empresa.invoice_service.models.entity.TimbradoFactura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class TimbradoJobRabbit implements Serializable {

    private PacRequest request;
    private Factura factura;
    private TimbradoFactura TimbradoFactura;
}