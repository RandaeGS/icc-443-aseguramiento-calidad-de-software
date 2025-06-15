<script setup lang="ts">
import { AlignLeft, Search, SlidersHorizontal } from 'lucide-vue-next'
import { ref } from 'vue'

const openList = ref<boolean>(false)
</script>

<template>
  <div class="flex flex-col grow">
    <header>
      <nav class="navbar bg-accent">
        <div class="navbar-start">
          <button class="btn btn-ghost" @click="openList = !openList">
            <AlignLeft />
          </button>
        </div>
        <div class="navbar-center"></div>
        <div class="navbar-end">
          <div class="join">
            <button class="btn join-item btn-error">Eliminar</button>
            <button class="btn join-item">Nuevo</button>
            <button class="btn join-item btn-primary">Salvar</button>
          </div>
        </div>
      </nav>
    </header>

    <main class="flex grow">
      <Transition name="slide-width">
        <div class="flex flex-col shadow" v-show="openList">
          <div class="p-4">
            <div class="join mx-1">
              <input type="text" class="input join-item" placeholder="Buscar por descripcion" />
              <button class="btn join-item"><Search /></button>
            </div>

            <button class="btn btn-ghost"><SlidersHorizontal /></button>
          </div>

          <div class="grow my-2">
            <div class="p-2 bg-base-200">
              <p>codigo</p>
              <p>nombre</p>
              <p>precio</p>
            </div>
            <hr />
            <div class="p-2 bg-base-200">
              <p>codigo</p>
              <p>nombre</p>
              <p>precio</p>
            </div>
          </div>

          <div class="join mx-auto my-2">
            <button class="join-item btn">«</button>
            <button class="join-item btn">Page 0</button>
            <button class="join-item btn">»</button>
          </div>
        </div>
      </Transition>

      <form @submit.prevent="" class="flex flex-col grow p-4 bg-base-200">
        <fieldset class="fieldset">
          <legend class="fieldset-legend">Codigo</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Nombre</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Descripcion</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Categoria</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Precio</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Costo</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Costo</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Costo</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Costo</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>

        <fieldset class="fieldset">
          <legend class="fieldset-legend">Costo</legend>
          <input type="text" class="input" placeholder="000000" />
          <p class="label">Optional</p>
        </fieldset>
      </form>
    </main>
  </div>
</template>

<style>
.slide-width-enter-active,
.slide-width-leave-active {
  transition: max-width 0.3s ease;
}

.slide-width-enter-from,
.slide-width-leave-to {
  max-width: 0;
}

.slide-width-enter-to,
.slide-width-leave-from {
  max-width: 250px;
}
</style>
