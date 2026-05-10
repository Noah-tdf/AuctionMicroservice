package com.ryannoah.auction.datamappinglayer;

import com.ryannoah.auction.businesslogiclayer.InvoiceApplicationService;
import com.ryannoah.auction.domain.Invoice;
import com.ryannoah.auction.presentationlayer.dto.CreateInvoiceRequestDTO;
import com.ryannoah.auction.presentationlayer.dto.InvoiceResponseDTO;
import com.ryannoah.auction.presentationlayer.dto.UpdateInvoiceRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceApplicationService.CreateInvoiceCommand toCreateCommand(CreateInvoiceRequestDTO request) {
        return new InvoiceApplicationService.CreateInvoiceCommand(
                request.auctionId(),
                request.buyerId(),
                request.sellerId(),
                request.dueDate(),
                request.finalSaleAmount(),
                request.currency(),
                request.method()
        );
    }

    public InvoiceApplicationService.UpdateInvoiceCommand toUpdateCommand(UpdateInvoiceRequestDTO request) {
        return new InvoiceApplicationService.UpdateInvoiceCommand(
                request.dueDate(),
                request.finalSaleAmount(),
                request.currency(),
                request.method()
        );
    }

    public InvoiceResponseDTO toResponseDTO(Invoice invoice) {
        return new InvoiceResponseDTO(
                invoice.getInvoiceId().value(),
                invoice.getAuctionId().value(),
                invoice.getBuyerId().value(),
                invoice.getSellerId().value(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getFinalSaleAmount().amount(),
                invoice.getFinalSaleAmount().currency(),
                invoice.getStatus().name(),
                invoice.getMethod().name()
        );
    }
}
