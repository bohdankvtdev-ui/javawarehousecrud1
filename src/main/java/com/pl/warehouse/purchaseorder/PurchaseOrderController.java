package com.pl.warehouse.purchaseorder;

import com.pl.warehouse.purchaseorder.dto.PurchaseOrderRequest;
import com.pl.warehouse.purchaseorder.dto.PurchaseOrderResponse;
import com.pl.warehouse.purchaseorder.dto.ReceivePurchaseOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * STUDY — Purchase Order HTTP API (create → submit → receive).
 *
 * <pre>
 * GET    /api/v1/purchase-orders
 * GET    /api/v1/purchase-orders/{id}
 * POST   /api/v1/purchase-orders
 * POST   /api/v1/purchase-orders/{id}/submit
 * POST   /api/v1/purchase-orders/{id}/receive
 * POST   /api/v1/purchase-orders/{id}/cancel
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public List<PurchaseOrderResponse> list() {
        return purchaseOrderService.findAll();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable UUID id) {
        return purchaseOrderService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.create(request);
    }

    @PostMapping("/{id}/submit")
    public PurchaseOrderResponse submit(@PathVariable UUID id) {
        return purchaseOrderService.submit(id);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse receive(
            @PathVariable UUID id,
            @Valid @RequestBody ReceivePurchaseOrderRequest request
    ) {
        return purchaseOrderService.receive(id, request);
    }

    @PostMapping("/{id}/cancel")
    public PurchaseOrderResponse cancel(@PathVariable UUID id) {
        return purchaseOrderService.cancel(id);
    }
}
