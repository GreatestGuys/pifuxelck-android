package com.everythingissauce.pifuxelck.api;

class HttpRequestFactory {

  private final String mProtocol;
  private final String mHost;
  private final int mPort;

  public HttpRequestFactory(String protcol, String host, int port) {
    mProtocol = protcol;
    mHost = host;
    mPort = port;
  }

  public HttpRequest newRequest() {
    return new HttpRequest(mProtocol, mHost, mPort);
  }
}