package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.logger.*;
import com.dw.system.xmldata.XmlData;
import com.dw.user.provider.DefaultUserProvider;

public class UserManager
{
	static Object lockObj = new Object();

	static UserManager defUserMgr = null;

	static ILogger log = LoggerManager.getLogger(UserManager.class
			.getCanonicalName());

	public static UserManager getDefaultIns()
	{
		if (defUserMgr != null)
			return defUserMgr;

		synchronized (lockObj)
		{
			if (defUserMgr != null)
				return defUserMgr;

			defUserMgr = new UserManager();
			// 设置UserProvider，可能会根据定制的信息来设置
			defUserMgr.setUserProviders(null, new DefaultUserProvider());
			return defUserMgr;
		}
	}

	// public static UserManager getInstance()
	// {
	// if (userMgr != null)
	// return userMgr;
	//
	// synchronized (lockObj)
	// {
	// if (userMgr != null)
	// return userMgr;
	//
	// try
	// {
	// userMgr = new UserManager();
	// return userMgr;
	// }
	// catch (Exception ee)
	// {
	// log.error(ee);
	// ee.printStackTrace();
	// return null;
	// }
	// }
	// }

	Hashtable<String, UserProvider> name2provider = new Hashtable<String, UserProvider>();

	UserProvider defaultProvider = null;
	
	Class userExtClass = UserExtItemDefault.class ;
	

	private UserManager()
	{
		// provider = new provider.DefaultUserProvider();
		// provider = new provider.MembershipUserProvider();
		// provider = new provider.LDAPUserProvider();

		// SecurityConfigItem ci = WBConfig.GetConfigItem("Security") as
		// SecurityConfigItem;
		// if (ci == null)
		// provider = new provider.DefaultUserProvider();
		// else
		// provider = ci.CurUserProvider;
		
		//load default user ext class
		//"user_ext_class"
	}

	public void setUserProviders(String prov_name, UserProvider up)
	{
		if (prov_name == null)
			prov_name = "";

		if (prov_name.equals(""))
			defaultProvider = up;

		name2provider.put(prov_name, up);
	}

	public String[] getUserProviderNames()
	{
		String[] rets = new String[name2provider.size()];
		name2provider.keySet().toArray(rets);
		return rets;
	}

	public UserProvider getUserProvider(String prov_name)
	{
		if (prov_name == null || prov_name.equals(""))
			return defaultProvider;

		return name2provider.get(prov_name);
	}

	public UserProvider getDefaultUserProvider()
	{
		return defaultProvider;
	}
	
	public String getUserPsw(String usern) throws Exception
	{
		return getUserPsw(0,usern);
	}

	public String getUserPsw(int domain,String usern) throws Exception
	{
		return defaultProvider.getPassword(domain,usern);
	}

	// / <summary>
	// /
	// / </summary>
	// / <param name="username"></param>
	// / <param name="password"></param>
	// / <returns></returns>
	public String ValidateUserLogin(String username, String password)
			throws Exception
	{
		return ValidateUserLogin(username, password, null,null);
	}

	/// <summary>
	/// 根据用户名，密码和应用名称判断用户是否可以登陆系统
	/// 如果没有填写应用名称，则只判断用户名和密码
	///
	/// 注：用户能否登陆一个应用，可能和该用户的角色，所在部门有关
	/// </summary>
	/// <param name="username"></param>
	/// <param name="password"></param>
	/// <param name="application"></param>
	/// <returns></returns>
	public String ValidateUserLogin(String username, String password,
			String application,String md5_32_prefix) throws Exception
	{
		return ValidateUserLogin(0,username, password,
				application,md5_32_prefix);
	}
	
	public String ValidateUserLogin(int domain,String username, String password,
			String application,String md5_32_prefix) throws Exception
	{
		UserProvider.UserProfileItem upi = defaultProvider.ValidateUser(domain,username, password,md5_32_prefix);
		if(upi==null)
			return null ;
		
		return upi.getUserInfo().getUserId();
	}

