package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailException;
import com.dw.comp.mail.WBMailManager;
import com.dw.comp.mail.config.FolderMap;
import com.dw.comp.mail.config.MailTransportAgent;
import com.dw.comp.mail.config.PostOffice;
import com.dw.comp.mail.config.WBMailConfiguration;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.util.Date;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;

public class WBMailSession
{
  private static ILogger log = LoggerManager.getLogger(WBMailSession.class
    .getCanonicalName());

  String userName = null;

  String password = null;

  WBMailAuthenticator auth = null;

  private boolean bAuthenticated = false;

  transient Session mailSession = null;

  private transient Transport mailTransport = null;

  private transient URLName mailTransURLName = null;

  private transient Store mailStore = null;

  transient PostOffice postOffice = null;

  transient FolderMap folderMap = null;

  private transient MailTransportAgent sendMailTA = null;

  private transient WBMailStore wbMailStore = null;

  private transient int accessCount = 0;

  public WBMailSession(PostOffice po, FolderMap fm, String usern, String password) throws Exception
  {
    this.postOffice = po;

    this.folderMap = fm;

    this.userName = usern;

    this.auth = new WBMailAuthenticator(usern, password);

    this.sendMailTA = WBMailManager.getInstance().getConfiguration().getMTA();

    initMailSession();
  }

  public int increaseAccessCount()
  {
    this.accessCount += 1;
    return this.accessCount;
  }

  public String getUniqueKey()
  {
    return this.userName + "@" + this.postOffice.getName();
  }

  public String getUserName()
  {
    return this.userName;
  }

  public PostOffice getPostOffice()
  {
    return this.postOffice;
  }

  public boolean isAuthenticated()
  {
    return this.bAuthenticated;
  }

  private void initMailSession()
    throws WBMailException
  {
    log.debug("initMailSession()");
    try
    {
      if (this.mailStore == null)
      {
        this.mailSession = Session.getInstance(System.getProperties(), this.auth);
        log.debug("Retrieved Session instance.");

        this.mailStore = this.mailSession.getStore(this.postOffice.getProtocol());
        log.debug("Retrieved store instance");
      }
      this.mailStore.connect(this.postOffice.getAddress(), this.postOffice.getPort(), 
        this.userName, this.password);

      log.debug("connected to store");

      if (this.sendMailTA.isAuthenticated())
      {
        this.mailTransURLName = new URLName(this.sendMailTA.getProtocol(), 
          this.sendMailTA.getAddress(), this.sendMailTA.getPort(), null, 
          this.userName, this.password);
        this.mailTransport = this.mailSession.getTransport(this.mailTransURLName);
        log.debug("Prepared authenticated transport.");
      }
      else
      {
        this.mailTransURLName = new URLName(this.sendMailTA.getProtocol(), 
          this.sendMailTA.getAddress(), this.sendMailTA.getPort(), null, 
          null, null);
        this.mailTransport = this.mailSession.getTransport(this.mailTransURLName);
        log.debug("Prepared non authenticated transport.");
      }

      this.mailStore.addConnectionListener(new StoreConnectionHandler());

      this.wbMailStore = WBMailStore.createJwmaStoreImpl(this, this.mailStore, this.folderMap);

      this.bAuthenticated = true;
      log.debug("Created JwmaTransport wrapper instance.");
    }
    catch (AuthenticationFailedException afe)
    {
      this.mailSession = null;

      throw new WBMailException("session.login.authentication", true)
        .setException(afe);
    }
    catch (MessagingException e)
    {
      e.printStackTrace();

      this.mailSession = null;

      throw new WBMailException("jwma.session.initmail", true)
        .setException(e);
    }
  }

  public boolean isClose()
  {
    if (this.mailStore == null) {
      return true;
    }

    return !this.mailStore.isConnected();
  }

  public void endMailSession()
  {
    try
    {
      this.mailSession = null;
      if (this.mailStore != null)
        this.mailStore.close();
      this.mailStore = null;
    }
    catch (Exception ex)
    {
      log.error(ex);
    }
  }

  public void end()
  {
    if (isAuthenticated())
    {
      endMailSession();
    }
  }

  public void finalize()
  {
    end();
  }

  public void sendMessage(Message msg)
    throws MessagingException
  {
    try
    {
      if (!this.mailTransport.isConnected())
      {
        this.mailTransport.connect(this.mailTransURLName.getHost(), 
          this.mailTransURLName.getPort(), this.userName, this.password);
        log.debug("Connected to transport:" + this.mailTransURLName.toString());
      }

      msg.setSentDate(new Date());
      this.mailTransport.sendMessage(msg, msg.getAllRecipients());
    }
    finally
    {
      this.mailTransport.close();
    }
  }

  public WBMailStore getMailStore()
  {
    return this.wbMailStore;
  }

  class StoreConnectionHandler extends ConnectionAdapter
  {
    public StoreConnectionHandler()
    {
    }

    public void disconnected(ConnectionEvent e)
    {
      try
      {
        if (WBMailSession.this.isAuthenticated())
        {
          WBMailSession.this.mailStore.connect(WBMailSession.this.postOffice.getAddress(), 
            WBMailSession.this.postOffice.getPort(), WBMailSession.this.userName, WBMailSession.this.password);
          WBMailSession.log.debug("Reconnected " + WBMailSession.this.mailStore.getURLName());
        }
      }
      catch (Exception ex)
      {
        WBMailSession.log.debug("StoreConnectionHandler.disconnected()", ex);

        WBMailSession.this.end();
      }
    }
  }
}
