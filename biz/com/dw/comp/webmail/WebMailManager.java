package com.dw.comp.webmail;

import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.comp.mail.WBMailManager;
import com.dw.comp.mail.model.WBMailStore;
import com.dw.mailserver.JamesServerBootComp;
import com.dw.system.AppConfig;
import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.DBResult;
import com.dw.system.gdb.DataRow;
import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GdbException;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlHelper;
import com.dw.user.User;
import com.dw.user.UserManager;
import com.dw.user.sso.ModuleUserAccount;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import javax.mail.Folder;
import javax.mail.internet.InternetAddress;
import org.w3c.dom.Element;

public class WebMailManager
{
  static int MAX_ATTACH_LEN = 83886080;

  static WebMailManager ins = null;
  static ILogger log = LoggerManager.getLogger(WebMailManager.class);

  HashMap<String, WebMailPlugInfo> app2plug = null;
  HashMap<String, ArrayList<WebMailAddrPlug>> app2addrplug = null;

  String innerDomain = null;

  boolean recv_new_default_removed_by_client = false;

  String mailEditSign = null;

  static WebMailItem.State[] RECVED_STS = { WebMailItem.State.recved_new, WebMailItem.State.recved_seen };

  public static WebMailManager getInstance()
  {
    if (ins != null) {
      return ins;
    }
    synchronized (log)
    {
      if (ins != null) {
        return ins;
      }
      try
      {
        ins = new WebMailManager();
        return ins;
      }
      catch (Exception e)
      {
        log.error(e);
        return null;
      }
    }
  }

  public static boolean isInnerMailServer()
  {
    return false;
  }

  private WebMailManager()
  {
    Element ele = AppConfig.getConfElement("mail");
    if (ele != null)
    {
      this.innerDomain = ele.getAttribute("inner_domain");

      this.recv_new_default_removed_by_client = "true".equalsIgnoreCase(ele.getAttribute("recv_new_default_removed_by_client"));
    }
  }

  public String getInnerDomain()
  {
    return this.innerDomain;
  }

  public boolean isRecv_new_default_removed_by_client()
  {
    return this.recv_new_default_removed_by_client;
  }

  public String getPubMailEditSign()
  {
    return this.mailEditSign;
  }

  public WebMailPlugInfo getMailPlugInfoByAppName(String appn)
  {
    HashMap n2p = getMailPlugInfo();
    if (n2p == null) {
      return null;
    }
    return (WebMailPlugInfo)n2p.get(appn);
  }

  public WebMailPlugInfo[] getAllMailPlugInfos()
  {
    HashMap n2wmpi = getMailPlugInfo();
    if (n2wmpi == null) {
      return null;
    }

    WebMailPlugInfo[] rets = new WebMailPlugInfo[n2wmpi.size()];
    n2wmpi.values().toArray(rets);
    return rets;
  }

  public HashMap<String, WebMailPlugInfo> getMailPlugInfo()
  {
    if (this.app2plug != null) {
      return this.app2plug;
    }
    HashMap tmp = new HashMap();
    AppInfo[] ais = CompManager.getInstance().getAllAppInfo();
    for (AppInfo ai : ais)
    {
      AppWebConfig awc = AppWebConfig.getModuleWebConfig(ai.getContextName());
      if (awc != null)
      {
        Element ele = awc.getConfElement("mail_plugs");
        if (ele != null)
        {
          Element[] eles = XmlHelper.getSubChildElement(ele, "mail_plug");
          WebMailPlugInfo wmpi;
          if ((eles != null) && (eles.length > 0))
          {
            HashMap hm = XmlHelper.getEleAttrNameValueMap(eles[0]);
            if (hm.size() > 0)
            {
              wmpi = new WebMailPlugInfo(ai.getContextName(), hm);
              tmp.put(wmpi.getAppName(), wmpi);
            }
          }

          eles = XmlHelper.getSubChildElement(ele, "mail_addr_selector");
          if ((eles != null) && (eles.length > 0))
          {
            for (Element tmpe : eles)
            {
              WebMailAddrPlug p = WebMailAddrPlug.createIns(ai.getContextName(), tmpe);
              if (p != null);
            }

          }

        }

      }

    }

    this.app2plug = tmp;
    return tmp;
  }

  public HashMap<String, ArrayList<WebMailAddrPlug>> getMailAddrPlugMap()
  {
    if (this.app2addrplug != null) {
      return this.app2addrplug;
    }
    HashMap tmp = new HashMap();
    AppInfo[] ais = CompManager.getInstance().getAllAppInfo();
    for (AppInfo ai : ais)
    {
      String appn = ai.getContextName();
      AppWebConfig awc = AppWebConfig.getModuleWebConfig(ai.getContextName());
      if (awc != null)
      {
        Element ele = awc.getConfElement("mail_plugs");
        if (ele != null)
        {
          Element[] eles = XmlHelper.getSubChildElement(ele, "mail_addr_selector");
          if ((eles != null) && (eles.length > 0))
          {
            for (Element tmpe : eles)
            {
              WebMailAddrPlug p = WebMailAddrPlug.createIns(ai.getContextName(), tmpe);
              if (p != null)
              {
                ArrayList ps = (ArrayList)tmp.get(appn);
                if (ps == null)
                {
                  ps = new ArrayList();
                  tmp.put(appn, ps);
                }

                ps.add(p);
              }
            }
          }
        }
      }
    }
    this.app2addrplug = tmp;
    return this.app2addrplug;
  }

