<script setup>
import { ProductService } from '@/service/ProductService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { nextTick, onMounted, reactive, ref, watch } from 'vue';
import axiosInstance from '@/plugins/axios';
import { z } from 'zod';
import { Form } from '@primevue/forms';

onMounted(() => {
    ProductService.getProducts().then((data) => (products.value = data));
});

const toast = useToast();
const dt = ref();
const products = ref();
const productDialog = ref(false);
const deleteProductDialog = ref(false);
const deleteProductsDialog = ref(false);
const selectedProducts = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

function formatCurrency(value) {
    if (value) return value.toLocaleString('en-US', { style: 'currency', currency: 'DOP' });
}

function openNew() {
    productDialog.value = true;
}

function exportCSV() {
    dt.value.exportCSV();
}

function confirmDeleteSelected() {
    deleteProductsDialog.value = true;
}

function deleteSelectedProducts() {
    products.value = products.value.filter((val) => !selectedProducts.value.includes(val));
    deleteProductsDialog.value = false;
    selectedProducts.value = null;
    toast.add({ severity: 'success', summary: 'Successful', detail: 'Products Deleted', life: 3000 });
}

// Dialog controls
const hideDialog = () => {
    Object.assign(product, cleanProduct);
    errors.value = {};
    productDialog.value = false;
};

//Validation
const errors = ref({});
const productSchema = z.object({
    id: z.any().optional(),
    name: z.string().min(3, { message: 'Name is required (min 3 chars)' }),
    description: z.string().min(3, { message: 'Description is required (min 3 chars)' }),
    category: z.string().min(1, { message: 'Category is required' }),
    price: z.number().gt(0, { message: 'Price must be greater than 0' }),
    cost: z.number().gt(0, { message: 'Cost must be greater than 0' }),
    quantity: z.number().gte(0, { message: 'Quantity must be 0 or greater' }),
    profit: z.number().optional()
});

const validateProduct = () => {
    try {
        productSchema.parse(product);
        errors.value = {};
        return true;
    } catch (error) {
        errors.value = {};
        error.errors.forEach((err) => {
            errors.value[err.path[0]] = err.message;
        });
        return false;
    }
};

//Delete
const confirmDeleteProduct = (prod) => {
    Object.assign(product, prod);
    deleteProductDialog.value = true;
};

const deleteProduct = async () => {
    if (product?.id === undefined) return;
    try {
        await axiosInstance.delete(`/productos/${product.id}`);
        deleteProductDialog.value = false;
        toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Product deleted',
            life: 3000
        });
        await loadList();
    } catch (error) {
        console.error(error);
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Error deleting product',
            life: 3000
        });
    }
};

// Form
const formRef = ref();
const product = reactive({
    id: undefined,
    name: '',
    description: '',
    category: '',
    price: 0,
    cost: 0,
    profit: 0,
    quantity: 0
});

const submitForm = async () => {
    if (validateProduct()) {
        if (product.id === undefined) {
            await saveProduct();
        } else {
            await updateProduct();
        }
    }
};

const updateProduct = async () => {
    try {
        await axiosInstance.put('/productos', product);
        hideDialog();
        toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Product updated',
            life: 3000
        });
        await loadList();
    } catch (error) {
        console.error(error);
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Error updating product',
            life: 3000
        });
    }
};

const saveProduct = async () => {
    try {
        await axiosInstance.post('/productos', product);
        hideDialog();
        toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Product saved',
            life: 3000
        });
        await loadList();
    } catch (error) {
        console.log(error);
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Error saving product',
            life: 3000
        });
    }
};

const editProduct = async (prod) => {
    productDialog.value = true;
    Object.assign(product, prod);
    await nextTick();
};

let cleanProduct = {
    id: undefined,
    name: '',
    description: '',
    category: '',
    price: null,
    cost: null,
    profit: null,
    quantity: null
};

// List
const productPage = ref([]);
const page = ref(0);
const size = ref(5);

