package tech.pegasys.artemis.networking.handel.libp2p;

import io.libp2p.core.P2PAbstractChannel;
import io.libp2p.core.multistream.Mode;
import io.libp2p.core.multistream.ProtocolBinding;
import io.libp2p.core.multistream.ProtocolMatcher;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class HandelBinding implements ProtocolBinding<HandelController> {

  private final HandelProtocol handel;

  public HandelBinding(HandelProtocol handel) {
    this.handel = handel;
  }

  @NotNull
  @Override
  public String getAnnounce() {
    return "/handel/0.1.0/";
  }

  @NotNull
  @Override
  public ProtocolMatcher getMatcher() {
    return new ProtocolMatcher(Mode.STRICT, getAnnounce());
  }

  @NotNull
  @Override
  public CompletableFuture<? extends HandelController> initChannel(@NotNull P2PAbstractChannel ch,
      @NotNull String selectedProtocol) {
    return handel.initChannel(ch);
  }
}
