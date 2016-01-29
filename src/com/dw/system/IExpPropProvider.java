package com.dw.system;

/**
 * 如果对象实现该接口,则对象可以用来直接支持形如 xxx.propname操作
 * 
 * 如果一些在表达式中运行的对象,想通过自身特殊实现来支持 属性操作符 的运算
 * 应该实现该接口
 * 
 * @author Jason Zhu
 */
public interface IExpPropProvider
{
//	/**
//	 * 改类用来替代getPropValue方法，使得根据名称获得的对应属性即可以支持
//	 * @author Jason Zhu
//	 *
//	 */
//	public static abstract class ValueWrapper
//	{
//		Object orgVal = null ;
//		
//		protected ValueWrapper(Object orgalval)
//		{
//			orgVal = orgalval;
//		}
//		
//		
//		public Object getValue()
//		{
//			return orgVal ;
//		}
//		/**
//		 * 如果写入成功，则返回自己，否则null
//		 * @param v
//		 * @return
//		 */
//		public abstract Object setNewValue(Object v) ;
//	}
	
	public Object getPropValue(String propname) ;
	
	public void setPropValue(String propname,Object v) ;
	
//	/**
//	 * 在表达式中，通过 xxx.v1获得值没有问题
//	 * 但反过来 实现 xxx.v1=1 的赋值情况下，原先getPropValue方法仅仅是获得最终值
//	 *  无法通过表达式进行真正的值设置
//	 *  
//	 *  所以一般通过此方法获得对应属性的设置值对象，用来支持xxx.v1=1的操作
//	 * @return
//	 */
//	public ValueWrapper getPropValueWrapper(String propname) ;
}
