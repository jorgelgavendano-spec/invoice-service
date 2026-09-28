package com.empresa.invoice_service.service;

import java.util.List;

import com.empresa.invoice_service.models.dto.projection.TotalesClienteProjection;
import com.empresa.invoice_service.models.dto.projection.FacturaVencidaProjection;

public interface ReporteriaService {
    public List<TotalesClienteProjection> obtenerTotalesClientes();
    public List<FacturaVencidaProjection> obtenerFacturasVencidasDeuda();
}
