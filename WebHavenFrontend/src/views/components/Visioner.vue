<template>
  <ErrorToast ref="toastRef" ></ErrorToast>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <FlowerMenu
        v-if="flowerMenu"
        :menu="flowerMenu"
        @select="handleFlowerMenuSelect"
        @close="closeFlowerMenu"
    />
    <div class="bg-white rounded-lg shadow relative">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="flex justify-center">
          <div class="bg-white rounded-lg shadow relative">
            <div class="relative" style="width: 1100px; height: 1100px;">
              <canvas id="mapCanvas" class="relative" width="1100" height="1100"
                      style="width: 1100px; height: 1100px;" @mousemove="handleMouseMove"
                      @mouseout="hideTooltip" @contextmenu.prevent @mousedown="handleMouseDown"></canvas>
              <div v-if="hoveredObject"
                   class="absolute z-10 bg-black bg-opacity-75 text-white p-2 rounded text-sm"
                   :style="tooltipStyle">
                <div v-if="hoveredObject.resources">
                  <div>ID: {{ hoveredObject.id }}</div>
                  <div>Position: ({{ Math.round(hoveredObject.coordsX) }}, {{
                      Math.round(hoveredObject.coordsY)
                    }})
                  </div>
                  <div v-for="(resource, index) in hoveredObject.resources" :key="index">
                    <div v-if="resourceMap[resource]">
                      {{ resourceMap[resource].name }} {{resourceMap[resource].layers}}

                    </div>
                  </div>
                </div>
                <div v-else>
                  <div>ID: {{ hoveredObject.id }}</div>
                  <div>Position: ({{ Math.round(hoveredObject.coordsX) }}, {{
                      Math.round(hoveredObject.coordsY)
                    }})
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
import {ref, inject, computed, watch, onMounted, onBeforeUnmount} from 'vue';
import {useRoute} from 'vue-router';
import FlowerMenu from './FlowerMenu.vue';
import ErrorToast from "./ErrorToast.vue";
const toastRef = ref(null);

const resourceMap = ref({});

const alreadyRequested = ref({});

const VIEWPORT_SIZE = 1100;
const PLAYER_SIZE = 5;
const SCALE = 1;
const HOVER_RADIUS = 10;
const route = useRoute();
const programName = computed(() => route.params.name);
const {sendMessage, registerMessageCallback, unregisterMessageCallback} = inject('websocket');
const flowerMenu = ref(null);
const globalObjectState = ref(null);
const onData = (data) => {
  if(data.cmdType == "fullobj") {
    globalObjectState.value = data.data;
  } else if(data.cmdType == "objectchanged") {
    if(globalObjectState.value) {
      globalObjectState.value[data.data.id] = data.data;
    }
  } else if (data.cmdType == "objectremoved") {
    if(globalObjectState.value) {
      delete globalObjectState.value[data.data.id];
    }
  } else if (data.cmdType == "objectadded") {
    if(globalObjectState.value) {
      globalObjectState.value[data.data.id] = data.data;
    }
  } else if(data.cmdType == "flowermenu") {
    if(data.data) {
      flowerMenu.value = data.data;
    } else {
      flowerMenu.value = null;
    }
  } else if(data.cmdType == "error") {
    toastRef.value?.addToast(data.data);
  } else if(data.cmdType == "resource") {
    resourceMap.value[data.data.id] = data.data.resource;
  }
};

const requestResourceId = (resid) => {
  // check for 20 seconds
  if (alreadyRequested.value[resid] && new Date() - alreadyRequested.value[resid] < 20000) {
    return;
  }
  sendMessage("proginput", {
    program: programName.value,
    cmdType: 'requestresource',
    idOf: resid
  });
  alreadyRequested.value[resid] = new Date();
};

const closeFlowerMenu = () => {
  sendMessage("proginput", {
    program: programName.value,
    cmdType: 'flowermenu',
    option: -1
  });
};

const handleFlowerMenuSelect = (option) => {
  sendMessage("proginput", {
    program: programName.value,
    cmdType: 'flowermenu',
    option: option
  });
};

const hoveredObject = ref(null);
const tooltipStyle = ref({
  left: '0px',
  top: '0px',
});

