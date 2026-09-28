package com.empresa.invoice_service.models.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "timbrado_facturas", indexes = {
    @Index(name = "idx_invoice_id", columnList = "invoice_id"),
    @Index(name = "idx_external_id", columnList = "external_id")
})
@Data 
@NoArgsConstructor 
@AllArgsConstructor 

public class TimbradoFactura {
    
    @Id
    @Column (name = "invoice_id") 
    private Long invoiceId; 

    @Column (name = "external_id", unique = true)
    private String externalId;  

    @Column (name = "amount")
    private BigDecimal amount; 

    @Column (name = "uuid_fiscal")
    private String uuidFiscal; 
    
    @Column (name = "estatus")
    private String estatus; 

    @Column (name = "fecha_envio_timbrado")
    private LocalDateTime fecha_envio_timbrado;

    @Column (name = "fecha_emision")
    private LocalDateTime  fechaEmision;
    

     
}
