package top.cch.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CacheResult<T> {

    private T data;

    private String source;

    private double costMs;
}
