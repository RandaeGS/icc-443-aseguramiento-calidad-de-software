<script setup>
import { ProductService } from '@/service/ProductService';
import { useToast } from 'primevue/usetoast';
import { nextTick, onMounted, reactive, ref, watch } from 'vue';
import axiosInstance from '@/plugins/axios';
import { z } from 'zod';
import { Form } from '@primevue/forms';
import { useKeycloak } from '@dsb-norge/vue-keycloak-js';
import router from '@/router';

onMounted(() => {
    ProductService.getProducts().then((data) => (products.value = data));
});

const keycloak = useKeycloak();
const toast = useToast();
const dt = ref();
const products = ref();
const productDialog = ref(false);
const deleteProductDialog = ref(false);
const deleteProductsDialog = ref(false);
const selectedProducts = ref();

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
    errors.value = {};
    productDialog.value = false;
    deleteProductsDialog.value = false;
    deleteProductDialog.value = false;
    stockDialog.value = false;
    stockQuantity.value = 0;
    Object.assign(product, cleanProduct);
};

//Validation
const errors = ref({});
const productSchema = z
    .object({
        id: z.any().optional(),
        name: z.string().min(3, { message: 'Name is required (min 3 chars)' }),
        description: z.string().min(3, { message: 'Description is required (min 3 chars)' }),
        category: z.string().min(1, { message: 'Category is required' }),
        price: z.number().gt(0, { message: 'Price must be greater than 0' }),
        cost: z.number().gt(0, { message: 'Cost must be greater than 0' }),
        quantity: z.number().gte(0, { message: 'Quantity must be 0 or greater' }),
        minimumStock: z.number().gte(0, { message: 'Minimum stock must be 0 or greater' }),
        profit: z.number().optional()
    })
    .refine((data) => data.quantity >= data.minimumStock, {
        message: 'Quantity must be greater than or equal to minimum stock',
        path: ['quantity']
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
    quantity: 0,
    minimumStock: 0
});

const categoryList = ['Fitness', 'Electronics', 'Clothing', 'Accessories'];

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
    quantity: null,
    minimumStock: null
};

// List
const productPage = ref([]);
const page = ref(0);
const size = ref(5);
const filters = reactive({
    name: undefined,
    maxPrice: undefined,
    minPrice: undefined,
    category: undefined
});
const filtersDialog = ref(false);

const applyFilters = () => {
    if (filters.minPrice > filters.maxPrice) {
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Min price cannot be greater',
            life: 3000
        });
        return;
    }

    filtersDialog.value = false;
    loadList();
};

const clearFilters = () => {
    filters.minPrice = undefined;
    filters.maxPrice = undefined;
    filters.name = undefined;
    filters.category = undefined;
    filtersDialog.value = false;
    loadList();
};

const loadList = async () => {
    try {
        const response = await axiosInstance.get('/productos', {
            params: {
                page: page.value,
                size: size.value,
                name: filters.name,
                category: filters.category,
                minPrice: filters.minPrice,
                maxPrice: filters.maxPrice
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

// Stock
const stockDialog = ref(false);
const stockQuantity = ref(0);
const stockErrors = ref({});
const stockSchema = z.object({
    stockQuantity: z.number().refine((val) => val !== 0, { message: 'Quantity cannot be 0' })
});

const showStockDialog = (selectedProduct) => {
    Object.assign(product, selectedProduct);
    stockErrors.value = {};
    stockDialog.value = true;
};

const validateStockMovement = () => {
    try {
        stockSchema.parse({ stockQuantity: stockQuantity.value });
        stockErrors.value = {};
        return true;
    } catch (error) {
        stockErrors.value = {};
        error.errors.forEach((err) => {
            stockErrors.value[err.path[0]] = err.message;
        });
        console.log(stockErrors.value);
        return false;
    }
};

const productMovement = async () => {
    if (!validateStockMovement()) return;
    try {
        await axiosInstance.put(
            `/productos/${product.id}/update-quantity`,
            {},
            {
                params: {
                    quantity: stockQuantity.value
                }
            }
        );
        toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Quantity has been updated',
            life: 3000
        });
        hideDialog();
        loadList();
    } catch (error) {
        let errorMessage = error.response.data.message;
        if (errorMessage === 'Minimum stock exceeded') {
            stockError.value = errorMessage;
        }
        console.error(error.response.data.message);
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Error updating quantity',
            life: 3000
        });
    }
};

// Hooks
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

const callIntegrationApi = async () => {
    try {
        const response = await axiosInstance.get('http://localhost:8081/integrations');
        toast.add({
            severity: 'success',
            summary: 'Success',
            detail: response.data,
            life: 3000
        });
    } catch (error) {
        console.error(error);
        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Error during process',
            life: 3000
        });
    }
};

const viewHistory = (val) => {
    router.push({ name: 'history', params: { id: val.id } });
};

