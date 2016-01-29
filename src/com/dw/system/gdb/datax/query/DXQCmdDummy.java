package com.dw.system.gdb.datax.query;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * 虚假命令，该类不会做任何运行。但它可以描述其他所有命令的基本信息。 如命令名称，输入，输出参数等。
 * 
 * 由于很多命令的输出参数和对应的DataXClass有关系，而这种信息必须在服务器中解析后
 * 才能够最终获得。但如果客户端想知道一条命令输入输出信息，就必须通过该类了。
 * 
 * 客户端把指令提交给服务器后，可以要求服务器返回一个Dummy命令对象。这样，客户端就 可以根据该Dummy命令对象，做一些工作了：
 * 如：管理客户端可以为通过命令建立的一种BizAction，获取Dummy信息，然后它就可以
 * 确定出这种BizAction的输入输出参数格式，也就可以在BizFunc中定义使用了。
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
	 * 空构造函数，主要用来支持IXmlDataable自动构造
	 */
	public DXQCmdDummy()
	{}
	
	/**
	 * 根据其他命令对象建立对应的Dummy对象
	 * @param othercmd
	 */
	public DXQCmdDummy(IDXQCmd othercmd)
	{
		this(othercmd.getName(),othercmd.getCmdStr(),
				othercmd.getInputStruct(),
				othercmd.getOutputStruct());
	}

	/**
	 * 根据具体的信息建立Dummy对象
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
