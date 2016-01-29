package com.dw.system.dict;

import java.util.*;

import com.dw.system.dict.ioimpl.DBDDNode;
import com.dw.system.gdb.GDB;

public abstract class DataClassIO
{
	protected DataClass ConstructDataClass(String modulen,
            int cid,String namecn,String nameen,
            String aspconst,String ver,
            Date ct,Date lut)
        {
            DataClass dc = new DataClass(modulen) ;
            dc.classId = cid ;
            dc.classNameCn = namecn ;
            dc.classNameEn = nameen ;
            //dc.aspConst = aspconst ;
            dc.version = ver ;
            dc.createTime = ct ;
            dc.lastUpdateTime = lut ;
            
            return dc ;
        }

        protected DataNode ConstructDataNode(
            int id,int pid,String nameCn,String nameEn,
            int orderNo,boolean bvis,boolean bforbidden,
            Date ct,Date lut)
        {
            DataNode dn = new DataNode() ;

            dn.id = id ;
            dn.parentNodeId = pid ;
            dn.nameCn = nameCn ;
            dn.nameEn = nameEn ;
            dn.orderNo = orderNo ;
            dn.bVisiable = bvis ;
            dn.bForbidden = bforbidden ;
            dn.createTime = ct ;
            dn.lastUpdateTime = lut ;

            return dn ;
        }

        
        /// <summary>
        /// 装载所有的DataClass实体
        /// </summary>
        /// <returns></returns>
        public abstract int[] getAllClassIds(String modulen) throws Exception ;

        /// <summary>
        /// 根据类id装载DataClass实体
        /// </summary>
        /// <param name="classid"></param>
        /// <returns></returns>
        public abstract DataClass loadDataClass(String modulen,int classid) throws Exception ;

        /// <summary>
        /// 根据字典类id装载，所有的数据节点实体
        /// </summary>
        /// <param name="classid"></param>
        /// <returns></returns>
        public abstract List<DataNode> loadAllDataNodes(String modulen,int classid) throws Exception ;
        
        
        public abstract void addDataNode(String modulen,int classid,DataNode dn) throws Exception ;
        
        public abstract void updateDataNode(String modulen,long autoid,DataNode dn) throws Exception;
    	
    	public abstract void delDataNode(long autoid) throws Exception;
    	
    	public abstract void setDefaultDataNode(String modulen,int classid,int dnid)
			throws Exception;
}
