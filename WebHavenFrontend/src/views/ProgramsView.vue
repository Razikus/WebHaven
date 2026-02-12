<template>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="bg-white rounded-lg shadow p-6" v-if="programData && programData.name">
            <!-- Header Section -->
            <div class="flex items-center justify-between">
                <div class="flex items-center space-x-3">
                    <span class="bg-gray-100 text-gray-800 px-3 py-1.5 rounded-full text-sm font-medium">
                        Program Info
                    </span>
                    <span class="text-sm text-gray-500">
                        ID: {{ programData.name }}
                    </span>
                </div>
                <button @click="sendMessage('stop_program', { program: programName }); router.push('/')"
                    class="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition-colors duration-200 flex items-center space-x-2">
                    <span class="hidden sm:inline">Stop Program</span>
                    <span class="sm:hidden">Stop</span>
                </button>
            </div>

            <!-- Info Grid -->
            <div class="mt-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="space-y-1" >
                    <div class="text-sm text-gray-500" :title="programData.programClass">Type</div>
                    <div class="font-medium" :title="programData.programClass">{{ programData.programClass.split('.').pop() }}</div>
                </div>
                <div class="space-y-1">
                    <div class="text-sm text-gray-500">Total Sessions</div>
                    <div class="font-medium">{{ programData.sessionNames.length }}</div>
                </div>
            </div>

            <!-- Sessions List -->
            <div class="mt-6" v-if="programData.sessionNames.length">
                <div class="text-sm text-gray-500 mb-2">Active Sessions</div>
                <div class="bg-gray-50 rounded-lg p-3 space-y-2">
                    <div v-for="(session, index) in programData.sessionNames" :key="index"
                        class="flex items-center justify-between">
                        <div class="flex items-center space-x-2">
                            <div class="w-2 h-2 bg-green-500 rounded-full"></div>
                            <span class="font-medium text-gray-700 text-sm">{{ session }}</span>
                        </div>
                        <button @click="stopSession(session)"
                            class="text-red-600 hover:text-red-800 text-sm font-medium transition-colors duration-200 flex items-center space-x-1">
                            <span>Stop Session</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <Chatter v-if="programData && programData.programClass == 'tech.razikus.headlesshaven.bot.ChatterProgram'" />
    <Visioner v-else-if="programData && programData.programClass == 'tech.razikus.headlesshaven.bot.AroundVisionProgram'" />
    <Generic v-else-if="programData"/>

</template>

<script setup>
import { ref, inject, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Chatter from './components/Chatter.vue';
import Visioner from './components/Visioner.vue';
import Generic from './components/Generic.vue';


const route = useRoute();
const router = useRouter();

const programName = computed(() => route.params.name);

watch(
    () => route.params.name,
    (newId, oldId) => {
        console.log('Route changed from', oldId, 'to', newId);
    }
)



const { sendMessage, runningProgramData } = inject('websocket');

const programData = computed(() => runningProgramData.value[programName.value]);

const stopSession = (session) => {
    sendMessage('stop_session', { session });
};

</script>