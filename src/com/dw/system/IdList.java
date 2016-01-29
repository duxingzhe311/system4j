package com.dw.system ;

import java.util.* ;

/**
 * 本类为解决Web Application中常见的查询数据列表的同步缓冲问题而设计。<br>
 * 经常会遇到这样的问题：<br>
 * 给定一组数据对象(通常来自于某一个数据库表)，需要完成对它的查询任务，并将数据列表显示。
 * 通常的，这一查询过程可能耗时较长，而这种查询的发生频度很高，
 * 不可避免的会增大对数据库、应用服务器的压力，这种情况下，而对上述数据的更新频度小且分散。
 * 应用最基本的原则，应当对数据进行缓冲，并保持缓冲中数据与实际数据间的同步更新。
 * 这就是本类要解决的同步缓冲问题。<br>
 *
 * 本类的解决方法基于下列事实或约定：<br>
 * 1. 对于每一个数据对象，都具有一个<b>唯一的标识</b>。
 * 这个标识可能是固有的(如客户信息中的身份证号码)或人为附加的(如新闻id)。<br>
 * 2. 虽然唯一标识有多种类型，但通常约定使用<b>数字</b>或字母编号。
 * 在公司内部，现在广泛使用数字编号(long)。<br>
 * 3. 对于一个查询结果，一般表现为一个数据对象队列，包含下述信息：<br>
 * 1) 数据的约束条件。作为判断数据是否符合队列要求的条件。对应与SQL中的Where子句。<br>
 * 2) 数据的排序条件。它决定数据间的顺序关系。对应与SQL中的Order by子句。<br>
 * 基于上述三点，使用了一个类和两个接口来解决这一问题。<br>
 * IdList保存查询结果(数据队列)，使用id作为数据索引。使用ListFilter接口描述数据的约束条件。
 * 使用Comparator作为数据的排序条件。当发生数据更新时，需要调用IdList的相应方法。<br>
 *
 * @see ListFilter
 * @see java.util.Comparator
 */
public class IdList
{
	private String name ;
	private long [] idList ;
	private Object [] sortedList ;

	private ListFilter filter ;
	private ListComparator listComparator ;
	private Comparator orderComparator ;

	/**
	 * 创建一个IdList，包括起初始数据列表，列表过滤器和排序比较器。
	 * @param sortedList 已排序的id列表。
	 * @param listComparator 该列表依据的排序条件。
	 * @param filter 包含该列表的过滤条件。
	 */
	public IdList (long [] sortedList , ListFilter filter , ListComparator listComparator)
	{
		this.filter = filter ;
		this.listComparator = listComparator ;
		this.orderComparator = new InnerComparator (listComparator) ;

		if (sortedList == null || sortedList.length == 0)
		{
			idList = new long [0] ;
			this.sortedList = new Object [0] ;
			return ;
		}

		this.sortedList = new Object [sortedList.length] ;
		for (int i = 0 ; i < sortedList.length ; i ++)
		{
			this.sortedList [i] = new long [] {sortedList [i]} ;
		}

		this.idList = sortedList ;
		Arrays.sort (idList) ;
	}

	/**
	 * 获得列表名称
	 */
	public String getName ()
	{
		return name ;
	}

	/**
	 * 获取列表长度。
	 */
	public int getSize ()
	{
		return sortedList.length ;
	}

	/**
	 * 获得id列表。
	 * @param index 列表中的起始位置。如果index < 0，将从0开始。
	 * @param count 列表长度。如果count <= 0 将返回全部内容。
	 * @return id的数组。
	 */
	public long [] getList (int index , int count)
	{
		Object [] tmpSortedList = sortedList ;
	//	System.out.println ("tmpSortedList = " + tmpSortedList) ;
		if (index >= tmpSortedList.length)
			return new long [0] ;

		if (index < 0)
			index = 0 ;

		if (count <= 0)
			return new long [0] ;

		if (index + count > tmpSortedList.length)
		{
			count = tmpSortedList.length - index ;
		}

		long [] list = new long [count] ;
		for (int i = 0 ; i < count ; i ++)
		{
			long [] la = (long []) tmpSortedList [i + index] ;
			if (la == null)
			{
				System.out.println ("list [" + (i + index) + "] = null") ;
				continue ;
			}
			list [i] = la [0] ;
		}
		return list ;
	}

