package com.newframe.entity.order;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author kfm
 */

@Data
@Entity
@Table(name = "order_renter")
public class OrderRenter {
    /** 创建时间*/
    public static final String CTIME = "ctime";
    /** 未删除状态*/
    public static final Integer NO_DELETE_STATUS = 0;
    /** 删除状态*/
    public static final Integer DELETE_STATUS = 1;
    /**租赁商订单状态*/
    public static final String ORDER_STATUS = "orderStatus";
    /**租赁商订单状态*/
    public static final String ORDER_TYPE = "orderType";

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "partner_order_id")
    private String partnerOrderId;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "renter_id")
    private Long renterId;

    @Column(name = "renter_name")
    private String renterName;

    @Column(name = "uid")
    private Long uid;

    @Column(name = "user_realname")
    private String userRealname;

    @Column(name = "user_id_number")
    private String userIdNumber;

    @Column(name = "user_mobile")
    private String userMobile;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "user_credit_score")
    private Integer userCreditScore;

    @Column(name = "user_credit_line")
    private Integer userCreditLine;

    @Column(name = "product_brand")
    private String productBrand;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_color")
    private String productColor;

    @Column(name = "product_storage")
    private Integer productStorage;

    @Column(name = "product_random_memory")
    private Integer productRandomMemory;

    @Column(name = "monthly_payment")
    private BigDecimal monthlyPayment;

    @Column(name = "number_of_payments")
    private Integer numberOfPayments;

    @Column(name = "down_payment")
    private BigDecimal downPayment;

    @Column(name = "accident_benefit")
    private BigDecimal accidentBenefit;

    @Column(name = "order_status")
    private Integer orderStatus;

    @Column(name = "delete_status")
    private Integer deleteStatus;

    @Column(name = "ctime")
    private Integer ctime;

    @Column(name = "utime")
    private Integer utime;

    @Column(name = "order_type")
    private Integer orderType;


}
