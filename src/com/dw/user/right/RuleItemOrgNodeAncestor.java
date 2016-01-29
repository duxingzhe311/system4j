package com.dw.user.right;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dw.user.OrgManager;
import com.dw.user.OrgNode;
import com.dw.user.User;
import com.dw.user.UserProfile;

public class RuleItemOrgNodeAncestor extends StrRuleItem {
	public static final String NAME = "orgnode_with_ancestor_users";

	public RuleItemOrgNodeAncestor() {
		// super(rr);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getTitle(String lang) {
		if (lang.startsWith("zh"))
			return "机构(部门)及高层部门用户";
		else
			return "dept with superior";
	}

	@Override
	public String getDesc() {
		return "机构(部门)直接用户——本部门内部用户";
	}

	@Override
	public boolean CheckUser(UserProfile up) throws Exception {
		List<OrgNode> nodes = up.getOrgNodeInfo();
		if (nodes == null || nodes.size() == 0)
			return false;

		// 查找Values中的在每个机构节点，并向上查找，判断是否命中用户所在节点
		for (String v : getValues()) {
			OrgNode tmpr = null;
			// if(belongToRR.CanRepaint())
			{
				tmpr = OrgManager.getDefaultIns().getOrgNodeByPath(v);
			}
			// else
			// {
			// tmpr =
			// OrgManager.getDefaultIns().GetOrgNodeById(Long.parseLong(v));
			// }
			if (tmpr == null)
				continue;

			do {
				if (nodes.contains(tmpr))
					return true;

				tmpr = tmpr.getParentNode();
			} while (tmpr != null);
		}

		return false;
	}

	@Override
	public HashSet<String> getFitUserNames() throws Exception {
		HashSet<String> hs = new HashSet<String>();

		List<String> ss = getValues();
		if (ss != null) {
			OrgManager omgr = OrgManager.getDefaultIns();
			ArrayList<String> onids = new ArrayList<String>();
			for (String s : ss) {
				// if(belongToRR.CanRepaint())
				{
					OrgNode tmpn = omgr.getOrgNodeByPath(s);
					if (tmpn == null)
						continue;

					if (!onids.contains(tmpn.getOrgNodeId()))
						onids.add(tmpn.getOrgNodeId());

					while ((tmpn = tmpn.getParentNode()) != null) {
						String tmpnid = tmpn.getOrgNodeId();
						if (!onids.contains(tmpnid))
							onids.add(tmpnid);
					}
				}
				// else
				// {
				// long onid = Long.parseLong(s);
				// OrgNode tmpn = omgr.GetOrgNodeById(onid);
				// if(tmpn==null)
				// continue ;
				//
				// if(!onids.contains(onid))
				// onids.add(onid);
				//
				// while((tmpn=tmpn.getParentNode())!=null)
				// {
				// long tmpnid = tmpn.getOrgNodeId();
				// if(!onids.contains(tmpnid))
				// onids.add(tmpnid);
				// }
				// }
			}

			// 循环
			List<User> us = omgr.GetUsersInOrgNodes(onids);
			if (us != null) {
				for (User u : us)
					hs.add(u.getUniqueUserName());
			}
		}
		return hs;
	}

	@Override
	protected RuleItem CreateEmptyIns() {
		return new RuleItemOrgNodeAncestor();
	}

	// @Override
	protected String StrValsToSetDesc0(List<String> vals) throws Exception {
		if (vals == null || vals.size() <= 0)
			return "";

		StringBuilder sb = new StringBuilder();
		List<OrgNode> rs = new ArrayList<OrgNode>(vals.size());
		OrgManager omgr = OrgManager.getDefaultIns();
		for (String rid : vals) {
			OrgNode r = null;
			// if(belongToRR.CanRepaint())
			{
				r = omgr.getOrgNodeByPath(rid);
			}
			// else
			// {
			// r = omgr.GetOrgNodeById(Long.parseLong(rid));
			// }
			if (r == null)
				continue;

			rs.add(r);
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
