package com.dw.system.gdb.conf;

import java.util.HashMap;

import com.dw.system.gdb.conf.buildin.*;

/**
 * ���ڲ�ͬ�����ݿ⣬�ڶ������ݿ���ʱ���ܻ�ʹ��ȱʡֵ������ȱʡֵ�Ĳ�������
 * �ڲ������ṩ����sqlserver�������ʱ�����͵��е�ȱʡֵ��������getdata()����
 * ���������ݿ�ʹ�����������������ܶ����ݿⲻ֧���������ݿ��е�ȱʡֵ�ɶ�̬����
 * �ṩ��
 * 
 * �ڴˣ�gdbͨ���ṩ�Լ����ڲ�������֧�������������ʹ�����ݿ����ʱ����Ҫʹ�����ݿ�
 * �ض��Ķ�����Ӱ������ֲ�ԡ�
 * 
 * ȱ�㣺����ڷֲ�ʽ�����ʹ���������Ҫ��һЩȱʡֵ�Ĳ����ڲ�ͬ��Ӧ�÷����������ͬ��
 * Ӧ�÷�������ʱ��ֵ���ܻ��в��졣��һЩ��ʱ������е�ϵͳ�в��ʺϡ��������������Ӧ��
 * ͳһʹ�����ݿ��е�ʱ��ֵ��
 * 
 * @author Jason Zhu
 */
public abstract class BuildInValGenerator
{
	private static HashMap<String,BuildInValGenerator> n2bi = new HashMap<String,BuildInValGenerator>();
	/**
	 * �������ƻ���ڲ�ֵ������
	 * @param vg_name
	 * @return
	 */
	public static BuildInValGenerator getBuildInVG(String vg_name)
	{
		return n2bi.get(vg_name);
	}
	
	private static void putVG(BuildInValGenerator vg)
	{
		n2bi.put(vg.getName(), vg);
	}
	
	static
	{
		putVG(new VGCurTimestamp());
		putVG(new VGRandomUUID());
	}
	
	
	public abstract String getName();
	
	public abstract Object getVal(String[] parms);
	
	public abstract String getDesc();
}
