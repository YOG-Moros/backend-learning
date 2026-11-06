package top.cch.test.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResult(BigDecimal originAmount,
                          BigDecimal discount,
                          BigDecimal finalAmount,
                          List<String> appliedRules) {
}
