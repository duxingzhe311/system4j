package com.dw.user.provider;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.*;
import com.dw.user.*;
public class MsAdUserProvider extends UserProvider
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
        const int ALL_USER_TIMEOUT = 30;

        string ldapHost = null;
        string domain = null;
        string connUser = null;
        string connPsw = null;

        string ldapUrl = null;
        string ldapUserName = null;

        //InitUser do it
        Dictionary<string, User> regname2user = null;
        List<User> allUserCache = null;
        DateTime lastGetAllUser = DateTime.MinValue;

        public MsAdUserProvider(string ldaphost, string domain, string conn_user, string conn_psw)
        {
            ldapHost = ldaphost;
            this.domain = domain;
            connUser = conn_user;
            connPsw = conn_psw;

            init();
        }

        void init()
        {
            ldapUrl = "LDAP://" + ldapHost;

            StringBuilder tmpsb = new StringBuilder() ;
            tmpsb.Append("cn=").Append(connUser).Append(",cn=Users") ;
            string[] dms = domain.Split('.') ;
            foreach(string dm in dms)
            {
                tmpsb.Append(",dc=").Append(dm) ;
            }
            ldapUserName = tmpsb.ToString();

            InitUser();
        }

        void SetDirty()
        {
            lastGetAllUser = DateTime.MinValue;
        }

        List<User> InitUser()
        {
            TimeSpan ts = DateTime.Now - lastGetAllUser;
            if (ts.TotalSeconds < ALL_USER_TIMEOUT)
            {
                return allUserCache;
            }

            lock (this)
            {
                ts = DateTime.Now - lastGetAllUser;
                if (ts.TotalSeconds < ALL_USER_TIMEOUT)
                {
                    return allUserCache;
                }

                allUserCache = GetAllUsers();

                Dictionary<string, User> tmpds = new Dictionary<string, User>();
                foreach (User u in allUserCache)
                {
                    tmpds[u.UserName] = u;
                }

                regname2user = tmpds;
                lastGetAllUser = DateTime.Now;

                return allUserCache;
            }
        }

        List<User> GetAllUsers()
        {
            DirectoryEntry de = new DirectoryEntry(ldapUrl, ldapUserName, connPsw, AuthenticationTypes.ServerBind);

            DirectorySearcher srch = new DirectorySearcher(de);
            srch.Filter = "(&(objectCategory=person)(objectClass=user))";

            List<User> us = new List<User>();
            foreach (SearchResult se in srch.FindAll())
            {
                DirectoryEntry ude = se.GetDirectoryEntry();
                //PropertyValueCollection pcc = ;

                //pcc.Value
                PropertyValueCollection pcc = ude.Properties["sAMAccountName"];
                string regname = null;
                if (pcc.Count > 0)
                    regname = pcc[0] as string;

                if (string.IsNullOrEmpty(regname))
                    continue;

                regname = regname.ToLower();

                pcc = ude.Properties["userPrincipalName"];
                string email = null;
                if (pcc.Count > 0)
                    email = pcc[0] as string;

                pcc = ude.Properties["displayName"];
                string disname = null;
                if (pcc.Count > 0)
                    disname = pcc[0] as string;

                //Console.WriteLine(">>{0}--{1}", regname, email);//, ude.Path);
                User u = new User(0, regname, null, disname, email, null);
                us.Add(u);

                if (regname == "administrator")
                {
                    u = new User(0, "admin", "系统管理员", "admin", null, null);
                    us.Add(u);
                }
            }

            return us;
        }


        public override bool ChangePassword(string username, string oldPassword, string newPassword)
        {
            //throw new Exception("The method or operation is not implemented.");
            return false;
        }

        public override bool ValidateUser(string username, string password, out string userid)
        {
            if (username == "admin")
            {
                username = "administrator";
            }

            userid = "0";

            bool flag1 = false;
            NetworkCredential credential1 = new NetworkCredential(username, password, domain);

            LdapConnection connection1 = new LdapConnection(ldapHost);
            try
            {
                connection1.Bind(credential1);
                //bind 成功查看是否需要重新获取所有数据
                User u = this.GetUser(username, false);
                if (u == null)
                {//重新获取所有数据
                    SetDirty();
                }
                flag1 = true;
            }
            catch (LdapException exception2)
            {
                if (exception2.ErrorCode != 0x31)
                {
                    throw;
                }
                return false;
            }
            finally
            {
                connection1.Dispose();
            }
            return flag1;
        }

        public override bool ChangePasswordQuestionAndAnswer(string username, string password, string newPasswordQuestion, string newPasswordAnswer)
        {
            return false;
        }

        public override User CreateUser(string userName, string password, string cnName, string enName, string email, string passwordQuestion, string passwordAnswer, out UserCreateStatus status)
        {
            status = UserCreateStatus.ProviderError;
            return null;
        }

        public override bool DeleteUser(string username, bool deleteAllRelatedData)
        {
            return false;
        }

        public override bool EnablePasswordReset
        {
            get { return false; }
        }

        public override bool EnablePasswordRetrieval
        {
            get { return false; }
        }


        private List<User> ExtractPageList(List<User> allus, int pageidx, int pagesize)
        {
            if (allus == null)
                return null;

            List<User> us = new List<User>();

            int s = pageidx * pagesize;
            int e = s + pagesize;
            for (int i = s; i < allus.Count && i < e; i++)
            {
                us.Add(allus[i]);
            }
            return us;
        }

        public override List<User> FindUsersByEmail(string emailToMatch, int pageIndex, int pageSize, out int totalRecords)
        {
            List<User> alus = InitUser();

            List<User> us = new List<User>();

            string etm = emailToMatch.ToLower();

            foreach (User u in alus)
            {
                string se = u.Email ;
                if(string.IsNullOrEmpty(se))
                    continue; 

                if(se.IndexOf(etm)>=0)
                    us.Add(u);
            }

            totalRecords = us.Count;

            return ExtractPageList(us, pageIndex, pageSize);
        }

        public override List<User> FindUsersByName(string usernameToMatch, int pageIndex, int pageSize, out int totalRecords)
        {
            List<User> alus = InitUser();

            List<User> us = new List<User>();

            string utm = usernameToMatch.ToLower();

            foreach (User u in alus)
            {
                string su = u.UserName;
                if (string.IsNullOrEmpty(su))
                    continue;

                if (su.IndexOf(utm) >= 0)
                    us.Add(u);
            }

            totalRecords = us.Count;

            return ExtractPageList(us, pageIndex, pageSize);
        }

        public override List<User> GetAllUsers(int pageIndex, int pageSize, out int totalRecords)
        {
            List<User> us = InitUser();
            if (us == null)
            {
                totalRecords = 0;
                return null;
            }

            totalRecords = us.Count;
            return ExtractPageList(us, pageIndex, pageSize);
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
            username = username.ToLower();

            List<User> us = InitUser();
            if (us == null)
                return null;

            User u ;
            if (regname2user.TryGetValue(username, out u))
                return u;

            return null;
        }

        public override User GetUserByEmail(string email)
        {
            email = email.ToLower();

            List<User> us = InitUser();
            if (us == null)
                return null;

            foreach (User u in us)
            {
                if (u.Email == email)
                    return u;
            }

            return null;
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
            throw new Exception("The method or operation is not implemented.");
        }

        public override bool UpdateUserUsbKey(string userName, string usbKeyEncType, string usbKeyTxt)
        {
            throw new Exception("not support now!");
        }
*/
}
