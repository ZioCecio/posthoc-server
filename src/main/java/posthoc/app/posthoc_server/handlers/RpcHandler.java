package posthoc.app.posthoc_server.handlers;

public interface RpcHandler<P, R> {
    R handle(P params);
}
