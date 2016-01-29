package com.dw.user;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

import com.dw.grid.GridClientManager;
import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.cache.Cacher;
import com.dw.system.encrypt.DES;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import com.dw.user.right.RightManager;
import com.dw.user.right.RightRule;

/// <summary>
/// 和一个用户登陆后有关的完整信息
/// </summary>
@XRmi(reg_name = "security_userprofile")
public class UserProfile implements IXmlDataable// :System.Web.Profile
{
	public static final String ATTRN_USER_PROFILE = "biz_user_profile" ;
	
	public static final String ATTRN_ADMIN_ROLES = "admin_roles" ;
	
	/**
	 * 在app.xml配置文件中定义的扩展管理员角色 &lt;user admin_roles="manager,"
	 */
	private static HashSet<String> EXT_ADMIN_ROLES = new HashSet<String>() ;
	static
	{
		Element userele = AppConfig.getConfElement("user") ;
		if(userele!=null)
		{
			String adminrs = userele.getAttribute(ATTRN_ADMIN_ROLES);
			if(Convert.isNotNullEmpty(adminrs))
			{
				StringTokenizer st = new StringTokenizer(adminrs," |,") ;
				while(st.hasMoreTokens())
				{
					EXT_ADMIN_ROLES.add(st.nextToken()) ;
				}
			}
		}
	}
	// / <summary>
	// / 根据用户名称获得对用的用户Profile信息
	// / </summary>
	// / <param name="username"></param>
	// / <returns></returns>
	public static UserProfile GetUserProfileByName(String username) throws Exception
	{
		return GetUserProfileByName(0,username);
	}
	
	public static UserProfile GetUserProfileByName(int domain,String username) throws Exception
	{
		UserProvider.UserProfileItem upi = UserManager.getDefaultIns().getDefaultUserProvider().getUserProfileFromDB(domain,username);
		if(upi==null)
			return null ;
		
		return getUserProfile(upi);
	}
	
	static Cacher user2profile_cache = null ;
	
	static
	{
		user2profile_cache = Cacher.getCacher("user2profile_cache_30mi");
		user2profile_cache.setMaxBufferLength(500) ;
	}
	
	public static UserProfile getUserProfileByNameCached(String usern)
		throws Exception
	{
		UserProfile up = (UserProfile)user2profile_cache.get(usern) ;
		if(up!=null)
			return up ;
		
		up = GetUserProfileByName(usern) ;
		if(up==null)
			return null ;
		
		user2profile_cache.cache(usern, up, 1800000, false) ;
		return up ;
	}


	public static UserProfile getUserProfile(UserProvider.UserProfileItem upi) throws Exception
	{
		if(upi==null)
			return null ;
		
		User u = upi.getUserInfo();
		if (u == null)
			return null;

		UserProfile up = new UserProfile();
		up.userInfo = u;

		RoleManager rm = RoleManager.getDefaultIns() ;
		int d = u.getUserDomainId() ;
		if(d<=0)
		{
			List<Role> rs = new ArrayList<Role>() ;
			for(String rid:upi.getRoleIds())
			{
				Role tmpr = rm.GetRole(rid);
				if(tmpr!=null)
					rs.add(tmpr);
			}
			up.roleInfo = rs;
	
			OrgManager om = OrgManager.getDefaultIns() ;
			
			List<OrgNode> ons = new ArrayList<OrgNode>() ;
			for(String onid:upi.getOrgNodeIds())
			{
				OrgNode nn = om.GetOrgNodeById(onid);
				if(nn!=null)
					ons.add(nn);
			}
			up.orgNodeInfo = ons;
		}
		else
		{
			List<Role> rs = rm.GetRolesForUser(d, u.getUserName()) ;
			up.roleInfo = rs;
		}
		
		return up;
	}
	
	
//	public static UserProfile GetUserProfileByName(String username) throws Exception
//	{
//		User u = UserManager.getDefaultIns().GetUser(username);
//		if (u == null)
//			return null;
//
//		UserProfile up = new UserProfile();
//		up.userInfo = u;
//
//		up.roleInfo = RoleManager.getDefaultIns().GetRolesForUser(username);
//		up.orgNodeInfo = OrgManager.getDefaultIns().GetOrgNodesByUserName(username);
//		return up;
//	}
	
	public static String createClientAuthLine(long clientid,String key)
	{
		String rid = UUID.randomUUID().toString();
		return clientid+":"+rid+":"+DES.encode(rid,  key) ;
	}
	