const playerPosition = computed(() => {
  if (!globalObjectState.value) {
    console.warn('No program data available');
    return {x: 0, y: 0};
  }
  const playerObj = Object.values(globalObjectState.value).find(obj => {
    return obj?.isMyself === true &&
        typeof obj.coordsX === 'number' &&
        typeof obj.coordsY === 'number';
  });

  if (!playerObj) {
    console.warn('Player object not found or has invalid coordinates');
    return {x: 0, y: 0};
  }

  return {
    x: playerObj.coordsX,
    y: playerObj.coordsY
  };
});

const worldToScreen = (worldX, worldY) => {
  const centerX = VIEWPORT_SIZE / 2;
  const centerY = VIEWPORT_SIZE / 2;
  const offsetX = (worldX - playerPosition.value.x) * SCALE;
  const offsetY = (worldY - playerPosition.value.y) * SCALE;
  return {
    x: centerX + offsetX,
    y: centerY + offsetY
  };
};
const handleMouseDown = (event) => {
  const canvas = event.target;
  const rect = canvas.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;

  const worldPos = screenToWorld(x, y);
  const button = event.button + 1;
  const modifiers = 0;

  // Send click event through websocket
  if(hoveredObject.value) {
    let meshId = -1
    if(hoveredObject.value.resources) {
      for (const resource of hoveredObject.value.resources) {
        let realResource = resourceMap.value[resource]
        if (realResource && realResource.layers) {
          for (const layer of realResource.layers) {
            if (layer.type == 'mesh' && layer.id != -1) {
              meshId = layer.id;
            }
          }
        }
      }
    }
    sendMessage("proginput", {
      program: programName.value,
      cmdType: 'gobclick',
      x: Math.round(worldPos.x),
      y: Math.round(worldPos.y),
      button: button,
      modifiers: modifiers,
      gobId: hoveredObject.value.id,
      meshId: meshId
    });
  } else {
    sendMessage("proginput", {
      program: programName.value,
      cmdType: 'click',
      x: Math.round(worldPos.x),
      y: Math.round(worldPos.y),
      button: button,
      modifiers: modifiers,
      meshId: -1
    });

  }
};

const screenToWorld = (screenX, screenY) => {
  const centerX = VIEWPORT_SIZE / 2;
  const centerY = VIEWPORT_SIZE / 2;
  return {
    x: (screenX - centerX) / SCALE + playerPosition.value.x,
    y: (screenY - centerY) / SCALE + playerPosition.value.y
  };
};

const handleMouseMove = (event) => {
  const canvas = event.target;
  const rect = canvas.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;

  const worldPos = screenToWorld(x, y);
  const programData = globalObjectState.value;

  if (!programData) return;

  // Find closest object
  let closest = null;
  let minDistance = HOVER_RADIUS;

  Object.values(programData).forEach(obj => {
    if (obj.coordsX != null && obj.coordsY != null) {
      const screenPos = worldToScreen(obj.coordsX, obj.coordsY);
      const distance = Math.sqrt(
          Math.pow(screenPos.x - x, 2) +
          Math.pow(screenPos.y - y, 2)
      );
      let isWall = false;
      for (const resource of obj.resources) {
        let realResource = resourceMap.value[resource];
        if(realResource) {
          if (realResource.name == "gfx/terobjs/arch/hwall") {
            isWall = true;
          } else if(realResource.name == "gfx/tiles/ridges/caveout") {
            // isWall = true;

          }

        }
      }

      if ((distance < minDistance) && !isWall) {
        minDistance = distance;
        closest = obj;
      }
    }
  });

  hoveredObject.value = closest;
  if (closest) {
    tooltipStyle.value = {
      left: `${x + 10}px`,
      top: `${y + 10}px`
    };
  }
};

const hideTooltip = () => {
  hoveredObject.value = null;
};

