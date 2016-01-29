package com.dw.comp.mail;

import com.dw.comp.mail.config.CastorXMLConfiguration;
import com.dw.comp.mail.config.FolderMap;
import com.dw.comp.mail.config.MailTransportAgent;
import com.dw.comp.mail.config.PostOffice;
import com.dw.comp.mail.config.Security;
import com.dw.comp.mail.config.WBMailConfiguration;
import com.dw.comp.mail.model.WBMailAttachment;
import com.dw.comp.mail.model.WBMailFolder;
import com.dw.comp.mail.model.WBMailMsgDisplay;
import com.dw.comp.mail.model.WBMailMsgInfo;
import com.dw.comp.mail.model.WBMailMsgInfoList;
import com.dw.comp.mail.model.WBMailSession;
import com.dw.comp.mail.model.WBMailSessionPool;
import com.dw.comp.mail.model.WBMailStore;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

public class WBMailManager
{
  public static final String LOGIN_VIEW = "view.login";
  public static final String ERROR_VIEW = "view.error";
  public static final String LOGOUT_VIEW = "view.logout";
  public static final String SUBSCRIBED_VIEW = "view.subscribed";
  public static final String UNSUBSCRIBED_VIEW = "view.unsubscribed";
  public static final String FOLDER_VIEW = "view.folder";
  public static final String MAILBOX_VIEW = "view.mailbox";
  public static final String MESSAGE_VIEW = "view.message";
  public static final String COMPOSE_VIEW = "view.compose";
  public static final String PREFERENCES_VIEW = "view.preferences";
  public static final String FIRSTTIME_VIEW = "view.firsttime";
  public static final String CONTACTS_VIEW = "view.contacts";
  public static final String CONTACT_VIEW = "view.contact";
  public static final String CONTACT_EDIT_VIEW = "view.contact.edit";
  public static final String CONTACTGROUP_VIEW = "view.contactgroup";
  public static final String CONTACTGROUP_EDIT_VIEW = "view.contactgroup.edit";
  public static final String ADMIN_STATUS_VIEW = "view.admin.status";
  public static final String ADMIN_PREFERENCES_VIEW = "view.admin.preferences";
  public static final String ADMIN_SETTINGS_VIEW = "view.admin.settings";
  public static final String ADMIN_LOGIN_VIEW = "view.admin.login";
  public static final String ADMIN_MENU_VIEW = "view.admin.menu";
  public static final String ADMIN_ERROR_VIEW = "view.admin.error";
  public static final String ACCOUNT_CREATION_VIEW = "view.account.creation";
  public static final int ROOT_DIR = 1;
  public static final int ETC_DIR = 2;
  public static final int LOG_DIR = 3;
  public static final int DATA_DIR = 4;
  private static WBMailManager c_Self = null;

  private static ILogger log = LoggerManager.getLogger(WBMailManager.class);

  private int m_Diag = 0;
  private WBMailConfiguration m_Configuration;
  private WBMailSessionPool sessionPool = null;
  private URLClassLoader m_i18nLoader;
  private ResourceBundle m_ErrorMessages;
  private boolean m_StatusEnabled;
  private String[] m_Admins;
  private String m_TextProcFile;
  private String m_RootDir;
  private String m_LogProperties;
  private String m_MainController;
  private String m_MailingController;
  private String m_AdminController;
  private String m_ContactsController;
  private Properties m_Views;

  public static WBMailManager getInstance()
    throws Exception
  {
    if (c_Self != null)
    {
      return c_Self;
    }

    return new WBMailManager();
  }

  private WBMailManager()
    throws Exception
  {
    c_Self = this;
    this.m_Views = new Properties();
    setup();
  }

  private void setup()
    throws Exception
  {
    loadConfiguration();
    this.m_Diag += 1;

    initSessionPool();
    try
    {
      preparePlugins();
      this.m_Diag += 1;

      prepareProcessing();
      this.m_Diag += 1;

      prepareMailServices();
      this.m_Diag += 1;

      this.m_Diag += 1;

      this.m_Diag += 1;
    }
    catch (WBMailException err)
    {
      if (err.hasException())
      {
        log.error(err.getException());
      }
      else
      {
        log.error(err);
      }
      throw err;
    }
    catch (Exception ex)
    {
      log.error(ex);
      throw ex;
    }
  }

  private void loadConfiguration()
    throws Exception
  {
    CastorXMLConfiguration cxmlconf = new CastorXMLConfiguration();

    this.m_Configuration = cxmlconf.load();
    log.info("Loaded configuration.");
  }

