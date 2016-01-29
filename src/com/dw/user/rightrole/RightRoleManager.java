package com.dw.user.rightrole;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GdbException;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlHelper;
import com.dw.user.Role;
import com.dw.user.RoleManager;
import com.dw.user.User;
import com.dw.user.UserProfile;

public class RightRoleManager
{
	public static final String APP_NAME = "appName";

	public static final String GROUPS = "groups";

	public static final String NAMES_PREFIX = "names";

	public static final String TITLES_PREFIX = "titles";

	public static final String NAME_TAIL = "Rule";

	//���ں�̨��ɫ������ĳ����ɫ֮����ʾ�Ľ�ɫ��Ա����
	public static final String PLUGS_URL = "plugs_url";
	
	public static final String PLUGS_TITLE = "plugs_title";
	
	//���ں�̨��ɫ����ѡ��ĳ����ɫ֮��ı༭��ɫ��ɾ����ɫ����
	public static final String OPER_URL = "oper_url";
	
	public static final String OPER_TITLE = "oper_title";
	
	private static ILogger log = LoggerManager
			.getLogger(RightRoleManager.class);

	private Map<String, RightRolePlugInfo> app2plug;

	private RightRoleManager()
	{

	}

	private static RightRoleManager INS;

	private static Object locker = new Object();

	public static RightRoleManager getInstance()
	{
		if (null != INS)
			return INS;
		synchronized (locker)
		{
			if (null != INS)
				return INS;
			INS = new RightRoleManager();
			return INS;
		}
	}

	public Map<String, RightRolePlugInfo> getRightRolePlugInfo()
	{
		if (app2plug != null)
			return app2plug;
		app2plug = new HashMap<String, RightRolePlugInfo>();

		AppInfo appinfo[] = CompManager.getInstance().getAllAppInfo();
		if (null == appinfo || appinfo.length == 0)
			return app2plug;

		HashMap<String, RightRolePlugInfo> tmp = new HashMap<String, RightRolePlugInfo>();
		for (int i = 0; i < appinfo.length; i++)
		{
			AppInfo ai = appinfo[i];
			AppWebConfig awc = AppWebConfig.getModuleWebConfig(ai
					.getContextName());
			if (awc != null)
			{
				Element ele = awc.getConfElement("rightrole_plugs");
				if (ele != null)
				{
					Element eles[] = XmlHelper.getSubChildElement(ele,
							"rightrole_plug");
					if (eles != null && eles.length > 0)
					{
						HashMap<String, String> hm = XmlHelper
								.getEleAttrNameValueMap(eles[0]);
						if (null != hm && hm.size() > 0)
						{
							String name = ai.getContextName();

							if (log.isDebugEnabled())
								log.debug("regist rightrole_plug for " + name);

							RightRolePlugInfo wmpi = new RightRolePlugInfo(
									name, hm);
							tmp.put(wmpi.getAppName(), wmpi);
						}
					}
				}
			}
		}
		app2plug = tmp;
		return tmp;
	}

	public String getAppName(XmlData xd)
	{
		if (null == xd)
			return null;
		return xd.getParamValueStr(APP_NAME);
	}

	public String[][] getGroupNameTitles(XmlData xd)
	{
		if (null == xd)
			return null;
		String[] nametitle = xd.getParamValuesStr(GROUPS);
		int len = 0;
		if (null != nametitle && (len = nametitle.length) > 0)
		{
			String[][] res = new String[len][len];
			for (int i = 0; i < len; i++)
			{
				String nt = nametitle[i];
				res[i] = nt.split("_");
			}
			return res;
		}
		return null;
	}

	public String[][] getItemNameTitles(XmlData xd, String group)
	{
		if (null == xd)
			return null;
		String[] names = xd.getParamValuesStr(NAMES_PREFIX + "_" + group);
		String[] titles = xd.getParamValuesStr(TITLES_PREFIX + "_" + group);
		return new String[][]
		{
				names, titles
		};
	}
	
	public String[][] getPlugUrlTitles(XmlData xd)
	{
		if(null == xd)
			return null;
		String[] titles = xd.getParamValuesStr(PLUGS_TITLE);
		String[] urls = xd.getParamValuesStr(PLUGS_URL);
		
		if(null == titles || null == urls)
			return null;
		if(titles.length != urls.length)
			return null;
		
		return new String[][]
		{
			titles,urls
		};
	}

	public String[][] getPlugOperUrlTitles(XmlData xd)
	{
		if(null == xd)
			return null;
		String[] titles = xd.getParamValuesStr(OPER_TITLE);
		String[] urls = xd.getParamValuesStr(OPER_URL);
		
		if(null == titles || null == urls)
			return null;
		if(titles.length != urls.length)
			return null;
		
		return new String[][]
		{
			titles,urls
		};
	}
	
