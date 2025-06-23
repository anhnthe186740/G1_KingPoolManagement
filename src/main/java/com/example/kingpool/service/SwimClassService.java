package com.example.kingpool.service;

import com.example.kingpool.entity.ClassSchedule;
import com.example.kingpool.entity.SwimClass;
import com.example.kingpool.entity.SwimClassRegistration;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.ClassScheduleRepository;
import com.example.kingpool.repository.SwimClassRegistrationRepository;
import com.example.kingpool.repository.SwimClassRepository;
import com.example.kingpool.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class SwimClassService {

    @Autowired
    private SwimClassRepository swimClassRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private SwimClassRegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<SwimClass> getAllSwimClasses() {
        return swimClassRepository.findAll();
    }

    public Page<SwimClass> getAllSwimClasses(Pageable pageable) {
        return swimClassRepository.findAll(pageable);
    }

    public List<SwimClass> getSwimClassesByStatus(String status) {
        return swimClassRepository.findByStatus(status);
    }

    public Page<SwimClass> getSwimClassesByStatus(String status, Pageable pageable) {
        return swimClassRepository.findByStatus(status, pageable);
    }

    public List<SwimClass> getSwimClassesByDate(LocalDate date) {
        List<ClassSchedule> schedules = classScheduleRepository.findByStartTimeBetween(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());

        List<SwimClass> result = new ArrayList<>();
        for (ClassSchedule schedule : schedules) {
            if (!result.contains(schedule.getSwimClass())) {
                result.add(schedule.getSwimClass());
            }
        }
        return result;
    }

    public List<SwimClass> getUserClasses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Học viên không tồn tại."));
        List<SwimClassRegistration> registrations = registrationRepository.findByUserUserId(user.getUserId());
        List<SwimClass> userClasses = new ArrayList<>();
        for (SwimClassRegistration reg : registrations) {
            if ("CONFIRMED".equalsIgnoreCase(reg.getStatus())) {
                SwimClass swimClass = swimClassRepository.findByIdWithSchedules(reg.getSwimClass().getClassId())
                        .orElse(null);
                if (swimClass != null && !userClasses.contains(swimClass)) {
                    userClasses.add(swimClass);
                }
            }
        }
        return userClasses;
    }

    public SwimClass getSwimClassById(Integer id) {
        return swimClassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));
    }

    public SwimClass getSwimClassWithSchedules(Integer id) {
        return swimClassRepository.findByIdWithSchedules(id)
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));
    }

    @Transactional
    public SwimClass createSwimClassWithSchedules(
            String name,
            User coach,
            String level,
            int maxStudents,
            String description,
            List<DayOfWeek> studyDays,
            LocalDate startDate,
            int durationWeeks,
            LocalTime classTime,
            Duration duration) {
        SwimClass swimClass = new SwimClass();
        swimClass.setName(name);
        swimClass.setCoach(coach);
        swimClass.setLevel(level);
        swimClass.setMaxStudents(maxStudents);
        swimClass.setCurrentStudents(0);
        swimClass.setDescription(description);
        swimClass.setStatus("MỞ");

        List<ClassSchedule> schedules = generateSchedules(studyDays, startDate, durationWeeks, classTime, duration);
        schedules.forEach(schedule -> schedule.setSwimClass(swimClass));
        swimClass.setSchedules(schedules);

        return swimClassRepository.save(swimClass);
    }

    public List<ClassSchedule> generateSchedules(
            List<DayOfWeek> studyDays,
            LocalDate startDate,
            int durationWeeks,
            LocalTime classTime,
            Duration duration) {
        List<ClassSchedule> result = new ArrayList<>();
        LocalDate endDate = startDate.plusWeeks(durationWeeks);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (studyDays.contains(date.getDayOfWeek())) {
                LocalDateTime start = LocalDateTime.of(date, classTime);
                LocalDateTime end = start.plus(duration);

                ClassSchedule schedule = new ClassSchedule();
                schedule.setStartTime(start);
                schedule.setEndTime(end);
                schedule.setDescription("" + date.getDayOfWeek());
                schedule.setStatus("CHUA_HOC");

                result.add(schedule);
            }
        }

        return result;
    }

    @Transactional
    public SwimClass updateSwimClassWithSchedules(
            Integer classId,
            String name,
            User coach,
            String level,
            int maxStudents,
            String description,
            List<DayOfWeek> studyDays,
            LocalDate startDate,
            int durationWeeks,
            LocalTime classTime,
            Duration duration) {

        SwimClass existingClass = swimClassRepository.findByIdWithSchedules(classId)
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));

        existingClass.setName(name);
        existingClass.setCoach(coach);
        existingClass.setLevel(level);
        existingClass.setMaxStudents(maxStudents);
        existingClass.setDescription(description);

        classScheduleRepository.deleteAll(existingClass.getSchedules());

        List<ClassSchedule> newSchedules = generateSchedules(studyDays, startDate, durationWeeks, classTime, duration);
        newSchedules.forEach(schedule -> schedule.setSwimClass(existingClass));
        existingClass.setSchedules(newSchedules);

        return swimClassRepository.save(existingClass);
    }

    @Transactional
    public void deleteSwimClass(Integer id) {
        SwimClass swimClass = swimClassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lớp học không tồn tại"));

        registrationRepository.deleteBySwimClassClassId(id);
        classScheduleRepository.deleteAll(swimClass.getSchedules());
        swimClassRepository.delete(swimClass);
    }

    @Transactional
    public void updateStatus(Integer classId, String newStatus) {
        SwimClass swimClass = swimClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học"));
        swimClass.setStatus(newStatus);
        swimClassRepository.save(swimClass);
    }
}