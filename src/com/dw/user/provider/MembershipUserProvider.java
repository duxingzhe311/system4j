package com.dw.user.provider;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.*;
import com.dw.user.*;

public class MembershipUserProvider extends UserProvider
{

	@Override
	public String getProviderType() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean ChangePassword(int domain,String username, String oldPassword, String newPassword) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void resetTempPassword(int domain,String username,String resetpsw) throws Exception
	{
		
	}
	
	@Override
	public boolean changePasswordByResetTemp(int domain,String username,String newpsw,String resetpsw) throws Exception
	{
		return false;
	}

	public User GetUserById(String uid) throws Exception
	{
		return null;
	}
	
	@Override
	public boolean ChangePasswordByAdm(int domain,String username, String newpsw) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean ChangeUserState(int domain,String username, int state) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UserProfileItem ValidateUser(int domain,String username, String password,String md5_32_prefix) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User CreateUser(int domain,String userName, String password,User.UserState st,
			String fullname,String cnName, String enName, String email) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean DeleteUser(int domain,String username, boolean deleteAllRelatedData) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean DeleteUser(String userid) throws Exception
	{
		return false;
	}
	
	@Override
	public boolean isEnablePasswordReset() throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnablePasswordRetrieval() throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UserList FindUsersByEmail(int domain,String emailToMatch, int pageIndex, int pageSize,User.UserState[] ust) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserList FindUsersByName(int domain,String usernameToMatch,boolean isname_prefix, int pageIndex, int pageSize,User.UserState[] ust) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public int getUserNum(int domainid,User.UserState[] ust) throws Exception
	{
		return -1 ;
	}

	@Override
	public UserList GetAllUsers(int domain,int pageIndex, int pageSize,User.UserState[] ust) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UserList GetAllUsersAfter(int domain,String afterusername,int pageidx, int pagesize,User.UserState[] ust) throws Exception
	{
		return null ;
	}

	@Override
	public int GetNumberOfUsersOnline() throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String GetPassword(int domain,String username, String answer) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User GetUser(int domain,String username, boolean userIsOnline) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User GetUserByEmail(String email) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxInvalidPasswordAttempts() throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinRequiredPasswordLength() throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String ResetPassword(int domain,String username, String answer) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean UnlockUser(int domain,String userName) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean UpdateUser(User user) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUserBelongOrgNodeId(String username, String orgnodeid) throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean UpdateUserUsbKey(String userName, String usbKeyEncType, String usbKeyTxt) throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}
	/*
        private User MembershipUser2User(MembershipUser mu)
        {
            return new User(0, mu.UserName, "", "", mu.Email, mu.PasswordQuestion);
        }

        private List<User> MembershipUserCollection2UserList(MembershipUserCollection muc)
        {
            List<User> us = new List<User>() ;
            if(muc==null||muc.Count==0)
                return us ;

            foreach(MembershipUser mu in muc)
            {
                us.Add(MembershipUser2User(mu));
            }

            return us;
        }
        

        public override bool ChangePassword(string username, string oldPassword, string newPassword)
        {
            //Membership.
            return false;
        }

        public override bool ValidateUser(string username, string password, out string userid)
        {
            userid = username;
            return Membership.ValidateUser(username, password);
        }

        public override bool ChangePasswordQuestionAndAnswer(string username, string password, string newPasswordQuestion, string newPasswordAnswer)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public override User CreateUser(string userName, string password, string cnName, string enName, string email, string passwordQuestion, string passwordAnswer, out UserCreateStatus status)
        {
            MembershipCreateStatus  mcs;
            MembershipUser mu = Membership.CreateUser(userName, password,email,passwordQuestion,passwordAnswer,true,out mcs);

            if (mcs == MembershipCreateStatus.Success)
            {
                User u = new User(0, userName, cnName, enName, email, passwordQuestion);

                status = UserCreateStatus.Success;
                return u;
            }

            status = (UserCreateStatus)mcs;
            return null;
        }

        public override bool DeleteUser(string username, bool deleteAllRelatedData)
        {
            return Membership.DeleteUser(username, deleteAllRelatedData);
        }

        public override bool EnablePasswordReset
        {
            get { throw new Exception("The method or operation is not implemented."); }
        }

        public override bool EnablePasswordRetrieval
        {
            get { throw new Exception("The method or operation is not implemented."); }
        }

        public override List<User> FindUsersByEmail(string emailToMatch, int pageIndex, int pageSize, out int totalRecords)
        {
            MembershipUserCollection muc = Membership.FindUsersByEmail(emailToMatch, pageIndex, pageSize, out totalRecords);
            return MembershipUserCollection2UserList(muc);
        }

        public override List<User> FindUsersByName(string usernameToMatch, int pageIndex, int pageSize, out int totalRecords)
        {
            MembershipUserCollection muc = Membership.FindUsersByName(usernameToMatch, pageIndex, pageSize, out totalRecords);
            return MembershipUserCollection2UserList(muc);
        }

        public override List<User> GetAllUsers(int pageIndex, int pageSize, out int totalRecords)
        {
            MembershipUserCollection muc = Membership.GetAllUsers(pageIndex, pageSize, out totalRecords);
            return MembershipUserCollection2UserList(muc);
        }

        public override int GetNumberOfUsersOnline()
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public override string GetPassword(string username, string answer)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public override User GetUser(string username, bool userIsOnline)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public override User GetUserByEmail(string email)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public override int MaxInvalidPasswordAttempts
        {
            get { throw new Exception("The method or operation is not implemented."); }
        }

        public override int MinRequiredPasswordLength
        {
            get { throw new Exception("The method or operation is not implemented."); }
        }

        public override string ResetPassword(string username, string answer)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public override bool UnlockUser(string userName)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        public override bool UpdateUser(User user)
        {
            //Membership.UpdateUser(
            return false;
        }

        public override bool UpdateUserUsbKey(string userName, string usbKeyEncType, string usbKeyTxt)
        {
            throw new Exception("not support now!");
        }
    */
}
