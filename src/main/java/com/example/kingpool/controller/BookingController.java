package com.example.kingpool.controller;

import com.example.kingpool.entity.Booking;
import com.example.kingpool.entity.User;
import com.example.kingpool.service.AuthService;
import com.example.kingpool.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthService authService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public String showBookingPage(Model model) {
        logger.debug("Accessing booking page");
        try {
            model.addAttribute("schedules", bookingService.getAvailableSchedules());
            return "booking/book-ticket";
        } catch (Exception e) {
            logger.error("Error loading booking page: {}", e.getMessage());
            model.addAttribute("error", "Lỗi khi tải trang đặt vé");
            return "booking/book-ticket";
        }
    }

    @GetMapping("/freestyle-booking")
    public String showFreestyleBookingPage(Model model) {
        logger.debug("Accessing freestyle booking page");
        try {
            model.addAttribute("schedules", bookingService.getAvailableSchedules());
            return "booking/freestyle-booking";
        } catch (Exception e) {
            logger.error("Error loading freestyle booking page: {}", e.getMessage());
            model.addAttribute("error", "Lỗi khi tải trang đặt lịch bơi tự do");
            return "booking/freestyle-booking";
        }
    }

    @PostMapping("/book")
    public String bookTicket(
            @RequestParam("scheduleId") Integer scheduleId,
            @RequestParam("quantityAdult") Integer quantityAdult,
            @RequestParam("quantityChild") Integer quantityChild,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        logger.debug("Booking ticket: scheduleId={}, quantityAdult={}, quantityChild={}", 
                     scheduleId, quantityAdult, quantityChild);

        if (scheduleId == null || quantityAdult == null || quantityChild == null ||
            quantityAdult < 0 || quantityChild < 0 || (quantityAdult + quantityChild) <= 0) {
            logger.error("Invalid input: scheduleId={}, quantityAdult={}, quantityChild={}", 
                         scheduleId, quantityAdult, quantityChild);
            redirectAttributes.addFlashAttribute("error", "Dữ liệu đầu vào không hợp lệ.");
            return "redirect:/booking/freestyle-booking";
        }

        try {
            Booking booking = bookingService.bookTicket(authentication, scheduleId, quantityAdult, quantityChild);
            redirectAttributes.addFlashAttribute("success", "Đặt vé thành công! Mã đặt vé: " + booking.getBookingId());

            // Gửi thông báo WebSocket
            String scheduleSql = "SELECT schedule_id, start_time, end_time, max_tickets, adult_price, child_price, status " +
                                "FROM Schedules WHERE schedule_id = ?";
            Map<String, Object> schedule = jdbcTemplate.queryForMap(scheduleSql, scheduleId);
            messagingTemplate.convertAndSend("/topic/bookings", schedule);

            return "redirect:/booking/confirmation/" + booking.getBookingId();
        } catch (Exception e) {
            logger.error("Lỗi khi đặt vé: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking/freestyle-booking";
        }
    }

    @GetMapping("/confirmation/{bookingId}")
    public String showConfirmationPage(@PathVariable("bookingId") Integer bookingId, Model model, 
            Authentication authentication, RedirectAttributes redirectAttributes) {
        logger.debug("Showing confirmation for bookingId={}", bookingId);

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access to confirmation for bookingId={}", bookingId);
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để xem xác nhận đặt vé.");
            return "redirect:/login";
        }

        try {
            User currentUser = authService.getUserFromAuthentication(authentication);

            String bookingSql = "SELECT b.booking_id, b.user_id, b.schedule_id, b.quantity_adult, b.quantity_child, " +
                               "b.total_price, b.status, u.name " +
                               "FROM Booking b JOIN Users u ON b.user_id = u.user_id WHERE b.booking_id = ?";
            Map<String, Object> bookingData = jdbcTemplate.queryForMap(bookingSql, bookingId);

            Long bookingUserId = ((Number) bookingData.get("user_id")).longValue();
            if (!bookingUserId.equals(currentUser.getUserId().longValue())) {
                logger.warn("User {} attempted to access booking {} belonging to another user", 
                            currentUser.getUsername(), bookingId);
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem thông tin đặt vé này.");
                return "redirect:/booking/freestyle-booking";
            }

            String scheduleSql = "SELECT start_time, end_time FROM Schedules WHERE schedule_id = ?";
            Map<String, Object> schedule = jdbcTemplate.queryForMap(scheduleSql, bookingData.get("schedule_id"));

            Booking booking = new Booking();
            booking.setBookingId(((Number) bookingData.get("booking_id")).intValue());
            booking.setScheduleId(((Number) bookingData.get("schedule_id")).intValue());
            booking.setQuantityAdult(((Number) bookingData.get("quantity_adult")).intValue());
            booking.setQuantityChild(((Number) bookingData.get("quantity_child")).intValue());
            booking.setTotalPrice(((Number) bookingData.get("total_price")).doubleValue());
            booking.setStatus((String) bookingData.get("status"));

            User user = new User();
            user.setName((String) bookingData.get("name"));
            user.setUserId(bookingUserId.intValue());
            booking.setUser(user);

            model.addAttribute("booking", booking);
            model.addAttribute("schedule", schedule);
            return "booking/confirmation";
        } catch (EmptyResultDataAccessException e) {
            logger.error("Booking with ID {} not found", bookingId);
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin đặt vé.");
            return "redirect:/booking/freestyle-booking";
        } catch (Exception e) {
            logger.error("Error processing confirmation for bookingId {}: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi truy xuất thông tin đặt vé.");
            return "redirect:/booking/freestyle-booking";
        }
    }

    @GetMapping("/history")
    public String showBookingHistory(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        logger.debug("Accessing booking history");
        try {
            List<Map<String, Object>> bookings = bookingService.getBookingHistory(authentication);
            model.addAttribute("bookings", bookings);
            return "booking/history";
        } catch (Exception e) {
            logger.error("Lỗi khi tải lịch sử đặt vé: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking/freestyle-booking";
        }
    }

    @GetMapping("/cancel/{bookingId}")
    public String cancelBooking(
            @PathVariable("bookingId") Integer bookingId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        logger.debug("Attempting to cancel bookingId={}", bookingId);
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access to cancel bookingId={}", bookingId);
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để hủy vé");
            return "redirect:/login";
        }

        try {
            bookingService.cancelBooking(authentication, bookingId);

            // Gửi thông báo WebSocket
            String bookingSql = "SELECT schedule_id FROM Booking WHERE booking_id = ?";
            Map<String, Object> bookingData = jdbcTemplate.queryForMap(bookingSql, bookingId);
            Integer scheduleId = ((Number) bookingData.get("schedule_id")).intValue();
            String scheduleSql = "SELECT schedule_id, start_time, end_time, max_tickets, adult_price, child_price, status " +
                                "FROM Schedules WHERE schedule_id = ?";
            Map<String, Object> schedule = jdbcTemplate.queryForMap(scheduleSql, scheduleId);
            messagingTemplate.convertAndSend("/topic/bookings", schedule);

            redirectAttributes.addFlashAttribute("success", "Hủy vé thành công!");
            logger.info("Booking {} cancelled successfully", bookingId);
        } catch (Exception e) {
            logger.error("Error cancelling booking {}: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/booking/history";
    }
}