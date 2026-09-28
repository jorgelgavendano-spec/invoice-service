
package com.empresa.invoice_service.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.empresa.invoice_service.models.dto.projection.FacturaVencidaProjection;
import com.empresa.invoice_service.models.dto.response.FacturaVencidaResponse;
import com.empresa.invoice_service.models.dto.response.FacturasVencidasResponse;

@Component
public class FacturaVencidaMapper {

    public FacturaVencidaResponse toResponse(FacturaVencidaProjection proyeccion) {
        return new FacturaVencidaResponse(
                proyeccion.getId(),
                proyeccion.getClienteId(),
                proyeccion.getFolio(),
                proyeccion.getTotal(),
                proyeccion.getSaldoPendiente()
        );
    }

    public FacturasVencidasResponse toResponseFacturasVencidas(List<FacturaVencidaProjection> proyecciones) {
        List<FacturaVencidaResponse> lista = proyecciones.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new FacturasVencidasResponse(lista.size(), lista);
    }
}