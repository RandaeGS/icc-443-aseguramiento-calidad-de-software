<template>
  <v-toolbar density="compact" title="Productos">
    <template #prepend>
      <v-btn icon="mdi-menu"></v-btn>
    </template>

    <v-btn-group class="mr-2" density="compact">
      <v-btn class="bg-red">Eliminar </v-btn>
      <v-btn class="bg-grey" @click="newFormulario">Nuevo</v-btn>
      <v-btn class="bg-green" @click="handleSubmit">Salvar</v-btn>
    </v-btn-group>
  </v-toolbar>

  <v-app>
    <v-row>
      <v-col cols="4">
        <!-- Barra de búsqueda -->
        <v-card class="mb-3" elevation="2">
          <v-card-text class="pa-3">
            <v-row no-gutters align="center">
              <v-col>
                <v-text-field
                  label="Buscar productos..."
                  prepend-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                ></v-text-field>
              </v-col>
              <v-col cols="auto" class="ml-2">
                <v-btn
                  color="primary"
                  icon="mdi-magnify"
                  size="small"
                  variant="elevated"
                ></v-btn>
              </v-col>
              <v-col cols="auto" class="ml-1">
                <v-btn
                  color="grey-darken-1"
                  icon="mdi-filter-variant"
                  size="small"
                  variant="elevated"
                ></v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <!-- Lista de productos -->
        <v-card elevation="2">
          <v-card-title class="pa-3">
            <v-icon class="mr-2">mdi-package-variant</v-icon>
            Productos ({{ productoList.content?.length || 0 }})
          </v-card-title>

          <v-divider></v-divider>

          <v-list density="compact" class="pa-0" style="overflow-y: auto">
            <v-list-item
              v-for="(producto, i) in productoList.content"
              :key="producto.id"
              :value="producto"
              color="primary"
              class="border-b"
              @click="selectProducto(producto)"
            >
              <template v-slot:prepend>
                <v-avatar size="40" color="blue" class="mr-3">
                  <v-icon size="20" color="white"> mdi-package-variant </v-icon>
                </v-avatar>
              </template>

              <v-list-item-title
                class="text-subtitle-2 font-weight-medium mb-1"
              >
                {{ producto.nombre }}
              </v-list-item-title>

              <v-list-item-subtitle class="text-caption">
                <div class="d-flex align-center justify-space-between">
                  <span class="text-grey-darken-1">ID: {{ producto.id }}</span>
                  <v-chip
                    size="x-small"
                    color="blue"
                    variant="outlined"
                    class="ml-2"
                  >
                    {{ producto.categoria || "Sin categoría" }}
                  </v-chip>
                </div>
                <div class="mt-1">
                  <span class="text-success font-weight-medium">
                    ${{ producto.precio || "0" }}
                  </span>
                  <span class="text-grey-darken-1 ml-2">
                    Stock: {{ producto.cantidadInicial || 0 }}
                  </span>
                </div>
              </v-list-item-subtitle>

              <template v-slot:append>
                <div class="d-flex flex-column align-center">
                  <v-btn
                    icon="mdi-dots-vertical"
                    variant="text"
                    size="small"
                  ></v-btn>
                  <v-chip
                    v-if="!producto.borrado"
                    size="x-small"
                    color="success"
                    variant="outlined"
                  >
                    Activo
                  </v-chip>
                  <v-chip v-else size="x-small" color="error">
                    Inactivo
                  </v-chip>
                </div>
              </template>
            </v-list-item>

            <!-- Estado vacío -->
            <v-list-item
              v-if="!productoList.content || productoList.content.length === 0"
            >
              <div class="text-center pa-8 w-100">
                <v-icon size="64" color="grey-lighten-2"
                  >mdi-package-variant-remove</v-icon
                >
                <p class="text-h6 text-grey mt-3 mb-1">No hay productos</p>
                <p class="text-body-2 text-grey">
                  Agrega tu primer producto para comenzar
                </p>
                <v-btn color="primary" variant="outlined" class="mt-3">
                  <v-icon class="mr-2">mdi-plus</v-icon>
                  Nuevo Producto
                </v-btn>
              </div>
            </v-list-item>

            <!-- Loading state -->
            <v-list-item v-if="loadingProductos">
              <div class="text-center pa-6 w-100">
                <v-progress-circular
                  indeterminate
                  color="primary"
                  size="40"
                ></v-progress-circular>
                <p class="text-body-2 text-grey mt-3">Cargando productos...</p>
              </div>
            </v-list-item>
          </v-list>

          <!-- Paginación -->
          <v-divider></v-divider>
          <v-card-actions class="pa-3">
            <div class="d-flex w-100 justify-center">
              <v-pagination
                :length="productoList.totalPages || 1"
                :model-value="(productoList.page || 0) + 1"
                :total-visible="5"
                density="compact"
                variant="elevated"
              ></v-pagination>
            </div>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col>
        <v-form ref="form">
          <v-container fluid>
            <v-row>
              <!-- Primera columna -->
              <v-col cols="12" md="6">
                <v-text-field
                  label="Codigo"
                  prepend-icon="$vuetify"
                  v-model.number="productoForm.id"
                  inputmode="numeric"
                  variant="outlined"
                  disabled
                ></v-text-field>

                <v-autocomplete
                  label="Categoria"
                  prepend-icon="$vuetify"
                  v-model="productoForm.categoria"
                  :rules="[rules.required]"
                  :items="categoriaItems"
                  variant="outlined"
                ></v-autocomplete>

                <v-autocomplete
                  label="Impuesto"
                  prepend-icon="$vuetify"
                  v-model="productoForm.impuesto"
                  variant="outlined"
                  :items="impuestoItems"
                ></v-autocomplete>
              </v-col>

              <!-- Segunda columna -->
              <v-col cols="12" md="6">
                <v-text-field
                  label="Nombre"
                  prepend-icon="$vuetify"
                  v-model="productoForm.nombre"
                  :rules="[rules.required]"
                  variant="outlined"
                ></v-text-field>

                <v-row dense>
                  <v-col>
                    <v-number-input
                      label="Precio"
                      prepend-icon="$vuetify"
                      v-model.number="productoForm.precio"
                      variant="outlined"
                      :rules="[rules.required]"
                      :precision="2"
                      @focus="$event.target.select()"
                      :min="0"
                      :max="999999"
                    ></v-number-input>
                  </v-col>

                  <v-col>
                    <v-number-input
                      label="Costo"
                      prepend-icon="$vuetify"
                      v-model="productoForm.costo"
                      variant="outlined"
                      :rules="[rules.required]"
                      :precision="2"
                      @focus="$event.target.select()"
                      :min="0"
                      :max="999999"
                    ></v-number-input>
                  </v-col>
                </v-row>

                <v-row dense>
                  <v-col>
                    <v-number-input
                      label="Beneficio"
                      prepend-icon="$vuetify"
                      v-model="productoForm.beneficio"
                      variant="outlined"
                      :precision="2"
                      control-variant="hidden"
                      :min="0"
                      :max="999999"
                      disabled
                    ></v-number-input>
                  </v-col>

                  <v-col>
                    <v-number-input
                      label="Cantidad inicial"
                      prepend-icon="$vuetify"
                      v-model="productoForm.cantidadInicial"
                      variant="outlined"
                      :rules="[rules.required]"
                      :min="0"
                      :max="999999"
                      @focus="$event.target.select()"
                    ></v-number-input>
                  </v-col>
                </v-row>
              </v-col>

              <v-col cols="12">
                <v-textarea
                  label="Descripcion"
                  prepend-icon="$vuetify"
                  v-model="productoForm.descripcion"
                  :rules="[rules.required]"
                  variant="outlined"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-container>
        </v-form>
      </v-col>
    </v-row>
  </v-app>
