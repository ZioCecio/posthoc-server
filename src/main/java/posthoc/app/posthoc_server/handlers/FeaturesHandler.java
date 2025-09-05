package posthoc.app.posthoc_server.handlers;

import java.util.ArrayList;
import java.util.List;

import enhsp2.ENHSPBuilder;
import posthoc.app.posthoc_server.params.FeatureAlgorithmParams;
import posthoc.app.posthoc_server.params.FeatureFormatParams;
import posthoc.app.posthoc_server.params.FeatureProblemTypeParams;
import posthoc.app.posthoc_server.results.FeatureAlgorithmResult;
import posthoc.app.posthoc_server.results.FeatureFormatResult;
import posthoc.app.posthoc_server.results.FeatureHeuristicResult;
import posthoc.app.posthoc_server.results.FeatureProblemTypeResult;

public class FeaturesHandler {
    public static List<FeatureAlgorithmResult> getAlgorithms(FeatureAlgorithmParams params) {
        ENHSPBuilder tmp = new ENHSPBuilder(true);

        String[][] availableSearchEngines = tmp.getAvailableSearchEngines();

        ArrayList<FeatureAlgorithmResult> results = new ArrayList<>();
        for (String[] engine : availableSearchEngines) {
            results.add(new FeatureAlgorithmResult(engine[0], engine[1], engine[2]));
        }

        return results;
    }

    public static List<FeatureHeuristicResult> getHeuristics(FeatureAlgorithmParams params) {
        ENHSPBuilder tmp = new ENHSPBuilder(true);

        String[][] availableHeuristics = tmp.getAvailableHeuristics();

        ArrayList<FeatureHeuristicResult> results = new ArrayList<>();
        for (String[] heuristic : availableHeuristics) {
            results.add(new FeatureHeuristicResult(heuristic[0], heuristic[1], heuristic[2]));
        }

        return results;
    }

    public static List<FeatureFormatResult> getFormats(FeatureFormatParams params) {
        return List.of(
                new FeatureFormatResult("grid", "Grid", "grid"),
                new FeatureFormatResult("xy", "Xy", "xy"));
    }

    public static List<FeatureProblemTypeResult> getProblemTypes(FeatureProblemTypeParams params) {
        return List.of(
                new FeatureProblemTypeResult("pathfinding", "Pathfinding", "Pathfinding problems"),
                new FeatureProblemTypeResult("plant-watering", "Plant Watering", "Plant Watering problems"));
    }
}
