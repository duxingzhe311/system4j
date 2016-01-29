package com.dw.user.access;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

import com.dw.mltag.util.XmlUtil;
import com.dw.system.AppConfig;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GdbException;
import com.dw.user.Role;
import com.dw.user.UserProfile;

public class AccessManager
{
	public static final String ATTRN_SWITCH = "switch" ;
	public static final String ATTRN_INNER_IP_PREFIX = "inner_ip_prefix" ;
	public static final String ATTRN_OUTER_IP_PREFIX = "outer_ip_prefix" ;
	public static final String ATTRN_KEY_FILE = "key_file" ;
	public static final String ATTRN_USB_KEY = "usb_key" ;
	public static final String ATTRN_TMP_AUTH = "tmp_auth" ;
	public static final String ATTRN_TMP_AUTH_IGNORE_ROLES = "tmp_auth_ignore_roles" ;
	
	
	private static AccessManager accessMgr = null ;
	
	private static Object locker = new Object() ;
	
	public static AccessManager getInstance()
	{
		if(accessMgr!=null)
			return accessMgr ;
		
		synchronized(locker)
		{
			accessMgr = new AccessManager() ;
			return accessMgr ;
		}
	}
	
	/**
	 * �Ƿ��������ʿ���
	 */
	boolean bOn = false;
	
	boolean bOuterNever = false;
	/**
	 * �ڲ�ipǰ׺
	 */
	ArrayList<String> innerIpPrefixes = new ArrayList<String>() ;
	
	/**
	 * �ⲿipǰ׺���壬���ڴ�������£��ڲ���ĳһ���̶�ip�п�����Ϊ�ⲿipת��
	 * ��ʱ����Ҫ�����趨Ϊ�ⲿip���ʣ�һ������ݲ���Ҫ����
	 */
	ArrayList<String> outerIpPrefixes = new ArrayList<String>() ;
	
	/**
	 * �Ƿ���Ҫ��Կ�ļ�
	 * ��֤�û�����ǰʹ��
	 */
	boolean bNeekKeyFile = false;
	
	/**
	 * �Ƿ���ҪUsb��Կ - Ҳ������Կ�ļ�����usb��,������֤ҳ����Ҫ���ؿؼ��Զ�����usb
	 */
	boolean bNeekUsbKey = false;
	
	/**
	 * �Ƿ���Ҫ֧����ʱ��Ȩ,Ҳ����ĳһ���û����������Ҫ��ʱ��Ȩ(��ʱ���)����
	 * ��֤�û�������ʱʹ��
	 */
	boolean bNeedTmpAuth = false;
	
	/**
	 * ���tmp_auth="true",��ô����������Ľ�ɫ���ƾ;Ͳ���Ҫ������ʱ��Ȩ
	 * Ҳ����һֱ���з���Ȩ��
	 */
	HashSet<String> needTmpAuthIgnoreRoles = new HashSet<String>() ;
	
	private AccessManager()
	{
		/*
		 <outer_access_limit switch="on"
			inner_ip_prefix="192.168.0."
			key_file="true"
			usb_key="false"
			tmp_auth="true"
			tmp_auth_ignore_roles="manager"
			/>
		 */
		Element userele = AppConfig.getConfElement("user") ;
		if(userele==null)
			return ;
		
		Element[] eles = XmlUtil.getSubChildElement(userele, "outer_access_limit") ;
		if(eles==null||eles.length<=0)
			return ;
		
		Element oal_ele = eles[0] ;
		bOn = "on".equalsIgnoreCase(oal_ele.getAttribute(ATTRN_SWITCH)) ;
		bOuterNever = "never".equalsIgnoreCase(oal_ele.getAttribute(ATTRN_SWITCH)) ;
		
		String inneripp = oal_ele.getAttribute(ATTRN_INNER_IP_PREFIX) ;
		if(inneripp!=null)
		{
			StringTokenizer st = new StringTokenizer(inneripp,",| ") ;
			while(st.hasMoreTokens())
			{
				innerIpPrefixes.add(st.nextToken());
			}
		}
		
		String outeripp = oal_ele.getAttribute(ATTRN_OUTER_IP_PREFIX) ;
		if(outeripp!=null)
		{
			StringTokenizer st = new StringTokenizer(outeripp,",| ") ;
			while(st.hasMoreTokens())
			{
				outerIpPrefixes.add(st.nextToken());
			}
		}
		
		
		bNeedTmpAuth = "true".equalsIgnoreCase(oal_ele.getAttribute(ATTRN_TMP_AUTH)) ;
		
		String auth_ignore_rs = oal_ele.getAttribute(ATTRN_TMP_AUTH_IGNORE_ROLES);
		if(auth_ignore_rs!=null)
		{
			StringTokenizer st = new StringTokenizer(auth_ignore_rs,",| ") ;
			while(st.hasMoreTokens())
			{
				needTmpAuthIgnoreRoles.add(st.nextToken());
			}
		}
	}
	
