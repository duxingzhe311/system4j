package com.dw.grid;

import java.util.*;

import com.dw.grid.*;

/**
 * 用来支持网格经纬度计算的支持
 * 
 * 每个网格应用都有一个划分基点
 * 
 * 每一大网格为555.6x555.6米的方格，每一大网格由9个185.2x185.2米的小网格组成。改变现在可以自动生成网格的模式，将网格固定。覆盖整个兰州路海事处辖区（见附件辖区图）。
 * 例如，以东方明珠（31°14′30″N，121°29′42″E）为基准点，按照经纬度方向建立网格。
 * 
 * -如上海东方明珠。在上海中的网格划分就确定了
 * 
 * 但在上海内部的某个单位的应用系统，不仅要符合以上的划分。而且还需要确定自己的网格编码。 在大网格中，横向从左到右采用ABCD...,编码(26进制编码)
 * 纵向从上到下采用1,2,3...编码 如；A1 B5 AA1 BA1 大网格中的的小网格，采用1-9数字编码 如 A1-1 A1-9 BA1-3
 * 
 * @author Jason Zhu
 */
public class GridDef
{
	static final int MAX_GRID_VAL = 26 * 26;

	/**
	 * 一个格子的定义,在定义中的相对于区域基准点的一个格子定义 如 A1 A1-2
	 * 
	 * @author Jason Zhu
	 */
	public static class GridOne
	{
		int x = 0;// 大格横向格子数-从左到右

		int y = 0;// 大格纵向格子数-从上到下

		int sm_1_9 = 0;// 小格定义，如果<=0表示没有定义的大格，1-9表示小格定义

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
		 * 根据输入的格式构造
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
			{// 包含小格
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
			// 计算大格

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
			// 计算x
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
	 * 由于小格网格长度185.2米，维度上它是十分之一分，也就是6秒。60的十分之一秒 由于经纬度可以表示成十分之一秒的整数。可以用该整数进行计算
	 */
	public static final int GRID_D_LAT_TENTH = 60;
	
	/**
	 * 由于小格网格长度185.2米，维度上它是十分之一分，也就是7秒。70的十分之一秒 由于经纬度可以表示成十分之一秒的整数。可以用该整数进行计算
	 */
	public static final int GRID_D_LNG_TENTH = 70;

	/**
	 * 网格定义的唯一名称
	 */
	private String name = null;

	/**
	 * 网格定义的描述
	 */
	private String title = null;

	/**
	 * 网格划分的基准位置
	 */
	private GpsPos baseGpsPos = null;

	/**
	 * 网格定义的区域起始位置
	 */
	private GpsPos areaBaseGpsPos = null;

	/**
	 * 相对于基准坐标的东西向格子数，第一个向东格子为1，向西为-1
	 */
	private int areaBaseX = 0;

	/**
	 * 相对于基准坐标南北向格子数，第一个向北为1，向南为-1
	 */
	private int areaBaseY = 0;

	/**
	 * 构造一个网格定义-根据一个基准划分的基础和一个顶点，其实格子的定义
	 * 
	 * @param base_gp
	 *            基准gps
	 * @param area_gp
	 *            区域顶点gps
	 */
	GridDef(String name, String title, GpsPos base_gp, GpsPos area_base_gp)
	{
		this.name = name;
		this.title = title;
		baseGpsPos = base_gp;
		areaBaseGpsPos = area_base_gp;

		// 计算areaBaseGpsPos相对于网格基准点的整数格子相对坐标，该坐标以标准的笛卡尔坐标实现
		int[] xy = calRelatedXY(areaBaseGpsPos) ;
		this.areaBaseX = xy[0] ;
		this.areaBaseY = xy[1] ;
	}

	/**
	 * 计算一个位置先对于基准点的网格相对坐标
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
			// 因为是左上角，所以应该尽可能的小-不计多余尺寸
			x = lng_ts / GRID_D_LNG_TENTH;
			// + ((lng_ts % GRID_D_TENTH) > 0 ? 1 : 0);
		}
		else
		{
			lng_ts = -lng_ts;
			// 因为是左上角，所以应该尽可能的小-要把多余的尺寸也记录
			x = -(lng_ts / GRID_D_LNG_TENTH + ((lng_ts % GRID_D_LNG_TENTH) > 0 ? 1 : 0));
		}

		int lat_ts = gp.getLatitudeAsTenthSecond()
				- baseGpsPos.getLatitudeAsTenthSecond();

		if (lat_ts >= 0)
		{
			// 因为是左上角，所以应该尽可能的大-计多余尺寸
			y = lat_ts / GRID_D_LAT_TENTH + ((lat_ts % GRID_D_LAT_TENTH) > 0 ? 1 : 0);
		}
		else
		{
			lat_ts = -lat_ts;
			// 因为是左上角，所以应该尽可能的大-不计多余尺寸
			y = -lat_ts / GRID_D_LAT_TENTH;
			// + ((lat_ts % GRID_D_TENTH) > 0 ? 1 : 0));
		}
		return new int[] { x, y };
	}

	/**
	 * 获得定义名词
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * 获得定义标题
	 * 
	 * @return
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * 获得网格划分基准点位置
	 * 
	 * @return
	 */
	public GpsPos getBaseGpsPos()
	{
		return this.baseGpsPos;
	}

	/**
	 * 获得区域基准点位置
	 * 
	 * @return
	 */
	public GpsPos getAreaBaseGpsPos()
	{
		return this.areaBaseGpsPos;
	}

	/**
	 * 根据输入的经纬度，计算网格小格的编码，例如 A1-3
	 * 
	 * @param gp
	 * @return
	 */
	public GridOne calGridOneByGpsPos(GpsPos gp)
	{
		// 先计算经纬度坐标相对于 基准点的格子xy
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

		// 再相对于 areaBaseX 和 areaBaseY 进行计算相对位置

		int xd = xy[0] - areaBaseX ;
		int yd = areaBaseY - xy[1] ; // 因为从上往下看数值增大

		if (xd < 0 || yd < 0)
			return null;
		// 计算大网格

		return new GridOne(xd / 3 + 1, yd / 3 + 1, (xd%3)+(yd%3)*3+1);
	}

	/**
	 * 根据经纬度double值，计算对应的GridOne
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
	 * 根据一个网格定义，计算该网格的四个边界点Gps位置
	 * 
	 * @param grido
	 * @return 以网格左上角为起点，顺时针方向的四个Gps位置数组
	 */
	public GpsPos[] calBoundGpsPosByGridOne(GridOne grido)
	{
		// 大网格和小网格处理不相同

		// 定位左上角顶点
		// 大格顶点
		int bx = (grido.x - 1) * 3;
		int by = (grido.y - 1) * 3;

		int b_lng_gp = baseGpsPos.getLongitudeAsTenthSecond();
		int b_lat_gp = baseGpsPos.getLatitudeAsTenthSecond();

		if (grido.sm_1_9 <= 0)
		{// 大格
		// 计算相对于基准点的x，y
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

		// 需要定位小格顶点
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
