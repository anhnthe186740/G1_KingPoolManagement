package com.example.kingpool.controller;

import com.example.kingpool.entity.Attendance;
import com.example.kingpool.entity.ClassSchedule;
import com.example.kingpool.entity.SwimClass;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.AttendanceRepository;
import com.example.kingpool.service.SwimClassRegistrationService;
import com.example.kingpool.service.SwimClassService;
import com.example.kingpool.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/coach")
@PreAuthorize("hasRole('COACH')")
public class CoachSwimClassController {

    private static final Logger log = LoggerFactory.getLogger(CoachSwimClassController.class);

    @Autowired
    private SwimClassService swimClassService;

    @Autowired
    private SwimClassRegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @GetMapping
    public String coachClasses(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User coach = userService.getUserByUsername(username);
            List<SwimClass> coachClasses = swimClassService.getClassesByCoachId(coach.getUserId());
            model.addAttribute("coachClasses", coachClasses);
            return "coach/classes";
        } catch (Exception e) {
            log.error("Lỗi khi tải danh sách lớp của coach: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách lớp: " + e.getMessage());
            return "coach/classes";
        }
    }

    @GetMapping("/schedules")
    @Transactional(readOnly = true)
    public String viewCoachSchedules(@RequestParam(value = "week", required = false) String selectedWeek,
            Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User coach = userService.getUserByUsername(username);

            int weekOffset = 0;
            try {
                weekOffset = selectedWeek != null ? Integer.parseInt(selectedWeek) : 0;
                List<SwimClass> coachClasses = swimClassService.getClassesByCoachId(coach.getUserId());
                int maxWeeks = calculateMaxWeeks(coachClasses);
                int minOffset = -2;
                int maxOffset = maxWeeks + 2;
                if (weekOffset < minOffset || weekOffset > maxOffset)
                    weekOffset = 0;
            } catch (NumberFormatException e) {
                log.warn("Invalid weekOffset, defaulting to 0: {}", e.getMessage());
                weekOffset = 0;
            }

            List<SwimClass> coachClasses = swimClassService.getClassesByCoachId(coach.getUserId());
            List<ClassSchedule> allSchedules = coachClasses.stream()
                    .flatMap(c -> c.getSchedules().stream())
                    .collect(Collectors.toList());

            LocalDate baseMonday = LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate startOfWeek = baseMonday.plusWeeks(weekOffset);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            LocalDateTime now = LocalDateTime.now();

            List<String> daysOfWeek = IntStream.range(0, 7)
                    .mapToObj(i -> {
                        LocalDate date = startOfWeek.plusDays(i);
                        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                                Locale.forLanguageTag("vi-VN")) + " - " + date.format(DATE_FORMATTER);
                    })
                    .collect(Collectors.toList());

            List<ClassSchedule> schedulesInWeek = coachClasses.stream()
                    .flatMap(c -> c.getSchedules().stream())
                    .filter(s -> {
                        LocalDate d = s.getStartTime().toLocalDate();
                        return !d.isBefore(startOfWeek) && !d.isAfter(endOfWeek);
                    })
                    .collect(Collectors.toList());

            List<Attendance> attendances = attendanceRepository.findByClassScheduleIdsAndUserId(
                    schedulesInWeek.stream().map(ClassSchedule::getClassScheduleId).collect(Collectors.toList()),
                    coach.getUserId());

            // ✅ Sửa: dùng HH:mm thay vì chỉ HH:00 để đồng bộ với key
            List<String> hourList = schedulesInWeek.stream()
                    .map(s -> s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .distinct()
                    .sorted()
                    .toList();
            List<Map<String, String>> timeSlots = hourList.stream()
                    .map(h -> Map.of("label", h, "value", h))
                    .collect(Collectors.toList());

            Map<String, List<Map<String, String>>> scheduleMap = new HashMap<>();
            for (ClassSchedule s : schedulesInWeek) {
                String day = s.getStartTime().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                        Locale.forLanguageTag("vi-VN")) + " - " + s.getStartTime().toLocalDate().format(DATE_FORMATTER);
                String hourStart = s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                String hourEnd = s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                String key = day + "_" + hourStart;

                List<User> students = registrationService.getRegisteredStudents(s.getSwimClass().getClassId());
                String studentsData = students.isEmpty() ? ""
                        : students.stream()
                                .map(student -> String.format("stt=%d,name=%s,dob=%s,email=%s,phone=%s",
                                        students.indexOf(student) + 1,
                                        student.getName(),
                                        student.getDateOfBirth() != null
                                                ? student.getDateOfBirth().format(DATE_FORMATTER)
                                                : "",
                                        student.getEmail(),
                                        student.getPhoneNumber()))
                                .collect(Collectors.joining(","));

                Attendance attendance = attendances.stream()
                        .filter(a -> a.getClassSchedule().getClassScheduleId().equals(s.getClassScheduleId()))
                        .findFirst()
                        .orElse(new Attendance(null, null, s, "CHƯA_HỌC", null, null));

                String status = attendance.getStatus();
                boolean canMarkAttendance = isWithinAttendancePeriod(s.getStartTime(), now);
                String statusClass = switch (status) {
                    case "ĐÃ_HỌC" -> "completed";
                    case "VẮNG" -> "missed";
                    case "CHƯA_HỌC" -> "upcoming";
                    default -> "";
                };

                Map<String, String> sessionData = new HashMap<>();
                sessionData.put("className", s.getSwimClass().getName());
                sessionData.put("coach", s.getSwimClass().getCoach().getName());
                sessionData.put("time", hourStart + " - " + hourEnd);
                sessionData.put("status", switch (status) {
                    case "ĐÃ_HỌC" -> "Đã học";
                    case "VẮNG" -> "Vắng";
                    default -> "Chưa học";
                });
                sessionData.put("statusClass", statusClass);
                sessionData.put("canMarkAttendance", String.valueOf(canMarkAttendance));
                sessionData.put("students", studentsData);

                scheduleMap.computeIfAbsent(key, k -> new ArrayList<>()).add(sessionData);
            }

            List<Map<String, String>> weeks = generateWeeks(LocalDate.now(), coachClasses);

            model.addAttribute("timeSlots", timeSlots);
            model.addAttribute("daysOfWeek", daysOfWeek);
            model.addAttribute("scheduleMap", scheduleMap);
            model.addAttribute("weeks", weeks);
            model.addAttribute("selectedWeek", String.valueOf(weekOffset));
            return "coach/schedules";

        } catch (Exception e) {
            log.error("Lỗi khi tải lịch dạy: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch dạy: " + e.getMessage());
            return setupDefaultModel(model, LocalDate.now(), "0");
        }
    }

    private List<Map<String, String>> generateWeeks(LocalDate baseDate, List<SwimClass> userClasses) {
        LocalDate currentMonday = baseDate.with(DayOfWeek.MONDAY);
        int maxWeeks = calculateMaxWeeks(userClasses);
        int minOffset = -2;
        int maxOffset = maxWeeks + 2;
        return IntStream.rangeClosed(minOffset, maxOffset)
                .mapToObj(i -> {
                    LocalDate monday = currentMonday.plusWeeks(i);
                    String label = monday.format(DATE_FORMATTER) + " → " + monday.plusDays(6).format(DATE_FORMATTER);
                    return Map.of("value", String.valueOf(i), "label", label);
                })
                .collect(Collectors.toList());
    }

    private int calculateMaxWeeks(List<SwimClass> userClasses) {
        if (userClasses == null || userClasses.isEmpty())
            return 0;
        return userClasses.stream()
                .mapToInt(swimClass -> {
                    LocalDate startDate = swimClass.getCreatedAt().toLocalDate();
                    LocalDate endDate = startDate.plusWeeks(5); // mặc định 5 tuần
                    return (int) ChronoUnit.WEEKS.between(startDate, endDate);
                }).max().orElse(0);
    }

    private boolean isWithinAttendancePeriod(LocalDateTime startTime, LocalDateTime now) {
        LocalDate startDate = startTime.toLocalDate();
        LocalDate currentDate = now.toLocalDate();
        return !currentDate.isAfter(startDate) && !currentDate.isBefore(startDate);
    }

    private String setupDefaultModel(Model model, LocalDate today, String selectedWeek) {
        int defaultOffset = selectedWeek != null ? Integer.parseInt(selectedWeek) : 0;
        List<SwimClass> coachClasses = swimClassService.getClassesByCoachId(getCurrentCoach().getUserId());
        List<ClassSchedule> allSchedules = coachClasses.stream()
                .flatMap(c -> c.getSchedules().stream())
                .collect(Collectors.toList());
        LocalDate minDate = allSchedules.stream()
                .map(s -> s.getStartTime().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(today);
        List<Map<String, String>> weeks = generateWeeks(minDate, coachClasses);
        model.addAttribute("weeks", weeks);
        model.addAttribute("selectedWeek", selectedWeek);
        model.addAttribute("timeSlots", new ArrayList<Map<String, String>>());
        model.addAttribute("daysOfWeek", IntStream.range(0, 7)
                .mapToObj(i -> {
                    LocalDate date = minDate.with(DayOfWeek.MONDAY).plusWeeks(defaultOffset).plusDays(i);
                    return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                            Locale.forLanguageTag("vi-VN")) + " - " + date.format(DATE_FORMATTER);
                }).collect(Collectors.toList()));
        model.addAttribute("scheduleMap", new HashMap<String, List<Map<String, String>>>());
        return "coach/schedules";
    }

    private User getCurrentCoach() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userService.getUserByUsername(username);
    }
}
