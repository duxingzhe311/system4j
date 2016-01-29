package com.dw.system.cache ;

import java.io.* ;
import java.util.* ;
/**
 * Cacher的缓冲区
 * 该缓冲区使用Hashtable&链表结合使得即可以快速的获得内容
 * 又可以控制缓冲区的长度,同时实现LRU算法对旧内容进行排除
 */

public class CacherBuffer
{
	 int iMaxBufferLen = Integer.MAX_VALUE ;
	 /**
	  * 用来指向Shell双向链表的首尾指针，它本身也是一个Shell
	  * 它的prev指向尾部，next指向链表头
	  */
	 Shell shHead = new Shell () ;
	 Hashtable htBuffer = new Hashtable () ;

	 public CacherBuffer ()
	 {
		  //create a empty list
		  shHead.setPrev (shHead) ;
		  shHead.setNext (shHead) ;
	 }

	 public CacherBuffer (int len)
	 {
		  this () ;
		  if (len<=0)
			   throw new RuntimeException ("Error:CacherBuffer cannot be set to <=0!") ;
		  iMaxBufferLen = len ;
	 }

	 /**
	  * 把一个新的shell加到链表的头中
	  */
	 private void addToListHead (Shell newsh)
	 {
		  if (newsh.getPrev()!=null||newsh.getNext()!=null)
			   throw new RuntimeException ("Shell cannot be added because of existing in buffer!") ;
		  newsh.setPrev (shHead) ;
		  newsh.setNext (shHead.getNext()) ;
		  shHead.getNext().setPrev (newsh) ;
		  shHead.setNext (newsh) ;
	 }


	 private Shell removeListTail ()
	 {
		  Shell tmpsh = getListTail () ;
		  if (tmpsh!=null)
			   removeFromList (tmpsh) ;
		  return tmpsh ;
	 }
	 /**
	  *
	  */
	 private void removeFromList (Shell sh)
	 {
		  if (sh.getPrev()==null||sh.getNext()==null)
			   throw new RuntimeException ("Shell cannot remove from list because of not existing in buffer!") ;
		  if (sh==shHead)
			   throw new RuntimeException ("Cannot remove head!") ;
		  sh.getPrev().setNext (sh.getNext()) ;
		  sh.getNext().setPrev (sh.getPrev()) ;
		  sh.setPrev (null) ;
		  sh.setNext (null) ;
	 }
	 /**
	  * 把已存在的shell一到头部，实现LRU排序
	  */
	 private void transferToListHead (Shell sh)
	 {
		  removeFromList (sh) ;
		  addToListHead (sh) ;
	 }

	 private Shell getListHead ()
	 {
		  Shell tmpsh = shHead.getNext () ;
		  if (tmpsh==shHead)
			   return null ;
		  else
			   return tmpsh ;
	 }

	 private Shell getListTail ()
	 {
		  Shell tmpsh = shHead.getPrev () ;
		  if (tmpsh==shHead)
			   return null ;
		  else
			   return tmpsh ;
	 }



	 synchronized public void addShell (Shell sh)
	 {
		  if (htBuffer.size()<iMaxBufferLen)
		  {
			   addToListHead (sh) ;
		  }
		  else
		  {//full and remove one shell USING LRU
			   Shell tmpsh = removeTailShell () ;
			   if (tmpsh==null)
					throw new RuntimeException ("Some Error:no shell in list while buffer is full??") ;
			   addToListHead (sh) ;
		  }

		  htBuffer.put (sh.getKey(),sh) ;
	 }
	 /**
	  * 获取某个shell，并把它放到链表的前面
	  */
	 synchronized public Shell accessShell (Object key)
	 {
		  Shell tmpsh = (Shell)htBuffer.get (key) ;
		  if (tmpsh==null)
			   return null ;
		  transferToListHead (tmpsh) ;
		  return tmpsh ;
	 }
	 /**
	  * 获取某个shell
	  */
	 public Shell getShell (Object key)
	 {
		  return (Shell)htBuffer.get (key) ;
	 }
	 synchronized public Shell removeShell (Object key)
	 {
		  Shell tmpsh = (Shell)htBuffer.get (key) ;
		  if (tmpsh==null)
			   return null ;
		  removeFromList (tmpsh) ;
		  htBuffer.remove (key) ;
		  return tmpsh ;
	 }

	 synchronized public void removeShell (Shell sh)
	 {
		  removeFromList (sh) ;
		  htBuffer.remove (sh.getKey()) ;
	 }

	 synchronized public Shell removeTailShell ()
	 {
		  Shell tmpsh = removeListTail () ;
		  if (tmpsh==null)
			   return null ;
		  htBuffer.remove (tmpsh.getKey()) ;
		  return tmpsh ;
	 }

	 synchronized public void emptyBuffer ()
	 {
		  shHead.setPrev (shHead) ;
		  shHead.setNext (shHead) ;
		  htBuffer.clear () ;
	 }

	 public boolean isEmpty()
	 {
		  return htBuffer.isEmpty() ;
	 }

	 public int size()
	 {
		  return htBuffer.size() ;
	 }

	 public int getMaxBufferLen ()
	 {
		  return iMaxBufferLen ;
	 }

	 synchronized public void setMaxBufferLen (int len)
	 {
		  if (len<=0)
			   throw new RuntimeException ("Error:CacherBuffer cannot be set to <=0!") ;
		  int curlen = getBufferLen () ;
		  for (int i = len ; i < curlen ; i ++)
			   removeTailShell () ;
		  iMaxBufferLen = len ;
	 }

	 public int getBufferLen ()
	 {
		  return htBuffer.size () ;
	 }

	 synchronized public Shell[] getAllShell ()
	 {
		  int s = htBuffer.size () ;
		  Shell[] rets = new Shell [s] ;
		  Shell tmpsh = shHead ;
		  for (int i = 0 ; i < s ; i ++)
		  {
			   rets[i] = tmpsh.getNext () ;
			   tmpsh = rets[i] ;
		  }
		  return rets ;
	 }


	 synchronized public Enumeration getAllKeys()
	 {
		  return htBuffer.keys() ;
	 }


	 synchronized public void list ()
	 {
		  System.out.println ("-----In list----------------") ;
		Shell[] shs = getAllShell () ;
		for (int i = 0 ; i < shs.length ; i ++)
			System.out.println (shs[i].toString()) ;
		System.out.println ("---------------------------") ;
		System.out.println ("-----In hash----------------") ;
		for (Enumeration en = htBuffer.keys () ; en.hasMoreElements () ;)
		{
			 String tmpkey = (String)en.nextElement () ;
			 System.out.println (tmpkey+"="+htBuffer.get(tmpkey)) ;
		}
		System.out.println ("---------------------------") ;
	 }
}