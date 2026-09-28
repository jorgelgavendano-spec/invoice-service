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
@Table (name = "facturas")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 

public class Factura {
    
    @Id 
    @Column (name = "id", unique = true)
    private Long id;

    @Column (name = "cliente_id")
    private Long clienteId;

    private String folio;

    private Long total;

    private String estatus;

    private String cfdiXml;

    @Column (name = "fecha_emision")
    private LocalDateTime  fechaEmision;
}