	/**
	 * 从列表中删除某个id。
	 * @param id 预备删除的id。
	 * @return 返回值<0表示该列表中没有该ID。其他表示删除成功。
	 */
	public synchronized int removeId (long id)
	{
		int index = Arrays.binarySearch (idList , id) ;
		if (index < 0)
			return -1 ;

		if (idList.length <= 1)
		{
			idList = new long [0] ;
			sortedList = new Object [0] ;
			return index ;
		}
		else
		{
			long [] tmpIdList = new long [idList.length - 1] ;

			if (index > 0)
				System.arraycopy (idList , 0 , tmpIdList , 0 , index) ;

			if (index < idList.length - 1)
				System.arraycopy (idList , index + 1 , tmpIdList ,
					index , idList.length - index - 1) ;
			idList = tmpIdList ;
		}

		Object [] tmpSortedList = new Object [sortedList.length - 1]  ;

		for (int i = 0 ; i < tmpSortedList.length ; i ++)
		{
			if (((long []) sortedList [i]) [0] == id)
			{
				System.arraycopy (sortedList , i + 1, tmpSortedList , i , sortedList.length - i - 1) ;
				break ;
			}
			else
				tmpSortedList [i] = sortedList [i] ;
		}

		sortedList = tmpSortedList ;
		return index ;
	}

/*
	private static void out (long [] list)
	{
		System.out.print ("Inner") ;
		for (int i = 0 ; i < list.length ; i ++)
		{
			System.out.print ("," + list [i]) ;
		}
		System.out.println () ;
	}
*/
	/**
	 * 将id增加到列表中。<br>
	 * 增加前将使用ListFilter检查该ID是否属于该列表。
	 * @see ListFilter#accept
	 */
	public synchronized void addId (long id)
	{
		if (! filter.accept (id))
			return ;

		insertId (id) ;
	}

	/**
	 * 将ID插入列表中，该方法用于其子类在覆盖addId等方法时使用。
	 */
	protected void insertId (long id)
	{
		// insert into id List.
		int index = Arrays.binarySearch (idList , id) ;
		if (index >= 0 && index < idList.length)
		{
			// already in list??
			return ;
		}


		index = -index -1 ;


		long [] tmpIdList = new long [idList.length + 1] ;

		if (index > 0)
			System.arraycopy (idList , 0 , tmpIdList , 0 , index) ;

		tmpIdList [index] = id ;
		if (index < sortedList.length - 1)
			System.arraycopy (idList , index , tmpIdList ,
				index + 1 , idList.length - index) ;

		idList = tmpIdList ;


		// insert into SortedList
		index = Arrays.binarySearch (sortedList , new long [] {id} , orderComparator) ;

		if (index >= 0 && index < sortedList.length)
		{
			// already in list??
			return ;
		}

		// insert obj
		index = -index -1 ;


		Object [] tmpSortedList = new Object [sortedList.length + 1] ;

		if (index > 0)
			System.arraycopy (sortedList , 0 , tmpSortedList , 0 , index) ;

		tmpSortedList [index] = new long [] {id} ;

		if (index < sortedList.length)
			System.arraycopy (sortedList , index , tmpSortedList ,
				index + 1 , sortedList.length - index) ;

		sortedList = tmpSortedList ;
	}

	/**
	 * 列表是否包含该ID。
	 */
	public boolean containsId (long id)
	{
		return Arrays.binarySearch (idList , id) >= 0 ;
	}

	/**
	 * 当某个ID所表征的数据发生变化时，调用该方法以调整列表保持同步。<br>
	 * 主要考虑了4种情况：<pre>
	 * 1. 更改前数据在列表中，更改后的数据不在列表中，须删除。
	 * 2. 更改前数据在列表中，更改后的数据仍在列表中，重新排序。
	 * 3. 更改前数据不在列表中，更改后的数据不在列表中，无动作。
	 * 4. 更改前数据不在列表中，更改后的数据在列表中，加入列表。
	 * </pre>
	 */
	public synchronized void updateId (long id)
	{
		int res = 0 ;
		if (filter.accept (id))
			res = 1 ;
		if (containsId (id))
			res += 2 ;
		// System.out.println ("updateId (" + id + "): " + res) ;
		switch (res)
		{
		case 1 :
			insertId (id) ;
			break ;
		case 2 :
			removeId (id) ;
			break ;
		case 3 :
			/*
			Object [] clonedList = (Object []) sortedList.clone () ;
			Arrays.sort (clonedList , orderComparator) ;
			sortedList = clonedList ;
			*/
			// remove id is a Full length search.
			// typically size of list is about 10M. will be complete in 10ms.
			removeId (id) ;
			// insert will use binary insert Alog.
			insertId (id) ;
			break ;
		case 0 :
		default :
			break ;
		}


	}

	private class InnerComparator implements Comparator
	{
		ListComparator comp ;
		public InnerComparator (ListComparator comp)
		{
			this.comp = comp ;
		}
		public int compare (Object obj1 , Object obj2)
		{
			return comp.compare (
				((long []) obj1) [0] , ((long []) obj2) [0]) ;
		}
	} ;
}
