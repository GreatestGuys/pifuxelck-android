package com.everythingissauce.pifuxelck.auth;

import com.everythingissauce.pifuxelck.Base64Util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.KeyPairGenerator;

/**
 * A representation of the user's identity.
 */
public class Identity {

  private static final int KEY_SIZE = 2048;

  /**
   * A partially constructed identity. This class represents an unvalidated
   * identity that does not have a user ID assigned from the backend.
   */
  public static class Partial {

    private final RSAPrivateKey mPrivateKey;
    private final RSAPublicKey mPublicKey;
    private final String mDisplayName;

    public Partial(String displayName) {
      mDisplayName = displayName;

      KeyPairGenerator keyGenerator = null;
      try {
        keyGenerator = KeyPairGenerator.getInstance("RSA");
      } catch (NoSuchAlgorithmException exception) {
        throw new IllegalStateException(exception);
      }

      keyGenerator.initialize(KEY_SIZE);
      KeyPair keyPair = keyGenerator.genKeyPair();

      mPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
      mPublicKey = (RSAPublicKey) keyPair.getPublic();
    }

    public String getModulusBase64() {
      return Base64Util.encode(mPublicKey.getModulus().toByteArray());
    }

    public String getPublicExponentBase64() {
      return Base64Util.encode(mPublicKey.getPublicExponent().toByteArray());
    }

    public String getPrivateExponentBase64() {
      return Base64Util.encode(mPrivateKey.getPrivateExponent().toByteArray());
    }

    public Identity build(long userId) {
      return new Identity(userId, mDisplayName, mPublicKey, mPrivateKey);
    }
  }

  private final long mUserId;
  private final String mDisplayName;
  private final RSAPublicKey mPublicKey;
  private final RSAPrivateKey mPrivateKey;

  private static BigInteger decodeBigInteger(String integerBase64) {
    return new BigInteger(Base64Util.decode(integerBase64));
  }

  public Identity(
      long userId,
      String displayName,
      String modulusBase64,
      String publicKeyBas64,
      String privateKeyBase64) {
    mUserId = userId;
    mDisplayName = displayName;

    KeyFactory keyFactory = null;
    try {
      keyFactory = KeyFactory.getInstance("RSA");
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }

    BigInteger modulus = decodeBigInteger(modulusBase64);
    BigInteger privateKey = decodeBigInteger(privateKeyBase64);
    BigInteger publicKey = decodeBigInteger(publicKeyBas64);

    RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulus, publicKey);
    RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(modulus, privateKey);

    try {
      mPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicSpec);
      mPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateSpec);
    } catch (InvalidKeySpecException exception) {
      throw new IllegalStateException(exception);
    }
  }

  public Identity(
      long userId,
      String displayName,
      RSAPublicKey publicKey,
      RSAPrivateKey privateKey) {
    mUserId = userId;
    mDisplayName = displayName;
    mPrivateKey = privateKey;
    mPublicKey = publicKey;
  }

  public String signBytes(byte[] data) {
    try {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(mPrivateKey);
      signature.update(data);
      return Base64Util.encode(signature.sign());
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    } catch (SignatureException exception) {
      throw new IllegalStateException(exception);
    } catch (InvalidKeyException exception) {
      throw new IllegalStateException(exception);
    }
  }

  public long getId() {
    return mUserId;
  }

  public String getDisplayName() {
    return mDisplayName;
  }

  public String getModulusBase64() {
    return Base64Util.encode(mPublicKey.getModulus().toByteArray());
  }

  public String getPublicExponentBase64() {
    return Base64Util.encode(mPublicKey.getPublicExponent().toByteArray());
  }

  public String getPrivateExponentBase64() {
    return Base64Util.encode(mPrivateKey.getPrivateExponent().toByteArray());
  }
}