  public ArrayList<WebMailAddrPlug> getMailAddrPlugAll()
  {
    ArrayList rets = new ArrayList();
    HashMap n2p = getMailAddrPlugMap();
    if (n2p == null) {
      return rets;
    }
    for (Map.Entry n2ps : n2p.entrySet())
    {
      rets.addAll((Collection)n2ps.getValue());
    }
    return rets;
  }

  public WebMailAddrPlug getMailAddrPlug(String appn, String plugn)
  {
    HashMap n2p = getMailAddrPlugMap();
    if (n2p == null) {
      return null;
    }
    ArrayList ps = (ArrayList)n2p.get(appn);
    if (ps == null) {
      return null;
    }
    for (WebMailAddrPlug p : ps)
    {
      if (plugn.equals(p.getName())) {
        return p;
      }
    }
    return null;
  }

  public void checkRecvNewMail(String username)
  {
  }

  public WebMailItem createMailItem(long replymailid, long forwardmailid, int domain, String username, String to, String cc, String bcc, String subject, Date date, WebMailItem.State state, int attchnum, int size, int level, String bodytxt, HashMap<String, WebMailAttachment> filename2cont)
    throws Exception
  {
    String from = null;
    User u = UserManager.getDefaultIns().GetUser(domain, username);
    from = username + "@" + u.getUserDomain();
    String rn = u.getRealName();
    if (rn != null) {
      from = rn + " [" + from + "]";
    }

    if (domain <= 0) {
      return new WebMailItem(replymailid, forwardmailid, from, to, cc, bcc, username, subject, date, state, attchnum, size, level, bodytxt, filename2cont);
    }
    return new WebMailItem(replymailid, forwardmailid, from, to, cc, bcc, username + "@" + domain, subject, date, state, attchnum, size, level, bodytxt, filename2cont);
  }

  public WebMailItem createMailItem(long mailid, long replymailid, long forwardmailid, int domain, String username, String to, String cc, String bcc, String subject, Date date, WebMailItem.State state, int attchnum, int size, int level, String bodytxt, HashMap<String, WebMailAttachment> filename2cont)
    throws Exception
  {
    String from = null;
    if (isInnerMailServer())
    {
      User u = UserManager.getDefaultIns().GetUser(domain, username);

      from = username + "@" + u.getUserDomain();
      String fn = u.getFullName();
      if (Convert.isNullOrEmpty(fn))
        fn = u.getCnName();
      if (Convert.isNullOrEmpty(fn)) {
        fn = u.getEnName();
      }
      if (Convert.isNotNullEmpty(fn))
        from = fn + " [" + from + "]";
    }
    else
    {
      ModuleUserAccount mua = AbstractMailServer.getInstance().getMailAccountInfo(username);
      from = mua.getLoginName();
      if (from.indexOf('@') < 0)
      {
        User u = UserManager.getDefaultIns().GetUser(username);
        if (u != null) {
          from = u.getEmail();
        }
      }
    }
    WebMailItem m = null;
    if (domain <= 0)
      m = new WebMailItem(replymailid, forwardmailid, from, to, cc, bcc, username, subject, date, state, attchnum, size, level, bodytxt, filename2cont);
    else {
      m = new WebMailItem(replymailid, forwardmailid, from, to, cc, bcc, username + "@" + domain, subject, date, state, attchnum, size, level, bodytxt, filename2cont);
    }
    m.mailId = mailid;
    return m;
  }

  public void saveMail(WebMailItem m) throws GdbException, Exception
  {
    GDB.getInstance().addXORMObjWithNewId(m);
    m.getMailId();

    HashMap appm = m.getAppMap();
    if (appm != null) appm.size();
  }

  public String saveTempMail(String appname, String appref, WebMailItem m)
    throws Exception
  {
    HashMap am = new HashMap();
    am.put(appname, appref);
    m.setAppMap(am);

    String uid = UUID.randomUUID().toString();
    File p = new File(AppConfig.getOrCreateDirInDataDirBase("mail_temp/"), uid);
    byte[] bytewmi = m.toMimeMsgByteArray();
    FileOutputStream os = null;
    try
    {
      os = new FileOutputStream(p);
      os.write(bytewmi);
    }
    finally
    {
      if (os != null)
        os.close();
    }
    return uid;
  }

