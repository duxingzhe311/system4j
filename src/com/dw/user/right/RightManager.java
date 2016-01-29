package com.dw.user.right;

import java.util.HashSet;

import com.dw.user.OrgManager;
import com.dw.user.RoleManager;
import com.dw.user.UserManager;
import com.dw.user.UserProfile;
import com.dw.user.sso.SSOManager;

/**
 * ͨ�������Ȩ�޹�����Դﵽ��������Ч��
 * 
 * �������������ɲ��� �ڴ�����������������ÿ�����ݶ����Լ���Ȩ�޹�����,����������Ч��,����ʱ�����Ҫ��Ȩ�� ����Ч�ʵ���.
 * 
 * �������: ����������ǳ�����ì��--���������·�ʽ���. ���ĳ���������ܴ�,�򲻿��ܶ���ÿһ�����ݽ��� ������Ȩ������.
 * ��ʱ��������level����,���Ǹ�����,����Ϊÿ������ָ��һ��level(�ñ��ܼ�����) ÿ���ܼ�����Ӧһ��Ȩ�޹���. ok ������. --
 * ����level�Ǻ����޵�(����10����),����һ���û�����ѯǰ,�ȸ����û��Ļ�����Ϣ�ж����û����Է�����
 * ����level,����Ҳ�ͻ���˸��û����Է��ʵ�level���Ӽ�. Ȼ���ѯʱ,���԰���Щ��Ϣ��Ϊ����
 * 
 * 
 * @author Jason Zhu
 */
public class RightManager {
	static Object lockObj = new Object();

	static RightManager defRightMgr = null;

	public static RightManager getDefaultIns() {
		if (defRightMgr != null)
			return defRightMgr;

		synchronized (lockObj) {
			if (defRightMgr != null)
				return defRightMgr;

			defRightMgr = new RightManager(UserManager.getDefaultIns(),
					RoleManager.getDefaultIns(), OrgManager.getDefaultIns());
			return defRightMgr;
		}
	}

	UserManager userMgr = null;
	RoleManager roleMgr = null;
	OrgManager orgMgr = null;

	SSOManager ssoMgr = null;

	public RightManager(UserManager um, RoleManager rm, OrgManager om) {
		userMgr = um;
		roleMgr = rm;
		orgMgr = om;

		ssoMgr = SSOManager.getInstance();
	}

	public SSOManager getSSOManager() {
		return ssoMgr;
	}

	public UserManager getUserManager() {
		return userMgr;
	}

	public RoleManager getRoleManager() {
		return roleMgr;
	}

	public OrgManager getOrgManager() {
		return orgMgr;
	}

	// public UserProfile GetUserProfile(String username)
	// throws Exception
	// {
	// return UserProfile.GetUserProfileByName(this,username);
	// }

	public boolean CheckUserRight(UserProfile up, RightRule rr)
			throws Exception {
		if (up == null)
			return false;

		// ���ڹ���Ա��ɫ������������Ȩ��
		if (up.isAdministrator())
			return true;

		return rr.CheckUserRight(up);
	}

	public boolean CheckUserRight(UserProfile up, String str_rule)
			throws Exception {
		RightRule rr = RightRule.Parse(str_rule);
		return CheckUserRight(up, rr);
	}

	// public boolean CheckUserRightByUserName(String username, String str_rule)
	// throws Exception
	// {
	// UserProfile up = GetUserProfile(username);
	// if (up == null)
	// return false;
	//
	// return CheckUserRight(up, str_rule);
	// }

	/**
	 * ����Ȩ�޹�����������û����б�
	 * 
	 * @return
	 */
	public HashSet<String> getUserNamesByRightRule(String str_rule)
			throws Exception {
		RightRule rr = RightRule.Parse(str_rule);
		if (rr == null)
			return null;

		return rr.GetFitUserNames();
	}
}
