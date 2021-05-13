package org.transcation.global.common;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分支事物
 * @author: huml
 * @createTime: 2021/1/18
 */
public class BranchTransaction {
    /**
     * 全局事务id
     */
    private String xid;
    /**
     * 分支事物id
     */
    private String branchId;
    /**
     * 事物类型
     */
    private TranscationType transcationType;
    /**
     * 标记主事物
     */
    private boolean isMaster;
    /**
     * 内部类
     * 提供阻塞事物线程和唤醒事物线程的方法
     */
    private Task task;

    public BranchTransaction(){
        this.task = new Task();
    }

    public BranchTransaction(String xid) {
        this.xid = xid;
        this.task = new Task();
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public TranscationType getTranscationType() {
        return transcationType;
    }

    public void setTranscationType(TranscationType transcationType) {
        this.transcationType = transcationType;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    /**
     * 事物状态
     */
    public enum TranscationType {
        /**
         * 提交成功
         */
        COMMIT,
        /**
         * 异常回滚
         */
        ROLLBACK;
    }

    public class Task {
        private Lock lock = new ReentrantLock();
        private Condition condition = lock.newCondition();

        /**
         * 阻塞
         */
        public void await(){
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        /**
         * 唤醒
         */
        public void signal(){
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

    }

}
