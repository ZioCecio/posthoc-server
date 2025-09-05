# Posthoc-server

Java-based WebSocket server that acts as a solver for the **Posthoc Visualizer**.  
This server works **only** with [our modified version of the posthoc-app](https://github.com/bernagit/posthoc-app), which has been extended to send additional information to the solver.

---

## Specifications
- **Java:** OpenJDK 24.0.2 (2025-07-15)  
- **Maven:** Apache Maven 3.9.11  
- **Operating System:** Linux (tested on Arch Linux, kernel 6.16.4-arch1-1, amd64)  

---

## Installation
Clone the repository and install dependencies:

```bash
git clone https://github.com/ZioCecio/posthoc-server.git
cd posthoc-server
mvn clean install
```

## Running the application
Start the server with:

```bash
mvn spring-boot:run
```

## Connect to Posthoc-app
Clone [our version of the posthoc-app](https://github.com/bernagit/posthoc-app) and follow its setup instructions to run the client.
Once the client is running:
1. Open **Settings > Extensions > Add adapter**. 
2. Enter the following connection details:
  - **URL**: `ws://localhost:4567`
  - **Connection Type**: `socket.io`

The client should now connect to the posthoc-server and use it as a solver.