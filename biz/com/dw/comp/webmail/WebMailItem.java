package com.dw.comp.webmail;

import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.user.UserProfile;
import com.dw.web_ui.WebUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

@XORMClass(table_name="webmail")
public class WebMailItem
{
  public static final String MAILEXTPRIFIX = "Tomato-";
  private static ILogger log = LoggerManager.getLogger(WebMailItem.class);

  @XORMProperty(name="MailId", has_col=true, is_pk=true, is_auto=true)
  long mailId = -1L;

  @XORMProperty(name="MsgUid", has_col=true, has_idx=true, max_len=100, is_unique_idx=true, auto_truncate=true, order_num=10)
  String msgUid = null;

  String mailName = null;

  @XORMProperty(name="UserName", has_col=true, has_idx=true, max_len=50, order_num=20)
  String userName = null;

  @XORMProperty(name="MailFrom", has_col=true, max_len=50, auto_truncate=true, order_num=30)
  String mailFrom = null;

  @XORMProperty(name="MailTo", has_col=true, max_len=100, auto_truncate=true, order_num=40)
  String mailTo = null;

  @XORMProperty(name="MailCc", has_col=true, max_len=100, auto_truncate=true, order_num=50)
  String mailCC = null;

  @XORMProperty(name="MailBcc", has_col=true, max_len=100, auto_truncate=true, order_num=60)
  String mailBCC = null;

  @XORMProperty(name="MailSubject", has_col=true, max_len=200, auto_truncate=true, order_num=70)
  String mailSubject = null;

  @XORMProperty(name="MailDate", has_col=true, has_idx=true, order_num=80)
  Date mailDate = null;

  State state = State.draft;

  @XORMProperty(name="RemovedByClient", has_col=true, order_num=93)
  boolean removedByClient = false;

  @XORMProperty(name="MailSendErrorNum", default_str_val="0", has_col=true, order_num=95)
  int sendErrorNum = 0;

  @XORMProperty(name="MailAttachNum", has_col=true, max_len=50, order_num=100)
  int mailAttachNum = 0;

  @XORMProperty(name="MailSize", has_col=true, order_num=110)
  int mailSize = -1;

  Level level = Level.normal;

  @XORMProperty(name="MailFolder", has_col=true, order_num=130)
  long folderId = -1L;

  @XORMProperty(name="ReplyMailId", has_col=true, default_str_val="0", order_num=140)
  long replyMailId = 0L;

  @XORMProperty(name="ForwardMailId", has_col=true, default_str_val="0", order_num=150)
  long forwardMailId = 0L;

  @XORMProperty(name="MailBeforeDelState", has_col=true, default_str_val="-1", order_num=160)
  int beforeDelState = -1;
  String bodyHtml;
  String bodyTxt;
  InternetAddress notificationTo = null;

  HashMap<String, String> headerExt = new HashMap();
  HashMap<String, WebMailAttachment> filename2cont;
  transient boolean isInnerMailInbox = false;

  byte[] jamesMsgHeader = null;
  byte[] jamesMsgBody = null;

  private boolean b_Pure_BodyHtml = false;

  transient MimeMessage mimeMsg = null;

  @XORMProperty(name="MailState", has_col=true, order_num=90)
  private int get_State()
  {
    if (this.state == null) {
      return State.draft.getValue();
    }
    return this.state.getValue();
  }

  private void set_State(int v) {
    this.state = State.valueOf(v);
  }

  @XORMProperty(name="MailLevel", has_col=true, order_num=120)
  private int get_Level()
  {
    return this.level.getValue();
  }

  private void set_Level(int v) {
    this.level = Level.valueOf(v);
  }

  @XORMProperty(name="MailCont", store_as_file=true, read_on_demand=true, order_num=140)
  private byte[] get_MailCont()
    throws Exception
  {
    try
    {
      return toMimeMsgByteArray();
    }
    catch (Exception e)
    {
      if (log.isErrorEnabled())
        log.error(e);
    }
    return null;
  }

