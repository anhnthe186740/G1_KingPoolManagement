package com.example.kingpool.service;

import com.example.kingpool.entity.SwimClass;
import com.example.kingpool.entity.SwimClassRegistration;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.SwimClassRegistrationRepository;
import com.example.kingpool.repository.SwimClassRepository;
import com.example.kingpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SwimClassRegistrationService {

    @Autowired
    private SwimClassRegistrationRepository registrationRepository;

    @Autowired
    private SwimClassRepository swimClassRepository;

    @Autowired
    private UserRepository userRepository;

    public List<SwimClassRegistration> getRegistrationsByClassId(Integer classId) {
        return registrationRepository.findBySwimClassClassId(classId);
    }

    @Transactional
    public SwimClassRegistration confirmRegistration(Integer registrationId) {
        SwimClassRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Đăng ký không tồn tại"));

        if ("Confirmed".equalsIgnoreCase(registration.getStatus())) {
            throw new IllegalStateException("Đăng ký đã được xác nhận.");
        }

        SwimClass swimClass = swimClassRepository.findById(registration.getSwimClass().getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));

        if (swimClass.getCurrentStudents() >= swimClass.getMaxStudents()) {
            throw new IllegalStateException("Lớp học đã đủ học viên.");
        }

        registration.setStatus("CONFIRMED");
        swimClass.setCurrentStudents(swimClass.getCurrentStudents() + 1);
        swimClassRepository.save(swimClass);
        return registrationRepository.save(registration);
    }

    @Transactional
    public SwimClassRegistration cancelRegistration(Integer registrationId, String cancelReason) {
        SwimClassRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Đăng ký không tồn tại"));

        if ("Cancelled".equalsIgnoreCase(registration.getStatus())) {
            throw new IllegalStateException("Đăng ký đã bị hủy.");
        }

        SwimClass swimClass = swimClassRepository.findById(registration.getSwimClass().getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));

        if ("Confirmed".equalsIgnoreCase(registration.getStatus())) {
            swimClass.setCurrentStudents(swimClass.getCurrentStudents() - 1);
            swimClassRepository.save(swimClass);
        }

        registration.setStatus("CANCELLED");
        registration.setCancelReason(cancelReason);
        return registrationRepository.save(registration);
    }

    @Transactional
    public void addStudentToClass(Integer classId, Integer studentId) {
        SwimClass swimClass = swimClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại."));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Học viên không tồn tại."));

        List<SwimClassRegistration> existingRegistrations = registrationRepository.findByUserUserId(studentId);

        for (SwimClassRegistration reg : existingRegistrations) {
            if (reg.getSwimClass().getClassId().equals(swimClass.getClassId())) {
                throw new IllegalStateException("Học viên đã đăng ký lớp này.");
            }
        }

        if (swimClass.getCurrentStudents() >= swimClass.getMaxStudents()) {
            throw new IllegalStateException("Lớp đã đủ học viên.");
        }

        SwimClassRegistration registration = new SwimClassRegistration();
        registration.setSwimClass(swimClass);
        registration.setUser(student);
        registration.setStatus("CONFIRMED");
        registration.setRegistrationDate(LocalDateTime.now());

        registrationRepository.save(registration);

        swimClass.setCurrentStudents(swimClass.getCurrentStudents() + 1);
        swimClassRepository.save(swimClass);
    }

    public List<SwimClassRegistration> getConfirmedRegistrationsByClassId(Integer classId) {
        return registrationRepository.findBySwimClass_ClassIdAndStatus(classId, "CONFIRMED");
    }
}
