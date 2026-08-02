package com.aw.common.util;

import org.springframework.beans.BeanUtils;

public class ObjectMapperUtils {
    /**
     * Map từ một object nguồn (Entity) sang object đích (DTO)
     */
    public static <S, T> T map(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi map dữ liệu: " + e.getMessage(), e);
        }
    }
}
