package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.Connection;
import io.libp2p.core.PeerId;
import io.libp2p.core.Stream;
import io.libp2p.core.StreamPromise;
import io.libp2p.core.crypto.KEY_TYPE;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.protocol.PingController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.pegasys.artemis.datastructures.operations.AttestationData;
import tech.pegasys.artemis.datastructures.util.DataStructureUtil;
import tech.pegasys.artemis.networking.p2p.jvmlibp2p.JvmLibp2pConfig;
import tech.pegasys.artemis.util.SSZTypes.Bitlist;
import tech.pegasys.artemis.util.bls.BLSKeyPair;
import tech.pegasys.artemis.util.bls.BLSPublicKey;
import tech.pegasys.artemis.util.bls.BLSSignature;
import tech.pegasys.artemis.util.mikuli.KeyPair;
import tech.pegasys.artemis.util.mikuli.PublicKey;
import tech.pegasys.artemis.util.mikuli.Signature;

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

//    Arrays.asList(
//        "/ip4/127.0.0.1/tcp/2457/p2p/" + PeerId.fromPubKey(privKey.publicKey()).toBase58()),

    HandelP2PNetwork p2p1 = new HandelP2PNetwork(jvmLibp2pConfig1, null);
    HandelP2PNetwork p2p2 = new HandelP2PNetwork(jvmLibp2pConfig2, null);

    p2p2.getHost()
        .start().thenAccept(i -> {
      System.out.println("TTTTEEESTING");
    }).get(5, TimeUnit.SECONDS);

    p2p1.getHost().start().thenAccept(ii -> {
      System.out.println("BOOO3333");
    }).get(5, TimeUnit.SECONDS);

    StreamPromise<PingController> ping = p2p1.getHost()
        .newStream("/ipfs/ping/1.0.0", p2p2.getHost().getPeerId(),
            new Multiaddr("/ip4/127.0.0.1/tcp/2457"));
    Stream stream = ping.getStream().get(5, TimeUnit.SECONDS);
    PingController o = ping.getControler().get(5, TimeUnit.SECONDS);
    // <PingController>
    Assertions.assertNotNull(stream);

//    stream.closeFuture();
//    o.ping().handle((i,e)-> { System.out.println("Closing ping"); return i; }).get(5, TimeUnit.SECONDS);

    System.out.println("ping stream:" + stream);

    // node keys
//    Pair<PrivKey, PubKey> aliceIdentityKey = generateKeyPair(KEY_TYPE.ECDSA);
//    Pair<PrivKey, PubKey> bobIdentityKey = generateKeyPair(KEY_TYPE.ECDSA);
//
//    NoiseXXSecureChannel ch1 = new NoiseXXSecureChannel(aliceIdentityKey.component1());
//    NoiseXXSecureChannel ch2 = new NoiseXXSecureChannel(bobIdentityKey.component1());
//
//    ProtocolSelect protocolSelect1 = new ProtocolSelect(Arrays.asList(ch1));
//    ProtocolSelect protocolSelect2 = new ProtocolSelect(Arrays.asList(ch2));
//
//    TestChannel eCh1 = new TestChannel("#1", true, new LoggingHandler("#1", LogLevel.ERROR),
//        Negotiator.INSTANCE.createRequesterInitializer(NoiseXXSecureChannel.announce),
//        protocolSelect1);
//
//    TestChannel eCh2 = new TestChannel("#2", false,
//        new LoggingHandler("#2", LogLevel.ERROR),
//        Negotiator.INSTANCE.createResponderInitializer(
//            Arrays.asList(new ProtocolMatcher(Mode.STRICT, NoiseXXSecureChannel.announce, null))),
//        protocolSelect2);
//
//    String s = "/ip4/127.0.0.1/tcp/2457/p2p/" + PeerId.fromPubKey(privKey2.publicKey()).toBase58();
//    Object oo = p2p1.connect(s).get(15, TimeUnit.SECONDS);
//    CopyOnWriteArrayList<Connection> connections = p2p1.getHost().getNetwork().getConnections();
//
//    System.out.println("connections:"+connections.size());

//    System.out.println("o:"+o);

    Assertions.assertNotNull(p2p1, "Handel P2P network 1 not expected to be null.");
    Assertions.assertNotNull(p2p2, "Handel P2P network 2 not expected to be null.");

