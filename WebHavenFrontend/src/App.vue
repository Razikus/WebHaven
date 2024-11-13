<template>
  <div class="min-h-screen bg-gray-100">
    <nav class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex">
            <router-link to="/" class="flex items-center px-4 font-semibold text-gray-900">
              Haven Client
            </router-link>
            <router-link v-for="program in runningPrograms" :key="program.name" :to="`/programs/${program.name}`"
              class="ml-8 flex items-center px-4 text-gray-600 hover:text-gray-900">{{ program.name }}
              <span :class="[
                'px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800'
              ]">
                {{ formatProgramName(program) }}
              </span></router-link>
            <!-- <router-link to="/sessions" class="ml-8 flex items-center px-4 text-gray-600 hover:text-gray-900">
              Active Sessions
            </router-link> -->
          </div>

          <!-- Connection Status Indicator -->
          <div class="flex items-center gap-2">
            <div class="relative flex">
              <div class="h-3 w-3 rounded-full" :class="isConnected
                ? 'bg-green-500 animate-pulse'
                : 'bg-gray-400'"></div>
              <div v-if="isConnected" class="absolute animate-ping h-3 w-3 rounded-full bg-green-400 opacity-75"></div>
            </div>
            <span class="text-sm text-gray-600">
              {{ isConnected ? 'Connected' : 'Disconnected' }}
            </span>
          </div>
        </div>
      </div>
    </nav>
    <router-view></router-view>
  </div>
</template>

<script setup>
import { provide } from 'vue';
import useWebSocket from './composables/useWebSocket';

const formatProgramName = (prog) => {
  const parts = prog.programClass.split('.');
  if (parts.length <= 2) return prog.programClass;
  return parts.slice(-2).join('.');
};

const {
  connect,
  sendMessage,
  isConnected,
  messagesPerSessionPerChannel,
  sessions,
  statePerSession,
  availablePrograms,
  runningPrograms,
  perProgramData,
  programSessions,
  runningProgramData,
  perProgramSpecificData
} = useWebSocket();

// Provide websocket functionality to all components
provide('websocket', {
  connect,
  sendMessage,
  isConnected,
  messagesPerSessionPerChannel,
  sessions,
  statePerSession,
  availablePrograms,
  runningPrograms,
  perProgramData,
  programSessions,
  runningProgramData,
  perProgramSpecificData
});
</script>