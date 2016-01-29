package com.dw.system ;

/**
 * 该类用于描述数据列表对数据的约束条件。<br>
 * ListFilter的一般实现方法：<br>
 * 如何描述约束条件呢？较好的办法使用类比对象。<br>
 * 可以认为，一个查询活动的目标就是寻找与某个已知对象相类似的对象集合。
 * 而这种相似性一般是体现在对象的属性上。
 * 例如：查找工业出版社出版图书，可以构造一个对象其属性press="工业出版社"。
 * 以这个对象为类比对象，就可以表述约束条件了。<br>
 * 使用类比对象的方法可以将查询条件对象化，
 * 而其规则可以封装在ListFilter中，并可以很容易的替换。<br>
 * 对于Web Application而言，还可以使用UseBean的方法简化Jsp代码。如下例：<pre><b>
 * &lt;jsp:useBean class="Book" id = "book" scope="request"/>
 * &lt;jsp:setProperty id="book" property = "*"/>
 * </b></pre>
 * 既然这样使用，那么就有另一个问题：
 * 图书还可以使用其他属性如行业，主题，出版日期等进行综合查询，
 * 如何判定给定的类比对象中属性时作为查询条件的？<br>
 * 我们知道，Java中每一个数据都有其缺省值，对象属性也可以自由定义其缺省值。
 * 通常情况下，我们定义的缺省值都是有效值，现在，
 * 我们可以将属性的缺省值定义为是否作为有效查询条件的无效值。例如：<pre><b>
 * class Book
 * {
 * 	private String press = null ;
 * 	private double price = -1.0 ;
 * }
 * </b></pre>
 * 这种情况下，就可以很容易的分辨出有效属性。<br>
 * 查询时还经常会有区间条件如：价格在20-100元之间，出版日期在9月-10月之间等等。
 * 这时，我们可以使用对象数组进行控制，例如：<pre><b>
 * &lt;jsp:useBean class="Book" id = "book" scope="request"/>
 * &lt;jsp:setProperty id="book" property = "*"/>
 *
 * &lt;jsp:useBean class="Book" id = "book2" scope="request"/>
 * &lt;jsp:setProperty id="book2" property = "price" param = "price2"/>
 * &lt;jsp:setProperty id="book2" property = "publishDate" param = "publishDate2"/>
 * &lt;% Book.searchBooks (new Book [] {book , book2}) ; %>
 * </b></pre>
 * 通常查询都是基于数据库的，因此，我们需要使用动态的SQL语句。可以参看com.css.sql.ParamSql。<br>
 * 而对于日期类型的参数在组装动态SQL时应当使用PrepareStatement的方法以屏蔽数据库差异。<br>
 * 下面是一个装配SQL的例子：<pre><b>
 * 	Book searchObject = ... ;
 *
 * 	String sql = "select book_id from book_table where" ;
 * 	if (searchObject.getPressName () != null)
 *		sql += " Press_Name = '" + searchObject.getName () + "'" ;
 * </b></pre>
 * 建议在生成SQL时使用ParamSql。
 *
 * @see IdList
 */
public interface ListFilter
{
	public boolean accept (long id) ;
}
