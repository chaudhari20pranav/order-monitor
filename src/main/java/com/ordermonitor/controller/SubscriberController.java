package com.ordermonitor.controller;

import com.ordermonitor.dto.OrderRequest;
import com.ordermonitor.dto.OrderResponse;
import com.ordermonitor.service.NotificationService;
import com.ordermonitor.service.OrderService;
import com.ordermonitor.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Subscriber dashboard – place, pay, cancel orders; view notifications.
 */
@Controller
@RequestMapping("/subscriber")
public class SubscriberController {

    private final OrderService orderService;
    private final NotificationService notificationService;
    private final UserService userService;

    public SubscriberController(OrderService orderService,
                                 NotificationService notificationService,
                                 UserService userService) {
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // ---------------------------------------------------------------
    // Dashboard page
    // ---------------------------------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!userService.isLoggedIn(session)) return "redirect:/login";
        if (userService.isAdmin(session)) return "redirect:/admin/dashboard";

        Long userId = userService.getSessionUserId(session);
        userService.updateLastActive(userId);

        List<OrderResponse> allOrders = orderService.getOrdersForUser(userId);

        model.addAttribute("orders", allOrders);
        model.addAttribute("liveOrders",
                allOrders.stream().filter(o -> List.of("PLACED", "PAID", "SHIPPED").contains(o.getOrderStatus())).toList());
        model.addAttribute("completedOrders",
                allOrders.stream().filter(o -> "DELIVERED".equals(o.getOrderStatus())).toList());
        model.addAttribute("cancelledOrders",
                allOrders.stream().filter(o -> "CANCELLED".equals(o.getOrderStatus())).toList());
        model.addAttribute("notifications", notificationService.getForUser(userId));
        model.addAttribute("unreadCount", notificationService.unreadCount(userId));
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userId", userId);

        return "subscriber/dashboard";
    }

    // ---------------------------------------------------------------
    // Place Order (REST – called by form submit via JS)
    // ---------------------------------------------------------------

    @PostMapping("/orders")
    @ResponseBody
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest request,
                                        HttpSession session) {
        if (!userService.isLoggedIn(session)) return ResponseEntity.status(401).body("Not logged in");
        if (userService.isAdmin(session)) return ResponseEntity.status(403).body("Admins cannot place orders");

        Long userId = userService.getSessionUserId(session);
        try {
            OrderResponse response = orderService.placeOrder(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Pay for Order
    // ---------------------------------------------------------------

    @PostMapping("/orders/{id}/pay")
    @ResponseBody
    public ResponseEntity<?> payOrder(@PathVariable Long id, HttpSession session) {
        if (!userService.isLoggedIn(session)) return ResponseEntity.status(401).body("Not logged in");

        Long userId = userService.getSessionUserId(session);
        try {
            OrderResponse response = orderService.payOrder(id, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Cancel Order
    // ---------------------------------------------------------------

    @PostMapping("/orders/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpSession session) {
        if (!userService.isLoggedIn(session)) return ResponseEntity.status(401).body("Not logged in");

        Long userId = userService.getSessionUserId(session);
        try {
            OrderResponse response = orderService.cancelOrder(id, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Mark notifications as read
    // ---------------------------------------------------------------

    @PostMapping("/notifications/read")
    @ResponseBody
    public ResponseEntity<?> markAllRead(HttpSession session) {
        if (!userService.isLoggedIn(session)) return ResponseEntity.status(401).build();
        notificationService.markAllRead(userService.getSessionUserId(session));
        return ResponseEntity.ok(Map.of("success", true));
    }
}