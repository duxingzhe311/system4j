package com.dw.user;

import java.io.*;
import java.util.Enumeration;
import java.util.*;

import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.XRmi;
import com.dw.user.User.UserState;

/**
 * UserProfile��һ���򻯰汾,���ǵ�.netϵͳ�е��û���Ϣ��java���ϴ�
 * Ϊ�˲��Ķ�.net�е�Ȩ��ϵͳ,���������ͳһ����֮��Ĺ�ϵ
 * 
 * ������Ժ�UserProfile�����ݽ����໥ת��,�����ڿͻ���Ҳ����ʹ�ø������Լ�
 * ��Ҫ����Ϣ.
 * 
 * ���಻����Ϊ�޸��û���Ϣ���������ʹ��
 * 
 * @author Jason Zhu
 */
@XRmi(reg_name="security_login_user_info")
public class LoginUserInfo implements IXmlDataable
{
	/**
	 * ��Ϊnull,ԭ���п������û���Ϣ�����ģ���ṩ
	 */
	String userId = null;
	
	int domainId = 0 ;
	
	String userName = null ;
	
	String cnName;

	String enName;

	Date creationDate;

	Date lastLoginDate;

	Date lastActivityDate;

	String email;

	//long belongToOrgNodeId = -1;

	Hashtable extProps = new Hashtable();
	
	////////////////
	Hashtable<String,String> roleId2RoleName = new Hashtable<String,String>();
	
	///////////
	Hashtable<String,String> orgNodeId2Name = new Hashtable<String,String>() ;
	
	public LoginUserInfo()
	{}
	
	public int getUserDomainId()
	{
		return domainId ;
	}
	
	public String getUserName()
	{
		return userName ;
	}
	
	public String getNameCn()
	{
		return cnName ;
	}
	
	public String getNameEn()
	{
		return enName ;
	}
	
	public Date getCreationDate()
	{
		return creationDate ;
	}
	
	public String getEmail()
	{
		return email ;
	}
	
	public Object getExtProp(String n)
	{
		return extProps.get(n);
	}
	
	public boolean hasRole(long id)
	{
		return roleId2RoleName.containsKey(id);
	}
	
	public boolean hasRole(String name)
	{
		return roleId2RoleName.containsValue(name);
	}
	
	public boolean hasOrgNodeById(String id)
	{
		return orgNodeId2Name.containsKey(id);
	}
	
	private static XmlData extPropToXmlData(Hashtable extp)
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

	private static Hashtable xmlDataToExtProp(XmlData xd)
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
		
		if(userId!=null)
		{
			xd.setParamValue("userid",userId);
		}
		
		xd.setParamValue("username", userName);
		if (cnName != null)
			xd.setParamValue("cn_name", cnName);
		if (enName != null)
			xd.setParamValue("en_name", enName);

		if (creationDate != null)
			xd.setParamValue("creation_date", creationDate);

		if (email != null)
			xd.setParamValue("email", email);

		XmlData extxd = extPropToXmlData(extProps);
		if (extxd != null)
		{
			xd.setSubDataSingle("ext_prop", extxd);
		}
		
		XmlData rolexd = xd.getOrCreateSubDataSingle("roles");
		for(Map.Entry<String, String> ens:roleId2RoleName.entrySet())
		{
			rolexd.setParamValue(ens.getKey(), ens.getValue());
		}
		
		XmlData rnxd = xd.getOrCreateSubDataSingle("orgnodes");
		for(Map.Entry<String, String> ens:orgNodeId2Name.entrySet())
		{
			rnxd.setParamValue(ens.getKey(), ens.getValue());
		}

		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		userId = xd.getParamValueStr("userid");
		userName = xd.getParamValueStr("username");
		
		cnName = xd.getParamValueStr("cn_name");
		enName = xd.getParamValueStr("en_name");
		creationDate = xd.getParamValueDate("creation_date", new Date());
		email = xd.getParamValueStr("email");

		XmlData extxd = xd.getSubDataSingle("ext_prop");
		extProps = xmlDataToExtProp(extxd);
		
		XmlData rolexd = xd.getSubDataSingle("roles");
		if(rolexd!=null)
		{
			for(String n:rolexd.getParamNames())
			{
				//Long k = new Long(n);
				String v = rolexd.getParamValueStr(n);
				roleId2RoleName.put(n, v);
			}
		}
		
		XmlData rnxd = xd.getSubDataSingle("orgnodes");
		if(rnxd!=null)
		{
			for(String n:rnxd.getParamNames())
			{
				//Long k = new Long(n);
				String v = rnxd.getParamValueStr(n);
				orgNodeId2Name.put(n, v);
			}
		}
	}
}
