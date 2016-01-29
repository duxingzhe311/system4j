package com.dw.grid;

import java.util.*;

import com.dw.system.Convert;
import com.dw.system.xmldata.*;

public class GpsPos implements IXmlDataable
{
	static double RC = 6378137; // ����뾶

	static double RJ = 6356725; // ���뾶

	/**
	 * ���ȶ����� true��ʾ���� ��ʾ���� ��֮������
	 */
	boolean b_longitude_positive = true;

	int longitude_degrees = -1;

	int longitude_minutes = -1;

	double longitude_seconds = -1;

	/**
	 * ���ȵ�ʮ��֮һ��������ʾ
	 */
	//int tenth_longitude = -1;

	/**
	 * ά���ϱ��� true��ʾ���� ��ʾ��γ�� ��֮����γ
	 */
	boolean b_latitude_positive = true;

	int latitude_degrees = -1;

	int latitude_minutes = -1;

	double latitude_seconds = -1;

	/**
	 * ά�ȵ�ʮ��֮һ���ʾ
	 */
	//int tenth_latitude = -1;//��3m���Ȳ�ʹ����

	// / <summary>
	// / ���θ߶�
	// / </summary>
	float seaLevel = -1;

	// / <summary>
	// / �ٶ�
	// / </summary>
	float speed = -1;

	// / <summary>
	// / ��������ʱ��
	// / </summary>
	Date gmtDateTime = null;

	Date findDate = new Date();

	int satellitesInSolution = -1;

	int satellitesInView = -1;

	int satelliteCount = -1;

	public GpsPos()
	{
	}

	public GpsPos(boolean long_positive, int long_d, int long_m, double long_s,
			boolean lat_positive, int lat_d, int lat_m, double lat_s)
	{
		init(long_positive, long_d, long_m, long_s, lat_positive, lat_d, lat_m,
				lat_s);
	}

	private void init(boolean long_positive, int long_d, int long_m,
			double long_s, boolean lat_positive, int lat_d, int lat_m,
			double lat_s)
	{
		b_longitude_positive = long_positive;

		longitude_degrees = long_d;
		longitude_minutes = long_m;
		longitude_seconds = long_s;

		// /////////
		b_latitude_positive = lat_positive;

		latitude_degrees = lat_d;
		latitude_minutes = lat_m;
		latitude_seconds = lat_s;

		//calTenthSecond();
	}

	/**
	 * ����AIS��������ݶ����ö�Ӧ�ľ�γ�� Longitude is given in in 1/10000 min. Values up to
	 * plus or minus 180 degrees, East = positive, West = negative. A value of
	 * 181 degrees (0x6791AC0 hex) indicates that longitude is not available and
	 * is the default.
	 * 
	 * 
	 * Latitude is given in in 1/10000 min. Values up to plus or minus 90
	 * degrees, North = positive, South = negative. A value of 91 degrees
	 * (0x3412140 hex) indicates latitude is not available and is the default.
	 * 
	 * @param lng
	 * @param lat
	 * @return
	 */
	public static GpsPos createByAIS(int lng, int lat)
	{
		if (Math.abs(lng) >= 108600000 || Math.abs(lat) >= 54600000)
			return null;

		return new GpsPos(((double) lng) / 600000, ((double) lat) / 600000);
	}

	public static GpsPos createByGPRMC(String str)
	{
		// ,204700,A,3403.868,N,11709.432,W,001.9,336.9,170698,013.6,E*6E
		if (str.startsWith("$GPRMC"))
			str = str.substring(6);

		List<String> ss = Convert.splitStrWith(str, ",");
		if (ss.size() < 6)
			return null;

		if (!"A".equals(ss.get(1)))
			return null;

		boolean lat_positive = true;
		if ("N".equals(ss.get(3)))
		{
			lat_positive = true;
		}
		else if ("S".equals(ss.get(3)))
		{
			lat_positive = false;
		}
		else
			return null;

		String nstr = ss.get(2);
		int p = nstr.indexOf('.');
		if (p <= 3)
			return null;

		int lat_d = Integer.parseInt(nstr.substring(0, p - 2));
		double lat_ms = Double.parseDouble(nstr.substring(p - 2));// ����
		int lat_m = (int) (lat_ms);
		double lat_s = (lat_ms - lat_m) * 60;

		boolean lng_positive = true;
		if ("E".equals(ss.get(5)))
		{
			lng_positive = true;
		}
		else if ("W".equals(ss.get(5)))
		{
			lng_positive = false;
		}
		else
			return null;

		nstr = ss.get(4);
		p = nstr.indexOf('.');
		if (p <= 3)
			return null;

		int lng_d = Integer.parseInt(nstr.substring(0, p - 2));
		double lng_ms = Double.parseDouble(nstr.substring(p - 2));// ����
		int lng_m = (int) (lng_ms);
		double lng_s = (lng_ms - lng_m) * 60;

		return new GpsPos(lng_positive, lng_d, lng_m, lng_s, lat_positive,
				lat_d, lat_m, lat_s);
	}

