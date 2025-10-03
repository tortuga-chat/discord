package chat.tortuga.discord.jellyfin.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class RequestLogFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext request, ClientResponseContext response) {
        log.info("ClientRequest {}: {} - {}", request.getClient(), request.getUri(), response.getStatus());
    }

}
