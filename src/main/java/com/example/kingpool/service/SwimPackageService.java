package com.example.kingpool.service;

import com.example.kingpool.entity.SwimPackages;
import com.example.kingpool.repository.SwimPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SwimPackageService {

    @Autowired
    private SwimPackageRepository swimPackageRepository;

    public List<SwimPackages> getAllPackages() {
        return swimPackageRepository.findAll();
    }

    public Optional<SwimPackages> getPackageById(Long id) {
        return swimPackageRepository.findById(id);
    }
}