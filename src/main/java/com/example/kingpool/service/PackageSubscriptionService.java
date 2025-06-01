package com.example.kingpool.service;

import com.example.kingpool.entity.PackageSubscriptions;
import com.example.kingpool.entity.Schedule;
import com.example.kingpool.repository.PackageSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageSubscriptionService {

    @Autowired
    private PackageSubscriptionRepository packageSubscriptionRepository;

    public List<Schedule> getUserSchedules(Long userId) {
        List<PackageSubscriptions> subscriptions = packageSubscriptionRepository.findByUserUserId(userId);
        return subscriptions.stream()
                .map(PackageSubscriptions::getSchedule)
                .filter(schedule -> schedule != null)
                .collect(Collectors.toList());
    }

    public void saveSubscription(PackageSubscriptions subscription) {
        packageSubscriptionRepository.save(subscription);
    }
}