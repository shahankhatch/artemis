package tech.pegasys.artemis.statetransition.util;

import static tech.pegasys.artemis.statetransition.util.BlockProcessorUtil.process_attestations;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.artemis.datastructures.operations.Attestation;
import tech.pegasys.artemis.datastructures.state.BeaconState;
import tech.pegasys.artemis.datastructures.state.BeaconStateWithCache;
import tech.pegasys.artemis.datastructures.util.AttestationUtil;
import tech.pegasys.artemis.datastructures.util.BeaconStateUtil;
import tech.pegasys.artemis.datastructures.util.DataStructureUtil;

public class FuzzUtil {
//  Logger logger = LogManager.getLogger();

  public void sayHello(String h) {
    System.out.println("hi:" + h + ":");
  }

  public static void main(String[] args) {
    FuzzUtil h = new FuzzUtil();
    h.sayHello("here");
    try {
      h.doblock();
    } catch (BlockProcessingException e) {
      System.out.println("error");
      System.err.println("e:" + e.getMessage());
    }
  }

  private void doblock() throws BlockProcessingException {
    BeaconState beaconState = new BeaconState();
    Attestation attestation = DataStructureUtil.randomAttestation(0);
    ArrayList<Attestation> attestations = new ArrayList<>();
    attestations.add(attestation);

    System.out.println("b_before:" + beaconState.getSlot());
    process_attestations(beaconState, attestations);
    System.out.println("b_after:" + beaconState.getSlot());
  }

  public static boolean testAttestation(byte[] in) {
    return true;
  }


}
