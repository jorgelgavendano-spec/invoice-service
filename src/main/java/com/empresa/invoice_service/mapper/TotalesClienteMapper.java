package com.empresa.invoice_service.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.empresa.invoice_service.models.dto.projection.TotalesClienteProjection;
import com.empresa.invoice_service.models.dto.response.ListadoTotalesClientesResponse;
import com.empresa.invoice_service.models.dto.response.TotalesClienteResponse;

@Component
public class TotalesClienteMapper {

    public TotalesClienteResponse toResponse(TotalesClienteProjection proyeccion) {
        return new TotalesClienteResponse(
                proyeccion.getclienteId(),
                proyeccion.getNombre(),
                proyeccion.getRfc(),
                proyeccion.getTotalFacturado(),
                proyeccion.getTotalPagado()
        );
    }

    public ListadoTotalesClientesResponse toResponseClienteTotales(List<TotalesClienteProjection> proyecciones) {
        List<TotalesClienteResponse> lista = proyecciones.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new ListadoTotalesClientesResponse(lista.size(), lista);
    }
}