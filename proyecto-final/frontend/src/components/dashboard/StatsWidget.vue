<script setup>
import { onMounted, reactive } from 'vue';
import axiosInstance from '@/plugins/axios';

const mostDemandedCategory = reactive({});
const leastDemandedCategory = reactive({});
const mostOrderedProduct = reactive({ name: '', quantity: 0 });
const leastOrderedProduct = reactive({ name: '', quantity: 0 });

const loadMostDemandedCategory = async () => {
    try {
        const res = await axiosInstance.get('http://localhost:8080/dashboard/most-demanded-category');
        Object.assign(mostDemandedCategory, res.data);
    } catch (error) {
        console.error(error);
    }
};

const loadLeastDemandedCategory = async () => {
    try {
        const res = await axiosInstance.get('http://localhost:8080/dashboard/least-demanded-category');
        Object.assign(leastDemandedCategory, res.data);
    } catch (error) {
        console.error(error);
    }
};

const loadMostOrderedProduct = async () => {
    try {
        const res = await axiosInstance.get('http://localhost:8080/dashboard/most-moved-product');
        Object.assign(mostOrderedProduct, res.data);
    } catch (error) {
        console.error(error);
    }
};

const loadLeastOrderedProduct = async () => {
    try {
        const res = await axiosInstance.get('http://localhost:8080/dashboard/least-moved-product');
        Object.assign(leastOrderedProduct, res.data);
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    loadMostOrderedProduct();
    loadLeastOrderedProduct();
    loadMostDemandedCategory();
    loadLeastDemandedCategory();
});
</script>

<template>
    <div class="col-span-12 lg:col-span-6 xl:col-span-3">
        <div class="card mb-0">
            <div class="flex justify-between mb-4">
                <div>
                    <span class="block text-muted-color font-medium mb-4">Most Ordered Product</span>
                    <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ mostOrderedProduct.name }}</div>
                </div>
                <div class="flex items-center justify-center bg-blue-100 dark:bg-blue-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                    <i class="pi pi-shopping-cart text-blue-500 !text-xl"></i>
                </div>
            </div>
            <span class="text-primary font-medium">{{ mostOrderedProduct.quantity }} orders</span>
        </div>
    </div>

    <div class="col-span-12 lg:col-span-6 xl:col-span-3">
        <div class="card mb-0">
            <div class="flex justify-between mb-4">
                <div>
                    <span class="block text-muted-color font-medium mb-4">Least Ordered Product</span>
                    <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ leastOrderedProduct.name }}</div>
                </div>
                <div class="flex items-center justify-center bg-orange-100 dark:bg-orange-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                    <i class="pi pi-dollar text-orange-500 !text-xl"></i>
                </div>
            </div>
            <span class="text-primary font-medium">{{ leastOrderedProduct.quantity }} orders</span>
        </div>
    </div>
    <div class="col-span-12 lg:col-span-6 xl:col-span-3">
        <div class="card mb-0">
            <div class="flex justify-between mb-4">
                <div>
                    <span class="block text-muted-color font-medium mb-4">Most Demanded Category</span>
                    <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ mostDemandedCategory.description }}</div>
                </div>
                <div class="flex items-center justify-center bg-cyan-100 dark:bg-cyan-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                    <i class="pi pi-users text-cyan-500 !text-xl"></i>
                </div>
            </div>
            <span class="text-primary font-medium">{{ mostDemandedCategory.quantity }} orders</span>
        </div>
    </div>
    <div class="col-span-12 lg:col-span-6 xl:col-span-3">
        <div class="card mb-0">
            <div class="flex justify-between mb-4">
                <div>
                    <span class="block text-muted-color font-medium mb-4">Least Demanded Category</span>
                    <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ leastDemandedCategory.description }}</div>
                </div>
                <div class="flex items-center justify-center bg-purple-100 dark:bg-purple-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                    <i class="pi pi-comment text-purple-500 !text-xl"></i>
                </div>
            </div>
            <span class="text-primary font-medium">{{ leastDemandedCategory.quantity }} orders</span>
        </div>
    </div>
</template>
