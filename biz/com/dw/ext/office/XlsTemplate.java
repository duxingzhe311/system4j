package com.dw.ext.office;

import com.dw.system.Convert;
import com.dw.system.gdb.DataRow;
import com.dw.system.gdb.DataTable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class XlsTemplate
{
  HashMap<String, Loc> name2Loc = new HashMap();
  Workbook tempWB;

  public XlsTemplate(InputStream is)
    throws Exception, IOException
  {
    Workbook workbook = Workbook.getWorkbook(is);
    this.tempWB = workbook;
    int sheetNum = workbook.getNumberOfSheets();
    for (int i = 0; i < sheetNum; i++)
    {
      Sheet sheet = workbook.getSheet(i);
      String sheetName = sheet.getName();
      int rowNum = sheet.getRows();
      int colNum = sheet.getColumns();

      for (int r = 0; r < rowNum; r++)
      {
        for (int c = 0; c < colNum; c++)
        {
          Cell cell = sheet.getCell(c, r);
          String content = cell.getContents();
          if (content != null)
          {
            content = content.trim();
            boolean isStart = content.startsWith("[#");
            boolean isEnd = content.endsWith("#]");
            if ((isStart) && (isEnd))
            {
              Loc loc = new Loc();
              String pn = content.substring(2, content.length() - 2).trim();
              StringTokenizer st = new StringTokenizer(pn, ";,|");
              while (st.hasMoreTokens())
              {
                String s = st.nextToken();
                int p = s.indexOf('=');
                if (p >= 0)
                {
                  String tmpn = s.substring(0, p).trim();
                  String tmpv = s.substring(p + 1).trim();
                  if ("name".equalsIgnoreCase(tmpn))
                  {
                    loc.name = tmpv;
                  }
                  else if (("max_rows".equalsIgnoreCase(tmpn)) || ("maxrows".equalsIgnoreCase(tmpn)))
                  {
                    loc.maxRows = Convert.parseToInt32(tmpv, -1);
                  }
                }
              }
              if (!Convert.isNullOrEmpty(loc.name))
              {
                loc.sheetIdx = i;
                loc.x = c;
                loc.y = r;
                this.name2Loc.put(loc.name, loc);
              }
            }
          }
        }
      }
    }
  }

  public void writeDataOut(OutputStream os, HashMap<String, DataTable> loc2dt) throws Exception {
    Iterator it = loc2dt.entrySet().iterator();
    byte[] tmp_cont = (byte[])null;
    Workbook tmpwb = null;
    while (it.hasNext())
    {
      if (tmp_cont != null)
      {
        ByteArrayInputStream bais = new ByteArrayInputStream(tmp_cont);
        tmpwb = Workbook.getWorkbook(bais);
      }
      else
      {
        tmpwb = this.tempWB;
      }
      ByteArrayOutputStream boas = new ByteArrayOutputStream();
      WritableWorkbook workbook = Workbook.createWorkbook(boas, tmpwb);

      Map.Entry entry = (Map.Entry)it.next();
      String parameterName = (String)entry.getKey();
      DataTable dt = (DataTable)entry.getValue();

      int dtcNum = dt.getColumnNum();
      int dtrNum = dt.getRowNum();

      Loc loc = (Loc)this.name2Loc.get(parameterName);
      if (loc != null)
      {
        int start_r = loc.y;
        int start_c = loc.x;

        if ((loc.maxRows > 0) && (dt.getRowNum() > loc.maxRows)) {
          throw new Exception("Xls Template with tag=" + loc.name + " has max row =[" + loc.maxRows + "],but input table rows[" + dt.getRowNum() + "] is much more than it!");
        }

        WritableSheet sheet = workbook.getSheet(loc.sheetIdx);
        int all_cs = sheet.getColumns();
        WritableCell[] wcs = new WritableCell[all_cs];
        for (int k = 0; k < all_cs; k++)
        {
          wcs[k] = sheet.getWritableCell(k, start_r);
        }

        for (int r = 0; r < dtrNum; r++)
        {
          for (int c = 0; c < all_cs; c++)
          {
            DataRow dr = dt.getRow(r);
            Object o = null;
            if (c < dt.getColumnNum()) {
              o = dr.getValue(c);
            }
            WritableCell wc = null;
            if (o != null)
            {
              if ((o instanceof java.lang.Number))
              {
                wc = new jxl.write.Number(c + start_c, r + start_r, ((java.lang.Number)o).doubleValue());
              }
              else
              {
                wc = new Label(c + start_c, r + start_r, o.toString());
              }
            }
            else
            {
              wc = wcs[c].copyTo(c, r + start_r);
            }

            sheet.addCell(wc);
          }

          if (loc.maxRows <= 0) {
            sheet.insertRow(r + start_r + 1);
          }
        }
        workbook.write();
        workbook.close();

        tmp_cont = boas.toByteArray();
      }
    }
    if (tmp_cont != null)
      os.write(tmp_cont);
  }

  public static void main(String[] args)
    throws Exception, Exception
  {
    DataTable dt = new DataTable();
    dt.addColumn("1", String.class);
    dt.addColumn("2", String.class);
    dt.addColumn("3", String.class);
    dt.addColumn("4", String.class);
    dt.addColumn("5", String.class);
    dt.addColumn("6", String.class);
    dt.addColumn("7", String.class);

    for (int r = 0; r < 10; r++)
    {
      for (int c = 0; c < 7; c++)
      {
        DataRow dr = dt.createNewRow();
        dr.putValue(c, "row" + r + ":" + "column" + c);
        dt.addRow(dr);
      }
    }

    HashMap hm = new HashMap();
    hm.put("d1", dt);
    hm.put("d2", dt);
    hm.put("d3", dt);

    File file = new File("E:/work_ll/demo.xls");
    FileInputStream fis = null;
    FileOutputStream fos = null;
    try
    {
      fis = new FileInputStream(file);
      fos = new FileOutputStream("E:/work_ll/o1.xls");

      XlsTemplate xt = new XlsTemplate(fis);

      Workbook workbook = Workbook.getWorkbook(file);
      xt.writeDataOut(fos, hm);
    }
    finally
    {
      if (fis != null)
        fis.close();
    }
  }

  static class Loc
  {
    String name = null;
    int sheetIdx = 0;
    int x = -1;
    int y = -1;

    int maxRows = -1;
  }
}
