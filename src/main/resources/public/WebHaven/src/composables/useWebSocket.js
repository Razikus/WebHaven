import { ref, onMounted, onUnmounted } from 'vue';

export default function useWebSocket() {
  const ws = ref(null);
  const isConnected = ref(false);
  const sessions = ref([]);
  const statePerSession = ref({});
  const messagesPerSessionPerChannel = ref({});
  const reconnectAttempts = ref(0);
  const maxReconnectAttempts = 5;  // Maximum number of reconnect attempts
  const baseReconnectDelay = 1000; // Start with 1 second delay
  let reconnectTimeout = null;

  const getReconnectDelay = () => {
    // Exponential backoff: 1s, 2s, 4s, 8s, 16s
    return Math.min(1000 * Math.pow(2, reconnectAttempts.value), 16000);
  };

  const connect = () => {
    // Clear any existing connection
    if (ws.value) {
      ws.value.close();
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/haven`;
    
    ws.value = new WebSocket(wsUrl);

    ws.value.onopen = () => {
      isConnected.value = true;
      reconnectAttempts.value = 0; // Reset attempts on successful connection
      console.log('Connected to WebSocket');
    };

    ws.value.onmessage = (event) => {
      const data = JSON.parse(event.data);
      switch (data.type) {
        case 'state':
          statePerSession.value[data.session] = data.data;
          break;
        case 'message':
          console.log(data);
          if (!messagesPerSessionPerChannel.value[data.session]) {
            messagesPerSessionPerChannel.value[data.session] = {};
          }
          if (!messagesPerSessionPerChannel.value[data.session][data.data.channel]) {
            messagesPerSessionPerChannel.value[data.session][data.data.channel] = [];
          }
          messagesPerSessionPerChannel.value[data.session][data.data.channel].push(data.data);
          break;
        case 'sessions':
          sessions.value = data.data;
          break;
      }
    };

    ws.value.onclose = () => {
      isConnected.value = false;
      console.log('Disconnected from WebSocket');
      
      // Attempt to reconnect if we haven't exceeded max attempts
      if (reconnectAttempts.value < maxReconnectAttempts) {
        const delay = getReconnectDelay();
        console.log(`Attempting to reconnect in ${delay}ms... (Attempt ${reconnectAttempts.value + 1})`);
        
        reconnectTimeout = setTimeout(() => {
          reconnectAttempts.value++;
          connect();
        }, delay);
      } else {
        console.log('Max reconnection attempts reached');
      }
    };

    ws.value.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  };

  const sendMessage = (type, data) => {
    if (ws.value && isConnected.value) {
      ws.value.send(JSON.stringify({ type, ...data }));
    }
  };

  // Manual reconnect function that resets the attempt counter
  const reconnect = () => {
    reconnectAttempts.value = 0;
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout);
    }
    connect();
  };

  onMounted(() => {
    connect();
  });

  onUnmounted(() => {
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout);
    }
    if (ws.value) {
      ws.value.close();
    }
  });

  return {
    connect,
    sendMessage,
    isConnected,
    messagesPerSessionPerChannel,
    sessions,
    statePerSession,
  };
}