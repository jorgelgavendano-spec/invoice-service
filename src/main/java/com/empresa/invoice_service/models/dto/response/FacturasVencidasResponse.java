package com.empresa.invoice_service.models.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturasVencidasResponse {
   
    private int total;
    private List<FacturaVencidaResponse> facturas; 
}
