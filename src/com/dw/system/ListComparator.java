package com.dw.system;

/**
 * ���ӿ���������{@link IdList}ʹ���б����ȽϽӿڡ�<br>
 * ��IdList�е����ݷ����仯ʱ��ʹ�øýӿڶ��б���������<br>
 * һ��ģ����������Ƕ���һ���򼸸����Ե���ϱȽϡ��������SQL��䣺<br>
 * <b>select * from test_table order by Name , Brith_DAY</b><br>
 * ��ˣ���ô��ʵ�������ص�ListComparator����ʹ�����д��룺<pre><b>
 * 	TestTable t1 = TastTable.getObject (id1) ;
 * 	TestTable t2 = TastTable.getObject (id2) ;
 * 	int ret = 0 ;
 *	ret = t1.getName ().compareTo (t2.getName ()) ;
 * 	if (ret != 0)
 * 		return ret ;
 * 	ret = t1.getBrithDay ().compareTo (t2.getBirthDay ()) ;
 * 	if (ret != 0)
 * 		return ret ;
 * 	return id1 - id2 ;
 * </b></pre>
 *
 * @see IdList
 * @see ListFilter
 */
public interface ListComparator // extends Comparator
{

	/**
	 * �Ƚ�����ID����Ķ���Ĵ�С�����������㷨��<br>
	 * ��Ҫע����ǣ���������������������������бȽ����ʱ��Ӧ��ʹ��id���бȽϣ�
	 * ��ΪArrays�������㷨�������������ʱ������쳣�����磺<pre><b>
	 * 	Object obj1 = ... ;
	 * 	Object obj2 = ... ;
	 * 	if (obj1.equals (obj2))
	 *		return id1 - id2 ;
	 * </b></pre>
	 * @param id1 ���ڱȽϵ�id
	 * @param id2 ���ڱȽϵ�id
	 * @return ��java.util.Comparator�ķ���һ�¡�
	 */
	public int compare (long id1 , long id2) ;
}
