package com.dw.system.task;

/**
 * ֧�ֽ��̺Ϳ�����֮��ͨ���ļ�����������ʽ���ź����ȣ����н�������
 * Ŀ�ģ����������Ա�֤�ܹ��������̣�����ֻ��һ������ʵ��
 * 
 * 
 * @author Jason Zhu
 */
public class ProHelper
{
	/**
	 * �÷��������Ƴ���ʹ�ã������������ȡ��Ӧ�Ľ���
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

        
        //��������û����������
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
    /// �÷�����ʵ�ֽ���ʹ�ã������ж��Ƿ��Ѿ����ڽ��̡�����Ѿ����ڣ����Լ���������
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
    /// ʵ���������ͨ�����ø÷����������Լ��Ƿ�Ҫ��������
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