	private static UserProfile acquireClientUP(HttpServletRequest req) throws Exception
	{
		String authline = req.getHeader(SessionManager.TBS_CLIENT_AUTH_ATTRN) ;
		if(Convert.isNullOrEmpty(authline))
		{
			authline = req.getParameter(SessionManager.TBS_CLIENT_AUTH_ATTRN);
			if(Convert.isNullOrEmpty(authline))
				return null ;
			authline = Convert.decodeSmartUrl(authline) ;
		}
		
		StringTokenizer st = new StringTokenizer(authline,":") ;
		int autoitemnum = st.countTokens() ;
		if(autoitemnum<3)
		{
			return null;
		}
		
		String strid = st.nextToken() ;
		
		com.dw.grid.GridClientItem gci = null ;
		long id = Long.parseLong(strid) ;
		String key = null ;
		if(id==0)
		{
			key = "12345678" ;
		}
		else
		{
			gci = GridClientManager.getInstance().getClientById(id) ;
			if(gci==null)
			{
				return null;
			}
			key = gci.getSecKey() ;
		}
		
		
		String guid = st.nextToken() ;
		String secstr = st.nextToken() ;
		
		String msgtype = null ;
		
		if(autoitemnum>=4)
			msgtype = st.nextToken() ;
		
		//System.out.println("msgtype=="+msgtype) ;
		
		String tmpguid = DES.decode(secstr,key) ;
		
		boolean errorMon = false;
		boolean isLostMon = false;
		String lost_divn = null ;
		//System.out.println(secstr+"="+tmpguid) ;
		if(!guid.equals(tmpguid))
		{
			return null ;
		}
		
		String gridcat = null;
		if(gci!=null)
			gridcat = gci.getClientCatName() ;
		
		req.setAttribute("grid_client_item",gci);
		String clientUserN = gci.getUserName() ;
		
		UserProfile client_up = null;
		if(Convert.isNotNullEmpty(clientUserN))
			client_up = UserProfile.GetUserProfileByName(clientUserN) ;

		return client_up ;
	}
	
	/**
	 * 从请求中获得对应的用户Profile
	 */
	public static UserProfile getUserProfile(HttpServletRequest req) throws Exception
	{
		UserProfile up = null;
		try
		{
			up = (UserProfile)req.getAttribute(ATTRN_USER_PROFILE);
			if(up!=null)
				return up ;
			
			up = SessionManager.getCurrentLoginUserProfile(req);
			
			if(up==null)
			{
				up = acquireClientUP(req) ;
			}
		
			if(up!=null)
				req.setAttribute(ATTRN_USER_PROFILE, up);
			
			
	//		if(up!=null)
	//			System.out.println("UserProfile--->"+up.toXmlData().toXmlString());
	//		else
	//			System.out.println("UserProfile--->[]");
			return up ;
		}
		finally
		{
			if(up!=null)
			{
				UserManager.getDefaultIns().logUserActivity(up.getDomainId(), up.getUserName());
			}
		}
	}
	/**
	 * 根据登陆信息获得用户Profile
	 * 
	 * @param rm
	 * @param lui
	 * @return
	 * @throws Exception
	 */
	public static UserProfile getUserProfileByLoginInfo(LoginUserInfo lui) throws Exception
	{
		return GetUserProfileByName(lui.getUserName());
	}

	// / <summary>
	// / 获得当前登陆用户的Profile信息（由可能通过cache获得）
	// / </summary>
	// public static UserProfile CurrentUserProfile
	// {
	// get
	// {
	// WBLoginSession ls = WBSessionManager.CurrentLoginSession;
	// if (ls == null)
	// return null;
	//
	// return new UserProfile(ls);
	// }
	// }

	public static final String EXT_NAME_USER_CN_NAME = "cn_name";

	public static final String EXT_NAME_USER_EN_NAME = "en_name";
	
	public static final String EXT_NAME_USER_EMAIL = "email";
	
	public static final String EXT_BELONG_TO_ORGNODE = "belongto_orgnode";

	public static final String EXT_NAME_ROLES = "roles";

	public static final String EXT_NAME_ORGNODES = "orgnodes";

	String sessionId = null;

	User userInfo = null;

	List<Role> roleInfo = null;

	List<OrgNode> orgNodeInfo = null;

	private UserProfile()
	{
	}

