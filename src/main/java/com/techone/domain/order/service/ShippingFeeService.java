package com.techone.domain.order.service;

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
import com.techone.domain.user.entity.District;
import com.techone.domain.user.entity.Ward;
import com.techone.domain.user.repository.DistrictRepository;
import com.techone.domain.user.repository.WardRepository;
import com.techone.request.GhnFeeRequest;
import com.techone.common.response.GhnFeeResponse;

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

    private final int FIXED_WEIGHT = 1000;
    private final int FIXED_HEIGHT = 10;
    private final int FIXED_LENGTH = 10;
    private final int FIXED_WIDTH = 10;

    public Integer calculateShippingFee(Integer provinceId, Integer districtId, String wardCode) {
        try {
            District district = districtRepository.findById(districtId)
                    .orElseThrow(() -> new RuntimeException("Huyện không tồn tại"));
            Ward ward = wardRepository.findById(wardCode).orElseThrow(() -> new RuntimeException("Xã không tồn tại"));
            ServiceInfo info = getAvailableServiceId(district.getId());
            if (info == null)
                return 50000;
            return tryCalculateFee(info, district.getId(), ward.getCode());
        } catch (Exception e) {
            return 50000;
        }
    }

    private Integer tryCalculateFee(ServiceInfo info, Integer toDistrictId, String toWardCode) {
        try {
            return callGhnFeeApi(info.serviceId, null, toDistrictId, toWardCode);
        } catch (Exception e) {
            if (e.getMessage().contains("route not found service") && info.serviceTypeId != null) {
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

        try {
            ResponseEntity<GhnFeeResponse> response = restTemplate.exchange(GHN_API_URL, HttpMethod.POST, entity,
                    GhnFeeResponse.class);
            if (response.getBody() != null && response.getBody().getCode() == 200) {
                return response.getBody().getData().getTotal();
            } else {
                throw new RuntimeException("GHN Error: "
                        + (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
            }
        } catch (HttpClientErrorException e) {
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
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
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
                }
            }
        } catch (Exception e) {
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

