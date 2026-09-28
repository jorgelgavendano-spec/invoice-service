package com.empresa.invoice_service.models.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalesClienteResponse {
    private Long clienteId;
    private String nombre;
    private String rfc;
    private Long totalFacturado;
    private Long totalPagado;
}