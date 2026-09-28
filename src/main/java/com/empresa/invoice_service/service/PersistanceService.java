package com.empresa.invoice_service.service;


import com.empresa.invoice_service.models.entity.TimbradoFactura;


public interface PersistanceService {
       public TimbradoFactura guardar(TimbradoFactura factura);

       public TimbradoFactura buscarFacturaExternalId(String externalId);
       
       public TimbradoFactura buscarFacturaInvoiceId(Long invoiceId);
}
