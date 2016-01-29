package com.dw.system.gdb.syn_client;

import java.util.*;

import com.dw.system.xmldata.*;

/**
 * ֧���ն˼򵥱�ͬ����ʵ�ֽӿ�
 * 
 * �ն����еĴ�ű�ṹ����ͬ pkid , up_dt, xd_cont
 * 
 * ��������ͨ��up_dt�����ն˽���ͬ������
 * 
 * @author Jason Zhu
 *
 */
public interface ISimpleSynTable
{
	/**
	 * ������ݱ�����
	 * @return
	 */
	public String SST_getTableName() ;
	
	/**
	 * ������һ�θ���ʱ�䣬���֮��ĸ���id��ʱ��
	 * <b>
	 * �÷���ʵ��ʱ����������ʱ��仯�ļ�¼��������״̬�������й��ˣ���ԭ�����п����ն˶�Ӧ��
	 * ��¼��Ҫ��ɾ���������SST_readItemsDetail���������ݼ���
	 * </b>
	 * @param clientid
	 * @param last_dt 
	 * @return
	 */
	public List<SynItem> SST_readIdAndDTAfter(String clientid,long last_dt)
		throws Exception;
	
	/**
	 * ���ն˳�ʼ��ʱ��ֱ�Ӹ����ն�id��ô��ն�id������Ч��id�͸���ʱ��
	 * <b>�÷���ʵ��ʱ��Ҫ���ǲ���Ҫ������ն˵�״̬�����й���</b>
	 * @param clientid
	 * @return
	 * @throws Exception
	 */
	public List<SynItem> SST_readIdAndDTValidAll(String clientid)
		throws Exception;
	/**
	 * ��������id��ȡ,��ص�ͬ��������
	 * Ҫ���ն˹���ÿ����¼�Ĵ�С������ÿ�λ�ȡ������
	 * 
	 * ���ĳһ��pkid��Ӧ��SynItem�����ڣ����ն���ɾ��
	 * @param pkid
	 * @return
	 */
	public List<SynItem> SST_readItemsDetail(String clientid,String[] pkid)
		throws Exception;
	
	/**
	 * ֧���ն������ķ���
	 * @param add_t
	 * @param xd
	 * @return
	 */
	public SynItem SST_onClientAdd(String clientid,String add_t,XmlData xd)
		throws Exception;
	
	/**
	 * ֧���ն˸��µķ���
	 * @param update_t ��������
	 * @param pkid
	 * @param xd
	 * @return ���ص�SynItem,��Ҫ���ݾ��������дupdateRes��Ա
	 * 	�������Ʒ��������ն˵�Ӧ��
	 */
	public SynItem SST_onClientUpdate(String clientid,String update_t,String pkid,XmlData xd)
		throws Exception;
	
	/**
	 * ֧���ն�ɾ���ķ���
	 * @param pkid
	 */
	public boolean SST_onClientDelete(String clientid,String pkid)
		throws Exception;
}
