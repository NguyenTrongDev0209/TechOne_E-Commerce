package com.techone.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techone.domain.order.service.ShippingFeeService;

@RestController
@RequestMapping("/api/shipping-fee")
public class ShippingFeeRestController {

    @Autowired
    private ShippingFeeService shippingFeeService;

    @GetMapping
    public ResponseEntity<?> getShippingFee(
            @RequestParam("districtId") Integer districtId,
            @RequestParam("wardCode") String wardCode) {
        try {
            // Province ID is not strictly needed for the calculation as District -> Ward
            // dictates route,
            // passing null or dummy if the service allows, but actually the service
            // signature needs it.
            // Let's pass null for provinceId as it's not actually used inside
            // calculateShippingFee
            Integer fee = shippingFeeService.calculateShippingFee(null, districtId, wardCode);
            return ResponseEntity.ok(fee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

