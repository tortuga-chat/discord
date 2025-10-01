package chat.tortuga.discord.jellyfin.resource;

import chat.tortuga.discord.jellyfin.service.DownloadsService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/downloads")
@RequiredArgsConstructor
public class DownloadsResource {

    private final DownloadsService service;

    @POST
    public Response downloadEnded(@HeaderParam("X-Download-Id") String id,
                                  @HeaderParam("X-Download-Name") String name,
                                  @HeaderParam("X-Download-Path") String path,
                                  @HeaderParam("X-Download-Tags") String tags) {
        if (id == null || name == null) throw new BadRequestException();
        service.downloadFinished(id, name, path, tags);
        return Response.ok().build();
    }

}
