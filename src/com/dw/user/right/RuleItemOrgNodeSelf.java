package com.dw.user.right;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dw.user.OrgManager;
import com.dw.user.OrgNode;
import com.dw.user.User;
import com.dw.user.UserProfile;

public class RuleItemOrgNodeSelf extends StrRuleItem {
	public static final String NAME = "orgnode_self_users";

	public RuleItemOrgNodeSelf() {
		super();
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getTitle(String lang) {
		if (lang.startsWith("zh"))
			return "机构(部门)直接用户";
		else
			return "dept inner";
	}

	@Override
	public String getDesc() {
		return "机构(部门)直接用户——本部门内部用户";
	}

	@Override
	public boolean CheckUser(UserProfile up) {
		List<OrgNode> nodes = up.getOrgNodeInfo();
		if (nodes == null || nodes.size() == 0)
			return false;

		for (OrgNode r : nodes) {
			// if(belongToRR.CanRepaint())
			{
				if (getValues().contains(r.getPath()))
					return true;
			}
			// else
			// {
			// if (getValues().contains(""+r.getOrgNodeId()))
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
			OrgManager orgm = OrgManager.getDefaultIns();
			for (String s : ss) {
				// if(belongToRR.CanRepaint())
				{
					OrgNode tmpon = orgm.getOrgNodeByPath(s);
					if (tmpon == null)
						continue;

					List<User> us = orgm
							.GetUsersInOrgNode(tmpon.getOrgNodeId());
					if (us != null) {
						for (User u : us)
							hs.add(u.getUniqueUserName());
					}
				}
				// else
				// {
				// long onid = Long.parseLong(s);
				// List<User> us = orgm.GetUsersInOrgNode(onid);
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
		return new RuleItemOrgNodeSelf();
	}

	// @Override
	protected String StrValsToSetDesc0(List<String> vals) throws Exception {
		if (vals == null || vals.size() <= 0)
			return "";

		StringBuilder sb = new StringBuilder();
		List<OrgNode> rs = new ArrayList<OrgNode>(vals.size());
		OrgManager omgr = OrgManager.getDefaultIns();
		for (String rid : vals) {
			// if(belongToRR.CanRepaint())
			{
				OrgNode r = omgr.getOrgNodeByPath(rid);
				if (r == null)
					continue;

				rs.add(r);
			}
			// else
			// {
			// OrgNode r = omgr.GetOrgNodeById(Long.parseLong(rid));
			// if (r == null)
			// continue;
			//
			// rs.add(r);
			// }
		}

		int c = rs.size();
		if (c == 0)
			return "";

		sb.append(rs.get(0).getOrgNodeName());
		for (int i = 1; i < c; i++) {
			sb.append(',').append(rs.get(i).getOrgNodeName());
		}
		return sb.toString();
	}

	private String extractPathName(String v) {
		if (v == null)
			return "";

		String[] ss = v.split("/");
		// int p = v.lastIndexOf('/',v.length()-2) ;
		// if(p<0)
		// return v ;
		// return v.substring(p+1) ;
		return ss[ss.length - 1];
	}

	@Override
	protected String StrValsToSetDesc(List<String> vals) {
		if (vals == null || vals.size() <= 0)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append(extractPathName(vals.get(0)));
		int c = vals.size();
		for (int i = 1; i < c; i++) {
			sb.append(',').append(extractPathName(vals.get(i)));
		}
		return sb.toString();
	}

	@Override
	public String toValueSetDescStr() throws Exception {
		return StrValsToSetDesc(this.getValues());
	}
}
