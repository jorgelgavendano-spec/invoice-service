package com.empresa.invoice_service.models.dto.projection;

/**
 * CLienteTotalesProjection
 */
public interface TotalesClienteProjection {
    Long getclienteId();
    String getNombre();
    String getRfc();
    Long getTotalFacturado();
    Long getTotalPagado();
}
