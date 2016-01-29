package com.dw.system.gdb.syn_client;

import com.dw.system.xmldata.*;

/**
 * Í¬²½Ïî
 * @author Jason Zhu
 *
 */
public class SynItem implements IXmlDataable
{
	public static enum UpdateRes
	{
		update,
		delete,
		do_nothing
	}
	
	String pkId = null ;
	
	long upDT = -1 ;
	
	XmlData xdCont = null ;
	
	
	transient UpdateRes updateRes = null;
	
	public SynItem()
	{}
	
	public SynItem(String pkid,long updt)
	{
		this.pkId = pkid ;
		this.upDT = updt ;
	}
	
	public SynItem(String pkid, long updt, XmlData xdcont)
    {
        this.pkId = pkid;
        this.upDT = updt;
        this.xdCont = xdcont;
    }

    public String getPkId()
    {
        return pkId;
    }

    public long getUpDT()
    {
        return upDT;
    }

    public XmlData getXDCont()
    {
        return xdCont;
    }
    
    public void setUpdateRes(UpdateRes ur)
    {
    	updateRes = ur ;
    }
    
    public UpdateRes getUpdateRes()
    {
    	return updateRes ;
    }


	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		xd.setParamValue("pkid", pkId) ;
		xd.setParamValue("updt", upDT) ;
		if(xdCont!=null)
			xd.setSubDataSingle("xd_cont", xdCont) ;
		return xd;
	}


	public void fromXmlData(XmlData xd)
	{
		pkId = xd.getParamValueStr("pkid");
		upDT = xd.getParamValueInt64("updt", -1) ;
		xdCont = xd.getSubDataSingle("xd_cont") ;
	}
}
