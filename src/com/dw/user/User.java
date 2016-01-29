package com.dw.user;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.*;
import com.dw.system.xmldata.xrmi.*;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

@XRmi(reg_name="security_user")
public class User implements IXmlDataable
{
	public enum UserState
	{
		Normal(0), Invalid(1), ResetPsw(3),New(4);

		private final int stVal;

		UserState(int v)
		{
			stVal = v;
		}

		public int getIntValue()
		{
			return stVal;
		}
		
		public static UserState getByIntValue(int v)
		{
			switch(v)
			{
			case 1:
				return Invalid;
			case 2:
				return Invalid;
			case 3:
				return ResetPsw;
			case 4:
				return New;
			default:
				return Normal;
			}
		}
	}

	// / <summary>
	// / UserProvider' Name
	// / </summary>
	String _ProviderName = null;

	/**
	 * 用户在Provider内的id号，之所以用String类型，是为了能够支持各种用户系统的挂接
	 */
	String userId;

	/**
	 * 用户在Provider内的唯一注册名称
	 */
	String userName;
	
	/**
	 * 用户所属的域id，域id在，app.xml中配置 system/domain节点
	 */
	int domainId = 0;
	/**
	 * 如果系统不区分中文名和英文名,则使用该名称
	 */
	String fullName ;

	String cnName;

	String enName;

	Timestamp creationDate;

	Timestamp lastLoginDate;

	Timestamp lastActivityDate;

	String email;

	UserState state = UserState.Normal;
	
	/**
	 * 对行政上用户所属部门，具体处理
	 */
	String belongToOrgNodeId = null;

	String usbKeyType = null ;
	String usbKeyTxt = null ;
	
	/**
	 * 要求属性的值都是基本类型
	 */
	Hashtable extProps = new Hashtable();
	
	
	UserExtItem extItem = null ;

	public User()
	{
	}

	public User(String userId,int domainid, String userName,String cnName, String enName,
			String email)
	{
		this(userId,domainid, userName,0,null, cnName, enName,email);
	}
	
	public User(String userId,String userName,String cnName, String enName,
			String email)
	{
		this(userId,0,userName,cnName,enName,email) ;
	}
	
	public User(String userId, String userName,int st,String fullname,
			String email)
	{
		this.userId = userId;
		this.userName = userName;
		this.state = UserState.getByIntValue(st);
		this.fullName = fullname ;
		this.email = email;
		// this.passwordQuestion = passwordQuestion;
	}
	
	public User(String userId,int domain, String userName,int st,String fullname, String cnName, String enName,
			String email)
	{
		this.userId = userId;
		this.domainId = domain;
		this.userName = userName;
		this.state = UserState.getByIntValue(st);
		this.fullName = fullname ;
		this.cnName = cnName;
		this.enName = enName;
		this.email = email;
		// this.passwordQuestion = passwordQuestion;
	}

	public User(String userId,int domain, String userName,int state,String cnName, String enName,
			String email,String belongtoorgnodeid, Date creationDate, Date lastLoginDate,
			Date lastActivityDate,String ext_strinfo)
	throws Exception
	{
		this(userId,domain, userName,state,null, cnName, enName,
				email,belongtoorgnodeid, creationDate, lastLoginDate,
				lastActivityDate,ext_strinfo);
	}
	
	public User(String userId,int domain, String userName,int state,String fullname, String cnName, String enName,
			String email,String belongtoorgnodeid, Date creationDate, Date lastLoginDate,
			Date lastActivityDate,String ext_strinfo)
		throws Exception
	{
		this.userId = userId;
		this.domainId = domain ;
		this.userName = userName;
		this.state = UserState.getByIntValue(state);
		this.fullName = fullname ;
		this.cnName = cnName;
		this.enName = enName;
		this.email = email;
		this.belongToOrgNodeId = belongtoorgnodeid;

		this.creationDate = new Timestamp(creationDate.getTime());
		this.lastLoginDate = new Timestamp(lastLoginDate.getTime());
		this.lastActivityDate = new Timestamp(lastActivityDate.getTime());
		
		if(ext_strinfo!=null&&!ext_strinfo.equals(""))
		{
			XmlData xd = XmlData.parseFromXmlStr(ext_strinfo);
			extProps = xmlDataToExtProp(xd);
		}
	}

	public String getProviderName()
	{
		return _ProviderName;
		// set { _ProviderName = value; }
	}

	// #region 与数据库交互属性-ORMapping
	//
	 private String get_UserId()
	 {
		 return userId;
	 }
	 private void set_UserId(String id)
	 {
		 userId = id;
	 }
	 
