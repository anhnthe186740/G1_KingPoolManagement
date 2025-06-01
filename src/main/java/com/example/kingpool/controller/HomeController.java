package com.example.kingpool.controller;

import com.example.kingpool.entity.Feedback;
import com.example.kingpool.entity.PackageSubscriptions;
import com.example.kingpool.entity.Schedule;
import com.example.kingpool.entity.SwimPackages;
import com.example.kingpool.entity.User;
import com.example.kingpool.service.FeedbackService;
import com.example.kingpool.service.PackageSubscriptionService;
import com.example.kingpool.service.ScheduleService;
import com.example.kingpool.service.SwimPackageService;
import com.example.kingpool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SwimPackageService swimPackageService;

    @Autowired
    private UserService userService;

    @Autowired
    private PackageSubscriptionService packageSubscriptionService;

    @GetMapping("/")
    public String getRoot(Model model, Authentication authentication) {
        // Lấy danh sách lịch hoạt động
        List<Schedule> schedules = scheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);

        // Lấy danh sách gói học bơi
        List<SwimPackages> packages = swimPackageService.getAllPackages();
        model.addAttribute("packages", packages);

        // Lấy danh sách phản hồi được hiển thị
        model.addAttribute("visibleFeedbacks", feedbackService.getVisibleFeedbacks());

        // Kiểm tra role nếu đã đăng nhập
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            User user = userService.findByUsername(username); // Dòng 60
            model.addAttribute("role", user.getRole().getRoleName());
        }

        return "homepage/index";
    }

    @GetMapping("/homepage")
    public String getHomePage(Model model, Authentication authentication) {
        // Lấy danh sách lịch hoạt động
        List<Schedule> schedules = scheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);

        // Lấy danh sách gói học bơi
        List<SwimPackages> packages = swimPackageService.getAllPackages();
        model.addAttribute("packages", packages);

        // Lấy danh sách phản hồi được hiển thị
        model.addAttribute("visibleFeedbacks", feedbackService.getVisibleFeedbacks());

        // Kiểm tra role nếu đã đăng nhập
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            User user = userService.findByUsername(username); // Dòng 83
            model.addAttribute("role", user.getRole().getRoleName());
        }

        return "homepage/index";
    }

    @GetMapping("/user-homepage")
    public String getUserHomePage(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Kiểm tra nếu người dùng chưa đăng nhập
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để truy cập trang này.");
            return "redirect:/login";
        }

        // Lấy thông tin người dùng
        String username = authentication.getName();
        User user = userService.findByUsername(username); // Dòng 100
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("name", user.getName());
        model.addAttribute("role", user.getRole().getRoleName());

        // Lấy danh sách lịch học của user
        List<Schedule> userSchedules = packageSubscriptionService.getUserSchedules(user.getUserId());
        model.addAttribute("userSchedules", userSchedules);

        // Lấy danh sách gói học bơi
        List<SwimPackages> packages = swimPackageService.getAllPackages();
        model.addAttribute("packages", packages);

        return "homepage/user-homepage";
    }

    @GetMapping("/login")
    public String getLogin(Model model) {
        model.addAttribute("user", new com.example.kingpool.entity.User());
        return "auth/login";
    }

    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute("user", new com.example.kingpool.entity.User());
        return "auth/register";
    }

    @GetMapping("/about")
    public String getAbout() {
        return "homepage/about";
    }

    @GetMapping("/purchase-package/{id}")
    public String purchasePackage(@PathVariable("id") Long packageId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            SwimPackages swimPackage = swimPackageService.getPackageById(packageId)
                    .orElseThrow(() -> new RuntimeException("Package not found"));

            PackageSubscriptions subscription = new PackageSubscriptions();
            subscription.setUser(user);
            subscription.setPackage(swimPackage);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now().plusDays(swimPackage.getDurationDays()));
            subscription.setStatus(PackageSubscriptions.SubscriptionStatus.Active);
            subscription.setCreatedAt(LocalDateTime.now());

            packageSubscriptionService.saveSubscription(subscription);
            redirectAttributes.addFlashAttribute("successMessage", "Gói học bơi đã được mua thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi mua gói: " + e.getMessage());
        }
        return "redirect:/user-homepage";
    }

    @PostMapping("/book-pool")
    public String bookPool(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("numPeople") int numPeople,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Logic đặt bể bơi riêng (placeholder)
            redirectAttributes.addFlashAttribute("successMessage", "Đặt bể bơi riêng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đặt bể bơi: " + e.getMessage());
        }
        return "redirect:/user-homepage";
    }

    @PostMapping("/submit-support-request")
    public String submitSupportRequest(
            @RequestParam("supportType") String supportType,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Logic gửi yêu cầu hỗ trợ (placeholder)
            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu hỗ trợ đã được gửi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi yêu cầu hỗ trợ: " + e.getMessage());
        }
        return "redirect:/user-homepage";
    }
}