package com.empresa.invoice_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.invoice_service.models.entity.TimbradoFactura;


public interface FacturaTimbrarRepository extends JpaRepository<TimbradoFactura, Long> {

    Optional<TimbradoFactura> findByInvoiceId(String invoiceId);

    Optional<TimbradoFactura> findByUuidFiscal(String uuidFiscal);

    Optional<TimbradoFactura> findByExternalId(String externalId);

    Optional<TimbradoFactura> findByInvoiceId(Long invoiceId);
}