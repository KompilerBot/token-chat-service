package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.ECKey.ECDSASignature;
import static org.ethereum.crypto.HashUtil.sha3;
import java.security.SignatureException;
import org.spongycastle.util.encoders.Hex;


public class AccountKeys {
  @JsonProperty
  private PreKeyStateV2Timestamped payload;

  @JsonProperty
  @NotEmpty
  private String signature;

  public AccountKeys() {}
  public AccountKeys(PreKeyStateV2Timestamped payload, String signature) {
    this.payload = payload;
    this.signature = signature;
  }

  public String getSignature() {
    return signature;
  }

  public PreKeyStateV2Timestamped getPayload() {
    return payload;
  }

  public String getAddress() throws JsonProcessingException {
    String hexAddress = null;

    ObjectMapper mapper = new ObjectMapper();
    String signablePayload = mapper.writeValueAsString(payload);

    byte[] hash = sha3(signablePayload.getBytes());
    byte[] sig = Hex.decode(signature);

    byte[] r = new byte[32];
    System.arraycopy(sig, 0, r, 0, 32);
    byte[] s = new byte[32];
    System.arraycopy(sig, 32, s, 0, 32);
    byte v = (byte) (sig[64] + 0x1b);

    try {
      ECDSASignature signature = ECKey.ECDSASignature.fromComponents(r, s, v);
      if (signature.validateComponents()) {
          byte[] address = ECKey.signatureToAddress(hash, signature);
          hexAddress = new String(Hex.encode(address));
      }
    } catch (SignatureException e) {
    }

    return hexAddress;
  }

}