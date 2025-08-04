package posthoc.app.posthoc_server.handlers;

import posthoc.app.posthoc_server.params.SolveParams;

public class SolveHandler {
    public static int solveProblem(SolveParams params) {
        System.out.println(params.mapURI);
        System.out.println(params.algorithm);
        System.out.println(params.format);
        System.out.println(params.instances);

        return 0;
    }
}
