package tech.pegasys.artemis.statetransition.util;

import static tech.pegasys.artemis.statetransition.util.BlockProcessorUtil.process_attestations;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.UnsignedLong;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.artemis.datastructures.operations.Attestation;
import tech.pegasys.artemis.datastructures.state.BeaconState;
import tech.pegasys.artemis.datastructures.state.BeaconStateWithCache;
import tech.pegasys.artemis.datastructures.state.PendingAttestation;
import tech.pegasys.artemis.datastructures.util.AttestationUtil;
import tech.pegasys.artemis.datastructures.util.BeaconStateUtil;
import tech.pegasys.artemis.datastructures.util.DataStructureUtil;
import tech.pegasys.artemis.datastructures.util.SimpleOffsetSerializer;

public class FuzzUtil {

  public void sayHello(String h) {
    System.out.println("hi:" + h + ":");
  }

  public static void main(String[] args) {
    final FuzzUtil h = new FuzzUtil();
    h.sayHello("here");
    try {
      h.doblock();
    } catch (BlockProcessingException e) {
//      System.out.println("error");
//      System.err.println("e:" + e.getMessage());
    }
    h.doblock2();
    boolean b = FuzzUtil.testAttestation(new byte[0], new byte[0]);
    System.out.println("bool:" + b);
  }

  private void doblock() throws BlockProcessingException {
    final BeaconState beaconState = new BeaconState();
    final Attestation attestation = DataStructureUtil.randomAttestation(0);

    try {
      BeaconState beaconState1 = testAttestation(beaconState, attestation);
      System.out.println("beaconstate1:" + beaconState1.getSlot());
    } catch (Exception e) {
      System.err.println("doblock:");
    }
    attestation.setData(attestation.getData().withIndex(UnsignedLong.ZERO));
//    PendingAttestation pendingAttestation = DataStructureUtil.randomPendingAttestation(0);

    final ArrayList<Attestation> attestations = new ArrayList<>();
    attestations.add(attestation);

    System.out.println("b_before:" + beaconState.getSlot());
    process_attestations(beaconState, attestations);
    System.out.println("b_after:" + beaconState.getSlot());
  }

  private void doblock2() {
    final Bytes inData = Bytes.wrap(new byte[0]);
    try {
      final BeaconStateWithCache state =
          BeaconStateWithCache.fromBeaconState(
              SimpleOffsetSerializer.deserialize(inData, BeaconState.class));
      System.out.println("statet:" + state.getSlot());
      final BeaconState result = null;
      SimpleOffsetSerializer.serialize(result).toArrayUnsafe();
    } catch (Exception e) {
      System.err.println("state error");
    }
  }

  public static boolean testAttestation(final byte[] beaconstate, final byte[] attestation) {
    byte[] ret = testAttestationRetState(beaconstate, attestation);
    System.out.println("retsize:" + ret.length);
    return true;
  }

  public static byte[] testAttestationRetState(final byte[] beaconstate, final byte[] attestation) {
    final Bytes inData = Bytes.wrap(beaconstate);
    try {
      final BeaconStateWithCache state =
          BeaconStateWithCache.fromBeaconState(
              SimpleOffsetSerializer.deserialize(inData, BeaconState.class));
      System.out.println("testAttestationRetState_slot:" + state.getSlot());
      final BeaconState result = null;
      SimpleOffsetSerializer.serialize(result).toArrayUnsafe();
    } catch (Exception e) {
      System.err.println("testAttestationRetState error");
    }

    return new byte[0];
  }

  public static BeaconState testAttestation(BeaconState beaconstate, Attestation attestation) {
    return new BeaconState();
  }


}
