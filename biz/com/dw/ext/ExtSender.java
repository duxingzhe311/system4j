package com.dw.ext;

import com.dw.system.DomainItem;
import com.dw.system.DomainManager;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;

public class ExtSender
{
  static Thread senderTh = null;

  static String URL_STR = "http://www.gojetsoft.com/system/product/coll.jsp";

  static Runnable senderRunner = new Runnable()
  {
    public void run()
    {
      long h2 = 3600000L;
      while (true)
      {
        try
        {
          Thread.sleep(h2);
        }
        catch (Exception ee)
        {
          break;
        }

        try
        {
          Calendar cal = Calendar.getInstance();
          int hofd = cal.get(11);
          if (hofd == 3)
          {
            ExtSender.access$0();
          }
        }
        catch (Exception ee) {
          ee.printStackTrace();
        }
      }

      ExtSender.senderTh = null;
    }
  };

  public static void start()
  {
    senderTh = new Thread(senderRunner, "ext_sender");
    senderTh.start();
  }

  private static void doSend()
    throws Exception, MalformedURLException, IOException
  {
    DomainItem[] dis = DomainManager.getInstance().getAllDomainItems();
    if ((dis == null) || (dis.length <= 0)) {
      return;
    }
    XmlData tmpxd = new XmlData();
    List xds = tmpxd.getOrCreateSubDataArray("domain_nums");
    for (DomainItem di : dis)
    {
      int n = UserManager.getDefaultIns().getUserNumValid(di.getId());
      XmlData xd = new XmlData();
      xd.setParamValue("domain", di.getDomain());
      xd.setParamValue("num", Integer.valueOf(n));
      xds.add(xd);
    }

    byte[] sd = tmpxd.toHexString().getBytes();
    byte[] lend = (sd.length + "\n").getBytes();

    OutputStream out = null;
    InputStream in = null;
    try
    {
      URL u = new URL(URL_STR);
      URLConnection conn = u.openConnection();

      conn.setDoOutput(true);

      out = conn.getOutputStream();

      out.write(lend);
      out.write(sd);

      out.flush();
      out.close();
      out = null;
      conn.connect();
      in = conn.getInputStream();
      byte[] buf = new byte[100];
      int ll = in.read(buf);
      in.close();

      in = null;
    }
    finally
    {
      if (out != null)
      {
        try
        {
          out.close();
        }
        catch (Exception localException)
        {
        }
      }
      if (in != null)
      {
        try
        {
          in.close();
        }
        catch (Exception localException1)
        {
        }
      }
    }
  }
}
