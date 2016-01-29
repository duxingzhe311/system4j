package com.dw.grid;

import java.util.*;

import com.dw.grid.*;

/**
 * ����֧������γ�ȼ����֧��
 * 
 * ÿ������Ӧ�ö���һ�����ֻ���
 * 
 * ÿһ������Ϊ555.6x555.6�׵ķ���ÿһ��������9��185.2x185.2�׵�С������ɡ��ı����ڿ����Զ����������ģʽ��������̶���������������·���´�Ͻ����������Ͻ��ͼ����
 * ���磬�Զ������飨31��14��30��N��121��29��42��E��Ϊ��׼�㣬���վ�γ�ȷ���������
 * 
 * -���Ϻ��������顣���Ϻ��е����񻮷־�ȷ����
 * 
 * �����Ϻ��ڲ���ĳ����λ��Ӧ��ϵͳ������Ҫ�������ϵĻ��֡����һ���Ҫȷ���Լ���������롣 �ڴ������У���������Ҳ���ABCD...,����(26���Ʊ���)
 * ������ϵ��²���1,2,3...���� �磻A1 B5 AA1 BA1 �������еĵ�С���񣬲���1-9���ֱ��� �� A1-1 A1-9 BA1-3
 * 
 * @author Jason Zhu
 */
public class GridDef
{
	static final int MAX_GRID_VAL = 26 * 26;

	/**
	 * һ�����ӵĶ���,�ڶ����е�����������׼���һ�����Ӷ��� �� A1 A1-2
	 * 
	 * @author Jason Zhu
	 */
	public static class GridOne
	{
		int x = 0;// �����������-������

		int y = 0;// ������������-���ϵ���

		int sm_1_9 = 0;// С���壬���<=0��ʾû�ж���Ĵ��1-9��ʾС����

		public GridOne()
		{
		}

		public GridOne(int x, int y, int sm19)
		{
			this.x = x;
			this.y = y;

			if (sm19 > 9)
				throw new IllegalArgumentException(
						"small grid num cannot bigger than 9");

			this.sm_1_9 = sm19;
		}

		/**
		 * ��������ĸ�ʽ����
		 * 
		 * @param gstr
		 */
		public GridOne(String gstr)
		{
			if (gstr == null)
				throw new IllegalArgumentException("str cannot be null");

			gstr = gstr.trim();
			if ("".equals(gstr))
				throw new IllegalArgumentException("str cannot be empty");

			gstr = gstr.toUpperCase();
			int bsm = gstr.indexOf('-');
			if (bsm > 0)
			{// ����С��
				sm_1_9 = Integer.parseInt(gstr.substring(bsm + 1).trim());
				if (sm_1_9 > 9)
					throw new IllegalArgumentException(
							"small grid num cannot bigger than 9");

				gstr = gstr.substring(0, bsm).trim();
			}

			int len = gstr.length();
			if (len < 2)
				throw new IllegalArgumentException(
						"Grid str must like A1 or A1-2");
			// ������

			String xstr = null;
			for (int p = 0; p < len; p++)
			{
				char c = gstr.charAt(p);
				if ('A' <= c && c <= 'Z')
				{
					continue;
				}
				else if ('0' <= c && c <= '9')
				{
					if (p <= 0)
						throw new IllegalArgumentException(
								"Grid str must like A1 or A1-2");
					xstr = gstr.substring(0, p);
					this.y = Integer.parseInt(gstr.substring(p));
					break;
				}
			}

			if (xstr == null)
				throw new IllegalArgumentException(
						"Grid str must like A1 or A1-2");
			// ����x
			int base = 1;
			for (int i = 0; i < xstr.length(); i++)
			{
				int lc = xstr.charAt(xstr.length() - i - 1);

				x += base * (lc - 'A' + 1);
				base *= 26;
			}
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public GridOne getParent()
		{
			if (sm_1_9 <= 0)
				return null;

			return new GridOne(x, y, 0);
		}

		public int getSmallOne2Nine()
		{
			return sm_1_9;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof GridOne))
				return false;

			GridOne ogo = (GridOne) o;

			if (this.x != ogo.x)
				return false;

			if (this.y != ogo.y)
				return false;

			if (this.sm_1_9 != ogo.sm_1_9)
				return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			return ("" + x + "_" + y + "_" + sm_1_9).hashCode();
		}

