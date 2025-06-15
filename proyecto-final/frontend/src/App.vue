<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="flex flex-col">
    <header>
      <nav class="navbar bg-base-300 shadow-sm">
        <div class="navbar-start"></div>
        <div class="navbar-center">
          <a class="btn btn-ghost text-xl">Proyecto Final</a>
        </div>
        <div class="navbar-end"></div>
      </nav>
    </header>

    <main class="flex grow">
      <RouterView />
    </main>
  </div>
</template>

<style scoped></style>
