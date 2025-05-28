package com.example.kingpool.repository;

import com.example.kingpool.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByUserId(Integer userId);
}
// This interface extends JpaRepository to provide CRUD operations for Ticket entities.