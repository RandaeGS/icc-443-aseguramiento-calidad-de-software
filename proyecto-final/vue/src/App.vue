<template>
  <div class="flex flex-col h-screen justify-between">
    <header class="bg-gray-800 text-white">
      <nav class="container mx-auto px-4 py-4 flex justify-center items-center">
        <h1 class="text-xl font-bold">Proyecto Final</h1>
      </nav>
    </header>

    <router-view class="overflow-scroll grow"></router-view>

    <footer class="bg-gray-800 text-white py-2">
      <div class="container mx-auto px-4 text-center">
        <p class="text-sm">Made by Randae Garcia</p>
        <Toast position="bottom-center" />
      </div>
    </footer>
  </div>
</template>

<script lang="ts" setup></script>
