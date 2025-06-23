package com.example.kingpool.service;

import com.example.kingpool.entity.ClassSchedule;
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

        if ("CONFIRMED".equalsIgnoreCase(registration.getStatus())) {
            throw new IllegalStateException("Đăng ký đã được xác nhận.");
        }

        SwimClass swimClass = swimClassRepository.findByIdWithSchedules(registration.getSwimClass().getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));

        if (swimClass.getCurrentStudents() >= swimClass.getMaxStudents()) {
            throw new IllegalStateException("Lớp học đã đủ học viên.");
        }

        User student = registration.getUser();
        checkScheduleConflict(student.getUserId(), swimClass.getSchedules(), registration.getRegistrationId());

        registration.setStatus("CONFIRMED");
        swimClass.setCurrentStudents(swimClass.getCurrentStudents() + 1);
        swimClassRepository.save(swimClass);
        return registrationRepository.save(registration);
    }

    @Transactional
    public void cancelRegistration(Integer registrationId, String cancelReason) {
        SwimClassRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Đăng ký không tồn tại"));

        if (!"PENDING".equalsIgnoreCase(registration.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hủy đăng ký ở trạng thái PENDING.");
        }

        registrationRepository.delete(registration);
    }

    @Transactional
    public void addStudentToClass(Integer classId, Integer studentId) {
        SwimClass swimClass = swimClassRepository.findByIdWithSchedules(classId)
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại."));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Học viên không tồn tại."));

        if (registrationRepository.existsBySwimClass_ClassIdAndUser_UserId(classId, studentId)) {
            throw new IllegalStateException("Học viên đã đăng ký lớp này.");
        }

        if (swimClass.getCurrentStudents() >= swimClass.getMaxStudents()) {
            throw new IllegalStateException("Lớp đã đủ học viên.");
        }

        checkScheduleConflict(studentId, swimClass.getSchedules(), null);

        SwimClassRegistration registration = new SwimClassRegistration();
        registration.setSwimClass(swimClass);
        registration.setUser(student);
        registration.setStatus("CONFIRMED");
        registration.setRegistrationDate(LocalDateTime.now());

        registrationRepository.save(registration);

        swimClass.setCurrentStudents(swimClass.getCurrentStudents() + 1);
        swimClassRepository.save(swimClass);
    }

    @Transactional
    public void removeStudentFromClass(Integer registrationId, String removeReason) {
        SwimClassRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Đăng ký không tồn tại"));

        if (!"CONFIRMED".equalsIgnoreCase(registration.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xóa học viên ở trạng thái CONFIRMED.");
        }

        SwimClass swimClass = swimClassRepository.findById(registration.getSwimClass().getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));

        int currentStudents = swimClass.getCurrentStudents() > 0 ? swimClass.getCurrentStudents() - 1 : 0;
        swimClass.setCurrentStudents(currentStudents);
        swimClassRepository.save(swimClass);

        registrationRepository.delete(registration);
    }

    @Transactional
    public void registerClass(Integer classId, String username) {
        SwimClass swimClass = swimClassRepository.findByIdWithSchedules(classId)
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại."));

        if (!"MỞ".equalsIgnoreCase(swimClass.getStatus())) {
            throw new IllegalStateException("Chỉ có thể đăng ký vào lớp học có trạng thái MỞ.");
        }

        if (swimClass.getCurrentStudents() >= swimClass.getMaxStudents()) {
            throw new IllegalStateException("Lớp học đã đủ học viên.");
        }

        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Học viên không tồn tại."));

        if (registrationRepository.existsBySwimClass_ClassIdAndUser_UserId(classId, student.getUserId())) {
            throw new IllegalStateException("Học viên đã đăng ký lớp này.");
        }

        checkScheduleConflict(student.getUserId(), swimClass.getSchedules(), null);

        SwimClassRegistration registration = new SwimClassRegistration();
        registration.setSwimClass(swimClass);
        registration.setUser(student);
        registration.setStatus("PENDING");
        registration.setRegistrationDate(LocalDateTime.now());

        registrationRepository.save(registration);
    }

    public List<SwimClassRegistration> getConfirmedRegistrationsByClassId(Integer classId) {
        return registrationRepository.findBySwimClass_ClassIdAndStatus(classId, "CONFIRMED");
    }

    private void checkScheduleConflict(Integer userId, List<ClassSchedule> newSchedules, Integer currentRegistrationId) {
        List<SwimClassRegistration> existingRegistrations = registrationRepository.findByUserUserId(userId);
        for (SwimClassRegistration reg : existingRegistrations) {
            if (currentRegistrationId != null && reg.getRegistrationId().equals(currentRegistrationId)) {
                continue;
            }
            if (!"CONFIRMED".equalsIgnoreCase(reg.getStatus())) {
                continue;
            }
            SwimClass registeredClass = swimClassRepository.findByIdWithSchedules(reg.getSwimClass().getClassId())
                    .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));
            for (ClassSchedule newSchedule : newSchedules) {
                for (ClassSchedule existingSchedule : registeredClass.getSchedules()) {
                    if (isScheduleConflict(newSchedule, existingSchedule)) {
                        throw new IllegalStateException("Học viên có lịch học trùng với lớp " + registeredClass.getName());
                    }
                }
            }
        }
    }

    private boolean isScheduleConflict(ClassSchedule newSchedule, ClassSchedule existingSchedule) {
        LocalDateTime newStart = newSchedule.getStartTime();
        LocalDateTime newEnd = newSchedule.getEndTime();
        LocalDateTime existingStart = existingSchedule.getStartTime();
        LocalDateTime existingEnd = existingSchedule.getEndTime();

        return newStart.toLocalDate().equals(existingStart.toLocalDate()) &&
               !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));
    }
}