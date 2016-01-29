package com.dw.system.encrypt;

import com.dw.system.Convert;

public class DesAvr
{
	static short[] sbox = {
	/* S-box 1 */
	0xE4, 0xD1, 0x2F, 0xB8, 0x3A, 0x6C, 0x59, 0x07, 0x0F, 0x74, 0xE2, 0xD1,
			0xA6, 0xCB, 0x95, 0x38, 0x41, 0xE8, 0xD6, 0x2B, 0xFC, 0x97, 0x3A,
			0x50, 0xFC, 0x82, 0x49, 0x17, 0x5B, 0x3E, 0xA0, 0x6D,
			/* S-box 2 */
			0xF1, 0x8E, 0x6B, 0x34, 0x97, 0x2D, 0xC0, 0x5A, 0x3D, 0x47, 0xF2,
			0x8E, 0xC0, 0x1A, 0x69, 0xB5, 0x0E, 0x7B, 0xA4, 0xD1, 0x58, 0xC6,
			0x93, 0x2F, 0xD8, 0xA1, 0x3F, 0x42, 0xB6, 0x7C, 0x05, 0xE9,
			/* S-box 3 */
			0xA0, 0x9E, 0x63, 0xF5, 0x1D, 0xC7, 0xB4, 0x28, 0xD7, 0x09, 0x34,
			0x6A, 0x28, 0x5E, 0xCB, 0xF1, 0xD6, 0x49, 0x8F, 0x30, 0xB1, 0x2C,
			0x5A, 0xE7, 0x1A, 0xD0, 0x69, 0x87, 0x4F, 0xE3, 0xB5, 0x2C,
			/* S-box 4 */
			0x7D, 0xE3, 0x06, 0x9A, 0x12, 0x85, 0xBC, 0x4F, 0xD8, 0xB5, 0x6F,
			0x03, 0x47, 0x2C, 0x1A, 0xE9, 0xA6, 0x90, 0xCB, 0x7D, 0xF1, 0x3E,
			0x52, 0x84, 0x3F, 0x06, 0xA1, 0xD8, 0x94, 0x5B, 0xC7, 0x2E,
			/* S-box 5 */
			0x2C, 0x41, 0x7A, 0xB6, 0x85, 0x3F, 0xD0, 0xE9, 0xEB, 0x2C, 0x47,
			0xD1, 0x50, 0xFA, 0x39, 0x86, 0x42, 0x1B, 0xAD, 0x78, 0xF9, 0xC5,
			0x63, 0x0E, 0xB8, 0xC7, 0x1E, 0x2D, 0x6F, 0x09, 0xA4, 0x53,
			/* S-box 6 */
			0xC1, 0xAF, 0x92, 0x68, 0x0D, 0x34, 0xE7, 0x5B, 0xAF, 0x42, 0x7C,
			0x95, 0x61, 0xDE, 0x0B, 0x38, 0x9E, 0xF5, 0x28, 0xC3, 0x70, 0x4A,
			0x1D, 0xB6, 0x43, 0x2C, 0x95, 0xFA, 0xBE, 0x17, 0x60, 0x8D,
			/* S-box 7 */
			0x4B, 0x2E, 0xF0, 0x8D, 0x3C, 0x97, 0x5A, 0x61, 0xD0, 0xB7, 0x49,
			0x1A, 0xE3, 0x5C, 0x2F, 0x86, 0x14, 0xBD, 0xC3, 0x7E, 0xAF, 0x68,
			0x05, 0x92, 0x6B, 0xD8, 0x14, 0xA7, 0x95, 0x0F, 0xE2, 0x3C,
			/* S-box 8 */
			0xD2, 0x84, 0x6F, 0xB1, 0xA9, 0x3E, 0x50, 0xC7, 0x1F, 0xD8, 0xA3,
			0x74, 0xC5, 0x6B, 0x0E, 0x92, 0x7B, 0x41, 0x9C, 0xE2, 0x06, 0xAD,
			0xF3, 0x58, 0x21, 0xE7, 0x4A, 0x8D, 0xFC, 0x90, 0x35, 0x6B };

	static short[] e_permtab = { 4, 6, /* 4 bytes in 6 bytes out */
	32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15,
			16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26,
			27, 28, 29, 28, 29, 30, 31, 32, 1 };

