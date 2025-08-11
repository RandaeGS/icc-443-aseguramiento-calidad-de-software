<script setup>
import { onMounted, ref } from 'vue';
import axiosInstance from '@/plugins/axios';

const chartData = ref();
const chartOptions = ref();
const data = ref(null);

const setChartData = () => {
    const documentStyle = getComputedStyle(document.body);

    return {
        labels: Object.keys(data.value),
        datasets: [
            {
                data: Object.values(data.value),
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

const loadData = async () => {
    try {
        const res = await axiosInstance.get('http://localhost:8080/dashboard/movements-per-category');
        data.value = res.data;
    } catch (error) {
        console.error(error);
    }
};

onMounted(async () => {
    await loadData();
    chartData.value = setChartData();
    chartOptions.value = setChartOptions();
});
</script>

<template>
    <div class="card">
        <div class="font-semibold text-xl mb-4">Movements Per Category</div>
        <Chart type="pie" :data="chartData" :options="chartOptions" class="w-full md:w-[30rem] mx-auto" />
    </div>
</template>
