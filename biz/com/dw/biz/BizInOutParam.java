package com.dw.biz;

import com.dw.system.Convert;
import com.dw.system.codedom.AbstractRunEnvironment;
import com.dw.system.codedom.IDomNode;
import com.dw.system.codedom.IOperRunner;
import com.dw.system.codedom.RunContext;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

public class BizInOutParam
  implements IXmlDataable
{
  private static Object locker = new Object();
  private static AbstractRunEnvironment ENV = null;

  private ParamStyle style = ParamStyle.All;
  private String scriptTxt = null;

  private transient IDomNode codeDomNode = null;

  private static AbstractRunEnvironment getEnv()
    throws Exception
  {
    if (ENV != null) {
      return ENV;
    }
    synchronized (locker)
    {
      if (ENV != null) {
        return ENV;
      }
      ENV = new REnv();
      ENV.init();
      return ENV;
    }
  }

  public BizInOutParam()
  {
  }

  public BizInOutParam(ParamStyle ps, String scriptt)
  {
    this.style = ps;
    this.scriptTxt = scriptt;
  }

  public ParamStyle getParamStyle()
  {
    return this.style;
  }

  public void setParamStyle(ParamStyle ps)
  {
    this.style = ps;
  }

  public String getScriptTxt()
  {
    return this.scriptTxt;
  }

  public void setScriptTxt(String st)
  {
    this.scriptTxt = st;

    this.codeDomNode = null;
  }

  public IDomNode getScriptCodeDom()
    throws Exception
  {
    if (this.codeDomNode != null) {
      return this.codeDomNode;
    }
    if (Convert.isNullOrTrimEmpty(this.scriptTxt)) {
      return null;
    }
    this.codeDomNode = AbstractRunEnvironment.parseCodeBlockToTree("{" + this.scriptTxt + "}", null);
    return this.codeDomNode;
  }

  public Object runScript(RunContext rc) throws Exception
  {
    IDomNode dn = getScriptCodeDom();
    if (dn == null) {
      return null;
    }
    return getEnv().runDomExp(rc, getScriptCodeDom(), null, null);
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("style", this.style.toString());
    if (this.scriptTxt != null)
      xd.setParamValue("script", this.scriptTxt);
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    String stylestr = xd.getParamValueStr("style");
    if (Convert.isNotNullEmpty(stylestr))
      this.style = ParamStyle.valueOf(stylestr);
    else {
      this.style = ParamStyle.All;
    }
    this.scriptTxt = xd.getParamValueStr("script");
  }

  public static enum ParamStyle
  {
    All, 
    Null, 
    Script;
  }

  static class REnv extends AbstractRunEnvironment
  {
    protected Object[] globalSupportedObj()
    {
      return null;
    }

    public IOperRunner getEnvOperRunner(String oper_str)
    {
      return null;
    }
  }
}
