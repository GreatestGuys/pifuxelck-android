package com.everythingissauce.pifuxelck.api;

/**
 * Provides the singleton Api instance.
 */
public class ApiProvider {

  private static final String PROTOCOL = "https";
  private static final String DOMAIN = "api.everythingissauce.com";
  private static final int PORT = 443;

  private static final HttpRequestFactory sHttpRequestFactory =
      new HttpRequestFactory(PROTOCOL, DOMAIN, PORT);

  private static final Api sInstance = new ApiImpl(sHttpRequestFactory);

  public static Api getApi() {
    return sInstance;
  }
}
