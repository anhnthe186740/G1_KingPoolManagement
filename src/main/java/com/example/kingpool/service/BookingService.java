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
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AuthService authService;

    public List<Map<String, Object>> getAvailableSchedules() {
        logger.debug("Fetching available schedules");
        String sql = "SELECT schedule_id, start_time, end_time, max_tickets, adult_price, child_price " +
                     "FROM Schedules WHERE status = 'Open'";
        return jdbcTemplate.queryForList(sql);
    }

    @Transactional
    public Booking bookTicket(Authentication authentication, Integer scheduleId, Integer quantityAdult, Integer quantityChild) {
        logger.debug("Booking ticket: scheduleId={}, quantityAdult={}, quantityChild={}", 
                     scheduleId, quantityAdult, quantityChild);
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Vui lòng đăng nhập để đặt vé");
        }

        User user = authService.getUserFromAuthentication(authentication);

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

        double totalPrice = (quantityAdult * adultPrice) + (quantityChild * childPrice);

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

        if (bookedTickets + requestedTickets >= maxTickets) {
            jdbcTemplate.update("UPDATE Schedules SET status = 'SoldOut' WHERE schedule_id = ?", scheduleId);
            logger.info("Khung giờ {} hết vé, cập nhật trạng thái thành SoldOut", scheduleId);
        }

        return booking;
    }

    public List<Map<String, Object>> getBookingHistory(Authentication authentication) {
        logger.debug("Fetching booking history");
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access to booking history");
            throw new RuntimeException("Vui lòng đăng nhập để xem lịch sử đặt vé");
        }

        User user = authService.getUserFromAuthentication(authentication);
        if (user.getUserId() == null) {
            logger.error("User ID is null for username: {}", user.getUsername());
            throw new RuntimeException("Không thể lấy ID người dùng");
        }

        logger.info("Fetching booking history for user: {} (userId: {})", user.getUsername(), user.getUserId());

        String sql = "SELECT b.booking_id, b.schedule_id, b.booking_type, b.quantity_adult, b.quantity_child, " +
                     "b.total_price, b.booking_date, b.status, s.start_time, s.end_time " +
                     "FROM Booking b " +
                     "JOIN Schedules s ON b.schedule_id = s.schedule_id " +
                     "WHERE b.user_id = ? " +
                     "AND b.status != 'Cancelled' " +
                     "ORDER BY b.booking_date DESC";
        
        try {
            List<Map<String, Object>> bookings = jdbcTemplate.queryForList(sql, user.getUserId());
            logger.info("Found {} bookings for user: {}", bookings.size(), user.getUsername());
            return bookings;
        } catch (Exception e) {
            logger.error("Error fetching booking history for user {}: {}", user.getUsername(), e.getMessage());
            throw new RuntimeException("Không thể tải lịch sử đặt vé: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelBooking(Authentication authentication, Integer bookingId) {
        logger.debug("Attempting to cancel bookingId={}", bookingId);
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access to cancel bookingId={}", bookingId);
            throw new RuntimeException("Vui lòng đăng nhập để hủy vé");
        }

        User user = authService.getUserFromAuthentication(authentication);
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);

        if (optionalBooking.isEmpty()) {
            logger.error("Booking with ID {} not found", bookingId);
            throw new RuntimeException("Không tìm thấy vé đặt với mã: " + bookingId);
        }

        Booking booking = optionalBooking.get();
        if (!booking.getUser().getUserId().equals(user.getUserId())) {
            logger.error("User {} does not have permission to cancel booking {}", user.getUsername(), bookingId);
            throw new RuntimeException("Bạn không có quyền hủy vé này");
        }

        if (!booking.getStatus().equals("Confirmed")) {
            logger.error("Booking {} cannot be cancelled, current status: {}", bookingId, booking.getStatus());
            throw new RuntimeException("Vé này không thể hủy (trạng thái: " + booking.getStatus() + ")");
        }

        bookingRepository.delete(booking);
        logger.info("Booking {} deleted by user {}", bookingId, user.getUsername());

        Integer bookedTickets = bookingRepository.countBookedTickets(booking.getScheduleId());
        bookedTickets = bookedTickets != null ? bookedTickets : 0;

        String sql = "SELECT max_tickets FROM Schedules WHERE schedule_id = ?";
        Map<String, Object> schedule = jdbcTemplate.queryForMap(sql, booking.getScheduleId());
        Integer maxTickets = ((Number) schedule.get("max_tickets")).intValue();

        if (bookedTickets < maxTickets) {
            jdbcTemplate.update("UPDATE Schedules SET status = 'Open' WHERE schedule_id = ?", booking.getScheduleId());
            logger.info("Schedule {} reopened due to cancellation", booking.getScheduleId());
        }
    }
}