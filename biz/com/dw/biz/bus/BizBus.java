package com.dw.biz.bus;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlData;
import java.util.Hashtable;

public class BizBus
{
  static ILogger log = LoggerManager.getLogger(BizBus.class.getCanonicalName());

  Hashtable<String, BizBusLine> name2Line = new Hashtable();

  Hashtable<String, BizBusAdapter> name2Adapter = new Hashtable();

  private IBizBusIO busIO = null;

  public BizBus(IBizBusIO bbio)
  {
    this.busIO = bbio;
  }

  public void registerAdapter(BizBusAdapter bba)
  {
  }

  public BizBusAdapter getRegisteredAdapter(String adpname)
  {
    return null;
  }

  public void setAdapterConfig(String adp_name, BizBusAdapter.BizBusAdpConfig config)
    throws Exception
  {
    BizBusAdapter bba = (BizBusAdapter)this.name2Adapter.get(adp_name);
    if (bba == null) {
      throw new Exception("cannot find adapter with name=" + adp_name);
    }
    synchronized (bba)
    {
      byte[] conf_cont = (byte[])null;
      if (config != null)
        conf_cont = config.toXmlData().toXmlString().getBytes("UTF-8");
      this.busIO.saveAdpConfig(adp_name, conf_cont);

      bba.setConfigInfo(config);
    }
  }

  void sendData(String adp_name, String line_name, String datatype, XmlData data)
  {
  }

  public BizBusLine getBusLine(String line_name)
  {
    BizBusLine bbl = (BizBusLine)this.name2Line.get(line_name);
    if (bbl != null) {
      return bbl;
    }
    synchronized (this)
    {
      bbl = (BizBusLine)this.name2Line.get(line_name);
      if (bbl != null) {
        return bbl;
      }
      try
      {
        byte[] cont = this.busIO.loadBusLineCont(line_name);
        if (cont == null) {
          return null;
        }
        XmlData tmpxd = XmlData.parseFromByteArray(cont, "UTF-8");
        bbl = new BizBusLine();
        bbl.fromXmlData(tmpxd);

        this.name2Line.put(line_name, bbl);
        return bbl;
      }
      catch (Exception e)
      {
        e.printStackTrace();
        log.error(e);
        return null;
      }
    }
  }
}
