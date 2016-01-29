package com.dw.system.dyn_auth;


import java.util.Hashtable;
import java.util.List;

import com.dw.system.gdb.*;

public class MgrNodeRightRoleManager
{
	private static Object locker = new Object() ; 
	private static MgrNodeRightRoleManager ccMgr = null ;
	
	public static MgrNodeRightRoleManager getInstance()
	{
		if(ccMgr!=null)
			return ccMgr ;
		
		synchronized(locker)
		{
			if(ccMgr!=null)
				return ccMgr ;
			
			ccMgr = new MgrNodeRightRoleManager() ;
			return ccMgr ;
		}
	}
	
	public MgrNodeRightRoleItem getMgrNodeRightRoleByMgrNodeId(String mgrNodeId) throws GdbException, Exception
	{
		MgrNodeRightRoleItem mi=null;
		if(mgrNodeId==null)
			return null;
		Hashtable ht=new Hashtable();
		ht.put("@MgrNodeId",mgrNodeId);
		List<MgrNodeRightRoleItem> mis=(List<MgrNodeRightRoleItem>)GDB.getInstance().accessDBPageAsXORMObjList("MgrNodeRightRole.getItemByMgrNodeId",ht,MgrNodeRightRoleItem.class,0,-1);
		if(mis.size()>0)
		{
			mi=mis.get(0);
		}
		return mi;
	}
}
