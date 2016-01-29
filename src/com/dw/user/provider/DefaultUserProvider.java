package com.dw.user.provider;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.encrypt.MD5;
import com.dw.system.gdb.*;
import com.dw.system.util.IdCreator;
import com.dw.user.*;

public class DefaultUserProvider extends UserProvider
{
	public UserProfileItem getUserProfileFromDB(String userid)
	throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("@UserId", userid);
		DBResult dbr = GDB.getInstance().accessDB("Security_User.GetUserProfileById", ht);
		return transDBRToUserProfileItem(dbr);
	}
	
	public UserProfileItem getUserProfileFromDB(int domain,String username)
	throws Exception
	{
		if(domain<0)
			domain = 0 ;
		Hashtable ht = new Hashtable();
		ht.put("@Domain",domain) ;
		ht.put("@UserName", username);
		DBResult dbr = GDB.getInstance().accessDB("Security_User.GetUserProfileByName", ht);
		return transDBRToUserProfileItem(dbr);
	}

	private UserProfileItem transDBRToUserProfileItem(DBResult dbr) throws Exception
	{
		List<User> us = dbr.transTable2ObjList("user_info", User.class);
		if(us==null||us.size()<=0)
			return null ;
		
		ArrayList<String> roleids = new ArrayList<String>() ;
        DataTable dt = dbr.getResultTable("role_info");
        int rn=0;
        if(dt!=null)
        {
	        rn = dt.getRowNum() ;
	        for(int i = 0 ; i < rn ; i ++)
	        {
	        	DataRow dr = dt.getRow(i);
	        	String n = (String)dr.getValue(0);
	        	roleids.add(n);
	        }
        }
        
        ArrayList<String> orgnids = new ArrayList<String>() ;
        dt = dbr.getResultTable("org_info");
        if(dt!=null)
        {
	        rn = dt.getRowNum() ;
	        for(int i = 0 ; i < rn ; i ++)
	        {
	        	DataRow dr = dt.getRow(i);
	        	String n = (String)dr.getValue(0);
	        	orgnids.add(n);
	        }
        }
        
        return new UserProfileItem(us.get(0),roleids,orgnids);
	}
	
	
	
	@Override
	public String getProviderType() throws Exception
	{
		return "";
	}

	@Override
	public boolean ChangePassword(int domain,String username, String oldPassword,
			String newPassword) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@NewPassword", newPassword);
		ht.put("@UserName", username);
		ht.put("@OldPassword", oldPassword);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.ChangePassword", ht);
		return dbr.getLastRowsAffected() == 1;
	}

	@Override
	public String getPassword(int domain,String username) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", username);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.GetPswByUserName", ht);
		return (String) dbr.getResultFirstColumnOfFirstRow();
	}

	@Override
	public boolean setProExtInfo(int domain,String username, byte[] pext) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", username);
		if (pext != null)
			ht.put("@ProExtInfo", pext);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.SetProExtInfo", ht);
		return dbr.getLastRowsAffected() == 1;
	}

	@Override
	public byte[] getProExtInfo(int domain,String username) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", username);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.GetProExtInfo", ht);
		if(dbr.getResultFirstRow()==null)
			return null ;
		byte[] ret = (byte[]) dbr.getResultFirstColumnOfFirstRow();
		if(ret==null)
			ret = new byte[0];
		
		return ret ;
	}

	@Override
	public boolean ChangePasswordByAdm(int domain,String username, String newpsw)
			throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@NewPassword", newpsw);
		ht.put("@UserName", username);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.ChangePasswordByAdm", ht);
		return dbr.getLastRowsAffected() == 1;
	}

	@Override
	public void resetTempPassword(int domain,String username,String resetpsw) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@ResetPsw", resetpsw);
		ht.put("@UserName", username);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.ResetTempPassword", ht);
		
	}
	
	@Override
	public boolean changePasswordByResetTemp(int domain,String username,String newpsw,String resetpsw) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@NewPassword", newpsw);
		ht.put("@ResetPsw", resetpsw);
		ht.put("@UserName", username);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.ChangePasswordByResetTemp", ht);
		return dbr.getResultFirstColOfFirstRowNumber().intValue() == 1;
	}
	
	
	@Override
	public boolean ChangeUserState(int domain,String username, int state) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@State", state);
		ht.put("@UserName", username);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.ChangeUserState", ht);
		return dbr.getLastRowsAffected() == 1;
	}

	@Override
	public UserProfileItem ValidateUser(int domain,String username, String password,String md5_32_prefix) throws Exception
	{
		if(Convert.isNullOrTrimEmpty(md5_32_prefix))
		{
			Hashtable ht = new Hashtable();
			if(domain<0)
				domain= 0 ;
			ht.put("@Domain", domain);
			ht.put("@UserName", username);
			ht.put("@Password", password);
			DBResult dbr = GDB.getInstance().accessDB("Security_User.CheckPassword", ht);
			return transDBRToUserProfileItem(dbr);
		}
		else
		{
			Hashtable ht = new Hashtable();
			if(domain<0)
				domain= 0 ;
			ht.put("@Domain", domain);
			ht.put("@UserName", username);
			DBResult dbr = GDB.getInstance().accessDB("Security_User.GetUserDetail", ht);
			UserProfileItem upi = transDBRToUserProfileItem(dbr);
			if(upi==null)
				return null ;

			DataTable dtpsw = dbr.getResultTable("psw_info");
			String psw = (String)dtpsw.getFirstColumnOfFirstRow() ;
			if(Convert.isNullOrEmpty(password)&&Convert.isNullOrEmpty(psw))
				return upi ;// no password
			
			if(password.equalsIgnoreCase(MD5.encryptMD5(md5_32_prefix+psw)))
				return upi ;
			return null ;
		}
	}
	
	/**
	 * 对用户登录进行记录
	 * @param domain
	 * @param username
	 */
	@Override
	public void logUserLogin(int domain,String username)
		throws Exception
	{
		
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", username);
		ht.put("@DT", new Date());
		GDB.getInstance().accessDB("Security_User.log_login", ht);
	}
	
	/**
	 * 对用户登出进行记录
	 * @param domain
	 * @param username
	 */
	@Override
	public void logUserActivity(int domain,String username)
	throws Exception
	{
		
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", username);
		ht.put("@DT", new Date());
		GDB.getInstance().accessDB("Security_User.log_activity", ht);
	}

	@Override
	public User CreateUser(int domain,String userName, String password, User.UserState st,
			String fullname,String cnname,
			String enname, String email) throws Exception
	{
		Hashtable ht = new Hashtable();
		String nid = IdCreator.newSeqId() ;
		ht.put("@UserId", nid) ;
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", userName);
		if(st!=null)
			ht.put("@State", st.getIntValue());
		if(fullname!=null)
			ht.put("@FullName", fullname);
		if(cnname!=null)
			ht.put("@CnName", cnname);
		if(enname!=null)
			ht.put("@EnName", enname);
		ht.put("@Password", password);
		ht.put("@Email", email);
		//ht.put("@UserName", userName);
		//ht.put("@UserName", userName);

		DBResult dbr = GDB.getInstance().accessDB("Security_User.AddNewUser",
				ht);
		//DataTable dt = dbr.getResultTable(1);
		
		//Number n = (Number) dt.getFirstColumnOfFirstRow();//dbr.getResultFirstColumnOfFirstRow();
		//long nid = n.longValue();

		return new User(nid,domain, userName,st.getIntValue(),fullname, cnname, enname, email);
	}

	@Override
	public boolean DeleteUser(int domain,String username, boolean deleteAllRelatedData)
			throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", username);
		String udom = username ;
		if(domain>0)
			udom += ("@"+domain) ;
		ht.put("@UserNameWithDomain", udom);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.DeleteUserByUserName", ht);
		return dbr.getLastRowsAffected() == 1;
	}
	
	public boolean DeleteUser(String userid) throws Exception
	{
		User u = this.GetUserById(userid) ;
		if(u==null)
			return false;
		Hashtable ht = new Hashtable();
		ht.put("@UserId", userid);
		String udom = u.getUserName() ;
		int domain = u.getUserDomainId() ;
		if(domain>0)
			udom += ("@"+domain) ;
		ht.put("@UserNameWithDomain", udom);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.DeleteUserByUserId", ht);
		return dbr.getLastRowsAffected() == 1;
	}

	@Override
	public boolean isEnablePasswordReset() throws Exception
	{
		return true;
	}

	@Override
	public boolean isEnablePasswordRetrieval() throws Exception
	{
		return false;
	}

	@Override
	public UserList FindUsersByEmail(int domain,String emailToMatch, int pageidx,
			int pagesize,User.UserState[] ust) throws Exception
	{
		if(Convert.isNullOrEmpty(emailToMatch))
			throw new IllegalArgumentException("email to match is empty!") ;
		
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("$emailToMatch", "'%" + emailToMatch + "%'");
		if(ust!=null&&ust.length>0)
		{
			String tmps = ""+ust[0].getIntValue() ;
			for(int i = 1; i < ust.length ; i ++)
				tmps += (","+ust[i].getIntValue()) ;
			ht.put("$StateInStr", tmps) ;
		}
		DBResult dbr = GDB.getInstance().accessDBPage(
				"Security_User.FindUsersByEmail", ht, pageidx, pagesize);
		List<User> us = (List<User>) dbr.transTable2ObjList(0, User.class);
		int totalc = dbr.getResultFirstTable().getTotalCount();
		return new UserList(us, pageidx, pagesize, totalc);
		
	}

	@Override
	public UserList FindUsersByName(int domain,String usernameToMatch,boolean isname_prefix, int pageidx,
			int pagesize,User.UserState[] ust) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@IsNamePrefix", isname_prefix) ;
		if(isname_prefix)
			ht.put("$usernameToMatch", "'" + usernameToMatch + "%'");
		else
			ht.put("$usernameToMatch", "'%" + usernameToMatch + "%'");
		
		if(ust!=null&&ust.length>0)
		{
			String tmps = ""+ust[0].getIntValue() ;
			for(int i = 1; i < ust.length ; i ++)
				tmps += (","+ust[i].getIntValue()) ;
			ht.put("$StateInStr", tmps) ;
		}
		DBResult dbr = GDB.getInstance().accessDBPage(
				"Security_User.FindUserByUserName", ht, pageidx, pagesize);
		List<User> us = (List<User>) dbr.transTable2ObjList(0, User.class);
		int totalc = dbr.getResultFirstTable().getTotalCount();
		return new UserList(us, pageidx, pagesize, totalc);
	}
	
	@Override
	public int getUserNum(int domainid,User.UserState[] ust) throws Exception
	{
		Hashtable ht = new Hashtable() ;
		if(domainid<0)
			domainid= 0 ;
		ht.put("@Domain", domainid);
		if(ust!=null&&ust.length>0)
		{
			String tmps = ""+ust[0].getIntValue() ;
			for(int i = 1; i < ust.length ; i ++)
				tmps += (","+ust[i].getIntValue()) ;
			ht.put("$StateInStr", tmps) ;
		}
		
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.GetUserNum", ht);
		
		return dbr.getResultFirstColOfFirstRowNumber().intValue() ;
	}

	@Override
	public UserList GetAllUsers(int domain,int pageidx, int pagesize,User.UserState[] ust) throws Exception
	{
		Hashtable ht = new Hashtable() ;
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		if(ust!=null&&ust.length>0)
		{
			String tmps = ""+ust[0].getIntValue() ;
			for(int i = 1; i < ust.length ; i ++)
				tmps += (","+ust[i].getIntValue()) ;
			ht.put("$StateInStr", tmps) ;
		}
		DBResult dbr = GDB.getInstance().accessDBPage(
				"Security_User.GetAllUsers", ht, pageidx, pagesize);
		List<User> us = (List<User>) dbr.transTable2ObjList(0, User.class);
		int totalc = dbr.getResultFirstTable().getTotalCount();
		return new UserList(us, pageidx, pagesize, totalc);
	}
	
	
	@Override
	public UserList GetAllUsersAfter(int domain,String afterusername,int pageidx, int pagesize,User.UserState[] ust) throws Exception
	{
		Hashtable ht = new Hashtable() ;
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		if(afterusername!=null)
			ht.put("@AfterUserName", afterusername);
		if(ust!=null&&ust.length>0)
		{
			String tmps = ""+ust[0].getIntValue() ;
			for(int i = 1; i < ust.length ; i ++)
				tmps += (","+ust[i].getIntValue()) ;
			ht.put("$StateInStr", tmps) ;
		}
		DBResult dbr = GDB.getInstance().accessDBPage(
				"Security_User.GetAllUsersAfter", ht, pageidx, pagesize);
		List<User> us = (List<User>) dbr.transTable2ObjList(0, User.class);
		int totalc = dbr.getResultFirstTable().getTotalCount();
		return new UserList(us, pageidx, pagesize, totalc);
	}

	@Override
	public int GetNumberOfUsersOnline() throws Exception
	{
		throw new Exception("not support here!");
	}

	@Override
	public String GetPassword(int domain,String username, String answer) throws Exception
	{
		throw new Exception("not support here!");
	}

	@Override
	public User GetUser(int domain,String username, boolean userIsOnline) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain= 0 ;
		ht.put("@Domain", domain);
		ht.put("@UserName", username);
		List<User> us = (List<User>) GDB.getInstance().accessDBAsObjList(
				"Security_User.GetUserByName", ht, User.class);
		if (us == null || us.size() <= 0)
			return null;
		return us.get(0);
	}
	
	public User GetUserById(String uid) throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("@UserId", uid);
		List<User> us = (List<User>) GDB.getInstance().accessDBAsObjList(
				"Security_User.GetUserById", ht, User.class);
		if (us == null || us.size() <= 0)
			return null;
		return us.get(0);
	}

	@Override
	public User GetUserByEmail(String email) throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("@Email", email);
		List<User> us = (List<User>) GDB.getInstance().accessDBAsObjList(
				"Security_User.GetUserByEmail", ht, User.class);
		if (us == null || us.size() <= 0)
			return null;
		return us.get(0);
	}

	@Override
	public int getMaxInvalidPasswordAttempts() throws Exception
	{
		return 20;
	}

	@Override
	public int getMinRequiredPasswordLength() throws Exception
	{
		return 3;
	}
	

	
	
	@Override
	public String ResetPassword(int domain,String username, String answer)
			throws Exception
	{
		throw new Exception("not support here!");
	}

	@Override
	public boolean UnlockUser(int domain,String userName) throws Exception
	{
		throw new Exception("not support here!");
	}

	@Override
	public boolean UpdateUser(User user) throws Exception
	{
		DBResult dbr = GDB.getInstance().accessDBWithObjParm(
				"Security_User.UpdateUser", User.class, user);
		return dbr.getLastRowsAffected() == 1;
	}

	@Override
	public void setUserBelongOrgNodeId(String username, String orgnodeid)
			throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("@Domain", 0);
		ht.put("@UserName", username);
		if(Convert.isNotNullEmpty(orgnodeid))
			ht.put("@OrgNodeId", orgnodeid);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.SetUserBelongToOrgNodeId", ht);
		// return dbr.getRowsAffected()==1;
	}

	@Override
	public boolean UpdateUserUsbKey(String userName, String usbKeyEncType,
			String usbKeyTxt) throws Exception
	{
		Hashtable ht = new Hashtable();
		ht.put("@Domain", 0);
		ht.put("@UsbKeyEncType", usbKeyEncType);
		ht.put("@UsbKeyTxt", usbKeyTxt);
		ht.put("@UserName", userName);
		DBResult dbr = GDB.getInstance().accessDB(
				"Security_User.UpdateUserUsbKey", ht);
		return dbr.getLastRowsAffected() == 1;
	}
	/*
	 * 
	 * 
	 * public override string GetPassword(string userName, string
	 * passwordAnswer) { Hashtable ht = new Hashtable(); ht["@UserName"] =
	 * userName; ht["@PasswordAnswer"] = passwordAnswer; DBResult dbr =
	 * GDB.Instance().AccessDB("Security_User.GetPasswordByAnswer", ht); return
	 * dbr.ResultFirstColumnOfFirstRow as string; }
	 * 
	 */
}
