package org.transcation.global.lock;

import lombok.Data;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author: huml
 * @createTime: 2021/1/20
 */
@Data
public class MyLock {

    private Lock lock;


    class Lock extends AbstractQueuedSynchronizer {
        /**
         * 获取锁
         */
        public void lock(){
            acquire(1);
        }

        /**
         * 释放锁
         */
        public void unlock(){
            release(1);
        }

        /**
         * 释放锁实现
         * @param arg
         * @return
         */
        @Override
        public boolean tryRelease(int arg){
            final Thread thread = Thread.currentThread();
            //依据可重入锁的概念  state减去arg 如果减后仍大于0 锁持有数减少 并不释放
            //只有当减后等于0时 锁才释放
            if (getExclusiveOwnerThread() != thread) {
                throw new RuntimeException("非锁持有者");
            }
            int state = getState() - arg;
            boolean isRelease = false;
            if (state <= 0) {
                state = 0;
                //当前持有线程置为空
                setExclusiveOwnerThread(null);
                isRelease = true;
            }
            setState(state);

            return isRelease;
        }

    }

    /**
     * 公平锁实现类
     */
    class FairLock extends Lock {
        /**
         * 获取锁实现
         * 成功获取锁有两种情况
         * 1.state=0,当前无持有线程且阻塞链表中没有等待线程 CAS设置state 然后设置持有线程为当前线程
         * 2.state>0,判断当前锁持有线程是不是当前线程, 是:state加, 不是,获取锁失败
         * @param arg
         * @return
         */
        @Override
        public boolean tryAcquire(int arg){
            final Thread thread = Thread.currentThread();
            //公平锁 只有state=0并且队列中没有线程时才可以去获取
            int state = getState();
            if (state == 0) {
                if (!hasQueuedPredecessors() && compareAndSetState(0, arg)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
            } else if (getExclusiveOwnerThread() == thread) {
                //重入锁实现方式
                state += arg;
                setState(state);
                return true;
            }
            //AQS的acquire方法中已经实现了将获取锁失败的线程加入阻塞链表的操作
            return false;
        }

    }

    /**
     * 非公平锁实现类
     */
    class UnfairLock extends Lock {

        @Override
        public void lock(){
            //首先尝试获取一下
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                acquire(1);
            }
        }

        /**
         * 获取锁实现
         * 成功获取锁有两种情况
         * 1.state=0,当前无持有线程 CAS设置state 然后设置持有线程为当前线程
         * 2.state>0,判断当前锁持有线程是不是当前线程, 是:state加, 不是,获取锁失败
         * @param arg
         * @return
         */
        @Override
        protected boolean tryAcquire(int arg) {
            final Thread thread = Thread.currentThread();
            //公平锁 只有state=0并且队列中没有线程时才可以去获取
            int state = getState();
            if (state == 0) {
                if (compareAndSetState(0, arg)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
            } else if (getExclusiveOwnerThread() == thread) {
                //重入锁实现方式
                state += arg;
                setState(state);
                return true;
            }
            //AQS的acquire方法中已经实现了将获取锁失败的线程加入阻塞链表的操作
            return false;
        }
    }

    public MyLock(){
        lock = new FairLock();
    }

    public MyLock(boolean isFair) {
        lock = isFair ? new FairLock() : new UnfairLock();
    }

    /**
     * 获取锁
     */
    public void lock(){
        lock.lock();
    }

    /**
     * 释放锁
     */
    public void unlock(){
        lock.unlock();
    }

}
