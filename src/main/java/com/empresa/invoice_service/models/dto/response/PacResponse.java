package com.empresa.invoice_service.models.dto.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data   
@NoArgsConstructor 
@AllArgsConstructor 
@JsonIgnoreProperties (ignoreUnknown = true)
public class PacResponse  {

    private HttpStatus httpCode;
    
    @JsonProperty("success")
    private boolean success;  

    @JsonProperty("external_id")
    private String externalId; 

    @JsonProperty("uuid_fiscal")
    private String uuidFiscal; 

    @JsonProperty("error")
    private String error;  

}
