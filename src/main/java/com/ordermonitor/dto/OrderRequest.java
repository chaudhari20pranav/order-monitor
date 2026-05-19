package com.ordermonitor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for placing a new product order.
 */
@Data
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
}
