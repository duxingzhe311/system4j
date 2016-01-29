package com.dw.user.right;

/**
 * 由于权限规则,除了内部用Id表示的内容外,还有一个对应的自然语言的描述 该接口主要提供了从内部表示的权限信息到自然语言描述信息的支持
 * 
 * @author Jason Zhu
 * 
 */
public interface IRightDescProvider {
	/**
	 * 
	 * @param rl
	 *            规则行,
	 * @param lang
	 *            描述的语言
	 * @return
	 */
	public String getRuleLineDesc(RuleLine rl, String lang);

	/**
	 * 
	 * @param rr
	 *            权限规则
	 * @param lang
	 *            描述语言
	 * @return
	 */
	public String getRightRuleDesc(RightRule rr, String lang);
}