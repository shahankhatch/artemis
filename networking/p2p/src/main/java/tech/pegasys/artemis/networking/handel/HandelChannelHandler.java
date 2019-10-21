package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.Libp2pException;
import io.libp2p.etc.types.ByteArrayExtKt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import kotlin.Pair;

public class HandelChannelHandler {

  public static class HandelInitiatorChannelHandler extends
      SimpleChannelInboundHandler<ByteBuf> implements HandelController {

    CompletableFuture<Void> activeFuture = new CompletableFuture<>();
    Map<String, Pair<Long, CompletableFuture<Long>>> requests = Collections
        .synchronizedMap(new HashMap<String, Pair<Long, CompletableFuture<Long>>>());
    ChannelHandlerContext ctx ;
    boolean closed = false;

    @Override
    public void ping() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
      String dataS = ByteArrayExtKt.toHex(msg.array());
      Pair<Long, CompletableFuture<Long>> remove = requests.remove(dataS);
      if (remove == null) {
        // nop for now if the ping is unknown
        return;
      }
      // TODO put something more meaningful in place of L
      remove.component2().complete(1L);
    }
  }

  public static class HandelResponderChannelHandler extends ChannelInboundHandlerAdapter implements HandelController {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      ctx.writeAndFlush(msg);
    }

    @Override
    public void ping() {
      throw new Libp2pException("This Handel handler is responder only");
    }
  }
}
