package com.newframe.services.account;

import com.newframe.controllers.JsonResult;

import java.math.BigDecimal;

/**
 * @author:zww 31个接口
 * @description:账户相关模块的接口
 */
public interface AccountService {


    /**
     * 充值
     * 这里应该有一个公共的账户系统
     * 直接往里面充值人民币
     *
     * @return
     */
    JsonResult recharge(BigDecimal amount);

    /**
     * 提现
     * 这里提现的金额只允许可用金额提现
     *
     * @return
     */
    JsonResult withdrawDeposit();


    /**
     * 租赁方相关的接口
     */


    /**
     * 租赁商获取账户信息
     * 1、用户的手机号码
     * 2、设置过密码
     * 3、收货地址
     * 4、授权申请信息
     *
     * @return
     */
    JsonResult getRenterAccountInfo();

    /**
     * 租赁商获取账户资产
     * 1、可用余额
     * 2、资产总额
     * 3、冻结资产
     * 4、保证金余额
     * 5、保证金垫付金额
     * 6、代收金额
     * 7、本月应收
     *
     * @return
     */
    JsonResult getRenterAssetAccount();

    /**
     * 获取租赁商租赁明细
     * 涉及到分页
     *
     * @return
     */
    JsonResult getRenterRentDetail(Integer currentPage, Integer pageSize);

    /**
     * 获取租赁商订单融资账户
     * 涉及到
     * 1、订单融资余额
     * 2、已结清融资本息
     * 3、未结清融资本息
     * 4、本月应还
     *
     * @return
     */
    JsonResult getRenterOrderFinanceAccount();

    /**
     * 订单融资列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listRenterOrderFinance(Integer currentPage, Integer pageSize);

    /**
     * 租赁商订单融资列列表
     * 根据订单的ID去查询订单融资的金额
     *
     * @param orderId
     * @return
     */
    JsonResult getRenterOrderFinanceDetail(Long orderId);

    /**
     * 获取租赁商租赁账户
     * 涉及到
     * 1、租赁总额
     * 2、累计应付租金
     * 3、已付租金
     * 4、待付租金
     *
     * @return
     */
    JsonResult getRenterOrderRentAccount();

    /**
     * 租赁商租赁账户下
     * 租赁明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listRenterOrderRentAccount(Integer currentPage, Integer pageSize);

    /**
     * 租赁商租赁账户下
     * 租赁明细列表
     * 根据订单的Id,去查看详情
     *
     * @param orderId
     * @return
     */
    JsonResult getRenterOrderRentDetail(Long orderId);


    /**
     * 获取租赁商订单逾期账户
     * 涉及到
     * 1、逾期金额合计
     * 2、逾期笔数
     * 3、逾期率
     *
     * @return
     */
    JsonResult getRenterOrderOverdueAccount();

    /**
     * 租赁商订单逾期账户下
     * 租赁明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listRenterOrderOverdue(Integer currentPage, Integer pageSize);

    /**
     * 租赁商订单逾期账户下
     * 逾期订单租赁明细列表
     * 根据订单的Id,去查看详情
     *
     * @param orderId
     * @return
     */
    JsonResult getRenterOrderOverdueDetail(Long orderId);

    /**
     * 资金方相关的接口
     */

    /**
     * 资金方获取账户资产
     * 1、可用余额
     * 2、资产总额
     * 3、冻结资产
     * 4、保证金余额
     * 5、保证金垫付金额
     * 6、代收金额
     * 7、本月应收
     *
     * @return
     */
    JsonResult getFunderAssetAccount(Long uid);


    /**
     * 获取资金方金融资产账户
     * 涉及到
     * 1、投资回报率
     * 2、市场平均投资回报率
     *
     * @return
     */
    JsonResult getFunderOrderFinancialAssets(Long uid);

    /**
     * 获取资金方金融资产下
     * 投资明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listFunderOrderInvestment(Long uid, Integer currentPage, Integer pageSize);

    /**
     * 获取资金方金融资产下
     * 获取资金方金融资产下
     * 根据订单的Id,去查看详情
     *
     * @param orderId
     * @return
     */
    JsonResult getFunderOrderInvestmentDetail(Long uid, Long orderId);

    /**
     * 获取资金方逾期资产账户
     * 涉及到
     * 1、逾期金融合计
     * 2、逾期笔数
     * 3、逾期率
     *
     * @return
     */
    JsonResult getFunderOrderOverdueAssets();

    /**
     * 获取资金方逾期资产
     * 逾期明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listFunderOrderOverdue(Integer currentPage, Integer pageSize);

    /**
     * 获取资金方逾期资产
     * 逾期明细列表
     * 根据订单的Id,去查看详情
     *
     * @param orderId
     * @return
     */
    JsonResult getFunderOrderOverdueDetail(Long orderId);

    /**
     * 供应商相关账户接口
     */

    /**
     * 供应商获取账户资产
     * 1、可用余额
     * 2、资产总额
     * 3、冻结资产
     *
     * @return
     */
    JsonResult getSupplierAssetAccount();


    /**
     * 获取供应商销售账户
     * 涉及到
     * 1、累计营收
     * 2、累计销售数量
     * 3、待发货数量
     *
     * @return
     */
    JsonResult getSupplierOrderSellAssets();

    /**
     * 获取供应商销售账户下
     * 销售明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listSupplierOrderSell(Integer currentPage, Integer pageSize);

    /**
     * 出租方相关接口
     */

    /**
     * 出租方获取账户资产
     * 1、可用余额
     * 2、资产总额
     * 3、冻结资产
     * 4、保证金
     * 5、代收金额
     * 6、本月应收
     *
     * @return
     */
    JsonResult getHirerAssetAccount();


    /**
     * 获取出租方逾期资产账户
     * 涉及到
     * 1、逾期金额合计
     * 2、逾期笔数
     * 3、逾期率
     *
     * @return
     */
    JsonResult getHirerOrderOverdueAssets();

    /**
     * 获取出租方逾期资产账户下
     * 逾期明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listHirerOrderOverdue(Integer currentPage, Integer pageSize);

    /**
     * 获取出租方逾期资产账户下
     * 逾期明细列表
     *
     * @return
     */
    JsonResult getHirerOrderOverdueDetail(Long orderId);

    /**
     * 获取出租方实物资产账户
     * 涉及到
     * 1、租赁总额
     * 2、累计应付租金
     * 3、已付租金
     * 4、待付租金
     * 5、投资回报率
     * 6、市场平均投资回报率
     *
     * @return
     */
    JsonResult getHirerOrderMaterialAssets();

    /**
     * 获取出租方实物资产账户下
     * 实物明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    JsonResult listHirerOrderMaterial(Integer currentPage, Integer pageSize);

    /**
     * 获取出租方实物资产账户下
     * 实物明细列表
     *
     * @return
     */
    JsonResult getHirerOrderMaterialDetail(Long orderId);

}