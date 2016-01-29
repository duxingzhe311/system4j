package com.dw.ttt;

import java.io.*;
import java.util.*;

import com.dw.system.xevent.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public class TTT
{

	public static void main(String[] args)
	{
		try
		{
			String inputLine;
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
				System.in));

			while ( (inputLine = in.readLine()) != null)
			{
				try
				{
					StringTokenizer st = new StringTokenizer(inputLine, " ", false);
					String cmds[] = new String[st.countTokens()];
					for (int i = 0; i < cmds.length; i++)
					{
						cmds[i] = st.nextToken();
					}

					if ("listevent".equals(cmds[0]))
					{
						Class[] cs = XEventDispatcher.getAllRegisteredEvent();
						for (int i = 0; i < cs.length; i++)
						{
							System.out.println(cs[i].getName());
							EventStyle es = XEventDispatcher.getEventStyle(cs[i]);
							System.out.println("   " + es);
						}
					}
					else if ("listlis".equals(cmds[0]))
					{
						ListenerShell[] lss = XEventDispatcher.getAllListeners();
						for (int i = 0; i < lss.length; i++)
						{
							System.out.println(lss[i]);
						}
					}
					else if ("sendevent".equals(cmds[0]))
					{
						TTTEvent te = new TTTEvent(cmds[1],
							Integer.parseInt(cmds[2]));
						TTTEvent1 te1 = new TTTEvent1(cmds[1],
							Integer.parseInt(cmds[2]));
						for (int i = 0; i < 6; i++)
						{
							XEventDispatcher.fireEvent(te);
							XEventDispatcher.fireEvent(te1);
						}
					}
					else if ("checkevent".equals(cmds[0]))
					{
						if (cmds.length < 2)
						{
							System.out.println(
								"   Error:no event class name input!");
							continue;
						}

						try
						{
							Class c = Class.forName(cmds[1]);
							Vector v = XEventDispatcher.getTargetListeners(c);
							int s = v.size();
							for (int i = 0; i < s; i++)
							{
								System.out.println(v.elementAt(i));
							}
						}
						catch (Exception e0)
						{
							e0.printStackTrace();
						}
					}
				}
				catch (Exception _e)
				{
					_e.printStackTrace();
				}
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}