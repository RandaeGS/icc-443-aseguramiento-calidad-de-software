package com.randaegarcia.controller;

import com.randaegarcia.domain.model.Producto;
import com.randaegarcia.service.DashboardService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GET
    @Path("movements-per-day")
    public List<Long> movementsPerDay() {
        return  dashboardService.movementsPerDay();
    }

    @GET
    @Path("movements-per-category")
    public Map<String, Long> movementsPerCategory() {
        return  dashboardService.movementsPerCategory();
    }

    @GET
    @Path("most-moved-product")
    public Map<String, ?> mostMovedProduct() {
        return  dashboardService.mostMovedProduct();
    }

    @GET
    @Path("least-moved-product")
    public Map<String, ?> leastMovedProduct() {
        return dashboardService.leastMovedProduct();
    }

    @GET
    @Path("most-demanded-category")
    public Map<String, ?> mostDemandedCategory() {
        return dashboardService.mostDemandedCategory();
    }

    @GET
    @Path("least-demanded-category")
    public Map<String, ?> leastDemandedCategory() {
        return dashboardService.leastDemandedCategory();
    }
}
