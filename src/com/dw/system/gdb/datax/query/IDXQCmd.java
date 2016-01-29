package com.dw.system.gdb.datax.query;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;

public interface IDXQCmd
{
	public String getName();
	
	public String getCmdStr();
	
	public XmlDataStruct getInputStruct();
	
	public XmlDataStruct getOutputStruct();
	
	public XmlData doCmd(XmlData inputxd) throws Exception;
}
