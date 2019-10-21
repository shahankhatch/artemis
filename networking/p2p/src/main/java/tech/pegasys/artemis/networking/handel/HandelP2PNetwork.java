/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.artemis.networking.handel;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import identify.pb.IdentifyOuterClass;
import io.libp2p.core.Connection;
import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.crypto.KEY_TYPE;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.BuildersJKt;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.etc.types.ByteArrayExtKt;
import io.libp2p.mux.mplex.MplexStreamMuxer;
import io.libp2p.protocol.Identify;
import io.libp2p.protocol.Ping;
import io.libp2p.security.noise.NoiseXXSecureChannel;
import io.libp2p.transport.tcp.TcpTransport;
import io.netty.handler.logging.LogLevel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.Level;
import tech.pegasys.artemis.networking.p2p.api.P2PNetwork;
import tech.pegasys.artemis.networking.p2p.jvmlibp2p.JvmLibp2pConfig;
import tech.pegasys.artemis.networking.p2p.jvmlibp2p.Libp2pPeerManager;
import tech.pegasys.artemis.util.alogger.ALogger;
import tech.pegasys.artemis.util.cli.VersionProvider;

public class HandelP2PNetwork implements P2PNetwork {

  private static final ALogger STDOUT = new ALogger("stdout");

  private final PrivKey privKey;
  private final JvmLibp2pConfig config;
  private final Host host;
  private final ScheduledExecutorService scheduler;
  private final Libp2pPeerManager peerManager;

  private Connection connection;

  public Connection getConnection() {
    return connection;
  }

  public JvmLibp2pConfig getConfig() {
    return config;
  }


  public HandelP2PNetwork(final JvmLibp2pConfig config, final EventBus eventBus) {
    this.privKey =
        config
            .getPrivateKey()
            .orElseGet(() -> KeyKt.generateKeyPair(KEY_TYPE.SECP256K1).component1());
    this.config = config;
    scheduler =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("libp2p-%d").build());
    peerManager = new Libp2pPeerManager(scheduler);

    host =
        BuildersJKt.hostJ(
            b -> {
              b.getIdentity().setFactory(() -> privKey);
              b.getTransports().add(TcpTransport::new);
              b.getSecureChannels().add(NoiseXXSecureChannel::new);
              b.getMuxers().add(MplexStreamMuxer::new);
              b.getNetwork()
                  .listen(
                      "/ip4/" + config.getNetworkInterface() + "/tcp/" + config.getListenPort());

              b.getProtocols().add(new Ping());
              b.getProtocols().add(new HandelBinding(new HandelProtocol()));

              if (config.isLogWireCipher()) {
                b.getDebug().getBeforeSecureHandler().setLogger(LogLevel.DEBUG, "wire.ciphered");
              }
              if (config.isLogWirePlain()) {
                b.getDebug().getAfterSecureHandler().setLogger(LogLevel.DEBUG, "wire.plain");
              }
              if (config.isLogMuxFrames()) {
                b.getDebug().getMuxFramesHandler().setLogger(LogLevel.DEBUG, "wire.mux");
              }

              b.getConnectionHandlers().add(peerManager);
            });
  }

  public Host getHost() {
    return host;
  }

  public Libp2pPeerManager getPeerManager() {
    return peerManager;
  }

  @Override
  public void run() {
    STDOUT.log(Level.INFO, "Starting handel noise network...");
    host.start()
        .thenApply(
            i -> {
              STDOUT.log(
                  Level.INFO,
                  "Listening for connections on port "
                      + config.getListenPort()
                      + " with peerId "
                      + PeerId.fromPubKey(privKey.publicKey()).toBase58());
              return null;
            });

    for (String peer : config.getPeers()) {
      connect(peer);
    }
  }

  /**
   * Convenience method to connect to the given peer at the provided multiaddr.
   *
   * @param peer Peer {@code Multiaddr} to connect to.
   * @return
   */
  @Override
  public CompletableFuture<?> connect(final String peer) {
    return peerManager.connect(new Multiaddr(peer), host.getNetwork());
  }

  @Override
  public void stop() {
    host.stop();
    scheduler.shutdownNow();
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }
}
