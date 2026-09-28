package com.empresa.invoice_service.models.dto.projection;

public interface FacturaVencidaProjection {
    Long getId();
    Long getClienteId();
    String getFolio();
    Long getTotal();
    Long getSaldoPendiente();
}