	/**
	 * ת����������ʮ��֮һ��ֵ֮���ת��
	 * 
	 * @param d
	 * @return
	 */
	
//	private void calTenthSecond()
//	{
//		// ����10��֮һ��
//		tenth_longitude = longitude_degrees * 36000 + longitude_minutes * 600
//				+ (int) (longitude_seconds * 10);
//
//		if (!b_longitude_positive)
//			tenth_longitude = -tenth_longitude;
//
//		tenth_latitude = latitude_degrees * 36000 + latitude_minutes * 600
//				+ (int) (latitude_seconds * 10);
//
//		if (!b_latitude_positive)
//			tenth_latitude = -tenth_latitude;
//	}

	/**
	 * ����ʮ��֮һ���������γ��ֵ���������
	 * 
	 * @param long_tenth_seconds
	 * @param lat_tenth_seconds
	 */
	public GpsPos(int long_tenth_seconds, int lat_tenth_seconds)
	{
		//tenth_longitude = long_tenth_seconds;
		// ���㾭γ��
		b_longitude_positive = long_tenth_seconds >= 0;
		int ll = Math.abs(long_tenth_seconds);

		longitude_degrees = ll / 36000;
		ll %= 36000;
		longitude_minutes = ll / 600;
		ll %= 600;
		longitude_seconds = ((double) ll) / 10;

		//tenth_latitude = lat_tenth_seconds;

		this.b_latitude_positive = lat_tenth_seconds >= 0;
		ll = Math.abs(lat_tenth_seconds);

		latitude_degrees = ll / 36000;
		ll %= 36000;
		latitude_minutes = ll / 600;
		ll %= 600;
		latitude_seconds = ((double) ll) / 10;
	}

	/**
	 * ͨ��������ֵ����λ�ö���
	 * 
	 * @param long_d
	 * @param lat_d
	 */
	public GpsPos(double long_double, double lat_double)
	{
		double tmpd = long_double > 0 ? long_double : -long_double;
		int lng_d = (int) (tmpd);
		double tmplng = (tmpd - lng_d) * 60;
		int lng_m = (int) tmplng;
		tmplng -= lng_m;
		tmplng *= 60;

		tmpd = lat_double > 0 ? lat_double : -lat_double;
		int lat_d = (int) (tmpd);
		double tmplat = (tmpd - lat_d) * 60;
		int lat_m = (int) tmplat;
		tmplat -= lat_m;
		tmplat *= 60;

		init(long_double >= 0, lng_d, lng_m, tmplng, lat_double >= 0, lat_d,
				lat_m, tmplat);
	}

	public boolean isLongitudePositive()
	{
		return b_longitude_positive;
	}

	public int getLongitudeDegrees()
	{
		return longitude_degrees;
	}

	public int getLongitudeMinutes()
	{
		return longitude_minutes;
	}

	public double getLongitudeSeconds()
	{
		return longitude_seconds;
	}

	public double getLongitudeAsDouble()
	{
		double d = longitude_degrees + ((double) longitude_minutes) / 60
				+ longitude_seconds / 3600;
		if (!this.b_longitude_positive)
			return -d;
		return d;
	}

	/**
	 * ���ʮ��֮һ���������ʾ
	 * 
	 * @return
	 */
	public int getLongitudeAsTenthSecond()
	{
		//return tenth_longitude;
		
		int tenth_longitude = longitude_degrees * 36000 + longitude_minutes * 600
		+ (int) (longitude_seconds * 10);

		if (!b_longitude_positive)
			tenth_longitude = -tenth_longitude;
		return tenth_longitude;
	}

