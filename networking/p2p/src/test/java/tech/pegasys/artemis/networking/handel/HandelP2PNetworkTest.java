package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.Connection;
import io.libp2p.core.PeerId;
import io.libp2p.core.Stream;
import io.libp2p.core.StreamPromise;
import io.libp2p.core.crypto.KEY_TYPE;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.etc.AttributesKt;
import io.libp2p.protocol.PingController;
import io.libp2p.tools.TestChannel;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import tech.pegasys.artemis.util.bls.BLSVerify;
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

//    Executor executor = Executors.newFixedThreadPool(10);

    // define startup of peer nodes
    List<CompletableFuture<?>> collect = nodes.stream().map(
        n -> {
          testLogger.debug("Attempting to start node on port " + n.getConfig().getListenPort());
          return n.getHost().start().whenComplete((a, b) -> {
            testLogger.debug(n.getConfig().getListenPort() + " started.");
          });
        }
    ).collect(Collectors.toList());

    // execute all the startup futures
    CompletableFuture.allOf(collect.toArray(new CompletableFuture[0])).join();

    // define a single node as the central server
    String centralServer =
        "/ip4/127.0.0.1/tcp/" + handelServer.getConfig().getListenPort() + "/p2p/"
            + handelServer.getHost().getPeerId();

    // define connection of all peers to central server
    List<CompletableFuture<Connection>> completableFutureStream = nodes
        .subList(1, nodes.size()).stream()
        .map(n -> {
          testLogger.debug("Connecting from:" + n.getConfig().getListenPort());
          CompletableFuture<Connection> connectionCompletableFuture = n
              .getHost().getNetwork().connect(handelServer.getHost().getPeerId(),
                  new Multiaddr("/ip4/127.0.0.1/tcp/" + handelServer.getConfig().getListenPort()))
              .thenApply(fff -> {
                n.setConnection(fff);
                testLogger.debug("Connected successfully:" + fff);
                return fff;
              });
          return connectionCompletableFuture;

//            .thenApply(it -> it.getMuxerSession().createStream(
//            Multistream.create(new Ping()).toStreamHandler()))

        }).collect(Collectors.toList());

    // wait for connections to complete
    CompletableFuture.allOf(
        completableFutureStream.toArray(new CompletableFuture[0]))
        .join();

    testLogger.debug("Completed connections");

    // create a committee of peer validators
    // populate the committee with a new HandelParticipant for each peer
    HandelCommittee hc = new HandelCommittee(
        handelServer.getConfig().getPrivateKey().get().publicKey(),
        Bytes32.ZERO, nodes.stream().map(n -> {
      HandelParticipant handelParticipant = new HandelParticipant(
          n.getConfig().getPrivateKey().get().publicKey());
      KeyPair randomKeyPair = KeyPair.random();
      BLSKeyPair kkp = new BLSKeyPair(randomKeyPair);
      handelParticipant.setKp(randomKeyPair);
      handelParticipant.setBlsKeyPair(kkp);
      handelParticipant.setNetwork(n);
      return handelParticipant;
    }).collect(Collectors.toList()));

    Assertions.assertTrue(hc.getParticipants().size() > 0);

    HandelSummary hs = new HandelSummary(); // summary of current aggregation state

    // assume that there is a single common attestation data that all peers want to certify
    AttestationData attestationData = DataStructureUtil.randomAttestationData();

    // get the key by which to sign all outgoing aggregations
    // that is, the connections are initiaded by the peers to the central node
    // and the
    BLSKeyPair serverkp = new BLSKeyPair(hc.getParticipants().get(0).getKp());

    testLogger.debug("Activate each participant");
    hc.getParticipants().subList(1, hc.getParticipants().size()).forEach(p -> {

      // initialize a node's simulated incoming connections
      List<HandelAggregation> incomingAggregations = p.getIncomingAggregations();
      for (int j = 0; j < 10; j++) {
        HandelAggregation ha = new HandelAggregation();
        ha.setHash(Bytes32.ZERO);
        ha.setSigners(new Bitlist(hc.getSortedParticipantKeys().size(),
            hc.getSortedParticipantKeys().size()));
        ha.getSigners()
            .setBit(hc.getSortedParticipantKeys().indexOf(p.getKey())); // set our own bit

        ha.setSignature(
            BLSSignature.sign(serverkp, Bytes32.ZERO.reverse(), Bytes32.ZERO.reverse()));
        ha.setPubkey(serverkp.getPublicKey());
        incomingAggregations.add(ha);
      }

      testLogger.debug("Added simulated incoming aggregtions");
      Assertions.assertEquals(10, incomingAggregations.size(),
          "Incoming aggregations were not properly formed.");

      Map<Bytes32, HandelAggregation> mapHashToAggr = p.getMapHashToAggr();

//      Attestation attestation = new Attestation(Bytes32.ZERO, attestationData, )
      // tuweni-specific stuff, stuff that typically goes into Bocu's BLS types
//      org.apache.tuweni.crypto.mikuli.KeyPair kp2 = org.apache.tuweni.crypto.mikuli.KeyPair
//          .random();
//      SignatureAndPublicKey attestationSignatureAndPubkey = BLS12381
//          .sign(kp2, attestationData.hash_tree_root().toArray(), 0);

      testLogger.debug("Starting to aggregate incoming");

      // handle incoming

      testLogger.debug("Incoming aggregations - presort: " + incomingAggregations.toString());

      // sort aggregations by their BLSPubKey order
      Arrays.sort(incomingAggregations.toArray(new HandelAggregation[0]),
          (Comparator<HandelAggregation>) (ths, other) -> {
            return
                HandelCommittee.compare(
                    HandelCommittee.determineVPByBLSPubKey(hc.getParticipants(), Bytes32.ZERO)
                        .stream().filter(participant -> {
                      return ths.getPubkey().equals(participant.getBlsKeyPair().getPublicKey());
                    }).collect(Collectors.toList()).get(0).getBlsKeyPair().getPublicKey().toBytes()
                        .toArray(),
                    HandelCommittee.determineVPByBLSPubKey(hc.getParticipants(), Bytes32.ZERO)
                        .stream().filter(participant -> {
                      return other.getPubkey().equals(participant.getBlsKeyPair().getPublicKey());
                    }).collect(Collectors.toList()).get(0).getBlsKeyPair().getPublicKey().toBytes()
                        .toArray());
          });

      testLogger.debug("Incoming aggregations - postsort: " + incomingAggregations.toString());

      // move incoming aggregations
      p.setIncomingAggregations(new ArrayList<>());

      incomingAggregations.forEach(incomingAggr -> {
        // set initial value to first encounter
        if (!mapHashToAggr.containsKey(incomingAggr.getHash())) {
          mapHashToAggr.put(incomingAggr.getHash(), incomingAggr);
          return;
        }

        // verify the signature of the incoming aggregation is valid
        boolean aggrVerification = BLSVerify
            .bls_verify(incomingAggr.getPubkey(), incomingAggr.getHash(),
                incomingAggr.getSignature(), Bytes32.ZERO);

        if (!aggrVerification) {
          testLogger.debug("Verification of incoming aggr failed");
        }
        testLogger.debug("Verification of incoming aggr passed");

        // check that we should aggregate this signature

        // signataure bitlists don't have much functionality i.e., there isn't an xor op
        // so it's easiest to just iterate over bitlist and use individual participant's attestation signature

        // get the current aggregation state for this hash
        PublicKey aggrKey = mapHashToAggr.get(incomingAggr.getHash()).getPubkey().getPublicKey();
        Bitlist aggrSigners = mapHashToAggr.get(incomingAggr.getHash()).getSigners().copy();
        BLSSignature combine = mapHashToAggr.get(incomingAggr.getHash()).getSignature();

        // TODO: mark location for optimizations that order signers
        // iterate over signers and add each to the current hash signature if not present
        for (int i = 0; i < incomingAggr.getSigners().getMaxSize(); i++) {
          if (i == HandelCommittee.determineVPByBLSPubKey(hc.getParticipants(), Bytes32.ZERO)
              .indexOf(p)) {
            testLogger.debug("Skipping our own bit:" + i);
            // skip our own bit
            continue;
          }
          // skip signers that are already part of the signature
          if (aggrSigners.getBit(i) != 0) {
            continue;
          }
          // TODO: mark location for optimizations that skip certain signers

          aggrSigners.setBit(i);
          combine = new BLSSignature(incomingAggr.getSignature().getSignature()
              .combine(combine.getSignature()));
          aggrKey = PublicKey
              .aggregate(Arrays.asList(aggrKey, incomingAggr.getPubkey().getPublicKey()));
        }

        // update the current state for that hash
        HandelAggregation ha = mapHashToAggr.get(incomingAggr.getHash());
        ha.setHash(incomingAggr.getHash());
        ha.setSigners(aggrSigners);
        ha.setPubkey(new BLSPublicKey(aggrKey));
        ha.setSignature(combine);

      });
      // completed processing incoming edges

      // now send outgoing
      Connection connection = p.getNetwork().getConnection();
      ChannelFuture channelFuture = connection.getNettyChannel().writeAndFlush(Unpooled.wrappedBuffer("Hello World from 1".getBytes()));
//      channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer("Hello World from 2".getBytes()));
      try {
        channelFuture.get(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (TimeoutException e) {
        e.printStackTrace();
      }

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
