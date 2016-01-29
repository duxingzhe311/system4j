package com.dw.system.gdb.datax;

public class DataXFileVer implements Comparable
    {
        public static DataXFileVer createFirstAutoVer()
        {
            DataXFileVer dv = new DataXFileVer();
            dv.autoSeg = new int[1];
            dv.autoSeg[0] = 1;
            return dv;
        }


        public static String TransToShowStr(String ss)
        {
            if (ss==null||ss.equals(""))
                return "";

            if (ss.startsWith("000"))
                return ss.substring(3);
            else if (ss.startsWith("00"))
                return ss.substring(2);
            else if (ss.startsWith("0"))
                return ss.substring(1);

            return ss;
        }


        int userSeg = 0;
        int[] autoSeg = null;

        private DataXFileVer()
        {
        }

        public DataXFileVer(String strver)
        {
            if (strver==null||strver.equals(""))
                return;

            String[] ss = strver.split(".");
            if (ss.length <= 0)
                return;

            userSeg = Integer.parseInt(ss[0]);
            autoSeg = new int[ss.length - 1];
            for (int i = 0; i < autoSeg.length; i++)
            {
                autoSeg[i] = Integer.parseInt(ss[i + 1]);
            }
        }

        public DataXFileVer(DataXFileVer dv)
        {
            this.userSeg = dv.userSeg;
            if (dv.autoSeg != null)
            {
                autoSeg = new int[dv.autoSeg.length];
                System.arraycopy(dv.autoSeg,0, autoSeg,0, autoSeg.length);
            }
        }
        /// <summary>
        /// 根据已经存在的一个版本，自动增长版本单位
        /// </summary>
        /// <param name="dv"></param>
        /// <param name="auto_increase"></param>
        public DataXFileVer AutoIncrease()
        {
            DataXFileVer newdv = new DataXFileVer(this) ;

            if (newdv.autoSeg == null||newdv.autoSeg.length==0)
            {
                newdv.autoSeg = new int[1];
                newdv.autoSeg[0] = 1;
                return newdv ;
            }

            int c = newdv.autoSeg.length - 1;

            if (newdv.autoSeg[c] < 999)
            {
                newdv.autoSeg[c]++;
                return newdv;
            }
            else
            {
                int[] tmpis = newdv.autoSeg;
                newdv.autoSeg = new int[newdv.autoSeg.length + 1];
                System.arraycopy(tmpis,0, newdv.autoSeg,0, tmpis.length);
                newdv.autoSeg[c] = 1;
            }

            return newdv;
        }

        /// <summary>
        /// 用户版本号
        /// </summary>
        public int getUserVer()
        {
            return userSeg;
        }


        public DataXFileVer UserIncrease(int num)
        {
            if (num <= 0)
                throw new IllegalArgumentException("increase num < = 0 ");

            if(userSeg+num>9999)
                throw new IllegalArgumentException("version cannot > 9999") ;

            DataXFileVer newdv = new DataXFileVer(this);
            newdv.userSeg += num;
            return newdv;
        }

        public DataXFileVer(DataXFileVer basedv, int user_ver)
        {
            if (user_ver <= basedv.userSeg)
                throw new IllegalArgumentException("user version must > base ver");

            if (user_ver > 9999)
                throw new IllegalArgumentException("user version cannot > 9999");

            this.userSeg = user_ver;
            if (basedv.autoSeg != null)
            {
                autoSeg = new int[basedv.autoSeg.length];
                System.arraycopy(basedv.autoSeg,0, autoSeg,0, autoSeg.length);
            }
        }


        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            String strus = "" + userSeg;
            int c = strus.length();
            if (c == 1)
            {
                strus = "000" + strus;
            }
            else if (c == 2)
            {
                strus = "00" + strus;
            }
            else if (c == 3)
            {
                strus = "0" + strus;
            }
            sb.append(strus);

            if (autoSeg != null && autoSeg.length > 0)
            {
                for (int i : autoSeg)
                {
                    String tmps = "" + i;
                    c = tmps.length();
                    if (c == 1)
                    {
                        tmps = "00" + tmps;
                    }
                    else if (c == 2)
                    {
                        tmps = "0" + tmps;
                    }

                    sb.append('.').append(tmps);
                }
            }

            return sb.toString();
        }

        
        public int compareTo(Object obj)
        {
            DataXFileVer ov = (DataXFileVer)obj ;
            int i = this.userSeg - ov.userSeg;
            if (i != 0)
                return i;

            int aslen = 0 ;
            if(autoSeg!=null)
                aslen = autoSeg.length ;
            int oaslen = 0 ;
            if(ov.autoSeg!=null)
                oaslen = ov.autoSeg.length;

            int c = Math.max(aslen,oaslen) ;
            for (i = 0; i < c; i++)
            {
                if (aslen < i + 1)
                    return -1;

                if (oaslen < i + 1)
                    return 1;

                int tmpi = autoSeg[i] - ov.autoSeg[i];
                if (tmpi != 0)
                    return tmpi;
            }

            return 0;
        }

}
