package org.transcation.global.common;

import io.netty.channel.ChannelId;
import lombok.Data;

/**
 * 分支事物与对应的通道信息
 * @author: huml
 * @createTime: 2021/1/20
 */
@Data
public class Branch {

    private String branchId;

    private ChannelId channelId;


}
