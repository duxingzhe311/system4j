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
	 * 是否启动访问控制
	 */
	boolean bOn = false;
	
	boolean bOuterNever = false;
	/**
	 * 内部ip前缀
	 */
	ArrayList<String> innerIpPrefixes = new ArrayList<String>() ;
	
	/**
	 * 外部ip前缀定义，如在代理情况下，内部的某一个固定ip有可能作为外部ip转发
	 * 此时，需要进行设定为外部ip访问，一般吃内容不需要配置
	 */
	ArrayList<String> outerIpPrefixes = new ArrayList<String>() ;
	
	/**
	 * 是否需要密钥文件
	 * 验证用户密码前使用
	 */
	boolean bNeekKeyFile = false;
	
	/**
	 * 是否需要Usb密钥 - 也就是密钥文件存在usb中,并且验证页面需要下载控件自动访问usb
	 */
	boolean bNeekUsbKey = false;
	
	/**
	 * 是否需要支持临时授权,也就是某一个用户外出访问需要临时授权(有时间段)才行
	 * 验证用户名密码时使用
	 */
	boolean bNeedTmpAuth = false;
	
	/**
	 * 如果tmp_auth="true",那么本变量里面的角色名称就就不需要进行临时授权
	 * 也就是一直都有访问权限
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
	 * 判断是否要打开远程访问控制
	 * @return
	 */
	public boolean isOutterAccessCheckOn()
	{
		return bOn ;
	}
	
	/**
	 * 判断是否外部访问限制
	 * @return
	 */
	public boolean isOutterAccessNever()
	{
		return bOuterNever;
	}
	/**
	 * 判定某个ip是否是内部ip
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
	 * 判断一个请求是否需要进行密钥验证
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
	 * 判断是否需要临时授权验证
	 * @return
	 */
	public boolean isNeedTmpAuth()
	{
		return bNeedTmpAuth ;
	}
	/**
	 * 该方法提供临时授权访问权限
	 * 
	 * 
	 * @param up
	 * @return 如果返回<0,表明没有权限, 0 - 表示没有时间限制 ,>0 表示限制的毫秒数
	 * 		验证程序可以根据本值写入到session中,这样就不需要进行往后的验证访问数据库.
	 * 
	 */
	public long checkTmpAuth(UserProfile up)
	{
		//判断是否需要临时授权
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
	 * 根据用户名获得对于的临时授权
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
	 * 删除用户临时授权访问
	 * @param autoid
	 * @throws Exception
	 */
	public void delUserTmpAuth(String autoid) throws Exception
	{
		GDB.getInstance().deleteXORMObjFromDB(autoid, TmpAuthItem.class);
	}
	
	/**
	 * 列举所有用户临时授权
	 * @return
	 * @throws Exception
	 */
	public List<TmpAuthItem> listAllUserTmpAuth() throws Exception
	{
		return (List<TmpAuthItem>)GDB.getInstance().listXORMAsObjList(TmpAuthItem.class, "", "", 0, -1) ;
	}
}
