package posthoc.app.posthoc_server.handlers;

public class HandlerEntry<P, R> {
    public final Class<P> paramType;
    public final RpcHandler<P, R> handler;

    public HandlerEntry(Class<P> paramType, RpcHandler<P, R> handler) {
        this.paramType = paramType;
        this.handler = handler;
    }
}