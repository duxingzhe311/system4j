package com.dw.comp.mail.config;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.Serializable;
import java.net.InetAddress;

public class PostOffice
  implements Serializable
{
  private static ILogger log = LoggerManager.getLogger(PostOffice.class.getCanonicalName());
  public static final String TYPE_MIXED = "mixed";
  public static final String TYPE_PLAIN = "plain";
  private String m_Name = "Default";

  private String m_Address = "localhost";

  private int m_Port = -1;

  private String m_RootFolder = "";

  private boolean m_Secure = false;

  private String m_Type = "mixed";

  private String m_Protocol = "imap";

  private boolean m_Default = false;
  private String m_ReplyToDomain;
  private String sign = "";

  public String getName()
  {
    return this.m_Name;
  }

  public void setName(String name)
  {
    this.m_Name = name;
  }

  public String getProtocol()
  {
    return this.m_Protocol;
  }

  public void setProtocol(String protocol)
  {
    this.m_Protocol = protocol;
  }

  public String getType()
  {
    return this.m_Type;
  }

  public void setType(String type)
  {
    this.m_Type = type;
  }

  public boolean isType(String type)
  {
    return this.m_Type.equals(type);
  }

  public String getAddress()
  {
    return this.m_Address;
  }

  public void setAddress(String address)
  {
    try
    {
      if ((address == null) || (address.equals("")) || 
        (address.equals("localhost")))
      {
        this.m_Address = InetAddress.getLocalHost().getHostName();
      }
    }
    catch (Exception ex)
    {
      log.error(ex);
    }
    this.m_Address = address;
  }

  public int getPort()
  {
    return this.m_Port;
  }

  public void setPort(int port)
  {
    this.m_Port = port;
  }

  public boolean isSecure()
  {
    return this.m_Secure;
  }

  public void setSecure(boolean secure)
  {
    this.m_Secure = secure;
  }

  public String getRootFolder()
  {
    return this.m_RootFolder;
  }

  public void setRootFolder(String folder)
  {
    this.m_RootFolder = folder;
  }

  public String getReplyToDomain()
  {
    return this.m_ReplyToDomain;
  }

  public void setReplyToDomain(String domain)
  {
    this.m_ReplyToDomain = domain;
  }

  public boolean isDefault()
  {
    return this.m_Default;
  }

  public void setDefault(boolean aDefault)
  {
    this.m_Default = aDefault;
  }

  public String getSign()
  {
    return this.sign;
  }

  public void setSign(String s)
  {
    this.sign = s;
  }
}