  public void deleteTempMail(String tempid)
  {
    if ((tempid != null) && (!tempid.equals("")))
    {
      String p = AppConfig.getDataDirBase() + "/mail_temp/" + tempid;
      File fp = new File(p);
      if (fp.exists())
        fp.delete();
    }
  }

  public WebMailItem loadTempMail(String tempid) throws Exception
  {
    String p = AppConfig.getDataDirBase() + "/mail_temp/" + tempid;
    File fp = new File(p);
    if (!fp.exists()) {
      return null;
    }
    FileInputStream instr = null;
    try
    {
      instr = new FileInputStream(fp);
      WebMailItem wmi = new WebMailItem(instr);

      return wmi;
    }
    finally
    {
      if (instr != null)
        instr.close();
    }
  }

  public void setMailAppMap(long mid, String appname, String appref)
  {
  }

  public void unsetMailAppMap(long mid, String appname, String appref)
  {
  }

  public WebMailItem loadMail(long mid)
  {
    return null;
  }

  public boolean checkMailExistedByMsgUid(String username, String msguid)
    throws GdbException, Exception
  {
    if (Convert.isNullOrEmpty(msguid)) {
      return false;
    }
    Hashtable ht = new Hashtable();
    ht.put("@MsgUid", msguid);
    ht.put("@UserName", username);
    DBResult dbr = GDB.getInstance().accessDB("AppMail.CheckMsgUid", ht);
    Object o = dbr.getResultFirstColumnOfFirstRow();
    return o != null;
  }

  public WebMailItemList listMails(String username, WebMailItem.State p)
  {
    return null;
  }

  public WebMailItemList listMails(String username, long folderid, String appname, String appref)
  {
    return null;
  }

  public void sendMailInner(String username, WebMailItem wmi)
    throws Exception
  {
    JamesServerBootComp.sendMail(wmi.toMimeMsg(), true);
  }

  public boolean sendMail(String username, WebMailItem m)
    throws Exception
  {
    if (m.getState() != WebMailItem.State.sending) {
      return false;
    }
    WebMailItem wmi = getMailById(m.getMailId());
    try
    {
      AbstractMailServer.getInstance().sendMail(username, wmi);
    }
    catch (Exception ee)
    {
      if (log.isDebugEnabled()) {
        log.error(ee);
      }
      int sen = m.getMailSendErrorNum();
      if (sen < 0) {
        sen = 0;
      }
      sen++;
      m.setMailSendErrorNum(sen);
      GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(wmi.getMailId()), WebMailItem.class, 
        new String[] { "MailSendErrorNum" }, new Object[] { Integer.valueOf(sen) });

      return false;
    }

    wmi.setMailState(WebMailItem.State.sent);

