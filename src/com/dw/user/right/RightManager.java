package com.dw.user.right;

import java.util.HashSet;

import com.dw.user.OrgManager;
import com.dw.user.RoleManager;
import com.dw.user.UserManager;
import com.dw.user.UserProfile;
import com.dw.user.sso.SSOManager;

/**
 * 通过规则的权限管理可以达到任意灵活的效果
 * 
 * 但有种情况会造成不便 在大数据量的情况下如果每个数据都有自己的权限规则定义,则会产生如下效果,查找时如果需要做权限 过滤效率低下.
 * 
 * 解决方法: 这个世界总是充满了矛盾--可以用如下方式解决. 如果某种数据量很大,则不可能对其每一条数据进行 单独的权限设置.
 * 此时可以引入level概念,他是个整数,可以为每个数据指定一个level(好比密级概念) 每个密级都对应一个权限规则. ok 问题解决. --
 * 由于level是很有限的(估计10以下),并且一个用户做查询前,先根据用户的基本信息判定该用户可以访问那
 * 几种level,这样也就获得了该用户可以访问的level的子集. 然后查询时,可以把这些信息作为条件
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

		// 对于管理员角色，可以有所有权限
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
	 * 根据权限规则获得满足的用户名列表
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