</template>

<script lang="ts" setup>
import type { Producto } from "@/models/Producto";
import { ref } from "vue";
import axiosInstance from "@/plugins/axios";

// Lista
const page = ref<number>(0);
const size = ref<number>(10);
onMounted(() => {
  loadProductos();
});

const productoList = ref([]);
const loadProductos = async () => {
  try {
    const response = await axiosInstance.get("/productos", {
      params: {
        page: page.value,
        size: size.value,
      },
    });
    productoList.value = response.data;
  } catch (error) {
    console.log("error");
  }
};

const selectProducto = (selected: Producto) => {
  console.log(selected);
  Object.assign(productoForm, selected);
};

// Formulario
const form = ref(null);
const isEditingMode = ref<boolean>(false);

const productoForm = reactive<Producto>({
  id: undefined,
  nombre: "",
  descripcion: "",
  categoria: "",
  precio: 0,
  costo: 0,
  beneficio: 0,
  impuesto: undefined,
  cantidadInicial: 0,
});

const impuestoItems = [18, 11];
const categoriaItems = [
  "Ropa",
  "Electrodomesticos",
  "Medicina",
  "Alimentos",
  "Varios",
];

// Funciones de formulario
const handleSubmit = async () => {
  await form.value.validate();
  if (!form.value.isValid) {
    console.log("Formulario invalido");
    return;
  }

  let response: any = undefined;
  if (productoForm.id == undefined) {
    response = await axiosInstance.post("/productos", productoForm);
    console.log(response);
    newFormulario();
  } else {
    response = await axiosInstance.put("/productos", productoForm);
    console.log(response);
    newFormulario();
  }
};

const newFormulario = async () => {
  productoForm.id = undefined;
  productoForm.nombre = "";
  productoForm.descripcion = "";
  productoForm.categoria = "";
  productoForm.precio = 0;
  productoForm.costo = 0;
  productoForm.beneficio = 0;
  productoForm.cantidadInicial = 0;
  productoForm.impuesto = undefined;

  await nextTick();
  form.value.resetValidation();
};

const calcularBeneficio = () => {
  const precio = Number(productoForm.precio) || 0;
  const costo = Number(productoForm.costo) || 0;
  const impuesto = Number(productoForm.impuesto) || 0;

  const margen = precio - costo;
  const factorImpuesto = 1 - impuesto / 100;
  const beneficio = margen * factorImpuesto;

  return Math.max(0, Number(beneficio.toFixed(2)));
};

// Reglas
const rules = {
  required: (value: any) => !!value || "Campo requerido",

  positive: (value: number) => value >= 0 || "Debe ser positivo",

  email: (value: string) => {
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return !value || pattern.test(value) || "Email inválido";
  },

  notEmpty: (value: string) => {
    return (value && value.trim().length > 0) || "No puede estar vacío";
  },

  numeric: (value: string) => {
    return !value || !isNaN(Number(value)) || "Debe ser un número";
  },
};

// Watchers
watch(
  () => [productoForm.precio, productoForm.costo, productoForm.impuesto],
  () => {
    productoForm.beneficio = calcularBeneficio();
  },
  { immediate: true },
);
</script>