	/**
	 * �ж��Ƿ�Ҫ��Զ�̷��ʿ���
	 * @return
	 */
	public boolean isOutterAccessCheckOn()
	{
		return bOn ;
	}
	
	/**
	 * �ж��Ƿ��ⲿ��������
	 * @return
	 */
	public boolean isOutterAccessNever()
	{
		return bOuterNever;
	}
	/**
	 * �ж�ĳ��ip�Ƿ����ڲ�ip
	 * @param ip
	 * @return
	 */
	public boolean isInnerIp(String ip)
	{
		for(String s:outerIpPrefixes)
		{
			if(ip.startsWith(s))
			{
				char c = ip.charAt(ip.length()-1) ;
				if(c=='.'||c==':')
					return false ;
				else if(ip.equals(s))
					return false;
			}
		}
		
		for(String s:innerIpPrefixes)
		{
			if(ip.startsWith(s))
				return true ;
		}
		
		return false;
	}
	
	/**
	 * �ж�һ�������Ƿ���Ҫ������Կ��֤
	 * @param req
	 * @return
	 */
	public boolean isNeedAuth(HttpServletRequest req)
	{
		if(!bOn)
			return false;
		
		if(AppConfig.isSole())
			return false;
		
		String host = req.getRemoteAddr() ;
		//check user ip to det
		return !isInnerIp(host);
	}
	
	public boolean isInnerRequest(HttpServletRequest req)
	{
		String host = req.getRemoteAddr() ;
		//check user ip to det
		return isInnerIp(host) ;
	}
	
	/**
	 * �ж��Ƿ���Ҫ��ʱ��Ȩ��֤
	 * @return
	 */
	public boolean isNeedTmpAuth()
	{
		return bNeedTmpAuth ;
	}
	/**
	 * �÷����ṩ��ʱ��Ȩ����Ȩ��
	 * 
	 * 
	 * @param up
	 * @return �������<0,����û��Ȩ��, 0 - ��ʾû��ʱ������ ,>0 ��ʾ���Ƶĺ�����
	 * 		��֤������Ը��ݱ�ֵд�뵽session��,�����Ͳ���Ҫ�����������֤�������ݿ�.
	 * 
	 */
	public long checkTmpAuth(UserProfile up)
	{
		//�ж��Ƿ���Ҫ��ʱ��Ȩ
		List<Role> rs = up.getRoleInfo() ;
		if(rs!=null)
		{
			for(Role r:rs)
			{
				if(needTmpAuthIgnoreRoles.contains(r.getName()))
					return 0 ; //no time limit
			}
		}
		
		//check from db
		try
		{
			TmpAuthItem tai = getUserTmpAuth(up.getUserName()) ;
			if(tai==null)
				return -1 ;
			
			Date sd = tai.getStartDate() ;
			if(sd==null)
				return -1 ;
			
			long cur_t = System.currentTimeMillis() ;
			
			if(sd.getTime()>cur_t)
			{
				return -1 ;// no right yet
			}
			
			Date endd = tai.getEndDate() ;
			if(endd==null)
				return -1 ;
			
			long enddt = endd.getTime() ;
			if(cur_t>enddt)
				return -1 ;
			
			return enddt ;
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
			return -1 ;//error with no limit
		}
	}
	
	/**
	 * 
	 * @param autoid
	 * @return
	 * @throws Exception
	 */
	public TmpAuthItem getUserTmpAuthById(String autoid) throws Exception
	{
		return (TmpAuthItem)GDB.getInstance().getXORMObjByPkId(TmpAuthItem.class, autoid) ;
	}
	/**
	 * �����û�����ö��ڵ���ʱ��Ȩ
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public TmpAuthItem getUserTmpAuth(String username) throws Exception
	{
		return (TmpAuthItem)GDB.getInstance().getXORMObjByUniqueColValue(TmpAuthItem.class, "UserName", username, false);
	}
	
	public void setUserTmpAuth(TmpAuthItem tai) throws Exception
	{
		//String username,Date startd,Date endd
		TmpAuthItem tai0 = getUserTmpAuth(tai.getUserName()) ;
		if(tai0==null)
		{
			GDB.getInstance().addXORMObjWithNewId(tai);
		}
		else
		{
			GDB.getInstance().updateXORMObjToDB(tai0.getAutoId(), tai);
		}
	}
	
	/**
	 * ɾ���û���ʱ��Ȩ����
	 * @param autoid
	 * @throws Exception
	 */
	public void delUserTmpAuth(String autoid) throws Exception
	{
		GDB.getInstance().deleteXORMObjFromDB(autoid, TmpAuthItem.class);
	}
	
	/**
	 * �о������û���ʱ��Ȩ
	 * @return
	 * @throws Exception
	 */
	public List<TmpAuthItem> listAllUserTmpAuth() throws Exception
	{
		return (List<TmpAuthItem>)GDB.getInstance().listXORMAsObjList(TmpAuthItem.class, "", "", 0, -1) ;
	}
}