	// / <summary>
	// / 修改密码
	// / </summary>
	// / <param name="username"></param>
	// / <param name="oldpsw"></param>
	// / <param name="newpsw"></param>
	// / <returns></returns>
	public boolean ChangePassword(String username, String oldpsw, String newpsw)
		throws Exception
	{
			return ChangePassword(0,username, oldpsw, newpsw);
	}
	
	public boolean ChangePassword(int domain,String username, String oldpsw, String newpsw)
			throws Exception
	{
		return defaultProvider.ChangePassword(domain,username, oldpsw, newpsw);
	}

	/**
	 * 
	 * @param username
	 * @param newpsw
	 * @return
	 * @throws Exception
	 */
	public boolean ChangePasswordByAdm(String username, String newpsw)
	throws Exception
	{
		return ChangePasswordByAdm(0,username, newpsw) ;
	}
	
	public boolean ChangePasswordByAdm(int domain,String username, String newpsw)
			throws Exception
	{
		return defaultProvider.ChangePasswordByAdm(domain,username, newpsw);
	}
	
	/**
	 * 根据用户名,设置本用户临时的reset密码
	 * 
	 * 情景如下:用户忘记了密码,他点击reset后,系统会产生一个随机reset临时密码
	 *   系统通过邮件等外部手段给用户发送本临时密码
	 *   
	 *   用户根据外界手段获得此临时密码(一个临时url),后可以直接修改,其方法调用changePasswordByResetTemp
	 * @param username
	 * @param tempresetpsw
	 * @throws Exception
	 */
	public void resetUserTempPassword(String username,String tempresetpsw)
		throws Exception
	{
			resetUserTempPassword(0,username,tempresetpsw);
	}
	
	public void resetUserTempPassword(int domain,String username,String tempresetpsw)
		throws Exception
	{
		defaultProvider.resetTempPassword(domain,username, tempresetpsw) ;
	}
	
	/**
	 * 根据临时的reset密码,更新用户的密码
	 * 
	 * @param username
	 * @param newpsw
	 * @param resetpsw
	 * @return
	 * @throws Exception
	 */
	public boolean changePasswordByResetTemp(String username,String newpsw,String resetpsw)
	throws Exception
	{
		return changePasswordByResetTemp(0,username,newpsw,resetpsw);
	}
	public boolean changePasswordByResetTemp(int domain,String username,String newpsw,String resetpsw)
	throws Exception
	{
		return defaultProvider.changePasswordByResetTemp(domain,username, newpsw, resetpsw);
	}

	public boolean ChangeUserState(String username, User.UserState us)
		throws Exception
	{
			return ChangeUserState(0,username, us);
	}
	
	public boolean ChangeUserState(int domain,String username, User.UserState us)
			throws Exception
	{
		if ("root".equals(username))
			throw new Exception("root cannot be changed");

		return defaultProvider.ChangeUserState(domain,username, us.getIntValue());
	}

	public void setUserBelongOrgNodeId(String username,
			String orgnodeid) throws Exception
	{
//		List<OrgNode> ons = OrgManager.getDefaultIns().GetOrgNodesByUserName(username);
//		long onid = -1;
//		for (OrgNode n : ons)
//		{
//			if (n.getOrgNodeId() == orgnodeid)
//			{
//				onid = orgnodeid;
//				break;
//			}
//		}
//
//		if (onid <= 0)
//			throw new Exception("user has not in org node with id=" + orgnodeid);

		defaultProvider.setUserBelongOrgNodeId(username, orgnodeid);
	}

	public User CreateUser(String userName, String password, String cnName,
			String enName, String email, UserCreateStatus ucs) throws Exception
	{
		return CreateUser(0,userName, password, User.UserState.Normal, null,
				cnName, enName, email, ucs);
	}
	
	public User CreateUser(int domain,String userName, String password, String cnName,
			String enName, String email, UserCreateStatus ucs) throws Exception
	{
		return CreateUser(domain,userName, password, User.UserState.Normal, null,
				cnName, enName, email, ucs);
	}

