package com.dw.user.right;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.dw.user.UserProfile;

/// <summary>
/// Ȩ�޹����������ʾ��������Ϣ��
///  1��Ȩ���ɶ���������ɣ����������ȼ�����˳������ִ�С��������������Ч���򷵻ؽ���������Ч
///  ��������һ�������С�
///  2��ÿ�������б��������������е�һ��������/�ų�
///  Ȩ�޹���
///     a,���ϵ������ȼ�����
///     b,�������ĳһ��RuleLine���������гɹ��ͷ��ؽ��
///         ����������һ��
///     c,�����������û�����гɹ����򷵻�false���
/// 
/// 
/// ���磺
///   exclude:role=1,2&selfuser_orgnode=3
///   include:orgnode_self_users=3
/// 
///     ��ʾ����3�ڲ�Ա�������������������û������ɫ1��2��Ա��
///     
/// ���磺
///   include:orgnode_with_offspring_users=0
///      ��ʾ�����Žڵ㼰��������ڵ��а������û������㡪��Ҳ������֯����������Ա������������
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
	 * ȷ��Ȩ�޹����Ƿ������ֲ �����,��Ȩ�޹����е�����,���������ƻ�·������,����ʹ��id
	 * ����,��ֲ��Ŀ��ϵͳ��ʱ,ֻҪĿ��ϵͳ�еĽ�ɫ,��֯��������ص�����,��Ϳ�����Ч
	 * 
	 * �ò���Ϊ�����㲻ͬ��ϵͳ���������ͬ�Ľ�ɫ������������ͬ��id,��������ֲ������
	 */
	boolean canRepaint = false;

	public RightRule(boolean canrep) {
		canRepaint = canrep;
	}

	public RightRule() {
		this(false);
	}

	/**
	 * �ж��Ƿ������ֲ
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
	 * ������
	 */
	Vector<RuleLine> ruleLines = new Vector<RuleLine>();

	public Vector<RuleLine> getRuleLines() {
		return ruleLines;
	}

	// / <summary>
	// / �����û���Profile��Ϣ��Ȩ�޹����ж��û��Ƿ���Ȩ��
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
				continue;// ��û�а���ʲôҲû���ų�ʲô
			if (rl.getType() == RuleLine.ExeType.include) {// ����������������������,ͬʱ,�ᱻ�ų����ϵ����ݹ���
				for (String s : hs) {
					if (exc_ns.contains(s))
						continue;// �Ѿ����ų�

					inc_ns.add(s);
				}
			} else {
				for (String s : hs) {
					if (inc_ns.contains(s))
						continue;// �Ѿ�������

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
				sb.append("�� ");
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
				sb.append("�� ");
			else
				sb.append("and ");
			sb.append(ruleLines.get(i).ToDescJSString(lang)).append("\\r\\n");
		}

		return sb.toString();
	}
}
