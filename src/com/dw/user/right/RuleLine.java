package com.dw.user.right;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dw.user.UserProfile;

/// <summary>
/// 权限规则的一行,如：include:user=xx,yy & role=1,3
/// </summary>
public class RuleLine {
	public enum ExeType {
		exclude(0), include(1);

		private final int val;

		ExeType(int v) {
			val = v;
		}
	}

	public static RuleLine Parse(String s) {
		int i = s.indexOf(':');
		if (i <= 0)
			throw new RuntimeException("Invalid RuleLine=" + s);

		RuleLine rl = new RuleLine();

		String t = s.substring(0, i).trim().toLowerCase();
		if ("include".equals(t))
			rl.exeType = ExeType.include;
		else if ("exclude".equals(t))
			rl.exeType = ExeType.exclude;
		else
			throw new RuntimeException("Invalid RuleLine=" + s
					+ "  unknown ExeType=" + t);

		s = s.substring(i + 1);

		String[] ss = s.split("&");
		for (String tmps : ss) {
			String ts = tmps.trim();
			if (ts == null || ts.equals(""))
				continue;

			RuleItem ri = RuleItem.Parse(ts);
			rl.ruleItems.add(ri);
		}

		if (rl.ruleItems.size() == 0)
			throw new RuntimeException("Invalid RuleLine=" + s
					+ " no RuleItem String found!");

		return rl;
	}

	// private transient RightRule belongToRightRule = null ;

	List<RuleItem> ruleItems = new ArrayList<RuleItem>();
	ExeType exeType = ExeType.include;

	public RuleLine() {
		// belongToRightRule = rr ;
	}

	public ExeType getType() {
		return exeType;
	}

	public void setType(ExeType et) {
		exeType = et;
	}

	public List<RuleItem> getRuleItems() {
		return ruleItems;
	}

	// / <summary>
	// / 判断该规则行是否有效
	// / </summary>
	public boolean IsValid() {
		if (ruleItems.size() <= 0)
			return false;

		return true;
	}

	// / <summary>
	// / 根据用户的Profile检查用户权限——规则为必须所有的RuleItem都满足才算成功
	// / </summary>
	// / <param name="up"></param>
	// / <returns></returns>
	public boolean CheckUser(UserProfile up) throws Exception {
		for (RuleItem ri : ruleItems) {
			if (!ri.CheckUser(up))
				return false;
		}

		return true;
	}

	public HashSet<String> GetFitUserNames() throws Exception {
		HashSet<String> rets = null;
		for (RuleItem ri : ruleItems) {
			HashSet<String> hs = ri.getFitUserNames();
			if (hs == null || hs.size() <= 0)
				return null;

			if (rets == null) {
				rets = hs;
				continue;
			}

			if (rets.size() <= 0)
				return null;
			// 交集运算
			HashSet<String> tmphs = new HashSet<String>();
			for (String s : hs) {
				if (rets.contains(s))
					tmphs.add(s);
			}

			rets = tmphs;

		}

		return rets;
	}

	public String toString() {
		if (ruleItems == null)
			return "";

		int c = ruleItems.size();
		if (c == 0)
			return "";

		StringBuilder tmpsb = new StringBuilder();
		tmpsb.append(exeType).append(':').append(ruleItems.get(0).toString());

		for (int i = 1; i < c; i++)
			tmpsb.append('&').append(ruleItems.get(i).toString());

		return tmpsb.toString();
	}

	public String ToDescString(String lang) throws Exception {
		int c = ruleItems.size();
		if (c <= 0)
			return "";

		if (lang == null)
			lang = "";

		StringBuilder sb = new StringBuilder();

		if (exeType == ExeType.include) {
			if (lang.startsWith("zh")) {
				sb.append("包含以下条件用户:");
			} else {
				sb.append("include:");
			}
		} else {
			if (lang.startsWith("zh"))
				sb.append("排除以下条件用户:");
			else
				sb.append("exclude:");
		}

		sb.append("{").append(ruleItems.get(0).ToDescString(lang));
		for (int i = 1; i < c; i++) {
			if (lang.startsWith("zh"))
				sb.append(" 且 ");
			else
				sb.append(" and ");

			sb.append(ruleItems.get(i).ToDescString(lang));
		}

		sb.append("}");

		return sb.toString();
	}

	public String ToDescJSString(String lang) throws Exception {
		int c = ruleItems.size();
		if (c <= 0)
			return "";

		if (lang == null)
			lang = "";

		StringBuilder sb = new StringBuilder();

		if (exeType == ExeType.include) {
			sb.append("包含以下条件用户:");
		} else {
			sb.append("排除以下条件用户:");
		}

		sb.append("{").append(ruleItems.get(0).ToDescString(lang));
		for (int i = 1; i < c; i++) {
			sb.append(" 且 ").append(ruleItems.get(i).ToDescString(lang));
		}

		sb.append("}");

		return sb.toString();
	}
}