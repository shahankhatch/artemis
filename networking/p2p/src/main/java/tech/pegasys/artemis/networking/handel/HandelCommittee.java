package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.crypto.PubKey;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.artemis.datastructures.operations.Attestation;
import tech.pegasys.artemis.util.SSZTypes.Bitlist;

public class HandelCommittee {

  private PubKey myKey;
  private Bytes32 seed;

  private List<HandelParticipant> participants; // this is probably better as a map

  private List<PubKey> allParticipants; // the elements will be sorted by PrivKey, will be made robust via seed later

  public HandelCommittee(PubKey myKey, Bytes32 seed,
      List<HandelParticipant> participants) {
    this.myKey = myKey;
    this.seed = seed;
    this.participants = participants;

    allParticipants = participants.stream().map(e -> e.getKey()).collect(Collectors.toList());
    allParticipants.add(myKey);
    allParticipants.sort((c, d) -> {
      return compare(c.bytes(), d.bytes());
    });
  }

  // get partition for us
  public List<PubKey> getPartition(int level) {
    Collections.rotate(allParticipants, -allParticipants.indexOf(myKey));
    return getPartition(0, level);
  }

  /**
   Get partition of PubKeys at the given {@code level} in reference to a node at position {@code index} at level 0.
   */
  public List<PubKey> getPartition(int index, int level) {
    // check level is valid
    if (level == 0) {
      throw new IndexOutOfBoundsException("No partition for level 0 exists (it is us).");
    }
    // check index is in range
    if (index >= allParticipants.size()) {
      throw new IndexOutOfBoundsException("Cannot find partition for index:" + index);
    }
    if (index > 0) Collections.rotate(allParticipants, -index);

    // simply find index range, which is 2^(level-1) since it's a binary tree
    int rangeStart = (int) Math.pow(2, level - 1);
    int rangeEnd = (int) Math.pow(2, level);
    if (rangeStart >= allParticipants.size()) {
      throw new IndexOutOfBoundsException("No partition exists for level:" + level);
    }
    List<PubKey> ret;
    if (rangeEnd < allParticipants.size()) {
      ret = allParticipants.subList(rangeStart, rangeEnd);
    } else {
      ret = allParticipants.subList(rangeStart, allParticipants.size() - 1);
    }
    if (ret.size() == 0) {
      throw new IndexOutOfBoundsException(
          "Error finding partition for index " + index + " and level " + level);
    }
    return ret;
  }

  public int compare(byte[] left, byte[] right) {
    for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
      int a = (left[i] & 0xff);
      int b = (right[j] & 0xff);
      if (a != b) {
        return a - b;
      }
    }
    return left.length - right.length;
  }

  // prepare an individual contribution to be disseminated to the overlay
  public void attestation(long height, Attestation attestation) {
    Bitlist bl = new Bitlist(allParticipants.size(), allParticipants.size());
    bl.setBit(allParticipants.indexOf(myKey));

    HandelAggregation agg = new HandelAggregation(attestation.hash_tree_root(),
        attestation.getAggregate_signature(), bl);
  }

}