	public UserProfile(LoginSession ls) throws Exception
	{
		if (ls == null)
			throw new IllegalArgumentException("session obj cannot be null!");

		sessionId = ls.getSessionId();
		if (sessionId == null || sessionId.equals(""))
			throw new IllegalArgumentException(
					"invalid LoginSession because has no session id!");

		userInfo = new User();
		//userInfo.userId = Long.parseLong(ls.getUserId());
		userInfo.userId = ls.getUserId();
		
		userInfo.domainId = ls.getUserDomainId() ;
		userInfo.userName = ls.getUserName();

		userInfo.cnName = ls.GetExtInfo(EXT_NAME_USER_CN_NAME);
		userInfo.enName = ls.GetExtInfo(EXT_NAME_USER_EN_NAME);
		userInfo.email = ls.GetExtInfo(EXT_NAME_USER_EMAIL);
		userInfo.belongToOrgNodeId = ls.GetExtInfo(EXT_BELONG_TO_ORGNODE) ;

		String tmps = ls.GetExtInfo(EXT_NAME_ROLES);
		if (tmps != null && !tmps.equals(""))
		{
			roleInfo = new ArrayList<Role>();
			String[] ss = tmps.split(",");
			for (String s : ss)
			{
				if (s == null || s.equals(""))
					continue;

				String rid = s;
				Role r = RoleManager.getDefaultIns().GetRole(rid);
				if (r == null)
					continue;

				roleInfo.add(r);

			}
		}

		tmps = ls.GetExtInfo(EXT_NAME_ORGNODES);
		if (tmps != null && !tmps.equals(""))
		{
			orgNodeInfo = new ArrayList<OrgNode>();
			String[] ss = tmps.split(",");
			for (String s : ss)
			{
				if (s == null || s.equals(""))
					continue;

				//long nodeid = Long.parseLong(s);
				String nodeid = s;
				OrgNode n = OrgManager.getDefaultIns().GetOrgNodeById(nodeid);
				if (n == null)
					continue;

				orgNodeInfo.add(n);
			}

		}
	}

	public LoginSession toLoginSession()
	{
		HashMap<String, String> extinfo = new HashMap<String, String>();

		String cnname = userInfo.cnName;
		if (cnname == null)
			cnname = "";

		String enname = userInfo.enName;
		if (enname == null)
			enname = "";
		
		String email = userInfo.email ;
		if(email==null)
			email = "" ;

		extinfo.put(EXT_NAME_USER_CN_NAME, cnname);
		extinfo.put(EXT_NAME_USER_EN_NAME, enname);
		extinfo.put(EXT_NAME_USER_EMAIL, email);
		extinfo.put(EXT_BELONG_TO_ORGNODE,""+userInfo.getBelongToOrgNodeId()) ;

		if (roleInfo != null && roleInfo.size() > 0)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(roleInfo.get(0).getId());
			int c = roleInfo.size();
			for (int i = 1; i < c; i++)
			{
				sb.append(',').append(roleInfo.get(i).getId());
			}

			extinfo.put(EXT_NAME_ROLES, sb.toString());
		}

		if (orgNodeInfo != null && orgNodeInfo.size() > 0)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(orgNodeInfo.get(0).getOrgNodeId());
			int c = orgNodeInfo.size();
			for (int i = 1; i < c; i++)
			{
				sb.append(',').append(orgNodeInfo.get(i).getOrgNodeId());
			}

