package com.dw.comp.mail.config;

import java.io.Serializable;
import java.util.Properties;

public class Security
  implements Serializable
{
  private String m_SSLFactory = "javax.net.ssl.SSLSocketFactory";

  public String getSecureSocketFactory()
  {
    return this.m_SSLFactory;
  }

  public void setSecureSocketFactory(String factory)
  {
    if ((factory != null) && (factory.length() > 0))
      this.m_SSLFactory = factory;
  }

  public void addSocketProperties(String protocol, int port)
  {
    Properties props = System.getProperties();
    props.setProperty(
      "mail." + protocol + ".socketFactory.class", this.m_SSLFactory);

    props.setProperty(
      "mail." + protocol + ".socketFactory.fallback", "false");

    props.setProperty(
      "mail." + protocol + ".socketFactory.port", port);

    props.setProperty(
      "mail." + protocol + ".port", port);
  }
}
