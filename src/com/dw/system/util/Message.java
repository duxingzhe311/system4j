package com.dw.system.util ;

import java.util.* ;
import java.io.* ;
import java.lang.reflect.* ;

//import sun.io.ByteToCharConverter ;
import com.dw.system.util.lopt.* ;

/**
 * A common object packing utility. Using LOPT instead of reflect access. <br/>
 * This File is copy from WBMessage.java from package com.css.util,
 * for future used, the com.css.util.WBMessage will be <code>deprecated</code>. <br/>
 * <B>How to Use it?</B><br/>
 *
 * <pre>
 * 	msg.add (-10834) ; // int
 * 	msg.add (-10834L) ; // long
 * 	msg.add ((byte) 0x12) ; // byte
 * 	msg.add ((short) -2934) ; // short
 * 	msg.add (293455.45545f) ; // float
 * 	msg.add ("中文测试aaaa") ; // String
 *
 * 	msg.add (null) ; // null value
 * 	msg.add (true) ; // boolean
 * 	msg.add (new Date ()) ; // date
 *
 * 	msg.add (new byte [] {2 , 3}) ; // byte aray
 * 	msg.add (new float [] {1.0f , 293455.45545f}) ; // float array
 * 	msg.add (new double [] {343.4654D , -909043.6788D}) ; // double array
 * </pre>
 * @version 1.0.1
 */

public class Message
{
	/** indentify a null object */
	public static final short NULL = 0 ;
	/** Message Object */
	public static final short MSG = 1 ;

	/** Date type, will convert into long */
	public static final short DATETIME = 3 ;

	/** String object, encode by using special encoding */
	public static final short STRING = 8 ;
	/** a boolean value, true or flase */
	public static final short BOOLEAN = 9 ;
	// protected static final short IPDATA = 10 ;
	// protected static final short INT = 11 ;
	// protected static final short UINT = 12 ;
	/** short, signed 16bits */
	public static final short SHORT = 13 ;
	/** byte, unsigned 8bits */
	public static final short BYTE = 14 ;
	/** integer, signed 32bits */
	public static final short INTEGER = 15 ;
	/** long, signed 64bits */
	public static final short LONG = 16 ;

	/** unsigned char, 16bits */
	public static final short CHAR = 17 ;

	/** array of char */
	public static final short CHAR_ARRAY = 18 ;

	/** single-float-point, 32bits */
	public static final short FLOAT = 24 ;
	/** double-float-point, 64bits */
	public static final short DOUBLE = 25 ;
	/*
	public static final short IPPORT16 = 26 ;
	public static final short IPADDR32 = 27 ;
	public static final short ENCRYPTED = 32 ;
	*/
	/** array of byte */
	public static final short BYTE_ARRAY = 34 ;
	/** array of integer */
	public static final short INTEGER_ARRAY = 35 ;
	/** array of double */
	public static final short DOUBLE_ARRAY = 36 ;
	/** array of float */
	public static final short FLOAT_ARRAY = 37 ;
	/** array of long */
	public static final short LONG_ARRAY = 38 ;
	/** array of short */
	public static final short SHORT_ARRAY = 39 ;
	/** array of string */
	public static final short STRING_ARRAY = 41 ;
	/** array of boolean, currently supported */
	public static final short BOOLEAN_ARRAY = 40 ;
	/*
	public static final short U64ARRAY = 41 ;
	public static final short F32ARRAY = 44 ;
	public static final short F64ARRAY = 45 ;
	*/

	/** XML content, currently not supported */
	public static final short XML = 47 ;
	/** Simple bean */
	protected static final short BEAN = 49 ;

	/** Simple bean Array */
	protected static final short BEAN_ARRAY = 50 ;

	/** Hash table */
	protected static final short HASH_TABLE = 51 ;
	/** Void value */
	protected static final short VOID = 52 ;
	/*
	public static final short USER_FIRST = 128 ;
	public static final short USER_LAST = 255 ;
	*/
	/** Indentify vData begin */
	protected static final short VDATA_START = 101 ;
	/** Indentify vData end */
	protected static final short VDATA_END = 102 ;
	/** Indentify kData begin */
	protected static final short KDATA_START = 103 ;
	/** Indentify kData end */
	protected static final short KDATA_END = 104 ;
	/** Indentify key name begin */
	protected static final short KEY_NAME = 105 ;
	/** reserved type, currently no use */
	protected static final short RESERVED = 127 ;
	/** the magic number */
	protected static final short MAGIC = 126 ;

	private Vector vDatas = new Vector (10) ;
	private Hashtable kDatas = new Hashtable (5) ;
	private String encoding = null ;

	/**
	 * Get character encoding string.<br/>
	 * If no encoding assigned, default encoding is used.
	 * @return null if no encoding assigned.
	 */
	public String getEncoding ()
	{
		return encoding ;
	}

