package posthoc.app.posthoc_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

@org.springframework.context.annotation.Configuration
public class SocketIOConfig {
    @Value("${wss.server.host}")
	private String host;

	@Value("${wss.server.port}")
	private Integer port;

	@Bean
	public SocketIOServer socketIOServer() {
		Configuration configuration = new Configuration();
		configuration.setHostname(host);
		configuration.setPort(port);

		return new SocketIOServer(configuration);
	};
}