  private void initSessionPool() throws Exception
  {
    if (this.m_Configuration == null) {
      return;
    }
    this.sessionPool = new WBMailSessionPool(this.m_Configuration.getDefaultPostOffice(), this.m_Configuration.getFolderMap());
  }

  private void preparePlugins()
    throws Exception
  {
  }

  private void prepareProcessing()
    throws Exception
  {
  }

  private void prepareMailServices()
    throws Exception
  {
    MailTransportAgent mta = this.m_Configuration.getMTA();

    System.getProperties().put("mail." + mta.getProtocol() + ".auth", 
      new Boolean(mta.isAuthenticated()).toString());
    System.getProperties().put("mail.smtp.host", mta.getAddress());
    if (mta.isSecure())
    {
      this.m_Configuration.getSecurity().addSocketProperties(
        mta.getProtocol(), mta.getPort());
    }

    for (Iterator iter = this.m_Configuration.getPostOffices(); iter.hasNext(); )
    {
      PostOffice po = (PostOffice)iter.next();
      if (po.isSecure())
      {
        this.m_Configuration.getSecurity().addSocketProperties(
          po.getProtocol(), po.getPort());
      }
    }
  }

  public WBMailConfiguration getConfiguration()
  {
    return this.m_Configuration;
  }

  public WBMailSessionPool getSessionPool()
  {
    return this.sessionPool;
  }

  public ClassLoader getResourceClassLoader()
  {
    return this.m_i18nLoader;
  }

  public String getErrorMessage(String key)
  {
    if (key == null)
    {
      return "KEY NULL!!!!!!!!";
    }
    try
    {
      return this.m_ErrorMessages.getString(key);
    }
    catch (MissingResourceException mrex) {
    }
    return "#UNDEFINED=" + key + "#";
  }

