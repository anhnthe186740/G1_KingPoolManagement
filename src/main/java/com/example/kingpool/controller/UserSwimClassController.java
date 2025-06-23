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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/schedules")
    public String viewSchedules(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            List<SwimClass> allClasses = swimClassService.getAllSwimClasses();
            List<SwimClass> userClasses = swimClassService.getUserClasses(username);

            // Tạo danh sách lịch học phẳng
            List<ClassSchedule> userSchedules = new ArrayList<>();
            for (SwimClass uc : userClasses) {
                if (uc.getSchedules() != null) {
                    userSchedules.addAll(uc.getSchedules());
                }
            }
            List<ClassSchedule> allSchedules = new ArrayList<>();
            for (SwimClass ac : allClasses) {
                if (ac.getSchedules() != null) {
                    allSchedules.addAll(ac.getSchedules());
                }
            }

            log.info("Số lượng lịch học của USER {}: {}", username, userSchedules.size());
            log.info("Số lượng tất cả lịch học: {}", allSchedules.size());
            model.addAttribute("userSchedules", userSchedules);
            model.addAttribute("allSchedules", allSchedules);
            return "user/schedules";
        } catch (Exception e) {
            log.error("Lỗi khi tải lịch học: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch học: " + e.getMessage());
            return "user/schedules";
        }
    }
}