package com.dw.mconn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class MsgPacker
{
  public static final int ValueTypeStr = 0;
  public static final int ValueTypeByteArray = 1;
  Hashtable pname2v = new Hashtable();

  public void put(String pname, String v)
  {
    if (v == null)
      this.pname2v.remove(pname);
    else
      this.pname2v.put(pname, v);
  }

  public void put(String pname, byte[] cont)
  {
    if (cont == null)
      this.pname2v.remove(pname);
    else
      this.pname2v.put(pname, cont);
  }

  public Object get(String pname)
  {
    return this.pname2v.get(pname);
  }

  public String getString(String pname)
  {
    return (String)this.pname2v.get(pname);
  }

  public byte[] getByteArray(String pname)
  {
    return (byte[])this.pname2v.get(pname);
  }

  public byte[] toBytes()
    throws Exception
  {
    ByteArrayOutputStream ms = new ByteArrayOutputStream();

    DataOutputStream bw = new DataOutputStream(ms);

    for (Iterator en = this.pname2v.keySet().iterator(); en.hasNext(); )
    {
      String k = (String)en.next();

      byte[] kb = k.getBytes("UTF-8");
      bw.writeInt(kb.length);
      bw.write(kb);

      Object v = this.pname2v.get(k);
      byte[] vb = (byte[])null;
      int vt = 1;
      if ((v instanceof String))
      {
        vb = ((String)v).getBytes("UTF-8");
        vt = 0;
      }
      else if ((v instanceof byte[]))
      {
        vb = (byte[])v;
        vt = 1;
      }

      if (vb != null)
      {
        bw.writeInt(vb.length + 4);
        bw.writeInt(vt);
        bw.write(vb);
      }
    }

    bw.flush();
    return ms.toByteArray();
  }

  public static MsgPacker parseFrom(byte[] cont)
    throws Exception
  {
    MsgPacker mp = new MsgPacker();

    if ((cont == null) || (cont.length <= 0)) {
      return mp;
    }
    ByteArrayInputStream ms = new ByteArrayInputStream(cont);
    DataInputStream br = new DataInputStream(ms);
    try
    {
      while (true)
      {
        int klen = br.readInt();
        byte[] kv = new byte[klen];
        br.read(kv);
        String k = new String(kv, "UTF-8");

        int vlen = br.readInt();
        int vt = br.readInt();
        byte[] bv = new byte[vlen - 4];
        br.read(bv);
        if (vt == 1)
        {
          mp.pname2v.put(k, bv);
        }
        else if (vt == 0)
        {
          String sv = new String(bv, "UTF-8");
          mp.pname2v.put(k, sv);
        }
      }

    }
    catch (EOFException localEOFException)
    {
    }

    return mp;
  }
}
