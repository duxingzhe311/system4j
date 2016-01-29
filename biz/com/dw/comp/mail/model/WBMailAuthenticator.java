package com.dw.comp.mail.model;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class WBMailAuthenticator extends Authenticator
{
  private PasswordAuthentication m_Authentication;

  public WBMailAuthenticator(String username, String password)
  {
    this.m_Authentication = new PasswordAuthentication(username, password);
  }

  protected PasswordAuthentication getPasswordAuthentication()
  {
    return this.m_Authentication;
  }

  public String getPassword()
  {
    return this.m_Authentication.getPassword();
  }

  public String getUserName()
  {
    return this.m_Authentication.getUserName();
  }
}
