package com.ryannoah.auction.presentationlayer;

import com.ryannoah.auction.businesslogiclayer.InvoiceApplicationService;
import com.ryannoah.auction.datamappinglayer.InvoiceMapper;
import com.ryannoah.auction.presentationlayer.dto.CreateInvoiceRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.InvoiceResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateInvoiceRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceApplicationService invoiceApplicationService;
    private final InvoiceMapper invoiceMapper;

    public InvoiceController(InvoiceApplicationService invoiceApplicationService, InvoiceMapper invoiceMapper) {
        this.invoiceApplicationService = invoiceApplicationService;
        this.invoiceMapper = invoiceMapper;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody CreateInvoiceRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceMapper.toResponseDTO(invoiceApplicationService.createInvoice(invoiceMapper.toCreateCommand(request))));
    }

    @PutMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(@PathVariable String invoiceId, @Valid @RequestBody UpdateInvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceMapper.toResponseDTO(invoiceApplicationService.updateInvoice(invoiceId, invoiceMapper.toUpdateCommand(request))));
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable String invoiceId) {
        invoiceApplicationService.deleteInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{invoiceId}/pay")
    public ResponseEntity<InvoiceResponseDTO> payInvoice(@PathVariable String invoiceId) {
        return ResponseEntity.ok(invoiceMapper.toResponseDTO(invoiceApplicationService.payInvoice(invoiceId)));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponseDTO> getInvoice(@PathVariable String invoiceId) {
        return ResponseEntity.ok(invoiceMapper.toResponseDTO(invoiceApplicationService.getInvoice(invoiceId)));
    }

    @GetMapping
    public ResponseEntity<java.util.List<InvoiceResponseDTO>> listInvoices() {
        return ResponseEntity.ok(invoiceApplicationService.listInvoices().stream().map(invoiceMapper::toResponseDTO).toList());
    }
}
