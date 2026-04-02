package com.techone.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.techone.model.District;
import com.techone.model.Province;
import com.techone.model.Ward;
import com.techone.repository.DistrictRepository;
import com.techone.repository.ProvinceRepository;
import com.techone.repository.WardRepository;

@RestController
@RequestMapping("/api/address")
public class AddressRestController {

    @Autowired
    private ProvinceRepository provinceRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private WardRepository wardRepository;

    @GetMapping("/provinces")
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    @GetMapping("/districts")
    public List<District> getDistrictsByProvince(@RequestParam Integer provinceId) {
        return districtRepository.findByProvinceId(provinceId);
    }

    @GetMapping("/wards")
    public List<Ward> getWardsByDistrict(@RequestParam Integer districtId) {
        return wardRepository.findByDistrictId(districtId);
    }
}
