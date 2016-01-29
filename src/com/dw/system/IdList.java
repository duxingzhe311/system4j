package com.dw.system ;

import java.util.* ;

/**
 * ����Ϊ���Web Application�г����Ĳ�ѯ�����б��ͬ�������������ơ�<br>
 * �������������������⣺<br>
 * ����һ�����ݶ���(ͨ��������ĳһ�����ݿ��)����Ҫ��ɶ����Ĳ�ѯ���񣬲��������б���ʾ��
 * ͨ���ģ���һ��ѯ���̿��ܺ�ʱ�ϳ��������ֲ�ѯ�ķ���Ƶ�Ⱥܸߣ�
 * ���ɱ���Ļ���������ݿ⡢Ӧ�÷�������ѹ������������£������������ݵĸ���Ƶ��С�ҷ�ɢ��
 * Ӧ���������ԭ��Ӧ�������ݽ��л��壬�����ֻ�����������ʵ�����ݼ��ͬ�����¡�
 * ����Ǳ���Ҫ�����ͬ���������⡣<br>
 *
 * ����Ľ����������������ʵ��Լ����<br>
 * 1. ����ÿһ�����ݶ��󣬶�����һ��<b>Ψһ�ı�ʶ</b>��
 * �����ʶ�����ǹ��е�(��ͻ���Ϣ�е����֤����)����Ϊ���ӵ�(������id)��<br>
 * 2. ��ȻΨһ��ʶ�ж������ͣ���ͨ��Լ��ʹ��<b>����</b>����ĸ��š�
 * �ڹ�˾�ڲ������ڹ㷺ʹ�����ֱ��(long)��<br>
 * 3. ����һ����ѯ�����һ�����Ϊһ�����ݶ�����У�����������Ϣ��<br>
 * 1) ���ݵ�Լ����������Ϊ�ж������Ƿ���϶���Ҫ�����������Ӧ��SQL�е�Where�Ӿ䡣<br>
 * 2) ���ݵ��������������������ݼ��˳���ϵ����Ӧ��SQL�е�Order by�Ӿ䡣<br>
 * �����������㣬ʹ����һ����������ӿ��������һ���⡣<br>
 * IdList�����ѯ���(���ݶ���)��ʹ��id��Ϊ����������ʹ��ListFilter�ӿ��������ݵ�Լ��������
 * ʹ��Comparator��Ϊ���ݵ��������������������ݸ���ʱ����Ҫ����IdList����Ӧ������<br>
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
	 * ����һ��IdList���������ʼ�����б��б������������Ƚ�����
	 * @param sortedList �������id�б�
	 * @param listComparator ���б����ݵ�����������
	 * @param filter �������б�Ĺ���������
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
	 * ����б�����
	 */
	public String getName ()
	{
		return name ;
	}

	/**
	 * ��ȡ�б��ȡ�
	 */
	public int getSize ()
	{
		return sortedList.length ;
	}

	/**
	 * ���id�б�
	 * @param index �б��е���ʼλ�á����index < 0������0��ʼ��
	 * @param count �б��ȡ����count <= 0 ������ȫ�����ݡ�
	 * @return id�����顣
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
	 * ���б���ɾ��ĳ��id��
	 * @param id Ԥ��ɾ����id��
	 * @return ����ֵ<0��ʾ���б���û�и�ID��������ʾɾ���ɹ���
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
	 * ��id���ӵ��б��С�<br>
	 * ����ǰ��ʹ��ListFilter����ID�Ƿ����ڸ��б�
	 * @see ListFilter#accept
	 */
	public synchronized void addId (long id)
	{
		if (! filter.accept (id))
			return ;

		insertId (id) ;
	}

	/**
	 * ��ID�����б��У��÷��������������ڸ���addId�ȷ���ʱʹ�á�
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
	 * �б��Ƿ������ID��
	 */
	public boolean containsId (long id)
	{
		return Arrays.binarySearch (idList , id) >= 0 ;
	}

	/**
	 * ��ĳ��ID�����������ݷ����仯ʱ�����ø÷����Ե����б���ͬ����<br>
	 * ��Ҫ������4�������<pre>
	 * 1. ����ǰ�������б��У����ĺ�����ݲ����б��У���ɾ����
	 * 2. ����ǰ�������б��У����ĺ�����������б��У���������
	 * 3. ����ǰ���ݲ����б��У����ĺ�����ݲ����б��У��޶�����
	 * 4. ����ǰ���ݲ����б��У����ĺ���������б��У������б�
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