	public String getLongitudeStr()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getLongitudeDegrees()).append('d').append(
				this.getLongitudeMinutes()).append('m').append(
				this.getLongitudeSeconds()).append('s');
		if (b_longitude_positive)
			sb.append(" E");
		else
			sb.append(" W");
		return sb.toString();
	}

	public boolean isLatitudePositive()
	{
		return b_latitude_positive;
	}

	public int getLatitudeDegrees()
	{
		return latitude_degrees;
	}

	public int getLatitudeMinutes()
	{
		return latitude_minutes;
	}

	public double getLatitudeSeconds()
	{
		return latitude_seconds;
	}

	public double getLatitudeAsDouble()
	{
		double d = latitude_degrees + ((double) latitude_minutes) / 60
				+ latitude_seconds / 3600;
		if (!this.b_latitude_positive)
			return -d;
		return d;
	}

	/**
	 * ���ʮ��֮һ���������ʾ
	 * 
	 * @return
	 */
	public int getLatitudeAsTenthSecond()
	{
		//return tenth_latitude;
		int tenth_latitude = latitude_degrees * 36000 + latitude_minutes * 600
		+ (int) (latitude_seconds * 10);

		if (!b_latitude_positive)
			tenth_latitude = -tenth_latitude;
		
		return tenth_latitude;
	}

	public String getLatitudeStr()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getLatitudeDegrees()).append('d').append(
				this.getLatitudeMinutes()).append('m').append(
				this.getLatitudeSeconds()).append('s');
		if (b_latitude_positive)
			sb.append(" N");
		else
			sb.append(" S");
		return sb.toString();
	}

	public float getSeaLevel()
	{
		return seaLevel;
	}

	public float getSpeed()
	{
		return speed;
	}

	public Date getGmtDateTime()
	{
		return gmtDateTime;
	}

	public String getGmtToLocalTimeStr()
	{
		if (gmtDateTime == null)
			return "";

		return Convert.toFullYMDHMS(gmtDateTime);
	}

	public String toGooglePosStr()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(latitude_degrees).append(' ');
		double dd = this.latitude_minutes + this.latitude_seconds / 60;
		sb.append(Convert.toDecimalDigitsStr(dd, 3, true)).append('\'');
		if (b_latitude_positive)
			sb.append("N");
		else
			sb.append("S");

		sb.append(',').append(longitude_degrees).append(' ');
		dd = this.longitude_minutes + this.longitude_seconds / 60;
		sb.append(Convert.toDecimalDigitsStr(dd, 3, true)).append('\'');
		if (b_longitude_positive)
			sb.append("E");
		else
			sb.append("W");

		return sb.toString();
	}

	public String toDegreeMinSecStr()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(latitude_degrees).append('��');
		sb.append(latitude_minutes).append('\'');
		sb.append(this.latitude_seconds).append('\"');
		if (b_latitude_positive)
			sb.append("N");
		else
			sb.append("S");

		sb.append(longitude_degrees).append('��');
		sb.append(longitude_minutes).append('\'');
		sb.append(this.longitude_seconds).append('\"');
		if (b_longitude_positive)
			sb.append("E");
		else
			sb.append("W");

		return sb.toString();
	}

	public String toDescString()
	{
		StringBuilder sb = new StringBuilder();
		if (b_longitude_positive)
			sb.append("����:");
		else
			sb.append("����:");
		sb.append(this.getLongitudeDegrees()).append("��").append(
				this.getLongitudeMinutes()).append('\'').append(
				this.getLongitudeSeconds()).append("\"\n");

		if (b_latitude_positive)
			sb.append("��γ:");
		else
			sb.append("��γ:");
		sb.append(this.getLatitudeDegrees()).append("��").append(
				this.getLatitudeMinutes()).append('\'').append(
				this.getLatitudeSeconds()).append("\"\n");

		sb.append("�߶�:").append(this.getSeaLevel()).append("\n");

		sb.append("�ٶ�:").append(this.getSpeed()).append("\n");

		sb.append("ʱ��:").append(this.getGmtToLocalTimeStr()).append("\n");

		sb.append("ʹ��������:").append(this.satellitesInSolution).append("\n");

		sb.append("�ɼ�������:").append(this.satellitesInView).append("\n");

		sb.append("������:").append(this.satelliteCount).append("\n");

		return sb.toString();
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();

		xd.setParamValue("long_positive", b_longitude_positive);
		xd.setParamValue("long_d", longitude_degrees);
		xd.setParamValue("long_m", longitude_minutes);
		xd.setParamValue("long_s", longitude_seconds);

		xd.setParamValue("lat_positive", b_latitude_positive);
		xd.setParamValue("lat_d", latitude_degrees);
		xd.setParamValue("lat_m", latitude_minutes);
		xd.setParamValue("lat_s", latitude_seconds);

		xd.setParamValue("sea_level", seaLevel);

		xd.setParamValue("speed", speed);

		if (gmtDateTime != null)
			xd.setParamValue("gmt_dt", gmtDateTime);

		xd.setParamValue("find_date", findDate);

		xd.setParamValue("sat_ins", satellitesInSolution);
		xd.setParamValue("sat_inv", satellitesInView);
		xd.setParamValue("sat_c", satelliteCount);

		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		b_longitude_positive = xd.getParamValueBool("long_positive", true);

		longitude_degrees = xd.getParamValueInt32("long_d", -1);
		longitude_minutes = xd.getParamValueInt32("long_m", -1);
		longitude_seconds = xd.getParamValueDouble("long_s", -1);

		b_latitude_positive = xd.getParamValueBool("lat_positive", true);

		latitude_degrees = xd.getParamValueInt32("lat_d", -1);
		latitude_minutes = xd.getParamValueInt32("lat_m", -1);
		latitude_seconds = xd.getParamValueDouble("lat_s", -1);

		seaLevel = xd.getParamValueFloat("sea_level", -1);

		speed = xd.getParamValueFloat("speed", -1);

		gmtDateTime = xd.getParamValueDate("gmt_dt", null);

		findDate = xd.getParamValueDate("find_date", null);

		satellitesInSolution = xd.getParamValueInt32("sat_ins", -1);
		satellitesInView = xd.getParamValueInt32("sat_inv", -1);
		satelliteCount = xd.getParamValueInt32("sat_c", -1);

		// �����Ա�֤����
		//calTenthSecond();
	}

	/**
	 * ����Ϊ��λ������������γ��֮��ľ���
	 * 
	 * @param ogp
	 * @return
	 */
	public double calDistanceMeter(GpsPos ogp)
	{
		return distanceByLnglat(
				this.getLongitudeAsDouble(),this.getLatitudeAsDouble(), 
				ogp.getLongitudeAsDouble(),ogp.getLatitudeAsDouble());
	}

	/**
	 * �Ժ��Ӣ�Ϊ��λ������������γ��֮��ľ���
	 * @param ogp
	 * @return
	 */
	public double calDistanceMile(GpsPos ogp)
	{//�й��ĺ���涨
		double d = calDistanceMeter(ogp) ;
		return d/1852 ;
	}
	/**
	 * ����������γ�ȵ�������
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static double distanceByLnglat(
			double lng1, double lat1,
			double lng2, double lat2)
	{
		double radLat1 = lat1 * Math.PI / 180;
		double radLat2 = lat2 * Math.PI / 180;
		double a = radLat1 - radLat2;
		double b = lng1 * Math.PI / 180 - lng2 * Math.PI / 180;
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * RC;// ȡWGS84��׼�ο������еĵ��򳤰뾶(��λ:m)
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	/**
	 * �ӱ��㵽������֮���0-360�ȷ�λ�����û�з�λ���򷵻�511
	 * 
	 * @return
	 */
	public double calDirection360(GpsPos ogp)
	{
		return calDirectionByLnglat(
				this.getLongitudeAsDouble(),this.getLatitudeAsDouble(), 
				ogp.getLongitudeAsDouble(),ogp.getLatitudeAsDouble());
	}

	/**
	 * �ӱ��㵽������֮���0-2PI�ȷ�λ�����û�з�λ
	 * 
	 * @return
	 */
	public double calDirectionPI(GpsPos ogp)
	{
		double d = calDirection360(ogp) ;
		return d *Math.PI / 180.;
	}

	
	private static double calDirectionByLnglat(
			double lng1, double lat1,
			double lng2, double lat2)
	{
		double rad_lng1 = lng1 * Math.PI / 180.;
		double rad_lat1 = lat1 * Math.PI / 180.;

		double rad_lng2 = lng2 * Math.PI / 180.;
		double rad_lat2 = lat2 * Math.PI / 180.;

		double ec1 = RJ + (RC - RJ) * (90. - rad_lat1) / 90.;
		double ed1 = ec1 * Math.cos(rad_lng1);

		double ec2 = RJ + (RC - RJ) * (90. - rad_lat2) / 90.;
		double ed2 = ec2 * Math.cos(rad_lng2);

		double dx = (rad_lng2 - rad_lng1) * ed1;
		double dy = (rad_lat2 - rad_lat1) * ec1;
		// double out = Math.sqrt(dx * dx + dy * dy);
		double angle = angle = Math.atan(Math.abs(dx / dy)) * 180. / Math.PI;
		// �ж�����
		double dLo = rad_lng2 - rad_lng1;
		double dLa = rad_lat2 - rad_lat1;

		if (dLo > 0 && dLa <= 0)
		{
			angle = (90. - angle) + 90.;
		}
		else if (dLo <= 0 && dLa < 0)
		{
			angle = angle + 180.;
		}
		else if (dLo < 0 && dLa >= 0)
		{
			angle = (90. - angle) + 270;
		}
		return angle;
	}
	
	/**
	 * ��ָ����ʱ���ڣ����㵽��һ����γ�ȵı�׼m/s�ٶ�
	 * @param ogp
	 * @param dur_sec
	 * @return
	 */
	public double calSpeedStd(GpsPos ogp,double dur_sec)
	{
		double d =  calDistanceMeter(ogp);
		return d/dur_sec ;
	}
	
	/**
	 * �Խ�Ϊ��λ���㴬��,��λ�Ǻ���ÿСʱ��Ҳ����1852m��λÿСʱ
	 * @param ogp
	 * @param dur_sec
	 * @return
	 */
	public double calSpeedMile(GpsPos ogp,double dur_sec)
	{//1��=1����/Сʱ
		double d =  calDistanceMile(ogp) ;
		return d/dur_sec*3600 ;
	}
}
