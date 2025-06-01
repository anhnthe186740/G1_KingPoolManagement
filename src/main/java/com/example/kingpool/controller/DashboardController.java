package com.example.kingpool.controller;

import com.example.kingpool.entity.*;
import com.example.kingpool.service.BookingService;
import com.example.kingpool.service.FeedbackService;
import com.example.kingpool.service.ScheduleService;
import com.example.kingpool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('Admin')")
    public String getDashboard(Model model) {
        return "dashboard/dashboard";
    }

    @GetMapping("/dashboard/users")
    @PreAuthorize("hasRole('Admin')")
    public String viewUsers(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> users = userService.getUsersByRoleId(1);
            model.addAttribute("users", users);
            if (users.isEmpty()) {
                model.addAttribute("message", "No customers found.");
            }
            return "dashboard/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading customer list: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/dashboard/user/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String viewUserDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getRole().getRoleId() != 1) {
                throw new RuntimeException("User is not a customer");
            }
            model.addAttribute("user", user);
            return "dashboard/user-detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/users";
        }
    }

    @GetMapping("/dashboard/user/add")
    @PreAuthorize("hasRole('Admin')")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "dashboard/add-user";
    }

    @PostMapping("/dashboard/user/add")
    @PreAuthorize("hasRole('Admin')")
    public String addUser(
            @RequestParam("name") String name,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDate local = LocalDate.now();
            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all required fields.");
            }
            if (userService.getUserByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username already exists.");
            }
            if (userService.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email already exists.");
            }

            User user = new User();
            user.setName(name);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            Role role = new Role();
            role.setRoleId(1); // Role Customer
            user.setRole(role);
            user.setStatus(User.Status.Active);
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "User added successfully!");
            return "redirect:/dashboard/users";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/user/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding user: " + e.getMessage());
            return "redirect:/dashboard/user/add";
        }
    }

    @PostMapping("/dashboard/user/delete/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            bookingService.deleteBookingsByUserId(id);
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/dashboard/users";
    }

    @GetMapping("/dashboard/coaches")
    @PreAuthorize("hasRole('Admin')")
    public String viewCoaches(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> coaches = userService.getUsersByRoleId(2);
            model.addAttribute("coaches", coaches);
            if (coaches.isEmpty()) {
                model.addAttribute("message", "No coaches found.");
            }
            return "dashboard/coaches";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading coach list: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/dashboard/coach/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String viewCoachDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User coach = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));
            if (coach.getRole().getRoleId() != 2) {
                throw new RuntimeException("User is not a coach");
            }
            model.addAttribute("coach", coach);
            return "dashboard/coach-detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/coaches";
        }
    }

    @GetMapping("/dashboard/coach/add")
    @PreAuthorize("hasRole('Admin')")
    public String showAddCoachForm(Model model) {
        model.addAttribute("coach", new User());
        return "dashboard/add-coach";
    }

    @PostMapping("/dashboard/coach/add")
    @PreAuthorize("hasRole('Admin')")
    public String addCoach(
            @RequestParam("name") String name,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all required fields.");
            }
            if (userService.getUserByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username already exists.");
            }
            if (userService.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email already exists.");
            }

            User coach = new User();
            coach.setName(name);
            coach.setUsername(username);
            coach.setPassword(passwordEncoder.encode(password));
            coach.setEmail(email);
            Role role = new Role();
            role.setRoleId(2); // Role Coach
            coach.setRole(role);
            coach.setStatus(User.Status.Active);
            userService.saveUser(coach);
            redirectAttributes.addFlashAttribute("success", "Coach added successfully!");
            return "redirect:/dashboard/coaches";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/coach/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding coach: " + e.getMessage());
            return "redirect:/dashboard/coach/add";
        }
    }

    @GetMapping("/dashboard/coach/edit/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String showEditCoachForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User coach = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));
            if (coach.getRole().getRoleId() != 2) {
                throw new RuntimeException("User is not a coach");
            }
            model.addAttribute("coach", coach);
            return "dashboard/edit-coach";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/coaches";
        }
    }

    @PostMapping("/dashboard/coach/edit/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String editCoach(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("hireDate") String hireDate,
            @RequestParam("status") String status,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDate local = LocalDate.now();
            User coach = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));
            if (name.isEmpty() || username.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all required fields (name, username, email).");
            }
            User existingUser = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));
            if (existingUser.getRole().getRoleId() != 2) {
                throw new RuntimeException("User is not a coach");
            }
            if (!existingUser.getUsername().equals(username) && userService.getUserByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username already exists.");
            }
            if (!existingUser.getEmail().equals(email) && userService.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email already exists.");
            }

            existingUser.setName(name);
            existingUser.setUsername(username);
            if (password != null && !password.isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(password));
            }
            existingUser.setEmail(email);
            existingUser.setPhoneNumber(phoneNumber.isEmpty() ? null : phoneNumber);

            if (!hireDate.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate hireDateParsed = LocalDate.parse(hireDate, formatter);
                existingUser.setHireDate(hireDateParsed);
            } else {
                existingUser.setHireDate(LocalDate.now());
            }

            existingUser.setStatus(User.Status.valueOf(status));
            userService.saveUser(existingUser);
            redirectAttributes.addFlashAttribute("success", "Coach updated successfully!");
            return "redirect:/dashboard/coaches";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("coach", userService.getUserById(id).orElseThrow(() -> new RuntimeException("Coach not found")));
            return "dashboard/edit-coach";
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Invalid date format. Please use: yyyy-MM-dd (e.g., 2025-05-30)");
            model.addAttribute("coach", userService.getUserById(id).orElseThrow(() -> new RuntimeException("Coach not found")));
            return "dashboard/edit-coach";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating coach: " + e.getMessage());
            return "redirect:/dashboard/coach/edit/" + id;
        }
    }

    @PostMapping("/dashboard/coach/delete/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String deleteCoach(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User coach = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("Coach not found"));
            bookingService.deleteBookingsByUserId(id);
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Coach deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting coach: " + e.getMessage());
        }
        return "redirect:/dashboard/coaches";
    }

    @GetMapping("/dashboard/schedules")
    @PreAuthorize("hasRole('Admin')")
    public String viewSchedules(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Schedule> schedules = scheduleService.getAllSchedules();
            model.addAttribute("schedules", schedules);
            if (schedules.isEmpty()) {
                model.addAttribute("message", "No schedules found.");
            }
            return "dashboard/schedules";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading schedule list: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/dashboard/schedule/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String viewScheduleDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Schedule schedule = scheduleService.getScheduleById(id)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            model.addAttribute("schedule", schedule);
            return "dashboard/schedule-detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/schedules";
        }
    }

    @GetMapping("/dashboard/schedule/add")
    @PreAuthorize("hasRole('Admin')")
    public String showAddScheduleForm(Model model) {
        model.addAttribute("schedule", new Schedule());
        return "dashboard/add-schedule";
    }

    @PostMapping("/dashboard/schedule/add")
    @PreAuthorize("hasRole('Admin')")
    public String addSchedule(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam("maxTickets") int maxTickets,
            @RequestParam("adultPrice") double adultPrice,
            @RequestParam("childPrice") double childPrice,
            @RequestParam("status") String status,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (startTime == null || startTime.isEmpty() || endTime == null || endTime.isEmpty()) {
            model.addAttribute("error", "Start time and end time are required.");
            model.addAttribute("schedule", new Schedule());
            return "dashboard/add-schedule";
        }

        if (maxTickets <= 0) {
            model.addAttribute("error", "Max tickets must be greater than 0.");
            model.addAttribute("schedule", new Schedule());
            return "dashboard/add-schedule";
        }
        if (adultPrice < 0 || childPrice < 0) {
            model.addAttribute("error", "Prices cannot be negative.");
            model.addAttribute("schedule", new Schedule());
            return "dashboard/add-schedule";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);

            if (!end.isAfter(start)) {
                model.addAttribute("error", "End time must be after start time.");
                model.addAttribute("schedule", new Schedule());
                return "dashboard/add-schedule";
            }

            Schedule schedule = new Schedule();
            schedule.setStartTime(start);
            schedule.setEndTime(end);
            schedule.setMaxTickets(maxTickets);
            schedule.setAdultPrice(adultPrice);
            schedule.setChildPrice(childPrice);
            schedule.setStatus(Schedule.ScheduleStatus.valueOf(status));
            scheduleService.saveSchedule(schedule);
            redirectAttributes.addFlashAttribute("success", "Schedule added successfully!");
            return "redirect:/dashboard/schedules";
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Invalid time format. Please use: yyyy-MM-dd HH:mm (e.g., 2025-05-30T14:00)");
            model.addAttribute("schedule", new Schedule());
            return "dashboard/add-schedule";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid status: " + e.getMessage());
            model.addAttribute("schedule", new Schedule());
            return "dashboard/add-schedule";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding schedule: " + e.getMessage());
            return "redirect:/dashboard/schedule/add";
        }
    }

    @GetMapping("/dashboard/schedule/edit/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String showEditScheduleForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Schedule schedule = scheduleService.getScheduleById(id)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            model.addAttribute("schedule", schedule);
            return "dashboard/edit-schedule";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/schedules";
        }
    }

    @PostMapping("/dashboard/schedule/edit/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String editSchedule(
            @PathVariable Long id,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam("maxTickets") int maxTickets,
            @RequestParam("adultPrice") double adultPrice,
            @RequestParam("childPrice") double childPrice,
            @RequestParam("status") String status,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (startTime == null || startTime.isEmpty() || endTime == null || endTime.isEmpty()) {
            model.addAttribute("error", "Start time and end time are required.");
            model.addAttribute("schedule", scheduleService.getScheduleById(id).orElseThrow(() -> new RuntimeException("Schedule not found")));
            return "dashboard/edit-schedule";
        }

        if (maxTickets <= 0) {
            model.addAttribute("error", "Max tickets must be greater than 0.");
            model.addAttribute("schedule", scheduleService.getScheduleById(id).orElseThrow(() -> new RuntimeException("Schedule not found")));
            return "dashboard/edit-schedule";
        }
        if (adultPrice < 0 || childPrice < 0) {
            model.addAttribute("error", "Prices cannot be negative.");
            model.addAttribute("schedule", scheduleService.getScheduleById(id).orElseThrow(() -> new RuntimeException("Schedule not found")));
            return "dashboard/edit-schedule";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);

            if (!end.isAfter(start)) {
                model.addAttribute("error", "End time must be after start time.");
                model.addAttribute("schedule", scheduleService.getScheduleById(id).orElseThrow(() -> new RuntimeException("Schedule not found")));
                return "dashboard/edit-schedule";
            }

            Schedule schedule = scheduleService.getScheduleById(id)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            schedule.setStartTime(start);
            schedule.setEndTime(end);
            schedule.setMaxTickets(maxTickets);
            schedule.setAdultPrice(adultPrice);
            schedule.setChildPrice(childPrice);
            schedule.setStatus(Schedule.ScheduleStatus.valueOf(status));
            scheduleService.saveSchedule(schedule);
            redirectAttributes.addFlashAttribute("success", "Schedule updated successfully!");
            return "redirect:/dashboard/schedules";
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Invalid time format. Please use: yyyy-MM-dd HH:mm (e.g., 2025-05-30T14:00)");
            model.addAttribute("schedule", scheduleService.getScheduleById(id).orElseThrow(() -> new RuntimeException("Schedule not found")));
            return "dashboard/edit-schedule";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid status: " + e.getMessage());
            model.addAttribute("schedule", scheduleService.getScheduleById(id).orElseThrow(() -> new RuntimeException("Schedule not found")));
            return "dashboard/edit-schedule";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating schedule: " + e.getMessage());
            return "redirect:/dashboard/schedule/edit/" + id;
        }
    }

    @PostMapping("/dashboard/schedule/delete/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("success", "Schedule deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting schedule: " + e.getMessage());
        }
        return "redirect:/dashboard/schedules";
    }

    @GetMapping("/dashboard/feedbacks")
    @PreAuthorize("hasRole('Admin')")
    public String viewFeedbacks(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
            model.addAttribute("feedbacks", feedbacks);
            if (feedbacks.isEmpty()) {
                model.addAttribute("message", "No feedbacks found.");
            }
            return "dashboard/feedbacks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading feedback list: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/dashboard/feedback/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String viewFeedbackDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Feedback feedback = feedbackService.getFeedbackById(id)
                    .orElseThrow(() -> new RuntimeException("Feedback not found"));
            model.addAttribute("feedback", feedback);
            return "dashboard/feedback-detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard/feedbacks";
        }
    }

    @PostMapping("/dashboard/feedback/{id}/toggle-visibility")
    @PreAuthorize("hasRole('Admin')")
    public String toggleFeedbackVisibility(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedbackService.updateVisibility(id, !feedbackService.getFeedbackById(id)
                    .orElseThrow(() -> new RuntimeException("Feedback not found")).isVisible());
            redirectAttributes.addFlashAttribute("success", "Feedback visibility updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating visibility: " + e.getMessage());
        }
        return "redirect:/dashboard/feedbacks";
    }
}