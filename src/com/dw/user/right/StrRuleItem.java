package com.dw.user.right;

import java.util.ArrayList;
import java.util.List;

public abstract class StrRuleItem extends RuleItem {
	List<String> vals = new ArrayList<String>();

	// public StrRuleItem(IList<String> vals)
	// {
	// this.vals.AddRange(vals) ;
	// }

	public StrRuleItem()// RightRule rr)
	{
		// super(rr);
	}

	public List<String> getValues() {
		return vals;
	}

	public void setValues(List<String> vs) {
		vals.addAll(vs);
	}

	@Override
	public String getValuesStr() {
		if (vals == null)
			return "";

		int c = vals.size();
		if (c <= 0)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append(vals.get(0));
		for (int i = 1; i < c; i++) {
			sb.append(',').append(vals.get(i));
		}
		return sb.toString();
	}

	@Override
	public String getValuesDescStr() throws Exception {
		return StrValsToSetDesc(getValues());
	}

	public String toString() {
		String n = getName();
		if (n == null || n.equals(""))
			return "";

		if (vals == null)
			return "";

		int c = vals.size();
		if (c == 0)
			return "";

		StringBuilder tmpsb = new StringBuilder();
		tmpsb.append(n).append('=').append(vals.get(0));
		for (int i = 1; i < c; i++) {
			tmpsb.append(',').append(vals.get(i));
		}

		return tmpsb.toString();
	}

	@Override
	public RuleItem CreateMe(String[] strvals) {
		StrRuleItem ri = (StrRuleItem) CreateEmptyIns();
		for (String s : strvals) {
			ri.vals.add(s);
		}
		return ri;
	}

	@Override
	public String ToDescString(String lang) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(getTitle(lang));

		if (lang.startsWith("zh"))
			sb.append("ÔÚ(");
		else
			sb.append(" in(");

		sb.append(StrValsToSetDesc(getValues()));

		if (lang.startsWith("zh"))
			sb.append(")ÄÚ");
		else
			sb.append(")");

		return sb.toString();
	}

	protected abstract String StrValsToSetDesc(List<String> vals)
			throws Exception;
}