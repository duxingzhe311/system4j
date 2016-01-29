package com.dw.system ;

/**
 * �����������������б�����ݵ�Լ��������<br>
 * ListFilter��һ��ʵ�ַ�����<br>
 * �������Լ�������أ��Ϻõİ취ʹ����ȶ���<br>
 * ������Ϊ��һ����ѯ���Ŀ�����Ѱ����ĳ����֪���������ƵĶ��󼯺ϡ�
 * ������������һ���������ڶ���������ϡ�
 * ���磺���ҹ�ҵ���������ͼ�飬���Թ���һ������������press="��ҵ������"��
 * ���������Ϊ��ȶ��󣬾Ϳ��Ա���Լ�������ˡ�<br>
 * ʹ����ȶ���ķ������Խ���ѯ�������󻯣�
 * ���������Է�װ��ListFilter�У������Ժ����׵��滻��<br>
 * ����Web Application���ԣ�������ʹ��UseBean�ķ�����Jsp���롣��������<pre><b>
 * &lt;jsp:useBean class="Book" id = "book" scope="request"/>
 * &lt;jsp:setProperty id="book" property = "*"/>
 * </b></pre>
 * ��Ȼ����ʹ�ã���ô������һ�����⣺
 * ͼ�黹����ʹ��������������ҵ�����⣬�������ڵȽ����ۺϲ�ѯ��
 * ����ж���������ȶ���������ʱ��Ϊ��ѯ�����ģ�<br>
 * ����֪����Java��ÿһ�����ݶ�����ȱʡֵ����������Ҳ�������ɶ�����ȱʡֵ��
 * ͨ������£����Ƕ����ȱʡֵ������Чֵ�����ڣ�
 * ���ǿ��Խ����Ե�ȱʡֵ����Ϊ�Ƿ���Ϊ��Ч��ѯ��������Чֵ�����磺<pre><b>
 * class Book
 * {
 * 	private String press = null ;
 * 	private double price = -1.0 ;
 * }
 * </b></pre>
 * ��������£��Ϳ��Ժ����׵ķֱ����Ч���ԡ�<br>
 * ��ѯʱ�������������������磺�۸���20-100Ԫ֮�䣬����������9��-10��֮��ȵȡ�
 * ��ʱ�����ǿ���ʹ�ö���������п��ƣ����磺<pre><b>
 * &lt;jsp:useBean class="Book" id = "book" scope="request"/>
 * &lt;jsp:setProperty id="book" property = "*"/>
 *
 * &lt;jsp:useBean class="Book" id = "book2" scope="request"/>
 * &lt;jsp:setProperty id="book2" property = "price" param = "price2"/>
 * &lt;jsp:setProperty id="book2" property = "publishDate" param = "publishDate2"/>
 * &lt;% Book.searchBooks (new Book [] {book , book2}) ; %>
 * </b></pre>
 * ͨ����ѯ���ǻ������ݿ�ģ���ˣ�������Ҫʹ�ö�̬��SQL��䡣���Բο�com.css.sql.ParamSql��<br>
 * �������������͵Ĳ�������װ��̬SQLʱӦ��ʹ��PrepareStatement�ķ������������ݿ���졣<br>
 * ������һ��װ��SQL�����ӣ�<pre><b>
 * 	Book searchObject = ... ;
 *
 * 	String sql = "select book_id from book_table where" ;
 * 	if (searchObject.getPressName () != null)
 *		sql += " Press_Name = '" + searchObject.getName () + "'" ;
 * </b></pre>
 * ����������SQLʱʹ��ParamSql��
 *
 * @see IdList
 */
public interface ListFilter
{
	public boolean accept (long id) ;
}
