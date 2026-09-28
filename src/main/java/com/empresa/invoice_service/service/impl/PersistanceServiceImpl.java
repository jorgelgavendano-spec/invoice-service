package com.empresa.invoice_service.service.impl;


import org.springframework.stereotype.Service;
import com.empresa.invoice_service.models.entity.TimbradoFactura;
import com.empresa.invoice_service.repository.FacturaTimbrarRepository;
import com.empresa.invoice_service.service.PersistanceService;


@Service
public class PersistanceServiceImpl implements PersistanceService {

        private final FacturaTimbrarRepository timbradoFacturasRepository;

        PersistanceServiceImpl(FacturaTimbrarRepository timbradoFacturasRepository) {
                this.timbradoFacturasRepository = timbradoFacturasRepository;
        }
        
        @Override 
        public TimbradoFactura guardar(TimbradoFactura factura) {
                return timbradoFacturasRepository.save(factura);
        }

        @Override
        public TimbradoFactura buscarFacturaExternalId(String externalId) {
                return timbradoFacturasRepository.findByExternalId(externalId).orElse(null);
        }

        @Override 
        public TimbradoFactura buscarFacturaInvoiceId(Long invoiceId){
                return timbradoFacturasRepository.findByInvoiceId(invoiceId).orElse(null);
        }
}
