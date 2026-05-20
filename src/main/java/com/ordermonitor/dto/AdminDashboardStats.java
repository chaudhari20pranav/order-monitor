package com.ordermonitor.dto;

/**
 * Stats shown at the top of the admin dashboard.
 */
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

    public AdminDashboardStats() {}

    public AdminDashboardStats(long totalOrders, long placedOrders, long paidOrders,
                               long shippedOrders, long deliveredOrders, long cancelledOrders,
                               long delayedShipments, long delayedDeliveries, long activeSubscribers) {
        this.totalOrders = totalOrders;
        this.placedOrders = placedOrders;
        this.paidOrders = paidOrders;
        this.shippedOrders = shippedOrders;
        this.deliveredOrders = deliveredOrders;
        this.cancelledOrders = cancelledOrders;
        this.delayedShipments = delayedShipments;
        this.delayedDeliveries = delayedDeliveries;
        this.activeSubscribers = activeSubscribers;
    }

    public long getTotalOrders()         { return totalOrders; }
    public void setTotalOrders(long v)   { this.totalOrders = v; }

    public long getPlacedOrders()        { return placedOrders; }
    public void setPlacedOrders(long v)  { this.placedOrders = v; }

    public long getPaidOrders()          { return paidOrders; }
    public void setPaidOrders(long v)    { this.paidOrders = v; }

    public long getShippedOrders()       { return shippedOrders; }
    public void setShippedOrders(long v) { this.shippedOrders = v; }

    public long getDeliveredOrders()       { return deliveredOrders; }
    public void setDeliveredOrders(long v) { this.deliveredOrders = v; }

    public long getCancelledOrders()       { return cancelledOrders; }
    public void setCancelledOrders(long v) { this.cancelledOrders = v; }

    public long getDelayedShipments()       { return delayedShipments; }
    public void setDelayedShipments(long v) { this.delayedShipments = v; }

    public long getDelayedDeliveries()       { return delayedDeliveries; }
    public void setDelayedDeliveries(long v) { this.delayedDeliveries = v; }

    public long getActiveSubscribers()       { return activeSubscribers; }
    public void setActiveSubscribers(long v) { this.activeSubscribers = v; }
}