	static short[] p_permtab = { 4, 4, /* 32 bit -> 32 bit */
	16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14,
			32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25 };

	static short[] ip_permtab = { 8, 8, /* 64 bit -> 64 bit */
	58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46,
			38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8, 57, 49, 41, 33,
			25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29,
			21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7 };

	static short[] inv_ip_permtab = { 8, 8, /* 64 bit -> 64 bit */
	40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46,
			14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12,
			52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50,
			18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25 };

	static short[] pc1_permtab = { 8, 7, /* 64 bit -> 56 bit */
	57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43,
			35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7,
			62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28,
			20, 12, 4 };

	static short[] pc2_permtab = { 7, 6, /* 56 bit -> 48 bit */
	14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7,
			27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44,
			49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32 };

	static short[] splitin6bitword_permtab = { 8, 8, /* 64 bit -> 64 bit */
	64, 64, 1, 6, 2, 3, 4, 5, 64, 64, 7, 12, 8, 9, 10, 11, 64, 64, 13, 18, 14,
			15, 16, 17, 64, 64, 19, 24, 20, 21, 22, 23, 64, 64, 25, 30, 26, 27,
			28, 29, 64, 64, 31, 36, 32, 33, 34, 35, 64, 64, 37, 42, 38, 39, 40,
			41, 64, 64, 43, 48, 44, 45, 46, 47 };

	static short[] shiftkey_permtab = { 7, 7, /* 56 bit -> 56 bit */
	2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
			23, 24, 25, 26, 27, 28, 1, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
			40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56,
			29 };

	static short[] shiftkeyinv_permtab = { 7, 7, 28, 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
			27, 56, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
			44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55 };

	static final int ROTTABLE = 0x7EFC;

	static final int ROTTABLE_INV = 0x3F7E;

	static void permute(short[] ptable, byte[] in, byte[] out)
	{
		//System.out.println("in len=="+in.length);
		short ib, ob; /* in-bytes and out-bytes */
		short bb, b_bit; /* counter for bit and byte */
		ib = ptable[0];// pgm_read_byte(&(ptable[0]));
		ob = ptable[1];// pgm_read_byte(&(ptable[1]));
		// ptable = &(ptable[2]);
		for (bb = 0; bb < ob; ++bb)
		{
			short x, t = 0;
			for (b_bit = 0; b_bit < 8; ++b_bit)
			{
				x = (short) (ptable[bb * 8 + b_bit + 2] - 1);// pgm_read_byte(&(ptable[byte*8+b_bit]))
																// -1 ;
				t <<= 1;
				if (((in[x / 8]) & (0x80 >> (x % 8))) > 0)
				{
					t |= 0x01;
				}
			}
			out[bb] = (byte) t;
		}
	}

	/** *************************************************************************** */

	static long changeendian32_(long a)
	{
		return (a & 0x000000FF) << 24 | (a & 0x0000FF00) << 8
				| (a & 0x00FF0000) >> 8 | (a & 0xFF000000) >> 24;
	}

	/** *************************************************************************** */

	static void shiftkey(byte[] key)
	{
		byte[] k = new byte[7];
		// memcpy(k, key, 7);
		System.arraycopy(key, 0, k, 0, 7);
		permute(shiftkey_permtab, k, key);
	}

	static void shiftkey_inv(byte[] key)
	{
		byte[] k = new byte[7];
		// memcpy(k, key, 7);
		System.arraycopy(key, 0, k, 0, 7);
		permute(shiftkeyinv_permtab, k, key);

	}

	/** *************************************************************************** */
	// static inline
	// uint64_t splitin6bitwords0(uint64_t a){
	// uint64_t ret=0;
	// a &= 0x0000ffffffffffffLL;
	// permute((prog_uint8_t*)splitin6bitword_permtab, (uint8_t*)&a,
	// (uint8_t*)&ret);
	// return ret;
	// }
	static void splitin6bitwords_uint64(byte[] p8, byte[] p8_out)
	{

		// a &= 0x0000ffffffffffffLL; p8改造如下
		// p8[0] = 0 ;
		// p8[1] = 0 ;
		// p8[2] = p8[3]= p8[4]= p8[5]= p8[6]= p8[7]= 0xff ;

		p8[0] = p8[1] = p8[2] = p8[3] = p8[4] = p8[5] = (byte) 0xff;
		p8[6] = p8[7] = 0;

		permute(splitin6bitword_permtab, p8, p8_out);
	}

