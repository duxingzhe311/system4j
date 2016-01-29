package com.dw.user.right;

/**
 * ����Ȩ�޹���,�����ڲ���Id��ʾ��������,����һ����Ӧ����Ȼ���Ե����� �ýӿ���Ҫ�ṩ�˴��ڲ���ʾ��Ȩ����Ϣ����Ȼ����������Ϣ��֧��
 * 
 * @author Jason Zhu
 * 
 */
public interface IRightDescProvider {
	/**
	 * 
	 * @param rl
	 *            ������,
	 * @param lang
	 *            ����������
	 * @return
	 */
	public String getRuleLineDesc(RuleLine rl, String lang);

	/**
	 * 
	 * @param rr
	 *            Ȩ�޹���
	 * @param lang
	 *            ��������
	 * @return
	 */
	public String getRightRuleDesc(RightRule rr, String lang);
}