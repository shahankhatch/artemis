package tech.pegasys.artemis.networking.handel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.artemis.datastructures.util.SimpleOffsetSerializer;
import tech.pegasys.artemis.util.SSZTypes.Bitlist;
import tech.pegasys.artemis.util.SSZTypes.SSZContainer;
import tech.pegasys.artemis.util.bls.BLSPublicKey;
import tech.pegasys.artemis.util.bls.BLSSignature;
import tech.pegasys.artemis.util.sos.SimpleOffsetSerializable;

public class HandelAggregation implements SimpleOffsetSerializable, SSZContainer {

  private Bytes32 hash; // the data that has been signed

  private BLSSignature signature; // signature
  private Bitlist signers; // signers

  private BLSPublicKey pubkey; // pubkey corresponding to this aggregation (which is used to verify signature).
  // The pubkey can also be generated from the signers pubkeys which are set in the bitlist.
  // We keep it here for convenience and to avoid recomputation costs.

  public HandelAggregation() {
  }

  public HandelAggregation(Bytes32 hash, BLSSignature signature,
      Bitlist signers, BLSPublicKey pubkey) {
    this.hash = hash;
    this.signature = signature;
    this.signers = signers;
    this.pubkey = pubkey;
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

  @Override
  public int getSSZFieldCount() {
    return 3;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    return Arrays.asList(
        getHash(),
        SimpleOffsetSerializer.serialize(getSignature()),
        getSigners().serialize()
    );
  }

  public BLSPublicKey getPubkey() {
    return pubkey;
  }

  public void setPubkey(BLSPublicKey pubkey) {
    this.pubkey = pubkey;
  }
}
