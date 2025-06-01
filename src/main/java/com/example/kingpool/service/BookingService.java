package com.example.kingpool.service;

import com.example.kingpool.entity.Booking;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AuthService authService;

    // Lấy danh sách khung giờ khả dụng từ bảng Schedules
    public List<Map<String, Object>> getAvailableSchedules() {
        String sql = "SELECT schedule_id, start_time, end_time, max_tickets, adult_price, child_price " +
                     "FROM Schedules WHERE status = 'Open'";
        return jdbcTemplate.queryForList(sql);
    }

    // Đặt vé
    @Transactional
    public Booking bookTicket(Authentication authentication, Integer scheduleId, Integer quantityAdult, Integer quantityChild) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Vui lòng đăng nhập để đặt vé");
        }

        // Lấy thông tin người dùng
        User user = authService.getUserFromAuthentication(authentication);

        // Lấy thông tin khung giờ
        String sql = "SELECT max_tickets, adult_price, child_price, status FROM Schedules WHERE schedule_id = ? AND status = 'Open'";
        Map<String, Object> schedule;
        try {
            schedule = jdbcTemplate.queryForMap(sql, scheduleId);
        } catch (Exception e) {
            throw new RuntimeException("Khung giờ không tồn tại hoặc không khả dụng");
        }

        Integer maxTickets = ((Number) schedule.get("max_tickets")).intValue();
        Double adultPrice = ((Number) schedule.get("adult_price")).doubleValue();
        Double childPrice = ((Number) schedule.get("child_price")).doubleValue();

        // Kiểm tra số vé còn lại
        Integer bookedTickets = bookingRepository.countBookedTickets(scheduleId);
        bookedTickets = bookedTickets != null ? bookedTickets : 0;
        int requestedTickets = quantityAdult + quantityChild;
        int availableTickets = maxTickets - bookedTickets;

        if (requestedTickets <= 0) {
            throw new RuntimeException("Vui lòng chọn ít nhất 1 vé");
        }

        if (requestedTickets > availableTickets) {
            throw new RuntimeException("Chỉ còn " + availableTickets + " vé khả dụng");
        }

        // Tính tổng giá
        double totalPrice = (quantityAdult * adultPrice) + (quantityChild * childPrice);

        // Tạo bản ghi Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setScheduleId(scheduleId);
        booking.setBookingType("Ticket");
        booking.setQuantityAdult(quantityAdult);
        booking.setQuantityChild(quantityChild);
        booking.setTotalPrice(totalPrice);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("Confirmed");

        booking = bookingRepository.save(booking);
        logger.info("Tạo booking thành công cho user {} với bookingId {}", user.getUsername(), booking.getBookingId());

        // Cập nhật trạng thái khung giờ nếu hết vé
        if (bookedTickets + requestedTickets >= maxTickets) {
            jdbcTemplate.update("UPDATE Schedules SET status = 'SoldOut' WHERE schedule_id = ?", scheduleId);
            logger.info("Khung giờ {} hết vé, cập nhật trạng thái thành SoldOut", scheduleId);
        }

        return booking;
    }
}