package top.cch.test.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderRequest(
        @NotNull(message = "用户 ID 不能为空") Long userId,
        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.01", message = "金额必须大于 0") BigDecimal amount,
        String memberLevel,
        @Min(value = 1, message = "数量至少为 1") Integer quantity) {

    public OrderRequest {
        if (memberLevel == null || memberLevel.isBlank()) {
            memberLevel = "NORMAL";
        }
        if (quantity == null) {
            quantity = 1;
        }
    }
}
