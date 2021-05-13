package org.transcation.global.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.transcation.global.TransactionManage;
import org.transcation.global.common.BranchTransaction;

/**
 * 事物切面
 * @author: huml
 * @createTime: 2021/1/18
 */
@Component
@Aspect
public class GlobalTranscationAspect {
    /**
     * 对于加了@GlobalTranscation注解的, 创建分支事物
     * 根据方法的执行情况设定事物状态,最后注册事务
     * @param proceed
     */
    @Around("@annotation(org.transcation.global.annotation.GlobalTranscation)")
    public void invoke(ProceedingJoinPoint proceed){

        //创建/加入事物
        BranchTransaction branchTranscation = TransactionManage.createOrAddTransaction();
        try {
            proceed.proceed();
            branchTranscation.setTranscationType(BranchTransaction.TranscationType.COMMIT);
        } catch (Throwable throwable) {
            branchTranscation.setTranscationType(BranchTransaction.TranscationType.ROLLBACK);
            throwable.printStackTrace();
        }

        //注册事物(发送消息到TC)
        TransactionManage.regTransaction();

    }

}