	/**
	 * 不考虑用户中英文名的用户创建
	 * 
	 * @param userName
	 * @param password
	 * @param st
	 * @param fullname
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public User CreateUserWithFullName(String userName, String password,
			User.UserState st, String fullname, String email) throws Exception
	{
		return CreateUserWithFullName(0,userName, password,
				st, fullname, email);
	}
	
	public User CreateUserWithFullName(int domain,String userName, String password,
			User.UserState st, String fullname, String email) throws Exception
	{
		UserCreateStatus ucs = new UserCreateStatus();
		return CreateUser(domain,userName, password, st, fullname, null, null, email,
				ucs);
	}
	
	public User CreateUser(String userName, String password, User.UserState st,
			String fullname, String cnName, String enName, String email,
			UserCreateStatus ucs) throws Exception
	{
		return CreateUser(0,userName, password, st,
				fullname, cnName, enName, email,
				ucs);
	}

	public User CreateUser(int domain,String userName, String password, User.UserState st,
			String fullname, String cnName, String enName, String email,
			UserCreateStatus ucs) throws Exception
	{

		if (this.GetUser(domain,userName) != null)
		{
			ucs.setStatusValue(UserCreateStatus.DuplicateUserName);
			return null;
		}

		if (Convert.isNotNullEmpty(email)&&this.GetUserByEmail(email) != null)
		{
			ucs.setStatusValue(UserCreateStatus.DuplicateEmail);
			return null;
		}
		
//		StringBuilder sb  =new StringBuilder() ;
//		if(!Convert.checkVarName(userName, sb))
//		{
//			ucs.setStatusValue(UserCreateStatus.InvalidUserName);
//			return null ;
//		}

		return defaultProvider.CreateUser(domain,userName, password, st, fullname,
				cnName, enName, email);

	}
	
//	public User CreateUser(String username, String password, String email,
//			UserCreateStatus ucs) throws Exception
//	{
//		return CreateUser(0,username, password, email,
//				ucs);
//	}

//	public User CreateUser(int domain,String username, String password, String email,
//			UserCreateStatus ucs) throws Exception
//	{
//		return CreateUser(domain,username, password, null, null, email, ucs);
//	}
//
//	public User CreateUser(String username, String password,
//			UserCreateStatus ucs) throws Exception
//	{
//		return CreateUser(username, password, null, ucs);
//	}

	public boolean DeleteUser(int domain,String username, boolean deleteAllRelatedData)
			throws Exception
	{
		if ("root".equals(username))
			throw new Exception("root cannot be deleted");

		boolean b = defaultProvider.DeleteUser(domain,username, deleteAllRelatedData);
		
		if(b)
		{
			try
			{//删除相关角色
				RoleManager.getDefaultIns().removeUserAllRoles(domain, username) ;
			}
			catch(Exception ee)
			{
				
			}
		}
		
		return b ;
	}

	public boolean DeleteUser(int domain,String username) throws Exception
	{
		return DeleteUser(domain,username, true);
	}
	
	static User.UserState[] VALID_USER_ST = new User.UserState[]{User.UserState.Normal,User.UserState.New,User.UserState.ResetPsw};
	
	public UserList FindUsersByName(String usernameToMatch, int pageIndex,
			int pageSize) throws Exception
	{
		return FindUsersByName(usernameToMatch, pageIndex,
				pageSize,VALID_USER_ST);
	}
	
	public UserList FindUsersByName(int domain,String usernameToMatch, int pageIndex,
			int pageSize) throws Exception
	{
		return FindUsersByName(domain,usernameToMatch, pageIndex,
				pageSize,VALID_USER_ST);
	}

	public UserList FindUsersByName(String usernameToMatch, int pageIndex,
			int pageSize,User.UserState[] ust) throws Exception
	{
		return defaultProvider.FindUsersByName(0,usernameToMatch, pageIndex,
				pageSize,ust);
	}
	
	public UserList FindUsersByName(int domain,String usernameToMatch, int pageIndex,
			int pageSize,User.UserState[] ust) throws Exception
	{
		return FindUsersByName(domain,usernameToMatch,false, pageIndex,
				pageSize,ust);
	}
	
	public UserList FindUsersByName(int domain,String usernameToMatch,boolean isnameprefix, int pageIndex,
			int pageSize,User.UserState[] ust) throws Exception
	{
		return defaultProvider.FindUsersByName(domain,usernameToMatch,isnameprefix, pageIndex,
				pageSize,ust);
		
	}

	public UserList FindUsersByName(String usernameToMatch) throws Exception
	{
		int num = 0;
		return FindUsersByName(usernameToMatch, 0, 0x7fffffff);
	}
	
	public UserList FindUsersByEmail(String emailToMatch, int pageIndex,
			int pageSize) throws Exception
	{
		return FindUsersByEmail(0,emailToMatch, pageIndex,
				pageSize,User.UserState.Normal);
	}

	public UserList FindUsersByEmail(int domain,String emailToMatch, int pageIndex,
			int pageSize,User.UserState ust) throws Exception
	{
		// SecUtility.CheckParameter(ref emailToMatch, false, false, false, 0,
		// "emailToMatch");
		if (pageIndex < 0)
		{
			throw new IllegalArgumentException("bad page index");
		}
		if (pageSize < 1)
		{
			throw new IllegalArgumentException("bad page size");
		}

		return defaultProvider.FindUsersByEmail(domain,emailToMatch, pageIndex,
				pageSize,new User.UserState[]{ust});
	}

	public UserList FindUsersByEmail(String emailToMatch) throws Exception
	{
		int num = 0;
		return FindUsersByEmail(emailToMatch, 0, 0x7fffffff);
	}
	
	/**
	 * 根据域名id获得对应的用户数量
	 * @param domain
	 * @return
	 */
	public int getUserNum(int domainid,User.UserState[] ust) throws Exception
	{
		return defaultProvider.getUserNum(domainid,ust) ;
	}
	
