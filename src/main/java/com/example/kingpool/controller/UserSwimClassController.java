package com.example.kingpool.controller;

import com.example.kingpool.entity.ClassSchedule;
import com.example.kingpool.entity.SwimClass;
import com.example.kingpool.entity.SwimClassRegistration;
import com.example.kingpool.entity.User;
import com.example.kingpool.service.SwimClassRegistrationService;
import com.example.kingpool.service.SwimClassService;
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

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/user/swim-classes")
public class UserSwimClassController {

    private static final Logger log = LoggerFactory.getLogger(UserSwimClassController.class);

    @Autowired
    private SwimClassService swimClassService;

    @Autowired
    private SwimClassRegistrationService registrationService;

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

    @Transactional(readOnly = true)
    @GetMapping("/schedules")
    public String viewSchedules(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            List<SwimClass> userClasses = swimClassService.getUserClasses(username);

            // Tạo danh sách lịch học phẳng từ tất cả các lớp của USER
            List<ClassSchedule> userSchedules = new ArrayList<>();
            for (SwimClass uc : userClasses) {
                if (uc.getSchedules() != null) {
                    userSchedules.addAll(uc.getSchedules());
                }
            }
            log.info("Số lượng lịch học của USER {}: {}", username, userSchedules.size());
            for (ClassSchedule schedule : userSchedules) {
                log.debug("Lịch học: classId={}, name={}, startTime={}", 
                          schedule.getSwimClass() != null ? schedule.getSwimClass().getClassId() : null,
                          schedule.getSwimClass() != null ? schedule.getSwimClass().getName() : "null",
                          schedule.getStartTime());
            }

            // Tính danh sách giờ duy nhất từ userSchedules
            Set<String> uniqueHours = new TreeSet<>();
            for (ClassSchedule schedule : userSchedules) {
                if (schedule.getStartTime() != null && schedule.getSwimClass() != null) {
                    uniqueHours.add(String.format("%02d", schedule.getStartTime().getHour()));
                }
            }
            List<String> hours = new ArrayList<>(uniqueHours);
            log.info("Số lượng giờ duy nhất: {}", hours.size());

            if (userSchedules.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy lịch học nào cho tài khoản hiện tại.");
            } else if (hours.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy giờ hợp lệ trong lịch học.");
            }
            model.addAttribute("hours", hours);
            model.addAttribute("userSchedules", userSchedules);
            return "user/schedules";
        } catch (Exception e) {
            log.error("Lỗi khi tải lịch học: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch học: " + e.getMessage());
            return "user/schedules";
        }
    }
}