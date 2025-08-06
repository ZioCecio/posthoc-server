package posthoc.app.posthoc_server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class Main {

    public static void main(String[] args) {
        String filePath = "/home/alessandro/Projects/robotica/posthoc-server/output.txt";

        String input;
        try {
            input = Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        String[] parts = input.split("SearchNode\\{");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", "1.4.0");

        List<Map<String, Object>> eventsList = new ArrayList<>();

        // === Define mapping dictionaries ===
        Map<String, String> domainActionToType = Map.of(
                "drive", "move",
                "buy-all", "pick",
                "buy-allneeded", "pick");

        for (String part : parts) {
            if (part.trim().isEmpty())
                continue;

            String fullNode = "SearchNode{" + part;

            String id = extractValue(fullNode, "id=([a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12})");
            String parent = extractValue(fullNode, "parent=([a-f0-9]{8}(?:-[a-f0-9]{4}){3}-[a-f0-9]{12})");
            String action = extractValue(fullNode, "action=\\(([^)]+)\\)");

            String type = inferTypeFromAction(action, domainActionToType);

            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", type);
            event.put("id", id);
            event.put("pId", parent != null ? parent : null);

            eventsList.add(event);
        }

        result.put("events", eventsList);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        Yaml yaml = new Yaml(options);
        String yamlOutput = yaml.dump(result);

        String outputFilePath = "out.trace.yaml";
        try {
            Files.writeString(Paths.get(outputFilePath), yamlOutput);
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    private static String extractValue(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String inferTypeFromAction(String action, Map<String, String> domainActionMap) {
        if (action == null)
            return "unknown";
        String actionLower = action.toLowerCase();

        for (String domainAction : domainActionMap.keySet()) {
            if (actionLower.startsWith(domainAction)) {
                return domainActionMap.get(domainAction);
            }
        }

        return "unknown";
    }
}
