package com.dw.user.right;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.dw.user.UserProfile;

/// <summary>
/// 权限规则对象，它表示了如下信息：
///  1，权限由多行命令组成，其运行优先级按照顺序往下执行。如果发现命令有效，则返回结果，如果无效
///  则运行下一条命令行。
///  2，每条命令行必须有两种类型中的一个，包含/排除
///  权限规则：
///     a,从上到下优先级降低
///     b,如果运行某一行RuleLine，发现运行成功就返回结果
///         否则运行下一行
///     c,如果到结束都没有运行成功，则返回false结果
/// 
/// 
/// 例如：
///   exclude:role=1,2&selfuser_orgnode=3
///   include:orgnode_self_users=3
/// 
///     表示部门3内部员工满足条件，但除了用户满足角色1，2的员工
///     
/// 又如：
///   include:orgnode_with_offspring_users=0
///      表示根部门节点及所有子孙节点中包含的用户都满足——也就是组织机构内所有员工都满足条件
/// 
/// </summary>
public class RightRule {
	static Hashtable<String, RuleItem> name2ruleitem = new Hashtable<String, RuleItem>();

	static {
		SetRuleItem(new RuleItemUser());
		SetRuleItem(new RuleItemRole());
		SetRuleItem(new RuleItemOrgNodeSelf());
		SetRuleItem(new RuleItemOrgNodeOffspring());
		SetRuleItem(new RuleItemOrgNodeAncestor());
	}

	static void SetRuleItem(RuleItem ri) {
		name2ruleitem.put(ri.getName(), ri);
	}

	public static RuleItem GetRuleItemByName(String n) {
		return name2ruleitem.get(n);
	}

	public static RightRule parse(String s) {
		return Parse(s);
	}

	public static RightRule Parse(String s) {
		RightRule rr = new RightRule(true);
		if (s == null || (s = s.trim()).equals(""))
			return rr;

		try {
			StringReader strr = new StringReader(s);
			BufferedReader sr = new BufferedReader(strr);
			do {
				String l = sr.readLine();
				if (l == null)
					break;

				l = l.trim();
				if (l.equals(""))
					continue;

				if (l.equals("repaint=true")) {
					rr.setCanRepaint(true);
					continue;
				}

				rr.ruleLines.add(RuleLine.Parse(l));
			} while (true);
		} catch (IOException ioe) {

		}
		return rr;
	}

	/**
	 * 确定权限规则是否可以移植 如果是,则权限规则中的内容,都是用名称或路径定义,而不使用id
	 * 这样,移植到目标系统上时,只要目标系统中的角色,组织机构有相关的内容,则就可以生效
	 * 
	 * 该参数为了满足不同的系统可能针对相同的角色或机构会产生不同的id,而不能移植的问题
	 */
	boolean canRepaint = false;

	public RightRule(boolean canrep) {
		canRepaint = canrep;
	}

	public RightRule() {
		this(false);
	}

	/**
	 * 判断是否可以移植
	 * 
	 * @return
	 */
	public boolean CanRepaint() {
		return canRepaint;
	}

	private void setCanRepaint(boolean bcanrep) {
		canRepaint = bcanrep;
	}

	/**
	 * 规则行
	 */
	Vector<RuleLine> ruleLines = new Vector<RuleLine>();

	public Vector<RuleLine> getRuleLines() {
		return ruleLines;
	}

	// / <summary>
	// / 根据用户的Profile信息和权限规则，判断用户是否有权限
	// / </summary>
	// / <param name="up"></param>
	// / <param name="rr"></param>
	// / <returns></returns>
	public boolean CheckUserRight(UserProfile up) throws Exception {
		for (RuleLine rl : ruleLines) {
			if (rl.CheckUser(up)) {
				if (rl.getType() == RuleLine.ExeType.include)
					return true;
				else
					return false;
			}
		}
		return false;
	}

	public HashSet<String> GetFitUserNames() throws Exception {
		HashSet<String> inc_ns = new HashSet<String>();
		HashSet<String> exc_ns = new HashSet<String>();
		for (RuleLine rl : ruleLines) {
			HashSet<String> hs = rl.GetFitUserNames();
			if (hs == null || hs.size() <= 0)
				continue;// 既没有包含什么也没有排除什么
			if (rl.getType() == RuleLine.ExeType.include) {// 往包含集合中做并集操作,同时,会被排除集合的内容过滤
				for (String s : hs) {
					if (exc_ns.contains(s))
						continue;// 已经被排除

					inc_ns.add(s);
				}
			} else {
				for (String s : hs) {
					if (inc_ns.contains(s))
						continue;// 已经被包含

					exc_ns.add(s);
				}
			}
		}
		return inc_ns;
	}

	// public boolean CheckUserRight(String username,List<Role>
	// roles,List<OrgNode> orgns)
	// {
	// return false;
	// }

	public String toString() {
		StringBuilder tmpsb = new StringBuilder();
		if (this.canRepaint)
			tmpsb.append("repaint=true\r\n");

		if (ruleLines == null || ruleLines.size() == 0)
			return tmpsb.toString();

		for (RuleLine rl : ruleLines) {
			tmpsb.append(rl.toString());
			tmpsb.append("\r\n");
		}

		return tmpsb.toString();
	}

	public String ToDescString(String lang) throws Exception {
		int c = ruleLines.size();
		if (c <= 0) {
			return "";
		}

		if (lang == null)
			lang = "";

		StringBuilder sb = new StringBuilder();

		sb.append(ruleLines.get(0).ToDescString(lang)).append("\r\n");
		for (int i = 1; i < c; i++) {
			if (lang.startsWith("zh"))
				sb.append("或 ");
			else
				sb.append("or ");

			sb.append(ruleLines.get(i).ToDescString(lang)).append("\r\n");
		}

		return sb.toString();
	}

	public String ToDescJSString(String lang) throws Exception {
		int c = ruleLines.size();
		if (c <= 0) {
			return "";
		}

		if (lang == null)
			lang = "";

		StringBuilder sb = new StringBuilder();

		sb.append(ruleLines.get(0).ToDescJSString(lang)).append("\\r\\n");
		for (int i = 1; i < c; i++) {
			if (lang.startsWith("zh"))
				sb.append("或 ");
			else
				sb.append("and ");
			sb.append(ruleLines.get(i).ToDescJSString(lang)).append("\\r\\n");
		}

		return sb.toString();
	}
}