	/** *************************************************************************** */

	static short substitute(short a, short[] sbp, int startidx)
	{
		short x;
		x = sbp[a >> 1 + startidx];// pgm_read_byte(&(sbp[a>>1]));
		x = (short) (((a & 1) > 0) ? x & 0x0F : x >> 4);
		return x;

	}

	static long byte4_to_long(byte[] bs, int startidx)
	{
		long r = 0;
		r += bs[3 + startidx];
		r <<= 8;
		r += bs[2 + startidx];
		r <<= 8;
		r += bs[1 + startidx];
		r <<= 8;
		r += bs[startidx];
		return r;
	}

	static long byte4_to_long(byte[] bs)
	{
		return byte4_to_long(bs, 0);
	}

	static byte[] long_to_byte4(long v)
	{
		byte[] r = new byte[4];

		r[0] = (byte) (v & 0xFF);
		v >>= 8;
		r[1] = (byte) (v & 0xFF);
		v >>= 8;
		r[2] = (byte) (v & 0xFF);
		v >>= 8;
		r[3] = (byte) (v & 0xFF);

		return r;
	}

	static void long_to_byte4(long v, byte[] r, int startidx)
	{
		r[startidx] = (byte) (v & 0xFF);
		v >>= 8;
		r[1 + startidx] = (byte) (v & 0xFF);
		v >>= 8;
		r[2 + startidx] = (byte) (v & 0xFF);
		v >>= 8;
		r[3 + startidx] = (byte) (v & 0xFF);

	}

	/** *************************************************************************** */
	// r 4字节
	static long des_f(long r, byte[] kr)
	{
		short i;
		byte[] ret = new byte[4];

		// t[0] = t[1] = t[2] = t[3] = 0 ;
		long t = 0;

		byte[] data = new byte[8], tmp_data = new byte[8];// uint64_t data;
															// //改造成8字节数组处理
		short[] sbp; /* sboxpointer */
		permute(e_permtab, long_to_byte4(r), data);
		for (i = 0; i < 7; ++i)
			data[i] ^= kr[i];

		/* Sbox substitution */
		splitin6bitwords_uint64(data, tmp_data);// splitin6bitwords(data);
		sbp = sbox;
		int startidx = 0;
		for (i = 0; i < 8; ++i)
		{
			short x;
			x = substitute(tmp_data[i], sbp, startidx);// x =
														// substitute(((uint8_t*)&data)[i],
														// sbp);
			t <<= 4;
			t |= x;
			startidx += 32;
		}
		t = changeendian32_(t);

		permute(p_permtab, long_to_byte4(t), ret);

		return byte4_to_long(ret);
	}

	/** *************************************************************************** */

	public static byte[] des_enc(byte[] out, byte[] in, byte[] key)
	{
		// #define R *((uint32_t*)&(data[4]))
		// #define L *((uint32_t*)&(data[0]))

		byte[] data = new byte[8], kr = new byte[7], k = new byte[7];
		short i;

		permute(ip_permtab, in, data);
		permute(pc1_permtab, key, k);
		for (i = 0; i < 8; ++i)
		{
			shiftkey(k);
			if ((ROTTABLE & ((1 << ((i << 1) + 0)))) > 0)
				shiftkey(k);
			permute(pc2_permtab, k, kr);
			// L ^= des_f(R, kr);

			long tmpl = byte4_to_long(data, 0);
			tmpl ^= des_f(byte4_to_long(data, 4), kr);
			long_to_byte4(tmpl, data, 0);

			shiftkey(k);
			if ((ROTTABLE & ((1 << ((i << 1) + 1)))) > 0)
				shiftkey(k);
			permute(pc2_permtab, k, kr);
			// R ^= des_f(L, kr);
			long tmpr = byte4_to_long(data, 4);
			tmpr ^= des_f(byte4_to_long(data, 0), kr);
			long_to_byte4(tmpr, data, 4);

		}
		/* L <-> R */
		// R ^= L;
		long tmpr = byte4_to_long(data, 4);
		tmpr ^= byte4_to_long(data, 0);
		long_to_byte4(tmpr, data, 4);

		// L ^= R;
		long tmpl = byte4_to_long(data, 0);
		tmpl ^= byte4_to_long(data, 4);
		long_to_byte4(tmpl, data, 0);

		// R ^= L;
		tmpr = byte4_to_long(data, 4);
		tmpr ^= byte4_to_long(data, 0);
		long_to_byte4(tmpr, data, 4);

		permute(inv_ip_permtab, data, out);

		return out;
	}