	public int getUserNumValid(int domainid) throws Exception
	{
		return defaultProvider.getUserNum(domainid,VALID_USER_ST) ;
	}

	public UserList GetAllUsers(int pageIndex, int pageSize) throws Exception
	{
		return GetAllUsers(0,pageIndex, pageSize,VALID_USER_ST);
	}
	
	
	public UserList GetAllUsers(int domain,int pageIndex, int pageSize) throws Exception
	{
		return GetAllUsers(domain,pageIndex, pageSize,VALID_USER_ST);
	}
	
	
	public UserList GetAllUsers(int pageIndex, int pageSize,User.UserState[] st) throws Exception
	{
		if (pageIndex < 0)
		{
			throw new IllegalArgumentException("PageIndex < 0");
		}

		return defaultProvider.GetAllUsers(0,pageIndex, pageSize,st);
	}
	
	public UserList GetAllUsers(int domain,int pageIndex, int pageSize,User.UserState[] st) throws Exception
	{
		if (pageIndex < 0)
		{
			throw new IllegalArgumentException("PageIndex < 0");
		}

		return defaultProvider.GetAllUsers(domain,pageIndex, pageSize,st);
	}
	
	public UserList GetAllUsersAfter(int domain,String afterusername,int pageIndex, int pageSize,User.UserState[] st) throws Exception
	{
		if (pageIndex < 0)
		{
			throw new IllegalArgumentException("PageIndex < 0");
		}

		return defaultProvider.GetAllUsersAfter(domain,afterusername,pageIndex, pageSize,st);
	}

	// private static String GetCurrentUserName()
	// {
	// if (HostingEnvironment.IsHosted)
	// {
	// HttpContext context1 = HttpContext.Current;
	// if (context1 != null)
	// {
	// return context1.User.Identity.Name;
	// }
	// }
	//
	// IPrincipal principal1 = Thread.CurrentPrincipal;
	// if ((principal1 != null) && (principal1.Identity != null))
	// {
	// return principal1.Identity.Name;
	// }
	// return String.Empty;
	// }

