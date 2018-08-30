package com.newframe.dto.account.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 资金方金融资产表
 * </p>
 *
 * @author zww
 * @since 2018-08-29
 */
@Data
public class AccountFundingFinanceAssetListDTO {
    /**
     * ctime
     */
    private Long ctime;
    /**
     * utime
     */
    private Long utime;

    /**
     * 关联订单id
     */
    private Long orderId;

    /**
     * 投资方式(1：融资购机)
     */
    private Integer investWay;

    /**
     * 投资金额
     */
    private BigDecimal investAmount;


    /**
     * 投资期限
     */
    private Integer investMonth;

    /**
     * 收益率
     */
    private BigDecimal earningsRate;
    /**
     * 融资人-租赁商ID
     */
    private Long renterId;
    /**
     * 租赁商的名字
     */
    private String renterName;
    /**
     * 订单的状态。1:正常
     */
    private Integer orderStatus;

}