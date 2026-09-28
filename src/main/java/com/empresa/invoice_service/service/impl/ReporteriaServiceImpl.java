package com.empresa.invoice_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.empresa.invoice_service.models.dto.projection.TotalesClienteProjection;
import com.empresa.invoice_service.models.dto.projection.FacturaVencidaProjection;
import com.empresa.invoice_service.repository.ClienteRepository;
import com.empresa.invoice_service.repository.FacturaRepository;
import com.empresa.invoice_service.service.ReporteriaService;


import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class ReporteriaServiceImpl implements ReporteriaService {

    private final ClienteRepository clienteRepository;
    private final FacturaRepository facturaRepository;
    
    @Override 
    public List<TotalesClienteProjection> obtenerTotalesClientes(){
        return clienteRepository.findTotalesClientes();
    };
    
    @Override 
    public List<FacturaVencidaProjection> obtenerFacturasVencidasDeuda(){
        return facturaRepository.findVencidasConPagosPendientes();
    };
}
