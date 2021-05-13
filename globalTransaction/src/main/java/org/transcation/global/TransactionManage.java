package org.transcation.global;

import com.alibaba.fastjson.JSON;
import io.micrometer.core.instrument.util.StringUtils;
import org.transcation.global.common.BranchTransaction;
import org.transcation.global.netty.NettyClient;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事物管理 创建事物 注册事物
 * @author: huml
 * @createTime: 2021/1/18
 */
public class TransactionManage {
    /**
     * 存放当前线程对应的分支事物信息
     */
    private static ThreadLocal <BranchTransaction> localBranchTransaction = new ThreadLocal<>();
    /**
     * 存放当前线程对应的全局事物ID
     */
    private static ThreadLocal<String> localXid = new ThreadLocal<>();
    /**
     * 存放已经注册的事物
     * key: xid
     */
    private static Map<String, BranchTransaction> transactionMap = new ConcurrentHashMap<>();

    /**
     * 获取当前线程的事物
     * @return
     */
    public static BranchTransaction getCurrentTransaction(){
        return localBranchTransaction.get();
    }

    /**
     * 创建当前线程的xid
     * @return
     */
    public static String createCurrentXid(){
        String xid = localXid.get();
        if (xid == null) {
            xid = UUID.randomUUID().toString();
            localXid.set(xid);
        }
        return xid;
    }

    /**
     * 设置xid
     * @param xid
     */
    public static void setCurrentXid(String xid){
        localXid.set(xid);
    }

    /**
     * 创建或加入全局事物
     * @return
     */
    public static BranchTransaction createOrAddTransaction(){
        BranchTransaction branchTranscation = new BranchTransaction();

        String xid = localXid.get();
        if (StringUtils.isEmpty(xid)) {
            xid = createCurrentXid();
            branchTranscation.setMaster(true);
        } else {
            branchTranscation.setMaster(false);
        }
        branchTranscation.setXid(xid);
        branchTranscation.setBranchId(UUID.randomUUID().toString());
        localBranchTransaction.set(branchTranscation);

        return branchTranscation;
    }

    /**
     * 注册事物
     */
    public static void regTransaction(){
        BranchTransaction branchTransaction = localBranchTransaction.get();
        try {
            transactionMap.put(createCurrentXid(), branchTransaction);
            NettyClient.sendMessage(JSON.toJSONString(branchTransaction));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 完成事物
     * 设置事物的状态并唤醒事物线程
     * 该方法有netty接收到TC发送过来的消息后调用
     * @param msgTransaction
     */
    public static void finishTransaction(BranchTransaction msgTransaction){
        BranchTransaction branchTransaction = transactionMap.get(msgTransaction.getXid());
        branchTransaction.setTranscationType(msgTransaction.getTranscationType());
        branchTransaction.getTask().signal();
    }

}
