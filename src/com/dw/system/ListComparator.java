package com.dw.system;

/**
 * 本接口用于描述{@link IdList}使用列表对象比较接口。<br>
 * 当IdList中的数据发生变化时，使用该接口对列表重新排序。<br>
 * 一般的，排序条件是对象一个或几个属性的组合比较。如下面的SQL语句：<br>
 * <b>select * from test_table order by Name , Brith_DAY</b><br>
 * 因此，那么，实现与此相关的ListComparator可以使用下列代码：<pre><b>
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
	 * 比较两个ID代表的对象的大小，用于排序算法。<br>
	 * 需要注意的是，当两个对象的依据排序条件进行比较相等时，应当使用id进行比较，
	 * 因为Arrays的排序算法当两个对象相等时会出现异常。例如：<pre><b>
	 * 	Object obj1 = ... ;
	 * 	Object obj2 = ... ;
	 * 	if (obj1.equals (obj2))
	 *		return id1 - id2 ;
	 * </b></pre>
	 * @param id1 用于比较的id
	 * @param id2 用于比较的id
	 * @return 与java.util.Comparator的返回一致。
	 */
	public int compare (long id1 , long id2) ;
}
