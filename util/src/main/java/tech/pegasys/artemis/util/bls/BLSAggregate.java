/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.artemis.util.bls;

import java.util.List;
import java.util.stream.Collectors;
import tech.pegasys.artemis.util.mikuli.Signature;

public class BLSAggregate {

  /**
   * The bls_aggregate_pubkeys() function as defined in the Eth2 specification
   *
   * @param pubKeys the list of compressed public keys
   * @return the aggregated public key
   */
  public static BLSPublicKey bls_aggregate_pubkeys(List<BLSPublicKey> pubKeys) {
    return BLSPublicKey.aggregate(pubKeys);
  }

  /**
   * The bls_aggregate_signatures() function as defined in the Eth2 specification
   *
   * @param signatures the list of signature objects
   * @return the aggregated signature
   */
  public static BLSSignature bls_aggregate_signatures(List<BLSSignature> signatures) {
    try {
      return aggregate(signatures);
    } catch (RuntimeException e) {
      // TODO: once we stop using random (unseeded signatures) keypairs,
      // then the signatures will be predictable and the resulting state can be precomputed
      return BLSSignature.random();
    }
  }

  /**
   * Aggregates a list of signatures into a single signature using BLS magic.
   *
   * @param signatures the list of signatures to be aggregated
   * @return the aggregated signature
   */
  public static BLSSignature aggregate(List<BLSSignature> signatures) {
    List<Signature> signatureObjects =
        signatures.stream().map(BLSSignature::getSignature).collect(Collectors.toList());
    return new BLSSignature(Signature.aggregate(signatureObjects));
  }
}