const getRowClass = (data) => {
    if (data.quantity <= data.minimumStock * 1.2) {
        return '!bg-orange-100';
    }
    return '';
};
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
                    <div class="flex items-center gap-2">
                        <Button label="Operate" icon="pi pi-receipt" severity="secondary" @click="callIntegrationApi" v-if="keycloak.hasRealmRole('admin')" />
                        <Button label="Export" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                    </div>
                </template>
            </Toolbar>

            <DataTable
                id="table"
                ref="dt"
                dataKey="id"
                v-model:selection="selectedProducts"
                :value="productPage.content"
                :rows="size"
                :totalRecords="productPage.totalElements"
                :rowClass="getRowClass"
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
                        <div class="flex items-center gap-2 text-sm text-gray-600">
                            <h4 class="m-0">Manage Products</h4>
                            <div class="w-4 h-4 bg-orange-100 border border-orange-200 rounded"></div>
                            <span>Low Stock</span>
                        </div>
                        <div class="flex gap-2">
                            <Button id="filter" icon="pi pi-filter" @click="filtersDialog = true" />
                            <InputGroup>
                                <InputText v-model="filters.name" @keyup.enter="loadList" placeholder="Search..." />
                                <InputGroupAddon>
                                    <Button id="search-button" icon="pi pi-search" severity="secondary" variant="text" @click="loadList" />
                                </InputGroupAddon>
                            </InputGroup>
                        </div>
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
                <Column header="Stock" :exportable="false" style="min-width: 1rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-arrow-right-arrow-left" outlined rounded class="mr-2 update-stock-button" @click="showStockDialog(slotProps.data)" />
                        <Button icon="pi pi-warehouse" outlined rounded severity="info" @click="viewHistory(slotProps.data)" />
                    </template>
                </Column>
                <Column header="Actions" :exportable="false" style="min-width: 2rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2 edit-product" @click="editProduct(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" class="delete-product" @click="confirmDeleteProduct(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog id="productDialog" v-model:visible="productDialog" :style="{ width: '450px' }" header="Product Details" :modal="true">
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

                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-6">
                        <label for="category" class="block font-bold mb-3">Category</label>
                        <Select id="category" name="category" v-model="product.category" :options="categoryList" fluid />
                        <small v-if="errors.category" class="text-red-500">{{ errors.category }}.</small>
                    </div>
                    <div class="col-span-6">
                        <label for="minimumStock" class="block font-bold mb-3">Min. Stock</label>
                        <InputNumber id="minimumStock" name="minimumStock" v-model="product.minimumStock" fluid />
                        <small v-if="errors.minimumStock" class="text-red-500">{{ errors.minimumStock }}.</small>
                    </div>
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
                        <InputNumber id="profit" v-model="product.profit" mode="currency" currency="USD" locale="en-US" fluid readonly />
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
                <Button label="No" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Yes" icon="pi pi-check" @click="deleteProduct" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteProductsDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product">Are you sure you want to delete the selected products?</span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Yes" icon="pi pi-check" text @click="deleteSelectedProducts" />
            </template>
        </Dialog>

        <Dialog id="filters-dialog" v-model:visible="filtersDialog" header="Filters" :modal="true">
            <div class="flex flex-col gap-4">
                <div>
                    <label for="categoryFilter" class="block font-bold mb-3">Category</label>
                    <Select id="categoryFilter" name="categoryFilter" v-model.trim="filters.category" :options="categoryList" autofocus fluid />
                </div>

                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-6">
                        <label for="minPrice" class="block font-bold mb-3">Minimum Price</label>
                        <InputNumber id="minPrice" name="minPrice" v-model="filters.minPrice" mode="currency" currency="USD" locale="en-US" fluid />
                    </div>
                    <div class="col-span-6">
                        <label for="minPrice" class="block font-bold mb-3">Maximum Price</label>
                        <InputNumber id="maxPrice" name="maxPrice" v-model="filters.maxPrice" mode="currency" currency="USD" locale="en-US" fluid />
                    </div>
                </div>
            </div>

            <template #footer>
                <Button label="Clear" icon="pi pi-filter-slash" text @click="clearFilters" />
                <Button label="Apply" icon="pi pi-filter" text @click="applyFilters" />
            </template>
        </Dialog>

        <Dialog id="stockDialog" v-model:visible="stockDialog" header="Product Movement" class="lg:w-1/4 sm:w-1/2" :modal="true">
            <div class="flex flex-col gap-4">
                <div>
                    <label for="stockProduct" class="block font-bold mb-3">Product</label>
                    <InputText id="stockProduct" name="stockProduct" v-model="product.name" fluid readonly />
                </div>
                <div class="grid grid-cols-12 gap-4">
                    <div class="col-span-6">
                        <label for="stock" class="block font-bold mb-3">Minimum stock</label>
                        <InputText id="minimumStock" name="stock" v-model="product.minimumStock" fluid readonly />
                    </div>
                    <div class="col-span-6">
                        <label for="actualStock" class="block font-bold mb-3">Actual stock</label>
                        <InputText id="actualStock" name="actualStock" v-model="product.quantity" fluid readonly />
                    </div>
                </div>
                <div>
                    <label for="stock" class="block font-bold mb-3">Quantity</label>
                    <InputNumber input-id="stockQuantity" name="stockQuantity" v-model="stockQuantity" fluid />
                    <small v-if="stockErrors.stockQuantity" class="text-red-500 block">{{ stockErrors.stockQuantity }}.</small>
                </div>
            </div>

            <template #footer>
                <Button label="Cancel" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Accept" icon="pi pi-check" text @click="productMovement" />
            </template>
        </Dialog>
    </div>
</template>
