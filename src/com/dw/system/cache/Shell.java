package com.dw.system.cache ;

/**
 * Cacher的外壳
 */

public class Shell
{
	 /**
	  * 时间段保存，以月为单位
	  */
	 //public static final short BASE_MONTH = 1 ;
	 /**
	  * 时间段保存，以周为单位
	  */
	 //public static final short BASE_WEEK = 2 ;
	 /**
	  * 时间段保存，以天为单位
	  */
	 //public static final short BASE_DAY = 3 ;
	 /**
	  *
	  */
	 Object key = null ;
	 /**
	  * 可以存在内存中的时间段
	  */
	 long lLiveTime = -1 ;
	 /**
	  * 过期时间点
	  */
	 long lExpireTime = -1 ;
	 /**
	  * 是否可被更新——当被访问后，对过期时间点进行更新
	  */
	 boolean bRefresh = true ;
	 /**
	  * 被cache的内容
	  */
	 Object object = null ;



	 public Shell ()
	 {}

	 /**
	  * 永久保存一个对象
	  */
	 public Shell (Object key,Object ob)
	 {
		  this.key = key ;
		  object = ob ;
	 }
	 /**
	  * 保存一个有时间期限的对象，refresh=true
	  *@param ob 被保存对象
	  *@param livetime 被保存时间
	  */
	 public Shell (Object key,Object ob,long livetime)
	 {
		this (key,ob) ;
		this.lLiveTime = livetime ;
		if (livetime>0)
			 lExpireTime = System.currentTimeMillis () + livetime ;
	 }
	 /**
	  * 保存一个有时间期限的对象
	  *@param ob 被保存对象
	  *@param livetime 被保存时间
	  *@param refresh 如果不希望过期时间被更新=false
	  */
	 public Shell (Object key,Object ob,long livetime,boolean refresh)
	 {
		  this (key,ob,livetime) ;
		  bRefresh = refresh ;
	 }

	 public Object clone ()
	 {
		  return new Shell (this.key,this.object,this.lLiveTime,this.bRefresh) ;
	 }
	 /*
	 public Shell (Object ob, short base,long start,long end)
	 {
		object = ob ;
		if (lifetime>0)
			timeout = System.currentTimeMillis () + lifetime ;
		else
			timeout = -1 ; //cache it forever
	 }
	 */
	 public Object getKey ()
	 {
		  return key ;
	 }
	 /**
	  * 过期时间是否可被更新
	  */
	 public boolean isRefresh ()
	 {
		  return bRefresh ;
	 }

	 public void setRefresh (boolean brefresh)
	 {
		  bRefresh = brefresh ;
	 }
	 /**
	  * 判断是否过期
	  */
	 public boolean isTimeOut ()
	 {
		  if (lExpireTime<0)
			   return false ;
		  else if (lExpireTime>System.currentTimeMillis())
			   return false ;
		  else
			   return true ;
	 }

	 public long getLiveTime ()
	 {
		  return lLiveTime ;
	 }
	 /**
	  * 查看内容，不对过期时间做任何事
	  */
	 public Object peekContent ()
	 {
		  return object ;
	 }
	 /**
	  * 得到被cache的内容，如果要refresh，则对过期时间进行更新
	  */
	 public Object getContent ()
	 {
		if (lExpireTime>0&&bRefresh)
		{
			 lExpireTime = System.currentTimeMillis () + lLiveTime ;
		}

		return object ;
	 }
	 /**
	  * 直接设置过期时间
	  */
	 public void setExpireTime (long exptime)
	 {
		  this.lExpireTime = exptime ;
	 }

	 public void setLiveTime (long livetime)
	 {
		  this.lLiveTime = livetime ;

		if (livetime>0)
			 lExpireTime = System.currentTimeMillis () + livetime ;
		else
			   lExpireTime = -1 ;
	 }
	 /**
	  * 设置被缓冲内容
	  */
	 public void setContent (Object ob)
	 {
		  object = ob ;
	 }

	 public void clear ()
	 {
		  lLiveTime = -1 ;
		  lExpireTime = -1 ;
		  bRefresh = true ;
		  object = null ;
	 }

	 public String toString ()
	 {
		  return "("+key+"=" +object +"," + lLiveTime +","+lExpireTime +","+bRefresh+ ")" ;
	 }

	 Shell prev = null ;
	 Shell next = null ;

	 public Shell getPrev ()
	 {
		  return prev ;
	 }
	 public void setPrev (Shell sh)
	 {
		  prev = sh ;
	 }

	 public Shell getNext ()
	 {
		  return next ;
	 }
	 public void setNext (Shell sh)
	 {
		  next = sh ;
	 }
}