package com.dw.net.dirqueue;


import java.io.*;
import java.util.*;
 
public class QueueThreadNoWait
{
        private int NO_WAIT_CHECK_INTERVAL = 10;
        private Thread procTh = null;
        private IObjHandler handler = null;


        /**
         * 正常列表
         */
        private LinkedList<QueItem> normalList = new LinkedList<QueItem>();

        /**
         * 重新尝试列表,里面的内容需要做排序
         */
        private LinkedList<QueItem> retryList = new LinkedList<QueItem>();

        public QueueThreadNoWait(IObjHandler h)
        {
            handler = h;
        }

        Runnable runner = new Runnable()
        {
	        public void run()
	        {
	            while (procTh != null)
	            {
	                QueItem qi = dequeue_qi();
	                if (qi == null)
	                {
	                	try
	                	{
		                    Thread.sleep(NO_WAIT_CHECK_INTERVAL);
		                }
	                    catch(Exception e)
	                    { }
	                    continue;
	                }
	
	                try
	                {
	                    HandleResult hr = handler.processObj(qi.objV, qi.retryTime);
	                    if (hr == HandleResult.Failed_Retry_Later)
	                    {
	                        doRetry(qi);
	                        continue;
	                    }
	
	                    if (hr == HandleResult.Handler_Invalid)
	                    {//处理器本身无效,
	                        long hvw = handler.handlerInvalidWait();
	                        if (hvw > 0)
	                        {
	                            try
	                            {
	                                Thread.sleep(hvw);
	                            }
	                            catch(Exception e)
	                            { }
	                        }
	                        continue;
	                    }
	
	                    //					处理成功
	                    remove_qi(qi);
	                    continue;
	                }
	                catch(Exception ee)
	                {
	                    doRetry(qi);
	                }
	            }
	        }
        };
        
        private void doRetry(QueItem qi)
        {
            int rtn = handler.processFailedRetryTimes();
            int oldrtn = qi.retryTime;
            if (rtn > 0 && oldrtn < rtn)
            {
                long wt = handler.processRetryDelay(oldrtn + 1);
                if (wt <= 0)
                    wt = 10;
                qi.retryTime++;
                qi.startProcT = System.currentTimeMillis() + wt;
                remove_qi(qi);
                enqueue_retry_qi(qi);//add to queue again
            }
            else
            {
                //discard
                try
                {
                    handler.processObjDiscard(qi.objV);
                }
                catch(Exception ee)
                { }

                remove_qi(qi);
            }
        }

        public void start()
        {
            if (procTh != null)
                return;

            procTh = new Thread(runner);
            procTh.start();
        }

        public void stop()
        {
            if (procTh == null)
                return;

            procTh.interrupt();
            procTh = null;
        }



        public boolean isEmpty()
        {
            synchronized (this)
            {
                return (size() == 0);
            }
        }

        public int size()
        {
        	synchronized (this)
            {
                return normalList.size() + retryList.size();
            }
        }

        public void enqueue(Object o)
        {
            QueItem qi = new QueItem(o);
            enqueue_qi(qi);
        }

        private void enqueue_qi(QueItem o)
        {
        	synchronized (this)
            {
                o.belongToLL = normalList;
                normalList.addLast(o);
            }
        }

        private void enqueue_retry_qi(QueItem qi)
        {
        	synchronized (this)
            {
                qi.belongToLL = retryList;
                retryList.addLast(qi);
                sort(retryList);

            }
        }

        static void sort(LinkedList<QueItem> ll)
        {
            if (ll == null)
                return;

            if (ll.size() <= 0)
                return;

            QueItem[] ss = new QueItem[ll.size()];

            ll.toArray(ss);//.copyTo(ss, 0);

            Arrays.sort(ss);

            ll.clear();
            for (QueItem c : ss)
            {
                ll.addLast(c);
            }
        }

        boolean lastPeekNormal = false;

        /**
         * 实现队列查找--能够避免retry的delay不会造成不被处理的情况
         * @return
         */
        private QueItem peekQueue()
        {
            if (normalList.size() == 0)
            {
                if (retryList.size() == 0)
                    return null;
                else
                {
                    lastPeekNormal = false;//peek retry
                    return retryList.getFirst();
                }
            }
            else
            {
                if (retryList.size() == 0)
                {
                    lastPeekNormal = true;
                    return normalList.getFirst();
                }
                else
                {
                    QueItem n_qi = normalList.getFirst();
                    QueItem r_qi = retryList.getFirst();
                    if (r_qi.startProcT > System.currentTimeMillis())
                    {//重新尝试列表的内容需要等待,normal优先级高
                        lastPeekNormal = true;
                        return n_qi;
                    }

                    //如果都不需要等待,则交替处理
                    if (lastPeekNormal)
                    {
                        lastPeekNormal = false;
                        return r_qi;
                    }
                    else
                    {
                        lastPeekNormal = true;
                        return n_qi;
                    }
                }
            }
        }

        private QueItem dequeue_qi()
        {//为了避免retry列表被忽略,dequeue支持交替的算法.
        	synchronized (this)
            {
                QueItem qi = peekQueue();

                if (qi == null)
                {
                    return null;
                }


                long ct = System.currentTimeMillis();
                if (qi.startProcT > ct)
                {
                    return null;
                }

                return qi;
            }
        }

        private void remove_qi(QueItem qi)
        {
        	synchronized (this)
            {
                qi.removeFromList();
            }
        }

        /**
         * 清空，队列中的所有内容
         */
        public void emptyQueue()
        {
            synchronized (this)
            {
                normalList.clear();
                retryList.clear();
            }
        }


    }

    
    class QueItem implements Comparable<QueItem>
    {
        Object objV = null;
        /**
         * 已经尝试次数
         */
        int retryTime = 0;
        /**
         * 开始处理时间
         */
        long startProcT = -1;

        LinkedList<QueItem> belongToLL = null;

        QueItem(Object ov)
        {
            objV = ov;
        }

        void removeFromList()
        {
            belongToLL.remove(this);
        }

        //#region IComparable<QueItem> 成员

        public int compareTo(QueItem other)
        {
            long v = startProcT - other.startProcT;
            if (v > 0)
                return 1;
            if (v < 0)
                return -1;
            return 0;
        }

        //#endregion
    }
