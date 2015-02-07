package com.everythingissauce.pifuxelck.api;

/**
 * Provides the singleton Api instance.
 */
public class ApiProvider {

  private static final String PROTOCOL = "http";
  private static final String DOMAIN = "shamblinghalfling.com";
  private static final int PORT = 4242;

  private static final HttpRequestFactory sHttpRequestFactory =
      new HttpRequestFactory(PROTOCOL, DOMAIN, PORT);

  private static final Api sInstance = new ApiImpl(sHttpRequestFactory);

  public static Api getApi() {
    return sInstance;
  }
}
