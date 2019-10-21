package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.P2PAbstractChannel;
import io.libp2p.core.P2PAbstractHandler;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class HandelProtocol implements P2PAbstractHandler<HandelController> {

  @NotNull
  @Override
  public CompletableFuture<? extends HandelController> initChannel(@NotNull P2PAbstractChannel ch) {
    if (ch.isInitiator()) {
      HandelChannelHandler.HandelInitiatorChannelHandler handelInitiatorChannelHandler = new HandelChannelHandler.HandelInitiatorChannelHandler();
      ch.getNettyChannel().pipeline().addLast(handelInitiatorChannelHandler);
      return handelInitiatorChannelHandler.activeFuture.thenApply(fn -> handelInitiatorChannelHandler);
    } else {
      HandelChannelHandler.HandelResponderChannelHandler handelResponderChannelHandler = new HandelChannelHandler.HandelResponderChannelHandler();
      ch.getNettyChannel().pipeline().addLast(handelResponderChannelHandler);
      return CompletableFuture.completedFuture(handelResponderChannelHandler);
    }
  }

}