    GDB.getInstance().updateXORMObjToDBWithHasColNames(Long.valueOf(wmi.getMailId()), wmi, new String[] { "MailState" });
    try
    {
      WebMailAddrManager.getInstance().setupMailByStr(wmi.getMailTo());
      WebMailAddrManager.getInstance().setupMailByStr(wmi.getMailCc());
    }
    catch (Exception ee)
    {
      if (log.isDebugEnabled())
        log.error(ee);
    }
    return true;
  }

  public void sendSystemMail(WebMailItem m)
    throws Exception
  {
    AbstractMailServer.getInstance().sendSystemMail(m);
  }

  public void addDraftMail(WebMailItem m)
    throws GdbException, Exception
  {
    GDB.getInstance().addXORMObjWithNewId(m);
  }

  public void addMail(WebMailItem m) throws GdbException, Exception
  {
    GDB.getInstance().addXORMObjWithNewId(m);
  }

  public boolean updateMail(long mid, WebMailItem m)
    throws Exception
  {
    return GDB.getInstance().updateXORMObjToDB(Long.valueOf(mid), m);
  }

  public boolean updateMailState(long mid, WebMailItem m) throws Exception
  {
    return GDB.getInstance().updateXORMObjToDBWithHasColNames(Long.valueOf(mid), m, new String[] { "MailState" });
  }

  public void updateMailStateByMids(int domainid, String username, WebMailItem.State oldst, long[] mids, WebMailItem.State st) throws Exception
  {
    if ((mids == null) || (mids.length <= 0)) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(mids[0]);
    for (int i = 1; i < mids.length; i++)
      sb.append(',').append(mids[i]);
    Hashtable ht = new Hashtable();
    ht.put("$MailIds", sb.toString());
    if (username != null)
    {
      if (domainid <= 0)
        ht.put("@UserName", username);
      else
        ht.put("@UserName", username + "@" + domainid);
    }
    ht.put("@MailState", Integer.valueOf(st.getValue()));
    ht.put("@OldMailState", Integer.valueOf(oldst.getValue()));
    int beforedst = -1;
    if (st == WebMailItem.State.delete)
    {
      beforedst = oldst.getValue();
    }
    ht.put("@MailBeforeDelState", Integer.valueOf(beforedst));
    GDB.getInstance().accessDB("AppMail.UpdateMailStateByMailIds", ht);
  }

  public boolean updateMailState(long mid, WebMailItem.State st)
    throws Exception
  {
    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(mid), WebMailItem.class, new String[] { "MailState" }, new Object[] { Integer.valueOf(st.getValue()) });
  }

  public void recoverDeletedMail(int domainid, String username, long[] mids)
    throws Exception
  {
    if ((mids == null) || (mids.length <= 0)) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(mids[0]);
    for (int i = 1; i < mids.length; i++)
      sb.append(',').append(mids[i]);
    Hashtable ht = new Hashtable();
    ht.put("$MailIds", sb.toString());
    if (username != null)
    {
      if (domainid <= 0)
        ht.put("@UserName", username);
      else {
        ht.put("@UserName", username + "@" + domainid);
      }
    }
    ht.put("@DelMailSt", Integer.valueOf(WebMailItem.State.delete.getValue()));
    GDB.getInstance().accessDB("AppMail.RecoverDeletedByMailIds", ht);
  }

  public void updateDeletedMailToHidden(Date deleted_daybefore)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@BeforeDay", deleted_daybefore);

    GDB.getInstance().accessDB("AppMail.UpdateDeletedToHiddenBeforeDay", ht);
  }

  public void deleteDelHiddenMailWithServer(Date deleted_daybefore)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@BeforeDay", deleted_daybefore);

    DBResult dbr = GDB.getInstance().accessDB("AppMail.ListHasDelHiddenBeforeDayUserNames", ht);
    DataTable dt = dbr.getResultTable(0);
    if (dt == null) {
      return;
    }
    int rn = dt.getRowNum();
    for (int r = 0; r < rn; r++)
    {
      String un = (String)dt.getRow(r).get(Integer.valueOf(0));
      deleteUserDelHiddenMailWithServer(un, deleted_daybefore);
    }
  }

  public void deleteUserDelHiddenMailWithServer(String username, Date deleted_daybefore)
    throws Exception
  {
    int IDLEN = 20;

    Hashtable ht0 = new Hashtable();
    ht0.put("@BeforeDay", deleted_daybefore);
    ht0.put("@UserName", username);
    DBResult dbr = GDB.getInstance().accessDB("AppMail.ListDelHiddenBeforeDayMailIds", ht0);
    DataTable dt = dbr.getResultTable(0);
    if (dt == null) {
      return;
    }
    int mailidn = dt.getRowNum();
    if (log.isDebugEnabled())
    {
      log.debug("to be del mail num===" + mailidn);
    }

    int cc = mailidn / 20 + mailidn % 20 > 0 ? 1 : 0;
    for (int t = 0; t < cc; t++)
    {
      ArrayList mids = new ArrayList(20);
      for (int k = 0; k < 20; k++)
      {
        int i = t * 20 + k;
        if (i >= mailidn)
          break;
        long mid = dt.getRow(i).getValueInt64(0, -1L);
        if (mid > 0L)
        {
          mids.add(Long.valueOf(mid));
        }
      }
      if (log.isDebugEnabled())
      {
        log.debug("to be del mail ids num===" + mids.size());
      }
      deleteMailPermanentWithServer(username, mids);
    }
  }

  public boolean deleteMailPermanentWithServer(String username, long mailid)
    throws Exception
  {
    ArrayList mids = new ArrayList(1);
    mids.add(Long.valueOf(mailid));
    int c = deleteMailPermanentWithServer(username, mids);
    return c > 0;
  }

  public int deleteMailPermanentWithServer(String username, ArrayList<Long> mailids)
    throws Exception
  {
    List wmis = listMailWithNoDetailByIds(mailids);
    if (wmis == null) {
      return 0;
    }
    HashSet hs = new HashSet();
    for (WebMailItem wmi : wmis)
    {
      String msgid = wmi.getMsgUid();
      if (!Convert.isNullOrEmpty(msgid))
      {
        hs.add(msgid);
      }
    }

    AbstractMailServer.getInstance().delMailFromServerById(username, hs);

    return hs.size();
  }

  public void addOrUpdateMail(WebMailItem m)
    throws Exception
  {
    long mid = m.getMailId();
    if (mid > 0L)
    {
      updateMail(mid, m);
    }
    else
    {
      addMail(m);
    }
  }

  public WebMailItem getMailById(long mid)
    throws GdbException, Exception
  {
    return (WebMailItem)GDB.getInstance().getXORMObjByPkId(WebMailItem.class, Long.valueOf(mid), true);
  }

  public WebMailItem getMailWithNoDetailById(long mid) throws GdbException, Exception
  {
    return (WebMailItem)GDB.getInstance().getXORMObjByPkId(WebMailItem.class, Long.valueOf(mid), false);
  }

  public List<WebMailItem> listMailWithNoDetailByIds(ArrayList<Long> mids) throws GdbException, Exception
  {
    if ((mids == null) || (mids.size() <= 0)) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(mids.get(0));
    int s = mids.size();
    for (int i = 1; i < s; i++)
    {
      sb.append(',').append(mids.get(i));
    }
    return GDB.getInstance().listXORMAsObjList(WebMailItem.class, 
      "MailId in (" + sb.toString() + ")", null, 0, -1);
  }

  public InputStream getMailDetailInputStream(long mid)
    throws Exception
  {
    File f = GDB.getInstance().getXORMFile(WebMailItem.class, "MailCont", mid);
    if (f == null) {
      return null;
    }
    if (!f.exists()) {
      return null;
    }
    return new FileInputStream(f);
  }

  public byte[] getMailDetail(long mid)
    throws Exception
  {
    File f = GDB.getInstance().getXORMFile(WebMailItem.class, "MailCont", mid);
    if (f == null) {
      return null;
    }
    if (!f.exists()) {
      return null;
    }
    int flen = (int)f.length();
    byte[] rets = new byte[flen];
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(f);
      fis.read(rets);
      return rets;
    }
    finally
    {
      if (fis != null)
        fis.close();
    }
  }

  public WebMailItem getMailByIdNoDetail(long mid)
    throws GdbException, Exception
  {
    return (WebMailItem)GDB.getInstance().getXORMObjByPkId(WebMailItem.class, Long.valueOf(mid), false);
  }

  public WebMailItemList searchMail(String key)
  {
    return null;
  }

  public void setRemovedByClient(long mid, boolean b) throws ClassNotFoundException, GdbException, Exception
  {
    GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(mid), WebMailItem.class, new String[] { "RemovedByClient" }, new Object[] { Boolean.valueOf(b) });
  }

  private Date isTodayAndGetStart(Date d)
  {
    if (d == null) {
      return null;
    }

    Calendar today = Calendar.getInstance();
    Calendar dc = Calendar.getInstance();
    dc.setTime(d);

    int today_y = today.get(1);
    int today_m = today.get(2);
    int today_d = today.get(5);

    int d_y = dc.get(1);
    int d_m = dc.get(2);
    int d_d = dc.get(5);

    if (today_y > d_y) {
      return null;
    }
    if ((today_y == d_y) && (today_m > d_m)) {
      return null;
    }
    if ((today_y == d_y) && (today_m == d_m) && (today_d > d_d)) {
      return null;
    }
    today.set(11, 0);
    today.set(12, 0);
    today.set(13, 0);
    return today.getTime();
  }

  public int[] recvMailsFromServer(String username, Date startd, Date endd, boolean is_sent_box, StringBuilder failedreson)
    throws Exception
  {
    WebMailUserLog wmul = (WebMailUserLog)GDB.getInstance().getXORMObjByUniqueColValue(WebMailUserLog.class, "UserName", username, true);
    boolean normal_recv = false;
    Date normal_this_recv_date = new Date();
    if ((startd == null) && (endd == null) && (!is_sent_box))
    {
      normal_recv = true;

      if (wmul != null) {
        startd = wmul.getLastRecvDate();
      }

      Date td = isTodayAndGetStart(startd);
      if (td != null)
      {
        startd = td;

        Calendar c = Calendar.getInstance();
        c.set(5, c.get(5) - 7);
        startd = c.getTime();
      }

    }

    int[] rets = new int[2];
    try
    {
      rets[0] = AbstractMailServer.getInstance().recvNewMail(username, startd, endd, is_sent_box, failedreson);
      if (rets[0] < 0)
      {
        return null;
      }

      if (normal_recv)
      {
        if (wmul != null)
        {
          wmul.lastRecvDate = normal_this_recv_date;
          GDB.getInstance().updateXORMObjToDB(Long.valueOf(wmul.getAutoId()), wmul);
        }
        else
        {
          wmul = new WebMailUserLog(username, normal_this_recv_date);
          GDB.getInstance().addXORMObjWithNewId(wmul);
        }

      }

    }
    catch (Exception eee)
    {
      rets[0] = 0;
    }

    rets[1] = getUserMailCount(username, WebMailItem.State.recved_new);
    return rets;
  }

  public int getUserMailCount(String username, WebMailItem.State st)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@UserName", username);
    if (st != null)
      ht.put("@MailState", Integer.valueOf(st.getValue()));
    DBResult dbr = GDB.getInstance().accessDB("AppMail.GetMailCountByUser", ht);
    return dbr.getResultFirstColOfFirstRowNumber().intValue();
  }

  public List<Folder> listAllFolderByUser(String username)
    throws Exception
  {
    StringBuilder failedreson = new StringBuilder();
    WBMailStore wbms = WBMailManager.getInstance().getUserMailStore(username, failedreson);
    if (wbms == null)
    {
      return null;
    }
    if (wbms == null)
    {
      return null;
    }

    return wbms.listAllFolders();
  }

  public List<WebMailItem> getMailByState(String username, int state, String from, String to, String subject)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@UserName", username);
    ht.put("@MailState", Integer.valueOf(state));

    if ((from != null) && (!from.equals("")))
    {
      ht.put("$From", from);
    }

    if ((to != null) && (!to.equals("")))
    {
      ht.put("$To", to);
    }

    if ((subject != null) && (!subject.equals("")))
    {
      ht.put("$Subject", subject);
    }
    return GDB.getInstance().accessDBPageAsXORMObjList("AppMail.GetMailByState", ht, WebMailItem.class, 0, -1);
  }

  public HashMap<String, Integer> getUserMailNumByState(WebMailItem.State st)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@MailState", Integer.valueOf(st.getValue()));
    DBResult dbr = GDB.getInstance().accessDB("AppMail.GetUserMailNumByState", ht);

    DataTable dt = dbr.getResultFirstTable();
    int rn = dt.getRowNum();
    HashMap ret = new HashMap();
    for (int i = 0; i < rn; i++)
    {
      DataRow dr = dt.getRow(i);
      String un = (String)dr.getValue("UserName");
      int num = ((Number)dr.getValue("Num")).intValue();
      ret.put(un, Integer.valueOf(num));
    }
    return ret;
  }

  public DataTable GetMailByInOut(String mailapp, String mailappref) throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@MailApp", mailapp);
    ht.put("@MailAppRef", mailappref);
    DBResult dbr = GDB.getInstance().accessDBPage("AppMail.GetMailByInOut", ht, 0, -1);
    return dbr.getResultFirstTable();
  }

  public List<WebMailItem> GetMailListByInOut(String mailapp, String mailappref) throws Exception
  {
    DataTable dt = GetMailByInOut(mailapp, mailappref);
    if (dt == null) {
      return null;
    }
    ArrayList rets = new ArrayList(dt.getRowNum());
    return (List)DBResult.transTable2XORMObjList(WebMailItem.class, dt);
  }

  public DataTable GetMailByMap(Date sdate, Date edate, String username, int state, String mailapp, String mailappref, String from, String to, String subject) throws Exception
  {
    Hashtable ht = new Hashtable();
    if (sdate != null)
    {
      ht.put("@StartDate", sdate);
    }
    if (edate != null)
    {
      ht.put("@EndDate", edate);
    }
    if ((from != null) && (!from.equals("")))
    {
      ht.put("$From", from);
    }
    if ((to != null) && (!to.equals("")))
    {
      ht.put("$To", to);
    }
    if ((subject != null) && (!subject.equals("")))
    {
      ht.put("$Subject", subject);
    }
    if ((username != null) && (!username.equals("")))
    {
      ht.put("@UserName", username);
    }
    ht.put("@MailState", Integer.valueOf(state));
    ht.put("@MailApp", mailapp);
    ht.put("@MailAppRef", mailappref);

    DBResult dbr = GDB.getInstance().accessDBPage("AppMail.GetMailByMap", ht, 0, -1);
    return dbr.getResultFirstTable();
  }

  public List<WebMailItem> GetMailListByMap(Date sdate, Date edate, String username, int state, String mailapp, String mailappref, String from, String to, String subject) throws Exception
  {
    DataTable dt = GetMailByMap(sdate, edate, username, state, mailapp, mailappref, from, to, subject);

    if (dt == null) {
      return null;
    }
    ArrayList rets = new ArrayList(dt.getRowNum());
    return (List)DBResult.transTable2XORMObjList(WebMailItem.class, dt);
  }

  public DataTable GetMailByUser(Date sdate, Date edate, int domainid, String username, int[] state, String from, String to, String subject)
    throws Exception
  {
    return GetMailByUser(sdate, edate, domainid, username, state, from, to, subject, 0, -1);
  }

  public DataTable GetMailByUser(Date sdate, Date edate, int domainid, String username, int[] state, String from, String to, String subject, int pageidx, int pagesize) throws Exception
  {
    Hashtable ht = new Hashtable();

    if ((from != null) && (!from.equals("")))
    {
      ht.put("$From", from);
    }
    if ((to != null) && (!to.equals("")))
    {
      ht.put("$To", to);
    }
    if ((subject != null) && (!subject.equals("")))
    {
      ht.put("$Subject", subject);
    }
    if (sdate != null)
    {
      ht.put("@StartDate", sdate);
    }
    if (edate != null)
    {
      ht.put("@EndDate", edate);
    }

    if (domainid <= 0)
      ht.put("@UserName", username);
    else {
      ht.put("@UserName", username + "@" + domainid);
    }
    if ((state != null) && (state.length > 0))
    {
      String tmps = state[0];
      for (int k = 1; k < state.length; k++)
        tmps = tmps + "," + state[k];
      ht.put("$MailState", tmps);
    }

    DBResult dbr = GDB.getInstance().accessDBPage("AppMail.GetMailByUser", ht, pageidx, pagesize);
    return dbr.getResultFirstTable();
  }

  public List<WebMailItem> GetMailListByUser(Date sdate, Date edate, int domainid, String username, int[] state, String from, String to, String subject)
    throws Exception
  {
    return GetMailListByUser(sdate, edate, domainid, username, state, from, to, subject, 0, 0);
  }

  public List<WebMailItem> GetMailListByUser(Date sdate, Date edate, int domainid, String username, int[] state, String from, String to, String subject, int pageidx, int pagesize) throws Exception
  {
    DataTable dt = GetMailByUser(sdate, edate, domainid, username, state, from, to, subject, pageidx, pagesize);
    if (dt == null) {
      return null;
    }

    return (List)DBResult.transTable2XORMObjList(WebMailItem.class, dt);
  }

  public List<WebMailItem> getMailListByMaidIds(Long[] mids)
    throws GdbException, Exception
  {
    if ((mids == null) || (mids.length <= 0)) {
      return new ArrayList(0);
    }
    Hashtable ht = new Hashtable();
    StringBuilder sb = new StringBuilder();
    sb.append(mids[0]);
    for (int i = 1; i < mids.length; i++)
    {
      sb.append(',').append(mids[i]);
    }

    ht.put("$MailIds", sb.toString());

    DBResult dbr = GDB.getInstance().accessDBPage("AppMail.GetMailByMailIds", ht, 0, -1);
    List wmi = (List)dbr.transTable2XORMObjList(0, WebMailItem.class);
    return wmi;
  }

  public List<WebMailAddr> searchMailAddr(String searchtxt, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    if (Convert.isNotNullEmpty(searchtxt)) {
      ht.put("$SearchTxt", searchtxt);
    }
    String tmps = RECVED_STS[0].getValue();
    for (int k = 1; k < RECVED_STS.length; k++)
      tmps = tmps + "," + RECVED_STS[k].getValue();
    ht.put("$MailState", tmps);

    DBResult dbr = GDB.getInstance().accessDBPage("AppMail.SearchMailAddr", ht, pageidx, pagesize);
    DataTable dt = dbr.getResultFirstTable();
    if (dt == null) {
      return null;
    }
    int rn = dt.getRowNum();
    ArrayList rets = new ArrayList(rn);
    for (int i = 0; i < rn; i++)
    {
      DataRow dr = dt.getRow(i);
      String v = (String)dr.getValue(0);
      if (!Convert.isNullOrEmpty(v))
      {
        InternetAddress ad = WebMailItem.transIntenetAddr(v);
        if (ad != null)
        {
          WebMailAddr wma = new WebMailAddr(ad.getPersonal(), ad.getAddress());
          rets.add(wma);
        }
      }
    }
    return rets;
  }

  public List<WebMailAppMap> GetMailMapByCase(long mid, String app_name, String app_ref)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@MailId", Long.valueOf(mid));
    ht.put("@MailAppName", app_name);
    ht.put("@MailAppRef", app_ref);
    List wmams = GDB.getInstance().accessDBPageAsXORMObjList("AppMail.GetMailByNameRef", ht, WebMailAppMap.class, 0, -1);

    return wmams;
  }

  public Date getUserRecvedMailMaxDate(String usern)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();

    ht.put("@UserName", usern);

    String tmps = RECVED_STS[0].getValue();
    for (int k = 1; k < RECVED_STS.length; k++)
      tmps = tmps + "," + RECVED_STS[k].getValue();
    ht.put("$MailState", tmps);

    DBResult dbr = GDB.getInstance().accessDB("AppMail.GetMailMaxDateByUser", ht);
    return (Date)dbr.getResultFirstColumnOfFirstRow();
  }

  public Date getUserLastRecvedMailDate(String usern) throws GdbException, Exception
  {
    List ll = GDB.getInstance().listXORMAsObjList(WebMailUserLog.class, "UserName='" + usern + "'", null, -1, -1);
    if ((ll == null) || (ll.size() <= 0)) {
      return null;
    }
    WebMailUserLog wmu = (WebMailUserLog)ll.get(0);
    return wmu.getLastRecvDate();
  }

  public void deleteMap(long mid)
    throws GdbException, Exception
  {
    GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(mid), WebMailAppMap.class);
  }

  public boolean deleteMailById(int domainid, String username, long mid)
    throws GdbException, Exception
  {
    WebMailItem wmi = (WebMailItem)GDB.getInstance().getXORMObjByPkId(WebMailItem.class, Long.valueOf(mid), false);
    if (wmi == null) {
      return false;
    }
    String nn = username;
    if (domainid > 0)
      nn = nn + "@" + domainid;
    if (!nn.equals(wmi.getUserName())) {
      return false;
    }
    return GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(mid), WebMailItem.class);
  }

  public void deleteMailByIds(int domainid, String username, long[] mids) throws GdbException, Exception
  {
    if ((mids == null) || (mids.length <= 0)) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(mids[0]);
    for (int i = 1; i < mids.length; i++)
      sb.append(',').append(mids[i]);
    Hashtable ht = new Hashtable();
    ht.put("$MailIds", sb.toString());
    if (username != null)
    {
      if (domainid <= 0)
        ht.put("@UserName", username);
      else {
        ht.put("@UserName", username + "@" + domainid);
      }
    }
    GDB.getInstance().accessDB("AppMail.DeleteMailStateByMailIds", ht);
  }

  public void setRemovedByClient(int domainid, String username, long[] mids, boolean removed_by_client)
    throws Exception
  {
    if ((mids == null) || (mids.length <= 0)) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(mids[0]);
    for (int i = 1; i < mids.length; i++)
      sb.append(',').append(mids[i]);
    Hashtable ht = new Hashtable();
    ht.put("$MailIds", sb.toString());
    if (username != null)
    {
      if (domainid <= 0)
        ht.put("@UserName", username);
      else {
        ht.put("@UserName", username + "@" + domainid);
      }
    }
    ht.put("@RemovedByClient", Boolean.valueOf(removed_by_client));

    GDB.getInstance().accessDB("AppMail.SetRemoveByClientByMailIds", ht);
  }

  public void addMap(WebMailAppMap m) throws GdbException, Exception
  {
    GDB.getInstance().addXORMObjWithNewId(m);
  }

  public List<WebMailItem> GetMailByDate(Date sdate, Date edate) throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@StartDate", sdate);
    ht.put("@EndDate", edate);
    List wmis = GDB.getInstance().accessDBPageAsXORMObjList("AppMail.GetMailByDate", ht, WebMailItem.class, 0, -1);
    return wmis;
  }

  public List<WebMailItem> SearchMail(String txt, String un) throws Exception
  {
    return SearchMail(txt, un, 0, -1);
  }

  public List<WebMailItem> SearchMail(String txt, String un, int pageidx, int pagesize) throws Exception
  {
    Hashtable ht = new Hashtable();

    if (Convert.isNotNullTrimEmpty(un)) {
      ht.put("@UserName", un);
    }
    ht.put("$Text", txt);
    List wmis = GDB.getInstance().accessDBPageAsXORMObjList("AppMail.SearchMail", ht, WebMailItem.class, pageidx, pagesize);
    return wmis;
  }

  public List<WebMailItem> searchMailPro(String username, String title, String sendt, String recv, WebMailItem.State[] sts, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();

    if (Convert.isNotNullTrimEmpty(username)) {
      ht.put("@UserName", username);
    }
    if (Convert.isNotNullTrimEmpty(title)) {
      ht.put("$title", title);
    }
    if (Convert.isNotNullTrimEmpty(sendt)) {
      ht.put("$sender", sendt);
    }
    if (Convert.isNotNullTrimEmpty(recv))
      ht.put("$recver", recv);
    if ((sts == null) || (sts.length <= 0))
    {
      ht.put("$StateStr", "0,1,2,3");
    }
    else
    {
      StringBuilder sb = new StringBuilder();
      sb.append(sts[0].getValue());
      for (int i = 1; i < sts.length; i++)
      {
        sb.append(',').append(sts[i].getValue());
      }
      ht.put("$StateStr", sb.toString());
    }

    List wmis = GDB.getInstance().accessDBPageAsXORMObjList("AppMail.SearchMailPro", ht, WebMailItem.class, pageidx, pagesize);
    return wmis;
  }

  public List<WebMailItem> getMailsByAddress(String address)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("$address", address);
    List wmis = GDB.getInstance().accessDBPageAsXORMObjList("AppMail.GetMailsByAddress", ht, WebMailItem.class, 0, -1);
    return wmis;
  }

  public List listMailIds(String username, WebMailItem.State[] sts)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@UserName", username);
    String ststr = sts[0].getValue();
    for (int k = 1; k < sts.length; k++)
    {
      ststr = ststr + "," + sts[k].getValue();
    }
    ht.put("$MailState", ststr);

    DBResult dbr = GDB.getInstance().accessDB("AppMail.ListMailIds", ht);
    return dbr.getResultFirstTable().getColumnValuesAsList(0);
  }

  public List listMailIdsWithCheckClientRemove(String username, WebMailItem.State[] sts)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@UserName", username);
    String ststr = sts[0].getValue();
    for (int k = 1; k < sts.length; k++)
    {
      ststr = ststr + "," + sts[k].getValue();
    }
    ht.put("$MailState", ststr);

    DBResult dbr = GDB.getInstance().accessDB("AppMail.ListMailIds_CheckClient", ht);
    return dbr.getResultFirstTable().getColumnValuesAsList(0);
  }
}
