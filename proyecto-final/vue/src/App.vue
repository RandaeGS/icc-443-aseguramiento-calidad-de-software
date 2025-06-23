<template>
  <div class="flex flex-col h-screen">
    <header class="bg-gray-800 text-white">
      <nav class="container mx-auto px-4 py-4 flex justify-center items-center">
        <h1 class="text-xl font-bold">Proyecto Final</h1>
      </nav>
    </header>

    <main class="flex-grow">
      <router-view></router-view>
    </main>

    <footer class="bg-gray-800 text-white py-2">
      <div class="container mx-auto px-4 text-center">
        <p class="text-sm">Made by Randae Garcia</p>
      </div>
    </footer>
  </div>
</template>

<script lang="ts" setup>
// No se necesitan importaciones adicionales para este ejemplo
</script>
