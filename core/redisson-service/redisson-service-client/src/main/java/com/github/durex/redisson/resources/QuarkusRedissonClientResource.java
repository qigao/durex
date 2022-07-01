package com.github.durex.redisson.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.github.durex.redisson.service.RemoteServiceReactiveApi;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RExecutorFuture;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.redisnode.RedisNodes;

@Slf4j
@Produces(APPLICATION_JSON)
@Path("/quarkus-redisson-client")
public class QuarkusRedissonClientResource {

  @Inject RedissonClient redisson;
  @Inject RedissonReactiveClient reactiveRedisson;
  @Inject RemoteServiceReactiveApi remoteService;
  @Inject RExecutorFuture<String> futureTask;

  @GET
  @Path("/map")
  public Uni<String> map() {
    RMapReactive<String, String> m = reactiveRedisson.getMap("test");
    var result =
        m.put("1", "2").then(m.get("1")).doOnEach(v -> log.info("map result: {}", v.get()));
    return Uni.createFrom().publisher(result);
  }

  @GET
  @Path("/remoteService")
  public Uni<String> remoteService() {
    return Uni.createFrom().publisher(remoteService.executeMe());
  }

  @GET
  @Path("/pingAll")
  public Uni<String> pingAll() {
    redisson.getRedisNodes(RedisNodes.SINGLE).pingAll();
    return Uni.createFrom().item("OK");
  }

  @GET
  @Path("/executeTask")
  public Uni<String> executeTask() {
    return Uni.createFrom().future(futureTask);
  }
}
