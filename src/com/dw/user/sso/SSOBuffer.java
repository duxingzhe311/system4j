package com.dw.user.sso;

import java.io.*;
import java.util.*;

import com.dw.user.*;

/**
 * ����Ѿ���½���û�Profile��Ϣ
 * 
 * @author Jason Zhu
 */
public class SSOBuffer
{
	private Hashtable<String, SOItem> userName2SO = new Hashtable<String, SOItem>();

	public SSOBuffer()
	{

	}

	public UserProfile getUserProfile(String usern)
	{
		SOItem soi = userName2SO.get(usern);
		if (soi == null)
			return null;

		if(soi.isTimeOut())
			return null ;
		
		return soi.getUserProfile();
	}

	public synchronized List<UserProfile> getUserProfiles()
	{
		ArrayList<UserProfile> ups = new ArrayList<UserProfile>() ;
		
		for (Map.Entry<String, SOItem> ens : userName2SO.entrySet())
		{
			SOItem tmpsoi = ens.getValue();
			if (tmpsoi.isTimeOut())
				continue ;
			
			ups.add(tmpsoi.getUserProfile());
		}
		
		return ups ;
	}
	/**
	 * �����Ѿ���½���û�
	 * 
	 * @param usern
	 * @return
	 */
	public UserProfile accessSOUser(String usern)
	{
		SOItem soi = userName2SO.get(usern);
		if (soi == null)
			return null;

		synchronized (this)
		{
			if (soi.isTimeOut())
			{
				userName2SO.remove(usern);
				return null;
			}
			soi.doAccess();
			return soi.getUserProfile();
		}
	}
	
	

	transient int CLEAR_COUNT = 0;

	/**
	 * ��һ���û��ɹ���½��,Ӧ�õ��ø÷���,���м��뵽������
	 * 
	 * @param up
	 */
	public synchronized void setSOUser(UserProfile up)
	{
		SOItem sio = new SOItem(up);
		userName2SO.put(sio.getUserName(), sio);

		CLEAR_COUNT++;

		if (CLEAR_COUNT > 30)
		{// ÿ��½30���û�,��һ���������
			clearTimeInfo();
			CLEAR_COUNT = 0;
		}
	}

	private synchronized void clearTimeInfo()
	{
		ArrayList<String> ns = new ArrayList<String>();
		for (Map.Entry<String, SOItem> ens : userName2SO.entrySet())
		{
			SOItem tmpsoi = ens.getValue();
			if (tmpsoi.isTimeOut())
				ns.add(ens.getKey());
		}

		for (String n : ns)
		{
			userName2SO.remove(n);
		}
	}
}
