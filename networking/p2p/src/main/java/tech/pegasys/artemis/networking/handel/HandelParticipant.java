package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.crypto.PubKey;

public class HandelParticipant {
  private long ipaddress;
  private PubKey key; // this type needs to be validator's (signing) privkey

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
}
