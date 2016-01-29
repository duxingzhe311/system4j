package com.dw.net;

import java.io.*;
import java.util.*;

public class MsgPacker {
	public static final int ValueTypeStr = 0;
	public static final int ValueTypeByteArray = 1;

	Hashtable<String, Object> pname2v = new Hashtable<String, Object>();

	public void put(String pname, String v) {
		if (v == null)
			pname2v.remove(pname);
		else
			pname2v.put(pname, v);
	}

	public void put(String pname, byte[] cont) {
		if (cont == null)
			pname2v.remove(pname);
		else
			pname2v.put(pname, cont);
	}

	public Object get(String pname) {
		return pname2v.get(pname);
	}

	public String getString(String pname) {
		return (String) pname2v.get(pname);
	}

	public byte[] getByteArray(String pname) {
		return (byte[]) pname2v.get(pname);
	}

	public byte[] toBytes() throws Exception {
		ByteArrayOutputStream ms = new ByteArrayOutputStream();

		DataOutputStream bw = new DataOutputStream(ms);

		for (Iterator<String> en = pname2v.keySet().iterator(); en.hasNext();) {
			String k = en.next();
			// write key
			byte[] kb = k.getBytes("UTF-8");
			bw.writeInt(kb.length);
			bw.write(kb);

			// write value
			Object v = pname2v.get(k);
			byte[] vb = null;
			int vt = ValueTypeByteArray;
			if (v instanceof String) {
				vb = ((String) v).getBytes("UTF-8");
				vt = ValueTypeStr;
			} else if (v instanceof byte[]) {
				vb = (byte[]) v;
				vt = ValueTypeByteArray;
			}

			if (vb != null) {
				bw.writeInt(vb.length + 4);
				bw.writeInt((int) vt);
				bw.write(vb);
			}
		}

		bw.flush();
		return ms.toByteArray();
	}

	public static MsgPacker parseFrom(byte[] cont) throws Exception {
		MsgPacker mp = new MsgPacker();

		if (cont == null || cont.length <= 0)
			return mp;

		ByteArrayInputStream ms = new ByteArrayInputStream(cont);
		DataInputStream br = new DataInputStream(ms);

		try {
			while (true) {
				// read key
				int klen = br.readInt();
				byte[] kv = new byte[klen];
				br.read(kv);
				String k = new String(kv, "UTF-8");

				// read value
				int vlen = br.readInt();
				int vt = br.readInt();
				byte[] bv = new byte[vlen - 4];
				br.read(bv);
				if (vt == ValueTypeByteArray) {
					mp.pname2v.put(k, bv);
				} else if (vt == ValueTypeStr) {
					String sv = new String(bv, "UTF-8");
					mp.pname2v.put(k, sv);
				}
			}
		} catch (EOFException eofe) {

		}

		return mp;
	}
}