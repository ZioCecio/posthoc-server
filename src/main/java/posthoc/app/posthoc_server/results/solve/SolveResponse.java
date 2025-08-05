package posthoc.app.posthoc_server.results.solve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SolveResponse {
    public String version;
    public Render render;
    public List<Event> events;

    public SolveResponse(List<Event> dynamicEvents) {
        this.version = "1.0.5";
        this.render = buildRender();
        this.events = dynamicEvents;
    }

    private Render buildRender() {
        Render render = new Render();
        render.context = new HashMap<>(); // empty context

        // Build components
        Component components = new Component();
        List<TileComponent> tiles = new ArrayList<>();

        for (String type : List.of("source", "destination", "plant", "tap", "move", "pour", "pick")) {
            TileComponent tile = new TileComponent();
            tile.type = "rect";
            tile.width = 1;
            tile.height = 1;
            tile.x = "{{$.event.x}}";
            tile.y = "{{$.event.y}}";
            tile.fill = "{{ $.color[$.event.type] }}";
            tile.condition = String.format("${{ $.event.type == \"%s\" }}", type);
            tiles.add(tile);
        }

        components.tile = tiles;
        render.components = components;

        // Build views
        View mainView = new View();
        ComponentReference tileRef = new ComponentReference();
        tileRef.ref = "tile";
        mainView.components = Collections.singletonList(tileRef);

        Views views = new Views();
        views.main = mainView;
        render.views = views;

        return render;
    }
}
