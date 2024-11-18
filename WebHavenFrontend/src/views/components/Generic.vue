<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="bg-white rounded-lg shadow divide-y divide-gray-200">
      <!-- Header -->
      <div class="px-6 py-4">
        <h2 class="text-xl font-semibold text-gray-900">Program: {{ programName }}</h2>
      </div>

      <!-- State Section -->
      <div class="px-6 py-4">
        <div class="flex items-center space-x-3">
          <div class="font-medium text-gray-700">State:</div>
          <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-gray-100">
            {{ globalObjectState || 'Unknown' }}
          </span>
        </div>
      </div>

      <!-- Message Section -->
      <div class="px-6 py-4">
        <div class="space-y-2">
          <div class="font-medium text-gray-700">Message:</div>
          <div class="bg-gray-50 rounded-md p-4 text-sm text-gray-900">
            {{ message || 'No messages' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, inject, computed, watch, onMounted, onBeforeUnmount} from 'vue';
import {useRoute} from 'vue-router';

const route = useRoute();
const programName = computed(() => route.params.name);
const {sendMessage, registerMessageCallback, unregisterMessageCallback} = inject('websocket');
const globalObjectState = ref("");
const message = ref("");

const onData = (data) => {
  if(data.cmdType == "state") {
    globalObjectState.value = data.data;
  } else if (data.cmdType == "message") {
    message.value = data.data;
  }
};

onMounted(() => {
  registerMessageCallback(programName.value, onData);
});

onBeforeUnmount(() => {
  unregisterMessageCallback(programName.value, onData);
});
</script>