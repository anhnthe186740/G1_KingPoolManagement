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
@RequestMapping("/user/swim-classes")
public class UserSwimClassController {

    private static final Logger log = LoggerFactory.getLogger(UserSwimClassController.class);

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
    public String listOpenClasses(Model model) {
        try {
            List<SwimClass> openClasses = swimClassService.getSwimClassesByStatus("MỞ");
            log.info("Số lượng lớp học đang mở: {}", openClasses.size());
            if (openClasses.isEmpty()) {
                model.addAttribute("error", "Không có lớp học nào đang mở.");
            }
            model.addAttribute("openClasses", openClasses);
            return "user/classes";
        } catch (Exception e) {
            log.error("Lỗi khi tải danh sách lớp học: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách lớp học: " + e.getMessage());
            return "user/classes";
        }
    }

    @PostMapping("/register/{classId}")
    public String registerClass(@PathVariable Integer classId, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            registrationService.registerClass(classId, username);
            redirectAttributes.addFlashAttribute("success", "Đăng ký lớp học thành công, chờ xác nhận.");
            return "redirect:/user/swim-classes";
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký lớp học {}: {}", classId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đăng ký: " + e.getMessage());
            return "redirect:/user/swim-classes";
        }
    }

    @GetMapping("/schedules")
    @Transactional(readOnly = true)
    public String viewSchedules(@RequestParam(value = "week", required = false) String selectedWeek,
            Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = null;
            try {
                user = userService.getUserByUsername(username);
            } catch (Exception e) {
                log.error("Không tìm thấy user với username {}: {}", username, e.getMessage());
                model.addAttribute("error", "Không tìm thấy thông tin người dùng.");
                return setupDefaultModel(model, LocalDate.now(), "0");
            }

            // Validate weekOffset
            int weekOffset;
            try {
                weekOffset = selectedWeek != null ? Integer.parseInt(selectedWeek) : 0;
                int maxWeeks = calculateMaxWeeks(swimClassService.getUserClasses(username));
                int minOffset = -2;
                int maxOffset = maxWeeks + 2;
                if (weekOffset < minOffset || weekOffset > maxOffset)
                    weekOffset = 0;
            } catch (NumberFormatException e) {
                weekOffset = 0;
            }

            // Lấy mốc tuần từ hiện tại
            LocalDate baseMonday = LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate startOfWeek = baseMonday.plusWeeks(weekOffset);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            LocalDateTime startDateTime = startOfWeek.atStartOfDay();
            LocalDateTime endDateTime = endOfWeek.atTime(23, 59, 59);

            List<SwimClass> userClasses = swimClassService.getUserClasses(username);
            List<ClassSchedule> schedulesInWeek = userClasses.stream()
                    .flatMap(c -> c.getSchedules().stream())
                    .filter(s -> {
                        LocalDate d = s.getStartTime().toLocalDate();
                        return !d.isBefore(startOfWeek) && !d.isAfter(endOfWeek);
                    })
                    .collect(Collectors.toList());

            List<Attendance> attendances = new ArrayList<>();
            try {
                attendances = attendanceRepository.findByUserIdAndDateRange(user.getUserId(), startDateTime,
                        endDateTime);
            } catch (Exception e) {
                log.warn("Lỗi khi lấy điểm danh: {}", e.getMessage());
            }

            // ✅ Lấy danh sách giờ từ startTime chính xác
            List<String> hourList = schedulesInWeek.stream()
                    .map(s -> s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .distinct()
                    .sorted()
                    .toList();
            List<Map<String, String>> timeSlots = hourList.stream()
                    .map(h -> Map.of("label", h, "value", h))
                    .toList();

            List<String> daysOfWeek = IntStream.range(0, 7)
                    .mapToObj(i -> {
                        LocalDate date = startOfWeek.plusDays(i);
                        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                                Locale.forLanguageTag("vi-VN")) + " - " + date.format(DATE_FORMATTER);
                    })
                    .collect(Collectors.toList());

            Map<String, List<Map<String, String>>> scheduleMap = new HashMap<>();
            for (ClassSchedule s : schedulesInWeek) {
                String day = s.getStartTime().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                        Locale.forLanguageTag("vi-VN")) + " - " + s.getStartTime().toLocalDate().format(DATE_FORMATTER);
                String hourStart = s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                String hourEnd = s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                String key = day + "_" + hourStart;

                Attendance attendance = attendances.stream()
                        .filter(a -> a.getClassSchedule().getClassScheduleId().equals(s.getClassScheduleId()))
                        .findFirst()
                        .orElse(new Attendance(null, user, s, "CHƯA_HỌC", null, null));

                String status = attendance.getStatus();
                String statusClass = switch (status) {
                    case "ĐÃ_HỌC" -> "completed";
                    case "VẮNG" -> "missed";
                    case "CHƯA_HỌC" -> "upcoming";
                    default -> "";
                };

                Map<String, String> sessionData = new HashMap<>();
                sessionData.put("className", s.getSwimClass().getName());
                sessionData.put("coach",
                        s.getSwimClass().getCoach() != null ? s.getSwimClass().getCoach().getName() : "Chưa có HLV");
                sessionData.put("time", hourStart + " - " + hourEnd);
                sessionData.put("status", switch (status) {
                    case "ĐÃ_HỌC" -> "Đã học";
                    case "VẮNG" -> "Vắng";
                    default -> "Chưa học";
                });
                sessionData.put("statusClass", statusClass);

                scheduleMap.computeIfAbsent(key, k -> new ArrayList<>()).add(sessionData);
            }

            List<Map<String, String>> weeks = generateWeeks(LocalDate.now(), userClasses);

            model.addAttribute("timeSlots", timeSlots);
            model.addAttribute("daysOfWeek", daysOfWeek);
            model.addAttribute("scheduleMap", scheduleMap);
            model.addAttribute("weeks", weeks);
            model.addAttribute("selectedWeek", String.valueOf(weekOffset));
            return "user/schedules";

        } catch (Exception e) {
            log.error("Lỗi khi tải lịch học: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch học: " + e.getMessage());
            return setupDefaultModel(model, LocalDate.now(), "0");
        }
    }

    private String setupDefaultModel(Model model, LocalDate today, String selectedWeek) {
        model.addAttribute("weeks", generateWeeks(today, Collections.emptyList()));
        model.addAttribute("selectedWeek", selectedWeek);
        model.addAttribute("timeSlots", new ArrayList<>());
        model.addAttribute("daysOfWeek", IntStream.range(0, 7)
                .mapToObj(i -> {
                    LocalDate date = today.with(DayOfWeek.MONDAY).plusDays(i);
                    return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                            Locale.forLanguageTag("vi-VN")) + " - " + date.format(DATE_FORMATTER);
                }).toList());
        model.addAttribute("scheduleMap", new HashMap<String, List<Map<String, String>>>());
        return "user/schedules";
    }

    private List<Map<String, String>> generateWeeks(LocalDate today, List<SwimClass> userClasses) {
        LocalDate currentMonday = today.with(DayOfWeek.MONDAY);
        int maxWeeks = calculateMaxWeeks(userClasses);
        int minOffset = -2;
        int maxOffset = maxWeeks + 2;
        return IntStream.rangeClosed(minOffset, maxOffset)
                .mapToObj(i -> {
                    LocalDate monday = currentMonday.plusWeeks(i);
                    String label = monday.format(DATE_FORMATTER) + " → " + monday.plusDays(6).format(DATE_FORMATTER);
                    return Map.of("value", String.valueOf(i), "label", label);
                }).collect(Collectors.toList());
    }

    private int calculateMaxWeeks(List<SwimClass> userClasses) {
        if (userClasses == null || userClasses.isEmpty())
            return 0;
        return userClasses.stream()
                .mapToInt(swimClass -> {
                    LocalDate startDate = swimClass.getCreatedAt().toLocalDate();
                    LocalDate endDate = startDate.plusWeeks(5);
                    return (int) ChronoUnit.WEEKS.between(startDate, endDate);
                }).max().orElse(0);
    }

}
