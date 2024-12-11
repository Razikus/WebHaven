<template>
  <div
      class="fixed z-50 select-none"
      :style="{ left: menuPosition.x + 'px', top: menuPosition.y + 'px' }"
  >
    <div class="w-72 bg-white rounded-lg shadow-xl border border-gray-200 overflow-hidden">
      <div
          @mousedown.stop="startDragging"
          class="flex items-center justify-between px-4 py-3 bg-gradient-to-r from-blue-50 to-indigo-50 border-b border-gray-200 cursor-move"
      >
        <div class="flex items-center gap-2">
          <div class="flex space-x-1">
            <div class="w-3 h-3 rounded-full bg-gray-300"></div>
            <div class="w-3 h-3 rounded-full bg-gray-300"></div>
            <div class="w-3 h-3 rounded-full bg-gray-300"></div>
          </div>
          <span class="font-medium text-gray-700">{{ menu.title || 'Menu' }}</span>
        </div>
        <button
            @click="handleClose"
            class="text-gray-500 hover:text-gray-700 transition-colors duration-200"
        >
          <svg
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
          >
            <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <div class="p-3">
        <div class="space-y-1">
          <button
              v-for="(option, index) in menu.options"
              :key="index"
              @click="handleSelect(index)"
              class="w-full px-4 py-2.5 text-left text-gray-700 hover:bg-blue-50 rounded-md transition-colors duration-200 flex items-center gap-2"
          >
            <span v-if="option.icon" class="text-blue-500">{{ option.icon }}</span>
            <span>{{ typeof option === 'string' ? option : option.label }}</span>
          </button>
        </div>

        <div class="mt-3 pt-3 border-t border-gray-100">
          <button
              @click="handleClose"
              class="w-full px-4 py-2.5 text-center text-gray-600 hover:bg-gray-50 rounded-md transition-colors duration-200 border border-gray-200"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, defineProps, defineEmits } from 'vue';

const props = defineProps({
  menu: {
    type: Object,
    required: true,
    validator: (value) => {
      return value.options && Array.isArray(value.options);
    }
  },
  initialPosition: {
    type: Object,
    default: () => ({x: 200, y: 200})
  }
});

const emit = defineEmits(['select', 'close']);

const menuPosition = ref(props.initialPosition);
const isDragging = ref(false);
const dragOffset = ref({x: 0, y: 0});

const startDragging = (event) => {
  isDragging.value = true;
  dragOffset.value = {
    x: event.clientX - menuPosition.value.x,
    y: event.clientY - menuPosition.value.y
  };

  // Add drag cursor to body while dragging
  document.body.style.cursor = 'grabbing';

  document.addEventListener('mousemove', handleDrag);
  document.addEventListener('mouseup', stopDragging);
};

const handleDrag = (event) => {
  if (!isDragging.value) return;

  // Calculate new position
  const newX = event.clientX - dragOffset.value.x;
  const newY = event.clientY - dragOffset.value.y;

  // Keep menu within viewport bounds
  const menuElement = document.querySelector('.menu');
  const menuWidth = menuElement?.offsetWidth || 288; // 288px = w-72
  const menuHeight = menuElement?.offsetHeight || 300;

  menuPosition.value = {
    x: Math.max(0, Math.min(window.innerWidth - menuWidth, newX)),
    y: Math.max(0, Math.min(window.innerHeight - menuHeight, newY))
  };
};

const stopDragging = () => {
  isDragging.value = false;
  document.body.style.cursor = '';
  document.removeEventListener('mousemove', handleDrag);
  document.removeEventListener('mouseup', stopDragging);
};

const handleSelect = (index) => {
  emit('select', index);
};

const handleClose = () => {
  emit('close');
};

onMounted(() => {
  const menuElement = document.querySelector('.menu');
  if (menuElement) {
    const {x, y} = menuPosition.value;
    menuPosition.value = {
      x: Math.max(0, Math.min(window.innerWidth - menuElement.offsetWidth, x)),
      y: Math.max(0, Math.min(window.innerHeight - menuElement.offsetHeight, y))
    };
  }
});

onUnmounted(() => {
  document.removeEventListener('mousemove', handleDrag);
  document.removeEventListener('mouseup', stopDragging);
});
</script>