	// public int GetNumberOfUsersOnline()
	// {
	// return provider.GetNumberOfUsersOnline();
	// }
	//
	// public User GetCurrentUser(boolean userIsOnline)
	// {
	// return GetUser(GetCurrentUserName(), userIsOnline);
	// }
	//
	// public User GetCurrentUser()
	// {
	// return GetUser(GetCurrentUserName(), true);
	// }

	public User GetUser(String username) throws Exception
	{
		return GetUser(0,username, false);
	}
	
	public User GetUser(int domain,String username) throws Exception
	{
		return GetUser(domain,username, false);
	}

	public User getUserByUniqueName(String unique_name) throws Exception
	{
		String provname = "";
		String username = unique_name;
		int p = unique_name.indexOf('@');
		if (p > 0)
		{
			provname = unique_name.substring(p + 1);
			username = unique_name.substring(0, p);
		}

		UserProvider up = name2provider.get(provname);
		if (up == null)
			throw new RuntimeException("no provider found with name="
					+ provname);

		return up.GetUser(0,username, false);
	}
	
	public User GetUser(String username, boolean userIsOnline) throws Exception
	{
		return defaultProvider.GetUser(0,username, userIsOnline);
	}

	public User GetUser(int domain,String username, boolean userIsOnline) throws Exception
	{
		return defaultProvider.GetUser(domain,username, userIsOnline);
	}
	
	public User GetUserById(String uid) throws Exception
	{
		return defaultProvider.GetUserById(uid);
	}

	public User GetUserByEmail(String emailToMatch) throws Exception
	{
		// SecUtility.CheckParameter(ref emailToMatch, false, false, false, 0,
		// "emailToMatch");
		return defaultProvider.GetUserByEmail(emailToMatch);
	}
	
	public UserProfile ValidateUser(String username, String password) throws Exception
	{
		return ValidateUser(username, password,null);
	}
	
	public UserProfile ValidateUser(int domain,String username, String password) throws Exception
	{
		return ValidateUser(domain,username, password,null);
	}
	
	/**
	 * 得到用户扩展类
	 * @return
	 */
	public Class getUserExtItemClass()
	{
		return userExtClass ;
	}
	
	/**
	 * 设置用户的扩展信息
	 * 
	 * 该方法在一个项目应用中，应该最多被调用一次，并且调用过程是通过主应用构件初始化的时候进行
	 * 外界不推荐调用此方法
	 * @param c
	 */
	public void setUserExtItemClass(Class c)
	{
		userExtClass = c ;
	}
	/**
	 * 根据用户名称获得对应的扩展信息
	 * @param username
	 * @return
	 */
	public UserExtItem getUserExtItem(String username)
		throws Exception
	{
		return (UserExtItem)GDB.getInstance().getXORMObjByUniqueColValue(userExtClass, "UserName", username, true) ;
	}
	
	public void setUserExtItem(String username,UserExtItem uei)
		throws Exception
	{
		UserExtItem old_uei = (UserExtItem)GDB.getInstance().getXORMObjByUniqueColValue(userExtClass, "UserName", username, true) ;
		if(old_uei==null)
		{//insert
			uei.setUserName(username) ;
			GDB.getInstance().addXORMObjWithNewId(uei) ;
		}
		else
		{
			uei.setUserName(username) ;
			GDB.getInstance().updateXORMObjToDB(old_uei.getAutoId(), uei) ;
		}
	}
	