	 private String get_UserName()
	 {
		 return userName;
	 }
	 private void set_UserName(String un)
	 {
		 userName = un;
	 }
	 
	 
	 private int get_DomainId()
	 {
		 return this.domainId;
	 }
	 private void set_DomainId(int did)
	 {
		 domainId = did;
	 }
	 
	 private String get_FullName()
	 {
		 return fullName ;
	 }
	 private void set_FullName(String fn)
	 {
		 fullName =fn ;
	 }
	
	 private String get_CnName()
	 {
		 return cnName;
	 }
	 private void set_CnName(String un)
	 {
		 cnName = un;
	 }
	 
	 private String get_EnName()
	 {
		 return enName;
	 }
	 private void set_EnName(String un)
	 {
		 enName = un;
	 }
	 
	 private int get_State()
	 {
		 return state.getIntValue();
	 }
	 private void set_State(int st)
	 {
		 state = UserState.getByIntValue(st);
	 }
	 
	 private String get_BelongToOrgNodeId()
	 {
		 return this.belongToOrgNodeId ;
	 }
	 private void set_BelongToOrgNodeId(String st)
	 {
		 belongToOrgNodeId = st ;
	 }
	 
	 private String get_Email()
	 {
		 return email;
	 }
	 private void set_Email(String un)
	 {
		 email = un;
	 }
	 
	 private Timestamp get_CreationDate()
	 {
		 return creationDate;
	 }
	 private void set_CreationDate(Timestamp cd)
	 {
		 creationDate = cd;
	 }
	 
	 private Timestamp get_LastActivityDate()
	 {
		 return lastActivityDate;
	 }
	 private void set_LastActivityDate(Timestamp cd)
	 {
		 lastActivityDate = cd;
	 }
	 
	 private Timestamp get_LastLoginDate()
	 {
		 return lastLoginDate;
	 }
	 private void set_LastLoginDate(Timestamp cd)
	 {
		 lastLoginDate = cd;
	 }
	
	
	 private String get_UsbKeyEncType()
	 {
		 return usbKeyType;
	 }
	 private void set_UsbKeyEncType(String un)
	 {
		 usbKeyType = un;
	 }
	 
	 private String get_UsbKeyTxt()
	 {
		 return usbKeyTxt;
	 }
	 private void set_UsbKeyTxt(String un)
	 {
		 usbKeyTxt = un;
	 }
	 
	 private String get_ExtInfo()
	 {
		 return extPropToXmlData(extProps).toXmlString();
	 }
	 
	 private void set_ExtInfo(String ei) throws Exception
	 {
		 if(ei==null)
		 {
			 extProps = new Hashtable();
			 return;
		 }
		 
		 XmlData xd = XmlData.parseFromXmlStr(ei);
		 extProps = xmlDataToExtProp(xd) ;
	 }
	//
	 

	// DateTime _LastPasswordChangedDate
	// {
	// get { return lastPasswordChangedDate; }
	// set { lastPasswordChangedDate = value; }
	// }
	//
	// String _PasswordQuestion
	// {
	// get { return passwordQuestion; }
	// set { passwordQuestion = value; }
	// }
	//
	
	//
	// #endregion

	public String getUserId()
	{
		return userId;
	}

	public String getUserName()
	{
		return userName;
	}
	
	/**
	 * 获得用户所属的域id
	 * @return
	 */
	public int getUserDomainId()
	{
		return domainId ;
	}
	
	
	public String getUserNameWithDomainId()
	{
		if(domainId<=0)
			return userName ;
		
		return userName+"@"+domainId ;
	}
	
	/**
	 * 得到用户所属的域名-该域名和内部邮件服务器对应
	 * 
	 * @return
	 */
	public String getUserDomain()
	{
		DomainItem di = DomainManager.getInstance().getDomainItem(domainId) ;
		if(di==null)
			return null ;
		return di.getDomain();
	}

	public String getUniqueUserName()
	{
		if (this._ProviderName == null || _ProviderName.equals(""))
			return userName;

		return userName + "@" + _ProviderName;
	}

	public int hashCode()
	{
		return getUniqueUserName().hashCode();
	}
	
	public boolean equals(Object o)
	{
		return getUniqueUserName().equals(o);
	}
	
	public UserState getState()
	{
		return state;
	}

	public void setState(UserState us)
	{
		state = us;
	}
	
	public String getFullName()
	{
		return fullName ;
	}
	
	public void setFullName(String fn)
	{
		fullName = fn ;
	}

	public String getCnName()
	{
		return cnName;
	}

	public void setCnName(String v)
	{
		cnName = v;
	}

	public String getEnName()
	{
		return enName;
	}

	public void setEnName(String v)
	{
		enName = v;
	}
	
	
	public String getRealName()
	{
		if(Convert.isNotNullEmpty(cnName))
		{
			return this.cnName;
		}
		
		if(Convert.isNotNullEmpty(enName))
			return enName ;
		
		return null;
	}