	public List<RightRoleItem> getRightRoleItemByRoleId(String roleId)
	{
		if (Convert.isNullOrTrimEmpty(roleId))
			return null;

		String[] cols = new String[]
		{
				"RoleId"
		};
		String[] opers = new String[]
		{
				"="
		};
		String[] vals = new String[]
		{
				roleId
		};
		boolean[] null_ignores = new boolean[opers.length];
		Arrays.fill(null_ignores, true);

		try
		{
			List<RightRoleItem> list = GDB.getInstance()
					.listXORMAsObjListByColOperValue(RightRoleItem.class, cols,
							opers, vals, null_ignores, "RecordTime", 0, -1,
							null);
			return list;
		}
		catch (GdbException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public RightRoleItem getRightRoleItem(String roleId,String itemName)
	{
		if (Convert.isNullOrTrimEmpty(roleId))
			return null;

		String[] cols = new String[]
		{
				"RoleId", "RightName"
		};
		String[] opers = new String[]
		{
				"=", "="
		};
		String[] vals = new String[]
		{
				roleId, itemName
		};
		boolean[] null_ignores = new boolean[opers.length];
		Arrays.fill(null_ignores, true);

		try
		{
			List<RightRoleItem> list = GDB.getInstance()
					.listXORMAsObjListByColOperValue(RightRoleItem.class, cols,
							opers, vals, null_ignores, "RecordTime", 0, -1,
							null);
			if (null == list || list.isEmpty())
				return null;
			return list.get(0);
		}
		catch (GdbException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean checkHasRight(String roleId, String itemName)
	{
		RightRoleItem rri = getRightRoleItem(roleId,itemName);
		return null == rri ? false : rri.isHasRight();
	}

	/**
	 * ���up��itemName�Ƿ����Ȩ�ޣ��÷���ֻ����up���߱��Ľ�ɫ��itemName�Ƿ����Ȩ��
	 * @param up
	 * @param itemName
	 * @return
	 */
	public boolean checkHasRightOnlyWithRole(UserProfile up,String itemName)
	{
		if(null == up)
			return false;
		if(Convert.isNullOrTrimEmpty(itemName))
			return false;
		
		User u = up.getUserInfo();
		if(null == u)
			return false;
		
		try
		{
			List<Role> roles = RoleManager.getDefaultIns().GetRolesForUser(u.getUserName());
			if(null == roles || roles.size() == 0)
				return false;
			for(Role role : roles)
			{
				if(null == role)
					continue;
				if(checkHasRight(role.getId(),itemName))
					return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public void doAdd(XmlData xd)
	{
		if (null == xd)
			return;

		String[] names = xd.getParamNames();
		if (null == names || names.length == 0)
			return;
		String roleId = xd.getParamValueStr("roleId");

		if (Convert.isNullOrTrimEmpty(roleId))
			return;

		Date d = new Date();
		for (String n : names)
		{
			if (!n.endsWith(NAME_TAIL))
				continue;

			boolean val = xd.getParamValueBool(n, false);
			try
			{
				RightRoleItem rri = new RightRoleItem();

				rri.setRoleId(roleId);
				rri.setRightName(n);
				rri.setRecordTime(d);
				rri.setHasRight(val);

				GDB.getInstance().addXORMObjWithNewId(rri);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	public void doDel(XmlData xd)
	{
		if (null == xd)
			return;
		String roleId = xd.getParamValueStr("roleId");

		if (Convert.isNullOrTrimEmpty(roleId))
			return;
		// delete all
		try
		{
			Hashtable ht = new Hashtable();
			ht.put("@RoleId", roleId);
			GDB.getInstance().accessDB("RightRole.deleteByRoleId", ht);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void doEdit(XmlData xd)
	{
		if (null == xd)
			return;

		String[] names = xd.getParamNames();
		if (null == names || names.length == 0)
			return;
		String roleId = xd.getParamValueStr("roleId");

		if (Convert.isNullOrTrimEmpty(roleId))
			return;
		
		try
		{
			//�Ȳ�һ���Ƿ��Ѿ��и�Role��Ӧ�ļ�¼�����û�У���Ҫȥ��ӣ��������޸ġ�
			int count = GDB.getInstance().getCountByXORMColOperValue(RightRoleItem.class,new String[]{"RoleId"},new String[]{"="},new String[]{roleId});
			if(0 == count)
			{
				doAdd(xd);
				return;
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		for (String n : names)
		{
			if (!n.endsWith(NAME_TAIL))
				continue;

			RightRoleItem rri = getRightRoleItem(roleId,n);
			boolean val = xd.getParamValueBool(n, false);

			try
			{
				//û�еĻ������
				if(null == rri)
				{
					rri = new RightRoleItem();
					rri.setRecordTime(new Date());
					rri.setRoleId(roleId);
					rri.setHasRight(val);
					rri.setRightName(n);
					GDB.getInstance().addXORMObjWithNewId(rri);
					return;
				}
				
				Hashtable ht = new Hashtable();
				ht.put("@RightName", n);
				ht.put("@RoleId", roleId);
				ht.put("@HasRight", val ? 1 : 0);
				GDB.getInstance().accessDB("RightRole.updateByNameAndRole", ht);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
