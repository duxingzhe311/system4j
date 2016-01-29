package com.dw.system.gdb.datax.query;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * ���������಻�����κ����С�������������������������Ļ�����Ϣ�� ���������ƣ����룬��������ȡ�
 * 
 * ���ںܶ��������������Ͷ�Ӧ��DataXClass�й�ϵ����������Ϣ�����ڷ������н�����
 * ���ܹ����ջ�á�������ͻ�����֪��һ���������������Ϣ���ͱ���ͨ�������ˡ�
 * 
 * �ͻ��˰�ָ���ύ���������󣬿���Ҫ�����������һ��Dummy��������������ͻ��˾� ���Ը��ݸ�Dummy���������һЩ�����ˣ�
 * �磺����ͻ��˿���Ϊͨ���������һ��BizAction����ȡDummy��Ϣ��Ȼ�����Ϳ���
 * ȷ��������BizAction���������������ʽ��Ҳ�Ϳ�����BizFunc�ж���ʹ���ˡ�
 * 
 * @author Jason Zhu
 */
@XRmi(reg_name = "datax_cmd_dummy")
public class DXQCmdDummy implements IDXQCmd, IXmlDataable
{
	String cmdName = null;

	String cmdStr = null;

	XmlDataStruct inputXDS = new XmlDataStruct();

	XmlDataStruct outputXDS = new XmlDataStruct();
	
	/**
	 * �չ��캯������Ҫ����֧��IXmlDataable�Զ�����
	 */
	public DXQCmdDummy()
	{}
	
	/**
	 * �������������������Ӧ��Dummy����
	 * @param othercmd
	 */
	public DXQCmdDummy(IDXQCmd othercmd)
	{
		this(othercmd.getName(),othercmd.getCmdStr(),
				othercmd.getInputStruct(),
				othercmd.getOutputStruct());
	}

	/**
	 * ���ݾ������Ϣ����Dummy����
	 * @param cmdname
	 * @param cmdstr
	 * @param inputxds
	 * @param outputxds
	 */
	public DXQCmdDummy(String cmdname, String cmdstr, XmlDataStruct inputxds,
			XmlDataStruct outputxds)
	{
		cmdName = cmdname;
		cmdStr = cmdstr;

		if (inputxds != null)
			inputXDS = inputxds;

		if (outputxds != null)
			outputXDS = outputxds;
	}

	public String getName()
	{
		return cmdName;
	}

	public String getCmdStr()
	{
		return cmdStr;
	}

	public XmlDataStruct getInputStruct()
	{
		return inputXDS;
	}

	public XmlDataStruct getOutputStruct()
	{
		return outputXDS;
	}

	public XmlData doCmd(XmlData inputxd) throws Exception
	{
		throw new RuntimeException("Dummy Cmd cannot run!");
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("cmd_name", cmdName);
		xd.setParamValue("cmd_str", cmdStr);

		xd.setSubDataSingle("inputxd_struct", inputXDS.toXmlData());
		xd.setSubDataSingle("outputxd_struct", outputXDS.toXmlData());
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		cmdName = xd.getParamValueStr("cmd_name");
		cmdStr = xd.getParamValueStr("cmd_str");

		XmlData tmpxd = xd.getSubDataSingle("inputxd_struct");
		inputXDS.fromXmlData(tmpxd);
		tmpxd = xd.getSubDataSingle("outputxd_struct");
		outputXDS.fromXmlData(tmpxd);
	}

}
