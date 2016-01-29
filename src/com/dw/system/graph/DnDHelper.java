package com.dw.system.graph;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JOptionPane;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

public class DnDHelper
{
	public static Transferable createTransfer(IXmlDataable xda)
	{
		XmlData tmpxd = new XmlData();
		tmpxd.setParamValue("classname", xda.getClass().getCanonicalName());
		tmpxd.setSubDataSingle("xml_data", xda.toXmlData());
		return new StringSelection(tmpxd.toXmlString());
	}
	
	public static IXmlDataable xmlDataToObj(XmlData xd) throws Exception
	{
		String cn = xd.getParamValueStr("classname");
		if(cn==null||cn.equals(""))
			return null ;
		
		Class c = Class.forName(cn);
		IXmlDataable ret = (IXmlDataable)c.newInstance() ;
		
		XmlData tmpxd = xd.getSubDataSingle("xml_data");
		if(tmpxd!=null)
			ret.fromXmlData(tmpxd);
		
		return ret ;
	}
	
	public static abstract class DropTargetHelper
	{
		DropTarget dropTarget = null;
		Component comp = null ;
		
		public DropTargetHelper(Component comp)
		{
			this.comp = comp ;
			dropTarget = new DropTarget(comp, DnDConstants.ACTION_REFERENCE,
					dropTL);
		}

		DropTargetListener dropTL = new DropTargetListener()
		{

			public void dragEnter(DropTargetDragEvent arg0)
			{

			}

			public void dragOver(DropTargetDragEvent arg0)
			{

			}

			public void dropActionChanged(DropTargetDragEvent arg0)
			{

			}

			public void dragExit(DropTargetEvent arg0)
			{

			}

			public void drop(DropTargetDropEvent e)
			{
				Point p = e.getLocation();
				try
				{
					DataFlavor stringFlavor = DataFlavor.stringFlavor;
					Transferable tr = e.getTransferable();

					if (e.isDataFlavorSupported(stringFlavor))
					{
						String xxx = (String) tr.getTransferData(stringFlavor);

						e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
//						JOptionPane.showMessageDialog(comp, "x="
//								+ p.getX() + " y=" + p.y + " o=" + xxx);
						XmlData xd = XmlData.parseFromXmlStr(xxx);
						//xd.getParamValuesStr(xxx)
						IXmlDataable xdobj = xmlDataToObj(xd) ;
						e.dropComplete(true);
						
						onDrop(e.getLocation(),xdobj);
					}
					else
					{
						e.rejectDrop();
					}
				}
				catch (Exception ioe)
				{
					ioe.printStackTrace();
				}
			}
		};
		
		public abstract void onDrop(Point location,IXmlDataable xdobj);
	}
}
