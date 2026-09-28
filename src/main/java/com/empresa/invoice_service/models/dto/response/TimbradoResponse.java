package com.empresa.invoice_service.models.dto.response;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data   
@NoArgsConstructor 
@AllArgsConstructor 
@JsonIgnoreProperties (ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimbradoResponse  {
    
    @JsonIgnore
    private HttpStatus httpCode;
    
    @JsonProperty("status")
    private String status;  

    @JsonProperty("external_id")
    private String externalId; 

    @JsonProperty("uuid_fiscal")
    private String uuidFiscal; 
    
    @JsonProperty("error")
    private String error; 

    @JsonProperty("mensaje")
    private String mensaje; 
}
