package com.everythingissauce.pifuxelck.api;

import com.everythingissauce.pifuxelck.api.Api.Callback;

import org.apache.commons.io.IOUtils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Represents the API that is used to communicate with an abstract backend.
 */
class HttpRequest {

  public static final String GET = "GET";

  public static final String POST = "POST";

  private static final String TAG = "HttpRequest";
  private static final boolean DEBUG = true;

  @Nullable private Callback<String> mCallback;
  @Nullable private String mBody;
  @Nullable private String mEndPoint;

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
    mCallback = null;
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

  public HttpRequest setCallback(Callback<String> callback) {
    checkFrozen();
    mCallback = callback;
    return this;
  }

  public void makeRequest() {
    mFrozen = true;

    // AsyncTasks must be created from the UI thread.
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        new HttpRequestTask().execute();
      }
    });
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
  private class HttpRequestTask extends AsyncTask<Void, Void, String> {

    @Override
    public String doInBackground(Void... params) {
      HttpURLConnection connection = null;
      try {
        String endPoint = mEndPoint == null ? "/" : mEndPoint;
        URL url = new URL(mProtocol, mHost, mPort, endPoint);
        if (DEBUG) Log.i(TAG, "Connecting to: " + url);

        connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(mMethod);
        if (DEBUG) Log.i(TAG, "Setting method: " + mMethod);

        if (mBody != null) {
          if (DEBUG) Log.i(TAG, "Setting body: " + mBody);
          connection.setDoOutput(true);
          Writer writer = new OutputStreamWriter(
              new BufferedOutputStream(connection.getOutputStream()));
          writer.write(mBody, 0, mBody.length());
        }

        int responseCode = connection.getResponseCode();
        if (DEBUG) Log.i(TAG, "Got response code: " + responseCode);
        if (responseCode < 200 || 299 < responseCode) {
          return null;
        }

        InputStream responseInputStream = new BufferedInputStream(
            connection.getInputStream());
        String responseBody = IOUtils.toString(responseInputStream, "UTF-8");
        if (DEBUG) Log.i(TAG, "Got response: " + responseBody);

        return responseBody;
      } catch (IOException exception) {
        Log.e(TAG, "Unable to complete HTTP request.", exception);
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }

      return null;
    }

    @Override
    public void onPostExecute(String httpBody) {
      if (mCallback == null) {
        return;
      }

      if (httpBody == null) {
        mCallback.onApiFailure();
      }
      mCallback.onApiSuccess(httpBody);
    }
  }
}