	// public String getComment()
	// {
	// return comment;
	// }
	//        
	// public void setComment(String v)
	// {
	// comment = v;
	// }

	public Date getCreationDate()
	{
		return creationDate;
	}
	
	/**
	 * 上一次登录时间
	 * @return
	 */
	public Date getLastLoginDate()
	{
		return lastLoginDate ;
	}
	
	/**
	 * 上一次活动时间
	 * @return
	 */
	public Date getLastActivityDate()
	{
		return lastActivityDate;
	}

	public void setCreationDate(Date v)
	{
		creationDate = new Timestamp(v.getTime());
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String v)
	{
		email = v;
	}

	public String getUsbKeyType()
	{
		return usbKeyType ;
	}
	
	public String getUsbKeyTxt()
	{
		return usbKeyTxt ;
	}

	public Object getExtProp(String pn)
	{
		return extProps.get(pn);
	}

	public void setExtProp(String pn, Object o)
	{
		if (o == null)
			extProps.remove(pn);
		else
			extProps.put(pn, o);
	}
	
	public String toString()
	{
		return getUniqueUserName();
	}
	
	public String getBelongToOrgNodeId()
	{
		return this.belongToOrgNodeId;
	}
	
	public OrgNode getBelongToOrgNode() throws Exception
	{
		OrgManager om = OrgManager.getDefaultIns() ;
		if(this.belongToOrgNodeId!=null)
			return om.GetOrgNodeById(belongToOrgNodeId) ;
		
		List<OrgNode> ons = om.GetOrgNodesByUserName(getUserName()) ;
		if(ons==null||ons.size()<=0)
			return null ;
		
		return ons.get(0) ;
	}
	
//	public UserExtItem getUserExtItem()
//	{
//		return extItem ;
//	}
	
//	public OrgNode getBelongToOrgNode()
//		throws Exception
//	{
//		List<OrgNode> ons = OrgManager.getInstance().GetOrgNodesByUserName(getUniqueUserName());
//		if(ons==null||ons.size()<=0)
//			return null ;
//		
//		if(ons.size()==1)
//			return ons.get(0);
//		
//		for(OrgNode tmpn : ons)
//		{
//			if(tmpn.getOrgNodeId()==belongToOrgNodeId)
//				return tmpn;
//		}
//		
//		return ons.get(0);
//	}
	
	

	public static XmlData extPropToXmlData(Hashtable extp)
	{
		if (extp == null)
			return null;

		XmlData xd = new XmlData();
		for (Enumeration en = extp.keys(); en.hasMoreElements();)
		{
			String pn = (String) en.nextElement();
			xd.setParamValue(pn, extp.get(pn));
		}
		return xd;
	}

	public static Hashtable xmlDataToExtProp(XmlData xd)
	{
		Hashtable ht = new Hashtable();
		if (xd == null)
			return ht;
		
		String[] ns = xd.getParamNames();
		for (String pn : ns)
		{

			Object v = xd.getParamValue(pn);
			ht.put(pn, v);
		}

		return ht;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("user_id", this.userId);
		xd.setParamValue("domain_id", domainId) ;
		xd.setParamValue("unique_username", this.getUniqueUserName());
		if (cnName != null)
			xd.setParamValue("cn_name", cnName);
		if (enName != null)
			xd.setParamValue("en_name", enName);

		if (creationDate != null)
			xd.setParamValue("creation_date", creationDate);

		if (email != null)
			xd.setParamValue("email", email);

		xd.setParamValue("state", state.getIntValue());
		xd.setParamValue("belong_orgnode", belongToOrgNodeId);

		XmlData extxd = extPropToXmlData(extProps);
		if (extxd != null)
		{
			xd.setSubDataSingle("ext_prop", extxd);
		}

		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		//userId = xd.getParamValueInt64("user_id",-1);
		userId = xd.getParamValueStr("user_id");
		domainId = xd.getParamValueInt32("domain_id", 0) ;
		userName = xd.getParamValueStr("unique_username");
		int p = userName.indexOf('@');
		if (p > 0)
		{
			this._ProviderName = userName.substring(p + 1);
			userName = userName.substring(0, p);
		}

		cnName = xd.getParamValueStr("cn_name");
		enName = xd.getParamValueStr("en_name");
		Date tmpd = xd.getParamValueDate("creation_date", new Date()) ;
		creationDate = new Timestamp(tmpd.getTime());
		email = xd.getParamValueStr("email");

		state = UserState.getByIntValue(xd.getParamValueInt32("state", 0));
		belongToOrgNodeId = xd.getParamValueStr("belong_orgnode");

		XmlData extxd = xd.getSubDataSingle("ext_prop");
		extProps = xmlDataToExtProp(extxd);
	}
}
