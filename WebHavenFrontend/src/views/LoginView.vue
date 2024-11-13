<template>
  <div class="max-w-md mx-auto mt-10 bg-white rounded-lg shadow-md p-6">
    <h2 class="text-2xl font-bold mb-6 text-center">Start New Program</h2>
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
      
      <div>
        <label class="block text-sm font-medium text-gray-700">Program</label>
        <select
          v-model="program"
          required
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        >
          <option value="" disabled>Select a program</option>
          <option
            v-for="prog in availablePrograms"
            :key="prog.programClass"
            :value="prog.programClass"
            :title="prog.programClass"
          >
            {{ formatProgramName(prog) }}
          </option>
        </select>
      </div>
      
      <div 
        v-if="Object.keys(programArgs).length > 0"
        class="space-y-4 p-4 bg-gray-50 rounded-md"
      >
        <h3 class="text-sm font-medium text-gray-700 mb-2">Program Arguments</h3>
        <div 
          v-for="(value, key) in programArgs" 
          :key="key"
          class="space-y-2"
        >
          <label class="block text-sm font-medium text-gray-700">
            {{ formatArgName(key) }}
            <span class="text-xs text-gray-500 ml-2">
              ({{ getArgDescription(key) }})
            </span>
          </label>
          <input
            v-model="programArgs[key]"
            type="text"
            required
            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700">Program Name</label>
        <div class="mt-1 flex rounded-md shadow-sm">
          <input
            v-model="programName"
            type="text"
            required
            class="block w-full rounded-l-md border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <button
            type="button"
            @click="regenerateName"
            class="inline-flex items-center px-3 rounded-r-md border border-l-0 border-gray-300 bg-gray-50 text-gray-500 hover:bg-gray-100"
          >
            🔄
          </button>
        </div>
      </div>
      
      
      <button
        type="submit"
        :disabled="!availablePrograms.length"
        class="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400"
      >
        {{ availablePrograms.length ? 'Start Session' : 'Loading programs...' }}
      </button>
    </form>
    
    <!-- Debug information -->
    <div class="mt-4 text-sm text-gray-500">
      Available programs: {{ availablePrograms.length }}
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';

import { uniqueNamesGenerator, adjectives, colors, animals } from 'unique-names-generator';



const router = useRouter();
const { sendMessage, availablePrograms } = inject('websocket');

const username = ref('');
const password = ref('');
const characterName = ref('');
const program = ref('');
const programName = ref('');

const programArgs = ref({});


const customConfig = {
  dictionaries: [adjectives, colors],
  separator: '-',
  length: 2,
};

const regenerateName = () => {
  programName.value = uniqueNamesGenerator(customConfig);
};
// Set initial program value once programs are loaded
onMounted(() => {
  if (availablePrograms.value?.length > 0) {
    program.value = availablePrograms.value[0].programClass;
    updateProgramArgs();
  }
  regenerateName();
});

watch(availablePrograms, (newPrograms) => {
  if (newPrograms.length > 0) {
    program.value = newPrograms[0].programClass;;
  }
});

const updateProgramArgs = () => {
    const selectedProgram = availablePrograms.value.find(
      prog => prog.programClass === program.value
    );
    if (selectedProgram?.declaredArgs) {
      const newArgs = {};
      Object.keys(selectedProgram.declaredArgs).forEach(key => {
        newArgs[key] = '';
      });
      programArgs.value = newArgs;
    } else {
      programArgs.value = {};
    }
  };


  watch(program, () => {
    updateProgramArgs();
  });


const formatProgramName = (prog) => {
  const parts = prog.programClass.split('.');
  if (parts.length <= 2) return prog.programClass;
  return parts.slice(-2).join('.');
};


const formatArgName = (key) => {
  return key.replace(/_/g, ' ')
    .replace(/\b\w/g, l => l.toUpperCase());
};

const getArgDescription = (key) => {
  const selectedProgram = availablePrograms.value.find(
    prog => prog.programClass === program.value
  );
  return selectedProgram?.declaredArgs[key] || '';
};

const handleSubmit = () => {
  if (!program.value) {
    console.error('No program selected');
    return;
  }

  sendMessage('start_program', {
    username: username.value,
    password: password.value,
    altname: characterName.value,
    program: program.value,
    programName: programName.value,
    args: programArgs.value
  });
  
  router.push('/programs/' + programName.value);
};
</script>