	/**
	 * 设置用户的扩展值
	 * @param username
	 * @param propname
	 * @param val
	 * @throws Exception
	 */
	public void setUserExtItemValue(String username,String[] propname,Object[] val)
		throws Exception
	{
		UserExtItem uei = (UserExtItem)GDB.getInstance().getXORMObjByUniqueColValue(userExtClass, "UserName", username, true) ;
		if(uei==null)
		{//insert
			XmlData xd = new XmlData() ;
			for(int i = 0 ; i < propname.length ; i ++)
			{
				xd.setParamValue(propname[i], val[i]) ;
			}
				
			uei = (UserExtItem)userExtClass.newInstance() ;
			XORMUtil.injectXmlDataToObj(uei, xd) ;
			uei.setUserName(username) ;
			
			GDB.getInstance().addXORMObjWithNewId(uei) ;
		}
		else
		{
			XmlData xd = new XmlData() ;
			for(int i = 0 ; i < propname.length ; i ++)
			{
				xd.setParamValue(propname[i], val[i]) ;
			}
			
			uei = (UserExtItem)userExtClass.newInstance() ;
			XORMUtil.injectXmlDataToObj(uei, xd) ;
			uei.setUserName(username) ;
			
			GDB.getInstance().updateXORMObjToDBWithHasColNames(uei.getAutoId(), uei, propname) ;
		}
	}
	
	
	/**
	 * 根据用户扩展的唯一值来获得用户对象
	 * @param ext_propname
	 * @param ext_val
	 * @return
	 * @throws Exception
	 */
	public User getUserByUniqueExtValue(String ext_propname,Object ext_val) throws Exception
	{
		UserExtItem uei = (UserExtItem)GDB.getInstance().getXORMObjByUniqueColValue(getUserExtItemClass(), ext_propname, ext_val, false) ;
		if(uei==null)
			return null ;
		
		User u = defaultProvider.GetUser(0,uei.getUserName(), false);
		if(u==null)
			return null ;
		
		u.extItem = uei ;
		return u ;
	}

	/**
	 * 验证用户密码
	 * @param username
	 * @param password
	 * @param md5_32_prefix 如果客户端通过脚本验证,着客户端输入的密码会被加个前缀后
	 * 	再进行md5加密,而这个前缀每次login时都不一样(验证时存在于session中)
	 *  改串会被用来和系统中的原始密码进行组合,并用md5加密出 加密串,判断输入的password相同
	 *  就认为密码一致
	 *  
	 * @return
	 * @throws Exception
	 */
	public UserProfile ValidateUser(String username, String password,String md5_32_prefix) throws Exception
	{
		return ValidateUser(0,username, password,md5_32_prefix);
	}
	
	public UserProfile ValidateUser(int domain,String username, String password,String md5_32_prefix) throws Exception
	{
		UserProvider.UserProfileItem upi = defaultProvider.ValidateUser(domain,username, password,md5_32_prefix);
		if(upi==null)
			return null ;
		
		User.UserState ust = upi.getUserInfo().getState();
		if(ust==User.UserState.Invalid)//||ust==User.UserState.Deleted)
			return null ;
		
		
		return UserProfile.getUserProfile(upi);
	}
	
	
	public void logUserLogin(int domain,String username)
	{
		try
		{
			defaultProvider.logUserLogin(domain, username) ;
		}
		catch(Exception ee)
		{
			ee.printStackTrace();
		}
	}
	
	
	
	HashMap<String,Long> userdom2lastact = new HashMap<String,Long>() ;
	
	public void logUserActivity(int domain,String username)
	{
		String k = username+"@"+domain ;
		Long lastt = userdom2lastact.get(k) ;
		long ct =System.currentTimeMillis();
		if(lastt!=null&&ct-lastt<25000)
			return ;//没必要记录太快
		
		try
		{
			defaultProvider.logUserActivity(domain, username) ;
			userdom2lastact.put(k, ct);
		}
		catch(Exception ee)
		{
			ee.printStackTrace();
		}
	}
	
