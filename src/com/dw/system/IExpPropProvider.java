package com.dw.system;

/**
 * �������ʵ�ָýӿ�,������������ֱ��֧������ xxx.propname����
 * 
 * ���һЩ�ڱ��ʽ�����еĶ���,��ͨ����������ʵ����֧�� ���Բ����� ������
 * Ӧ��ʵ�ָýӿ�
 * 
 * @author Jason Zhu
 */
public interface IExpPropProvider
{
//	/**
//	 * �����������getPropValue������ʹ�ø������ƻ�õĶ�Ӧ���Լ�����֧��
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
//		 * ���д��ɹ����򷵻��Լ�������null
//		 * @param v
//		 * @return
//		 */
//		public abstract Object setNewValue(Object v) ;
//	}
	
	public Object getPropValue(String propname) ;
	
	public void setPropValue(String propname,Object v) ;
	
//	/**
//	 * �ڱ��ʽ�У�ͨ�� xxx.v1���ֵû������
//	 * �������� ʵ�� xxx.v1=1 �ĸ�ֵ����£�ԭ��getPropValue���������ǻ������ֵ
//	 *  �޷�ͨ�����ʽ����������ֵ����
//	 *  
//	 *  ����һ��ͨ���˷�����ö�Ӧ���Ե�����ֵ��������֧��xxx.v1=1�Ĳ���
//	 * @return
//	 */
//	public ValueWrapper getPropValueWrapper(String propname) ;
}
