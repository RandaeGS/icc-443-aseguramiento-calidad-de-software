package com.randaegarcia.domain.dto;

import java.time.LocalDateTime;

public record QuantityHistoryDto(
        String username,
        LocalDateTime revisionDate,
        Long quantity,
        Long previousQuantity,
        Long quantityChange
) { }
