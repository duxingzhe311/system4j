package com.dw.user;

import java.io.*;
import java.util.*;

public class LoginSession
{
	String sessionId;
    String userId;
    int domainId = 0 ;
    String userName;

    HashMap<String, String> extInfo;
    
    transient long lastAcc = -1 ;

    public LoginSession(int domainid,String userid, String username, HashMap<String, String> extinfo)
    {
    	this.domainId = domainid ;
        this.userId = userid;
        this.userName = username;
        this.extInfo = extinfo;
        lastAcc = System.currentTimeMillis() ;
    }

    LoginSession(int domainid,String sessionid, String userid, String username)
    {
    	this.domainId = domainid ;
        this.sessionId = sessionid;
        this.userId = userid;
        this.userName = username;
        lastAcc = System.currentTimeMillis() ;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public String getUserId()
    {
        return userId;
    }
    
    public int getUserDomainId()
    {
    	return domainId ;
    }

    public String getUserName()
    {
        return userName;
    }

    public String GetExtInfo(String ext_name)
    {
        if (extInfo == null)
            return null;

        return extInfo.get(ext_name);
    }


    public HashMap<String, String> getExtInfo()
    {
        return extInfo;
    }
    
    public String toString()
    {
    	return "userid="+userId+" username="+userName ;
    }
}