	/** *************************************************************************** */

	public static byte[] des_dec(byte[] out, byte[] in, byte[] key)
	{
		// #define R *((uint32_t*)&(data[4]))
		// #define L *((uint32_t*)&(data[0]))

		byte[] data = new byte[8], kr = new byte[7], k = new byte[7];
		short i;

		permute(ip_permtab, in, data);
		permute(pc1_permtab, key, k);
		for (i = 7; i >= 0; --i)
		{

			permute(pc2_permtab, k, kr);
			// L ^= des_f(R, kr);
			long tmpl = byte4_to_long(data, 0);
			tmpl ^= des_f(byte4_to_long(data, 4), kr);
			long_to_byte4(tmpl, data, 0);

			shiftkey_inv(k);
			if ((ROTTABLE & ((1 << ((i << 1) + 1)))) > 0)
			{
				shiftkey_inv(k);
			}

			permute(pc2_permtab, k, kr);
			// R ^= des_f(L, kr);
			long tmpr = byte4_to_long(data, 4);
			tmpr ^= des_f(byte4_to_long(data, 0), kr);
			long_to_byte4(tmpr, data, 4);

			shiftkey_inv(k);
			if ((ROTTABLE & ((1 << ((i << 1) + 0)))) > 0)
			{
				shiftkey_inv(k);
			}

		}
		/* L <-> R */
		// R ^= L;
		long tmpr = byte4_to_long(data, 4);
		tmpr ^= byte4_to_long(data, 0);
		long_to_byte4(tmpr, data, 4);

		// L ^= R;
		long tmpl = byte4_to_long(data, 0);
		tmpl ^= byte4_to_long(data, 4);
		long_to_byte4(tmpl, data, 0);

		// R ^= L;
		tmpr = byte4_to_long(data, 4);
		tmpr ^= byte4_to_long(data, 0);
		long_to_byte4(tmpr, data, 4);

		permute(inv_ip_permtab, data, out);

		return out;
	}

	/** *************************************************************************** */

	public static void tdes_enc(byte[] out, byte[] in, byte[] key)
	{
		byte[] tmpk = new byte[8];
		des_enc(out, in, key);
		System.arraycopy(key, 0, tmpk, 8, 8);
		des_dec(out, out, tmpk);
		System.arraycopy(key, 0, tmpk, 16, 8);
		des_enc(out, out, tmpk);
	}

	/** *************************************************************************** */

	public static void tdes_dec(byte[] out, byte[] in, byte[] key)
	{
		byte[] tmpk = new byte[8];
		des_dec(out, in, key);
		System.arraycopy(key, 0, tmpk, 8, 8);
		des_enc(out, out, tmpk);
		System.arraycopy(key, 0, tmpk, 16, 8);
		des_dec(out, out, tmpk);
	}

	public static void main(String[] args) throws Throwable
	{
		String data = "12345678";// >?:jklmn" ;
		String key = "Th345678";
		byte[] tmp_en=new byte[8],tmp_dc = new byte[8];
		// byte[] dd = encrypt(data.getBytes(), key.getBytes()) ;
		// 
		DesAvr.des_enc(tmp_en, data.getBytes(), key.getBytes());
		DesAvr.des_dec(tmp_dc, tmp_en, key.getBytes());
		
		String encdata = new String(tmp_en)+" | "+Convert.byteArray2HexStr(tmp_en);
		String decdata = new String(tmp_dc);

		System.out.println("enc data="+encdata) ;
		System.out.println("dec data=" + decdata);
	}
}
