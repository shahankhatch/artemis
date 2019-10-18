package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.crypto.PubKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.artemis.util.mikuli.KeyPair;

public class HandelParticipant {
  private long ipaddress; // TODO this is to be changed to a String or other struct

  private PubKey key; // validator's identity key
  private KeyPair kp; // keypair for bls signatures

  public List<HandelAggregation> getIncomingAggregations() {
    return incomingAggregations;
  }

  private List<HandelAggregation> incomingAggregations = new ArrayList<>(); // queue of incoming aggregations

  public Map<Bytes32, HandelAggregation> getMapHashToAggr() {
    return mapHashToAggr;
  }

  private Map<Bytes32, HandelAggregation> mapHashToAggr = new HashMap<>();


  public HandelParticipant(long ipaddress, PubKey key) {
    this.ipaddress = ipaddress;
    this.key = key;
  }

  public long getIpaddress() {
    return ipaddress;
  }

  public void setIpaddress(long ipaddress) {
    this.ipaddress = ipaddress;
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
}
