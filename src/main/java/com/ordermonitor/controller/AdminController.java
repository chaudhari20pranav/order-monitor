package com.ordermonitor.controller;

import com.ordermonitor.dto.OrderResponse;
import com.ordermonitor.repository.OrderRepository;
import com.ordermonitor.service.AuditService;
import com.ordermonitor.service.OrderService;
import com.ordermonitor.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin dashboard – view all orders, update statuses, monitor delays.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final AuditService auditService;
    private final UserService userService;
    private final OrderRepository orderRepository;

    public AdminController(OrderService orderService, AuditService auditService,
                           UserService userService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.auditService = auditService;
        this.userService = userService;
        this.orderRepository = orderRepository;
    }

    // ---------------------------------------------------------------
    // Dashboard page
    // ---------------------------------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!userService.isLoggedIn(session) || !userService.isAdmin(session)) {
            return "redirect:/login";
        }

        userService.updateLastActive(userService.getSessionUserId(session));

        model.addAttribute("stats", orderService.getAdminStats());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("recentEvents", auditService.getRecentEvents());
        model.addAttribute("userName", session.getAttribute("userName"));

        LocalDateTime ship24h = LocalDateTime.now().minusHours(24);
        LocalDateTime deliver48h = LocalDateTime.now().minusHours(48);
        model.addAttribute("delayedShipments", orderRepository.findDelayedShipments(ship24h));
        model.addAttribute("delayedDeliveries", orderRepository.findDelayedDeliveries(deliver48h));

        return "admin/dashboard";
    }

    // ---------------------------------------------------------------
    // Update order status (REST endpoint called by JS)
    // ---------------------------------------------------------------

    @PostMapping("/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          HttpSession session) {
        if (!userService.isLoggedIn(session) || !userService.isAdmin(session)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body("Status is required");
        }

        List<String> validStatuses = List.of("PLACED", "PAID", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(newStatus)) {
            return ResponseEntity.badRequest().body("Invalid status: " + newStatus);
        }

        try {
            OrderResponse updated = orderService.adminUpdateStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Dashboard stats (polled by JS on load, then WS handles updates)
    // ---------------------------------------------------------------

    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getStats(HttpSession session) {
        if (!userService.isLoggedIn(session) || !userService.isAdmin(session)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        return ResponseEntity.ok(orderService.getAdminStats());
    }
}