	/**
	 * Set character encoding.<br/>
	 * @exception UnsupportedEncodingException if an invalid encoding
	 */
	public void setEncoding (String en)
		throws UnsupportedEncodingException
	{
		if (en != null)
		{
			// test encoding
			" ".getBytes (en) ;

			encoding = en ;
		}
	}
	/**
	 * Simply set encoding to null.
	 */
	public void resetEncoding ()
	{
		encoding = null ;
	}
	/**
	 * The implemention for add a data by key.<br/>
	 * @param key	the key.
	 * @param o		the value.
	 * @param type	the type of value.
	 * @exception	IllegalArgumentException	if key is null.
	 */
	protected void addImpl (String key , Object o , short type)
	{
		if (key == null)
			throw new IllegalArgumentException ("null key is not allowed here!") ;

		kDatas.put (key , new Data (o , type)) ;
	}
	/**
	 * The implemention for add a data by key.<br/>
	 * @param key	the key.
	 * @param f		the Data value.
	 * @exception	IllegalArgumentException	if key is null.
	 */
	protected void addImpl (String key , Data f)
	{
		if (key == null)
			throw new IllegalArgumentException ("null key is not allowed here!") ;
		kDatas.put (key , f) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , Object o)
	{
		addImpl (key , Data.createData (o)) ;
	}
	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (int i)
	{
		addImpl (new Integer (i) , INTEGER) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , int i)
	{
		addImpl (key , new Integer (i) , INTEGER) ;
	}
	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (long i)
	{
		addImpl (new Long (i) , LONG) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , long i)
	{
		addImpl (key , new Long (i) , LONG) ;
	}
	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (double d)
	{
		addImpl (new Double (d) , DOUBLE) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , double d)
	{
		addImpl (key , new Double (d) , DOUBLE) ;
	}
	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (float f)
	{
		addImpl (new Float (f) , FLOAT) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , float f)
	{
		addImpl (key , new Float (f) , FLOAT) ;
	}
	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (byte b)
	{
		addImpl (new Byte (b) , BYTE) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , byte b)
	{
		addImpl (key , new Byte (b) , BYTE) ;
	}
	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (boolean b)
	{
		addImpl (new Boolean (b) , BOOLEAN) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , boolean b)
	{
		addImpl (key , new Boolean (b) , BOOLEAN) ;
	}
	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (short s)
	{
		addImpl (new Short (s) , SHORT) ;
	}

	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , short s)
	{
		addImpl (key , new Short (s) , SHORT) ;
	}

	/**
	 * @see #addImpl(Object,short)
	 */
	public void add (char c)
	{
		addImpl (new Character (c) , CHAR) ;
	}

	/**
	 * @see #addImpl(String,Object,short)
	 */
	public void add (String key , char c)
	{
		addImpl (key , new Character (c) , CHAR) ;
	}


	/**
	 * The implemention for add a data in rder.<br/>
	 * @param o		the value.
	 * @param type	the type of value.
	 */
	protected void addImpl (Object o , short type)
	{

		Data f = new Data (o , type) ;

		vDatas.addElement (f) ;
	}
	/**
	 * The implemention for add a data in rder.<br/>
	 * @param f		the data.
	 */
	protected void addImpl (Data f)
	{
		vDatas.addElement (f) ;
	}
	/**
	 * Add a value in order.
	 * @param o		the value.
	 * @exception	IllegalArgumentException	if the object type is unsupported
	 */
	public void add (Object o)
	{
		// if ()
		addImpl (Data.createData (o)) ;
	}

	/**
	 * @see #addImpl(Object,short)
	 * @exception IllegalArgumentException if msg is dead-locked.
	 */
	public void add (Message msg)
	{
		if (msg == null)
		{
			addImpl (Data.createData (null)) ;
			return ;
		}
		if (msg == this && msg.contains (this))
		{
			throw new IllegalArgumentException ("can't add dead-locked Message!") ;
		}
		addImpl (msg , MSG) ;
	}
	/**
	 * @see #addImpl(String,Object,short)
	 * @exception IllegalArgumentException if msg is dead-locked.
	 */
	public void add (String key , Message msg)
	{
		if (msg == null)
		{
			addImpl (key , Data.createData (null)) ;
			return ;
		}
		if (msg == this || msg.contains (this))
		{
			throw new IllegalArgumentException ("can't add dead-locked Message!") ;
		}
		addImpl (key , msg , MSG) ;
	}
	/**
	 * Check if contains this msg in data links.
	 */
	public boolean contains (Message msg)
	{
		if (msg == this)
			return true ;
		Data data = null ;
		Message tmp = null ;
		for (int i = 0 ; i < vDatas.size () ; i ++)
		{
			data = (Data) vDatas.elementAt (i) ;
			if (data.type == MSG)
			{
				tmp = (Message) data.value ;
				if (tmp == msg || tmp.contains (msg))
					return true ;
			}
		}

		for (Enumeration e = kDatas.elements () ; e.hasMoreElements () ; )
		{
			data = (Data) e.nextElement () ;

			if (data.type == MSG)
			{
				tmp = (Message) data.value ;
				if (tmp == msg || tmp.contains (msg))
					return true ;
			}
		}

		return false ;
	}
	/**
	 * Remove a value form data store by position.
	 * @param pos	the position want to removed.
	 * @exception	ArrayIndexOutOfBoundsException	if the pos outof range
	 */
	public Object remove (int pos)
	{
		Object o = vDatas.elementAt (pos) ;
		vDatas.removeElementAt (pos) ;
		return ((Data) o).value ;

	}
	/**
	 * Remove a value form data store by key.
	 * @param key	the key want to removed.
	 * @exception	IllegalArgumentException	if key is null
	 * @exception	NoSuchElementException		if no such key
	 */
	public Object remove (String key)
	{
		if (key == null)
			throw new IllegalArgumentException ("null key is not allowed!") ;

		if (! kDatas.containsKey (key))
			return null ;
			// throw new NoSuchElementException ("No such key [" + key + "]") ;

		return ((Data) kDatas.remove (key)).value ;
	}
	/**
	 * The implemention of insert action.
	 */
	protected void insertImpl (Object o , short type , int pos)
	{
		if (pos < 0 || pos > vDatas.size ())
			throw new ArrayIndexOutOfBoundsException ("insert index error: size=" +
				vDatas.size () + ", position=" + pos) ;

		vDatas.insertElementAt (new Data (o , type) , pos) ;
	}
	/**
	 * The implemention of insert action.
	 */
	protected void insertImpl (Data data , int pos)
	{
		if (pos < 0 || pos > vDatas.size ())
			throw new ArrayIndexOutOfBoundsException ("insert index error: size=" +
				vDatas.size () + ", position=" + pos) ;

		vDatas.insertElementAt (data , pos) ;
	}
	/**
	 * Insert a Object at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (Object o , int pos)
	{
		insertImpl (Data.createData (o) , pos) ;
	}
	/**
	 * Insert a integer number at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (int i , int pos)
	{
		insertImpl (new Integer (i) , INTEGER , pos) ;
	}
	/**
	 * Insert a single byte at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (byte b , int pos)
	{
		insertImpl (new Byte (b) , BYTE , pos) ;
	}
	/**
	 * Insert a boolean value at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (boolean b , int pos)
	{
		insertImpl (new Boolean (b) , BOOLEAN , pos) ;
	}
	/**
	 * Insert a floating-point number at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (float f , int pos)
	{
		insertImpl (new Float (f) , FLOAT , pos) ;
	}
	/**
	 * Insert a double-floating-point number at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (double d , int pos)
	{
		insertImpl (new Double (d) , DOUBLE , pos) ;
	}
	/**
	 * Insert a short number at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (short s , int pos)
	{
		insertImpl (new Short (s) , SHORT , pos) ;
	}
	/**
	 * Insert a long number at specified position.
	 * @exception ArrayIndexOutOfBoundsException if pos is invalid.
	 */
	public void insert (long s , int pos)
	{
		insertImpl (new Long (s) , LONG , pos) ;
	}

	/**
	 * The implemention of update action.
	 */
	protected Data updateImpl (Object o , short type , int pos)
	{
		if (pos < 0 || pos > vDatas.size ())
			throw new ArrayIndexOutOfBoundsException ("insert index error: size=" +
				vDatas.size () + ", position=" + pos) ;

		Data d = (Data) vDatas.elementAt (pos) ;
		vDatas.setElementAt (new Data (o , type) , pos) ;
		return d ;
	}
	/**
	 * The implemention of update action.
	 */
	protected Data updateImpl (Data data , int pos)
	{
		if (pos < 0 || pos > vDatas.size ())
			throw new ArrayIndexOutOfBoundsException ("insert index error: size=" +
				vDatas.size () + ", position=" + pos) ;
		if (pos == vDatas.size ())
		{
			vDatas.addElement (data) ;
			return null ;
		}
		Data d = (Data) vDatas.elementAt (pos) ;
		vDatas.setElementAt (data , pos) ;

		return d ;
	}

	public Data update (Object o , int pos)
	{
		return updateImpl (Data.createData (o) , pos) ;
	}

	public Data update (int i , int pos)
	{
		return updateImpl (new Integer (i) , INTEGER , pos) ;
	}

	public Data update (byte b , int pos)
	{
		return updateImpl (new Byte (b) , BYTE , pos) ;
	}

	public Data update (boolean b , int pos)
	{
		return updateImpl (new Boolean (b) , BOOLEAN , pos) ;
	}

	public Data update (float f , int pos)
	{
		return updateImpl (new Float (f) , FLOAT , pos) ;
	}

	public Data update (double d , int pos)
	{
		return updateImpl (new Double (d) , DOUBLE , pos) ;
	}

	public Data update (short s , int pos)
	{
		return updateImpl (new Short (s) , SHORT , pos) ;
	}

	public Data update (long s , int pos)
	{
		return updateImpl (new Long (s) , LONG , pos) ;
	}
	/**
	 * Get the value form data store by position.
	 * @param pos	the position of the value.
	 * @exception	ArrayIndexOutOfBoundsException	if the pos outof range
	 */
	public Object get (int pos)
	{
		Data d = (Data) vDatas.elementAt (pos) ;
		if (d.type == NULL)
			return null ;
		else
			return d.value ;
	}
	/**
	 * Get the value form data store by key.
	 * @param key	the key of the value.
	 * @return null if the type of data is NULL
	 * @exception	IllegalArgumentException	if key is null
	 * @exception	NoSuchElementException		if no such key
	 */
	public Object get (String key)
	{
		if (key == null)
			throw new IllegalArgumentException ("Illegal key assigned [null]") ;
		Data d = (Data) kDatas.get (key) ;
		if (d == null)
			return null ;
			// throw new NoSuchElementException ("No such key [" + key + "]") ;
		if (d.type == NULL)
			return null ;
		return d.value ;
	}
	/**
	 * Get ordered data size.
	 */
	public int getSize ()
	{
		return vDatas.size () ;
	}
	/**
	 * Get key-ordered data size.
	 */
	public int getKeySize ()
	{
		return kDatas.size () ;
	}
	/**
	 * Remove all data from message.
	 */
	public void clear ()
	{
		vDatas.removeAllElements () ;
		kDatas.clear () ;
	}

	/**
	 * Get keys as an Enumeration.
	 */
	public Enumeration getKeys ()
	{
		return kDatas.keys () ;
	}
	/**
	 * Check if contains this key.
	 */
	public boolean containsKey (String key)
	{
		return kDatas.containsKey (key) ;
	}

	public Vector getVector ()
	{
		Vector v = new Vector () ;

		for (int i = 0 ; i < vDatas.size () ; i ++)
		{
			v.addElement (((Data)vDatas.elementAt (i)).value) ;

		}

		return v ;
	}

	public Hashtable getKeyPairs ()
	{
		Hashtable table = new Hashtable (kDatas.size ()) ;

		for (Enumeration e = kDatas.keys () ; e.hasMoreElements () ; )
		{
			Object key = e.nextElement () ;
			Object value = ((Data)kDatas.get (key)).value ;
			if (value != null)
				table.put (key , value) ;
		}

		return table ;
	}
	/**
	 * @return theh type of data at specified position.
	 */
	public short getType (int pos)
	{
		return ((Data)(vDatas.elementAt (pos))).type ;
	}
	/**
	 * @return theh type of data by specified key.
	 */
	public short getType (String key)
	{
		if (key == null)
			throw new IllegalArgumentException ("Illegal key assigned [null]") ;
		Data d = (Data) kDatas.get (key) ;
		if (d == null)
			throw new NoSuchElementException ("access by key [" + key + "]") ;

		return d.type ;
	}
	/**
	 * Pack message Data to bytes array.
	 */
	public byte [] pack ()
		throws IOException
	{
		return Message.packToBytes (this) ;
	}
	/**
	 * Pack message Data to bytes array.
	 */
	@SuppressWarnings("deprecation")
	public static byte [] packToBytes (Message msg)
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream () ;
		String en = msg.getEncoding () ;
		for (int i = 0 ; i < msg.vDatas.size () ; i ++)
		{
			Data data = (Data) msg.vDatas.elementAt (i) ;
			packData (data , out , en) ;
		}
		byte [] vdataBytes = out.toByteArray () ;
		// out.reset () ;

		out = new ByteArrayOutputStream () ;
		for (Enumeration e = msg.kDatas.keys () ; e.hasMoreElements () ; )
		{
			String key = (String) e.nextElement () ;
			Data data = (Data) msg.kDatas.get (key) ;
			out.write ((byte) KEY_NAME) ;
			byte [] keyName = null ;
			if (en == null)
				keyName = key.getBytes () ;
			else
				keyName = key.getBytes (en) ;
			int keyLength = keyName.length ;
			if (keyLength > 0xFF)
				throw new ArrayIndexOutOfBoundsException ("key too long [" + keyLength + "]") ;
			out.write ((byte) keyLength) ;
			out.write (keyName) ;

			packData (data , out , en) ;
		}

		byte [] kdataBytes = out.toByteArray () ;

		if (en == null)
			en = sun.io.ByteToCharConverter.getDefault ().getCharacterEncoding () ;

		/*byte [] encodingBytes = new byte [en.length ()] ;
		// get as ascii
		for (int k = 0 ; k < en.length () ; k ++)
		{
			encodingBytes [k] = (byte) (en.charAt (k) & 0x7F) ;
		}
		*/
		byte [] encodingBytes = en.getBytes ("UTF8") ;
		byte [] message = new byte [
			1 // MAGIC
			+ 8 // message length ;
			+ 2 // encoding length ;
			+ encodingBytes.length
			+ 1 // VDATA_START
			+ 2 // VDATA_SIZE
			+ 4 // VDATA length
			+ vdataBytes.length
			+ 1 // VDATA_END
			+ 1 // KDATA_START
			+ 2 // KDATA_SIZE
			+ 4 // KDATA length
			+ kdataBytes.length
			+ 1 // KDATA_END
			] ;

		message [0] = (byte) MAGIC ;
		// message []
		System.arraycopy (longToBytes ((long) message.length) , 0 , message , 1 , 8) ;

		System.arraycopy (shortToBytes ((short) encodingBytes.length) , 0 , message , 9 , 2) ;

		System.arraycopy (encodingBytes , 0 , message , 11 , encodingBytes.length) ;

		int offset = 11 + encodingBytes.length ;

		message [offset] = (byte) VDATA_START ;
		offset ++ ;

		System.arraycopy (shortToBytes ((short) msg.vDatas.size ()) , 0 , message , offset , 2) ;
		offset += 2 ;

		System.arraycopy (intToBytes (vdataBytes.length) , 0 , message , offset , 4) ;
		offset += 4 ;

		System.arraycopy (vdataBytes , 0 , message , offset , vdataBytes.length) ;
		offset += vdataBytes.length ;

		message [offset] = (byte) VDATA_END ;

		offset += 1 ;

		message [offset] = (byte) KDATA_START ;
		offset += 1 ;

		System.arraycopy (shortToBytes ((short) msg.kDatas.size ()) , 0 , message , offset , 2) ;
		offset += 2 ;

		System.arraycopy (intToBytes (kdataBytes.length) , 0 , message , offset , 4) ;
		offset += 4 ;

		System.arraycopy (kdataBytes , 0 , message , offset , kdataBytes.length) ;

		message [message.length - 1] = (byte) KDATA_END ;

		return message ;
	}
	public static Message unpack (byte [] message) throws IOException
	{
		return extractFromStream (new ByteArrayInputStream (message)) ;
	}

	public static Message unpack (InputStream in) throws IOException
	{
		return extractFromStream (in) ;
	}
	/**
	 * extract message from bytes array.
	 */
	public static Message extractFromBytes (byte [] message) throws IOException
	{
		return extractFromStream (new ByteArrayInputStream (message)) ;
	}
	/**
	 * extract message from inputStream.
	 */
	public static Message extractFromStream (InputStream in)
		throws IOException
	{

		if (readByte (in) != MAGIC)
			throw new MessageFormatException ("First byte must be MAGIC!") ;
		long size = readLong (in) ;
		if (size <= 0)
		{
			throw new MessageFormatException ("message length error!") ;
		}
		// if (readLong (in) <= 0)

		//	throw new IOException ("message length error!") ;

		/*
		byte [] msgBuffer = new byte [size] ;
		count =
		while (count )
		*/
		short encodingSize = 0 ;
		if ((encodingSize = readShort (in)) <= 0)
			throw new MessageFormatException ("Wrong encoding size [" + encodingSize  + "]") ;


		byte [] encodingBytes = new byte [encodingSize] ;

		int ret = in.read (encodingBytes) ;
		if (ret < encodingSize)
			throw new MessageFormatException ("Error get encoding [" + ret + " < " + encodingSize + "]") ;

		Message msg = new Message () ;
		String encoding = new String (encodingBytes , "UTF8") ;

		msg.setEncoding (encoding) ;

		// System.out.println ("Encoding " + encoding) ;
		" ".getBytes (encoding) ;

		if (readByte (in) != VDATA_START)
			throw new MessageFormatException ("Must be VDATA_START!") ;

		int vSize = readShort (in) ;
		if (vSize < 0)
			throw new MessageFormatException ("VData size error [" + vSize + "]") ;

		int vLength = readInt (in) ;

		if (vLength < 0)
			throw new MessageFormatException ("VData length error [" + vLength + "]") ;

		// process VData
		short vType = 0 ;
		while ((vType = readByte (in)) != VDATA_END)
		{

			Data d1 = extractData (in , vType , encoding) ;
			// System.out.println (d1) ;
			msg.addImpl (d1) ;
		}


		if (readByte (in) != KDATA_START)
			throw new MessageFormatException ("Must be KDATA_START!") ;
		int kSize = readShort (in) ;
		if (kSize < 0)
			throw new MessageFormatException ("KData size error [" + kSize + "]") ;

		int kLength = readInt (in) ;

		if (kLength < 0)
			throw new MessageFormatException ("KData length error [" + kLength + "]") ;
		// process KData
		short kType = 0 ;
		while ((kType = readByte (in)) == KEY_NAME)
		{
			String key = null ;
			int keyLength = readByte (in) & 0xFF ;
			if (keyLength <= 0)
			{
				key = new String () ;
			}
			else
			{
				byte [] keyBytes = new byte [keyLength] ;

				in.read (keyBytes) ;

				key = new String (keyBytes , encoding) ;
			}

			kType = readByte (in) ;

			msg.addImpl (key , extractData (in , kType , encoding)) ;
		}

		if (kType != KDATA_END)
			throw new MessageFormatException ("Must be KDATA_END [" + kType + "]") ;


		return msg ;

	}
	private final static byte [] readBytes (InputStream in , int size)
		throws IOException
	{
		if (size <= 0)
			return new byte [0] ;

		byte [] buffer = new byte [size] ;

		int count = 0 ;
		int ret = 0 ;
		while (true)
		{
			ret = in.read (buffer , count , size - count) ;
			if (ret == -1)
				throw new IOException ("No more bytes! [" + count + " < " + size + "]") ;
			count += ret ;
			if (count == size)
				break ;

		}
		if (count != size)
			throw new IOException ("Must be " + size + " bytes! [" + count + "]") ;

		return buffer ;
	}
	/**
	 * read boolean value from inputStream.
	 */
	protected static boolean readBoolean (InputStream in)
		throws IOException
	{
		return byteToBoolean (readByte (in)) ;
	}
	/**
	 * read long value from inputStream.
	 */
	protected static long readLong (InputStream in)
		throws IOException
	{

		return bytesToLong (readBytes (in , 8)) ;
	}

	/**
	 * read byte value from inputStream.
	 */
	protected static byte readByte (InputStream in)
		throws IOException
	{
		int ret = in.read () ;
		if (ret < 0)
			throw new IOException ("Must be 1bytes! [" + ret + "]") ;

		return (byte) ret ;
	}

	/**
	 * read short value from inputStream.
	 */
	protected static short readShort (InputStream in)
		throws IOException
	{
		return bytesToShort (readBytes (in , 2)) ;
	}
	/**
	 * read integer value from inputStream.
	 */
	protected static int readInt (InputStream in)
		throws IOException
	{

		return bytesToInt (readBytes (in , 4)) ;
	}
	/**
	 * read float value from inputStream.
	 */
	protected static float readFloat (InputStream in)
		throws IOException
	{
		return bytesToFloat (readBytes (in , 4)) ;
	}
	/**
	 * read double value from inputStream.
	 */
	protected static double readDouble (InputStream in)
		throws IOException
	{

		return bytesToDouble (readBytes (in , 8)) ;
	}

	protected static byte [] readByteArray (InputStream in , int size)
		throws IOException
	{

		return readBytes (in , size) ;
	}

	protected static short [] readShortArray (InputStream in , int size)
		throws IOException
	{
		short [] shorts = new short [size] ;

		for (int i = 0 ; i < size ; i ++)
		{
			shorts [i] = readShort (in) ;
		}

		return shorts ;
	}

	protected static int [] readIntArray (InputStream in , int size)
		throws IOException
	{
		int [] ints = new int [size] ;

		for (int i = 0 ; i < size ; i ++)
		{
			ints [i] = readInt (in) ;
		}

		return ints ;
	}

	protected static long [] readLongArray (InputStream in , int size)
		throws IOException
	{
		long [] longs = new long [size] ;

		for (int i = 0 ; i < size ; i ++)
		{
			longs [i] = readLong (in) ;
		}

		return longs ;
	}


	protected static float [] readFloatArray (InputStream in , int size)
		throws IOException
	{
		float [] floats = new float [size] ;

		for (int i = 0 ; i < size ; i ++)
		{
			floats [i] = readFloat (in) ;
		}

		return floats ;
	}


	protected static double [] readDoubleArray (InputStream in , int size)
		throws IOException
	{
		double [] doubles = new double [size] ;

		for (int i = 0 ; i < size ; i ++)
		{
			doubles [i] = readDouble (in) ;
		}

		return doubles ;
	}

	protected static void packData (Data data , OutputStream out , String encoding)
		throws IOException
	{
		// System.out.println (Data.typeToString (data.type)) ;
		byte [] bytes = null ;
		int k = 0 ;
		switch (data.type)
		{
		case NULL :
			out.write ((byte) data.type & 0xFF) ;
			break ;
		case MSG :
			out.write ((byte) data.type & 0xFF) ;

			bytes = packToBytes ((Message) data.value) ;
			out.write (intToBytes (bytes.length)) ;

			out.write (bytes) ;
			break ;
		case CHAR :
			out.write ((byte) data.type & 0xFF) ;
			char c = ((Character) data.value).charValue () ;

			out.write (shortToBytes ((short) c)) ;

			break ;
		case CHAR_ARRAY :
			out.write ((byte) data.type & 0xFF) ;
			char [] ca = (char []) data.value ;

			out.write (intToBytes (ca.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < ca.length ; k ++)
			{
				out.write (shortToBytes ((short) ca [k])) ;
			}
			break ;

		case DATETIME :
			out.write ((byte) data.type & 0xFF) ;
			out.write (longToBytes (((Date) data.value).getTime ())) ;
			break ;
		case BYTE :
			out.write ((byte) data.type & 0xFF) ;
			out.write (((Byte) (data.value)).byteValue ()) ;
			break ;
		case SHORT :
			out.write ((byte) data.type & 0xFF) ;
			out.write (shortToBytes (((Short) (data.value)).shortValue ())) ;
			break ;
		case INTEGER :
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (((Integer) (data.value)).intValue ())) ;
			break ;
		case LONG :
			out.write ((byte) data.type & 0xFF) ;
			// byte [] lbytes = longToBytes (((Long) (data.value)).longValue ()) ;

			out.write (longToBytes (((Long) (data.value)).longValue ())) ;
			break ;
		case FLOAT :
			out.write ((byte) data.type & 0xFF) ;
			out.write (floatToBytes (((Float) (data.value)).floatValue ())) ;
			break ;
		case DOUBLE :
			out.write ((byte) data.type & 0xFF) ;
			out.write (doubleToBytes (((Double) (data.value)).doubleValue ())) ;
			break ;
		case BOOLEAN :
			out.write ((byte) data.type & 0xFF) ;
			out.write (booleanToByte (((Boolean) (data.value)).booleanValue ())) ;
			break ;
		case VOID :
			out.write ((byte) data.type & 0xFF) ;
			break ;
		case STRING :
			out.write ((byte) data.type & 0xFF) ;

			if (encoding == null)
				bytes = ((String) data.value).getBytes () ;
			else
			{
				bytes = ((String) data.value).getBytes (encoding) ;
			}
			// System.out.println ("packed String length = " + bytes.length) ;
			out.write (intToBytes (bytes.length)) ;
			out.write (bytes) ;
			/*
			String s = null ;
			if (encoding != null)
				s = new String (bytes , encoding) ;
			else
				s = new String (bytes) ;
			System.out.println ("[" + s + "]") ;
			*/
			break ;
		case BYTE_ARRAY :
			byte [] ba = (byte []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (ba.length)) ;
			out.write (ba) ;
			break ;
		case SHORT_ARRAY :
			short [] sa = (short []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (sa.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < sa.length ; k ++)
			{
				out.write (shortToBytes (sa [k])) ;
			}
			break ;
		case INTEGER_ARRAY :
			int [] ia = (int []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (ia.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < ia.length ; k ++)
			{
				out.write (intToBytes (ia [k])) ;
			}
			break ;
		case LONG_ARRAY :
			long [] la = (long []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (la.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < la.length ; k ++)
			{
				out.write (longToBytes (la [k])) ;
			}
			break ;
		case FLOAT_ARRAY :
			float [] fa = (float []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (fa.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < fa.length ; k ++)
			{
				out.write (floatToBytes (fa [k])) ;
			}
			break ;
		case DOUBLE_ARRAY :
			double [] da = (double []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (da.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < da.length ; k ++)
			{
				out.write (doubleToBytes (da [k])) ;
			}
			break ;
		case BOOLEAN_ARRAY :
			boolean [] bola = (boolean []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (bola.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < bola.length ; k ++)
			{
				out.write (booleanToByte (bola [k])) ;
			}
			break ;
		case STRING_ARRAY :
			String [] stra = (String []) data.value ;
			out.write ((byte) data.type & 0xFF) ;
			out.write (intToBytes (stra.length)) ;
			// out.write (ba) ;
			for (k = 0 ; k < stra.length ; k ++)
			{
				// out.write ((byte) STRING & 0xFF) ;

				if (encoding == null)
					bytes = (stra [k]).getBytes () ;
				else
				{
					bytes = (stra [k]).getBytes (encoding) ;
				}
				// System.out.println ("packed String length = " + bytes.length) ;
				out.write (intToBytes (bytes.length)) ;
				out.write (bytes) ;
				// out.write (stringToBytes (stra [k])) ;
			}
			break ;
		case BEAN_ARRAY :
			Class arrayClass = data.value.getClass () ;
			Object beanArray = data.value ;
			Class cClass = arrayClass.getComponentType () ;

			out.write ((byte) data.type & 0xFF) ;
			// Array Size
			int arraySize = java.lang.reflect.Array.getLength (beanArray) ;
			out.write (intToBytes (arraySize)) ;
			// array class name
			if (encoding == null)
				bytes = cClass.getName ().getBytes () ;
			else
			{
				bytes = cClass.getName ().getBytes (encoding) ;
			}
			// System.out.println ("packed String length = " + bytes.length) ;
			out.write (intToBytes (bytes.length)) ;
			out.write (bytes) ;

			for (k = 0 ; k < arraySize ; k ++)
			{
				packData (Data.createData (java.lang.reflect.Array.get (beanArray , k)) , out , encoding) ;
			}
			break ;
		case BEAN :
			Object bean = data.value ;
			Class beanClass = bean.getClass () ;

			Message beanMessage = new Message () ;

			beanMessage.setEncoding (encoding) ;

			try
			{
				ObjectPackingToolkit.packObject (bean , beanMessage) ;
			}
			catch (Throwable _t)
			{
				_t.printStackTrace () ;
				return ;
			}

			out.write ((byte) BEAN & 0xFF) ;

			bytes = beanMessage.pack () ;

			out.write (bytes) ;
			out.flush () ;
			return ;
		case HASH_TABLE :
			Hashtable hashtable = (Hashtable) data.value ;

			Message hashMsg = new Message () ;
			hashMsg.setEncoding (encoding) ;

			out.write ((byte) HASH_TABLE & 0xFF) ;
			out.write (intToBytes (hashtable.size ())) ;
			for (Enumeration keys = hashtable.keys () ; keys.hasMoreElements () ; )
			{
				Object key = keys.nextElement () ;
				Object value = hashtable.get (key) ;

				packData (Data.createData (key) , out , encoding) ;
				packData (Data.createData (value) , out , encoding) ;
			}

			out.flush () ;
			return ;
		default :
			throw new IllegalArgumentException ("Unsupported DataType " + data.type) ;
		}
	}

	protected static Data extractData (InputStream in , short type , String encoding)
		throws IOException
	{
		Data data = new Data (null , NULL) ;

		data.type = type ;
		int k = 0 ;
		switch (type)
		{
		case NULL :

			break ;
		case MSG :
			int msgSize = readInt (in) ;

			if (msgSize <= 0)
				throw new IOException ("Empty message error [" + msgSize + "]") ;

			byte [] msgBytes = readBytes (in , msgSize) ;

			data.value = Message.extractFromBytes (msgBytes) ;

			break ;
		case DATETIME :
			// out.write (longToBytes (((Date) data.value).getTime ())) ;
			data.value = new Date (readLong (in)) ;
			break ;
		case BYTE :
			data.value = new Byte (readByte (in)) ;
			break ;
		case SHORT :
			data.value = new Short (readShort (in)) ;

			break ;
		case CHAR :
			data.value = new Character ((char) (readShort (in) & 0xFFFF)) ;
			break ;
		case CHAR_ARRAY :
			int charArrayLength = readInt (in) ;
			char [] charArray = new char [charArrayLength] ;
			for (k = 0 ; k < charArrayLength ; k ++)
			{
				charArray [k] = (char) (readShort (in) & 0xFFFF) ;
			}
			data.value = charArray ;
			break ;
		case INTEGER :
			data.value = new Integer (readInt (in)) ;
			break ;
		case LONG :
			data.value = new Long (readLong (in)) ;
			break ;
		case FLOAT :
			data.value = new Float (readFloat (in)) ;
			break ;
		case DOUBLE :
			data.value = new Double (readDouble (in)) ;
			break ;
		case BOOLEAN :
			data.value = new Boolean (readBoolean (in)) ;
			break ;
		case VOID :
			data.value = null ;
			break ;
		case STRING :
			int stringLength = readInt (in) ;

			if (stringLength <= 0)
			{
				data.value = new String () ;
				break ;
			}

			byte [] stringBytes = readBytes (in , stringLength) ;

			data.value = new String (stringBytes , encoding) ;
			// System.out.println ("Extract String length = " + stringLength + "[" + data.value + "]") ;
			break ;
		case BYTE_ARRAY :
			int byteArrayLength = readInt (in) ;
			byte [] byteArray = readBytes (in , byteArrayLength) ;

			data.value = byteArray ;
			break ;
		case SHORT_ARRAY :
			int shortArrayLength = readInt (in) ;
			short [] shortArray = new short [shortArrayLength] ;
			for (k = 0 ; k < shortArrayLength ; k ++)
			{
				shortArray [k] = readShort (in) ;
			}
			data.value = shortArray ;
			break ;
		case INTEGER_ARRAY :
			int intArrayLength = readInt (in) ;
			int [] intArray = new int [intArrayLength] ;
			for (k = 0 ; k < intArrayLength ; k ++)
			{

				intArray [k] = readInt (in) ;
			}
			data.value = intArray ;
			break ;
		case LONG_ARRAY :
			int longArrayLength = readInt (in) ;
			long [] longArray = new long [longArrayLength] ;
			for (k = 0 ; k < longArrayLength ; k ++)
			{

				longArray [k] = readLong (in) ;
			}
			data.value = longArray ;
			break ;
		case FLOAT_ARRAY :
			int floatArrayLength = readInt (in) ;
			float [] floatArray = new float [floatArrayLength] ;
			for (k = 0 ; k < floatArrayLength ; k ++)
			{

				floatArray [k] = readFloat (in) ;
			}
			data.value = floatArray ;
			break ;
		case DOUBLE_ARRAY :
			int doubleArrayLength = readInt (in) ;
			double [] doubleArray = new double [doubleArrayLength] ;
			for (k = 0 ; k < doubleArrayLength ; k ++)
			{

				doubleArray [k] = readDouble (in) ;
			}
			data.value = doubleArray ;
			break ;
		case STRING_ARRAY :
			int stringArrayLength = readInt (in) ;
			String [] stringArray = new String [stringArrayLength] ;
			for (k = 0 ; k < stringArrayLength ; k ++)
			{
				int strLength = readInt (in) ;

				if (strLength <= 0)
				{
					data.value = new String () ;
					break ;
				}

				byte [] strBytes = readBytes (in , strLength) ;


				stringArray [k] = new String (strBytes , encoding) ;
			}
			data.value = stringArray ;
			break ;
		case BOOLEAN_ARRAY :
			int bolArrayLength = readInt (in) ;
			boolean [] bolArray = new boolean [bolArrayLength] ;
			for (k = 0 ; k < bolArrayLength ; k ++)
			{

				bolArray [k] = readBoolean (in) ;
			}
			data.value = bolArray ;
			break ;

		case BEAN_ARRAY :
			int arraySize = readInt (in) ;
			int nameSize = readInt (in) ;
			byte [] cbytes = readBytes (in , nameSize) ;
			String className = new String (cbytes , encoding) ;

			Object beanArray = null ;
			try
			{
				beanArray = java.lang.reflect.Array.newInstance (
					Class.forName (className) ,
					arraySize) ;

				for (int j = 0 ; j < arraySize ; j ++)
				{
					java.lang.reflect.Array.set (beanArray , j , extractData (in , (short)in.read () , encoding).value) ;
				}
			}
			catch (Throwable _t)
			{
				_t.printStackTrace () ;
			}
			data.value = beanArray ;
			break ;
		case BEAN :

			Message beanMessage = Message.unpack (in) ;

			try
			{
				data.value = ObjectPackingToolkit.extractObject (beanMessage) ;
			}
			catch (Throwable _t)
			{
				_t.printStackTrace () ;
				data.value = null ;
			}
			break ;

		case HASH_TABLE :
			int tableSize = readInt (in) ;
			data.value = new Hashtable () ;
			if (tableSize <= 0)
				break ;

			for (int i = 0 ; i < tableSize ; i ++)
			{
				short objectType = (short) in.read () ;

				if (objectType < 0)
					break ;

				Object key = extractData (in , objectType , encoding).value ;

				objectType = (short) in.read () ;

				if (objectType < 0)
					break ;
				Object value = extractData (in , objectType , encoding).value ;

				((Hashtable) data.value).put (key , value) ;
			}

			break ;
		default :
			throw new IllegalArgumentException ("Unsupported DataType " + data.type) ;
		}

		return data ;
	}


	protected final static byte [] intToBytes (int i)
	{
		// int is 32bits, 4Bytes
		byte [] bytes = new byte [4] ;

		bytes [3] = (byte) (i & 0xFF) ;

		i = i >>> 8 ;
		bytes [2] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [1] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [0] = (byte) (i & 0xFF) ;

		return bytes ;
	}

	protected final static short bytesToShort (byte [] bytes)
	{
		if (bytes == null || bytes.length != 2)
			throw new IllegalArgumentException ("byte array size must be 2!") ;

		short i = 0 ;
		i = (short) (bytes [0] & 0xFF) ;
		i = (short) ((i << 8) | (bytes [1] & 0xFF)) ;

		return i ;
	}

	protected final static byte [] shortToBytes (short i)
	{
		// int is 32bits, 4Bytes
		byte [] bytes = new byte [2] ;

		bytes [1] = (byte) (i & 0xFF) ;

		i = (short) (i >>> 8) ;
		bytes [0] = (byte) (i & 0xFF) ;

		return bytes ;
	}

	protected final static int bytesToInt (byte [] bytes)
	{
		if (bytes == null || bytes.length != 4)
			throw new IllegalArgumentException ("byte array size must be 4!") ;

		int i = 0 ;
		i = ((bytes [0] & 0xFF) << 8) | (bytes [1] & 0xFF) ;
		i = (i << 8) | (bytes [2] & 0xFF) ;
		i = (i << 8) | (bytes [3] & 0xFF) ;

		return i ;
	}

	protected final static byte [] longToBytes (long i)
	{
		// long is 64bits, 8Bytes
		byte [] bytes = new byte [8] ;

		bytes [7] = (byte) (i & 0xFF) ;

		i = i >>> 8 ;
		bytes [6] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [5] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [4] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [3] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [2] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [1] = (byte) (i & 0xFF) ;
		i = i >>> 8 ;
		bytes [0] = (byte) (i & 0xFF) ;

		return bytes ;
	}

	protected final static long bytesToLong (byte [] bytes)
	{
		if (bytes == null || bytes.length != 8)
			throw new IllegalArgumentException ("byte array size must be 8!") ;

		long i = 0 ;

		i = ((bytes [0] & 0xFF) << 8) | (bytes [1] & 0xFF) ;
		i = (i << 8) | (bytes [2] & 0xFF) ;
		i = (i << 8) | (bytes [3] & 0xFF) ;
		i = (i << 8) | (bytes [4] & 0xFF) ;
		i = (i << 8) | (bytes [5] & 0xFF) ;
		i = (i << 8) | (bytes [6] & 0xFF) ;
		i = (i << 8) | (bytes [7] & 0xFF) ;
		// i = (i << 8) | (bytes [3] & 0xFF) ;
		return i ;
	}

	protected final static byte [] floatToBytes (float f)
	{
		return intToBytes (Float.floatToIntBits (f)) ;
	}

	protected final static float bytesToFloat (byte [] bytes)
	{
		return Float.intBitsToFloat (bytesToInt (bytes)) ;
	}

	protected final static byte [] doubleToBytes (double f)
	{
		return longToBytes (Double.doubleToLongBits (f)) ;
	}

	protected final static double bytesToDouble (byte [] bytes)
	{
		return Double.longBitsToDouble (bytesToLong (bytes)) ;
	}

	protected final static byte booleanToByte (boolean b)
	{
		if (b)
			return (byte) 1 ;
		else
			return (byte) 0 ;
	}

	protected final static boolean byteToBoolean (byte b)
	{
		return b != 0 ;
	}

	public void log ()
	{
		System.out.println ("Encoding: [" + getEncoding () + "]") ;

		System.out.println ("Vdatas (" + vDatas.size () + "):") ;

		for (int i = 0 ; i < vDatas.size () ; i ++)
			System.out.println (" " + i + "\t" + vDatas.elementAt (i).toString ()) ;

		System.out.println ("Kdatas (" + kDatas.size () + "):") ;
		for (Enumeration e = kDatas.keys () ; e.hasMoreElements () ; )
		{
			Object key = e.nextElement () ;

			System.out.println ("\t" + key + "\t" + kDatas.get (key).toString ()) ;
		}

	}
	public String toString ()
	{
		String nl = System.getProperty ("line.separator") ;
		StringBuffer buf = new StringBuffer ("Encoding: [" + getEncoding () + "]" + nl) ;

		buf.append ("Vdatas (" + vDatas.size () + "):" + nl) ;

		for (int i = 0 ; i < vDatas.size () ; i ++)
			buf.append (" " + i + "\t" + vDatas.elementAt (i).toString () + nl) ;

		buf.append ("Kdatas (" + kDatas.size () + "):" + nl) ;
		for (Enumeration e = kDatas.keys () ; e.hasMoreElements () ; )
		{
			Object key = e.nextElement () ;

			buf.append ("\t" + key + "\t" + kDatas.get (key).toString () + nl) ;
		}

		return buf.toString () ;
	}
	/*
	public static void main (String argv [])
		throws Throwable
	{
		Message msg = new Message () ;
		Message msg1 = new Message () ;

		msg.add (-10834) ;
		msg.add (-10834L) ;
		msg.add ((byte) 0x12) ;
		msg.add ((short) -2934) ;
		msg.add (293455.45545f) ;
		msg.add ("中文测试aaaa") ;

		msg.add (null) ;
		msg.add (true) ;
		msg.add (new Date ()) ;

		msg.add (new byte [] {2 , 3}) ;
		msg.add (new float [] {1.0f , 293455.45545f}) ;
		msg.add (new double [] {343.4654D , -909043.6788D}) ;

		// msg.add (msg) ;
		msg1.add (12305) ;
		msg1.add (3665.90f) ;
		// msg1.add (msg) ;
		msg.add ("message" , msg1) ;
		msg.add ("abc" , 1000) ;
		msg.add ("xyz" , "abcdefghijklmn") ;
		// msg.setEncoding ("ISO8859_1") ;
		msg.log () ;
		msg1.log () ;
		byte [] bytes = Message.packToBytes (msg) ;

		Message msg2 = Message.extractFromBytes (bytes) ;

		msg2.log () ;

		((Message) msg2.get ("message")).log () ;

	}
	*/
}

