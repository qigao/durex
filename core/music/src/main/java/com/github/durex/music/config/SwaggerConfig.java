package com.github.durex.music.config;

import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
    tags = {
      @Tag(name = "Playlist", description = "Playlist related operations."),
      @Tag(name = "Music", description = "Music related operations."),
    },
    info =
        @Info(
            title = "XiaVan Music API",
            version = "0.0.1",
            contact =
                @Contact(name = "Xia Van", url = "http://mummyway.com", email = "xxx@xiavann.com")))
public class SwaggerConfig extends Application {}
