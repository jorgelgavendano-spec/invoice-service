package com.empresa.invoice_service.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaVencidaResponse {
    private Long id;
    private Long clienteId;
    private String folio;
    private Long total;
    private Long saldoPendiente;
}