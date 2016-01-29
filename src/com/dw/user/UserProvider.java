package com.dw.user;

import java.util.List;

public abstract class UserProvider
{
	public static class UserProfileItem
	{
		User userInfo = null ;
		List<String> roleIds = null ;
		List<String> orgNodeIds = null ;
		
		public UserProfileItem(User u,List<String> roleids,List<String> orgn_ids)
		{
			userInfo = u ;
			roleIds = roleids ;
			orgNodeIds = orgn_ids ;
		}
		
		public User getUserInfo()
		{
			return userInfo ;
		}
		
		public List<String> getRoleIds()
		{
			return roleIds ;
		}
		
		public List<String> getOrgNodeIds()
		{
			return orgNodeIds ;
		}
	}
	
	public UserProfileItem getUserProfileFromDB(long userid)
		throws Exception
	{
		throw new RuntimeException("not implements");
	}
	
	public UserProfileItem getUserProfileFromDB(int domain,String username)
	throws Exception
	{
		throw new RuntimeException("not implements");
	}
	
	
	public abstract String getProviderType() throws Exception;
	
	public abstract boolean ChangePassword(int domain,String username, String oldPassword,
			String newPassword) throws Exception;
	
	public String getPassword(int domain,String username) throws Exception
	{
		return null ;
	}
	
	public boolean setProExtInfo(int domain,String username,byte[] pext)throws Exception
	{
		return false;
	}
	
	
	/**
	 * �������null����ʾ�����ڼ�¼
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public byte[] getProExtInfo(int domain,String username)throws Exception
	{
		return null ;
	}

	public abstract boolean ChangePasswordByAdm(int domain,String username,String newpsw)
		throws Exception;
	
	public abstract boolean ChangeUserState(int domain,String username,int state)
		throws Exception;
	/**
	 * �����û���,���룬����û��Ƿ���Ч
	 * 
	 * @param username
	 * @param password
	 * @return �����û�id�����ʧ���򷵻�null
	 */
	public abstract UserProfileItem ValidateUser(int domain,String username, String password,String md5_32_prefix) throws Exception;

	/**
	 * ���û���¼���м�¼
	 * @param domain
	 * @param username
	 */
	public void logUserLogin(int domain,String username)throws Exception
	{}
	
	/**
	 * ���û��ǳ����м�¼
	 * @param domain
	 * @param username
	 */
	public void logUserActivity(int domain,String username)throws Exception
	{}
//	public abstract boolean ChangePasswordQuestionAndAnswer(String username,
//			String password, String newPasswordQuestion,
//			String newPasswordAnswer) throws Exception;


	public abstract User CreateUser(int domain,String userName, String password,User.UserState st,
			String fullname,String cnName, String enName, String email) throws Exception;

	public abstract boolean DeleteUser(String userid) throws Exception;
	
	public abstract boolean DeleteUser(int domain,String username,
			boolean deleteAllRelatedData) throws Exception;

	public abstract boolean isEnablePasswordReset() throws Exception;
	
	public abstract void resetTempPassword(int domain,String username,String resetpsw) throws Exception;
	
	public abstract boolean changePasswordByResetTemp(int domain,String username,String newpsw,String resetpsw) throws Exception;
	
	

	public abstract boolean isEnablePasswordRetrieval() throws Exception;

	public abstract UserList FindUsersByEmail(int domain,String emailToMatch,
			int pageIndex, int pageSize,User.UserState[] ust) throws Exception;

	public UserList FindUsersByName(int domain,
			String usernameToMatch,
			int pageIndex, int pageSize,User.UserState[] ust) throws Exception
	{
		return FindUsersByName(domain,
				usernameToMatch,false,
				pageIndex, pageSize,ust);
	}
	public abstract UserList FindUsersByName(int domain,
			String usernameToMatch,boolean isname_prefix,
			int pageIndex, int pageSize,User.UserState[] ust) throws Exception;

	public abstract int getUserNum(int domainid,User.UserState[] ust)throws Exception;
	
	public abstract UserList GetAllUsers(int domain,int pageIndex, int pageSize,User.UserState[] ust)throws Exception;

	public abstract UserList GetAllUsersAfter(int domain,String afterusername,int pageidx, int pagesize,User.UserState[] ust) throws Exception;
	
	public abstract int GetNumberOfUsersOnline() throws Exception;

	public abstract String GetPassword(int domain,String username, String answer) throws Exception;

	public abstract User GetUser(int domain,String username, boolean userIsOnline) throws Exception;

	public abstract User GetUserById(String uid) throws Exception;
	
	public abstract User GetUserByEmail(String email) throws Exception;

	public abstract int getMaxInvalidPasswordAttempts() throws Exception;

	public abstract int getMinRequiredPasswordLength() throws Exception;

	public abstract String ResetPassword(int domain,String username, String answer) throws Exception;

	public abstract boolean UnlockUser(int domain,String userName) throws Exception;

	public abstract boolean UpdateUser(User user) throws Exception;

	/**
	 * �����û������Ļ����ڵ㡪���û����ԺͶ�������ڵ㽨����ϵ����ֻ��һ�����������ڵ�
	 * @param username
	 * @param orgnodeid
	 */
	public abstract void setUserBelongOrgNodeId(String username,String orgnodeid)
		throws Exception;
	// / <summary>
	// / �����û�Usb��Կ��Ϣ
	// / </summary>
	// / <param name="userName"></param>
	// / <param name="usbKeyEncType"></param>
	// / <param name="usbKeyTxt"></param>
	// / <returns></returns>
	public abstract boolean UpdateUserUsbKey(String userName,
			String usbKeyEncType, String usbKeyTxt) throws Exception;
	// public abstract int PasswordAttemptWindow
	// {
	// get { throw new Exception("The method or operation is not implemented.");
	// }
	// }

	// public override MembershipPasswordFormat PasswordFormat
	// {
	// get { throw new Exception("The method or operation is not implemented.");
	// }
	// }

	// public override String PasswordStrengthRegularExpression
	// {
	// get { throw new Exception("The method or operation is not implemented.");
	// }
	// }

	// public override bool RequiresQuestionAndAnswer
	// {
	// get { throw new Exception("The method or operation is not implemented.");
	// }
	// }

	// public override bool RequiresUniqueEmail
	// {
	// get { throw new Exception("The method or operation is not implemented.");
	// }
	// }

}
