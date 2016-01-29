package com.dw.user.right;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dw.user.Role;
import com.dw.user.RoleManager;
import com.dw.user.User;
import com.dw.user.UserProfile;

public class RuleItemRole extends StrRuleItem {
	public static final String NAME = "role";

	public RuleItemRole()// (RightRule rr)
	{
		// super(rr);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getTitle(String lang) {
		if (lang.startsWith("zh"))
			return "½ÇÉ«";
		else
			return "role";
	}

	@Override
	public String getDesc() {
		return "½ÇÉ«";
	}

	@Override
	public boolean CheckUser(UserProfile up) {
		List<Role> rs = up.getRoleInfo();
		if (rs == null || rs.size() == 0)
			return false;

		for (Role r : rs) {

			// if(belongToRR.CanRepaint())
			{
				if (getValues().contains(r.getName()))
					return true;
			}
			// else
			// {
			// if (getValues().contains(""+r.getId()))
			// return true;
			// }
		}

		return false;
	}

	@Override
	public HashSet<String> getFitUserNames() throws Exception {
		HashSet<String> hs = new HashSet<String>();

		List<String> ss = getValues();
		if (ss != null) {
			RoleManager rolem = RoleManager.getDefaultIns();
			for (String s : ss) {
				// if(belongToRR.CanRepaint())
				{
					Role r = rolem.GetRoleByName(s);
					if (r == null)
						continue;
					List<User> us = rolem.GetUsersInRole(r.getId());
					if (us != null) {
						for (User u : us)
							hs.add(u.getUniqueUserName());
					}
				}
				// else
				// {
				// long rid = Long.parseLong(s);
				//
				// List<User> us = rolem.GetUsersInRole(rid);
				// if(us!=null)
				// {
				// for(User u:us)
				// hs.add(u.getUniqueUserName());
				// }
				// }
			}
		}
		return hs;
	}

	@Override
	protected RuleItem CreateEmptyIns() {
		return new RuleItemRole();
	}

	// @Override
	protected String StrValsToSetDesc0(List<String> vals) throws Exception {
		if (vals == null || vals.size() <= 0)
			return "";

		StringBuilder sb = new StringBuilder();
		List<Role> rs = new ArrayList<Role>(vals.size());
		for (String rid : vals) {
			Role r = null;
			// if(belongToRR.CanRepaint())
			r = RoleManager.getDefaultIns().GetRoleByName(rid);
			// else
			// r = RoleManager.getDefaultIns().GetRole(Long.parseLong(rid));
			if (r == null)
				continue;

			rs.add(r);
		}

		int c = rs.size();
		if (c == 0)
			return "";

		sb.append(rs.get(0).getName());
		for (int i = 1; i < c; i++) {
			sb.append(',').append(rs.get(i).getName());
		}
		return sb.toString();
	}

	@Override
	protected String StrValsToSetDesc(List<String> vals) {
		if (vals == null || vals.size() <= 0)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append(vals.get(0));
		int c = vals.size();
		for (int i = 1; i < c; i++) {
			sb.append(',').append(vals.get(i));
		}
		return sb.toString();
	}

	@Override
	public String toValueSetDescStr() throws Exception {
		return StrValsToSetDesc(this.getValues());
	}
}
