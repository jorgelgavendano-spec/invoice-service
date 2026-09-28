package com.empresa.invoice_service.models.dto.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data 
@RequiredArgsConstructor 
@JsonIgnoreProperties (ignoreUnknown = true)
public class PacRequest {

    public PacRequest(String externalId, Long invoiceId, BigDecimal amount, String cfdiXml) {
        this.externalId = externalId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.cfdiXml = cfdiXml;
    }
    @JsonProperty("external_id")
    private String externalId;  
    
    @JsonProperty("invoice_id")
    private Long invoiceId; 
    
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("cfdi_xml")
    private String cfdiXml; 

}