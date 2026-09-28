package com.empresa.invoice_service.models.dto.request;


import java.math.BigDecimal;

import com.empresa.invoice_service.models.entity.TimbradoFactura;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@JsonIgnoreProperties (ignoreUnknown = true)
public class TimbradoRequest {

    
    @NotBlank(message = "external_id es requerido")   
    @JsonProperty("external_id")
    private String externalId;  
    
    @NotNull(message = "invoice_id es requerido")
    @JsonProperty("invoice_id")
    private Long invoiceId; 
    
    @NotNull(message = "amount es requerido")
    @Positive(message = "amount debe ser una cantidad mayor a cero")
    @JsonProperty("amount")
    private BigDecimal amount;
 
    @JsonProperty("uuid_fiscal")
    private String uuidFiscal; 
    
    @JsonProperty("estatus")
    private String estatus; 

    public TimbradoFactura toTimbradoFacturasEntity() {
        TimbradoFactura timbradoFacturas = new TimbradoFactura( 
            this.invoiceId,
            this.externalId,
            this.amount,
            this.uuidFiscal,
            this.estatus,
            null,
            null
        );
        return timbradoFacturas;
    }  
}