  private void set_MailCont(byte[] cont)
    throws Exception
  {
    if ((cont == null) || (cont.length <= 0)) {
      return;
    }
    try
    {
      fromMimeMsgByteArray(cont);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public WebMailItem()
  {
  }

  public WebMailItem(String username, String msguid)
  {
    this.msgUid = msguid;
    this.userName = username;
    this.state = State.recved_new;
  }

  public WebMailItem(long replay_id, long forward_id, String from, String to, String cc, String bcc, String username, String subject, Date date, State state, int attchnum, int size, int level, String bodytxt, HashMap<String, WebMailAttachment> filename2cont)
    throws MessagingException
  {
    if (Convert.isNullOrEmpty(from))
    {
      throw new IllegalArgumentException("no from input!");
    }

    this.replyMailId = replay_id;
    this.forwardMailId = forward_id;
    this.mailFrom = from;
    this.mailTo = to;
    this.mailCC = cc;
    this.mailBCC = bcc;
    this.userName = username;
    this.mailSubject = subject;
    this.mailDate = date;
    this.state = state;
    this.mailAttachNum = attchnum;
    if (filename2cont != null)
      this.mailAttachNum = filename2cont.size();
    this.mailSize = size;
    set_Level(level);
    this.bodyHtml = bodytxt;
    this.filename2cont = filename2cont;
  }

  public WebMailItem(String from, String to, String subject, Date date, String content)
  {
    this.mailFrom = from;
    this.mailTo = to;
    this.mailSubject = subject;
    this.mailDate = date;
    this.bodyHtml = content;
  }

  public WebMailItem(String usern, long mid, String msgid, String from, String to, String subject, Date date, State st, int size, int attachn, Level lvl, byte[] msghead)
  {
    this.userName = usern;
    this.mailId = mid;
    this.msgUid = msgid;
    this.mailFrom = from;
    this.mailTo = to;
    this.mailSubject = subject;
    this.mailDate = date;

    this.state = st;
    this.mailSize = size;
    this.mailAttachNum = attachn;
    this.level = lvl;

    this.jamesMsgHeader = msghead;
  }

  public WebMailItem(byte[] mailCont)
    throws Exception
  {
    set_MailCont(mailCont);
  }

  public WebMailItem(InputStream ins)
    throws Exception
  {
    fromMimeMsgInputStream(ins);
  }

  public WebMailItem(long id)
  {
    this.mailId = id;

    this.removedByClient = WebMailManager.getInstance().recv_new_default_removed_by_client;
  }

  public WebMailItem(long id, byte[] cont)
  {
  }

  public byte[] getCont()
    throws Exception
  {
    return get_MailCont();
  }

  public Message toJavaMessage()
  {
    return null;
  }

  public long getMailId()
  {
    return this.mailId;
  }

  public File getMailFile() throws Exception
  {
    return GDB.getInstance().getXORMFile(WebMailItem.class, "MailCont", this.mailId);
  }

  public String getMailIdWithInnerBox()
  {
    if (this.isInnerMailInbox) {
      return "i_" + this.mailId;
    }
    return this.mailId;
  }

  public String getJamesMailName()
  {
    return this.mailName;
  }

  public void setJamesMailName(String mailn)
  {
    this.mailName = mailn;
  }

  public String getMsgUid()
  {
    return this.msgUid;
  }

  public void setMsgUid(String muid)
  {
    this.msgUid = muid;
  }

  public String getUserName()
  {
    return this.userName;
  }

  public void setUserName(String un)
  {
    this.userName = un;
  }

  public String getMailFrom()
  {
    if (this.mailFrom == null) {
      return "";
    }
    return this.mailFrom;
  }

  public void setRecverNotifyToSender(boolean b)
    throws Exception
  {
    if (!b)
    {
      this.notificationTo = null;
      return;
    }

    if (Convert.isNullOrEmpty(this.mailFrom)) {
      return;
    }
    this.notificationTo = transIntenetAddr(this.mailFrom);
  }

  public boolean hasRecverNotifySender()
  {
    return this.notificationTo != null;
  }

  public boolean isRecvedMail()
  {
    if (this.isInnerMailInbox) {
      return true;
    }
    if (this.state == State.delete)
    {
      if (getBeforeDeletedState() == State.recved_seen) {
        return true;
      }
    }
    return false;
  }

  public String getMailFromShort() throws Exception
  {
    if (Convert.isNullOrTrimEmpty(this.mailFrom))
      return "";
    InternetAddress from_addr = transIntenetAddr(this.mailFrom);
    if (from_addr == null)
      return this.mailFrom;
    String tmps = from_addr.getPersonal();
    if (Convert.isNotNullEmpty(tmps)) {
      return tmps;
    }
    tmps = from_addr.getAddress();
    if (Convert.isNullOrEmpty(tmps)) {
      return "";
    }
    int i = tmps.indexOf('@');
    if (i > 0)
      return tmps.substring(0, i);
    return tmps;
  }

  public String getMailTo()
  {
    if (this.mailTo == null) {
      return "";
    }
    return this.mailTo;
  }

  public String getMailBcc()
  {
    if (this.mailBCC == null) {
      return "";
    }
    return this.mailBCC;
  }

  public String getMailCc()
  {
    if (this.mailCC == null) {
      return "";
    }
    return this.mailCC;
  }

  public String getMailSenderAddr()
    throws Exception
  {
    InternetAddress ia = transIntenetAddr(this.mailFrom);
    if (ia == null) {
      return null;
    }
    return ia.getAddress();
  }

  public String[] getMailRecipientsAddr()
    throws Exception
  {
    ArrayList rets = new ArrayList();
    InternetAddress[] ias = transStringTOaddress(this.mailTo);
    if (ias != null)
    {
      for (InternetAddress ia : ias)
      {
        rets.add(ia.getAddress());
      }
    }

    ias = transStringTOaddress(this.mailCC);
    if (ias != null)
    {
      for (InternetAddress ia : ias)
      {
        rets.add(ia.getAddress());
      }
    }

    ias = transStringTOaddress(this.mailBCC);
    if (ias != null)
    {
      for (InternetAddress ia : ias)
      {
        rets.add(ia.getAddress());
      }
    }

    String[] ss = new String[rets.size()];
    rets.toArray(ss);
    return ss;
  }

  public String getMailSubject()
  {
    if (this.mailSubject == null) {
      return "";
    }
    return this.mailSubject;
  }

  public String getMailDateStr()
  {
    if (this.mailDate == null) {
      return "";
    }

    return Convert.toFullYMDHMS(this.mailDate);
  }

  public Date getMailDate()
  {
    return this.mailDate;
  }

  public int getMailAttachNum()
  {
    return this.mailAttachNum;
  }

  public HashMap<String, WebMailAttachment> getFileContent()
  {
    return this.filename2cont;
  }

  public int getMailSize()
  {
    return this.mailSize;
  }

  public String getMailSizeStr()
  {
    if (this.mailSize <= 0) {
      return "";
    }
    if (this.mailSize < 1024) {
      return "1KB";
    }
    int ms = this.mailSize / 1024;
    if (ms < 1024) {
      return ms + "KB";
    }
    ms /= 1024;

    return ms + "MB";
  }

  public void setMailSize(int ms)
  {
    this.mailSize = ms;
  }

  public Level getMailLevel()
  {
    return this.level;
  }

  public long getReplyMailId()
  {
    return this.replyMailId;
  }

  void setReplyMailId(long mid)
  {
    this.replyMailId = mid;
  }

  public long getForwardMailId()
  {
    return this.forwardMailId;
  }

  public State getBeforeDeletedState()
  {
    if (this.beforeDelState < 0) {
      return null;
    }
    return State.valueOf(this.beforeDelState);
  }

  public String getBodyHtml()
  {
    return this.bodyHtml;
  }

  public String getBodyTxt()
  {
    return this.bodyTxt;
  }

  public String getBodyShowHtml()
  {
    String bodyh = getBodyHtml();
    if (!Convert.isNullOrEmpty(bodyh)) {
      return bodyh;
    }
    if (!Convert.isNullOrEmpty(this.bodyTxt)) {
      return Convert.plainToHtml(this.bodyTxt);
    }
    return "";
  }

  public void setExtHeader(String n, String v)
  {
    this.headerExt.put(n, v);
  }

  public String getExtHeader(String n)
  {
    return null;
  }

  public void unsetExtHeader(String n)
  {
  }

  public void setAppMap(HashMap<String, String> ampm)
  {
    if (ampm == null) {
      return;
    }
    for (Map.Entry app : ampm.entrySet())
    {
      setExtHeader("appmap-" + (String)app.getKey(), (String)app.getValue());
    }
  }

  public HashMap<String, String> getAppMap()
  {
    if (this.headerExt == null) {
      return null;
    }
    HashMap ret = new HashMap();
    for (Map.Entry app : this.headerExt.entrySet())
    {
      if (((String)app.getKey()).startsWith("appmap-"))
      {
        String n = ((String)app.getKey()).substring("appmap-".length());
        ret.put(n, (String)app.getValue());
      }
    }
    return ret;
  }

  public byte[] toMimeMsgByteArray()
    throws Exception
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    MimeMessage mm = toMimeMsg();
    mm.writeTo(baos);
    return baos.toByteArray();
  }

  public void toMimeMsgOutputStream(OutputStream os)
    throws Exception
  {
    MimeMessage mm = toMimeMsg();
    mm.writeTo(os);
  }

  public static InternetAddress transIntenetAddr(String address)
    throws Exception
  {
    WebMailAddr wma = WebMailAddr.transIntenetAddr(address);
    if (wma == null) {
      return null;
    }
    InternetAddress internetAddr = new InternetAddress(wma.getEmail(), wma.getName(), "utf-8");
    return internetAddr;
  }

  public static String filterAddressList(String oldaddrs, String ignore_mail)
    throws Exception
  {
    if (Convert.isNullOrTrimEmpty(oldaddrs)) {
      return oldaddrs;
    }
    if (Convert.isNullOrTrimEmpty(ignore_mail)) {
      return oldaddrs;
    }
    InternetAddress ignoreadd = transIntenetAddr(ignore_mail);
    if (ignoreadd == null) {
      return oldaddrs;
    }
    InternetAddress[] addrs = transStringTOaddress(oldaddrs);
    if ((addrs == null) || (addrs.length == 0)) {
      return oldaddrs;
    }
    StringBuilder sb = new StringBuilder();
    for (InternetAddress tmpia : addrs)
    {
      String ad = tmpia.getAddress();
      if (!Convert.isNullOrEmpty(ad))
      {
        if (!ad.equals(ignoreadd.getAddress()))
        {
          sb.append(detransIntenetAddr(tmpia)).append(',');
        }
      }
    }
    return sb.toString();
  }

  public static InternetAddress[] transStringTOaddress(String mailaddr) throws Exception
  {
    if (Convert.isNullOrTrimEmpty(mailaddr)) {
      return null;
    }
    ArrayList rets = new ArrayList();

    StringTokenizer st = new StringTokenizer(mailaddr, ";,|，；");

    while (st.hasMoreTokens())
    {
      InternetAddress ia = transIntenetAddr(st.nextToken());
      if (ia != null)
      {
        rets.add(ia);
      }
    }
    InternetAddress[] toaddress = new InternetAddress[rets.size()];
    rets.toArray(toaddress);
    return toaddress;
  }

  public static ArrayList<WebMailAddr> transStringTOMailAddrs(String mailaddr)
  {
    if (Convert.isNullOrTrimEmpty(mailaddr)) {
      return null;
    }
    ArrayList rets = new ArrayList();

    StringTokenizer st = new StringTokenizer(mailaddr, ";,|�?);

    while (st.hasMoreTokens())
    {
      WebMailAddr ia = WebMailAddr.transIntenetAddr(st.nextToken());
      if (ia != null)
      {
        rets.add(ia);
      }
    }
    return rets;
  }

  public MimeMessage toMimeMsg() throws Exception
  {
    if (this.mimeMsg != null) {
      return this.mimeMsg;
    }
    MimeMessage message = new MimeMessage(null);

    InternetAddress fromAddress = transIntenetAddr(this.mailFrom);
    if (fromAddress != null) {
      message.setFrom(fromAddress);
    }
    if (this.mailDate != null) {
      message.setSentDate(this.mailDate);
    }
    message.setSubject(this.mailSubject, "UTF-8");
    if (this.headerExt.size() > 0) {
      for (Map.Entry h : this.headerExt.entrySet())
      {
        message.setHeader("Tomato-" + WebUtil.encodeHexUrl((String)h.getKey()), WebUtil.encodeHexUrl((String)h.getValue()));
      }
    }

    InternetAddress[] toaddress = transStringTOaddress(this.mailTo);
    if ((toaddress != null) && (toaddress.length > 0)) {
      message.setRecipients(Message.RecipientType.TO, toaddress);
    }
    if ((this.mailCC != null) && (!this.mailCC.equals("")))
    {
      InternetAddress[] toaddress3 = transStringTOaddress(this.mailCC);
      if ((toaddress3 != null) && (toaddress3.length > 0)) {
        message.setRecipients(Message.RecipientType.CC, toaddress3);
      }
    }
    if ((this.mailBCC != null) && (!this.mailBCC.equals("")))
    {
      InternetAddress[] toaddress4 = transStringTOaddress(this.mailBCC);
      if ((toaddress4 != null) && (toaddress4.length > 0)) {
        message.setRecipients(Message.RecipientType.BCC, toaddress4);
      }
    }
    if (this.notificationTo != null)
    {
      message.setHeader("Disposition-Notification-To", this.notificationTo.getAddress());
    }

    if (this.level == Level.High)
    {
      message.setHeader("Importance", "High");
    }
    else if (this.level == Level.low)
    {
      message.setHeader("Importance", "low");
    }

    if ((this.filename2cont == null) || (this.filename2cont.size() <= 0))
    {
      setPartContentHtmlTxt(this.state, message, this.bodyHtml);
    }
    else
    {
      MimeMultipart mm = new MimeMultipart();
      BodyPart mdp = new MimeBodyPart();

      setPartContentHtmlTxt(this.state, mdp, this.bodyHtml);

      mm.addBodyPart(mdp);

      for (Map.Entry fn2v : this.filename2cont.entrySet())
      {
        String fn = (String)fn2v.getKey();
        MimeBodyPart mdp1 = new MimeBodyPart();

        AttachDataSource fds = new AttachDataSource(fn, (WebMailAttachment)fn2v.getValue());
        DataHandler dh = new DataHandler(fds);
        mdp1.setFileName(MimeUtility.encodeText(fn, "UTF-8", null));
        mdp1.setDataHandler(dh);
        mm.addBodyPart(mdp1);
      }

      message.setContent(mm);
    }

    this.mimeMsg = message;
    return message;
  }

  private static void setPartContentHtmlTxt(State st, Part p, String htmlstr)
    throws MessagingException
  {
    if (htmlstr == null)
    {
      p.setContent("", "text/html;charset=UTF-8");
      return;
    }

    if ((st == State.draft) || (st == State.sending) || (st == State.sent))
    {
      String ptxt = null;
      try
      {
        ptxt = Convert.htmlToPlain(htmlstr);
        MimeMultipart mm = new MimeMultipart("alternative");
        BodyPart mdp = new MimeBodyPart();
        mdp.setContent(htmlstr, "text/html;charset=UTF-8");
        mm.addBodyPart(mdp);

        mdp = new MimeBodyPart();
        mdp.setContent(ptxt, "text/plain;charset=UTF-8");
        mm.addBodyPart(mdp);

        p.setContent(mm);
      }
      catch (Exception exp)
      {
        if (log.isErrorEnabled()) {
          log.error(exp);
        }
        p.setContent(htmlstr, "text/html;charset=UTF-8");
      }
    }
    else
    {
      p.setContent(htmlstr, "text/html;charset=UTF-8");
    }
  }

  public static String detransIntenetAddr(InternetAddress[] address)
  {
    if ((address == null) || (address.length == 0))
      return "";
    String str = "";
    str = detransIntenetAddr(address[0]) + ",";
    for (int i = 1; i < address.length; i++) {
      str = str + detransIntenetAddr(address[i]) + ",";
    }
    return str;
  }

  public static String detransIntenetAddr(InternetAddress address)
  {
    if (address == null) {
      return "";
    }
    String str = "";
    String str_per = address.getPersonal();
    String str_addr = address.getAddress();

    if (Convert.isNotNullEmpty(str_per))
    {
      str = str + str_per.trim();
    }

    if (Convert.isNotNullEmpty(str_addr))
      str = str + " [" + str_addr.trim() + "]";
    return str.trim();
  }

  public void fromMimeMsg(MimeMessage mm)
    throws Exception
  {
    this.mimeMsg = mm;

    InternetAddress[] address = (InternetAddress[])null;
    try
    {
      address = (InternetAddress[])mm.getFrom();

      if ((address != null) && (address.length > 0))
        this.mailFrom = detransIntenetAddr(address[0]);
    }
    catch (Exception localException)
    {
    }
    InternetAddress[] address1 = (InternetAddress[])null;
    try
    {
      address1 = (InternetAddress[])mm.getRecipients(Message.RecipientType.TO);
      if ((address1 != null) && (address1.length > 0))
      {
        this.mailTo = detransIntenetAddr(address1);
      }

    }
    catch (Exception localException1)
    {
    }

    try
    {
      address1 = (InternetAddress[])mm.getRecipients(Message.RecipientType.BCC);
      if ((address1 != null) && (address1.length > 0))
      {
        this.mailBCC = detransIntenetAddr(address1);
      }

    }
    catch (Exception localException2)
    {
    }

    try
    {
      address1 = (InternetAddress[])mm.getRecipients(Message.RecipientType.CC);
      if ((address1 != null) && (address1.length > 0))
      {
        this.mailCC = detransIntenetAddr(address1);
      }

    }
    catch (Exception localException3)
    {
    }

    String subject = mm.getSubject();
    if (subject == null) {
      subject = "";
    }

    this.mailSubject = subject;

    this.mailDate = mm.getSentDate();
    this.mailSize = mm.getSize();

    String[] dnts = mm.getHeader("Disposition-Notification-To");
    if ((dnts != null) && (dnts.length > 0))
    {
      InternetAddress[] tmpias = InternetAddress.parse(dnts[0]);
      if ((tmpias != null) && (tmpias.length > 0)) {
        this.notificationTo = tmpias[0];
      }
    }
    String[] importances = mm.getHeader("Importance");
    if ((importances != null) && (importances.length > 0))
    {
      if ("high".equalsIgnoreCase(importances[0]))
        this.level = Level.High;
      else if ("low".equalsIgnoreCase(importances[0])) {
        this.level = Level.low;
      }
    }

    for (Enumeration en = mm.getAllHeaders(); en.hasMoreElements(); )
    {
      Header h = (Header)en.nextElement();
      if (h.getName().startsWith("Tomato-"))
      {
        String n = h.getName().substring("Tomato-".length());
        String v = h.getValue();
        this.headerExt.put(WebUtil.decodeHexUrl(n), WebUtil.decodeHexUrl(v));
      }
    }
    Object o = mm.getContent();

    if ((o instanceof InputStream))
    {
      String fn = mm.getFileName();

      if (!Convert.isNullOrEmpty(fn))
      {
        fn = MimeUtility.decodeText(fn);

        String[] contids = mm.getHeader("Content-ID");
        String contid = "";
        if ((contids != null) && (contids.length > 0)) {
          contid = contids[0];
        }

        if (contid.startsWith("<"))
          contid = contid.substring(1);
        if (contid.endsWith(">")) {
          contid = contid.substring(0, contid.length() - 1);
        }
        InputStream bais = (InputStream)o;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] b = new byte[4096];
        int tmpl = 0;
        while ((tmpl = bais.read(b)) > 0)
        {
          baos.write(b, 0, tmpl);
        }

        if (this.filename2cont == null) {
          this.filename2cont = new HashMap();
        }
        WebMailAttachment wma = new WebMailAttachment(fn, contid, baos.toByteArray());
        this.filename2cont.put(fn, wma);
        this.mailAttachNum += 1;
        return;
      }
    }

    if ((o instanceof String))
    {
      if (mm.isMimeType("text/html"))
        this.bodyHtml = ((String)o);
      else {
        this.bodyTxt = ((String)o);
      }
      return;
    }

    if ((o instanceof MimeMultipart))
    {
      MimeMultipart mmt = (MimeMultipart)o;
      int c = mmt.getCount();

      for (int i = 0; i < c; i++)
      {
        MimeBodyPart bp = (MimeBodyPart)mmt.getBodyPart(i);

        String disposition = bp.getDisposition();

        String fn = bp.getFileName();

        if (("attachment".equals(disposition)) || (Convert.isNotNullEmpty(fn)))
        {
          if (fn != null)
          {
            fn = MimeUtility.decodeText(fn);
          }

          String[] contids = bp.getHeader("Content-ID");
          String contid = "";
          if ((contids != null) && (contids.length > 0)) {
            contid = contids[0];
          }

          if (contid.startsWith("<"))
            contid = contid.substring(1);
          if (contid.endsWith(">")) {
            contid = contid.substring(0, contid.length() - 1);
          }
          InputStream bais = bp.getInputStream();

          ByteArrayOutputStream baos = new ByteArrayOutputStream();

          byte[] b = new byte[4096];
          int tmpl = 0;
          while ((tmpl = bais.read(b)) > 0)
          {
            baos.write(b, 0, tmpl);
          }

          b = baos.toByteArray();
          if (Convert.isNullOrEmpty(fn))
          {
            ByteArrayInputStream bbis = new ByteArrayInputStream(b);
            MimeMessage tmpmm = new MimeMessage(null, bbis);
            fn = tmpmm.getSubject();
            if (fn != null) {
              fn = MimeUtility.decodeText(fn) + ".eml";
            }
          }
          if (Convert.isNullOrEmpty(fn))
          {
            int p = subject.indexOf(':');
            if (p > 0)
            {
              fn = MimeUtility.decodeText(subject.substring(p + 1)) + "_" + i + ".eml";
            }
            else
            {
              fn = "attach_mail_" + i + ".eml";
            }

          }

          if (this.filename2cont == null) {
            this.filename2cont = new HashMap();
          }
          WebMailAttachment wma = new WebMailAttachment(fn, contid, b);
          if (this.filename2cont.containsKey(fn))
            this.filename2cont.put(i + "_" + fn, wma);
          else
            this.filename2cont.put(fn, wma);
          this.mailAttachNum += 1;
        }
        else
        {
          Object oo = bp.getContent();
          if ((oo instanceof String))
          {
            if ("inline".equals(disposition))
            {
              if (bp.isMimeType("text/html"))
              {
                if (Convert.isNullOrEmpty(this.bodyHtml))
                  this.bodyHtml = ((String)oo);
                else {
                  this.bodyHtml += (String)oo;
                }

              }
              else if (Convert.isNullOrEmpty(this.bodyTxt))
                this.bodyTxt = ((String)oo);
              else {
                this.bodyTxt += (String)oo;
              }

            }
            else if (bp.isMimeType("text/html"))
              this.bodyHtml = ((String)oo);
            else {
              this.bodyTxt = ((String)oo);
            }

          }
          else if ((oo instanceof MimeMultipart))
          {
            MimeMultipart tmpmm = (MimeMultipart)oo;
            processBodyMM(tmpmm);
          }
          else if ((oo instanceof MimeMessage))
          {
            MimeMessage mm0 = (MimeMessage)oo;

            String[] contids = mm0.getHeader("Content-ID");
            String contid = "";
            if ((contids != null) && (contids.length > 0)) {
              contid = contids[0];
            }

            if (contid.startsWith("<"))
              contid = contid.substring(1);
            if (contid.endsWith(">")) {
              contid = contid.substring(0, contid.length() - 1);
            }
            if (contid == "") {
              contid = "inline_mimemsg_cont_" + this.mailAttachNum;
            }
            if (this.filename2cont == null) {
              this.filename2cont = new HashMap();
            }
            InputStream bais = bp.getInputStream();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] b = new byte[4096];
            int tmpl = 0;
            while ((tmpl = bais.read(b)) > 0)
            {
              baos.write(b, 0, tmpl);
            }

            String fn0 = mm0.getSubject();
            if (fn0 != null)
              fn0 = MimeUtility.decodeText(fn0) + ".eml";
            else
              fn0 = "inline_mail_" + this.mailAttachNum + ".eml";
            WebMailAttachment wma = new WebMailAttachment(fn0, contid, baos.toByteArray());
            this.filename2cont.put(fn0, wma);
            this.mailAttachNum += 1;
          }
        }
      }
    }
  }