const loadList = async () => {
    try {
        const response = await axiosInstance.get('/productos', {
            params: {
                page: page.value,
                size: size.value
            }
        });
        productPage.value = response.data;
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

onMounted(() => {
    loadList();
});

watch(
    () => [product.price, product.cost],
    () => {
        let result = product?.price - product?.cost;
        product.profit = Math.max(0, result) || 0;
    },
    { immediate: true }
);
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <Button label="New" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                    <Button label="Delete" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedProducts || !selectedProducts.length" />
                </template>

                <template #end>
                    <Button label="Export" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                dataKey="id"
                v-model:selection="selectedProducts"
                :value="productPage.content"
                :rows="size"
                :filters="filters"
                :totalRecords="productPage.totalElements"
                @page="paginate"
                lazy
                paginator
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10]"
                currentPageReportTemplate="Showing {first} to {last} of {totalRecords} products"
                show-gridlines
                striped-rows
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Manage Products</h4>
                        <IconField>
                            <InputIcon>
                                <i class="pi pi-search" />
                            </InputIcon>
                            <InputText v-model="filters['global'].value" placeholder="Search..." />
                        </IconField>
                    </div>
                </template>

                <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
                <Column field="id" header="Code" style="min-width: 2rem"></Column>
                <Column field="name" header="Name" style="min-width: 12rem"></Column>
                <Column field="category" header="Category" style="min-width: 10rem"></Column>
                <Column field="price" header="Price" style="min-width: 8rem">
                    <template #body="slotProps">
                        {{ formatCurrency(slotProps.data.price) }}
                    </template>
                </Column>
                <Column field="cost" header="Cost" style="min-width: 8rem">
                    <template #body="slotProps">
                        {{ formatCurrency(slotProps.data.cost) }}
                    </template>
                </Column>
                <Column :exportable="false" style="min-width: 12rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editProduct(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" @click="confirmDeleteProduct(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="productDialog" :style="{ width: '450px' }" header="Product Details" :modal="true">
            <Form ref="formRef" :validateOnBlur="true" @submit="product.value?.id === undefined ? saveProduct() : updateProduct()" class="flex flex-col gap-6">
                <div>
                    <label for="name" class="block font-bold mb-3">Name</label>
                    <InputText id="name" name="name" v-model.trim="product.name" required="true" autofocus fluid />
                    <small v-if="errors.name" class="text-red-500">{{ errors.name }}.</small>
                </div>

                <div>
                    <label for="description" class="block font-bold mb-3">Description</label>
                    <Textarea id="description" name="description" v-model="product.description" required="true" rows="3" cols="20" fluid />
                    <small v-if="errors.description" class="text-red-500">{{ errors.description }}.</small>
                </div>

                <div>
                    <span class="block font-bold mb-4">Category</span>
                    <div class="grid grid-cols-12 gap-4">
                        <div class="flex items-center gap-2 col-span-6">
                            <RadioButton id="category1" v-model="product.category" name="category" value="Accessories" />
                            <label for="category1">Accessories</label>
                        </div>
                        <div class="flex items-center gap-2 col-span-6">
                            <RadioButton id="category2" v-model="product.category" name="category" value="Clothing" />
                            <label for="category2">Clothing</label>
                        </div>
                        <div class="flex items-center gap-2 col-span-6">
                            <RadioButton id="category3" v-model="product.category" name="category" value="Electronics" />
                            <label for="category3">Electronics</label>
                        </div>
                        <div class="flex items-center gap-2 col-span-6">
                            <RadioButton id="category4" v-model="product.category" name="category" value="Fitness" />
                            <label for="category4">Fitness</label>
                        </div>
                    </div>
                    <small v-if="errors.category" class="text-red-500">{{ errors.category }}.</small>
                </div>

                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-6">
                        <label for="price" class="block font-bold mb-3">Price</label>
                        <InputNumber id="price" name="price" v-model="product.price" mode="currency" currency="USD" locale="en-US" fluid />
                        <small v-if="errors.price" class="text-red-500">{{ errors.price }}.</small>
                    </div>
                    <div class="col-span-6">
                        <label for="cost" class="block font-bold mb-3">Cost</label>
                        <InputNumber id="cost" name="cost" v-model="product.cost" mode="currency" currency="USD" locale="en-US" fluid />
                        <small v-if="errors.cost" class="text-red-500">{{ errors.cost }}.</small>
                    </div>
                </div>

                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-6">
                        <label for="profit" class="block font-bold mb-3">Profit</label>
                        <InputNumber id="profit" v-model="product.profit" mode="currency" currency="USD" locale="en-US" fluid />
                    </div>
                    <div class="col-span-6">
                        <label for="quantity" class="block font-bold mb-3">Quantity</label>
                        <InputNumber id="quantity" name="quantity" v-model="product.quantity" integeronly fluid />
                        <small v-if="errors.quantity" class="text-red-500">{{ errors.quantity }}.</small>
                    </div>
                </div>
            </Form>

            <template #footer>
                <Button label="Cancel" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Save" icon="pi pi-check" type="submit" @click="submitForm" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteProductDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product"
                    >Are you sure you want to delete <b>{{ product.name }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteProductDialog = false" />
                <Button label="Yes" icon="pi pi-check" @click="deleteProduct" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteProductsDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product">Are you sure you want to delete the selected products?</span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteProductsDialog = false" />
                <Button label="Yes" icon="pi pi-check" text @click="deleteSelectedProducts" />
            </template>
        </Dialog>
    </div>
</template>
