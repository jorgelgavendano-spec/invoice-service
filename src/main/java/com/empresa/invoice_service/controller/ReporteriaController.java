package com.empresa.invoice_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.invoice_service.mapper.FacturaVencidaMapper;
import com.empresa.invoice_service.mapper.TotalesClienteMapper;
import com.empresa.invoice_service.models.dto.projection.FacturaVencidaProjection;
import com.empresa.invoice_service.models.dto.projection.TotalesClienteProjection;
import com.empresa.invoice_service.models.dto.response.FacturasVencidasResponse;
import com.empresa.invoice_service.models.dto.response.ListadoTotalesClientesResponse;
import com.empresa.invoice_service.service.ReporteriaService;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@AllArgsConstructor 
@RequestMapping ("/api/reporteria")
public class ReporteriaController {
    final ReporteriaService reporteriaService;
    final FacturaVencidaMapper facturaVencidaMapper;
    final TotalesClienteMapper totalesClienteMapper;

    @GetMapping("/facturasVencidasDeuda")
    public ResponseEntity<FacturasVencidasResponse> facturasVencidasDeuda() {
        List<FacturaVencidaProjection> facturas = reporteriaService.obtenerFacturasVencidasDeuda();
        FacturasVencidasResponse response = facturaVencidaMapper.toResponseFacturasVencidas(facturas);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/totalesClientes")
    public ResponseEntity<ListadoTotalesClientesResponse> totalesClientes() {
        List<TotalesClienteProjection> totales = reporteriaService.obtenerTotalesClientes(); 
        ListadoTotalesClientesResponse response = totalesClienteMapper.toResponseClienteTotales(totales);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    
}
