package com.techone.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.Address;
import com.techone.domain.user.repository.AddressRepository;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public List<Address> findAddressesByAccount(Account account) {
        return addressRepository.findByAccountIdOrderByStatusDesc(account.getId());
    }

    @Transactional
    public Address save(Address address, Account account) {
        address.setAccount(account);
        if (address.getId() == null) {
            address.setCreateAt(LocalDateTime.now());
        } else {
            addressRepository.findById(address.getId()).ifPresent(existing -> {
                address.setCreateAt(existing.getCreateAt());
            });
        }

        List<Address> existingAddresses = addressRepository.findByAccountIdOrderByStatusDesc(account.getId());
        if (existingAddresses.isEmpty()) {
            address.setStatus(true);
        } else if (address.getStatus() != null && address.getStatus()) {
            Optional<Address> currentDefault = addressRepository.findByAccountIdAndStatusTrue(account.getId());
            currentDefault.ifPresent(d -> {
                if (address.getId() == null || !d.getId().equals(address.getId())) {
                    d.setStatus(false);
                    addressRepository.save(d);
                }
            });
        } else if (address.getId() == null) {
            address.setStatus(false);
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void delete(Integer id, Account account) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null && address.getAccount().getId().equals(account.getId())) {
            addressRepository.delete(address);
        }
    }
}
