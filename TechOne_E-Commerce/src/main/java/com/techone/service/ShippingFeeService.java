package com.techone.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.techone.model.District;
import com.techone.model.Ward;
import com.techone.repository.DistrictRepository;
import com.techone.repository.WardRepository;
import com.techone.request.GhnFeeRequest;
import com.techone.response.GhnFeeResponse;

@Service
public class ShippingFeeService {

    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private WardRepository wardRepository;

    @org.springframework.beans.factory.annotation.Value("${ghn.api.url}")
    private String GHN_API_URL;

    @org.springframework.beans.factory.annotation.Value("${ghn.api.token}")
    private String GHN_API_TOKEN;

    @org.springframework.beans.factory.annotation.Value("${ghn.shop.id}")
    private String SHOP_ID;

    @org.springframework.beans.factory.annotation.Value("${ghn.shop.district.id}")
    private Integer SHOP_DISTRICT_ID;

    // Cấu hình Hàng hóa cố định
    private final int FIXED_WEIGHT = 1000; // gram
    private final int FIXED_HEIGHT = 10;
    private final int FIXED_LENGTH = 10;
    private final int FIXED_WIDTH = 10;

    // Service Type ID = 2 (Chuẩn) hoặc 53320 (Chuyển phát thương mại điện tử) ->
    // Tùy shop cấu hình
    private final int SERVICE_TYPE_ID = 53320;

    public Integer calculateShippingFee(Integer provinceId, Integer districtId, String wardCode) {
        // 1. Lấy thông tin địa chỉ từ DB
        District district = districtRepository.findById(districtId)
                							  .orElseThrow(() -> new RuntimeException("Huyện không tồn tại"));
        Ward ward = wardRepository.findById(wardCode)
        						  .orElseThrow(() -> new RuntimeException("Xã không tồn tại"));

        // 2. Lấy Service Info khả dụng cho tuyến đường này
        ServiceInfo info = getAvailableServiceId(district.getId());
        if (info == null) {
            throw new RuntimeException("Không tìm thấy dịch vụ vận chuyển nào khả dụng cho tuyến đường này (GHN)");
        }

        // 3. Chuẩn bị Request Body & Gọi API (Thử 2 lần: service_id trước, sau đó
        // service_type_id)
        return tryCalculateFee(info, district.getId(), ward.getCode());
    }

    private Integer tryCalculateFee(ServiceInfo info, Integer toDistrictId, String toWardCode) {
        // Lần 1: Ưu tiên dùng service_id
        try {
            return callGhnFeeApi(info.serviceId, null, toDistrictId, toWardCode);
        } catch (Exception e) {
            if (e.getMessage().contains("route not found service") && info.serviceTypeId != null) {
                System.out.println("   -> service_id " + info.serviceId
                        + " failed with route error. Retrying with service_type_id " + info.serviceTypeId);
                try {
                    return callGhnFeeApi(null, info.serviceTypeId, toDistrictId, toWardCode);
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    private Integer callGhnFeeApi(Integer serviceId, Integer serviceTypeId, Integer toDistrictId, String toWardCode) {
        GhnFeeRequest request = new GhnFeeRequest();
        request.setService_id(serviceId);
        request.setService_type_id(serviceTypeId);
        request.setInsurance_value(0);
        request.setCoupon(null);
        request.setFrom_district_id(SHOP_DISTRICT_ID);
        request.setTo_district_id(toDistrictId);
        request.setTo_ward_code(toWardCode);
        request.setHeight(FIXED_HEIGHT);
        request.setLength(FIXED_LENGTH);
        request.setWeight(FIXED_WEIGHT);
        request.setWidth(FIXED_WIDTH);
        try {
            request.setShop_id(Integer.parseInt(SHOP_ID));
        } catch (Exception e) {
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_API_TOKEN);
        headers.set("ShopId", SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<GhnFeeRequest> entity = new HttpEntity<>(request, headers);

        System.out.println("Calling GHN Fee API with parameters: service_id=" + serviceId + ", service_type_id="
                + serviceTypeId + ", to_ward_code=" + toWardCode);

        try {
            ResponseEntity<GhnFeeResponse> response = restTemplate.exchange(GHN_API_URL, HttpMethod.POST, entity,
                    GhnFeeResponse.class);
            if (response.getBody() != null && response.getBody().getCode() == 200) {
                return response.getBody().getData().getTotal();
            } else {
                String msg = (response.getBody() != null ? response.getBody().getMessage() : "Unknown error");
                throw new RuntimeException("GHN Error: " + msg);
            }
        } catch (HttpClientErrorException e) {
            System.err.println("GHN API Fee HTTP Error: " + e.getResponseBodyAsString());
            throw new RuntimeException("GHN API Error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ServiceInfo getAvailableServiceId(Integer toDistrictId) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/available-services";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", GHN_API_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        try {
            body.put("shop_id", Integer.parseInt(SHOP_ID));
        } catch (Exception e) {
        }
        body.put("from_district", SHOP_DISTRICT_ID);
        body.put("to_district", toDistrictId);

        System.out.println("Calling GHN Available Services: " + url + " with body: " + body);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            System.out.println("GHN Available Services Response: " + response.getBody());

            if (response.getBody() != null && response.getBody().get("code") != null) {
                int code = ((Number) response.getBody().get("code")).intValue();
                if (code == 200) {
                    List<Map<String, Object>> services = (List<Map<String, Object>>) response.getBody().get("data");
                    if (services != null && !services.isEmpty()) {
                        for (Map<String, Object> service : services) {
                            Integer sId = ((Number) service.get("service_id")).intValue();
                            if (sId != null && (sId == 53320 || sId == 53321)) {
                                Integer stId = service.get("service_type_id") != null
                                        ? ((Number) service.get("service_type_id")).intValue()
                                        : null;
                                return new ServiceInfo(sId, stId);
                            }
                        }
                        Map<String, Object> first = services.get(0);
                        return new ServiceInfo(((Number) first.get("service_id")).intValue(),
                                first.get("service_type_id") != null
                                        ? ((Number) first.get("service_type_id")).intValue()
                                        : null);
                    }
                } else {
                    System.err.println("GHN Available Services Error: " + response.getBody().get("message"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching available services: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static class ServiceInfo {
        Integer serviceId;
        Integer serviceTypeId;

        ServiceInfo(Integer serviceId, Integer serviceTypeId) {
            this.serviceId = serviceId;
            this.serviceTypeId = serviceTypeId;
        }
    }
}