	/**
	 * 装载用户基本信息
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public UserProfile loadUserProfile(String username) throws Exception
	{
		return loadUserProfile(0,username);
	}
	
	public UserProfile loadUserProfile(int domain,String username) throws Exception
	{
		UserProvider.UserProfileItem upi = defaultProvider.getUserProfileFromDB(domain,username) ;
		if(upi==null)
			return null ;
		
		User.UserState ust = upi.getUserInfo().getState();
		if(ust==User.UserState.Invalid)//||ust==User.UserState.Deleted)
			return null ;
		
		return UserProfile.getUserProfile(upi);
	}

	public void UpdateUser(User user) throws Exception
	{
		if (user == null)
		{
			throw new IllegalArgumentException("user");
		}

		if ("root".equals(user.getUserName()))
			throw new Exception("root cannot be changed");

		defaultProvider.UpdateUser(user);
	}

	// / <summary>
	// / 为一个用户创建新的Usb密钥
	// / </summary>
	// / <param name="username"></param>
	// / <returns></returns>
	public String createAndSetNewUserUsbKey(String username, String usb_serial)
			throws Exception
	{
		if (username == null || username.equals(""))
			throw new IllegalArgumentException("UserName cannot be null!");

		UsbKey.IUsbKeyValidator ukv = UsbKeyValidatorManager
				.GetUsbKeyValidatorBySetting();
		String s = ukv.createNewUsbKey(username, usb_serial);

		if (defaultProvider.UpdateUserUsbKey(username, ukv
				.getUsbKeyEncTypeName(), s))
			return s;

		return null;
	}
	
	/**
	 * 验证用户密钥
	 * @param keytxt
	 * @return
	 * @throws Exception
	 */
	public String checkUserUsbKey(String keytxt) throws Exception
	{
		if(keytxt==null)
			return null;
		
		keytxt = keytxt.trim() ;
		
		if(Convert.isNullOrEmpty(keytxt))
			return null;
		
		UsbKey.IUsbKeyValidator ukv = UsbKeyValidatorManager
				.GetUsbKeyValidatorBySetting();
		
		UsbKey.KeyItem ki = ukv.parseUsbKey(keytxt) ;
		if(ki==null)
			return null ;
		
		String usern = ki.getUserName() ;
		if(Convert.isNullOrEmpty(usern))
			return null ;
		
		User u = this.GetUser(usern) ;
		if(u==null)
			return null;
		
		if(keytxt.equals(u.usbKeyTxt))
			return usern ;
		else
			return null ;
	}

	// / <summary>
	// / 删除用户的Usb密钥，如果登陆需要Usb密钥，则用户无法登陆
	// / </summary>
	// / <param name="username"></param>
	public boolean removeUserUsbKey(String username) throws Exception
	{
		if (username == null || username.equals(""))
			throw new IllegalArgumentException("UserName cannot be null!");

		return defaultProvider.UpdateUserUsbKey(username, "", "");
	}

	// public boolean UpdateUserUsbKey(String userName, String usbKeyEncType,
	// String usbKeyTxt)
	// {
	// if (String.IsNullOrEmpty(userName))
	// throw new ArgumentException("UserName cannot be null!");

	// return provider.UpdateUserUsbKey(userName, usbKeyEncType, usbKeyTxt);
	// }

	public HashMap<String, String> getProtectedExtInfo(String username)
			throws Exception
	{
		byte[] cont = defaultProvider.getProExtInfo(0,username);
		if (cont == null)
			throw new RuntimeException("no user info found with username="
					+ username);

		HashMap<String, String> p = new HashMap<String, String>();
		ByteArrayInputStream bais = new ByteArrayInputStream(cont);
		InputStreamReader isr = new InputStreamReader(bais, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while ((line = br.readLine()) != null)
		{
			if (line.equals(""))
				continue;

			int i = line.indexOf('=');
			if (i < 0)
				p.put(line, "");
			else
				p.put(line.substring(0, i), line.substring(i + 1));
		}
		return p;
	}

	public boolean setProtectedExtInfo(String username,
			HashMap<String, String> exti) throws Exception
	{
		byte[] cont = null;
		if (exti != null)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			for (Map.Entry<String, String> k2v : exti.entrySet())
			{
				bw.write(k2v.getKey());
				bw.write('=');
				bw.write(k2v.getValue());
				bw.newLine();
			}
			bw.flush();
			cont = baos.toByteArray();
		}

		return defaultProvider.setProExtInfo(0,username, cont);
	}
}