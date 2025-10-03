package chat.tortuga.discord.jellyfin.resource;

import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

@Slf4j
public class RequestLogFilter {

    @ServerRequestFilter(preMatching = true)
    public void log(ContainerRequestContext request) {
        log.info("{} {} - headers: {}", request.getMethod(), request.getUriInfo().getRequestUri(), request.getHeaders());
    }

}
