package com.dw.comp.mail.config;

import com.dw.system.AppConfig;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.FileInputStream;
import java.net.URL;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Element;

public class CastorXMLConfiguration
{
  private static ILogger log = LoggerManager.getLogger(CastorXMLConfiguration.class);
  private Mapping m_Mapping;

  public CastorXMLConfiguration()
  {
    if (!loadMapping())
    {
      throw new RuntimeException(
        "Failed to load configuration mapping file.");
    }
  }

  private boolean loadMapping()
  {
    try
    {
      ClassLoader cl = getClass().getClassLoader();
      URL mapping = cl
        .getResource("com/dw/comp/mail/config/configuration_mapping.xml");
      this.m_Mapping = new Mapping(cl);
      this.m_Mapping.loadMapping(mapping);
      log.debug("loadMapping(): Mapping loaded successfully.");
      return true;
    }
    catch (Exception ex)
    {
      log.error(ex);
    }return false;
  }

  public WBMailConfiguration load()
    throws Exception
  {
    WBMailConfiguration config = null;
    FileInputStream fis = null;
    try
    {
      Element ele = AppConfig.getConfElement("mail");

      Unmarshaller m_Unmarshaller = new Unmarshaller(this.m_Mapping);

      config = (WBMailConfiguration)m_Unmarshaller.unmarshal(ele);

      log.debug("load(): Configuration loaded successfully.");
      return config;
    }
    finally
    {
      if (fis != null)
        fis.close();
    }
  }
}
