<template>
  <div>
    <header class="bg-slate-700 p-1">
      <nav class="flex justify-between">
        <div class="flex content-center gap-1">
          <Button aria-label="Bookmark" icon="pi pi-bars" severity="secondary" variant="text" />
          <h2 class="text-2xl font-bold content-center">Productos</h2>
        </div>
        <div>
          <ButtonGroup>
            <Button icon="pi pi-trash" label="Eliminar" severity="danger"></Button>
            <Button
              icon="pi pi-refresh"
              label="Nuevo"
              severity="secondary"
              @click="newFormulario"
            ></Button>
            <Button
              icon="pi pi-save"
              label="Salvar"
              severity="success"
              @click="submitForm"
            ></Button>
          </ButtonGroup>
        </div>
      </nav>
    </header>

    <main class="flex grow min-h-0">
      <div class="flex flex-col grow mt-2 max-w-3/12">
        <div class="flex">
          <InputGroup>
            <InputText placeholder="Buscar" />
            <Button icon="pi pi-search" />
          </InputGroup>
        </div>

        <div class="flex flex-col grow">
          <div v-if="productoList.length === 0" class="text-center text-gray-500">
            No hay productos disponibles
          </div>

          <div
            v-for="item in productoList.content"
            v-else
            :key="item.id"
            class="flex flex-col mt-2"
          >
            <div class="flex items-center bg-slate-500 text-white p-2 rounded">
              <i class="pi pi-box mr-2" style="font-size: 1.5rem"></i>
              <div class="flex flex-col grow">
                <p class="font-semibold">{{ item.nombre }}</p>
                <p class="text-sm">ID: {{ item.id }}</p>
                <p class="text-sm">Precio: {{ item.precio }}$RD</p>
              </div>
              <p class="text-sm">{{ item.categoria }}</p>
            </div>
          </div>
        </div>

        <Paginator
          :rows="10"
          :totalRecords="productoList.totalElements"
          currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords}"
          template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
        />
      </div>

      <Form
        ref="formRef"
        v-slot="$form"
        :initialValues="productoForm"
        :resolver="resolver"
        :validateOnBlur="true"
        class="flex flex-col grow gap-4 p-2"
        @submit="onFormSubmit"
      >
        <div class="flex flex-row gap-x-2">
          <!-- Column 1 -->
          <div class="flex flex-col gap-4 grow">
            <FloatLabel variant="on">
              <InputText v-model="productoForm.id" fluid name="id" type="text" />
              <label for="codigo">Codigo</label>
            </FloatLabel>

            <Select
              v-model="productoForm.categoria"
              :options="categoriaList"
              name="categoria"
              placeholder="Categoria"
            />
            <Message v-if="$form.categoria?.invalid" severity="error" size="small" variant="simple">
              {{ $form.categoria.error?.message }}
            </Message>

            <Select
              v-model="productoForm.impuesto"
              :options="impuestoList"
              name="impuesto"
              placeholder="Impuesto"
              showClear
            />
          </div>

          <!-- Column 2 -->
          <div class="flex flex-col gap-4 grow">
            <FloatLabel variant="on">
              <InputText id="nombre" v-model="productoForm.nombre" fluid name="nombre" />
              <Message v-if="$form.nombre?.invalid" severity="error" size="small" variant="simple">
                {{ $form.nombre.error?.message }}
              </Message>
              <label for="nombre">Nombre</label>
            </FloatLabel>

            <div class="flex flex-row gap-1">
              <FloatLabel class="grow" variant="on">
                <InputNumber
                  v-model="productoForm.precio"
                  currency="DOP"
                  fluid
                  inputId="precio"
                  locale="en-US"
                  mode="currency"
                  name="precio"
                />
                <Message
                  v-if="$form.precio?.invalid"
                  severity="error"
                  size="small"
                  variant="simple"
                >
                  {{ $form.precio.error?.message }}
                </Message>
                <label for="precio">Precio</label>
              </FloatLabel>

              <FloatLabel class="grow" variant="on">
                <InputNumber
                  v-model="productoForm.costo"
                  class="w-full"
                  currency="DOP"
                  fluid
                  inputId="costo"
                  locale="en-US"
                  mode="currency"
                  name="costo"
                />
                <Message v-if="$form.costo?.invalid" severity="error" size="small" variant="simple">
                  {{ $form.costo.error?.message }}
                </Message>
                <label for="costo">Costo</label>
              </FloatLabel>
            </div>

            <div class="flex flex-row gap-1">
              <FloatLabel class="grow" variant="on">
                <InputNumber
                  v-model="productoForm.beneficio"
                  currency="DOP"
                  fluid
                  inputId="beneficio"
                  locale="en-US"
                  mode="currency"
                  readonly
                />
                <label for="beneficio">Beneficio</label>
              </FloatLabel>
              <FloatLabel class="grow" variant="on">
                <InputNumber
                  v-model="productoForm.cantidadInicial"
                  fluid
                  inputId="cantidadInicial"
                  locale="en-US"
                  name="cantidadInicial"
                />
                <Message
                  v-if="$form.cantidadInicial?.invalid"
                  severity="error"
                  size="small"
                  variant="simple"
                >
                  {{ $form.cantidadInicial.error?.message }}
                </Message>
                <label for="cantidadInicial">Cantidad inicial</label>
              </FloatLabel>
            </div>
          </div>
        </div>

        <!-- Row 1 -->
        <div class="flex flex-row">
          <FloatLabel class="grow" variant="on">
            <Textarea
              id="descripcion"
              v-model="productoForm.descripcion"
              cols="30"
              fluid
              name="descripcion"
              rows="5"
              style="resize: none"
            />
            <Message
              v-if="$form.descripcion?.invalid"
              severity="error"
              size="small"
              variant="simple"
            >
              {{ $form.descripcion.error?.message }}
            </Message>
            <label for="descripcion">Descripcion</label>
          </FloatLabel>
        </div>
      </Form>
    </main>
  </div>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import type { Producto } from '@/models/Producto.ts'
