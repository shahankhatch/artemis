package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.Connection;
import io.libp2p.core.PeerId;
import io.libp2p.core.Stream;
import io.libp2p.core.StreamPromise;
import io.libp2p.core.crypto.KEY_TYPE;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.crypto.PubKey;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.protocol.PingController;
import io.libp2p.security.noise.NoiseXXSecureChannel;
import io.libp2p.tools.TestChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import kotlin.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.pegasys.artemis.networking.p2p.jvmlibp2p.JvmLibp2pConfig;


import com.google.protobuf.ByteString;
import com.southernstorm.noise.protocol.HandshakeState;
import com.southernstorm.noise.protocol.Noise;
import io.libp2p.core.crypto.KEY_TYPE;
import static  io.libp2p.core.crypto.KeyKt.generateKeyPair;
import io.libp2p.core.multistream.Mode;
import io.libp2p.core.multistream.ProtocolMatcher;
import io.libp2p.multistream.Negotiator;
import io.libp2p.multistream.ProtocolSelect;
import static io.libp2p.tools.TestChannel.Companion.*;
import io.libp2p.tools.TestHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import spipe.pb.Spipe;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HandelP2PNetworkTest {

  @Test
  public void testHandelNetwork() {
    PrivKey privKey = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1).component1();

    JvmLibp2pConfig jvmLibp2pConfig = new JvmLibp2pConfig(
        Optional.of(privKey),
        "127.0.0.1",
        2456,
        2456,
        null,
        true,
        true,
        true);

    HandelP2PNetwork p2p = new HandelP2PNetwork(jvmLibp2pConfig, null);

    Assertions.assertNotNull(p2p, "Handel P2P network not expected to be null.");

  }

  @Test
  public void testHandelNetworkStartStop() throws ExecutionException, InterruptedException {
    PrivKey privKey = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1).component1();

    JvmLibp2pConfig jvmLibp2pConfig = new JvmLibp2pConfig(
        Optional.of(privKey),
        "127.0.0.1",
        2456,
        2456,
        Collections.emptyList(),
        true,
        true,
        true);

    HandelP2PNetwork p2p = new HandelP2PNetwork(jvmLibp2pConfig, null);

    Assertions.assertNotNull(p2p, "Handel P2P network not expected to be null.");

    // note: libp2p currently doesn't return messages as about start/stop
    // we're assuming that a successful future means the task completed w success

    CompletableFuture<Boolean> futureValueStart = p2p.getHost()
        .start().thenApply(i -> {
          return true;
        });
    Assertions.assertTrue(futureValueStart.get(), "Handel p2p network didn't start ok.");

    CompletableFuture<Boolean> futureValueStop = p2p.getHost()
        .stop().thenApply(i -> {
          return true;
        });
    Assertions.assertTrue(futureValueStop.get(), "Handel p2p network didn't stop ok.");
  }

  @Test
  public void testTwoHandelNetworks()
      throws ExecutionException, InterruptedException, TimeoutException {

    PrivKey privKey1 = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1).component1();
    PrivKey privKey2 = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1).component1();

    JvmLibp2pConfig jvmLibp2pConfig1 = new JvmLibp2pConfig(
        Optional.of(privKey1),
        "127.0.0.1",
        2456,
        2456,
        Collections.emptyList(),
        true,
        true,
        true);

    JvmLibp2pConfig jvmLibp2pConfig2 = new JvmLibp2pConfig(
        Optional.of(privKey2),
        "127.0.0.1",
        2457,
        2457,
        Collections.emptyList(),
        true,
        true,
        true);

    String s = "/ip4/127.0.0.1/tcp/2457/p2p/" + PeerId.fromPubKey(privKey2.publicKey()).toBase58();

//    Arrays.asList(
//        "/ip4/127.0.0.1/tcp/2457/p2p/" + PeerId.fromPubKey(privKey.publicKey()).toBase58()),

    HandelP2PNetwork p2p1 = new HandelP2PNetwork(jvmLibp2pConfig1, null);
    HandelP2PNetwork p2p2 = new HandelP2PNetwork(jvmLibp2pConfig2, null);

    p2p2.getHost()
        .start().thenAccept(i -> {
      System.out.println("TTTTEEESTING");
    }).get();

    p2p1.getHost().start().thenAccept(ii -> {
      System.out.println("BOOO3333");
    }).get();

    StreamPromise<Object> ping = p2p1.getHost()
        .newStream("/ipfs/ping/1.0.0", p2p2.getHost().getPeerId(),
            new Multiaddr("/ip4/127.0.0.1/tcp/2457"));
    Stream stream = ping.getStream().get();
    // <PingController>
    Assertions.assertNotNull(stream);

    System.out.println("ping stream:"+stream);


    // node keys
    Pair<PrivKey, PubKey> privKeyAlicePeer = generateKeyPair(KEY_TYPE.ECDSA);
    Pair<PrivKey, PubKey> privKeyBobPeer = generateKeyPair(KEY_TYPE.ECDSA);

//        val privateKey25519Alice = ByteArray(32)
//        Noise.random(privateKey25519Alice)
//        val privateKey25519Bob = ByteArray(32)
//        Noise.random(privateKey25519Bob)

    // noise keys
//        val ch1 = NoiseXXSecureChannel(privKeyAlicePeer, privateKey25519Alice)
//        val ch2 = NoiseXXSecureChannel(privKeyBobPeer, privateKey25519Bob)
    NoiseXXSecureChannel ch1 = new NoiseXXSecureChannel(privKeyAlicePeer.component1());
    NoiseXXSecureChannel ch2 = new NoiseXXSecureChannel(privKeyBobPeer.component1());

    ProtocolSelect protocolSelect1 = new ProtocolSelect(Arrays.asList(ch1));
    ProtocolSelect protocolSelect2 = new ProtocolSelect(Arrays.asList(ch2));

    TestChannel eCh1 = new TestChannel("#1", true, new LoggingHandler("#1", LogLevel.ERROR),
        Negotiator.INSTANCE.createRequesterInitializer(NoiseXXSecureChannel.announce),
        protocolSelect1);

    TestChannel eCh2 = new TestChannel("#2", false,
        new LoggingHandler("#2", LogLevel.ERROR),
        Negotiator.INSTANCE.createResponderInitializer(
            Arrays.asList(new ProtocolMatcher(Mode.STRICT, NoiseXXSecureChannel.announce, null))),
        protocolSelect2);

    Object o = p2p1.connect(s).get();
    CopyOnWriteArrayList<Connection> connections = p2p1.getHost().getNetwork().getConnections();

    System.out.println("connections:"+connections.size());

    System.out.println("o:"+o);

    Assertions.assertNotNull(p2p1, "Handel P2P network 1 not expected to be null.");
    Assertions.assertNotNull(p2p2, "Handel P2P network 2 not expected to be null.");

//    Assertions.assertTrue(futureConnect.get(5, TimeUnit.SECONDS), "Handel could not connect to its peer.");

  }

}