		public String toString()
		{
			if (x <= 0 || y <= 0)
				return "";

			String tmps = "";
			int tmpx = x;
			do
			{
				int ha = tmpx / 26;
				int la = tmpx % 26;
				tmps = (char) ('A' + la - 1) + tmps;
				tmpx = ha;
			}
			while (tmpx > 0);

			if (sm_1_9 <= 0)
				return tmps + y;
			else
				return tmps + y + '-' + sm_1_9;
		}
	}

	
	public static ArrayList<String> calSmallGridCodes(String sgc)
	{
		if(sgc.indexOf('-')>0)
		{
			ArrayList<String> rets = new ArrayList<String>(1) ;
			rets.add(sgc) ;
			return rets;
		}
		
		ArrayList<String> rets = new ArrayList<String>(9) ;
		for(int i = 1 ; i <=9 ; i ++)
			rets.add(sgc+"-"+i) ;
		
		return rets ;
	}
	
	/**
	 * ����С�����񳤶�185.2�ף�ά��������ʮ��֮һ�֣�Ҳ����6�롣60��ʮ��֮һ�� ���ھ�γ�ȿ��Ա�ʾ��ʮ��֮һ��������������ø��������м���
	 */
	public static final int GRID_D_LAT_TENTH = 60;
	
	/**
	 * ����С�����񳤶�185.2�ף�ά��������ʮ��֮һ�֣�Ҳ����7�롣70��ʮ��֮һ�� ���ھ�γ�ȿ��Ա�ʾ��ʮ��֮һ��������������ø��������м���
	 */
	public static final int GRID_D_LNG_TENTH = 70;

	/**
	 * �������Ψһ����
	 */
	private String name = null;

	/**
	 * �����������
	 */
	private String title = null;

	/**
	 * ���񻮷ֵĻ�׼λ��
	 */
	private GpsPos baseGpsPos = null;

	/**
	 * �������������ʼλ��
	 */
	private GpsPos areaBaseGpsPos = null;

	/**
	 * ����ڻ�׼����Ķ��������������һ���򶫸���Ϊ1������Ϊ-1
	 */
	private int areaBaseX = 0;

	/**
	 * ����ڻ�׼�����ϱ������������һ����Ϊ1������Ϊ-1
	 */
	private int areaBaseY = 0;

	/**
	 * ����һ��������-����һ����׼���ֵĻ�����һ�����㣬��ʵ���ӵĶ���
	 * 
	 * @param base_gp
	 *            ��׼gps
	 * @param area_gp
	 *            ���򶥵�gps
	 */
	GridDef(String name, String title, GpsPos base_gp, GpsPos area_base_gp)
	{
		this.name = name;
		this.title = title;
		baseGpsPos = base_gp;
		areaBaseGpsPos = area_base_gp;

		// ����areaBaseGpsPos����������׼�����������������꣬�������Ա�׼�ĵѿ�������ʵ��
		int[] xy = calRelatedXY(areaBaseGpsPos) ;
		this.areaBaseX = xy[0] ;
		this.areaBaseY = xy[1] ;
	}

	/**
	 * ����һ��λ���ȶ��ڻ�׼��������������
	 * 
	 * @param gp
	 * @return
	 */
	private int[] calRelatedXY(GpsPos gp)
	{
		int lng_ts = gp.getLongitudeAsTenthSecond()
				- baseGpsPos.getLongitudeAsTenthSecond();

		int x, y;
		if (lng_ts >= 0)
		{
			// ��Ϊ�����Ͻǣ�����Ӧ�þ����ܵ�С-���ƶ���ߴ�
			x = lng_ts / GRID_D_LNG_TENTH;
			// + ((lng_ts % GRID_D_TENTH) > 0 ? 1 : 0);
		}
		else
		{
			lng_ts = -lng_ts;
			// ��Ϊ�����Ͻǣ�����Ӧ�þ����ܵ�С-Ҫ�Ѷ���ĳߴ�Ҳ��¼
			x = -(lng_ts / GRID_D_LNG_TENTH + ((lng_ts % GRID_D_LNG_TENTH) > 0 ? 1 : 0));
		}

		int lat_ts = gp.getLatitudeAsTenthSecond()
				- baseGpsPos.getLatitudeAsTenthSecond();

		if (lat_ts >= 0)
		{
			// ��Ϊ�����Ͻǣ�����Ӧ�þ����ܵĴ�-�ƶ���ߴ�
			y = lat_ts / GRID_D_LAT_TENTH + ((lat_ts % GRID_D_LAT_TENTH) > 0 ? 1 : 0);
		}
		else
		{
			lat_ts = -lat_ts;
			// ��Ϊ�����Ͻǣ�����Ӧ�þ����ܵĴ�-���ƶ���ߴ�
			y = -lat_ts / GRID_D_LAT_TENTH;
			// + ((lat_ts % GRID_D_TENTH) > 0 ? 1 : 0));
		}
		return new int[] { x, y };
	}

	/**
	 * ��ö�������
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * ��ö������
	 * 
	 * @return
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * ������񻮷ֻ�׼��λ��
	 * 
	 * @return
	 */
	public GpsPos getBaseGpsPos()
	{
		return this.baseGpsPos;
	}

	/**
	 * ��������׼��λ��
	 * 
	 * @return
	 */
	public GpsPos getAreaBaseGpsPos()
	{
		return this.areaBaseGpsPos;
	}

	/**
	 * ��������ľ�γ�ȣ���������С��ı��룬���� A1-3
	 * 
	 * @param gp
	 * @return
	 */
	public GridOne calGridOneByGpsPos(GpsPos gp)
	{
		// �ȼ��㾭γ����������� ��׼��ĸ���xy
		int[] xy = calRelatedXY(gp) ;
//		int lng_ts = gp.getLongitudeAsTenthSecond()
//				- baseGpsPos.getLongitudeAsTenthSecond();
//		int x;
//		if (lng_ts >= 0)
//		{
//			x = lng_ts / GRID_D_TENTH + ((lng_ts % GRID_D_TENTH) > 0 ? 1 : 0);
//		}
//		else
//		{
//			lng_ts = -lng_ts;
//			x = -(lng_ts / GRID_D_TENTH + ((lng_ts % GRID_D_TENTH) > 0 ? 1 : 0));
//		}
//
//		int lat_ts = gp.getLatitudeAsTenthSecond()
//				- baseGpsPos.getLatitudeAsTenthSecond();
//		int y;
//		if (lat_ts >= 0)
//		{
//			y = lat_ts / GRID_D_TENTH + ((lat_ts % GRID_D_TENTH) > 0 ? 1 : 0);
//		}
//		else
//		{
//			lat_ts = -lat_ts;
//			y = -(lat_ts / GRID_D_TENTH + ((lat_ts % GRID_D_TENTH) > 0 ? 1 : 0));
//		}

		// ������� areaBaseX �� areaBaseY ���м������λ��

		int xd = xy[0] - areaBaseX ;
		int yd = areaBaseY - xy[1] ; // ��Ϊ�������¿���ֵ����

		if (xd < 0 || yd < 0)
			return null;
		// ���������

		return new GridOne(xd / 3 + 1, yd / 3 + 1, (xd%3)+(yd%3)*3+1);
	}

	/**
	 * ���ݾ�γ��doubleֵ�������Ӧ��GridOne
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public GridOne calGridOneByPos(double lat, double lng)
	{
		GpsPos latgp = new GpsPos(lng, lat);
		return calGridOneByGpsPos(latgp);
	}

	/**
	 * ����һ�������壬�����������ĸ��߽��Gpsλ��
	 * 
	 * @param grido
	 * @return ���������Ͻ�Ϊ��㣬˳ʱ�뷽����ĸ�Gpsλ������
	 */
	public GpsPos[] calBoundGpsPosByGridOne(GridOne grido)
	{
		// �������С��������ͬ

		// ��λ���ϽǶ���
		// ��񶥵�
		int bx = (grido.x - 1) * 3;
		int by = (grido.y - 1) * 3;

		int b_lng_gp = baseGpsPos.getLongitudeAsTenthSecond();
		int b_lat_gp = baseGpsPos.getLatitudeAsTenthSecond();

		if (grido.sm_1_9 <= 0)
		{// ���
		// ��������ڻ�׼���x��y
			int x = bx + areaBaseX;
			int y = areaBaseY - by;

			return new GpsPos[] {
					new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * x, b_lat_gp
							+ GRID_D_LAT_TENTH * y),
					new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * (x + 3), b_lat_gp
							+ GRID_D_LAT_TENTH * y),
					new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * (x + 3), b_lat_gp
							+ GRID_D_LAT_TENTH * (y - 3)),
					new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * (x), b_lat_gp
							+ GRID_D_LAT_TENTH * (y - 3)) };
		}

		// ��Ҫ��λС�񶥵�
		int x = bx + areaBaseX + (grido.sm_1_9 - 1) % 3;
		int y = areaBaseY - by - (grido.sm_1_9 - 1) / 3;
		return new GpsPos[] {
				new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * x, b_lat_gp + GRID_D_LAT_TENTH
						* y),
				new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * (x + 1), b_lat_gp
						+ GRID_D_LAT_TENTH * y),
				new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * (x + 1), b_lat_gp
						+ GRID_D_LAT_TENTH * (y - 1)),
				new GpsPos(b_lng_gp + GRID_D_LNG_TENTH * (x), b_lat_gp
						+ GRID_D_LAT_TENTH * (y - 1)) };
	}
}
