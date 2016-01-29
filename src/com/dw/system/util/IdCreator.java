package com.dw.system.util;

import java.util.UUID;

/**
 * 使用0-9 a-z的36进制标识的UUID
 * 使得产生的唯一id尽可能短-以方便与用来做数据库的主键
 * f81d4fae-7dec-11d0-a765-00a0c91e6bf6
 * @author Jason Zhu
 *
 */
public class IdCreator
{
	/**
	 * 从一个输入的uuid计算获得
	 * @param uuid
	 * @return
	 */
	public static String transFromUUID(String uuid)
	{
		StringBuilder sb = new StringBuilder() ;
		String[] ss = uuid.split("-") ;
		for(String s:ss)
		{
			long l = Long.parseLong(s, 16);
			sb.append(long2id36(l)) ;
		}
		
		return sb.toString() ;
	}
	
	//public static 
	/**
	 * long值转换成36进制的表示
	 * @param l
	 * @return
	 */
	private static String long2id36(long l)
	{
		return long2id36(l,0);
	}
	
	private static String long2id36(long l,int minlen)
	{
		String ret = "" ;
		long tmpl;
		do
		{
			tmpl = l % 36 ;
			if(tmpl<10)
				ret = ((char)('0'+tmpl))+ret ;
			else
				ret = ((char)('A'+tmpl-10))+ret ;
			l /= 36 ;
		}while(l!=0);
		
		int bl = minlen-ret.length() ;
		for(int i = 0 ; i < bl ; i ++)
			ret = '0'+ret ;
		return ret ;
	}
	
	static long ID36_V[] = new long[10] ;
	
	static
	{
		ID36_V[0] = 1 ;
		for(int k = 1 ; k < 10 ; k ++)
			ID36_V[k] = ID36_V[k-1]*36 ;
	}
	
	private static long id36_to_long(String strid36)
	{
		long rl = 0 ;
		int s = strid36.length() ;
		for(int i = 0 ; i < s ; i ++)
		{
			char c = strid36.charAt(s-i-1) ;
			int k;
			if(c>='0'&&c<='9')
				k = c - '0' ;
			else if(c>='A'&&c<='Z')
				k = c - 'A'+10 ;
			else
				throw new IllegalArgumentException("invalid id 36="+strid36) ;
			
			rl += ID36_V[i]*k ;
		}
		
		return rl ;
	}
	/**
	 * 从一个36进制表示的id转换成对应的UUID
	 * @param id36
	 * @return
	 */
	public static String transToUUID(String id36)
	{
		throw new RuntimeException("not impl") ;
	}
	
	static Object locker = new Object() ;
	static long LAST_CT = -1 ;
	static long SEQ_COUNT = 1 ;
	static long MAX_SEQ_COUNT = 36*36*36*36 ;
	/**
	 * 获得一个新id
	 * @return
	 */
	public static String newSeqId()
	{
		//return transFromUUID(UUID.randomUUID().toString()) ;
		long ct = System.currentTimeMillis() ;
		synchronized(locker)
		{
			if(ct==LAST_CT)
			{
				SEQ_COUNT++ ;
				if(SEQ_COUNT==MAX_SEQ_COUNT)
					SEQ_COUNT = 1 ;
			}
			else
			{
				LAST_CT = ct ;
				SEQ_COUNT = 1;
			}
		}
		
		StringBuilder sb = new StringBuilder() ;
		sb.append(long2id36(ct,10))
			.append(long2id36(SEQ_COUNT,4)) ;
		String ss = UUID.randomUUID().toString() ;
		//取最后的node值
		long l = Long.parseLong(ss.substring(25), 16);
		String tmps = long2id36(l,10) ;
		if(tmps.length()>10)
			tmps = tmps.substring(0,10) ;
		sb.append(tmps) ;
		return sb.toString() ;
	}
	
	/**
	 * 从顺序号里面提取时间的毫秒数
	 * 该方法用来支持通过seqid定位一些相关的时间
	 * @param seqid
	 * @return
	 */
	public static long extractTimeInMillInSeqId(String seqid)
	{
		if(seqid==null)
			return -1 ;
		
		if(seqid.length()<=10)
			return -1 ;
		
		//read 10 char
		String c10 = seqid.substring(0,10) ;
		
		return id36_to_long(c10) ;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println("cur time in mills="+System.currentTimeMillis());
		int v = Float.floatToIntBits(3.1415926f) ;
		System.out.println("int v="+v) ;
		
		float f = Float.intBitsToFloat(v) ;
		System.out.println("float v="+f) ;
		
		long ct0 = System.currentTimeMillis() ;
		String id0 = long2id36(ct0);
		System.out.println("long ct="+ct0+" longid="+id0+" id2long="+id36_to_long(id0));
		System.out.println("long max"+Long.MAX_VALUE+"="+long2id36(Long.MAX_VALUE)) ;
		System.out.println("ffffffffffff="+long2id36(Long.parseLong("ffffffffffff",16))) ;
		
		String s = transFromUUID("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
		System.out.println("sor uuid=f81d4fae-7dec-11d0-a765-00a0c91e6bf6") ;
		System.out.println("id36="+s) ;
		//System.out.println("uuid="+transToUUID(s)) ;
		
		for(int i = 0 ; i < 60 ; i ++)
			System.out.println("newid="+newSeqId()) ;
		
		for(int i = 0 ; i < 10 ; i ++)
		{
			String uid = UUID.randomUUID().toString();
			System.out.println(uid+"="+transFromUUID(uid)) ;
		}
		
	}
}
