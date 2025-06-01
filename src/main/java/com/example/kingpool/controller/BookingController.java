package com.example.kingpool.controller;

import com.example.kingpool.entity.Booking;
import com.example.kingpool.entity.User;
import com.example.kingpool.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Hiển thị trang đặt vé
    @GetMapping
    public String showBookingPage(Model model) {
        model.addAttribute("schedules", bookingService.getAvailableSchedules());
        return "booking/book-ticket";
    }

    // Xử lý đặt vé
    @PostMapping("/book")
    public String bookTicket(
            @RequestParam("scheduleId") Integer scheduleId,
            @RequestParam("quantityAdult") Integer quantityAdult,
            @RequestParam("quantityChild") Integer quantityChild,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.bookTicket(authentication, scheduleId, quantityAdult, quantityChild);
            redirectAttributes.addFlashAttribute("success", "Đặt vé thành công! Mã đặt vé: " + booking.getBookingId());
            return "redirect:/booking/confirmation/" + booking.getBookingId();
        } catch (Exception e) {
            logger.error("Lỗi khi đặt vé: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking";
        }
    }

    // Hiển thị trang xác nhận
    @GetMapping("/confirmation/{bookingId}")
    public String showConfirmationPage(@PathVariable("bookingId") Integer bookingId, Model model) {
        // Lấy thông tin Booking từ bảng Booking
        String bookingSql = "SELECT b.booking_id, b.user_id, b.schedule_id, b.quantity_adult, b.quantity_child, b.total_price, b.status, u.name " +
                           "FROM Booking b JOIN Users u ON b.user_id = u.user_id WHERE b.booking_id = ?";
        Map<String, Object> bookingData = jdbcTemplate.queryForMap(bookingSql, bookingId);

        // Lấy thông tin khung giờ từ bảng Schedules
        String scheduleSql = "SELECT start_time, end_time FROM Schedules WHERE schedule_id = ?";
        Map<String, Object> schedule = jdbcTemplate.queryForMap(scheduleSql, bookingData.get("schedule_id"));

        // Tạo đối tượng Booking để truyền vào model
        Booking booking = new Booking();
        booking.setBookingId((Integer) bookingData.get("booking_id"));
        booking.setScheduleId((Integer) bookingData.get("schedule_id"));
        booking.setQuantityAdult((Integer) bookingData.get("quantity_adult"));
        booking.setQuantityChild((Integer) bookingData.get("quantity_child"));
        booking.setTotalPrice(((Number) bookingData.get("total_price")).doubleValue());
        booking.setStatus((String) bookingData.get("status"));

        // Tạo đối tượng User giả lập để chứa tên
        User user = new User();
        user.setName((String) bookingData.get("name"));
        booking.setUser(user);

        model.addAttribute("booking", booking);
        model.addAttribute("schedule", schedule);
        return "booking/confirmation";
    }
}