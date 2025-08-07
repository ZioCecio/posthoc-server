package posthoc.app.posthoc_server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class Main {

    private static final String INPUT_FILE = "/home/alessandro/Projects/robotica/posthoc-server/output.txt";
    private static final String OUTPUT_FILE = "out.trace.yaml";
    private static final Map<String, String> DOMAIN_ACTION_TO_TYPE = Map.of(
            "drive", "move",
            "buy-all", "pick",
            "buy-allneeded", "pick");

    private static class Action {
        String id, parent, source, target, type, subject;
        Double gValue;

        public Action(String id, String parent, String source, String target, String type, String subject,
                Double gValue) {
            this.id = id;
            this.parent = parent;
            this.source = source;
            this.target = target;
            this.type = type;
            this.subject = subject;
            this.gValue = gValue;
        }

        public Map<String, Object> toEvent(Map<String, Integer> idMap) {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", type);
            event.put("id", idMap.get(id));
            event.put("pId", parent != null ? idMap.get(parent) : null);
            event.put("source", source);
            event.put("target", target);
            event.put("subject", subject);
            event.put("gValue", gValue);
            return event;
        }
    }

    public static void main(String[] args) {
        String input = readFile(INPUT_FILE);
        if (input == null)
            return;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", "1.4.0");

        List<Action> actions = parseActions(input);
        Map<String, Integer> idMap = generateIdMap(actions);

        List<Map<String, Object>> events = new ArrayList<>();
        for (Action action : actions) {
            events.add(action.toEvent(idMap));
        }

        result.put("events", events);
        writeYaml(result, OUTPUT_FILE);
    }

    private static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    private static void writeYaml(Map<String, Object> data, String path) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        Yaml yaml = new Yaml(options);
        try {
            Files.writeString(Paths.get(path), yaml.dump(data));
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    private static List<Action> parseActions(String input) {
        String[] parts = input.split("SearchNode\\{");
        List<Action> actions = new ArrayList<>();

        for (String part : parts) {
            if (part.trim().isEmpty())
                continue;
            String fullNode = "SearchNode{" + part;

            String id = extractValue(fullNode, "id=([a-f0-9\\-]{36})");
            if (id == null) {
                System.out.println("Warning: ID not found in part: " + part);
                continue;
            }

            String parent = extractValue(fullNode, "parent=([a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12})");
            String actionStr = extractValue(fullNode, "action=\\(([^)]+)\\)");
            String gValueStr = extractValue(fullNode, "gValue=([0-9]+(?:\\.[0-9]+)?)");

            String type = "unknown", subject = "", source = "", target = "";
            if (actionStr != null) {
                String[] partsArr = actionStr.split(" ");
                if (partsArr.length >= 4) {
                    type = inferTypeFromAction(partsArr[0]);
                    subject = partsArr[1];
                    source = partsArr[2];
                    target = partsArr[3];
                }
            }

            Double gValue = gValueStr != null ? Double.parseDouble(gValueStr) : null;
            actions.add(new Action(id, parent, source, target, type, subject, gValue));
        }

        return actions;
    }

    private static Map<String, Integer> generateIdMap(List<Action> actions) {
        Map<String, Integer> idMap = new LinkedHashMap<>();
        int counter = 1;
        for (Action action : actions) {
            if (!idMap.containsKey(action.id)) {
                idMap.put(action.id, counter++);
            }
        }
        return idMap;
    }

    private static String extractValue(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String inferTypeFromAction(String action) {
        return DOMAIN_ACTION_TO_TYPE.getOrDefault(action.toLowerCase(), "unknown");
    }
}
