package com.ordermonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stats shown at the top of the admin dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {

    private long totalOrders;
    private long placedOrders;
    private long paidOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long delayedShipments;
    private long delayedDeliveries;
    private long activeSubscribers;
}
