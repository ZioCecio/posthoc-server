package posthoc.app.posthoc_server.handlers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.hstairs.ppmajal.transition.TransitionGround;

import enhsp2.ENHSP;
import enhsp2.ENHSPBuilder;
import posthoc.app.posthoc_server.params.SolveInstance;
import posthoc.app.posthoc_server.params.SolveParams;
import posthoc.app.posthoc_server.results.solve.Event;
import posthoc.app.posthoc_server.results.solve.SolveResponse;

class Point {
    int x;
    int y;

    public Point(int val, int mapLen) {
        this.x = val % mapLen;
        this.y = val / mapLen;
    }
}

public class SolveHandler {
    private static String domain = "mt-plant-watering-constrained";
    private static final String BASE_DIR = "/home/ziocecio/Documents/Projects/jpddlplus/";
    private static final String JAR_FILE = BASE_DIR + "jpddlplus.jar";
    private static final String DOMAIN_FILE = BASE_DIR + "examples/plant-watering/domain.pddl";
    // private static final String RUN_COMMAND = "java -jar %s -o %s -f %s";

    public static SolveResponse solveProblem(SolveParams params) {
        // check if params are incomplete (we are setting params from the UI)
        if (isIncomplete(params)) {
            // return feedback to posthoc UI
            List<Event> events = partialResponse(params);
            return new SolveResponse(events);
        }
        // otherwise, run the code
        runCodee(params);

        return runCode(params);
    }