import axiosInstance from '@/plugins/axios.ts'
import { zodResolver } from '@primevue/forms/resolvers/zod'
import { z } from 'zod' // Hooks

// Hooks
onMounted(() => {
  loadList()
})

// State
const formRef = ref(null)
const page = ref<number>(0)
const size = ref<number>(10)
const productoForm = reactive<Producto>({
  id: undefined,
  nombre: '',
  descripcion: '',
  categoria: '',
  precio: 0,
  costo: 0,
  beneficio: 0,
  impuesto: undefined,
  cantidadInicial: 0,
})

// Form
const onFormSubmit = ({ valid }) => {
  console.log('formSubmit', valid)
}

const submitForm = () => {
  console.log(formRef.value.submit())
}

const newFormulario = async () => {
  productoForm.id = undefined
  productoForm.nombre = ''
  productoForm.descripcion = ''
  productoForm.categoria = ''
  productoForm.precio = 0
  productoForm.costo = 0
  productoForm.beneficio = 0
  productoForm.cantidadInicial = 0
  productoForm.impuesto = undefined

  await nextTick()
}

const resolver = ref(
  zodResolver(
    z.object({
      id: z.string().optional(),
      nombre: z.string().min(3, { message: 'Campo requerido' }),
      precio: z.number().gt(0, { message: 'Precio requerido' }),
      costo: z.number().gt(0, { message: 'Costo requerido' }),
      cantidadInicial: z.number().gte(0, { message: 'Cantidad inicial' }),
      categoria: z.string().min(1, { message: 'Categoria requerido' }),
      impuesto: z.number().optional(),
      beneficio: z.number().optional(),
      descripcion: z.string().min(3, { message: 'Descripcion requerido' }),
    }),
  ),
)

// List
const productoList = ref<Producto[]>([])
const impuestoList = [0, 11, 18]
const categoriaList = ['Electrodomesticos', 'Medicina', 'Ropa', 'Varios']
const loadList = async () => {
  try {
    const response = await axiosInstance.get('/productos', {
      params: {
        page: page.value,
        size: size.value,
      },
    })
    productoList.value = response.data
    console.log(productoList.value)
  } catch (error) {
    console.error(error)
  }
}
</script>

<style></style>