  private void processBodyMM(MimeMultipart mm) throws MessagingException, IOException
  {
    int bcount = mm.getCount();
    for (int j = 0; j < bcount; j++)
    {
      BodyPart tmpbp = mm.getBodyPart(j);
      String disp = tmpbp.getDisposition();
      if (tmpbp.isMimeType("text/html"))
      {
        if ("inline".equals(disp))
        {
          if (Convert.isNullOrEmpty(this.bodyHtml))
            this.bodyHtml = tmpbp.getContent().toString();
          else {
            this.bodyHtml += tmpbp.getContent().toString();
          }
        }
        else {
          this.bodyHtml = tmpbp.getContent().toString();
        }
      }
      else if (tmpbp.isMimeType("text/plain"))
      {
        if ("inline".equals(disp))
        {
          if (Convert.isNullOrEmpty(this.bodyTxt))
            this.bodyTxt = tmpbp.getContent().toString();
          else {
            this.bodyTxt += tmpbp.getContent().toString();
          }
        }
        else {
          this.bodyTxt = tmpbp.getContent().toString();
        }
      }
      else
      {
        Object bpoo = tmpbp.getContent();

        if ((bpoo instanceof MimeMultipart))
        {
          processBodyMM((MimeMultipart)bpoo);
        }
        else if ((bpoo instanceof InputStream))
        {
          processInnerAttachFile(tmpbp);
        }
      }
    }
  }

