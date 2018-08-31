package com.newframe.services.account;

import com.newframe.dto.OperationResult;
import com.newframe.dto.account.AccountRenterRentInfo;
import com.newframe.dto.account.RenterAccountInfo;
import com.newframe.dto.account.RenterOrderFinanceInfo;
import com.newframe.entity.account.AccountRenter;
import com.newframe.entity.account.AccountRenterFinancingMachine;

/**
 * @author:wangdong
 * @description:
 */
public interface AccountManageService {

    /**
     * 租赁商获取账户信息
     * @param uid
     * @return
     */
    OperationResult<RenterAccountInfo> getRenterAccountInfo(Long uid);

    /**
     * 获取租赁商的账户资产
     * @param uid
     * @return
     */
    OperationResult<AccountRenter> getRenterAssetAccount(Long uid);

    /**
     *
     * @param uid
     * @param orderStatus
     * @param currentPage
     * @param pageSize
     * @return
     */
    OperationResult<AccountRenterRentInfo> listRenterOrderRentAccount(Long uid, Integer orderStatus, Integer currentPage, Integer pageSize);

    /**
     * 租赁商获取订单融资账户
     * @param uid
     * @return
     */
    OperationResult<AccountRenterFinancingMachine> getRenterOrderFinanceAccount(Long uid);

    /**
     * 我是租赁商订单融资账户订单融资列表
     * @param uid
     * @param orderStatus
     * @param currentPage
     * @param pageSize
     * @return
     */
    OperationResult<RenterOrderFinanceInfo> listRenterOrderFinance(Long uid, Integer orderStatus, Integer currentPage, Integer pageSize);
}
