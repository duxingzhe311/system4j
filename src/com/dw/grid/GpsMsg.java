package com.dw.grid;

import java.util.*;

import com.dw.system.Convert;
import com.dw.system.xmldata.*;

/**
 * �ƶ�Gps�ն��ռ�����Ϣ
 * 
 * @author Jason Zhu
 */
public class GpsMsg
{
	public static final int DEFAULT_PORT = 55333;
	
	/**
	 * ��Ϣ�����ͼƬ��Ϣ
	 * @author Jason Zhu
	 *
	 */
	public static class GpsPic implements IXmlDataable
	{
		String fileName = null ;
		Date captureDate = null ;
		GpsPos gpsPos = null ;
		
		transient String localFilePath = null ;
		
		public GpsPic()
		{}
		
		public GpsPic(String filename,Date capd,GpsPos gp,String localfp)
		{
			if(Convert.isNullOrEmpty(filename))
				throw new IllegalArgumentException("file name cannot be null") ;
			
			fileName = filename ;
			captureDate = capd ;
			gpsPos = gp ;
			
			localFilePath = localfp ;
		}
		
		public String getFileName()
		{
			return fileName ;
		}
		
		public Date getCaptureDate()
		{
			return captureDate ;
		}
		
		public GpsPos getGpsPos()
		{
			return gpsPos ;
		}
		
		public String getLocalFilePath()
		{
			return localFilePath ;
		}
		
		public XmlData toXmlData()
		{
			XmlData xd = new XmlData() ;
			xd.setParamValue("filename",fileName) ;
			if(captureDate!=null)
				xd.setParamValue("capture_date",captureDate) ;
			if(gpsPos!=null)
				xd.setSubDataSingle("gps_pos",gpsPos.toXmlData()) ;
			return xd;
		}
		public void fromXmlData(XmlData xd)
		{
			fileName = xd.getParamValueStr("filename") ;
			captureDate = xd.getParamValueDate("capture_date",null);
			XmlData tmpxd = xd.getSubDataSingle("gps_pos") ;
			if(tmpxd!=null)
			{
				gpsPos = new GpsPos() ;
				gpsPos.fromXmlData(tmpxd) ;
			}
		}
	}
	
	
	/**
	 * ��Ϣid
	 */
	String msgId = null ;
	
	/**
	 * ��Ϣ����
	 */
	String msgType = null ;
	/**
	 * ��Ϣ���ύ���͵�ʱ�����ڵ�gpsλ����Ϣ
	 */
	GpsPos msgPos = null ;
	
	/**
	 * ��Ϣ���ύ���͵�ʱ���ʱ��
	 */
	Date msgDate = null ;
	
	/**
	 * ��Ϣ����
	 */
	XmlData msgBody = null ;
	
	/**
	 * ��Ϣ������ͼƬ��Ϣ
	 */
	ArrayList<GpsPic> gpsPics = new ArrayList<GpsPic>() ;
	
	public GpsMsg()
	{}
	
	
	
	public GpsMsg(String msgid,String msgtype,GpsPos mgp,Date msgdate,XmlData body,List<GpsPic> gpsps)
	{
		this.msgId = msgid ;
		this.msgType = msgtype ;
		this.msgPos = mgp ;
		this.msgDate = msgdate ;
		msgBody = body ;
		
		this.gpsPics.addAll(gpsps) ;
	}
	
	public GpsMsg(String msgid,String msgtype,GpsPos mgp,Date msgdate,XmlData body,GpsPic[] gpsps)
	{
		this.msgId = msgid ;
		this.msgType = msgtype ;
		this.msgPos = mgp ;
		this.msgDate = msgdate ;
		
		msgBody = body ;
		if(gpsps!=null)
		{
			for(GpsPic gp:gpsps)
			{
				if(gp==null)
					continue ;
				this.gpsPics.add(gp) ;
			}
		}
		
	}
	
	public String getMsgId()
	{
		return msgId ;
	}
	
	public String getMsgType()
	{
		return msgType ;
	}
	
	public GpsPos getMsgPos()
	{
		return msgPos ;
	}
	
	public void setMsgPos(GpsPos gp)
	{
		msgPos = gp ;
	}
	
	public Date getMsgDate()
	{
		return msgDate ;
	}
	
	public ArrayList<GpsPic> getGpsPics()
	{
		return gpsPics ;
	}
	
	public XmlData getMsgBody()
	{
		return msgBody;
	}
	
	public XmlDataWithFile toXmlDataWithFile()
	{
		XmlData inxd = new XmlData() ;
		inxd.setParamValue("msg_id",msgId) ;
		inxd.setParamValue("msg_type",msgType) ;
		if(msgPos!=null)
			inxd.setSubDataSingle("msg_pos",msgPos.toXmlData()) ;
		if(msgDate!=null)
			inxd.setParamValue("msg_date",msgDate) ;
		
		if(msgBody!=null)
			inxd.setSubDataSingle("msg_body",msgBody) ;
		
		List<XmlData> xds = inxd.getOrCreateSubDataArray("gps_pics") ;
		ArrayList<String> localfps = new ArrayList<String>() ;
		
		for(GpsPic gp:gpsPics)
		{
			xds.add(gp.toXmlData()) ;
			localfps.add(gp.getLocalFilePath()) ;
		}
		return new XmlDataWithFile(inxd,localfps) ;
	}
	
	
	public void fromXmlDataCombinedFileHead(XmlDataWithFile.CombinedFileHead cfh)
	{
		XmlData xd = cfh.getInnerXmlData() ;
		if(xd==null)
			return;
		
		msgId = xd.getParamValueStr("msg_id") ;
		msgType = xd.getParamValueStr("msg_type") ;
		
		XmlData tmpxd = xd.getSubDataSingle("msg_pos") ;
		if(tmpxd!=null)
		{
			msgPos = new GpsPos() ;
			msgPos.fromXmlData(tmpxd) ;
		}
		
		msgDate = xd.getParamValueDate("msg_date",null) ;
		
		msgBody = xd.getSubDataSingle("msg_body") ;
		List<XmlData> pics = xd.getSubDataArray("gps_pics") ;
		if(pics!=null)
		{
			for(XmlData p:pics)
			{
				GpsPic gp = new GpsPic() ;
				gp.fromXmlData(p) ;
				gpsPics.add(gp);
			}
		}
	}
}
