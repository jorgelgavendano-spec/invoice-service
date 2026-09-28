package com.empresa.invoice_service.models.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "clientes")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Cliente {

    @Id 
    private Long id;
    private String nombre;
    private String rfc; 
}
