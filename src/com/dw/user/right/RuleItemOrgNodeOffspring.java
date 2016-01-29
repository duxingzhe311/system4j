package com.dw.user.right;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dw.user.OrgManager;
import com.dw.user.OrgNode;
import com.dw.user.User;
import com.dw.user.UserProfile;

public class RuleItemOrgNodeOffspring extends StrRuleItem {
	public static final String NAME = "orgnode_with_offspring_users";

	public RuleItemOrgNodeOffspring() {

	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getTitle(String lang) {
		if (lang.startsWith("zh"))
			return "机构(部门)及子孙部门";
		else
			return "dept with offspring";
	}

	@Override
	public String getDesc() {
		return "机构(部门)，该部门及以下的子孙部门中的所有用户都被排除";
	}

	@Override
	public boolean CheckUser(UserProfile up) throws Exception {
		List<OrgNode> nodes = up.getOrgNodeInfo();
		if (nodes == null || nodes.size() == 0)
			return false;

		// 查找用户所在每个机构节点，并向上查找，判断是否接点在Values中
		for (OrgNode r : nodes) {
			// 由于Profile中的节点信息不完整,需要从OrgManager重新获得进行处理
			OrgNode tmpr = OrgManager.getDefaultIns().GetOrgNodeById(
					r.getOrgNodeId());
			do {
				// if(belongToRR.CanRepaint())
				{
					if (getValues().contains("" + tmpr.getPath()))
						return true;
				}
				// else
				// {
				// if (getValues().contains(""+tmpr.getOrgNodeId()))
				// return true;
				// }

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
			OrgManager orgm = OrgManager.getDefaultIns();
			ArrayList<String> onids = new ArrayList<String>();
			for (String s : ss) {
				// if(belongToRR.CanRepaint())
				{
					OrgNode tmpn = orgm.getOrgNodeByPath(s);
					if (tmpn == null)
						continue;

					if (!onids.contains(tmpn.getOrgNodeId()))
						onids.add(tmpn.getOrgNodeId());

					List<OrgNode> ons = tmpn.getOffspringOrgNodes();
					if (ons != null) {
						for (OrgNode n0 : ons) {
							String tmpnid = n0.getOrgNodeId();
							if (!onids.contains(tmpnid))
								onids.add(tmpnid);
						}
					}
				}
				// else
				// {
				// long onid = Long.parseLong(s);
				// OrgNode tmpn = orgm.GetOrgNodeById(onid);
				// if(tmpn==null)
				// continue ;
				//
				// if(!onids.contains(onid))
				// onids.add(onid);
				//
				// List<OrgNode> ons = tmpn.getOffspringOrgNodes();
				// if(ons!=null)
				// {
				// for(OrgNode n0:ons)
				// {
				// long tmpnid = n0.getOrgNodeId();
				// if(!onids.contains(tmpnid))
				// onids.add(tmpnid);
				// }
				// }
				// }
			}

			// 循环
			List<User> us = orgm.GetUsersInOrgNodes(onids);
			if (us != null) {
				for (User u : us)
					hs.add(u.getUniqueUserName());
			}
		}
		return hs;
	}

	@Override
	protected RuleItem CreateEmptyIns() {
		return new RuleItemOrgNodeOffspring();
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