  private void processInnerAttachFile(BodyPart bp)
    throws MessagingException, IOException
  {
    String fn = bp.getFileName();
    if (fn != null)
    {
      fn = MimeUtility.decodeText(fn);
    }

    String[] contids = bp.getHeader("Content-ID");
    String contid = "";
    if ((contids != null) && (contids.length > 0)) {
      contid = contids[0];
    }

    if (contid.startsWith("<"))
      contid = contid.substring(1);
    if (contid.endsWith(">")) {
      contid = contid.substring(0, contid.length() - 1);
    }
    if (Convert.isNullOrEmpty(fn))
      fn = contid;
    InputStream bais = bp.getInputStream();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] b = new byte[4096];
    int tmpl = 0;
    while ((tmpl = bais.read(b)) > 0)
    {
      baos.write(b, 0, tmpl);
    }

    if (this.filename2cont == null) {
      this.filename2cont = new HashMap();
    }
    WebMailAttachment wma = new WebMailAttachment(fn, contid, baos.toByteArray(), true);
    if (Convert.isNotNullEmpty(fn))
      this.filename2cont.put(fn, wma);
    this.mailAttachNum += 1;
  }

  public void fromMimeMsgByteArray(byte[] cont)
    throws Exception
  {
    ByteArrayInputStream contentInputs = new ByteArrayInputStream(cont);
    fromMimeMsgInputStream(contentInputs);
  }

  public void fromMimeMsgInputStream(InputStream is) throws Exception
  {
    MimeMessage m_mimeMessage = new MimeMessage(null, is);
    fromMimeMsg(m_mimeMessage);
  }

  public void writeContentToStream(OutputStream o)
  {
  }

  public void setMailState(State s)
  {
    this.state = s;
  }

  public int getMailState()
  {
    return this.state.getValue();
  }

  public boolean isRemoveByClient()
  {
    return this.removedByClient;
  }

  public void setRemoveByClient(boolean b)
  {
    this.removedByClient = b;
  }

  public State getState()
  {
    return this.state;
  }

  public int getMailSendErrorNum()
  {
    return this.sendErrorNum;
  }

  public void setMailSendErrorNum(int sen)
  {
    this.sendErrorNum = sen;
  }

  public boolean isSendFailed()
  {
    return this.sendErrorNum > 25;
  }

  public boolean isSendingNowWithErrorBefore()
  {
    if (this.sendErrorNum <= 0) {
      return true;
    }
    if (this.sendErrorNum > 25) {
      return false;
    }
    if (this.mailDate == null) {
      return true;
    }
    long tspan = System.currentTimeMillis() - this.mailDate.getTime();
    tspan /= 1000L;
    if (this.sendErrorNum < 10)
    {
      return tspan > 300 * this.sendErrorNum;
    }
    if (this.sendErrorNum < 20)
    {
      return tspan > 3000 + 600 * (this.sendErrorNum - 10);
    }

    return tspan > 9000 + 1200 * (this.sendErrorNum - 20);
  }

  public boolean isInnerMailInbox()
  {
    return this.isInnerMailInbox;
  }

  public void setIsInnerMailInbox(boolean b)
  {
    this.isInnerMailInbox = b;
  }

  public boolean checkUserCanRead(UserProfile up)
    throws Exception
  {
    if (up == null) {
      return false;
    }
    if (up.getUserNameWithDomainId().equalsIgnoreCase(getUserName())) {
      return true;
    }
    if (up.isAdministrator()) {
      return true;
    }

    WebMailPlugInfo[] wmpis = WebMailManager.getInstance().getAllMailPlugInfos();
    if ((wmpis == null) || (wmpis.length <= 0)) {
      return false;
    }
    for (WebMailPlugInfo wmpi : wmpis)
    {
      if (wmpi.checkUserCanRead(up, this)) {
        return true;
      }
    }
    return false;
  }

  public static String transMessageFlagStr(Flags.Flag f)
  {
    if (f == null) {
      return "";
    }
    if (f == Flags.Flag.ANSWERED)
      return "ANSWERED";
    if (f == Flags.Flag.DELETED)
      return "DELETED";
    if (f == Flags.Flag.DRAFT)
      return "DRAFT";
    if (f == Flags.Flag.FLAGGED)
      return "FLAGGED";
    if (f == Flags.Flag.RECENT)
      return "RECENT";
    if (f == Flags.Flag.SEEN) {
      return "SEEN";
    }
    if (f == Flags.Flag.USER) {
      return "USER";
    }
    return "";
  }

  public static enum State
  {
    recved_new(0), 
    recved_seen(1), 
    sending(2), 
    sent(3), 
    sent_error(4), 
    draft(5), 
    delete(6), 
    delete_hidden(7);

    private final int val;

    private State(int v)
    {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public static State valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return recved_new;
      case 1:
        return recved_seen;
      case 2:
        return sending;
      case 3:
        return sent;
      case 4:
        return sent_error;
      case 5:
        return draft;
      case 6:
        return delete;
      case 7:
        return delete_hidden;
      }
      return draft;
    }
  }

  public static enum Level
  {
    High(0), 
    normal(1), 
    low(2);

    private final int val;

    private Level(int v)
    {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public static Level valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return High;
      case 1:
        return normal;
      case 2:
        return low;
      }
      return normal;
    }
  }
}
