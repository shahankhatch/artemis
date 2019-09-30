package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.crypto.PubKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.artemis.datastructures.operations.Attestation;
import tech.pegasys.artemis.datastructures.operations.AttestationData;
import tech.pegasys.artemis.datastructures.util.DataStructureUtil;
import tech.pegasys.artemis.networking.handel.HandelAttestationFactory.Builder;
import tech.pegasys.artemis.networking.handel.HandelAttestationFactory.BuiltConstructorClosure;
import tech.pegasys.artemis.util.SSZTypes.Bitlist;
import tech.pegasys.artemis.util.bls.BLSSignature;

public class HandelCommittee {

  private PubKey myKey;
  private Bytes32 seed;

  private List<HandelParticipant> participants; // this is probably better as a map
  private List<PubKey> allParticipants; // the elements will be sorted by PrivKey

  private Map<PubKey, List<PubKey>> PV;

  public PubKey getMyKey() {
    return myKey;
  }

  public Bytes32 getSeed() {
    return seed;
  }

  public List<HandelParticipant> getParticipants() {
    return participants;
  }

  public List<PubKey> getAllParticipants() {
    return allParticipants;
  }

  public Map<PubKey, List<PubKey>> getPV() {
    return PV;
  }

  private void determineAllVPs(Bytes32 seed) {
    PV = new HashMap<>();
    allParticipants.stream().forEach(p -> {
      PV.compute(p, (k, v) -> {
        List<PubKey> ret = determineVP(allParticipants.indexOf(p), seed);
        return ret;
      });
    });
  }

  /**
   * Compute PVs of node at index {@code index} which excludes the element at the given index (a
   * node cannot be in a priority list for itself).
   *
   * @param index The element index of participants.
   * @param seed  The random seed by which to decide PVs (currently unused).
   * @return The ordered list of PVs for the node at {@code index}.
   */
  private List<PubKey> determineVP(int index, Bytes32 seed) {
    List<PubKey> parts = new ArrayList(allParticipants);
    parts.remove(allParticipants.get(index)); // remove node from its own VP
    Map<Bytes32, PubKey> intermediate = new HashMap<>();
    parts.forEach(p -> {
      // one way for deterministic sort is to XOR seed w/ pubkey first
      Bytes32 k = seed.xor(Bytes32.wrap(Arrays.copyOf(p.bytes(), 32)));
      intermediate.put(k, p);
    });
    List<Bytes32> keySet = new ArrayList<>(intermediate.keySet());
    keySet.sort((c, d) -> {
      return compare(c.toArray(), d.toArray());
    });
    List<PubKey> ret = keySet.stream().map(b -> {
      return intermediate.get(b);
    }).collect(Collectors.toList());
    return ret;
  }

  public HandelCommittee(PubKey myKey, Bytes32 seed,
      List<HandelParticipant> participants) {
    this.myKey = myKey;
    this.seed = seed;
    this.participants = new ArrayList<>();
    this.participants.addAll(participants);

    orderParticipants(myKey, this.participants);
    determineAllVPs(seed);
  }

  private void orderParticipants(PubKey myKey, List<HandelParticipant> participants) {
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
   * Get partition of PubKeys at the given {@code level} in reference to a node at position {@code
   * index} at level 0. The current implementation is not symmetric across nodes at the same level.
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
    if (index > 0) {
      Collections.rotate(allParticipants, -index);
    }

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

  BlockingQueue<Attestation> unprocessed = new LinkedBlockingQueue<>();

  // receive a contribution at a given height
  // also requires index of sender (for looking up their pubkey)
  public void attestation(long height, Attestation attestation) {
    Bitlist bl = new Bitlist(allParticipants.size(), allParticipants.size());
    bl.setBit(allParticipants.indexOf(myKey));

    HandelAggregation agg = new HandelAggregation(attestation.hash_tree_root(),
        attestation.getAggregate_signature(), bl);
//    unprocessed.add(agg);
  }


}
