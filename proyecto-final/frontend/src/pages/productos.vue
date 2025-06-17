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
                :rules="[rules.required]"
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
</template>

<script lang="ts" setup>
import type { Producto } from "@/models/Producto";
import { ref } from "vue";
import axiosInstance from "@/plugins/axios";

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
  if (form.value.validate()) {
    console.log("hola");
    return;
  }
  if (!isEditingMode.value) {
    await axiosInstance.post("/productos");
  } else {
    await axiosInstance.put("/productos");
  }
};

const newFormulario = () => {
  form.value.resetValidation();
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
</script>
