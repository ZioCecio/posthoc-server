package posthoc.app.posthoc_server.handlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hstairs.ppmajal.PDDLProblem.PDDLSolution;
import com.hstairs.ppmajal.search.searchnodes.SimpleSearchNode;

import enhsp2.ENHSP;
import enhsp2.ENHSPBuilder;
import posthoc.app.posthoc_server.loggers.PosthocStateLogger;
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
    private static final String DOMAIN_FILE = BASE_DIR + "examples/plant-watering/domain.pddl";

    public static SolveResponse solveProblem(SolveParams params) {
        // check if params are incomplete (we are setting params from the UI)
        if (isIncomplete(params)) {
            System.out.println("Incomplete params...");
            // return feedback to posthoc UI
            List<Event> events = partialResponse(params);
            return new SolveResponse(events);
        }
        // otherwise, run the code
        return runCode(params);
    }

    private static SolveResponse runCode(SolveParams params) {
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

        PosthocStateLogger logger = new PosthocStateLogger();
        ENHSP solver = new ENHSPBuilder(true)
            .defaultBuilder()
            .setDomainFile(DOMAIN_FILE)
            .setProblemFile(tempFilePath)
            .setSearchEngine(params.algorithm)
            .setExternalLogger(logger)
            .buildAndInitialize();

        solver.configurePlanner();
        solver.parsingDomainAndProblem(null);
        PDDLSolution solution = solver.planAndGetSolution();
        SimpleSearchNode node = solution.lastNode();
        List<Event> soluionEvents = partialResponse(params);

        List<Event> reversedEvents = new LinkedList<>();        
        while(node != null) {
            if(node.transition == null) break;

            String action = node.transition
                .toString()
                .split(" ")[0]
                .substring(1);

            if(action.contains("move")) {
                action = "move";
            } else if(action.equals("load")) {
                action = "pick";
            }

            String states = node.s
                .toString()
                .trim();

            int x = 0, y = 0;
            Pattern xPattern = Pattern.compile("\\(x agent1\\)=(\\d+\\.?\\d*)");
            Pattern yPattern = Pattern.compile("\\(y agent1\\)=(\\d+\\.?\\d*)");

            Matcher xMatcher = xPattern.matcher(states);
            Matcher yMatcher = yPattern.matcher(states);

            if(xMatcher.find()) {
                x = (int)Float.parseFloat(xMatcher.group(1)) - 1;
            }
            if(yMatcher.find()) {
                y = (int)Float.parseFloat(yMatcher.group(1)) - 1;
            }

            reversedEvents.add(new Event(action, node.id, x, y, node.father.id, (double) node.gValue, null));
            node = node.father;
        }
        
        if(params.instances.get(0).getSolutionOnly) {
            soluionEvents.addAll(reversedEvents.reversed());
        } else {
            soluionEvents.addAll(logger.getLoggedEvents());
        }

        return new SolveResponse(soluionEvents);
    }
    
    private static boolean isIncomplete(SolveParams params) {
        // Check if any required fields are missing
        return params.instances == null || params.instances.isEmpty() ||
                params.instances.get(0).start == 0 ||
                !isValidArray(params.instances.get(0).plants) ||
                !isValidArray(params.instances.get(0).taps) ||
                !isAllFilled(params.instances.get(0).pourAmounts) ||
                params.mapURI == null || params.mapURI.isEmpty() ||
                params.algorithm == null || params.algorithm.isEmpty();
    }

    private static boolean isAllFilled(Integer[] array){
        if (!isValidArray(array)) {
            return false;
        }
        for (Integer val : array) {
            if (val == null || val == 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidArray(Integer[] array) {
        return array != null && array.length > 0;
    }

    private static List<Event> partialResponse(SolveParams params) {
        List<Event> dynamicEvents = new ArrayList<>();
        int mapLen = params.getMapLen();

        Integer start = params.instances.get(0).start;
        Integer end = params.instances.get(0).end;
        Integer[] plants = params.instances.get(0).plants;
        Integer[] taps = params.instances.get(0).taps;

        if (start != null && start != 0) {
            Point p = new Point(start.intValue(), mapLen);
            dynamicEvents.add(new Event("source", UUID.randomUUID(), p.x, p.y, null, 0.0, 0));
        }

        if (end != null && end != 0) {
            Point p = new Point(end.intValue(), mapLen);
            dynamicEvents.add(new Event("destination", UUID.randomUUID(), p.x, p.y, null, 0.0, 0));
        }

        if (plants != null && plants.length > 0) {
            for (Integer plant : plants) {
                Point p = new Point(plant.intValue(), mapLen);
                dynamicEvents.add(new Event("plant", UUID.randomUUID(), p.x, p.y, null, 0.0, 0));
            }
        }

        if (taps != null && taps.length > 0) {
            for (Integer tap : taps) {
                Point p = new Point(tap.intValue(), mapLen);
                dynamicEvents.add(new Event("tap", UUID.randomUUID(), p.x, p.y, null, 0.0, 0));
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
}