  public WBMailAttachment getMailAppend(MimeMessage mmm, String fn)
    throws Exception
  {
    String encoding = null;

    Object o = mmm.getContent();

    if ((o instanceof String))
    {
      return null;
    }

    if ((o instanceof MimeMultipart))
    {
      MimeMultipart mm = (MimeMultipart)o;
      int c = mm.getCount();
      Vector fns = new Vector();
      for (int i = 0; i < c; i++)
      {
        BodyPart bp = mm.getBodyPart(i);
        MimeBodyPart mbp = (MimeBodyPart)bp;

        String filen = mbp.getFileName();
        if (filen != null)
        {
          if (fn.equals(filen))
          {
            String contType = bp.getContentType();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            InputStream is = mbp.getInputStream();

            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0)
            {
              int len;
              bos.write(buf, 0, len);
            }

            return new WBMailAttachment(MimeUtility.decodeText(filen), 
              contType, bos);
          }
        }
      }
    }
    return null;
  }

  private static String getInputMimeContent(InputStream is)
    throws Exception
  {
    MimeMessage mmsg = new MimeMessage(null, is);

    Object o = mmsg.getContent();
    if ((o instanceof MimeMultipart))
    {
      MimeMultipart mm = (MimeMultipart)o;
      int c = mm.getCount();
      Vector fns = new Vector();
      for (int i = 0; i < c; i++)
      {
        BodyPart bp = mm.getBodyPart(i);

        Object oo = bp.getContent();
        if ((oo instanceof String))
        {
          return (String)oo;
        }
      }
    }

    return o.getClass().getCanonicalName();
  }

  private static void printListMsgInfo(Folder f)
    throws Exception
  {
    try
    {
      if (!f.isOpen())
      {
        f.open(1);
      }

      System.out.println("unread msg num=" + f.getUnreadMessageCount() + 
        " total msg num=" + f.getMessageCount());
      Message[] msgs = f.getMessages();

      FetchProfile fp = new FetchProfile();

      fp.add(FetchProfile.Item.ENVELOPE);
      fp.add(FetchProfile.Item.FLAGS);

      f.fetch(msgs, fp);

      for (Message msg : msgs)
      {
        WBMailMsgInfo tmpmi = WBMailMsgInfo.createMsgInfo(msg);
        System.out.println("\nhead  -->" + tmpmi);

        WBMailMsgDisplay mailm = WBMailMsgDisplay.createWBMailMsg(msg);
        System.out.println("\ncontent >>" + mailm);
      }

    }
    finally
    {
      try
      {
        if (f.isOpen())
        {
          f.close(false);
        }
      }
      catch (MessagingException localMessagingException)
      {
      }
    }
  }

  static void printListMsgInfo(WBMailFolder f)
    throws Exception
  {
    System.out.println("unread msg num=" + f.getUnreadMessageCount() + 
      " total msg num=" + f.getMessageCount());
    WBMailMsgInfoList msgs = f.getMessageInfoList();

    for (WBMailMsgInfo tmpmi : msgs.listMessageInfos())
    {
      System.out.println("\nhead  -->" + tmpmi);
    }
  }

  public WBMailStore getUserMailStore(String usern, StringBuilder failedreson)
    throws Exception
  {
    WBMailSession wbms = this.sessionPool.acquireSession(usern, failedreson);
    if (wbms == null) {
      return null;
    }
    return wbms.getMailStore();
  }

  public WBMailSession getUserMailSession(String usern, StringBuilder failedreson)
    throws Exception
  {
    return this.sessionPool.acquireSession(usern, failedreson);
  }

  public WBMailSession getSystemMailSession()
    throws Exception
  {
    return this.sessionPool.createSystemSession();
  }

  public static void test(String username) throws Exception
  {
    WBMailManager wbmm = getInstance();

    PostOffice po = wbmm.getConfiguration().getDefaultPostOffice();
    FolderMap fm = wbmm.getConfiguration().getFolderMap();
    WBMailSessionPool sp = wbmm.getSessionPool();

    WBMailSession wbms = null;

    StringBuilder failedreson = new StringBuilder();
    wbms = sp.acquireSession(username, failedreson);
    if (wbms == null)
      throw new Exception(failedreson.toString());
    Folder fd = wbms.getMailStore().getRootFolder();
    WBMailFolder inboxfd = wbms.getMailStore().getInboxInfo();

    System.out.println("default folder=" + fd.getFullName() + " name=" + fd.getName());
    Folder[] fds = fd.list();
    for (Folder tmpfd : fds)
    {
      System.out.println("sub fold==" + tmpfd.getFullName() + " name=" + tmpfd.getName());
      Folder[] ffs = tmpfd.list();
      for (Folder f0 : ffs)
      {
        System.out.println("    sub fold==" + f0.getFullName() + " name=" + f0.getName());
      }
    }

    System.out.println("inbox folder=" + inboxfd.getName());
    System.out.println("list default folder--->");

    System.out.println("list inbox--->");
    printListMsgInfo(inboxfd);
  }

  public static void main(String[] args)
    throws Exception
  {
    WBMailManager wbmm = getInstance();

    PostOffice po = wbmm.getConfiguration().getDefaultPostOffice();
    FolderMap fm = wbmm.getConfiguration().getFolderMap();

    System.out.println("imap server ip=" + po.getAddress());
    WBMailSession wbms = null;
    try
    {
      wbms = new WBMailSession(po, fm, "admin", "123456");

      Folder fd = wbms.getMailStore().getRootFolder();

      Folder inboxfd = wbms.getMailStore().getFolder("drafts");

      System.out.println("default folder=" + fd.getFullName() + " name=" + fd.getName());
      Folder[] fds = fd.list();
      for (Folder tmpfd : fds)
      {
        System.out.println("sub fold==" + tmpfd.getFullName() + " name=" + tmpfd.getName());
        Folder[] ffs = tmpfd.list();
        for (Folder f0 : ffs)
        {
          System.out.println("    sub fold==" + f0.getFullName() + " name=" + f0.getName());
        }
      }

      System.out.println("inbox folder=" + inboxfd.getFullName());
      System.out.println("list default folder--->");

      System.out.println("list inbox--->");
      printListMsgInfo(inboxfd);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

      Object ct1 = new ReceivedDateTerm(6, 
        sdf.parse("2007-3-22"));
      Object ct2 = new ReceivedDateTerm(1, 
        sdf.parse("2007-3-24"));

      AndTerm searchat = new AndTerm((SearchTerm)ct1, (SearchTerm)ct2);
      if (!inboxfd.isOpen())
      {
        inboxfd.open(1);
      }

      BodyTerm fst = new BodyTerm("类似网页");
      FlagTerm fts = new FlagTerm(new Flags(Flags.Flag.SEEN), true);

      Message[] res_msgs = inboxfd.search(fts);
      System.out.println("search result--------------");
      if (res_msgs != null)
      {
        for (Message tmpm : res_msgs)
        {
          long st = System.currentTimeMillis();
          String subj = tmpm.getSubject();
          System.out.println(subj + " cost=" + (System.currentTimeMillis() - st));
        }

      }

    }
    finally
    {
      if (wbms != null)
        wbms.end();
    }
  }
}
