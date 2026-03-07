package com.techone.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.techone.model.Account;
import com.techone.model.Address;
import com.techone.repository.AddressRepository;

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
            // Preserve createAt from existing address
            addressRepository.findById(address.getId()).ifPresent(existing -> {
                address.setCreateAt(existing.getCreateAt());
            });
        }

        // If this is the first address, make it default
        List<Address> existingAddresses = addressRepository.findByAccountIdOrderByStatusDesc(account.getId());
        if (existingAddresses.isEmpty()) {
            address.setStatus(true);
        } else if (address.getStatus() != null && address.getStatus()) {
            // If setting as default, unmark other default addresses
            Optional<Address> currentDefault = addressRepository.findByAccountIdAndStatusTrue(account.getId());
            currentDefault.ifPresent(d -> {
                if (address.getId() == null || !d.getId().equals(address.getId())) {
                    d.setStatus(false);
                    addressRepository.save(d);
                }
            });
        } else if (address.getId() == null) {
            // New non-default address
            address.setStatus(false);
        }
        // If it's an update and status is null, we should keep the current status
        // but looking at the form, status is always sent.

        return addressRepository.save(address);
    }

    @Transactional
    public void delete(Integer id, Account account) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null && address.getAccount().getId().equals(account.getId())) {
            // If deleting default address, and there are other addresses, pick another one
            // to be default?
            // For now, just delete.
            addressRepository.delete(address);
        }
    }
}
