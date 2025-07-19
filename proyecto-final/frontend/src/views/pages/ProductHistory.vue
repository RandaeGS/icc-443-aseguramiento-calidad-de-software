<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue';
import axiosInstance from '@/plugins/axios';
import { useToast } from 'primevue/usetoast';
import { useRouter } from 'vue-router';

const toast = useToast();
const router = useRouter();

const props = defineProps({
    id: {
        type: [String, Number],
        required: true
    }
});

// List
const historyPage = ref([]);
const page = ref(0);
const size = ref(10);

const loadList = async () => {
    try {
        const response = await axiosInstance.get(`/productos/${props.id}/history`, {
            params: {
                page: page.value,
                size: size.value
            }
        });
        historyPage.value = response.data;
    } catch (error) {
        console.error(error);
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Error loading products',
            life: 3000
        });
    }
};

const paginate = async (paginator) => {
    page.value = paginator.page;
    size.value = paginator.rows;
    await loadList();
};

// Product
const product = reactive({});
const findProduct = async () => {
    try {
        const response = await axiosInstance.get(`/productos/${props.id}`);
        Object.assign(product, response.data);
    } catch (error) {
        console.error(error);
    }
};

// Helper functions
const formatDate = (dateString) => {
    if (!dateString) return '-';

    const date = new Date(dateString);

    // Extraer componentes
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();

    return `${hours}:${minutes}:${seconds} ${day}/${month}/${year}`;
};

// Hooks
onMounted(() => {
    nextTick();
    loadList();
    findProduct();
});
</script>

<template>
    <div class="card">
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Go back to products" icon="pi pi-undo" severity="primary" class="mr-2" @click="router.push({ name: 'products' })" />
            </template>

            <template #end>
                <div>
                    <p class="text-xl font-semibold">{{ product.id }} - {{ product.name }} - {{ product.category }}</p>
                </div>
            </template>
        </Toolbar>

        <DataTable
            ref="dt"
            dataKey="revisionDate"
            :value="historyPage.content"
            :rows="size"
            :totalRecords="historyPage.totalElements"
            @page="paginate"
            lazy
            paginator
            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
            :rowsPerPageOptions="[10, 20]"
            currentPageReportTemplate="Showing {first} to {last} of {totalRecords} products"
            show-gridlines
            striped-rows
        >
            <Column field="actualQuantity" header="Quantity" style="min-width: 2rem"></Column>
            <Column field="quantityChange" header="Quantity change" style="min-width: 2rem"></Column>
            <Column field="date" header="Date" style="min-width: 2rem">
                <template #body="slotProps">
                    {{ formatDate(slotProps.data.date) }}
                </template>
            </Column>
            <Column field="username" header="By user" style="min-width: 2rem"></Column>
        </DataTable>
    </div>
</template>

<style scoped lang="scss"></style>