const drawMap = () => {
  const canvas = document.getElementById('mapCanvas');
  const ctx = canvas.getContext('2d');

  ctx.clearRect(0, 0, VIEWPORT_SIZE, VIEWPORT_SIZE);

  // Draw player
  ctx.fillStyle = 'red';
  ctx.beginPath();
  ctx.arc(VIEWPORT_SIZE / 2, VIEWPORT_SIZE / 2, PLAYER_SIZE, 0, 2 * Math.PI);
  ctx.fill();

  // Draw grid
  ctx.strokeStyle = '#eee';
  ctx.lineWidth = 0.5;
  const gridSize = 50;

  for (let i = 0; i <= VIEWPORT_SIZE; i += gridSize) {
    ctx.beginPath();
    ctx.moveTo(i, 0);
    ctx.lineTo(i, VIEWPORT_SIZE);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(0, i);
    ctx.lineTo(VIEWPORT_SIZE, i);
    ctx.stroke();
  }


  const programData = globalObjectState.value;
  if (programData) {
    Object.values(programData).forEach(obj => {
      if (obj?.isMyself !== true && obj.coordsX != null && obj.coordsY != null) {
        const screenPos = worldToScreen(obj.coordsX, obj.coordsY);

        if (screenPos.x >= 0 && screenPos.x <= VIEWPORT_SIZE &&
            screenPos.y >= 0 && screenPos.y <= VIEWPORT_SIZE) {
          let pingSize = 3;
          let color = 'blue';
          let shape = 'circle';
          if (obj.resources && obj.resources.length > 0) {
            for (const resource of obj.resources) {
              if (!resourceMap.value[resource]) {
                requestResourceId(resource);
              }
            }
            let resource = resourceMap.value[obj.resources[0]];
            if(!resource){
              color='black';
            } else {
              if (resource.name.includes('plant')) {
                color = 'green';
              } else if (resource.name.includes('stockpile')) {
                color = 'orange';
              } else if (resource.name.includes('body')) {
                color = 'red';
                pingSize = 5;
              } else if (resource.name.includes('palisadeseg')) {
                color = 'brown';
                shape = 'square';
              } else if (resource.name.includes('palisadecp')) {
                color = 'brown';
                pingSize = 5;
                shape = 'square';
              }
            }
          }

          ctx.fillStyle = obj === hoveredObject.value ? 'yellow' : color;
          ctx.beginPath();
          if (shape === 'circle') {
            ctx.arc(screenPos.x, screenPos.y, pingSize, 0, 2 * Math.PI);
          } else if (shape === 'square') {
            ctx.rect(screenPos.x - pingSize, screenPos.y - pingSize, pingSize * 2, pingSize * 2);
          }
          ctx.fill();

          if (obj.resources) {
            for (const resource of obj.resources) {
              let realResource = resourceMap.value[resource];
              if (realResource && realResource.layers) {
                for (const layer of realResource.layers) {
                  if (layer.type == 'obstacle') {
                    ctx.strokeStyle = 'rgba(255, 0, 0, 0.5)';
                    ctx.lineWidth = 1;

                    for (const obstacle of layer.obstacle) {
                      if (obstacle.length > 0) {
                        // Calculate center of obstacle
                        let centerX = 0, centerY = 0;
                        for (const point of obstacle) {
                          centerX += point.x;
                          centerY += point.y;
                        }
                        centerX /= obstacle.length;
                        centerY /= obstacle.length;

                        ctx.save();
                        ctx.translate(screenPos.x + centerX, screenPos.y + centerY);
                        ctx.rotate(obj.angle || 0);
                        ctx.translate(-centerX, -centerY);

                        ctx.beginPath();
                        const firstPoint = obstacle[0];
                        ctx.moveTo(firstPoint.x, firstPoint.y);

                        for (let i = 1; i < obstacle.length; i++) {
                          const point = obstacle[i];
                          ctx.lineTo(point.x, point.y);
                        }

                        ctx.lineTo(firstPoint.x, firstPoint.y);
                        ctx.stroke();
                        ctx.restore();
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    });
  }
};

watch(
    () => globalObjectState.value,
    (newData) => {
      if (newData) {
        drawMap();
      }
    },
    {deep: true}
);

watch(hoveredObject, () => {
  drawMap();
});

onMounted(() => {
  drawMap();
  registerMessageCallback(programName.value, onData);
  sendMessage('proginput', {
    program: programName.value,
    cmdType: 'requestfullobj'
  })
});

onBeforeUnmount(() => {
  unregisterMessageCallback(programName.value, onData);
});
</script>