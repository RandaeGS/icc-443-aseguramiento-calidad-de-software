<script setup>
import StatsWidget from '@/components/dashboard/StatsWidget.vue';
import MovementsPieChart from '@/components/dashboard/MovementsPieChart.vue';
import MovementsPerDayBarChart from '@/components/dashboard/MovementsPerDayBarChart.vue';
</script>

<template>
    <div class="grid grid-cols-12 gap-8">
        <StatsWidget />

        <div class="col-span-12 xl:col-span-6">
            <MovementsPieChart />
        </div>
        <div class="col-span-12 xl:col-span-6">
            <MovementsPerDayBarChart />
        </div>
    </div>
</template>
