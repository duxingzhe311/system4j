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
	 * 对节点进行优化等操作，Dom继承节点如果需要对解析的内容进行优化，则应该实现/继承该接口
	 * 比如：根据包含的操作符装配特定的处理程序，和初始化一些计算量较大，但只需一次运行的内容
	 * 	
	 */
	public void compileNode(AbstractRunEnvironment env);
	
	/**
	 * 
	 * @return
	 */
	public int getLevel();
	
	/**
	 * 节点在运行时需要完成的动作
	 * 
	 * @param env 运行整体环境
	 * @param context 当前运行的上下文，其中上下文中的一些变量可能会被修改
	 * @return 如果节点运行不返回值，则返回null
	 */
	public Object runGetValue(AbstractRunEnvironment env,RunContext context)
		throws Exception;
}