			extinfo.put(EXT_NAME_ORGNODES, sb.toString());
		}

		return new LoginSession(userInfo.getUserDomainId(),"" + userInfo.getUserId(), userInfo
				.getUserName(), extinfo);
	}

	public String getSessionId()
	{
		return sessionId;
	}
	
	public int getDomainId()
	{
		if (userInfo == null)
			return 0;

		return userInfo.getUserDomainId();
	}

	public String getUserName()
	{
		if (userInfo == null)
			return null;

		return userInfo.getUserName();
	}
	
	public String getUserNameWithDomainId()
	{
		if (userInfo == null)
			return null;

		return userInfo.getUserNameWithDomainId();
	}

	public boolean isAdministrator()
	{
		if (roleInfo == null)
			return false;

		for (Role r : roleInfo)
		{
			String n = r.getName() ;
			if (n.equals("Administrators")||"root".equals(n))
				return true;
			
			if(EXT_ADMIN_ROLES.contains(n))
				return true ;
		}

		return false;
	}
	
	public boolean isGuest()
	{
		if(roleInfo==null||roleInfo.size()<=0)
			return true ;
		
		for (Role r : roleInfo)
		{
			String n = r.getName() ;
			if ("guest".equalsIgnoreCase(n))
				continue ;
			
			return false;
		}
		
		return true ;
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof UserProfile))
			return false;
		
		UserProfile oup = (UserProfile)o ;
		if(!this.getUserName().equals(oup.getUserName()))
			return false;
		return this.getDomainId()==oup.getDomainId() ;
	}

	public User getUserInfo()
	{
		return userInfo;
	}
	
	/**
	 * 得到用户的显示名词
	 * @return
	 */
	public String getDisUserName()
	{
		if(userInfo==null)
			return "" ;
		
		String fn = userInfo.getFullName() ;
		if(Convert.isNotNullEmpty(fn))
			return fn ;
		
		fn = userInfo.getCnName() ;
		if(Convert.isNotNullEmpty(fn))
			return fn ;
		
		fn = userInfo.getEnName() ;
		if(Convert.isNotNullEmpty(fn))
			return fn ;
		
		return userInfo.getUserName() ;
	}

	public List<Role> getRoleInfo()
	{
		return roleInfo;
	}
	
	public boolean isRoleById(String role_id)
	{
		if(roleInfo==null)
			return false;
		
		for(Role r:roleInfo)
		{
			if(role_id.equals(r.getId()))
				return true ;
		}
		return true ;
	}

	/**
	 * 判定是否包含角色名称
	 * @param rolename
	 * @return
	 */
	public boolean containsRoleName(String rolename)
	{
		if (roleInfo == null)
			return false;

		for (Role r : roleInfo)
		{
			if (r.getName().equals(rolename))
				return true;
		}

		return false;
	}
	
	/**
	 * 判断是否至少匹配了一个目标角色
	 * @param roleset
	 * @return
	 */
	public boolean checkMatchOneRoleName(HashSet<String> roleset)
	{
		if (roleInfo == null||roleset==null)
			return false;

		for (Role r : roleInfo)
		{
			if (roleset.contains(r.getName()))
				return true;
		}

		return false;
	}
	/**
	 * 判定是否包含角色id
	 * @param rid
	 * @return
	 * @throws Exception 
	 */
	public boolean containsRoleId(String rid) throws Exception
	{
		return checkHasRoleId(rid);
	}
	
	public static boolean checkHasRoleId(UserProfile up,String rid) throws Exception
	{
		Role r0 = RoleManager.getDefaultIns().GetRole(rid);
		if(r0==null)
			return false;
		
		if("guest".equalsIgnoreCase(r0.getName()))
			return true ;
		
		if(up==null)
			return false;
		
		return up.checkHasRoleId(rid);
	}
	/**
	 * 根据角色id,判断该用户是否满足该角色
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public boolean checkHasRoleId(String rid) throws Exception
	{
		Role r0 = RoleManager.getDefaultIns().GetRole(rid);
		if(r0==null)
			return false;
		
		if("guest".equalsIgnoreCase(r0.getName()))
			return true ;
		
		if (roleInfo == null)
			return false;
		
		for (Role r : roleInfo)
		{
			if (r.getId().equals(rid))
				return true;
		}

		return false;
	}

	public List<OrgNode> getOrgNodeInfo()
	{
		return orgNodeInfo;
	}
	
	/**
	 * 获得某一个组织机构节点下的和本用户相关的子孙机构节点
	 * @param orgn_path
	 * @return
	 * @throws Exception 
	 */
	public List<OrgNode> getOrgNodesBelow(String orgn_path) throws Exception
	{
		if(orgNodeInfo==null||orgNodeInfo.size()<=0)
			return null ;
		
		OrgNode n = OrgManager.getDefaultIns().getOrgNodeByPath(orgn_path) ;
		if(n==null)
			return null ;
		
		ArrayList<OrgNode> rets = new ArrayList<OrgNode>() ;
		for(OrgNode tmpn:orgNodeInfo)
		{
			if(tmpn.isAncestorNode(n))
				rets.add(tmpn) ;
		}
		
		return rets ;
	}
	
	/**
	 * 获得某一个组织机构节点下的和本用户相关的子机构节点
	 * @param orgn_path
	 * @return
	 * @throws Exception
	 */
	public List<OrgNode> getOrgNodesSub(String orgn_path) throws Exception
	{
		if(orgNodeInfo==null||orgNodeInfo.size()<=0)
			return null ;
		
		OrgNode n = OrgManager.getDefaultIns().getOrgNodeByPath(orgn_path) ;
		if(n==null)
			return null ;
		
		ArrayList<OrgNode> rets = new ArrayList<OrgNode>() ;
		for(OrgNode tmpn:orgNodeInfo)
		{
			if(n.equals(tmpn.parentNode))
				rets.add(tmpn) ;
		}
		
		return rets ;
	}
	/**
	 * 判定是否包含机构节点id
	 * @param oid
	 * @return
	 */
	public boolean containsOrgNodeId(String oid)
	{
		if (orgNodeInfo == null)
			return false;

		for (OrgNode r : orgNodeInfo)
		{
			if (r.getOrgNodeId().equals(oid))
				return true;
		}

		return false;
	}

	transient LoginUserInfo loginUserInfo = null;

	public LoginUserInfo toLoginUserInfo()
	{
		if (loginUserInfo != null)
			return loginUserInfo;

		loginUserInfo = new LoginUserInfo();

		loginUserInfo.userId = "" + userInfo.getUserId();
		loginUserInfo.domainId = userInfo.getUserDomainId();
		loginUserInfo.userName = userInfo.getUniqueUserName();
		loginUserInfo.cnName = userInfo.getCnName();
		loginUserInfo.enName = userInfo.getEnName();
		loginUserInfo.creationDate = userInfo.getCreationDate();
		loginUserInfo.lastLoginDate = userInfo.getLastLoginDate();
		loginUserInfo.email = userInfo.getEmail();

		loginUserInfo.extProps = userInfo.extProps;

		// //////////////
		if (roleInfo != null)
		{
			for (Role r : roleInfo)
			{
				loginUserInfo.roleId2RoleName.put(r.getId(), r.getName());
			}
		}

		// /////////
		if (orgNodeInfo != null)
		{
			for (OrgNode n : orgNodeInfo)
			{
				loginUserInfo.orgNodeId2Name.put(n.getOrgNodeId(), n
						.getOrgNodeName());
			}
		}

		return loginUserInfo;
	}

	public boolean checkRightRule(RightRule rr) throws Exception
	{
		if(userInfo.getUserDomainId()!=0)
			return false;
		
		if (isAdministrator())
			return true;
		
		if(rr==null)
			return false;

		return rr.CheckUserRight(this);
	}
	/**
	 * 输出过程中只输出基本的id信息
	 */
	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		if (sessionId != null)
			xd.setParamValue("session_id", sessionId);

		xd.setSubDataSingle("user", userInfo.toXmlData());

		if (roleInfo != null && roleInfo.size() > 0)
		{
			List<XmlData> rolexds = xd.getOrCreateSubDataArray("roles");
			for (Role r : roleInfo)
			{
				rolexds.add(r.toXmlData());
			}
		}

		if (orgNodeInfo != null && orgNodeInfo.size() > 0)
		{
			List<XmlData> orgnxds = xd.getOrCreateSubDataArray("orgnodes");
			for (OrgNode n : orgNodeInfo)
			{
				orgnxds.add(n.toXmlData());
			}
		}
		return xd;
	}

	/**
	 * 装配过程中，根据基本的id信息，和本地Manager进行构建 其中要求，本地的基本内容和远程一致才可以保证完全复原
	 */
	public void fromXmlData(XmlData xd)
	{
		sessionId = xd.getParamValueStr("session_id");

		XmlData userxd = xd.getSubDataSingle("user");
		if (userxd != null)
		{
			userInfo = new User();
			userInfo.fromXmlData(userxd);
		}

		List<XmlData> rxds = xd.getSubDataArray("roles");
		if (rxds != null)
		{
			roleInfo = new ArrayList<Role>(rxds.size());
			for (XmlData tmpxd : rxds)
			{
				Role r = new Role();
				r.fromXmlData(tmpxd);
				roleInfo.add(r);
			}
		}

		List<XmlData> oxds = xd.getSubDataArray("orgnodes");
		if (oxds != null)
		{
			orgNodeInfo = new ArrayList<OrgNode>(oxds.size());
			for (XmlData tmpxd : oxds)
			{
				OrgNode tmpn = new OrgNode();
				tmpn.fromXmlData(tmpxd);
				orgNodeInfo.add(tmpn);
			}
		}
	}
}
