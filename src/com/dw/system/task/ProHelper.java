package com.dw.system.task;

/**
 * 支持进程和控制器之间通过文件（或其他方式如信号量等）进行交互控制
 * 目的：控制器可以保证能够启动进程，并且只有一个进程实例
 * 
 * 
 * @author Jason Zhu
 */
public class ProHelper
{
	/**
	 * 该方法给控制程序使用，用来创建或获取对应的进程
	 * @param ctrlfn
	 * @param psi
	 * @return
	 */
	/*
    public static Process GetOrCreateTaskProcess(string ctrlfn,ProcessStartInfo psi)
    {
        int pid = GetExistedProcessPid(ctrlfn);
        if (pid > 0)
            return Process.GetProcessById(pid);

        
        //不存在且没有其他进程
        //psi.WindowStyle = ProcessWindowStyle.;
        //psi.CreateNoWindow = true;
        //psi.UseShellExecute = false;
        //psi.RedirectStandardInput = true;
        //psi.RedirectStandardOutput = true;
        //psi.RedirectStandardError = true;
        //psi.WindowStyle = ProcessWindowStyle.Hidden;
        
        return Process.Start(psi);
        //Thread.Sleep(500);
        
        //return GetOrCreateTaskProcess(ctrlfn, psi);
    }

    public static void StopTaskProcess(string ctrlfn)
    {
        FileInfo fi = new FileInfo(ctrlfn + ".pid");
        if (!fi.Exists)
            return;

        fi.Delete();
    }
    /// <summary>
    /// 该方法给实现进程使用，用来判断是否已经存在进程。如果已经存在，这自己不做启动
    /// </summary>
    /// <param name="ctrlfn"></param>
    /// <returns></returns>
    public static bool CheckTaskProcessExisted(string ctrlfn)
    {

        FileStream fs = TryWriteLockCtrlFile(ctrlfn);
        bool b = (fs == null);

        if (fs != null)
            fs.Close();

        return b;
    }


    /// <summary>
    /// 实现任务进程通过调用该方法来决定自己是否要继续运行
    /// </summary>
    /// <param name="ctrlfn"></param>
    /// <returns></returns>
    public static WebTaskCtrl TryToLockCtrlFileWithCurrentPid(string ctrlfn)
    {
        FileStream fs = TryWriteLockCtrlFile(ctrlfn);
        if (fs == null)
            return null;

        WriteCurrentPidToLockFile(ctrlfn);

        return new WebTaskCtrl(ctrlfn, fs);
    }

    static FileStream TryWriteLockCtrlFile(string ctrlfn)
    {
        //FileInfo f = new FileInfo(ctrlfn);
        //if (!f.Exists)
        //    return null;

        FileStream fs = null;

        try
        {
            fs = new FileStream(ctrlfn+".ctrl", FileMode.Create, FileAccess.Write);//, FileShare.Read,8,FileOptions.RandomAccess);
            return fs;
        }
        //catch// (UnauthorizedAccessException uae)
        //{
        //    return null;
        //}
        catch// (IOException se)
        {
            return null;
        }
    }

    static void WriteCurrentPidToLockFile(string ctrlfn)
    {
        Process p = Process.GetCurrentProcess();
        int pid = p.Id;

        FileStream fs = null;
        try
        {
            fs = new FileStream(ctrlfn + ".pid", FileMode.Create, FileAccess.Write);
            BinaryWriter bw = new BinaryWriter(fs);
            bw.Write(pid);
            bw.Flush();
            fs.Flush();
        }
        finally
        {
            if (fs != null)
                fs.Close();
//#if DEBUG
//            Console.WriteLine("CurPid={0}", pid);
//#endif
        }
    }

    //public static void UnlockCtrlFile(FileStream fs)
    //{
    //    if (fs != null)
    //        fs.Close();
    //}

    public static int GetExistedProcessPid(string ctrlfn)
    {
        if (!CheckTaskProcessExisted(ctrlfn))
            return -1;

        FileStream fs = null;
        FileInfo f = new FileInfo(ctrlfn+".pid");
        if (!f.Exists)
            return -1;

        try
        {

            fs = new FileStream(ctrlfn + ".pid", FileMode.Open, FileAccess.Read);//, FileShare.Read, 8, FileOptions.RandomAccess);
            System.IO.BinaryReader br = new BinaryReader(fs) ;
            return br.ReadInt32() ;
        }
        catch// (Exception e)
        {
            return -1;
        }
        finally
        {
            if (fs != null)
                fs.Close();
        }
    }
    */
}
