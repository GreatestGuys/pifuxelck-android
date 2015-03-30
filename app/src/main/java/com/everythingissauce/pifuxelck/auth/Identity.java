package com.everythingissauce.pifuxelck.auth;

import android.text.TextUtils;

import com.everythingissauce.pifuxelck.Base64Util;

import org.json.JSONException;
import org.json.JSONObject;

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

  private static final String FIELD_ID = "user_id";
  private static final String FIELD_KEY_MODULUS = "modulus";
  private static final String FIELD_KEY_PRIVATE = "private_exponent";
  private static final String FIELD_KEY_PUBLIC = "public_exponent";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_PASSWORD = "password";

  private final long mUserId;
  private final String mDisplayName;
  private final String mPassword;
  private final RSAPublicKey mPublicKey;
  private final RSAPrivateKey mPrivateKey;

  private static BigInteger decodeBigInteger(String integerBase64) {
    return new BigInteger(Base64Util.decode(integerBase64));
  }

  public Identity(
      long userId,
      String displayName,
      String password,
      String modulusBase64,
      String publicKeyBas64,
      String privateKeyBase64) {
    mUserId = userId;
    mDisplayName = displayName;
    mPassword = password;

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
      String password) {
    mUserId = userId;
    mDisplayName = displayName;
    mPassword = password;
    mPublicKey = null;
    mPrivateKey = null;
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

  public boolean hasPassword() {
    return !TextUtils.isEmpty(mPassword);
  }

  public String getPassword() {
    return mPassword;
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

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(FIELD_ID, getId());
    json.put(FIELD_NAME, getDisplayName());
    if (!TextUtils.isEmpty(mPassword)) {
      json.put(FIELD_PASSWORD, getPassword());
    }
    if (mPublicKey != null && mPrivateKey != null) {
      json.put(FIELD_KEY_PUBLIC, getPublicExponentBase64());
      json.put(FIELD_KEY_PRIVATE, getPrivateExponentBase64());
      json.put(FIELD_KEY_MODULUS, getModulusBase64());
    }
    return json;
  }

  public static Identity fromJson(JSONObject json) throws JSONException {
    String password = json.optString(FIELD_PASSWORD);
    return TextUtils.isEmpty(password)
        ? new Identity(
            json.getLong(FIELD_ID),
            json.getString(FIELD_NAME),
            password,
            json.getString(FIELD_KEY_MODULUS),
            json.getString(FIELD_KEY_PUBLIC),
            json.getString(FIELD_KEY_PRIVATE))
        : new Identity(
            json.getLong(FIELD_ID),
            json.getString(FIELD_NAME),
            password);
  }
}
