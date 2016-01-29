package com.dw.user.right;

import java.util.ArrayList;
import java.util.List;

public abstract class IntRuleItem extends RuleItem {
	List<Integer> vals = new ArrayList<Integer>();

	// public IntRuleItem(IList<int> vals)
	// {
	// this.vals.AddRange(vals) ;
	// }

	public IntRuleItem() {
		// super(rr);
	}

	public List<Integer> getValues() {
		return vals;

	}

	public void setValues(List<Integer> vs) {
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
	public String getValuesDescStr() {
		return IntValsToSetDesc(getValues());
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
		IntRuleItem ri = (IntRuleItem) CreateEmptyIns();
		for (String s : strvals) {
			ri.vals.add(Integer.parseInt(s));
		}
		return ri;
	}

	// / <summary>
	// /
	// / </summary>
	// / <returns></returns>
	@Override
	public String ToDescString(String lang) {
		StringBuilder sb = new StringBuilder();
		sb.append(getTitle(lang)).append("ÔÚ(")
				.append(IntValsToSetDesc(getValues())).append(")ÄÚ");
		return sb.toString();
	}

	protected abstract String IntValsToSetDesc(List<Integer> vals);
}