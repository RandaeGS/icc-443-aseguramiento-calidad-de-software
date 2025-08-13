package com.randaegarcia.service;

import com.randaegarcia.domain.model.ProductCategory;
import com.randaegarcia.domain.model.StockMovement;
import com.randaegarcia.domain.model.StockMovement$;
import com.speedment.jpastreamer.application.JPAStreamer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DashboardService {
    private final JPAStreamer jpaStreamer;

    public List<Long> movementsPerDay() {
        Map<DayOfWeek, Long> movementsByDay = jpaStreamer.stream(StockMovement.class)
                .filter( stockMovement -> stockMovement.producto.isActive == true)
                .collect(Collectors.groupingBy(
                        o -> o.date.getDayOfWeek(), Collectors.counting()
                ));

        Long[] result = new Long[7];
        Arrays.fill(result, 0L);

        movementsByDay.forEach((day, count) -> {
            result[day.ordinal()] += count;
        });
        jpaStreamer.close();
        return Arrays.asList(result);
    }

    public Map<String, Long> movementsPerCategory(){
        Map<String, Long> movementsByCategory = jpaStreamer.stream(StockMovement.class)
                .filter( stockMovement -> stockMovement.producto.isActive == true)
                .collect(Collectors.groupingBy(
                        o -> o.producto.category.name(), Collectors.counting()
                ));

        Map<String, Long> result = Arrays.stream(ProductCategory.values())
                .collect(Collectors.toMap(
                        ProductCategory::name,
                        category -> movementsByCategory.getOrDefault(category.name(), 0L)
                ));

        return result;
    }

    public Map<String, ?> mostMovedProduct(){
        return jpaStreamer.stream(StockMovement.class)
                .filter( stockMovement -> stockMovement.producto.isActive == true)
                .collect(Collectors.groupingBy(
                        o -> o.producto, Collectors.counting()
                ))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> Map.of(
                        "name", entry.getKey().name,
                        "quantity", entry.getValue()
                )).orElse(Collections.emptyMap());
    }


    public Map<String, ?> leastMovedProduct(){
        return jpaStreamer.stream(StockMovement.class)
                .filter( stockMovement -> stockMovement.producto.isActive == true)
                .collect(Collectors.groupingBy(
                        o -> o.producto, Collectors.counting()
                ))
                .entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> Map.of(
                        "name", entry.getKey().name,
                        "quantity", entry.getValue()
                )).orElse(Collections.emptyMap());
    }

    public Map<String, ?> mostDemandedCategory() {
        return jpaStreamer.stream(StockMovement.class)
                .filter( stockMovement -> stockMovement.producto.isActive == true)
                .collect(Collectors.groupingBy(
                        o -> o.producto.category, Collectors.counting()
                ))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> Map.of(
                        "description", entry.getKey(),
                        "quantity", entry.getValue()
                )).orElse(Collections.emptyMap());
    }

    public Map<String, ?> leastDemandedCategory() {
        return jpaStreamer.stream(StockMovement.class)
                .filter( stockMovement -> stockMovement.producto.isActive == true)
                .collect(Collectors.groupingBy(
                        o -> o.producto.category, Collectors.counting()
                ))
                .entrySet().stream()
                .min(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> Map.of(
                        "description", entry.getKey(),
                        "quantity", entry.getValue()
                )).orElse(Collections.emptyMap());
    }
}
