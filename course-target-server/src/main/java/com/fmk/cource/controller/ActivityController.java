package com.fmk.cource.controller;

import com.fmk.cource.common.Result;
import com.fmk.cource.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
@Tag(name = "系统活动日志(Redis)")
@RestController
@RequestMapping(value = "/api/activity", produces = "application/json")
@RequiredArgsConstructor
public class ActivityController {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取当前用户的最近活动")
    @GetMapping
    public Result<List<Map<String, Object>>> getActivities() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return Result.success(java.util.Collections.emptyList());
        }
        String key = "activity:logs:user:" + userId;
        try {
            Long size = stringRedisTemplate.opsForList().size(key);
            if (size == null || size == 0) {
                return Result.success(java.util.Collections.emptyList());
            }
            List<String> logsStr = stringRedisTemplate.opsForList().range(key, 0, 9);
            if (logsStr == null) {
                return Result.success(java.util.Collections.emptyList());
            }

            List<Map<String, Object>> result = logsStr.stream().map(str -> {
                try {
                    return objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    log.error("Failed to parse activity log", e);
                    return null;
                }
            }).filter(java.util.Objects::nonNull).collect(Collectors.toList());

            return Result.success(result);
        } catch (Exception e) {
            log.error("Failed to fetch activities from Redis", e);
            return Result.success(java.util.Collections.emptyList());
        }
    }
}