//    Assertions.assertTrue(futureConnect.get(5, TimeUnit.SECONDS), "Handel could not connect to its peer.");

  }

  @Test
  public void testHandel50Nodes()
      throws InterruptedException, ExecutionException, TimeoutException {

    Logger testLogger = LogManager.getLogger("testHandel50Nodes");
    Configurator.setLevel("testHandel50Nodes", Level.DEBUG);
    int startPort = 2455;

    int numNodes = 8;

    // prepare node configurations
    ArrayList<HandelP2PNetwork> nodes = new ArrayList<>();
    for (int i = 0; i < numNodes; i++) {
      int port = startPort + i;
      JvmLibp2pConfig jvmLibp2pConfig1 = new JvmLibp2pConfig(
          Optional.of(KeyKt.generateKeyPair(KEY_TYPE.SECP256K1).component1()),
          "127.0.0.1",
          port,
          port,
          Collections.emptyList(),
          true,
          true,
          true);
      HandelP2PNetwork p2p1 = new HandelP2PNetwork(jvmLibp2pConfig1, null);
      nodes.add(p2p1);
    }

    // isolate and start host node
    HandelP2PNetwork handelServer = nodes.get(0);
    nodes.remove(0);
    handelServer.getHost()
        .start().thenAccept(i -> {
      testLogger.debug("Node 0 (host node) started on port " + startPort);
    }).get(45, TimeUnit.SECONDS);

//    Executor executor = Executors.newFixedThreadPool(10);

    // start peer nodes
    List<CompletableFuture<?>> collect = nodes.stream().map(
        n -> {
          testLogger.debug("Attempting to start node on port " + n.getConfig().getListenPort());
          return n.getHost().start().whenComplete((a, b) -> {
            testLogger.debug(n.getConfig().getListenPort() + " started.");
          });
        }
    ).collect(Collectors.toList());

    CompletableFuture<Void> voidCompletableFuture = CompletableFuture
        .allOf(collect.toArray(new CompletableFuture[0]));

    voidCompletableFuture.join();

    String peer =
        "/ip4/127.0.0.1/tcp/" + handelServer.getConfig().getListenPort() + "/p2p/"
            + handelServer.getHost().getPeerId();

    java.util.stream.Stream<CompletableFuture<Connection>> completableFutureStream = nodes.stream()
        .parallel().map(n -> {
          testLogger.debug("Connecting from:" + n.getConfig().getListenPort());
          return n
              .getHost().getNetwork().connect(handelServer.getHost().getPeerId(),
                  new Multiaddr("/ip4/127.0.0.1/tcp/" + handelServer.getConfig().getListenPort()))
              //          .connect(peer)
              .thenApply(fff -> {
                testLogger.debug("Connected successfully:" + fff);
                return fff;
              });

//            .thenApply(it -> it.getMuxerSession().createStream(
//            Multistream.create(new Ping()).toStreamHandler()))

        });

    CompletableFuture.allOf(
        completableFutureStream.collect(Collectors.toList()).toArray(new CompletableFuture[0]))
        .join();

    testLogger.debug("Completed connections");

    // create a committee based on peers
    HandelCommittee hc = new HandelCommittee(
        handelServer.getConfig().getPrivateKey().get().publicKey(),
        Bytes32.ZERO, nodes.stream().map(n -> {
      // TODO: change HP to be an interface with an implementation that wraps current libp2p abstraction
      return new HandelParticipant(nodes.indexOf(n),
          n.getConfig().getPrivateKey().get().publicKey());
    }).collect(Collectors.toList()));

    Assertions.assertTrue(hc.getParticipants().size() > 0);

    HandelSummary hs = new HandelSummary(); // summary of current aggregation state

    // assume that there is a single common attestation data that all peers want to certify
    AttestationData attestationData = DataStructureUtil.randomAttestationData();

    // prepare initial state of all nodes
    testLogger.debug("Setting up initial committee participants");
    hc.getParticipants().forEach(p -> {
      KeyPair randomKeyPair = KeyPair.random();
      BLSKeyPair kkp = new BLSKeyPair(randomKeyPair);

      // get current participant's aggregation state
      // currently creating it anew, signing zero block
      HandelAggregation ha = new HandelAggregation();
      ha.setHash(Bytes32.ZERO);
      ha.setSigners(new Bitlist(hc.getParticipants().size(), hc.getParticipants().size()));
      ha.getSigners().setBit(hc.getSortedParticipantKeys().indexOf(p.getKey())); // set our own bit
      ha.setSignature(BLSSignature.sign(kkp, Bytes32.ZERO.reverse(), Bytes32.ZERO.reverse()));
      ha.setPubkey(kkp.getPublicKey());
    });

    testLogger.debug("Activate each participant");
    hc.getParticipants().forEach(p -> {

      Map<Bytes32, HandelAggregation> mapHashToAggr = p.getMapHashToAggr();

//      Attestation attestation = new Attestation(Bytes32.ZERO, attestationData, )
      // tuweni-specific stuff, stuff that typically goes into Bocu's BLS types
//      org.apache.tuweni.crypto.mikuli.KeyPair kp2 = org.apache.tuweni.crypto.mikuli.KeyPair
//          .random();
//      SignatureAndPublicKey attestationSignatureAndPubkey = BLS12381
//          .sign(kp2, attestationData.hash_tree_root().toArray(), 0);

      // handling incoming
      p.getIncomingAggregations().forEach(incomingAggr -> {
        // set initial value to first encounter
        if (!mapHashToAggr.containsKey(incomingAggr.getHash())) {
          mapHashToAggr.put(incomingAggr.getHash(), incomingAggr);
          return;
        }

        // check that we should aggregate this signature

        // signataure bitlists don't have much functionality i.e., there isn't an xor op
        // so it's easiest to just iterate over bitlist and use individual participant's attestation signature

        PublicKey aggrKey = mapHashToAggr.get(incomingAggr.getHash()).getPubkey().getPublicKey();
        Bitlist aggrSigners = mapHashToAggr.get(incomingAggr.getHash()).getSigners().copy();
        BLSSignature combine = mapHashToAggr.get(incomingAggr.getHash()).getSignature();

        for (int i = 0; i < incomingAggr.getSigners().getMaxSize(); i++) {
          if (i == hc.getSortedParticipantKeys().indexOf(p.getKey())) {
            testLogger.debug("Skiping our own bit:" + i);
            // skip our own bit
            continue;
          }
          aggrSigners.setBit(i);
          combine = new BLSSignature(incomingAggr.getSignature().getSignature()
              .combine(combine.getSignature()));
          aggrKey = PublicKey
              .aggregate(Arrays.asList(aggrKey, incomingAggr.getPubkey().getPublicKey()));
        }

        HandelAggregation ha = mapHashToAggr.get(incomingAggr.getHash());
        ha.setHash(incomingAggr.getHash());
        ha.setSigners(aggrSigners);
        ha.setPubkey(new BLSPublicKey(aggrKey));
        ha.setSignature(combine);

      });

    });


    testLogger.debug("Completed activation of nodes");

    // forward aggregated data every 20 ms
    Executor executor = CompletableFuture.delayedExecutor(20, TimeUnit.MILLISECONDS);

  }

  @Test
  public void testHandelP2PPingBackAndForth()
      throws InterruptedException, ExecutionException, TimeoutException {

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

//    Arrays.asList(
//        "/ip4/127.0.0.1/tcp/2457/p2p/" + PeerId.fromPubKey(privKey.publicKey()).toBase58()),

    HandelP2PNetwork p2p1 = new HandelP2PNetwork(jvmLibp2pConfig1, null);
    HandelP2PNetwork p2p2 = new HandelP2PNetwork(jvmLibp2pConfig2, null);

    p2p2.getHost()
        .start().thenAccept(i -> {
      System.out.println("TTTTEEESTING");
    }).get(5, TimeUnit.SECONDS);

    p2p1.getHost().start().thenAccept(ii -> {
      System.out.println("BOOO3333");
      String s =
          "/ip4/127.0.0.1/tcp/2457/p2p/" + PeerId.fromPubKey(privKey2.publicKey()).toBase58();
      try {
        Object oo = p2p1.getHost().getNetwork()
            .connect(p2p2.getHost().getPeerId(), new Multiaddr("/ip4/127.0.0.1/tcp/2457"))
            .get(15, TimeUnit.SECONDS);
        System.out.println("p2p1:" + p2p1.getHost().getNetwork().getConnections().size());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (TimeoutException e) {
        e.printStackTrace();
      }
    }).get(5, TimeUnit.SECONDS);

    StreamPromise<PingController> ping = p2p1.getHost()
        .newStream("/ipfs/ping/1.0.0", p2p2.getHost().getPeerId(),
            new Multiaddr("/ip4/127.0.0.1/tcp/2457"));
    Stream stream = ping.getStream().get(5, TimeUnit.SECONDS);
//    PingController o = ping.getControler().get(5, TimeUnit.SECONDS);
    // <PingController>

    stream.closeFuture();

    StreamPromise<PingController> ping2 = p2p2.getHost()
        .newStream("/ipfs/ping/1.0.0", p2p1.getHost().getPeerId(),
            new Multiaddr("/ip4/127.0.0.1/tcp/2456"));
    Stream stream2 = ping2.getStream().get(5, TimeUnit.SECONDS);
//    PingController o2 = ping2.getControler().get(5, TimeUnit.SECONDS);

    Assertions.assertNotNull(stream);
    Assertions.assertNotNull(stream2);
  }

}
