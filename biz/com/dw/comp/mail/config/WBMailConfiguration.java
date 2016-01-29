package com.dw.comp.mail.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WBMailConfiguration
  implements Serializable
{
  private boolean m_POAllowOverride = false;
  private List m_PostOffices;
  private FolderMap m_FolderMap = new FolderMap();
  private MailTransportAgent m_MTA;
  private Administration m_Administration;
  private Internationalization m_I18N;
  private Security m_Security;
  private boolean m_AutoAccount = true;
  private String m_DefaultMessageProcessor;
  private String m_PreferencePersistencePlugin;
  private String m_ContactManagementPlugin;
  private String m_RandomAppendPlugin;
  public static final String LOG_DIR = "logs";
  public static final String ETC_DIR = "etc";
  public static final String DATA_DIR = "data";
  public static final String I18N_DIR = "i18n";
  public static final String CONFIG_FILENAME = "configuration.xml";
  public static final String LOG4J_CONFIG = "log4j.properties";
  public static final String JTEXTPROC_CONFIG = "textproc.properties";
  public static final String TEMPLATE_FILENAME = "site_template.xml";

  public WBMailConfiguration()
  {
    this.m_PostOffices = Collections.synchronizedList(new ArrayList(5));
  }

  public Security getSecurity()
  {
    return this.m_Security;
  }

  public void setSecurity(Security security)
  {
    this.m_Security = security;
  }

  public Iterator getPostOffices()
  {
    return this.m_PostOffices.iterator();
  }

  public Collection getPostOfficeCollection()
  {
    return this.m_PostOffices;
  }

  public FolderMap getFolderMap()
  {
    return this.m_FolderMap;
  }

  public void setFolderMap(FolderMap fm)
  {
    this.m_FolderMap = fm;
  }

  public void setPostOfficeCollection(Collection collection)
  {
    this.m_PostOffices = Collections.synchronizedList(new ArrayList(collection));
  }

  public void addPostOffice(PostOffice po)
  {
    if (!existsPostOfficeByName(po.getName()))
    {
      this.m_PostOffices.add(po);
    }
  }

  public void removePostOffice(PostOffice po)
  {
    this.m_PostOffices.remove(po);
  }

  public boolean existsPostOfficeByName(String name)
  {
    for (Iterator iterator = this.m_PostOffices.iterator(); iterator.hasNext(); )
    {
      if (((PostOffice)iterator.next()).getName().equals(name))
      {
        return true;
      }
    }
    return false;
  }

  public PostOffice getPostOfficeByName(String name)
  {
    for (Iterator iterator = this.m_PostOffices.iterator(); iterator.hasNext(); )
    {
      PostOffice office = (PostOffice)iterator.next();
      if (office.getName().equals(name))
      {
        return office;
      }
    }
    return null;
  }

  public boolean getPostOfficeAllowOverride()
  {
    return this.m_POAllowOverride;
  }

  public void setPostOfficeAllowOverride(boolean b)
  {
    this.m_POAllowOverride = b;
  }

  public PostOffice getDefaultPostOffice()
  {
    for (Iterator iterator = this.m_PostOffices.iterator(); iterator.hasNext(); )
    {
      PostOffice office = (PostOffice)iterator.next();
      if (office.isDefault())
      {
        return office;
      }
    }
    return null;
  }

  public void setDefaultPostOffice(PostOffice ndpo)
  {
    PostOffice odpo = getDefaultPostOffice();
    odpo.setDefault(false);
    ndpo.setDefault(true);
  }

  public MailTransportAgent getMTA()
  {
    return this.m_MTA;
  }

  public void setMTA(MailTransportAgent mta)
  {
    this.m_MTA = mta;
  }

  public boolean isSSLSetupRequired()
  {
    if (this.m_MTA.isSecure())
    {
      return true;
    }
    for (Iterator iterator = this.m_PostOffices.iterator(); iterator.hasNext(); )
    {
      if (((PostOffice)iterator.next()).isSecure())
      {
        return true;
      }
    }
    return false;
  }

  public Administration getAdministration()
  {
    return this.m_Administration;
  }

  public void setAdministration(Administration administration)
  {
    this.m_Administration = administration;
  }

  public boolean isAccountCreationEnabled()
  {
    return this.m_AutoAccount;
  }

  public void setAccountCreationEnabled(boolean b)
  {
    this.m_AutoAccount = b;
  }

  public String getDefaultMessageProcessor()
  {
    return this.m_DefaultMessageProcessor;
  }

  public void setDefaultMessageProcessor(String name)
  {
    this.m_DefaultMessageProcessor = name;
  }

  public Internationalization getI18N()
  {
    return this.m_I18N;
  }

  public void setI18N(Internationalization i18N)
  {
    this.m_I18N = i18N;
  }

  public String getPreferencePersistencePlugin()
  {
    return this.m_PreferencePersistencePlugin;
  }

  public void setPreferencePersistencePlugin(String classname)
  {
    this.m_PreferencePersistencePlugin = classname;
  }

  public String getContactManagementPlugin()
  {
    return this.m_ContactManagementPlugin;
  }

  public void setContactManagementPlugin(String classname)
  {
    this.m_ContactManagementPlugin = classname;
  }

  public String getRandomAppendPlugin()
  {
    return this.m_RandomAppendPlugin;
  }

  public void setRandomAppendPlugin(String classname)
  {
    this.m_RandomAppendPlugin = classname;
  }
}
