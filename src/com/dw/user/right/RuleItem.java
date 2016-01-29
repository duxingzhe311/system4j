package com.dw.user.right;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import com.dw.user.UserProfile;

public abstract class RuleItem {
	public static RuleItem Parse(String s) {
		int i = s.indexOf('=');
		if (i <= 0)
			throw new RuntimeException("invalid RuleItem =" + s
					+ " ,it must be like 'user=xx,yy'");

		String n = s.substring(0, i).trim();
		RuleItem ri = RightRule.GetRuleItemByName(n);
		if (ri == null)
			throw new RuntimeException(
					"Cannot create RuleItem object with RuleItem Name=" + n);

		StringTokenizer st = new StringTokenizer(s.substring(i + 1), ", ");
		List<String> tmpss = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String s0 = st.nextToken();
			if (s0 == null || s0.equals(""))
				continue;

			tmpss.add(s0);
		}

		if (tmpss.size() == 0)
			throw new RuntimeException("No value find in RuleItem String!");

		String[] sss = new String[tmpss.size()];
		tmpss.toArray(sss);
		return ri.CreateMe(sss);
	}

	// protected transient RightRule belongToRR = null ;

	public RuleItem()// (RightRule rr)
	{
		// belongToRR = rr ;
	}

	public abstract String getName();

	public abstract String getTitle(String lang);

	public abstract String getDesc();

	public abstract String getValuesStr();

	public abstract String getValuesDescStr() throws Exception;

	public abstract boolean CheckUser(UserProfile up) throws Exception;

	/**
	 * 获得该规则项满足的所有用户
	 * 
	 * @param rm
	 * @return
	 */
	public abstract HashSet<String> getFitUserNames() throws Exception;

	public abstract RuleItem CreateMe(String[] strvals);

	protected abstract RuleItem CreateEmptyIns();

	// public abstract RightRule.CheckResult CheckUserExclude(UserProfile up);

	public abstract String toString();

	// public abstract String toValueSetStr();

	public abstract String toValueSetDescStr() throws Exception;

	public abstract String ToDescString(String lang) throws Exception;
}
