package tech.pegasys.artemis.networking.handel;

import io.libp2p.core.crypto.KEY_TYPE;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.crypto.PubKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kotlin.Pair;
import org.apache.tuweni.bytes.Bytes32;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.pegasys.artemis.datastructures.operations.Attestation;
import tech.pegasys.artemis.datastructures.operations.AttestationData;
import tech.pegasys.artemis.datastructures.util.DataStructureUtil;
import tech.pegasys.artemis.networking.handel.HandelAttestationFactory.Builder;
import tech.pegasys.artemis.networking.handel.HandelAttestationFactory.BuiltConstructorClosure;
import tech.pegasys.artemis.util.SSZTypes.Bitlist;
import tech.pegasys.artemis.util.bls.BLSSignature;

public class HandelCommitteeTest {

  @Test
  public void testCommitteePartitions() {
    HandelCommittee c = createHandelCommittee(10);

    Assertions
        .assertEquals(1, c.getPartition(1).size(), "Partition size for Level 1 is not correct.");
    Assertions
        .assertEquals(2, c.getPartition(2).size(), "Partition size for Level 2 is not correct.");
    Assertions
        .assertEquals(4, c.getPartition(3).size(), "Partition size for Level 3 is not correct.");
    Assertions
        .assertEquals(8, c.getPartition(4).size(), "Partition size for Level 4 is not correct.");
    List<PubKey> l5 = c.getPartition(5);
    Assertions
        .assertEquals(9, l5.size(), "Partition size for Level 5 is not correct.");
    Assertions.assertFalse(l5.contains(c.getMyKey()), "Partition should not contain self");
  }

  @Test
  public void testCommitteePVs() {
    int numParticipants = 10;
    List<HandelParticipant> participants = new ArrayList<>();
    for (int i = 0; i < numParticipants; i++) {
      Pair<PrivKey, PubKey> pair = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1);
      participants.add(new HandelParticipant(pair.component2()));
    }
    Pair<PrivKey, PubKey> mykey = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1);

    HandelCommittee handelCommittee = new HandelCommittee(mykey.component2(), Bytes32.random(),
        Collections.unmodifiableList(participants));

    Collections.shuffle(participants); // strangely, the order of generated participants are sorted, hence a shuffle
    // TODO: incorporate the seed into the identifier
    Assertions.assertFalse(Arrays.equals(participants.toArray(new HandelParticipant[0]),
        handelCommittee.getParticipants().toArray(new HandelParticipant[0])),
    "The validator list is in same order as the resulting node order.");
  }

  @NotNull
  private HandelCommittee createHandelCommittee(int numParticipants) {
    List<HandelParticipant> participants = new ArrayList<>();
    for (int i = 0; i < numParticipants; i++) {
      Pair<PrivKey, PubKey> pair = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1);
      participants.add(new HandelParticipant(pair.component2()));
    }
    Pair<PrivKey, PubKey> pair = KeyKt.generateKeyPair(KEY_TYPE.SECP256K1);

    return new HandelCommittee(pair.component2(), Bytes32.random(), participants);
  }

  @Test
  public void testCreateAttestations() {

    Attestation attestation = new Attestation(
        DataStructureUtil.randomBitlist(), DataStructureUtil.randomAttestationData(),
        DataStructureUtil.randomBitlist(), BLSSignature.random());

    Attestation attestation1 = HandelAttestationFactory.createAttestation(Attestation.class,
        new Class[]{Bitlist.class, AttestationData.class, Bitlist.class, BLSSignature.class},
        new Object[]{DataStructureUtil.randomBitlist(), DataStructureUtil.randomAttestationData(),
            DataStructureUtil.randomBitlist(), BLSSignature.random()});

    BuiltConstructorClosure<Attestation> attestationBuiltConstructorClosure = HandelAttestationFactory
        .buildConstructorClosure(Attestation.class,
            new Class[]{Bitlist.class, AttestationData.class, Bitlist.class, BLSSignature.class},
            new Builder[]{(Builder<Bitlist>) () -> DataStructureUtil.randomBitlist(),
                (Builder<AttestationData>) () -> DataStructureUtil.randomAttestationData(),
                (Builder<Bitlist>) () -> DataStructureUtil.randomBitlist(), null});

    Attestation attestation2 = attestationBuiltConstructorClosure
        .build(new Class[]{BLSSignature.class},
            new Builder[]{BLSSignature::random});

    Assertions.assertNotNull(attestation, "Did not expect null attestation from constructor.");
    Assertions.assertNotNull(attestation1, "Did not expect null attestation from builder.");
    Assertions.assertNotNull(attestation2, "Did not expect null attestation from dynamic builder.");
  }

}
