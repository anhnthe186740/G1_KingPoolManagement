package com.example.kingpool.controller;

import com.example.kingpool.entity.ClassSchedule;
import com.example.kingpool.entity.SwimClass;
import com.example.kingpool.entity.SwimClassRegistration;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.ClassScheduleRepository;
import com.example.kingpool.repository.RoleRepository;
import com.example.kingpool.repository.UserRepository;
import com.example.kingpool.service.SwimClassRegistrationService;
import com.example.kingpool.service.SwimClassService;
import jakarta.validation.Valid;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/swim-classes")
public class AdminSwimClassController {

    @Autowired
    private SwimClassService swimClassService;

    @Autowired
    private SwimClassRegistrationService registrationService;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private static final Logger log = LoggerFactory.getLogger(AdminSwimClassController.class);

    @GetMapping
    public String listSwimClasses(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "viewClassId", required = false) Integer viewClassId,
            @RequestParam(value = "modalType", required = false) String modalType,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SwimClass> swimClasses = (status != null && !status.isEmpty())
                    ? swimClassService.getSwimClassesByStatus(status, pageable)
                    : swimClassService.getAllSwimClasses(pageable);

            model.addAttribute("swimClasses", swimClasses.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", swimClasses.getTotalPages());
            model.addAttribute("totalItems", swimClasses.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("selectedStatus", status);

            if (viewClassId != null) {
                SwimClass selectedClass = "schedules".equals(modalType)
                        ? swimClassService.getSwimClassWithSchedules(viewClassId)
                        : swimClassService.getSwimClassById(viewClassId);

                model.addAttribute("viewClassId", viewClassId);
                model.addAttribute("modalType", modalType);

                if ("students".equals(modalType)) {
                    List<User> students = registrationService
                            .getConfirmedRegistrationsByClassId(viewClassId)
                            .stream().map(SwimClassRegistration::getUser).toList();
                    model.addAttribute("selectedClassStudents", students);
                }

                if ("schedules".equals(modalType)) {
                    model.addAttribute("selectedClass", selectedClass);
                }
            }

            return "admin/swimclass/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải danh sách lớp học.");
            return "redirect:/admin/swim-classes";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("coaches", userRepository.findByRoleRoleName("COACH"));
        model.addAttribute("daysOfWeek", List.of(DayOfWeek.values()));
        return "admin/swimclass/create";
    }

    @PostMapping("/create")
    public String createSwimClass(@RequestParam String name,
                                  @RequestParam Integer coachId,
                                  @RequestParam String level,
                                  @RequestParam Integer maxStudents,
                                  @RequestParam(required = false) String description,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam List<DayOfWeek> studyDays,
                                  @RequestParam Integer durationWeeks,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime classTime,
                                  @RequestParam Integer durationMinutes,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            User coach = userRepository.findById(coachId)
                    .orElseThrow(() -> new IllegalArgumentException("Huấn luyện viên không tồn tại"));

            SwimClass swimClass = swimClassService.createSwimClassWithSchedules(
                    name,
                    coach,
                    level,
                    maxStudents,
                    description,
                    studyDays,
                    startDate,
                    durationWeeks,
                    classTime,
                    Duration.ofMinutes(durationMinutes));

            redirectAttributes.addFlashAttribute("success", "Tạo lớp học thành công.");
            return "redirect:/admin/swim-classes";

        } catch (Exception e) {
            model.addAttribute("coaches", userRepository.findByRoleRoleName("COACH"));
            model.addAttribute("daysOfWeek", List.of(DayOfWeek.values()));
            model.addAttribute("error", "Lỗi khi tạo lớp học: " + e.getMessage());
            return "admin/swimclass/create";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteSwimClass(@PathVariable Integer id) {
        swimClassService.deleteSwimClass(id);
        return "redirect:/admin/swim-classes";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("classId") Integer classId,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes) {
        try {
            swimClassService.updateStatus(classId, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/swim-classes";
    }

    @GetMapping("/{id}/registrations")
    public String viewRegistrations(@PathVariable Integer id, Model model) {
        model.addAttribute("registrations", registrationService.getRegistrationsByClassId(id));
        model.addAttribute("classId", id);
        model.addAttribute("students", userRepository.findByRoleRoleName("USER"));
        return "admin/swimclass/registrations";
    }

    @PostMapping("/registrations/confirm/{registrationId}")
    public String confirmRegistration(@PathVariable Integer registrationId,
                                      @RequestParam Integer classId,
                                      RedirectAttributes redirectAttributes) {
        try {
            registrationService.confirmRegistration(registrationId);
            redirectAttributes.addFlashAttribute("success", "Xác nhận thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xác nhận: " + e.getMessage());
        }
        return "redirect:/admin/swim-classes/" + classId + "/registrations";
    }

    @PostMapping("/registrations/cancel/{registrationId}")
    public String cancelRegistration(@PathVariable Integer registrationId,
                                     @RequestParam Integer classId,
                                     @RequestParam String cancelReason,
                                     RedirectAttributes redirectAttributes) {
        try {
            registrationService.cancelRegistration(registrationId, cancelReason);
            redirectAttributes.addFlashAttribute("success", "Hủy thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hủy: " + e.getMessage());
        }
        return "redirect:/admin/swim-classes/" + classId + "/registrations";
    }

    @PostMapping("/{id}/registrations/add")
    public String addStudentToClass(@PathVariable Integer id,
                                    @RequestParam Integer studentId,
                                    RedirectAttributes redirectAttributes) {
        try {
            registrationService.addStudentToClass(id, studentId);
            redirectAttributes.addFlashAttribute("success", "Thêm học viên thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm học viên: " + e.getMessage());
        }
        return "redirect:/admin/swim-classes/" + id + "/registrations";
    }

    @GetMapping("/edit/{id}")
public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
    try {
        SwimClass swimClass = swimClassService.getSwimClassWithSchedules(id);
        model.addAttribute("swimClass", swimClass);
        model.addAttribute("coaches", userRepository.findByRoleRoleName("COACH"));
        model.addAttribute("daysOfWeek", List.of(DayOfWeek.values()));
        return "admin/swimclass/edit";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Không tìm thấy lớp học.");
        return "redirect:/admin/swim-classes";
    }
}
    @PostMapping("/edit/{id}")
public String updateSwimClass(@PathVariable Integer id,
                              @RequestParam String name,
                              @RequestParam Integer coachId,
                              @RequestParam String level,
                              @RequestParam Integer maxStudents,
                              @RequestParam(required = false) String description,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                              @RequestParam List<DayOfWeek> studyDays,
                              @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime classTime,
                              RedirectAttributes redirectAttributes,
                              Model model) {
    try {
        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy HLV"));

        int durationWeeks = 4; // default
        Duration duration = Duration.ofMinutes(45); // default

        swimClassService.updateSwimClassWithSchedules(
                id, name, coach, level, maxStudents, description,
                studyDays, startDate, durationWeeks, classTime, duration);

        redirectAttributes.addFlashAttribute("success", "Cập nhật lớp học thành công.");
        return "redirect:/admin/swim-classes";
    } catch (Exception e) {
        model.addAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        model.addAttribute("coaches", userRepository.findByRoleRoleName("COACH"));
        model.addAttribute("daysOfWeek", List.of(DayOfWeek.values()));
        return "admin/swimclass/edit";
    }
}



}
