package com.dw.user.right;

import java.util.HashSet;
import java.util.List;

import com.dw.user.UserProfile;

public class RuleItemUser extends StrRuleItem {
	public static final String NAME = "user";

	public RuleItemUser()// (RightRule rr)
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
			return "用户";
		else
			return "user";
	}

	@Override
	public String getDesc() {
		return "用户";
	}

	@Override
	public boolean CheckUser(UserProfile up) {
		if (getValues().contains(up.getUserInfo().getUserName()))
			return true;

		return false;
	}

	@Override
	public HashSet<String> getFitUserNames() {
		HashSet<String> hs = new HashSet<String>();
		List<String> ss = getValues();
		if (ss != null) {
			for (String s : ss)
				hs.add(s);
		}
		return hs;
	}

	@Override
	protected RuleItem CreateEmptyIns() {
		return new RuleItemUser();
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
	public String toValueSetDescStr() {
		return StrValsToSetDesc(this.getValues());
	}
}
