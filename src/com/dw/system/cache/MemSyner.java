package com.dw.system.cache;

/**
 * <p>Title: </p>
 * <p> 初始化过程：
 *    1,在新的Cacher类创建过程中，陷入阻塞状态。假设新Cacher名为A
 *    2,同步线程寻找一个已经正常运行的其他Cacher(名为B)，B进入冰冻状态（所有更改Cache操作都被阻塞）。
 *         但此时，两个Cacher都可以接受其他机器上的同名cache的变化，并保存在临时缓冲中。
 *    3，处于冰冻状态的B建立一个线程向A发送自己现有的内容。A把B传输过来的数据放入自己的内容中。
 *    4，传输结束后，A和B都进入自我更新——此时它把临时缓冲的内容取出并更新自己的内容，
 *         直到缓冲变空为止（此时缓冲还会接受新数据）。
 *    5，当A和B发现自己的缓冲区变空时，AB进入正常的运行状态。（其中B中被阻塞的修改缓冲区操作继续运行）: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MemSyner
{
	public static short STATE_FREEZEN = 1 ;
		public MemSyner()
	{
	}

}