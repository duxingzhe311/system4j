package com.dw.system.dyn_auth;

import java.util.ArrayList;
import java.util.List;

public class ServerMgr2Node
{
	String id = null;
	
	/**
	 * ��Ӧ��ģ��
	 */
	String moduleName = null ;

	String desc = null;

	List<MgrNode> subMgrNodes = new ArrayList<MgrNode>();
	
	public String getModuleName()
	{
		return moduleName ;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public List<MgrNode> getSubMgrNodes()
	{
		return subMgrNodes;
	}

	public void setSubMgrNodes(List<MgrNode> subMgrNodes)
	{
		this.subMgrNodes = subMgrNodes;
	}

	/**
	 * �������Ҷ�ӽڵ�
	 * @return
	 */
	public List<MgrNode> getLeafNodes()
	{
		if(subMgrNodes==null)
			return null ;
		
		ArrayList<MgrNode> rets = new ArrayList<MgrNode>() ;
		for(MgrNode mn:subMgrNodes)
		{
			listLeafNodes(rets,mn) ;
		}
		
		return rets ;
	}

	/**
	 * �ݹ��оٳ��ض��ڵ��µ�����Ҷ�ӽڵ�
	 * @param buf
	 * @param curn
	 */
	private void listLeafNodes(List<MgrNode> buf,MgrNode curn)
	{
		if(curn.isLeaf())
		{
			buf.add(curn);
			return ;
		}
		
		for(MgrNode mn:curn.subMgrNodes)
		{
			listLeafNodes(buf,mn) ;
		}
	}
	
	/**
	 * �ڶ�Ӧ�������ļ������������ֲ�����ؽڵ�
	 * @param n
	 * @return
	 */
	public MgrNode findNodeByName(String n)
	{
		MgrNode mn;
		for(MgrNode mne:this.subMgrNodes)
		 {
			 mn=getMgrNodeByNameFromMgrNode(n,mne);
			 if(mn!=null&&n.equals(mn.getName()))
			 {
				 return mn ;
			 }
		 }
		
		return null ;
	}
	

	MgrNode getMgrNodeByNameFromMgrNode(String name, MgrNode mne)
	{
		MgrNode mn = null;
		if (name == null)
			return null;
		if (name.equals(mne.getName()))
			mn = mne;
		else
		{
			if (mne.getSubMgrNodes().size() > 0)
			{
				for (MgrNode m : mne.getSubMgrNodes())
				{
					mn = getMgrNodeByNameFromMgrNode(name, m);
					if (mn != null && name.equals(mn.getName()))
						break;
				}
			}
		}
		return mn;
	}
}
