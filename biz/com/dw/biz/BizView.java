package com.dw.biz;

import com.dw.mltag.AbstractNode;
import com.dw.mltag.JspDirective;
import com.dw.mltag.NodeParser;
import com.dw.mltag.XmlNode;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataMember;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

@XRmi(reg_name="biz_view")
public class BizView extends BizNodeObj
  implements IBizFile
{
  String title = null;

  String desc = null;

  String strCont = null;

  boolean bInner = false;
  String innerName = null;
  BizNodeObj innerBelongTo = null;

  XmlDataStruct ctrlDataStruct = new XmlDataStruct();

  ArrayList<BizOutput> outputs = new ArrayList();

  ArrayList<BizEvent> events = new ArrayList();

  CounterSign counterSign = null;

  transient boolean bValid = false;

  private transient XmlNode contTreeRoot = null;

  transient Object runnerObj = null;

  transient BizManager bizMgr = null;

  JspDirective viewJspDir = null;

  private transient XmlDataStruct inOutStruct = null;

  public BizView()
  {
  }

  public BizView(String innername, BizNodeObj belongpath, String strcont)
  {
    this.bInner = true;
    this.innerName = innername;
    this.innerBelongTo = belongpath;
    this.strCont = strcont;
  }

  public boolean isInnerView()
  {
    return this.bInner;
  }

  public String getInnerName()
  {
    return this.innerName;
  }

  public BizNodeObj getInnerBelongTo()
  {
    return this.innerBelongTo;
  }

  public String getCanonicalPath()
  {
    if (isInnerView())
    {
      return getInnerBelongTo().getBizPathStr() + "$" + getInnerName();
    }

    return getBizPathStr();
  }

  public BizPath getBizPath()
  {
    if (isInnerView())
    {
      return new BizPath(getCanonicalPath());
    }

    return super.getBizPath();
  }

  public BizNode getBelongToBizNode()
  {
    if (isInnerView())
    {
      return getInnerBelongTo().getBelongToBizNode();
    }

    return super.getBelongToBizNode();
  }

  public BizView(String title, String desc, String strcont, XmlDataStruct ctrlxds, XmlDataStruct inputxds, List<BizOutput> outputs)
  {
    this.title = title;

    this.desc = desc;

    this.strCont = strcont;

    this.ctrlDataStruct = ctrlxds;

    if (outputs != null) {
      this.outputs.addAll(outputs);
    }
    for (BizOutput op : this.outputs)
    {
      op.belongTo = this;
    }
  }

  public void initView()
    throws Exception
  {
    if ((this.strCont == null) || (this.strCont.equals("")));
  }

  public void init(BizManager bizmgr)
    throws Exception
  {
    this.bizMgr = bizmgr;

    initView();

    this.bValid = true;
  }

  public boolean isValid()
  {
    return this.bValid;
  }

  private JspDirective getViewJspDirective()
  {
    if (this.viewJspDir != null) {
      return this.viewJspDir;
    }
    try
    {
      XmlNode xn = getContTreeNodeRoot();
      List ll = xn.getSubXmlNodeByType(JspDirective.class);
      if (ll != null)
      {
        for (Iterator localIterator = ll.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

          JspDirective jd = (JspDirective)o;
          if ("view".equalsIgnoreCase(jd.getJspDirectiveType()))
          {
            this.viewJspDir = jd;
            return this.viewJspDir;
          }
        }
      }
    }
    catch (Exception localException)
    {
    }

    this.viewJspDir = new JspDirective("view");
    return this.viewJspDir;
  }

  public String getTitle()
  {
    if (this.title != null) {
      return this.title;
    }
    JspDirective jd = getViewJspDirective();
    if (jd != null)
      this.title = jd.getAttribute("title");
    else
      this.title = "";
    return this.title;
  }

  public void setTitle(String t)
  {
    this.title = t;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public int getViewWidth()
  {
    JspDirective jd = getViewJspDirective();
    if (jd == null) {
      return 400;
    }
    String strw = jd.getAttribute("width");
    if ((strw == null) || (strw.equals(""))) {
      return 400;
    }
    return Integer.parseInt(strw);
  }

  public int getViewHeight()
  {
    JspDirective jd = getViewJspDirective();
    if (jd == null) {
      return 400;
    }
    String strw = jd.getAttribute("height");
    if ((strw == null) || (strw.equals(""))) {
      return 400;
    }
    return Integer.parseInt(strw);
  }

  public void setDesc(String d)
  {
    this.desc = d;
  }

  public String getStrCont()
  {
    return this.strCont;
  }

  public synchronized void setStrCont(String sc)
  {
    this.strCont = sc;
    this.contTreeRoot = null;
  }

  public CounterSign getCounterSign()
  {
    return this.counterSign;
  }

  public void setCounterSign(CounterSign cs)
  {
    this.counterSign = cs;
  }

  public XmlNode getContTreeNodeRoot() throws Exception
  {
    if (this.contTreeRoot != null) {
      return this.contTreeRoot;
    }
    synchronized (this)
    {
      if (this.contTreeRoot != null) {
        return this.contTreeRoot;
      }
      return reloadContTreeNodeRoot();
    }
  }

  public XmlNode reloadContTreeNodeRoot() throws IOException, Exception
  {
    if (this.strCont == null) {
      return null;
    }
    StringReader sr = new StringReader(this.strCont);
    NodeParser parser = new NodeParser(sr);

    parser.setIgnoreCase(true);

    parser.parse();

    XmlNode tmpxn = parser.getRoot();
    parseInclude(tmpxn);
    this.contTreeRoot = tmpxn;

    return this.contTreeRoot;
  }

  private void parseInclude(XmlNode xn)
    throws Exception
  {
    int cc = xn.getChildCount();
    for (int i = 0; i < cc; i++)
    {
      AbstractNode an = (AbstractNode)xn.getChildAt(i);
      if ((an instanceof JspDirective))
      {
        JspDirective jd = (JspDirective)an;
        if ("include".equalsIgnoreCase(jd.getJspDirectiveType()))
        {
          String src = jd.getAttribute("src");
          if (src != null)
          {
            BizPath bp = getBizPath();
            bp = bp.calRelatedPath(src);
            BizNode obn = getBizContainer().getBizNode(bp);
            if (obn != null)
            {
              BizView obv = (BizView)obn.getBizObj();
              if (obv != null)
              {
                XmlNode tmpxn = obv.getContTreeNodeRoot();
                XmlNode pxn = (XmlNode)an.getParent();
                int pi = pxn.getIndex(an);
                pxn.setChild(tmpxn.getChildren(), pi);
              }
            }
          }
        }
      }
      else if ((an instanceof XmlNode))
      {
        parseInclude((XmlNode)an);
      }
    }
  }

  public ArrayList<String> getViewCellNames()
    throws Exception
  {
    ArrayList rets = new ArrayList();
    XmlNode rootn = getContTreeNodeRoot();
    if (rootn == null) {
      return rets;
    }
    getViewCellNames(rets, rootn);
    return rets;
  }

  private void getViewCellNames(ArrayList<String> lls, XmlNode curn)
  {
    if ("view_cell".equals(curn.getAttribute("runas")))
    {
      String n = curn.getAttribute("id");
      if ((n != null) && (!n.equals(""))) {
        lls.add(n);
      }
      return;
    }

    int cc = curn.getChildCount();
    for (int i = 0; i < cc; i++)
    {
      AbstractNode cn = (AbstractNode)curn.getChildAt(i);
      if ((cn instanceof XmlNode))
      {
        getViewCellNames(lls, (XmlNode)cn);
      }
    }
  }

  public XmlDataStruct getCtrlDataStruct()
  {
    return this.ctrlDataStruct;
  }

  public void setCtrlDataStruct(XmlDataStruct xds)
  {
    if (xds != null)
      this.ctrlDataStruct = xds;
    else
      this.ctrlDataStruct = new XmlDataStruct();
  }

  public XmlDataStruct getFormInputDataStruct(Controller fc)
    throws Exception
  {
    return filterFormXDS(fc, getInOutXmlDataStruct());
  }

  private XmlDataStruct filterFormXDS(Controller fc, XmlDataStruct xds)
  {
    if (xds == null) {
      return null;
    }
    if (fc == null) {
      return xds;
    }
    XmlDataStruct retxds = xds.copyMe();
    BizViewCell.CtrlType ct;
    for (XmlDataMember xdm : retxds.getSubXmlDataMembers())
    {
      ct = fc.getCellCtrlType(xdm.getName());
      if ((ct == BizViewCell.CtrlType.ignore) || (ct == BizViewCell.CtrlType.read)) {
        retxds.unsetXmlDataMember(xdm.getName());
      }
    }
    for (String n : retxds.getSubStructNames())
    {
      BizViewCell.CtrlType ct = fc.getCellCtrlType(n);
      if ((ct == BizViewCell.CtrlType.ignore) || (ct == BizViewCell.CtrlType.read))
        retxds.unsetSubStruct(n);
    }
    return retxds;
  }

  public XmlDataStruct getFormSubmitOutputStruct(Controller fc)
    throws Exception
  {
    return filterFormXDS(fc, getInOutXmlDataStruct());
  }

  public BizOutput[] getOutputs()
  {
    try
    {
      XmlDataStruct xds = getBizContainer().getBizViewDataStruct(getBizPathStr());
      if (xds == null) {
        return null;
      }
      BizOutput[] rets = new BizOutput[1];
      rets[0] = new BizOutput("submit", xds);
      rets[0].belongTo = this;
      return rets;
    }
    catch (Exception e)
    {
    }
    return null;
  }

  public XmlDataStruct getInOutXmlDataStruct()
    throws Exception
  {
    if (this.inOutStruct != null) {
      return this.inOutStruct;
    }
    this.inOutStruct = getBizContainer().getBizViewDataStruct(getBizPathStr());

    return this.inOutStruct;
  }

  public void setOutputs(List<BizOutput> outs)
  {
    if (this.outputs == null) {
      this.outputs = new ArrayList();
    }
    this.outputs.clear();

    if (outs != null)
      this.outputs.addAll(outs);
  }

  public void setOutputs(BizOutput[] outs)
  {
    if (this.outputs == null) {
      this.outputs = new ArrayList();
    }
    this.outputs.clear();

    if (outs != null)
    {
      for (BizOutput bo : outs)
        this.outputs.add(bo);
    }
  }

  public BizOutput addOutput(String output_name, XmlDataStruct outputxds)
    throws Exception
  {
    if ((output_name == null) || (output_name.equals(""))) {
      throw new Exception("Oper Name Cannot be null or empty!");
    }
    if (outputxds == null) {
      outputxds = new XmlDataStruct();
    }
    BizOutput op = getOutput(output_name);
    if (op != null)
    {
      throw new Exception("Oper with name=" + output_name + 
        " is already existed!");
    }

    return setOutput(output_name, outputxds);
  }

  public BizOutput setOutput(String output_name, XmlDataStruct outputxds)
  {
    BizOutput op = getOutput(output_name);
    if (op != null)
    {
      op.setOutputDataStruct(outputxds);
      return op;
    }

    op = new BizOutput(output_name, outputxds);
    op.belongTo = this;

    this.outputs.add(op);

    return op;
  }

  public BizOutput getOutput(String output_name)
  {
    BizOutput[] bos = getOutputs();
    if (bos == null) {
      return null;
    }
    for (BizOutput op : bos)
    {
      if (op.getName().equals(output_name))
        return op;
    }
    return null;
  }

  public BizEvent[] getEvents()
  {
    BizEvent[] rets = new BizEvent[this.events.size()];
    this.events.toArray(rets);
    return rets;
  }

  public void setEvents(List<BizEvent> evs)
  {
    if (this.events == null) {
      this.events = new ArrayList();
    }
    this.events.clear();

    if (evs != null)
      this.events.addAll(evs);
  }

  public void setEvents(BizEvent[] evs)
  {
    if (this.events == null) {
      this.events = new ArrayList();
    }
    this.events.clear();

    if (evs != null)
    {
      for (BizEvent be : evs)
        this.events.add(be);
    }
  }

  public BizEvent addEvent(String name, XmlDataStruct evxds)
    throws Exception
  {
    if ((name == null) || (name.equals(""))) {
      throw new Exception("Event Name Cannot be null or empty!");
    }
    if (evxds == null) {
      evxds = new XmlDataStruct();
    }
    BizEvent op = getEvent(name);
    if (op != null)
    {
      throw new Exception("Event with name=" + name + 
        " is already existed!");
    }

    return setEvent(name, evxds);
  }

  public BizEvent setEvent(String name, XmlDataStruct evxds)
  {
    BizEvent op = getEvent(name);
    if (op != null)
    {
      op.setEventDataStruct(evxds);
      return op;
    }

    op = new BizEvent(name, evxds);

    this.events.add(op);

    return op;
  }

  public BizEvent getEvent(String ename)
  {
    for (BizEvent op : this.events)
    {
      if (op.getName().equals(ename))
        return op;
    }
    return null;
  }

  public void setRunnerObj(Object robj)
  {
    this.runnerObj = robj;
  }

  public Object getRunnerObj()
  {
    return this.runnerObj;
  }

  public boolean canRunInBizEnv()
    throws Exception
  {
    BizOutput[] bo = getOutputs();
    if ((bo == null) || (bo.length == 0) || (bo.length > 1)) {
      return false;
    }
    XmlDataStruct outxds = bo[0].getOutputDataStruct();
    if (outxds == null) {
      return false;
    }
    return outxds.checkFitFor(getInOutXmlDataStruct(), null);
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    if (this.title != null) {
      xd.setParamValue("title", this.title);
    }
    if (this.desc != null)
      xd.setParamValue("desc", this.desc);
    if (this.strCont != null) {
      xd.setParamValue("cont", this.strCont);
    }

    if (this.ctrlDataStruct != null)
    {
      xd.setSubDataSingle("ctrl_data_struct", this.ctrlDataStruct.toXmlData());
    }

    if (this.outputs != null)
    {
      List xds = xd.getOrCreateSubDataArray("outputs");
      for (BizOutput op : this.outputs)
      {
        xds.add(op.toXmlData());
      }
    }

    if (this.events != null)
    {
      List xds = xd.getOrCreateSubDataArray("events");
      for (BizEvent op : this.events)
      {
        xds.add(op.toXmlData());
      }
    }

    if (this.counterSign != null)
    {
      xd.setSubDataSingle("counter_sign", this.counterSign.toXmlData());
    }

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.title = xd.getParamValueStr("title");

    this.desc = xd.getParamValueStr("desc");
    this.strCont = xd.getParamValueStr("cont");

    XmlData tmpxd = null;

    tmpxd = xd.getSubDataSingle("ctrl_data_struct");
    if (tmpxd != null)
    {
      this.ctrlDataStruct = new XmlDataStruct();
      this.ctrlDataStruct.fromXmlData(tmpxd);
    }

    List operxds = xd.getSubDataArray("outputs");
    BizOutput tmpop;
    if (operxds != null)
    {
      for (XmlData xd0 : operxds)
      {
        tmpop = new BizOutput();
        tmpop.fromXmlData(xd0);
        tmpop.belongTo = this;

        this.outputs.add(tmpop);
      }
    }

    List evxds = xd.getSubDataArray("events");
    if (evxds != null)
    {
      for (XmlData xd0 : evxds)
      {
        BizEvent tmpop = new BizEvent();
        tmpop.fromXmlData(xd0);
        this.events.add(tmpop);
      }
    }

    XmlData csxd = xd.getSubDataSingle("counter_sign");
    if (csxd != null)
    {
      this.counterSign = new CounterSign();
      this.counterSign.fromXmlData(csxd);
    }
  }

  public void fromFileContent(byte[] cont)
  {
    try
    {
      this.strCont = new String(cont, "UTF-8");
    }
    catch (Exception localException)
    {
    }
  }

  @XRmi(reg_name="biz_view_ctrl")
  public static class Controller
    implements IXmlDataable
  {
    BizViewCell.CtrlType defaultCT = BizViewCell.CtrlType.write;

    Hashtable<String, BizViewCell.CtrlType> viewCellName2CT = new Hashtable();

    public Controller()
    {
    }

    public Controller(BizViewCell.CtrlType default_ct)
    {
      this.defaultCT = default_ct;
    }

    public Controller(Hashtable<String, BizViewCell.CtrlType> vcn2ct)
    {
      if (vcn2ct == null) {
        return;
      }
      this.viewCellName2CT = vcn2ct;
    }

    public Controller(BizViewCell.CtrlType default_ct, Hashtable<String, BizViewCell.CtrlType> vcn2ct)
    {
      this.defaultCT = default_ct;
      if (vcn2ct == null) {
        return;
      }
      this.viewCellName2CT = vcn2ct;
    }

    public void setDefaultCtrlType(BizViewCell.CtrlType default_ct)
    {
      this.defaultCT = default_ct;
    }

    public BizViewCell.CtrlType getDefaultCtrlType()
    {
      return this.defaultCT;
    }

    public BizViewCell.CtrlType getCellCtrlTypeValue(String n)
    {
      return (BizViewCell.CtrlType)this.viewCellName2CT.get(n);
    }

    public BizViewCell.CtrlType getCellCtrlType(String uniquen)
    {
      BizViewCell.CtrlType ct = (BizViewCell.CtrlType)this.viewCellName2CT.get(uniquen);
      if (ct == null) {
        return this.defaultCT;
      }
      return ct;
    }

    public void setCellCtrlType(String un, BizViewCell.CtrlType ct)
    {
      if (ct == null)
        this.viewCellName2CT.remove(un);
      else
        this.viewCellName2CT.put(un, ct);
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("default_ct", this.defaultCT.toString());
      XmlData tmpxd = xd.getOrCreateSubDataSingle("cell2ct");

      Iterator localIterator = this.viewCellName2CT
        .entrySet().iterator();

      while (localIterator.hasNext()) {
        Map.Entry n2vt = (Map.Entry)localIterator.next();

        tmpxd.setParamValue((String)n2vt.getKey(), ((BizViewCell.CtrlType)n2vt.getValue()).toString());
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      String strdef_ct = xd.getParamValueStr("default_ct");
      if ((strdef_ct != null) && (!strdef_ct.equals("")))
      {
        this.defaultCT = BizViewCell.CtrlType.valueOf(strdef_ct);
      }

      XmlData tmpxd = xd.getSubDataSingle("cell2ct");
      if (tmpxd != null)
      {
        for (String n : tmpxd.getParamNames())
        {
          String strv = tmpxd.getParamValueStr(n);
          this.viewCellName2CT.put(n, BizViewCell.CtrlType.valueOf(strv));
        }
      }
    }
  }

  public static class EventMapper
  {
    String eventName = null;

    List<String> relViewIds = new ArrayList();

    String outputName = null;
    String mapMethod = null;

    public EventMapper(String eventn, String tarvids, String mapm)
    {
      this(eventn, tarvids.split(","), mapm);
    }

    public EventMapper(String eventn, String tarvids, String outputn, String mapm)
    {
      this(eventn, tarvids.split(","), outputn, mapm);
    }

    public EventMapper(String eventn, String[] tarvids, String mapm)
    {
      this(eventn, tarvids, null, mapm);
    }

    public EventMapper(String eventn, String[] tarvids, String oname, String mapm)
    {
      this.eventName = eventn;

      for (String tvid : tarvids)
      {
        if (!(tvid = tvid.trim()).equals(""))
        {
          this.relViewIds.add(tvid);
        }
      }
      this.outputName = oname;
      this.mapMethod = mapm;
    }

    public String getEventName()
    {
      return this.eventName;
    }

    public List<String> getTargetViewIds()
    {
      return this.relViewIds;
    }

    public String getRelatedViewIdStr()
    {
      int s = this.relViewIds.size();
      if (s <= 0) {
        return "";
      }
      String rets = (String)this.relViewIds.get(0);
      for (int i = 1; i < s; i++) {
        rets = rets + "," + (String)this.relViewIds.get(i);
      }
      return rets;
    }

    public String getOutputName()
    {
      return this.outputName;
    }

    public String getMapMethod()
    {
      return this.mapMethod;
    }
  }

  public static class BeIncludeController
  {
  }

  public static enum CounterSignType
  {
    None(0), 
    Manual(1), 
    Auto(2);

    private final int stVal;

    private CounterSignType(int v)
    {
      this.stVal = v;
    }

    public int getIntValue()
    {
      return this.stVal;
    }

    public static CounterSignType getByIntValue(int v)
    {
      switch (v)
      {
      case 1:
        return Manual;
      case 2:
        return Auto;
      }
      return None;
    }
  }

  public static class CounterSign
    implements IXmlDataable
  {
    BizView.CounterSignType type = BizView.CounterSignType.None;

    String userRR = null;

    public CounterSign()
    {
    }

    public CounterSign(BizView.CounterSignType t, String user_rr) {
      this.type = t;
      if (this.type == null)
        this.type = BizView.CounterSignType.None;
      this.userRR = user_rr;
    }

    public BizView.CounterSignType getType()
    {
      return this.type;
    }

    public String getUserRightRule()
    {
      return this.userRR;
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("type", Integer.valueOf(this.type.getIntValue()));
      if (this.userRR != null)
        xd.setParamValue("user_rr", this.userRR);
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      int t = xd.getParamValueInt32("type", 0);
      this.type = BizView.CounterSignType.getByIntValue(t);
      this.userRR = xd.getParamValueStr("user_rr");
    }
  }
}
