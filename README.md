![WebHaven](https://i.imgur.com/rElLKwi.gif)



# WebHaven

Web-based client for Haven & Hearth that allows you to manage multiple game sessions through a web interface.

This is still alpha version, more like Proof of Concept


# Fast run
If you are not scared of running precompiled version of this client, and you have docker:
```

docker run -p 7901:7901 -d razikus/webhaven:0.1

open your browser on http://localhost:7901 


OR

docker run -p 7901:7901 -e AUTOLOGIN_USER=ACCNAME -e AUTOLOGIN_PASSWORD=PASSWORD -e AUTOLOGIN_CHAR=CHARNAME -d razikus/webhaven:0.1

This will additionally init your session

```

# Without docker

Download .jar from there

https://github.com/Razikus/WebHaven/releases/

Or build it yourself - ```mvn clean install```


## Prerequisites

- Java 21 or later
- Maven 3.9+
- Node.js 20 or later
- Yarn package manager

## Building from Source

1. Clone the repository:
```bash
git clone https://github.com/Razikus/WebHaven.git
cd WebHafen
```

2. Build the frontend:
```bash
cd src/main/resources/public/WebHaven
yarn install
yarn build
cd ../../../..
```

3. Build the backend:
```bash
mvn clean install
```

## Running

### Using Java

Run the compiled JAR directly:

```bash
java -jar target/WebHafen-1.0-SNAPSHOT.jar
```

### Using Docker

Build and run using Docker:

```bash
docker build -t webhaven .
docker run -p 7901:7901 webhaven
```

### Environment Variables

The following environment variables can be configured:

- `HOST` - Host to bind to (default: 0.0.0.0)
- `PORT` - Port to listen on (default: 7901)
- `AUTOLOGIN_USER` - Optional username for auto-login
- `AUTOLOGIN_PASSWORD` - Optional password for auto-login
- `AUTOLOGIN_CHAR` - Optional character name for auto-login

## Usage

1. Open a web browser and navigate to `http://localhost:7901` (or your configured host/port)
2. Use the login page to start a new game session
3. Manage active sessions through the sessions page
4. Chat and monitor players through the web interface

## Features

- Multiple simultaneous game sessions
- Web-based chat interface
- Player monitoring
- Real-time WebSocket updates
- Responsive web UI built with Vue 3 and Tailwind CSS

## Future
- Automation framework 
- Map view
- Around-objects view
- Headless ALT creation
- Alerts & notifications

## Development

The project consists of:

- Backend: Java with Maven
- Frontend: Vue 3 + Vite + Tailwind CSS
- WebSocket communication between frontend and backend
- Docker support for containerized deployment

To develop the frontend:

```bash
cd src/main/resources/public/WebHaven
yarn dev
```
