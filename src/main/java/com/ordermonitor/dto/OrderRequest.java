package com.ordermonitor.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for placing a new product order.
 */
public class OrderRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must be 100 chars or less")
    private String productName;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must be 50 chars or less")
    private String category;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    public OrderRequest() {}

    public OrderRequest(String productName, String category, Integer quantity, BigDecimal price) {
        this.productName = productName;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName()           { return productName; }
    public void setProductName(String v)     { this.productName = v; }

    public String getCategory()              { return category; }
    public void setCategory(String v)        { this.category = v; }

    public Integer getQuantity()             { return quantity; }
    public void setQuantity(Integer v)       { this.quantity = v; }

    public BigDecimal getPrice()             { return price; }
    public void setPrice(BigDecimal v)       { this.price = v; }
}