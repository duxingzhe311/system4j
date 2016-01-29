package com.dw.user;

import java.util.List;

import com.dw.system.gdb.xorm.XORMProperty;

/**
 * �ڲ�ͬ����Ŀ��,���п������ĳһ����֯�����ڵ��µ������ӽڵ�����һЩ��չ��Ϣ
 * ��Щ��չ��Ϣ������Ŀ����Ҫ������ͬ.
 * 
 * Ϊ�����������չ,�ش˶�������֯�����ڵ���չ�ļܹ�
 * 
 * Ϊ����㿪��--Ŀ���Ǽ�������д����,�������޶�
 * 1,�û���չ��Ϣ������һ������,���ҷ���XORM�ж���ı��ʽ--Ҳ���Ǽ̳б���
 * 	  ������붨��XORM��Ϣ,��ע��--����֮��Ͳ���Ҫ���κο�����
 * 2,
 * 
 * �ڲ�ͬ�Ĺ����У����ܻ���Բ�ͬ���ӽڵ�����趨
 * @author Jason Zhu
 */
public abstract class OrgNodeExtItem
{
	@XORMProperty(name="AutoId",has_col=true,is_pk=true,is_auto=true)
	protected String autoId = null ;
	
	//@XORMProperty(name="OrgNodeId",has_col=true,has_idx=true,max_len=15,is_unique_idx=true,has_fk=true,fk_table="security_org",fk_column="OrgNodeId",order_num=10)
	@XORMProperty(name="OrgNodeId",has_col=true,has_idx=true,max_len=15,is_unique_idx=true,order_num=10)
	protected String orgNodeId = null ;
	
	
	/**
	 * ���û�����ģ���ж�Ӧ�ĸ��ڵ�·������
	 * @return
	 */
	public abstract String[] getParentOrgNodePathNameArray() ;
	
	public abstract String[] getParentOrgNodePathTitleArray() ;
	
	public abstract String[] getParentOrgNodePathDescArray() ;
	
	public abstract List<String> getExtNames() ;
	
	public abstract Object getExtValue(String extname) ;
	
	/**
	 * ��Org���������ʱ����
	 */
	public abstract void onAddedByOrgMgr() ;
	
	/**
	 * ��Org�������޸�ʱ����
	 *
	 */
	public abstract void onUpdatedByOrgMgr() ;
	
	public String getAutoId()
	{
		return autoId ;
	}
	
	void setOrgNodeId(String un)
	{
		orgNodeId = un ;
	}
	
	public String getOrgNodeId()
	{
		return orgNodeId ;
	}
	
	public OrgNode getOrgNode() throws Exception
	{
		return OrgManager.getDefaultIns().GetOrgNodeById(orgNodeId);
	}
}
