package com.dw.system.gdb.syn_client;

import java.util.*;
import com.dw.system.xmldata.*;

/**
 * XORM������࣬���ʵ�ִ˽ӿڣ��������Ӧ�ı�֧�ַ�һ���ն�ЭͬӦ��
 * 
 * GDB�ڽ�������У����Զ��Ľ���һ����ص�log��������¼��Ӧ��ı仯
 * 
 * ʵ�ָýӿڣ���Ϊ�����ն��ṩ��һ��Эͬ��
 * @author Jason Zhu
 */
public interface ISynClientable
{
	/**
	 * ���ͬ���ն�����id��������
	 * ֮�����ж��������Ϊһ�����ݿ���п���Ҫͬʱ֧�ֶ��ֲ���������ն�
	 * ���磺ָ����ձ�����Ҫ��sender��recver
	 * 	sender�ļ�¼��ӣ�Ҫ֧��recver��log
	 *  ��recver�Ļظ�����Ҫͬʱ֧��sender��log
	 * ����ն�id��Ӧ�����ж������ÿ�μ�¼�ĸ��£��ͻ���ݴ˼�¼�еĶ�Ӧclientid��ֵ���Զ�
	 * 	��¼���log
	 * @return
	 */
	public String[] getSynClientClientIdColNames() ;
	
	/**
	 * ����ն˷�һ��ͬ���ı�����
	 * @param client_col �����ն������ʣ���ö�Ӧ���ն˱�����
	 * @return
	 */
	public String getSynClientTableName(String client_col) ;
	
	/**
	 * �������ݿ�����ƻ�Ķ�Ӧ���ն�������
	 * @param tablen
	 * @return
	 */
	public String getSynClientColByTableName(String tablen);
	
	/**
	 * ����������ն�id�����ƣ���ñ������Ӧ���ն�idֵ
	 * ��õ�ǰ��������Ӧ��¼���ն�id����ֵ
	 * @return
	 */
	public String getSynClientIdByColName(String client_col) ;
	
	/**
	 * ��õ�ǰ��������Ӧ��¼������id
	 * @return
	 */
	public String getSynClientPkId() ;
	
	/**
	 * ����ն˷�һ��ͬ���Ľṹ
	 * �˽ṹ���������ն˴洢�ı�ṹ��Ϣ���󲿷�����£��˽ṹ������������ݽṹ��ͬ
	 * �磺���͵�ָ�ʵ�ִ˽ӿڵ��ಢ������ָ������ݣ����н���id�����ն����������
	 * 	�����ݣ�������������ݣ�����ͨ��ʵ��{@see getSynClientData}���
	 * @param client_col ��Ӧ���ն��У�������ͬ���У�����Ϊ��ͬ���͵��ն��ṩ��ͬ��
	 * 	��ṹ
	 * @return
	 */
	public XmlDataStruct getSynClientStruct(String client_col) ;
	
	/**
	 * �����ն�����id����ñ���Ķ�Ӧ�ն˼�¼����
	 */
	public int getSynClientTableRecordNum(String client_col,String clientid)
		throws Exception;
	
//	/**
//	 * ����������ն��к��ն�idֵ���ӱ仯��־�У�����˳��
//	 * @param clientid
//	 * @return
//	 */
//	public List<String> readSynClientIdInChgLog(String client_col,String clientid) ;
	
	/**
	 * ����������ն�����id����ö�Ӧ���ն�ͬ�������б�
	 * �÷�������log��Ӧ��id���ʹ��
	 * @param client_pkids
	 * @return �������null�������ʾʧ�ܡ����򣬷��������б�����б���
	 * 	������ĳһ��client_pk��Ӧ��ֵ����ʾû��������¼���ն�Ӧ��ɾ����Ӧ��pkid��¼
	 * 	������ͬ��
	 */
	public List<XmlData> getSynClientData(String client_col,String clientid,List<String> client_pkids) throws Exception ;
	
	
	/**
	 * �����������һ������id����˳���ô��ڴ�����id�ģ�������¼
	 * ͬʱ����¼�����ܲ����������
	 * 
	 * @param client_col �ն���
	 * @param clientid
	 * @param last_pkid
	 * @param max_num
	 * @return
	 */
	public List<XmlData> getSynClientData(String client_col,String clientid,String last_pkid,int max_num) throws Exception ;
	
	/**
	 * ���ն����Ӽ�¼ʱ������������������ջ���ô˷���
	 * ˼·1�����ն�����id���Ա�����ַ�����ӳɹ����ն˲�֪��������Ҫ�ظ����� - X
	 * ˼·2���ɷ���������id����֪ͨ�նˣ�������ַ������ɹ����ն�ʧ�ܣ�����ͨ��������
	 * 		ͬ�������ն��Զ���ӡ���˼·������ͨ���������ṩ��web���淢����Ϣ�������ܹ�
	 * 		�Զ�ͬ������Ӧ���նˡ�--OKOK
	 * @param client_pkid �ն����ɵ�Ψһid,֮����
	 * @param xd 
	 * @return ����ɹ����򷵻ض�Ӧ��XmlData��¼���ն�Ӧ�ø��ݴ˼�¼��ӱ��ؼ�¼
	 * 	���������п���ͨ����һ�¼��㣬����һЩ���ݸ��նˡ�
	 */
	public XmlData onSynClientAdd(String client_col,String clientid,XmlData xd)
	 throws Exception;
	
	/**
	 * ���ն��޸ļ�¼ʱ������������͸�������
	 * @param client_pkid
	 * @param xd
	 * @return
	 */
	public XmlData onSynClientUpdate(String client_col,String clientid,String client_pkid,String[] update_cols,XmlData xd)
	 throws Exception;
	
	/**
	 * ���ն�ɾ����¼ʱ�������������ɾ������
	 * @param clientid
	 * @param client_pkid
	 */
	public void onSynClientDelete(String client_col,String clientid,String[] client_pkid)
	 throws Exception;
}
