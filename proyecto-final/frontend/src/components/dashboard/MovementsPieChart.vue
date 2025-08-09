<script setup>
import { onMounted, ref } from 'vue';

onMounted(() => {
    chartData.value = setChartData();
    chartOptions.value = setChartOptions();
});

const chartData = ref();
const chartOptions = ref();

const setChartData = () => {
    const documentStyle = getComputedStyle(document.body);

    return {
        labels: ['Fitness', 'Electronics', 'Clothing', 'Accessories'],
        datasets: [
            {
                data: [540, 325, 702, 600],
                backgroundColor: [documentStyle.getPropertyValue('--p-cyan-500'), documentStyle.getPropertyValue('--p-orange-500'), documentStyle.getPropertyValue('--p-gray-500'), documentStyle.getPropertyValue('--p-green-500')],
                hoverBackgroundColor: [documentStyle.getPropertyValue('--p-cyan-400'), documentStyle.getPropertyValue('--p-orange-400'), documentStyle.getPropertyValue('--p-gray-400'), documentStyle.getPropertyValue('--p-green-400')]
            }
        ]
    };
};

const setChartOptions = () => {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--p-text-color');

    return {
        plugins: {
            legend: {
                labels: {
                    usePointStyle: true,
                    color: textColor
                }
            }
        }
    };
};
</script>

<template>
    <div class="card">
        <div class="font-semibold text-xl mb-4">Movements Per Category</div>
        <Chart type="pie" :data="chartData" :options="chartOptions" class="w-full md:w-[30rem] mx-auto" />
    </div>
</template>
