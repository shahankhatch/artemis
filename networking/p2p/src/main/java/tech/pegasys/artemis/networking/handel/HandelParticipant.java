package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.crypto.PubKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.artemis.util.bls.BLSKeyPair;
import tech.pegasys.artemis.util.mikuli.KeyPair;

public class HandelParticipant {
  private HandelP2PNetwork network; // network on which this participant is executing - aka eth2

  private PubKey key; // validator's libp2p identity key
  private KeyPair kp; // keypair for bls signatures, mikuli type
  private BLSKeyPair blsKeyPair; // bocu packaged type for keypair

  public List<HandelAggregation> getIncomingAggregations() {
    return incomingAggregations;
  }

  public void setIncomingAggregations(
      List<HandelAggregation> incomingAggregations) {
    this.incomingAggregations = incomingAggregations;
  }

  private List<HandelAggregation> incomingAggregations = new ArrayList<>(); // queue of incoming aggregations

  public Map<Bytes32, HandelAggregation> getMapHashToAggr() {
    return mapHashToAggr;
  }

  private Map<Bytes32, HandelAggregation> mapHashToAggr = new HashMap<>();


  public HandelParticipant(PubKey key) {
    this.key = key;
  }

  public PubKey getKey() {
    return key;
  }

  public void setKey(PubKey key) {
    this.key = key;
  }

  public KeyPair getKp() {
    return kp;
  }

  public void setKp(KeyPair kp) {
    this.kp = kp;
  }

  public BLSKeyPair getBlsKeyPair() {
    return blsKeyPair;
  }

  public void setBlsKeyPair(BLSKeyPair blsKeyPair) {
    this.blsKeyPair = blsKeyPair;
  }

  public HandelP2PNetwork getNetwork() {
    return network;
  }

  public void setNetwork(HandelP2PNetwork network) {
    this.network = network;
  }
}
