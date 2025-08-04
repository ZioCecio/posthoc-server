package posthoc.app.posthoc_server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import posthoc.app.posthoc_server.handlers.AboutHandler;
import posthoc.app.posthoc_server.handlers.FeaturesHandler;
import posthoc.app.posthoc_server.handlers.HandlerEntry;
import posthoc.app.posthoc_server.handlers.RpcHandler;
import posthoc.app.posthoc_server.params.AboutParams;
import posthoc.app.posthoc_server.params.FeatureAlgorithmParams;

@Component
public class EventController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, HandlerEntry<?, ?>> handlers = new HashMap<>();
    
    @Autowired
    public EventController(SocketIOServer server) {
        register("about", AboutParams.class, AboutHandler::getInfo);
        register("features/algorithms", FeatureAlgorithmParams.class, FeaturesHandler::getAlgorithms);

        server.addConnectListener(client ->
            System.out.printf("🔌 Client connected: %s%n", client.getSessionId())
        );

        server.addEventListener("request", ObjectNode.class, this::onRequest);
    }

    private void onRequest(SocketIOClient client, ObjectNode request, AckRequest ack) {
        try {
            String method = request.get("method").asText();
            JsonNode paramsNode = request.get("params");
            JsonNode idNode = request.get("id");

            HandlerEntry<?, ?> handlerEntry = handlers.get(method);
            if(handlerEntry == null) {
                return;
            }

            sendResponse(handlerEntry, client, idNode, paramsNode);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private <P, R> void sendResponse(HandlerEntry<P, R> handlerEntry, SocketIOClient client, JsonNode idNode, JsonNode paramsNode) throws JsonProcessingException, IllegalArgumentException {
        P params = objectMapper.treeToValue(paramsNode, handlerEntry.paramType);
        R result = handlerEntry.handler.handle(params);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", idNode);
        response.set("result", objectMapper.valueToTree(result));

        client.sendEvent("response", response);
    }

    private <P, R> void register(String method, Class<P> paramType, RpcHandler<P, R> handler) {
        handlers.put(method, new HandlerEntry(paramType, handler));
    }
}
