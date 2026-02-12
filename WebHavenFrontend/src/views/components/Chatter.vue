<template>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="bg-white rounded-lg shadow">
            <div class="px-4 py-5 sm:p-6">
                <h2 class="text-lg font-medium text-gray-900">Active Sessions</h2>
                <div class="mt-4 space-y-4">
                    <div v-for="session in programSessions[programName]" :key="session" class="border rounded-lg p-4">
                        <div class="flex items-center justify-between">
                            <span class="font-medium">{{ session }}</span>
                            <div v-if="statePerSession[session]?.astronomy"
                                class="flex items-center space-x-3 text-sm text-gray-600">
                                <span class="inline-flex items-center bg-gray-100 px-2 py-1 rounded-md">
                                    {{ statePerSession[session].astronomy.dayTimeHours.toString().padStart(2, '0') }}:{{
                                        Math.floor(statePerSession[session].astronomy.dayTimeMinutes %
                                            60).toString().padStart(2, '0') }}
                                </span>
                                <span class="flex items-center">
                                    <span class="w-2 h-2 rounded-full mr-2"
                                        :style="{ backgroundColor: statePerSession[session].astronomy.moonColor }">
                                    </span>
                                    {{ statePerSession[session].astronomy.moonPhaseName }}
                                </span>
                                <span>|</span>
                                <span>{{ statePerSession[session].astronomy.timeOfDay }}</span>
                                <span>|</span>
                                <span>{{ statePerSession[session].astronomy.seasonName }}</span>
                                <span>|</span>
                                <span>{{ statePerSession[session].astronomy.formattedDate }}</span>
                            </div>
                            <button @click="stopSession(session)" class="text-red-600 hover:text-red-800">
                                Stop Session
                            </button>
                        </div>

                        <div v-if="statePerSession[session]" class="mt-4">
                            <h3 class="font-medium text-gray-700 mb-2 flex items-center space-x-2">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500"
                                    viewBox="0 0 20 20" fill="currentColor">
                                    <path
                                        d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z" />
                                </svg>
                                <span>Players Around</span>
                            </h3>
                            <ul class="space-y-2">
                                <li v-for="player in statePerSession[session].playersAround" :key="player.id"
                                    class="flex items-center justify-between px-3 py-2 rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors duration-150">
                                    <div class="flex items-center space-x-2">
                                        <span :class="[
                                            'inline-block w-2 h-2 rounded-full',
                                            player.buddyState ? 'bg-green-400' : 'bg-gray-400'
                                        ]"></span>
                                        <span :class="[
                                            'font-medium',
                                            player.buddyState ? 'text-green-600' : 'text-gray-600'
                                        ]">
                                            {{ player.buddyState ? player.buddyState.name : "???" }}
                                        </span>
                                    </div>
                                    <span :class="[
                                        'px-2 py-1 text-xs font-medium rounded-full',
                                        player.isVillageBuddy
                                            ? 'bg-green-100 text-green-800'
                                            : 'bg-gray-100 text-gray-800'
                                    ]">
                                        {{ player.isVillageBuddy ? "VILLAGER" : "NOT VILLAGER" }}
                                    </span>
                                </li>
                            </ul>

                            <h3 class="font-medium text-gray-700 mt-4 mb-2">Chat Channels</h3>
                            <div class="space-y-4">
                                <div v-for="channel in statePerSession[session].chatChannels" :key="channel">
                                    <div class="mt-4">
                                        <div class="border rounded-lg p-4 max-h-60 overflow-y-auto">
                                            <div v-if="messagesPerSessionPerChannel[session] && messagesPerSessionPerChannel[session][channel]"
                                                v-for="msg in messagesPerSessionPerChannel[session][channel]" :key="msg"
                                                class="mb-2">
                                                <span class="font-medium">{{ msg.from }}</span>
                                                <span class="text-gray-500"> in {{ channel }}: </span>
                                                <span>{{ msg.line }}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="flex items-center space-x-2">
                                        <input @keyup.enter="sendChat(session, channel)" v-model="chatMessage" :placeholder="`Message ${channel}`"
                                            class="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500" />
                                        <button @click="sendChat(session, channel)"
                                            class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
                                            Send
                                        </button>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, inject, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';


const route = useRoute();

const programName = computed(() => route.params.name);

watch(
    () => route.params.name,
    (newId, oldId) => {
        console.log('Route changed from', oldId, 'to', newId);
    }
)



const { sendMessage, messagesPerSessionPerChannel, statePerSession, programSessions } = inject('websocket');
const chatMessage = ref('');


const stopSession = (session) => {
    sendMessage('stop_session', { session });
};

const sendChat = (session, channel) => {
    if (chatMessage.value.trim()) {
        sendMessage('chat', {
            session,
            channel,
            text: chatMessage.value
        });
        if (!messagesPerSessionPerChannel.value[session]) {
            messagesPerSessionPerChannel.value[session] = {};
        }
        if (!messagesPerSessionPerChannel.value[session][channel]) {
            messagesPerSessionPerChannel.value[session][channel] = [];
        }
        messagesPerSessionPerChannel.value[session][channel].push({
            from: 'You',
            line: chatMessage.value
        });
        chatMessage.value = '';
    }
};
</script>