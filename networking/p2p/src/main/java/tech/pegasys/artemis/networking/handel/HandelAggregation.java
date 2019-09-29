package tech.pegasys.artemis.networking.handel;

import java.util.Objects;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.artemis.util.SSZTypes.Bitlist;
import tech.pegasys.artemis.util.bls.BLSSignature;

public class HandelAggregation {
  private Bytes32 hash;
  private BLSSignature signature;
  private Bitlist signers;

  public HandelAggregation() {}

  public HandelAggregation(Bytes32 hash, BLSSignature signature,
      Bitlist signers) {
    this.hash = hash;
    this.signature = signature;
    this.signers = signers;
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash, signature, signers);
  }

  @Override
  public boolean equals(Object obj) {
    if (Objects.isNull(obj)) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof HandelAggregation)) {
      return false;
    }

    HandelAggregation other = (HandelAggregation) obj;
    return Objects.equals(this.getHash(), other.getHash())
        && Objects.equals(this.getSignature(), other.getSignature())
        && Objects.equals(this.getSigners(), other.getSigners());
  }

  public Bytes32 getHash() {
    return hash;
  }

  public void setHash(Bytes32 hash) {
    this.hash = hash;
  }

  public BLSSignature getSignature() {
    return signature;
  }

  public void setSignature(BLSSignature signature) {
    this.signature = signature;
  }

  public Bitlist getSigners() {
    return signers;
  }

  public void setSigners(Bitlist signers) {
    this.signers = signers;
  }
}
