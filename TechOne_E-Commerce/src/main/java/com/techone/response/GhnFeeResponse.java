package com.techone.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GhnFeeResponse {
    private int code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private int total;
        private int service_fee;
        private int insurance_fee;
        private int pick_station_fee;
        private int coupon_value;
        private int r2s_fee;
    }
}
