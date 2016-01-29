package com.dw.system.codedom;

import com.dw.system.codedom.parser.Node;


public interface IDomNode
{
	public IDomNode jjtGetParent();

	/**
	 * This method returns a child node. The children are numbered from zero,
	 * left to right.
	 */
	public IDomNode jjtGetChild(int i);
	
	public int getChildIdx(Node n);

	/** Return the number of children the node has. */
	public int jjtGetNumChildren();
	
	public void dump();
	
	/**
	 * �Խڵ�����Ż��Ȳ�����Dom�̳нڵ������Ҫ�Խ��������ݽ����Ż�����Ӧ��ʵ��/�̳иýӿ�
	 * ���磺���ݰ����Ĳ�����װ���ض��Ĵ�����򣬺ͳ�ʼ��һЩ�������ϴ󣬵�ֻ��һ�����е�����
	 * 	
	 */
	public void compileNode(AbstractRunEnvironment env);
	
	/**
	 * 
	 * @return
	 */
	public int getLevel();
	
	/**
	 * �ڵ�������ʱ��Ҫ��ɵĶ���
	 * 
	 * @param env �������廷��
	 * @param context ��ǰ���е������ģ������������е�һЩ�������ܻᱻ�޸�
	 * @return ����ڵ����в�����ֵ���򷵻�null
	 */
	public Object runGetValue(AbstractRunEnvironment env,RunContext context)
		throws Exception;
}
