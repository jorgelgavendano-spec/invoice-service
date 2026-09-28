package com.empresa.invoice_service.models.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "pagos")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Pago {
    @Id
    private Long id;
    @Column (name = "factura_id")
    private Long facturaId;
    private Long monto;
    @Column (name = "fecha_pago")
    private LocalDateTime fechaPago;
}
