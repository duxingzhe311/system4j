package com.dw.system.gdb.datax;

import java.io.File;

/**
 * 记录DataXBase定义的信息的io，它一般在使用的地方被实现
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
	 * 获得文件存储新id
	 * 
	 * @param baseid
	 * @param classid
	 * @return
	 * @throws Exception
	 */
	public long getFileNewId(int baseid, int classid) throws Exception;

	/**
	 * 存储文件
	 * 
	 * @param baseid
	 * @param classid
	 * @param fid
	 * @param cont
	 * @return 如果需要保存历史文件信息,并且原来的文件已经存在,则返回生成的历史文件信息
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
	 * 得到文件对象.
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
