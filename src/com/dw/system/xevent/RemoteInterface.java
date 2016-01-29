package com.dw.system.xevent;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public abstract class RemoteInterface
{
	protected RemoteCallback callback = null;

	public RemoteInterface(RemoteCallback rcb)
	{
		if (rcb == null)
		{
			throw new IllegalArgumentException("RemoteCallback Cannot be null!");
		}
		callback = rcb;
	}

	public abstract void sendXEvent(byte[] eventdata);

}