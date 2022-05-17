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
            title = "Demo Music API",
            version = "0.0.1",
            contact = @Contact(name = "Durex Music", url = "https://github.com/qigao/durex")))
public class SwaggerConfig extends Application {}