    private static void runCodee(SolveParams params) {
        String content = getInstanceFile(params);

        System.out.println("Running the following instance file\n" + content + "\n\n\n");

        java.nio.file.Path tempFile;
        String tempFilePath;
        try {
            tempFile = java.nio.file.Files.createTempFile("instance-", ".pddl");
            java.nio.file.Files.write(tempFile, content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            tempFilePath = tempFile.toAbsolutePath().toString();
            System.out.println("Instance file written to: " + tempFilePath);

        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create/write temp instance file", e);
        }

        ENHSP solver = new ENHSPBuilder(true)
            .defaultBuilder()
            .setDomainFile(DOMAIN_FILE)
            .setProblemFile(tempFilePath)
            .buildAndInitialize();

        solver.configurePlanner();
        solver.parsingDomainAndProblem(null);
        LinkedList<ImmutablePair<BigDecimal, TransitionGround>> solution = solver.planAndGetSolution();

        for(ImmutablePair<BigDecimal, TransitionGround> element : solution) {
            System.out.println(element);
        }
    }

    private static SolveResponse runCode(SolveParams params) {
        // create instanceFile
        String content = getInstanceFile(params);

        // run the ENHSP jar file and get the response
        System.out.println("Running the following instance file\n" + content + "\n\n\n");

        // create a temporary file and write the content to it
        java.nio.file.Path tempFile;
        String tempFilePath;
        try {
            tempFile = java.nio.file.Files.createTempFile("instance-", ".pddl");
            java.nio.file.Files.write(tempFile, content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            tempFilePath = tempFile.toAbsolutePath().toString();
            System.out.println("Instance file written to: " + tempFilePath);

        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create/write temp instance file", e);
        }

        // run the command and capture the output
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(JAR_FILE);
        command.add("-o");
        command.add(DOMAIN_FILE);
        command.add("-f");
        command.add(tempFilePath);

        System.out.println("Running command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // merge stderr into stdout

        ArrayList<String> results = new ArrayList<>();
        try {
            Process process = processBuilder.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException(
                        "ENHSP process exited with code " + exitCode + "\nOutput:\n" + String.join("\n", results));
            }
            System.out.println("ENHSP output:\n" + String.join("\n", results));
        } catch (Exception e) {
            throw new RuntimeException("Failed to run ENHSP command", e);
        }

        return parseResult(results, params);
    }

    private static boolean isIncomplete(SolveParams params) {
        // Check if any required fields are missing
        return params.instances == null || params.instances.isEmpty() ||
                params.instances.get(0).plants == null || params.instances.get(0).taps == null ||
                params.instances.get(0).plants.length == 0 || params.instances.get(0).taps.length == 0 ||
                params.instances.get(0).start == 0 || params.instances.get(0).end == 0 ||
                params.instances.get(0).pourAmounts == null || params.instances.get(0).pourAmounts.length == 0 ||
                params.mapURI == null || params.mapURI.isEmpty() ||
                params.algorithm == null || params.algorithm.isEmpty();
    }

    private static List<Event> partialResponse(SolveParams params) {
        List<Event> dynamicEvents = new ArrayList<>();

        int mapLen = params.getMapLen();
        int id = 100;

        Integer start = params.instances.get(0).start;
        Integer end = params.instances.get(0).end;
        Integer[] plants = params.instances.get(0).plants;
        Integer[] taps = params.instances.get(0).taps;

        if (start != null && start != 0) {
            Point p = new Point(start.intValue(), mapLen);
            dynamicEvents.add(new Event("source", id++, p.x, p.y, 0L, 0.0, 0));
        }

        if (end != null && end != 0) {
            Point p = new Point(end.intValue(), mapLen);
            dynamicEvents.add(new Event("destination", id++, p.x, p.y, 0L, 0.0, 0));
        }

        if (plants != null && plants.length > 0) {
            for (Integer plant : plants) {
                Point p = new Point(plant.intValue(), mapLen);
                dynamicEvents.add(new Event("plant", id++, p.x, p.y, 0L, 0.0, 0));
            }
        }

        if (taps != null && taps.length > 0) {
            for (Integer tap : taps) {
                Point p = new Point(tap.intValue(), mapLen);
                dynamicEvents.add(new Event("tap", id++, p.x, p.y, 0L, 0.0, 0));
            }
        }

        return dynamicEvents;
    }

    private static String getInstanceFile(SolveParams params) {
        SolveInstance instance = params.instances.get(0); // only one instance
        StringBuilder sb = new StringBuilder();
        int mapLen = params.getMapLen();

        sb.append("(define (problem generated-instance)\n");
        sb.append("  (:domain ").append(domain).append(")\n");

        // Objects
        sb.append("  (:objects\n");
        sb.append("    tap1 - tap\n"); // CHECK from examples i see only one tap
        sb.append("    agent1 - agent\n");
        for (int i = 0; i < instance.pourAmounts.length; i++) {
            sb.append("plant").append(i + 1).append(" ");
        }
        sb.append("- plant\n");
        sb.append("  )\n\n");

        // Init
        sb.append("  (:init\n");
        sb.append("    (= (max_int) 60)\n");
        sb.append("    (= (maxx) ").append(mapLen).append(")\n");
        sb.append("    (= (minx) 1)\n");
        sb.append("    (= (maxy) ").append(mapLen).append(")\n");
        sb.append("    (= (miny) 1)\n");
        sb.append("    (= (carrying) 0)\n");
        sb.append("    (= (total_poured) 0)\n");
        sb.append("    (= (total_loaded) 0)\n");
        for (int i = 0; i < instance.pourAmounts.length; i++) {
            sb.append("    (= (poured plant").append(i + 1).append(") 0)\n");
        }

        // Agent start position
        Point agentStart = new Point(instance.start, mapLen);
        sb.append("    (= (x agent1) ").append(agentStart.x + 1).append(")\n");
        sb.append("    (= (y agent1) ").append(agentStart.y + 1).append(")\n");

        // Tap position (assume tap1 is at taps[0])
        if (instance.taps.length > 0) {
            Point tap = new Point(instance.taps[0], mapLen);
            sb.append("    (= (x tap1) ").append(tap.x + 1).append(")\n");
            sb.append("    (= (y tap1) ").append(tap.y + 1).append(")\n");
        }

        // Plant positions
        for (int i = 0; i < instance.plants.length && i < instance.pourAmounts.length; i++) {
            Point plant = new Point(instance.plants[i], mapLen);
            sb.append("    (= (x plant").append(i + 1).append(") ").append(plant.x + 1).append(")\n");
            sb.append("    (= (y plant").append(i + 1).append(") ").append(plant.y + 1).append(")\n");
        }
        sb.append("  )\n\n");

        // Goal
        sb.append("  (:goal (and\n");
        for (int i = 0; i < instance.pourAmounts.length; i++) {
            sb.append("    (= (poured plant").append(i + 1).append(") ")
                    .append(instance.pourAmounts[i]).append(")\n");
        }

        // Total poured = sum of all individual pours
        sb.append("    (= (total_poured) ");
        if (instance.pourAmounts.length == 1) {
            sb.append("(poured plant1)");
        } else {
            for (int i = 0; i < instance.pourAmounts.length; i++) {
                if (i == 0) {
                    sb.append("(poured plant1)");
                } else {
                    sb.insert(sb.lastIndexOf("(poured"), "(+ ");
                    sb.append(" (poured plant").append(i + 1).append("))");
                }
            }
        }
        sb.append(")\n");
        sb.append("  ))\n");
        sb.append(")\n");
        return sb.toString();
    }

    private static SolveResponse parseResult(List<String> results, SolveParams params) {
        // starting with base events (start, end, plants, taps)
        List<Event> dynamicEvents = partialResponse(params);
        int id = 100 + dynamicEvents.size(); // use the next available id

        // Starting position of the agent
        Point pos = new Point(params.instances.get(0).start, params.getMapLen());
        ArrayList<Point> plants = new ArrayList<>();
        for (Integer plant : params.instances.get(0).plants) {
            plants.add(new Point(plant, params.getMapLen()));
        }
        ArrayList<Point> taps = new ArrayList<>();
        for (Integer tap : params.instances.get(0).taps) {
            taps.add(new Point(tap, params.getMapLen()));
        }

        for (String line : results) {
            line = line.trim();
            if (line.isEmpty() || !line.contains(": (")) {
                continue;
            }

            try {
                // Extract time and action
                String[] parts = line.split(":\\s+\\(");
                double time = Double.parseDouble(parts[0]);
                String actionPart = parts[1].replace(")", "").trim();
                String[] tokens = actionPart.split("\\s+");

                if (tokens.length < 2)
                    continue;

                String action = tokens[0];
                String type;

                Map<String, int[]> directionMap = Map.of(
                        "move_up", new int[] { 0, 1 },
                        "move_down", new int[] { 0, -1 },
                        "move_left", new int[] { -1, 0 },
                        "move_right", new int[] { 1, 0 },
                        "move_up_left", new int[] { -1, 1 },
                        "move_up_right", new int[] { 1, 1 },
                        "move_down_left", new int[] { -1, -1 },
                        "move_down_right", new int[] { 1, -1 });

                if (directionMap.containsKey(action)) {
                    int[] delta = directionMap.get(action);
                    pos.x += delta[0];
                    pos.y += delta[1];
                    type = "move";
                } else {
                    switch (action) {
                        case "load":
                            type = "pick";
                            break;
                        case "pour":
                            type = "pour";
                            break;
                        default:
                            continue;
                    }
                }

                Event event = new Event(
                        type,
                        id++,
                        pos.x,
                        pos.y,
                        (long) time,
                        0.0,
                        0);

                dynamicEvents.add(event);

                // NOT NECESSARY since in the ui i set the color of pick and pour events
                // respectively equals to tap and plant colors
                //
                //
                // restore the plant color on the UI map after the move
                // if (type.equals("move") && plants.stream().anyMatch(p -> p.x == pos.x && p.y
                // == pos.y)) {
                // Event plantEvent = new Event(
                // "plant",
                // id++,
                // pos.x,
                // pos.y,
                // (long) time,
                // 0.0,
                // 0);
                // dynamicEvents.add(plantEvent);
                // }
                // // do the same for taps
                // if (type.equals("move") && taps.stream().anyMatch(t -> t.x == pos.x && t.y ==
                // pos.y)) {
                // Event tapEvent = new Event(
                // "tap",
                // id++,
                // pos.x,
                // pos.y,
                // (long) time,
                // 0.0,
                // 0);
                // dynamicEvents.add(tapEvent);
                // }

            } catch (Exception e) {
                continue;
            }
        }

        return new SolveResponse(dynamicEvents);
    }
}
