package com.empresa.invoice_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.empresa.invoice_service.models.dto.projection.FacturaVencidaProjection;
import com.empresa.invoice_service.models.entity.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    @Query (value = """
        SELECT 
            fa.id,
            fa.cliente_id AS clienteId,
            fa.folio,
            fa.total,
            fa.total - COALESCE(SUM(pa.monto),0) saldoPendiente
        FROM facturas fa
        LEFT JOIN pagos pa ON fa.id = pa.factura_id
        WHERE fa.estatus = 'VENCIDA'
        GROUP BY fa.id, fa.cliente_id, fa.folio, fa.total
        HAVING fa.total - COALESCE(SUM(pa.monto),0) > 0
        """, nativeQuery = true)
    List<FacturaVencidaProjection> findVencidasConPagosPendientes();
    Optional<Factura> findById(Long id);
}