package posthoc.app.posthoc_server.loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hstairs.ppmajal.extraUtils.ExternalLoggerLogType;
import com.hstairs.ppmajal.extraUtils.IExternalLogger;
import com.hstairs.ppmajal.search.searchnodes.SimpleSearchNode;

import posthoc.app.posthoc_server.results.solve.Event;

public class PosthocStateLogger implements IExternalLogger {
    List<Event> loggedEvents;
    
    public PosthocStateLogger() {
        loggedEvents = new ArrayList<>();
    }

    @Override
    public void log(SimpleSearchNode node, ExternalLoggerLogType externalLoggerLogType) {
        if(externalLoggerLogType != ExternalLoggerLogType.Expanding) return;
        
        String action = "";
        if(node.transition == null) {
            action = "source";
        } else {
            action = node.transition
                .toString()
                .split(" ")[0]
                .substring(1);
        }

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

        loggedEvents.add(new Event(action, node.id, x, y, node.father == null ? null : node.father.id, (double)node.gValue, null));
    }
    
    public List<Event> getLoggedEvents() {
        return loggedEvents;
    }
}
