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
  const availablePrograms = ref([]);
  const runningPrograms = ref([]);
  const runningProgramData = ref({});

  const perProgramSpecificData = ref({});

  const programSessions = ref({});


  let reconnectTimeout = null;
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const httpProtocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
  let wsUrl = `${protocol}//${window.location.host}/haven`;
  let programsUrl = `${httpProtocol}//${window.location.host}/programs`;

  const perProgramData = ref({});

  let devDebug = import.meta.env.VITE_DEBUG;
  if(devDebug == 'true' || devDebug == true) {
     wsUrl = `ws://localhost:7071/haven`;
     programsUrl = `http://localhost:7071/programs`;
  }

  const getReconnectDelay = () => {
    return Math.min(1000 * Math.pow(2, reconnectAttempts.value), 16000);
  };

  const fetchPrograms = async () => {
    try {
      const response = await fetch(programsUrl);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      availablePrograms.value = data;
      console.log('Fetched programs:', availablePrograms);
    } catch (error) {
      console.error('Failed to fetch programs:', error);
      availablePrograms.value = []; // Set empty array on error
    }
  }

  const connect = () => {
    // Clear any existing connection
    if (ws.value) {
      ws.value.close();
    }

    
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
          let programData = data.program;
          if (!perProgramData.value[programData.name]) {
            perProgramData.value[programData.name] = {};
          }
          perProgramData.value[programData.name][data.session] = data.data;
          statePerSession.value[data.session] = data.data;
          break;
        case 'message':
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
        case 'programs':
          runningPrograms.value = data.data;
          let newData = {}
          for (let program of data.data) {
            programSessions.value[program.name] = program.sessionNames;
            newData[program.name] = program;
          }
          runningProgramData.value = newData;
          break;
        case 'programdata':
          perProgramSpecificData.value[data.program] = data.data;
          console.log(data)
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
    availablePrograms.value = []
    fetchPrograms();
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
    availablePrograms, 
    runningPrograms,
    perProgramData,
    programSessions,
    runningProgramData,
    perProgramSpecificData
  };
}