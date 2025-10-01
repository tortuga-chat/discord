package chat.tortuga.discord.jellyfin.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;

@RegisterRestClient(configKey = "jellyfin")
public interface JellyfinClient {

    @POST
    @Path("/Library/Refresh")
    @ClientHeaderParam(name=AUTHORIZATION, value="${jellyfin.auth-token}")
    RestResponse<Void> libraryRefresh();

}
