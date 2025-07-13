package com.randaegarcia.domain.dto;

public record ProductoListRequestDto(int page, int size, String name, String category, double minPrice, double maxPrice) { }
