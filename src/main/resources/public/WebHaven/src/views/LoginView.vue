<template>
    <div class="max-w-md mx-auto mt-10 bg-white rounded-lg shadow-md p-6">
      <h2 class="text-2xl font-bold mb-6 text-center">Start New Session</h2>
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Username</label>
          <input
            v-model="username"
            type="text"
            required
            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Password</label>
          <input
            v-model="password"
            type="password"
            required
            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Character Name</label>
          <input
            v-model="characterName"
            type="text"
            required
            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
        <button
          type="submit"
          class="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          Start Session
        </button>
      </form>
    </div>
  </template>


<script setup>
import { ref, inject } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const { sendMessage } = inject('websocket');

const username = ref('');
const password = ref('');
const characterName = ref('');

const handleSubmit = () => {
  sendMessage('start_session', {
    username: username.value,
    password: password.value,
    altname: characterName.value
  });
  router.push('/sessions');
};
</script>