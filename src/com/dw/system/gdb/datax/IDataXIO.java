package com.dw.system.gdb.datax;

import java.io.File;

/**
 * ��¼DataXBase�������Ϣ��io����һ����ʹ�õĵط���ʵ��
 * 
 * @author Jason Zhu
 */
public interface IDataXIO
{
	public int getNewId() throws Exception;

	public void saveDataXDB(byte[] cont) throws Exception;

	public byte[] loadDataXDB() throws Exception;

	public void saveBaseById(int id, byte[] cont) throws Exception;

	public byte[] loadBaseById(int id) throws Exception;

	public int[] loadBaseAllIds() throws Exception;

	public void saveClassById(int baseid, int id, byte[] cont) throws Exception;

	public byte[] loadClassById(int baseid, int id) throws Exception;

	public int[] loadClassAllIds(int baseid) throws Exception;

	public void saveIndexById(int baseid, int classid, int id, byte[] cont)
			throws Exception;

	public byte[] loadIndexById(int baseid, int classid, int id)
			throws Exception;

	public int[] loadIndexAllIds(int baseid, int classid) throws Exception;

	public void saveFormById(int baseid, int classid, int id, byte[] cont)
			throws Exception;

	public byte[] loadFormById(int baseid, int classid, int id)
			throws Exception;

	public int[] loadFormAllIds(int baseid, int classid) throws Exception;

	/**
	 * ����ļ��洢��id
	 * 
	 * @param baseid
	 * @param classid
	 * @return
	 * @throws Exception
	 */
	public long getFileNewId(int baseid, int classid) throws Exception;

	/**
	 * �洢�ļ�
	 * 
	 * @param baseid
	 * @param classid
	 * @param fid
	 * @param cont
	 * @return �����Ҫ������ʷ�ļ���Ϣ,����ԭ�����ļ��Ѿ�����,�򷵻����ɵ���ʷ�ļ���Ϣ
	 * @throws Exception
	 */
	public String saveFile(int baseid, int classid, long fid, byte[] cont,
			boolean history) throws Exception;

	/**
	 * 
	 * @param baseid
	 * @param classid
	 * @param fid
	 * @return
	 * @throws Exception
	 */
	public byte[] loadFile(int baseid, int classid, long fid) throws Exception;

	/**
	 * �õ��ļ�����.
	 * 
	 * @param baseid
	 * @param classid
	 * @param fid
	 * @return
	 */
	public File getFile(int baseid, int classid, long fid);

	public void delFile(int baseid, int classid, long fid);

	public void delHisFile(int baseid, int classid, long fid, String hisid);

	public byte[] loadHisFile(int baseid, int classid, long fid, String hisid)
			throws Exception;

	public File getHisFile(int baseid, int classid, long fid, String hisid);
}
