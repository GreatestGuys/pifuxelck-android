package com.everythingissauce.pifuxelck.api;

import com.everythingissauce.pifuxelck.ThreadUtil;

import com.google.common.util.concurrent.ListenableFuture;

import org.apache.commons.io.IOUtils;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Represents the API that is used to communicate with an abstract backend.
 */
class HttpRequest {

  public static final String GET = "GET";
  public static final String POST = "POST";

  private static final String TAG = "HttpRequest";
  private static final boolean DEBUG = false;

  private static final String HEADER_AUTH = "x-pifuxelck-auth";

  @Nullable private String mBody;
  @Nullable private String mEndPoint;
  @Nullable private String mAuthToken;

  private String mMethod;

  private final String mProtocol;
  private final String mHost;
  private final int mPort;

  private final Object mLock = new Object();
  private boolean mFrozen;

  HttpRequest(String protocol, String host, int port) {
    mProtocol = protocol;
    mHost = host;
    mPort = port;

    mBody = null;
    mEndPoint = null;
    mMethod = GET;

    mFrozen = false;
  }

  public HttpRequest setEndPoint(String endPoint) {
    checkFrozen();
    mEndPoint = endPoint;
    return this;
  }

  public HttpRequest setMethod(String method) {
    checkFrozen();
    mMethod = method;
    return this;
  }

  public HttpRequest setBody(String body) {
    checkFrozen();
    mBody = body;
    return this;
  }

  public HttpRequest setAuthToken(String authToken) {
    checkFrozen();
    mAuthToken = authToken;
    return this;
  }

  public ListenableFuture<String> makeRequest() {
    mFrozen = true;
    return ThreadUtil.THREAD_POOL.submit(new HttpRequestCallable());
  }

  private void checkFrozen() {
    if (mFrozen) {
      throw new IllegalStateException(
          "Attempt to modify an in-flight request.");
    }
  }

  /**
   * Performs an HTTP request in a background thread and calls back the result
   * on the UI thread.
   */
  private class HttpRequestCallable implements Callable<String> {

    @Override
    public String call() throws IOException {
      HttpURLConnection connection = null;
      try {
        String endPoint = mEndPoint == null ? "/" : mEndPoint;
        URL url = new URL(mProtocol, mHost, mPort, endPoint);
        if (DEBUG) Log.i(TAG, "Connecting to: " + url);

        connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(mMethod);
        if (DEBUG) Log.i(TAG, "Setting method: " + mMethod);

        if (!TextUtils.isEmpty(mAuthToken)) {
          if (DEBUG) Log.i(TAG, "Adding auth token: " + mAuthToken);
          connection.addRequestProperty(HEADER_AUTH, mAuthToken);
        }

        if (mBody != null) {
          if (DEBUG) Log.i(TAG, "Setting body: " + mBody);
          connection.setDoOutput(true);
          OutputStream output =
              new BufferedOutputStream(connection.getOutputStream());
          IOUtils.write(mBody, output, "UTF-8");
          output.close();
        }

        int responseCode = connection.getResponseCode();
        if (DEBUG) Log.i(TAG, "Got response code: " + responseCode);
        if (responseCode < 200 || 299 < responseCode) {
          throw new IOException(
              "HTTP request returned error code " + responseCode + ".");
        }

        InputStream responseInputStream = new BufferedInputStream(
            connection.getInputStream());
        String responseBody = IOUtils.toString(responseInputStream, "UTF-8");
        if (DEBUG) Log.i(TAG, "Got response: " + responseBody);

        return responseBody;
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }
    }
  }
}
