package com.dw.net.dirqueue;


public interface IObjHandler
{
    /**
     * 失败的尝试次数
     * @return
     * @throws Exception
     */
    int processFailedRetryTimes();

    /**
     * 重新尝试处理的延迟-考虑到队列只有一个内容的情况,和现实
     * 需要被处理对象一旦出现错误需要重新处理的情况下,最好做一定的时间延迟
     * 并且延迟和不同的时间相关
     * @return
     */
    long processRetryDelay(int retrytime);

    /**
     * 正常的处理过程
     * @param o
     * @throws Exception
     */
    HandleResult processObj(Object o, int retrytime);

    /**
     * 处理器本身无效的等待时间,该时间不能太长,否则会影响处理速度
     * @return
     */
    long handlerInvalidWait();
    /**
     * 根据一定的重新尝试策略,最终确定需要抛弃的内容处理
     * @param o
     * @throws Exception
     */
    void processObjDiscard(Object o);
}
