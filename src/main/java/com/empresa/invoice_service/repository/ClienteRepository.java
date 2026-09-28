package com.empresa.invoice_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.empresa.invoice_service.models.dto.projection.TotalesClienteProjection;
import com.empresa.invoice_service.models.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query(value = """
        SELECT
            ce.id AS clienteId,
            ce.nombre AS nombre,
            ce.rfc AS rfc,
            COALESCE(f.total_facturado, 0) AS totalFacturado,
            COALESCE(p.total_pagado, 0) AS totalPagado
        FROM clientes ce
        LEFT JOIN (
            SELECT cliente_id, SUM(total) AS total_facturado
            FROM facturas
            WHERE estatus = 'VIGENTE'
            GROUP BY cliente_id
        ) f ON f.cliente_id = ce.id
        LEFT JOIN (
            SELECT fa.cliente_id, SUM(pa.monto) AS total_pagado
            FROM facturas fa
            JOIN pagos pa ON pa.factura_id = fa.id
            WHERE estatus = 'VIGENTE'
            GROUP BY fa.cliente_id
        ) p ON p.cliente_id = ce.id
        ORDER BY ce.nombre
        """, nativeQuery = true)
    List<TotalesClienteProjection> findTotalesClientes();
}