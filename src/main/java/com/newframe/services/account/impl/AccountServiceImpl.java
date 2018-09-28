package com.newframe.services.account.impl;

import com.google.common.collect.Lists;
import com.newframe.controllers.JsonResult;
import com.newframe.controllers.PageJsonResult;
import com.newframe.dto.OperationResult;
import com.newframe.dto.account.RentMachineStatistics;
import com.newframe.dto.account.RenterFinanceStatistics;
import com.newframe.dto.account.response.*;
import com.newframe.entity.account.*;
import com.newframe.entity.order.OrderSupplier;
import com.newframe.enums.BizErrorCode;
import com.newframe.enums.SystemCode;
import com.newframe.enums.order.PayStatusEnum;
import com.newframe.repositories.dataMaster.account.*;
import com.newframe.repositories.dataQuery.account.*;
import com.newframe.repositories.dataSlave.account.*;
import com.newframe.repositories.dataSlave.order.OrderFunderSlave;
import com.newframe.repositories.dataSlave.order.OrderHirerSlave;
import com.newframe.repositories.dataSlave.order.OrderSupplierSlave;
import com.newframe.services.account.AccountManageService;
import com.newframe.services.account.AccountService;
import com.newframe.utils.cache.IdGlobalGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author:zww 31个接口
 * @description:账户相关模块的接口实现
 */
@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    AccountSlave accountSlave;

    @Autowired
    AccountFundingFinanceAssetSlave accountFundingFinanceAssetSlave;
    @Autowired
    AccountFundingOverdueAssetSlave accountFundingOverdueAssetSlave;

    @Autowired
    AccountLessorMatterAssetSlave accountLessorMatterAssetSlave;
    @Autowired
    AccountLessorMatterAssetViewSlave accountLessorMatterAssetViewSlave;
    @Autowired
    AccountLessorOverdueAssetSlave accountLessorOverdueAssetSlave;

    @Autowired
    AccountSupplierSellSlave accountSupplierSellSlave;

    @Autowired
    OrderFunderSlave orderFunderSlave;
    @Autowired
    OrderHirerSlave orderHirerSlave;
    @Autowired
    OrderSupplierSlave orderSupplierSlave;

    @Autowired
    private AccountRenterRentSlave accountRenterRentSlave;

    @Autowired
    private AccountRenterAppointSupplierSlave accountRenterAppointSupplierSlave;

    @Autowired
    private AccountRenterFinancingMachineSlave accountRenterFinancingMachineSlave;

    @Autowired
    private AccountRenterFinancingSlave accountRenterFinancingSlave;

    @Autowired
    private AccountRenterRepaySlave accountRenterRepaySlave;

    @Autowired
    private AccountRenterRentMachineSlave accountRenterRentMachineSlave;

    @Autowired
    private AccountRenterRentDetailSlave accountRenterRentDetailSlave;

    @Autowired
    private AccountRenterOverdueAssetSlave accountRenterOverdueAssetSlave;

    @Autowired
    private AccountRenterOverdueDetailSlave accountRenterOverdueDetailSlave;

    @Autowired
    private AccountMaster accountMaster;

    @Autowired
    private IdGlobalGenerator idGlobal;

    @Autowired
    private AccountRenterRentMaster accountRenterRentMaster;

    @Autowired
    private AccountRenterRentDetailMaster accountRenterRentDetailMaster;

    @Autowired
    private AccountRenterRepayMaster accountRenterRepayMaster;

    @Autowired
    private AccountStatementMaster accountStatementMaster;

    @Autowired
    AccountFundingFinanceAssetMaster accountFundingFinanceAssetMaster;
    @Autowired
    AccountLessorMatterAssetMaster accountLessorMatterAssetMaster;
    @Autowired
    AccountManageService accountManageService;
    @Autowired
    private AccountRenterFinancingMaster accountRenterFinancingMaster;

    @Autowired
    private AccountRenterFinancingMachineMaster accountRenterFinancingMachineMaster;

    @Autowired
    private AccountRenterRentMachineMaster accountRenterRentMachineMaster;

    @Autowired
    private AccountRenterOverdueAssetMaster accountRenterOverdueAssetMaster;

    @Autowired
    private AccountRenterOverdueDetailMaster accountRenterOverdueDetailMaster;

    @Override
    public JsonResult recharge(BigDecimal amount) {
        return null;
    }

    @Override
    public JsonResult withdrawDeposit() {
        return null;
    }


    /**
     * 15.资金方获取账户资产
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
    @Override
    public JsonResult getFunderAssetAccount(Long uid) {
        Account entity = accountSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        //DueAmount 转化成 DueInAmount
        AccountFundingDTO dto = new AccountFundingDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setDueInAmount(entity.getDueAmount());
        return new JsonResult(SystemCode.SUCCESS, dto);
    }

    /**
     * 16.获取资金方金融资产账户
     * 涉及到
     * 1、投资回报率
     * 2、市场平均投资回报率
     *
     * @return
     */
    @Override
    public JsonResult getFunderOrderFinancialAssets(Long uid) {
        AccountFundingFinanceAsset entity = accountFundingFinanceAssetSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        AccountFundingFinanceAssetDTO dto = new AccountFundingFinanceAssetDTO();
        BeanUtils.copyProperties(entity, dto);
        return new JsonResult(SystemCode.SUCCESS, dto);
    }

    /**
     * 17.获取资金方金融资产下
     * 投资明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public JsonResult listFunderOrderInvestment(Long uid, Integer currentPage, Integer pageSize, Integer orderStatus) {
        if (null == currentPage || currentPage <= 1) {
            currentPage = 1;
        }
        if (null == pageSize || pageSize <= 1) {
            pageSize = 1;
        }
        currentPage--;
        Pageable pageable = new PageRequest(currentPage, pageSize, Sort.Direction.DESC, "ctime");
        AccountFundingFinanceAssetQuery query = new AccountFundingFinanceAssetQuery();
        query.setUid(uid);
        query.setOrderStatus(orderStatus);
        Page<AccountFundingFinanceAsset> page = accountFundingFinanceAssetSlave.findAll(query, pageable);

        List<AccountFundingFinanceAssetListDTO> dtoList = new ArrayList<>();
        for (AccountFundingFinanceAsset entity : page.getContent()) {
            AccountFundingFinanceAssetListDTO dto = new AccountFundingFinanceAssetListDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setInvestMonth(entity.getInvestDeadline());
            dto.setEarningsRate(entity.getYieldRate());
            dtoList.add(dto);
        }
        return new PageJsonResult(SystemCode.SUCCESS, dtoList, page.getTotalElements());
    }

    /**
     * 18.查看资金方分期收款计划
     *
     * @param orderId
     * @return
     */
    @Override
    public JsonResult getFunderOrderInvestmentDetail(Long uid, Long orderId) {
        if (null == orderId) {
            return null;
        }
        AccountRenterRepayQuery query = new AccountRenterRepayQuery();
        query.setOrderId(orderId);
        List<AccountRenterRepay> accountRenterRepays = accountRenterRepaySlave.findAll(query);
        return new JsonResult(SystemCode.SUCCESS, CollectionUtils.isEmpty(accountRenterRepays) ? Collections.EMPTY_LIST : accountRenterRepays);
    }

    /**
     * 19.获取资金方逾期资产账户
     * 涉及到
     * 1、逾期金融合计
     * 2、逾期笔数
     * 3、逾期率
     *
     * @return
     */
    @Override
    public JsonResult getFunderOrderOverdueAssets(Long uid) {
        AccountFundingOverdueAsset entity = accountFundingOverdueAssetSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        AccountFundingOverdueAssetDTO dto = new AccountFundingOverdueAssetDTO();
        BeanUtils.copyProperties(entity, dto);
        return new JsonResult(SystemCode.SUCCESS, dto);
    }

    /**
     * 20.获取资金方逾期资产
     * 逾期明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public JsonResult listFunderOrderOverdue(Long uid, Integer currentPage, Integer pageSize, Integer orderStatus) {
        if (null == currentPage || currentPage <= 1) {
            currentPage = 1;
        }
        if (null == pageSize || pageSize <= 1) {
            pageSize = 1;
        }
        currentPage--;
        Pageable pageable = new PageRequest(currentPage, pageSize);
        AccountFundingOverdueAssetQuery query = new AccountFundingOverdueAssetQuery();
        query.setUid(uid);
        query.setOrderStatus(orderStatus);
        Page<AccountFundingOverdueAsset> page = accountFundingOverdueAssetSlave.findAll(query, pageable);

        List<AccountFundingOverdueAssetListDTO> dtoList = new ArrayList<>();
        for (AccountFundingOverdueAsset entity : page.getContent()) {
            AccountFundingOverdueAssetListDTO dto = new AccountFundingOverdueAssetListDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setInvestType(entity.getInvestWay());
            dto.setInvestMonth(entity.getInvestDeadline());
            dto.setPayedAmount(entity.getRepayAmount());
            dto.setUnpayAmount(entity.getDueAmount());
            dto.setOverdueDays(entity.getOverdueDay());
            dto.setPayType(entity.getRepayWay());
            dtoList.add(dto);
        }
        return new PageJsonResult(SystemCode.SUCCESS, dtoList, page.getTotalElements());
    }

    /**
     * 21.获取资金方逾期资产
     * 逾期明细列表
     * 根据订单的Id,去查看详情
     *
     * @param orderId
     * @return
     */
    @Override
    public JsonResult getFunderOrderOverdueDetail(Long uid, Long orderId) {
        //查询相同数据表，返回结果相同
        return getFunderOrderInvestmentDetail(uid, orderId);
    }

    /**
     * 22.供应商获取账户资产
     * 1、可用余额
     * 2、资产总额
     * 3、冻结资产
     *
     * @return
     */
    @Override
    public JsonResult getSupplierAssetAccount(Long uid) {
        Account entity = accountSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        AccountSupplierDTO dto = new AccountSupplierDTO();
        BeanUtils.copyProperties(entity, dto);
        return new JsonResult(SystemCode.SUCCESS, dto);
    }

    /**
     * 23.获取供应商销售账户
     * 涉及到
     * 1、累计营收
     * 2、累计销售数量
     * 3、待发货数量
     *
     * @return
     */
    @Override
    public JsonResult getSupplierOrderSellAssets(Long uid) {
        AccountSupplierSell entity = accountSupplierSellSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        AccountSupplierSellDTO dto = new AccountSupplierSellDTO();
        BeanUtils.copyProperties(entity, dto);
        return new JsonResult(SystemCode.SUCCESS, dto);
    }

    /**
     * 24.获取供应商销售账户下
     * 销售明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public JsonResult listSupplierOrderSell(Long uid, Integer currentPage, Integer pageSize, Integer orderStatus) {
        if (null == currentPage || currentPage <= 1) {
            currentPage = 1;
        }
        if (null == pageSize || pageSize <= 1) {
            pageSize = 1;
        }
        currentPage--;
        Pageable pageable = new PageRequest(currentPage, pageSize);
        OrderSupplierQuery query = new OrderSupplierQuery();
        query.setOrderStatus(orderStatus);
        Page<OrderSupplier> page = orderSupplierSlave.findAll(query, pageable);

        List<AccountSupplierSellListDTO> dtoList = new ArrayList<>();
        for (OrderSupplier entity : page.getContent()) {
            AccountSupplierSellListDTO dto = new AccountSupplierSellListDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setProductMemory(entity.getProductRandomMemory());
            dto.setRenterId(entity.getMerchantId());
            dto.setRenterName(entity.getMerchantName());
            dto.setUserId(entity.getUid());
            dto.setUserName(entity.getReceiverName());
            dto.setDeliverTime(entity.getExpressTime());
            dto.setAssociatedOrderId(entity.getPartnerOrderId());
            dtoList.add(dto);
        }
        return new PageJsonResult(SystemCode.SUCCESS, dtoList, page.getTotalElements());
    }

    /**
     * 25.出租方获取账户资产
     * 1、可用余额
     * 2、资产总额
     * 3、冻结资产
     * 4、保证金
     * 5、代收金额
     * 6、本月应收
     *
     * @return
     */
    @Override
    public JsonResult getHirerAssetAccount(Long uid) {


        Account entity = accountSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        AccountLessorDTO dto = new AccountLessorDTO();
        BeanUtils.copyProperties(entity, dto);
        return new JsonResult(SystemCode.SUCCESS, entity);
    }

    /**
     * 26.获取出租方实物资产账户
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
    @Override
    public JsonResult getHirerOrderMaterialAssets(Long uid) {
        AccountLessorMatterAssetView entity = accountLessorMatterAssetViewSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        AccountLessorMatterAssetViewDTO dto = new AccountLessorMatterAssetViewDTO();
        BeanUtils.copyProperties(entity, dto);
        return new JsonResult(SystemCode.SUCCESS, dto);
    }

    /**
     * 27.获取出租方实物资产账户下
     * 实物明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public JsonResult listHirerOrderMaterial(Long uid, Integer currentPage, Integer pageSize, String associatedOrderStatus) {
        if (null == currentPage || currentPage <= 1) {
            currentPage = 1;
        }
        if (null == pageSize || pageSize <= 1) {
            pageSize = 1;
        }
        currentPage--;
        Pageable pageable = new PageRequest(currentPage, pageSize);
        AccountLessorMatterAssetQuery query = new AccountLessorMatterAssetQuery();
        query.setUid(uid);
        if (null != associatedOrderStatus) {
            query.setAssociatedOrderStatus(associatedOrderStatus);
        }

        Page<AccountLessorMatterAsset> page = accountLessorMatterAssetSlave.findAll(query, pageable);

        List<AccountLessorMatterAssetListDTO> dtoList = new ArrayList<>();
        for (AccountLessorMatterAsset entity : page.getContent()) {
            AccountLessorMatterAssetListDTO dto = new AccountLessorMatterAssetListDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setUserId(entity.getRenterId());
            dto.setUserName(entity.getRenterName());
            dto.setDeliverTime(entity.getRentTime());
            dto.setPurchaseAmount(entity.getMatterPrice());
            dto.setRentMonth(entity.getRentDeadline());
            dto.setTotalRentAmount(entity.getTotalAmount());
            dtoList.add(dto);
        }
        return new PageJsonResult(SystemCode.SUCCESS, dtoList, page.getTotalElements());
    }

    /**
     * 28.查看出租方分期收款计划
     *
     * @return
     */
    @Override
    public JsonResult getHirerOrderMaterialDetail(Long uid, Long orderId) {
        if (null == orderId) {
            return null;
        }
        AccountRenterRepayQuery query = new AccountRenterRepayQuery();
        query.setOrderId(orderId);
        List<AccountRenterRepay> accountRenterRepays = accountRenterRepaySlave.findAll(query);
        return new JsonResult(SystemCode.SUCCESS, CollectionUtils.isEmpty(accountRenterRepays) ? Collections.EMPTY_LIST : accountRenterRepays);
    }

    /**
     * 29.获取出租方逾期资产账户
     * 涉及到
     * 1、逾期金额合计
     * 2、逾期笔数
     * 3、逾期率
     *
     * @return
     */
    @Override
    public JsonResult getHirerOrderOverdueAssets(Long uid) {
        AccountLessorOverdueAsset entity = accountLessorOverdueAssetSlave.findOne(uid);
        if (null == entity) {
            return new JsonResult(SystemCode.SUCCESS, null);
        }
        AccountLessorOverdueAssetDTO dto = new AccountLessorOverdueAssetDTO();
        BeanUtils.copyProperties(entity, dto);
        return new JsonResult(SystemCode.SUCCESS, dto);
    }

    /**
     * 30.获取出租方逾期资产账户下
     * 逾期明细列表
     * 涉及到分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public JsonResult listHirerOrderOverdue(Long uid, Integer currentPage, Integer pageSize, Integer orderStatus) {
        if (null == currentPage || currentPage <= 1) {
            currentPage = 1;
        }
        if (null == pageSize || pageSize <= 1) {
            pageSize = 1;
        }
        currentPage--;
        Pageable pageable = new PageRequest(currentPage, pageSize);
        AccountLessorOverdueAssetQuery query = new AccountLessorOverdueAssetQuery();
        query.setUid(uid);
        query.setOrderStatus(orderStatus);
        Page<AccountLessorOverdueAsset> page = accountLessorOverdueAssetSlave.findAll(query, pageable);

        List<AccountLessorOverdueAssetListDTO> dtoList = new ArrayList<>();
        for (AccountLessorOverdueAsset entity : page.getContent()) {
            AccountLessorOverdueAssetListDTO dto = new AccountLessorOverdueAssetListDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setInvestType(entity.getInvestWay());
            dto.setOverdueDays(entity.getOverdueDay());
            dto.setPayType(entity.getRepayWay());
            dto.setOrderStatus(entity.getOverdueStatus());
            dto.setInvestMonth(entity.getInvestDeadline());
            dtoList.add(dto);
        }
        return new PageJsonResult(SystemCode.SUCCESS, dtoList, page.getTotalElements());

    }

    /**
     * 31.获取出租方逾期资产账户下
     * 逾期明细列表
     *
     * @return
     */
    @Override
    public JsonResult getHirerOrderOverdueDetail(Long uid, Long orderId) {
        return getHirerOrderMaterialDetail(uid, orderId);
    }

    /**
     * 获取租赁商账户资产
     *
     * @param uid
     * @return
     */
    @Override
    public Account getAccountRenter(Long uid) {
        if (null == uid) {
            return null;
        }

        Optional<Account> result = accountSlave.findById(uid);
        if (!result.isPresent()) {
            return null;
        }
        return result.get();
    }

    /**
     * 获取租赁商账户资产下的租赁明细表
     *
     * @param uid
     * @param associatedOrderStatus
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Page<AccountRenterRent> getAccountRenterRent(Long uid, String associatedOrderStatus, Integer currentPage, Integer pageSize) {
        if (null == uid || null == currentPage || null == pageSize) {
            return null;
        }

        AccountRenterRentQuery query = new AccountRenterRentQuery();
        query.setUid(uid);
        if (null != associatedOrderStatus) {
            query.setAssociatedOrderStatus(associatedOrderStatus);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "ctime");
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);

        Page<AccountRenterRent> rents = accountRenterRentSlave.findAll(query, pageRequest);
        return rents;
    }

    /**
     * 查询租赁商关联的供应商
     *
     * @param uid
     * @return
     */
    @Override
    public List<AccountRenterAppointSupplier> listAccountRenterAppointSupplier(Long uid) {
        if (null == uid) {
            return Collections.EMPTY_LIST;
        }
        AccountRenterAppointSupplierQuery query = new AccountRenterAppointSupplierQuery();
        query.setUid(uid);
        List<AccountRenterAppointSupplier> result = accountRenterAppointSupplierSlave.findAll(query);
        return CollectionUtils.isEmpty(result) ? Collections.EMPTY_LIST : result;
    }

    /**
     * 获取租赁商订单融资账户
     *
     * @param uid
     * @return
     */
    @Override
    public AccountRenterFinancingMachine getAccountRenterFinancingMachine(Long uid) {
        if (null == uid) {
            return null;
        }
        Optional<AccountRenterFinancingMachine> result = accountRenterFinancingMachineSlave.findById(uid);
        if (!result.isPresent()) {
            return null;
        }
        return result.get();
    }

    /**
     * 用户注册时，订单融资初始化
     *
     * @param uid
     * @return
     */
    @Override
    public AccountRenterFinancingMachine saveAccountRenterFinancingMachine(Long uid) {
        if (null == uid) {
            return null;
        }

        AccountRenterFinancingMachine machine = new AccountRenterFinancingMachine();
        machine.setUid(uid);
        machine.setMonthShouldRepay(BigDecimal.ZERO);
        machine.setOrderFinancing(BigDecimal.ZERO);
        machine.setUnsettledFinancing(BigDecimal.ZERO);
        machine.setSettleFinancing(BigDecimal.ZERO);
        return accountRenterFinancingMachineMaster.save(machine);
    }

    /**
     * 更新订单融资的统计信息
     *
     * @param machine
     * @return
     */
    @Override
    public AccountRenterFinancingMachine updateAccountRenterFinancingMachine(AccountRenterFinancingMachine machine) {
        if (null == machine || null == machine.getUid()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != machine.getMonthShouldRepay()) {
            updateFields.add("monthShouldRepay");
        }

        if (null != machine.getOrderFinancing()) {
            updateFields.add("orderFinancing");
        }

        if (null != machine.getSettleFinancing()) {
            updateFields.add("settleFinancing");
        }

        if (null != machine.getUnsettledFinancing()) {
            updateFields.add("unsettledFinancing");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountRenterFinancingMachineMaster.updateById(machine, machine.getUid(), array);
        return machine;
    }

    /**
     * 我是租赁商订单融资账户订单融资列表
     *
     * @param uid
     * @param orderStatus
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Page<AccountRenterFinancing> getAccountRenterFinancing(Long uid, Integer repaymentStatus, Integer orderStatus, Integer currentPage, Integer pageSize) {
        if (null == uid || null == currentPage || null == pageSize) {
            return null;
        }

        AccountRenterFinancingQuery query = new AccountRenterFinancingQuery();
        query.setUid(uid);
        query.setOrderStatus(orderStatus);
        query.setRepaymentStatus(repaymentStatus);
        Sort sort = new Sort(Sort.Direction.DESC, "ctime");
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);

        return accountRenterFinancingSlave.findAll(query, pageRequest);
    }

    /**
     * 我是租赁商订单融资账户订单融资列表查看订单详情
     *
     * @param orderId
     * @return
     */
    @Override
    public List<AccountRenterRepay> listAccountRenterRepay(Long orderId) {
        if (null == orderId) {
            return null;
        }

        AccountRenterRepayQuery query = new AccountRenterRepayQuery();
        query.setOrderId(orderId);
        List<AccountRenterRepay> accountRenterRepays = accountRenterRepaySlave.findAll(query);
        return CollectionUtils.isEmpty(accountRenterRepays) ? Collections.EMPTY_LIST : accountRenterRepays;
    }

    /**
     * 获取租赁商租赁账户
     *
     * @param uid
     * @return
     */
    @Override
    public AccountRenterRentMachine getAccountRenterRentMachine(Long uid) {
        if (null == uid) {
            return null;
        }
        Optional<AccountRenterRentMachine> result = accountRenterRentMachineSlave.findById(uid);
        if (!result.isPresent()) {
            return null;
        }
        return result.get();
    }

    /**
     * 10.我是租赁商租赁账户租赁明细列表
     *
     * @param uid
     * @param payStatus
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Page<AccountRenterRentDetail> getAccountRenterRentDetail(Long uid, Integer payStatus, Integer currentPage, Integer pageSize) {
        if (null == uid || null == currentPage || null == pageSize) {
            return null;
        }

        AccountRenterRentDetailQuery query = new AccountRenterRentDetailQuery();
        query.setUid(uid);
        if (null != payStatus) {
            query.setPayStatus(payStatus);
        }
        Sort sort = new Sort(Sort.Direction.DESC, "ctime");
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);


        return accountRenterRentDetailSlave.findAll(query, pageRequest);
    }

    /**
     * 12.获取租赁商订单逾期账户
     *
     * @param uid
     * @return
     */
    @Override
    public AccountRenterOverdueAsset getAccountRenterOverdueAsset(Long uid) {
        if (null == uid) {
            return null;
        }
        Optional<AccountRenterOverdueAsset> result = accountRenterOverdueAssetSlave.findById(uid);
        if (!result.isPresent()) {
            return null;
        }
        return result.get();
    }

    /**
     * 13.我是租赁商订单逾期账户下租赁明细列表
     *
     * @param uid
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Page<AccountRenterOverdueDetail> getAccountRenterOverdueDetail(Long uid, Integer currentPage, Integer pageSize) {
        if (null == uid || null == currentPage || null == pageSize) {
            return null;
        }

        AccountRenterOverdueQuery query = new AccountRenterOverdueQuery();
        query.setUid(uid);
        Sort sort = new Sort(Sort.Direction.DESC, "ctime");
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize, sort);


        return accountRenterOverdueDetailSlave.findAll(query, pageRequest);
    }

    /**
     * 保存账户的接口
     *
     * @param account
     * @return
     */
    @Override
    public Account saveAccount(Account account) {
        if (null == account) {
            return null;
        }

        return accountMaster.saveAndFlush(account);
    }

    /**
     * 保存租赁商的账户资产下的租赁明细
     *
     * @param accountRenterRent
     * @return
     */
    @Override
    public AccountRenterRent saveAccountRenterRent(AccountRenterRent accountRenterRent) {
        if (null == accountRenterRent) {
            return null;
        }
        if (null == accountRenterRent.getId()) {
            accountRenterRent.setId(idGlobal.getSeqId(AccountRenterRent.class));
        }

        return accountRenterRentMaster.saveAndFlush(accountRenterRent);
    }

    /**
     * 保存accountRenterRentDetail
     *
     * @param accountRenterRentDetail
     * @return
     */
    @Override
    public AccountRenterRentDetail saveAccountRenterRentDetail(AccountRenterRentDetail accountRenterRentDetail) {
        if (null == accountRenterRentDetail) {
            return null;
        }
        if (null == accountRenterRentDetail.getId()) {
            accountRenterRentDetail.setId(idGlobal.getSeqId(AccountRenterRentDetail.class));
        }
        return accountRenterRentDetailMaster.saveAndFlush(accountRenterRentDetail);
    }

    /**
     * 保存AccountRenterRepay
     *
     * @param accountRenterRepays
     * @return
     */
    @Override
    public List<AccountRenterRepay> saveAccountRenterRepay(List<AccountRenterRepay> accountRenterRepays) {
        if (CollectionUtils.isEmpty(accountRenterRepays)) {
            return Collections.EMPTY_LIST;
        }

        return accountRenterRepayMaster.saveAll(accountRenterRepays);
    }

    /**
     * 操作账户的数据
     *
     * @param accountStatement
     * @return
     */
    @Override
    public AccountStatement saveAccountStatement(AccountStatement accountStatement) {
        if (null == accountStatement) {
            return null;
        }
        if (null == accountStatement.getId()) {
            accountStatement.setId(idGlobal.getSeqId(AccountRenterRentDetail.class));
        }

        return accountStatementMaster.saveAndFlush(accountStatement);
    }

    /**
     * 出租方(租户)账户
     * 由订单中心那边，调用，将相关信息插入到表account_renter_rent和account_lessor_matter_asset
     * 在出租方发货（审核通过）时调用，更新出租方账户、生成租赁商还款计划
     *
     * @return
     */
    @Override
    public OperationResult<Boolean> saveAccountLessorMatterAssetDetail(Long uid, Long orderId, Long orderTime, Long renterId, String renterName, String associatedOrderId,
                                                                       String productBrand, String productModel, String productColour, String productStorage, String productMemory,
                                                                       BigDecimal totalRentAccount, Integer monthNumber) {
        if (null == uid || null == orderId || null == associatedOrderId || StringUtils.isEmpty(productBrand) || StringUtils.isEmpty(productModel) ||
                StringUtils.isEmpty(productColour) || StringUtils.isEmpty(productStorage) || StringUtils.isEmpty(productMemory) || null == totalRentAccount || null == monthNumber) {
            return new OperationResult<>(BizErrorCode.PARAM_INFO_ERROR);
        }
        AccountLessorMatterAsset accountLessorMatterAsset = new AccountLessorMatterAsset();
        accountLessorMatterAsset.setTotalAmount(totalRentAccount);
        accountLessorMatterAsset.setRentDeadline(monthNumber);
        accountLessorMatterAsset.setId(idGlobal.getSeqId(AccountLessorMatterAsset.class));
        accountLessorMatterAsset.setOrderId(orderId);
        accountLessorMatterAsset.setRentTime(orderTime);
        accountLessorMatterAsset.setRenterId(renterId);
        accountLessorMatterAsset.setRenterName(renterName);
        accountLessorMatterAsset.setAssociatedOrderId(associatedOrderId);
        accountLessorMatterAsset.setUid(uid);

        accountLessorMatterAsset.setProductBrand(productBrand);
        accountLessorMatterAsset.setProductModel(productModel);
        accountLessorMatterAsset.setProductColour(productColour);
        accountLessorMatterAsset.setProductStorage(productStorage);
        accountLessorMatterAsset.setProductMemory(productMemory);
        accountLessorMatterAsset.setOrderStatus(1);

        accountLessorMatterAssetMaster.save(accountLessorMatterAsset);
        // 王栋调用过了
//        accountManageService.saveAccountRenterRepay(orderId, totalRentAccount, monthNumber);
        return new OperationResult<>(true);
    }

    /**
     * 资金方账户
     * 由订单中心那边，调用，将相关信息插入到表account_renter_rent和account_funding_finance_asset
     * 在资金方放款完成之后调用，操作资金方金融资产账户、生成租赁商还款计划
     *
     * @return
     */
    @Override
    public OperationResult<Boolean> saveAccountFundingFinanceAssetDetail(Long uid, Long orderId, Long orderTime, Long renterId, String renterName, String relevanceOrderId, BigDecimal totalRentAccount, Integer monthNumber) {
        if (null == uid || null == orderId || null == totalRentAccount || null == monthNumber) {
            return new OperationResult<>(BizErrorCode.PARAM_INFO_ERROR);
        }
        AccountFundingFinanceAsset accountFundingFinanceAsset = new AccountFundingFinanceAsset();
        accountFundingFinanceAsset.setId(idGlobal.getSeqId(AccountFundingFinanceAsset.class));
        accountFundingFinanceAsset.setInvestDeadline(monthNumber);
        accountFundingFinanceAsset.setInvestAmount(totalRentAccount);
        accountFundingFinanceAsset.setUid(uid);
        accountFundingFinanceAsset.setOrderId(orderId);
        accountFundingFinanceAsset.setOrderTime(orderTime);
        accountFundingFinanceAsset.setOrderStatus(1);
        accountFundingFinanceAsset.setInvestWay(1);
        accountFundingFinanceAsset.setRenterId(renterId);
        accountFundingFinanceAsset.setRenterName(renterName);
        accountFundingFinanceAsset.setAssociatedOrderId(relevanceOrderId);
        // todo 平均投资回报率填0
        accountFundingFinanceAsset.setAverageInvestReturnRate(new BigDecimal("0"));
        accountFundingFinanceAsset.setInvestReturnRate(new BigDecimal("0"));
        accountFundingFinanceAsset.setYieldRate(new BigDecimal("0"));
        accountFundingFinanceAssetMaster.save(accountFundingFinanceAsset);
        //王栋已经调用了
//        accountManageService.saveAccountRenterRepay(orderId, totalRentAccount, monthNumber);
        return new OperationResult<>(true);
    }

    /**
     * 保存AccountRenterFinancing
     *
     * @param accountRenterFinancing
     * @return
     */
    @Override
    public AccountRenterFinancing saveAccountRenterFinancing(AccountRenterFinancing accountRenterFinancing) {
        if (null == accountRenterFinancing) {
            return null;
        }
        if (null == accountRenterFinancing.getId()) {
            accountRenterFinancing.setId(idGlobal.getSeqId(AccountRenterRentDetail.class));
        }
        return accountRenterFinancingMaster.saveAndFlush(accountRenterFinancing);
    }

    /**
     * 根据主键查询AccountRenterRepay
     *
     * @param id
     * @return
     */
    @Override
    public AccountRenterRepay getAccountRenterRepay(Long id) {
        if (null == id) {
            return null;
        }
        Optional<AccountRenterRepay> accountRenterRepay = accountRenterRepaySlave.findById(id);

        if (accountRenterRepay.isPresent()) {
            return accountRenterRepay.get();
        }
        return null;
    }

    /**
     * 根据订单Id去租赁商订单表查询信息
     *
     * @param orderId
     * @return
     */
    @Override
    public AccountRenterFinancing getAccountRenterFinancing(Long orderId) {

        if (null == orderId) {
            return null;
        }
        AccountRenterFinancingQuery query = new AccountRenterFinancingQuery();
        query.setOrderId(orderId);

        return accountRenterFinancingSlave.findOne(query);
    }

    /**
     * 根据订单Id,查询资金方的订单信息
     *
     * @param orderId
     * @return
     */
    @Override
    public AccountFundingFinanceAsset getAccountFundingFinanceAsset(Long orderId) {
        if (null == orderId) {
            return null;
        }

        AccountFundingFinanceAssetQuery query = new AccountFundingFinanceAssetQuery();
        query.setOrderId(orderId);

        return accountFundingFinanceAssetSlave.findOne(query);
    }

    /**
     * @param accountRenterFinancing
     * @return
     */
    @Override
    public AccountRenterFinancing updateAccountRenterFinancing(AccountRenterFinancing accountRenterFinancing) {
        if (null == accountRenterFinancing.getId()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != accountRenterFinancing.getRepaymentStatus()) {
            updateFields.add("repaymentStatus");
        }
        if (null != accountRenterFinancing.getOrderStatus()) {
            updateFields.add("orderStatus");
        }
        if (null != accountRenterFinancing.getFinancingAmount()) {
            updateFields.add("financingAmount");
        }
        if (null != accountRenterFinancing.getFinancingPrincipalInterest()) {
            updateFields.add("financingPrincipalInterest");
        }

        if (null != accountRenterFinancing.getFinancingInterest()) {
            updateFields.add("financingInterest");
        }

        if (null != accountRenterFinancing.getSettlePrincipalInterest()) {
            updateFields.add("settlePrincipalInterest");
        }
        if (null != accountRenterFinancing.getSettleInterest()) {
            updateFields.add("settleInterest");
        }
        if (null != accountRenterFinancing.getUnsettlePrincipalInterest()) {
            updateFields.add("unsettlePrincipalInterest");
        }
        if (null != accountRenterFinancing.getUnsettleInterest()) {
            updateFields.add("unsettleInterest");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountRenterFinancingMaster.updateById(accountRenterFinancing, accountRenterFinancing.getId(), array);
        return accountRenterFinancing;
    }

    /**
     * 还款后更新状态
     *
     * @param accountFundingFinanceAsset
     * @return
     */
    @Override
    public AccountFundingFinanceAsset updateAccountFundingFinanceAsset(AccountFundingFinanceAsset accountFundingFinanceAsset) {
        if (null == accountFundingFinanceAsset.getId()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != accountFundingFinanceAsset.getOrderStatus()) {
            updateFields.add("orderStatus");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountFundingFinanceAssetMaster.updateById(accountFundingFinanceAsset, accountFundingFinanceAsset.getId(), array);
        return accountFundingFinanceAsset;
    }

    /**
     * 更新还款后的状态
     *
     * @param accountRenterRepay
     * @return
     */
    @Override
    public AccountRenterRepay updateAccountRenterRepay(AccountRenterRepay accountRenterRepay) {

        if (null == accountRenterRepay.getId()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != accountRenterRepay.getOrderStatus()) {
            updateFields.add("orderStatus");
        }

        if (null != accountRenterRepay.getWithhold()) {
            updateFields.add("withhold");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountRenterRepayMaster.updateById(accountRenterRepay, accountRenterRepay.getId(), array);
        return accountRenterRepay;
    }

    /**
     * 操作出租方的账户
     *
     * @param orderId
     * @return
     */
    @Override
    public AccountLessorMatterAsset getAccountLessorMatterAsset(Long orderId) {

        if (null == orderId) {
            return null;
        }

        AccountLessorMatterAssetQuery query = new AccountLessorMatterAssetQuery();
        query.setOrderId(orderId);

        return accountLessorMatterAssetSlave.findOne(query);
    }

    /**
     * 更新AccountLessorMatterAsset
     *
     * @param accountLessorMatterAsset
     * @return
     */
    @Override
    public AccountLessorMatterAsset updateAccountLessorMatterAsset(AccountLessorMatterAsset accountLessorMatterAsset) {
        if (null == accountLessorMatterAsset.getId()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != accountLessorMatterAsset.getOrderStatus()) {
            updateFields.add("orderStatus");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountLessorMatterAssetMaster.updateById(accountLessorMatterAsset, accountLessorMatterAsset.getId(), array);
        return accountLessorMatterAsset;
    }

    /**
     * 根据这个用户uid，计算出所有他的融资金额
     *
     * @param uid
     * @return
     */
    @Override
    public RenterFinanceStatistics getorderFinancing(Long uid) {

        if (null == uid) {
            return null;
        }
        AccountRenterFinancingQuery query = new AccountRenterFinancingQuery();
        query.setUid(uid);
        List<AccountRenterFinancing> financingList = accountRenterFinancingSlave.findAll(query);
        if (CollectionUtils.isEmpty(financingList)) {
            return null;
        }
        //订单融资金额
        BigDecimal financingAmount = financingList.stream().map(AccountRenterFinancing::getFinancingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //已偿还本金
        BigDecimal settlePrincipalInterests = financingList.stream().map(AccountRenterFinancing::getSettlePrincipalInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        //已偿还利息
        BigDecimal settleInterests = financingList.stream().map(AccountRenterFinancing::getSettleInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        //已偿还本息
        BigDecimal settleFinancing = settlePrincipalInterests.add(settleInterests);
        //未偿还本金
        BigDecimal unsettlePrincipalInterest = financingList.stream().map(AccountRenterFinancing::getUnsettlePrincipalInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        //未偿还利息
        BigDecimal unsettleInterest = financingList.stream().map(AccountRenterFinancing::getUnsettleInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unsettledFinancing = unsettlePrincipalInterest.add(unsettleInterest);
        RenterFinanceStatistics renterFinanceStatistics = new RenterFinanceStatistics();
        renterFinanceStatistics.setRenterFinanceStatistics(financingAmount, settleFinancing, unsettledFinancing);
        return renterFinanceStatistics;
    }

    /**
     * 根据用户的Uid,查询本月应还的金额
     *
     * @param uid
     * @param firstDayOfMonth
     * @param lastDayOfMonth
     * @return
     */
    @Override
    public List<AccountRenterRepay> listAccountRenterRepay(Long uid, Integer firstDayOfMonth, Integer lastDayOfMonth) {
        if (null == uid || null == firstDayOfMonth || null == lastDayOfMonth) {
            return Collections.EMPTY_LIST;
        }
        AccountRenterRepayQuery query = new AccountRenterRepayQuery();
        query.setLastDayOfMonth(lastDayOfMonth);
        query.setFirstDayOfMonth(firstDayOfMonth);
        query.setUid(uid);
        //查询未付款
        query.setWithhold(1);
        List<AccountRenterRepay> renterRepays = accountRenterRepayMaster.findAll(query);

        return CollectionUtils.isEmpty(renterRepays) ? Collections.EMPTY_LIST : renterRepays;
    }

    /**
     * 保存AccountRenterRentMachine操作
     *
     * @param machine
     * @return
     */
    @Override
    public AccountRenterRentMachine saveAccountRenterRentMachine(AccountRenterRentMachine machine) {
        if (null == machine || null == machine.getUid()) {
            return null;
        }

        return accountRenterRentMachineMaster.save(machine);
    }

    /**
     * 根据订单的ID,获取租机订单
     *
     * @param orderId
     * @return
     */
    @Override
    public AccountRenterRent getAccountRenterRent(Long orderId) {
        if (null == orderId) {
            return null;
        }

        AccountRenterRentQuery query = new AccountRenterRentQuery();
        query.setOrderId(orderId);

        return accountRenterRentMaster.findOne(query);
    }

    @Override
    public AccountRenterRent updateAccountRenterRent(AccountRenterRent accountRenterRent) {

        if (null == accountRenterRent || null == accountRenterRent.getId()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != accountRenterRent.getReceivedAccount()) {
            updateFields.add("receivedAccount");
        }
        if (null != accountRenterRent.getDueInAccount()) {
            updateFields.add("dueInAccount");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountRenterRentMaster.updateById(accountRenterRent, accountRenterRent.getId(), array);
        return accountRenterRent;
    }

    /**
     * 根据订单的id查询租机的详情
     *
     * @param orderId
     * @return
     */
    @Override
    public AccountRenterRentDetail getAccountRenterRentDetail(Long orderId) {
        if (null == orderId) {
            return null;
        }

        AccountRenterRentDetailQuery query = new AccountRenterRentDetailQuery();
        query.setOrderId(orderId);

        return accountRenterRentDetailSlave.findOne(query);
    }

    /**
     * 更新AccountRenterRentDetail
     *
     * @param accountRenterRentDetail
     * @return
     */
    @Override
    public AccountRenterRentDetail updateAccountRenterRentDetail(AccountRenterRentDetail accountRenterRentDetail) {
        if (null == accountRenterRentDetail || null == accountRenterRentDetail.getId()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != accountRenterRentDetail.getPayedAccount()) {
            updateFields.add("payedAccount");
        }
        if (null != accountRenterRentDetail.getUnpayedAccount()) {
            updateFields.add("unpayedAccount");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountRenterRentDetailMaster.updateById(accountRenterRentDetail, accountRenterRentDetail.getId(), array);
        return accountRenterRentDetail;
    }

    /**
     * 统计租机订单下的数据
     *
     * @param uid
     * @return
     */
    @Override
    public RentMachineStatistics getRentMachineStatistics(Long uid) {
        if (null == uid) {
            return null;
        }
        AccountRenterRentDetailQuery query = new AccountRenterRentDetailQuery();
        query.setUid(uid);

        List<AccountRenterRentDetail> accountRenterRentDetailList = accountRenterRentDetailSlave.findAll(query);
        //BigDecimal financingAmount = financingList.stream().map(AccountRenterFinancing::getFinancingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //计算租赁总额
        BigDecimal rentAccount = accountRenterRentDetailList.stream().map(AccountRenterRentDetail::getTotalRentAccount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //计算已付的总额
        BigDecimal payedAccount = accountRenterRentDetailList.stream().map(AccountRenterRentDetail::getPayedAccount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //计算未付的总额
        BigDecimal unpayAccount = accountRenterRentDetailList.stream().map(AccountRenterRentDetail::getUnpayedAccount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //计算累计应付的总额
        BigDecimal totalPayableAccount = payedAccount.add(unpayAccount);

        RentMachineStatistics rentMachineStatistics = new RentMachineStatistics();
        rentMachineStatistics.setRentMachineStatistics(rentAccount, totalPayableAccount, payedAccount, unpayAccount);
        return rentMachineStatistics;
    }

    /**
     * 更新AccountRenterRentMachine
     *
     * @param accountRenterRentMachine
     * @return
     */
    @Override
    public AccountRenterRentMachine updateAccountRenterRentMachine(AccountRenterRentMachine accountRenterRentMachine) {
        if (null == accountRenterRentMachine || null == accountRenterRentMachine.getUid()) {
            return null;
        }
        List<String> updateFields = Lists.newArrayList();
        if (null != accountRenterRentMachine.getRentAccount()) {
            updateFields.add("rentAccount");
        }
        if (null != accountRenterRentMachine.getTotalPayableAccount()) {
            updateFields.add("totalPayableAccount");
        }
        if (null != accountRenterRentMachine.getPayedAccount()) {
            updateFields.add("payedAccount");
        }
        if (null != accountRenterRentMachine.getUnpayAccount()) {
            updateFields.add("unpayAccount");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountRenterRentMachineMaster.updateById(accountRenterRentMachine, accountRenterRentMachine.getUid(), array);
        return accountRenterRentMachine;
    }

    /**
     * 保存AccountRenterOverdueAsset
     *
     * @param accountRenterOverdueAsset
     * @return
     */
    @Override
    public AccountRenterOverdueAsset saveAccountRenterOverdueAsset(AccountRenterOverdueAsset accountRenterOverdueAsset) {
        if (null == accountRenterOverdueAsset || null == accountRenterOverdueAsset.getUid()) {
            return null;
        }


        return accountRenterOverdueAssetMaster.save(accountRenterOverdueAsset);
    }

    /**
     * 根据用户的uid和订单的状态，去查询租赁商租机逾期的订单
     *
     * @param uid
     * @param overdue
     * @return
     */
    @Override
    public List<AccountRenterRentDetail> listAccountRenterRentDetail(Long uid, PayStatusEnum overdue) {
        if (null == uid || null == overdue) {
            return Collections.EMPTY_LIST;
        }

        AccountRenterRentDetailQuery query = new AccountRenterRentDetailQuery();
        query.setUid(uid);
        query.setPayStatus(overdue.getCode());
        List<AccountRenterRentDetail> list = accountRenterRentDetailSlave.findAll(query);
        return CollectionUtils.isNotEmpty(list) ? list : Collections.EMPTY_LIST;
    }

    /**
     * 根据用户的uid和订单的状态，去查询租赁商融资购机逾期的订单
     *
     * @param uid
     * @param overdue
     * @return
     */
    @Override
    public List<AccountRenterFinancing> listAccountRenterFinancing(Long uid, PayStatusEnum overdue) {
        if (null == uid || null == overdue) {
            return Collections.EMPTY_LIST;
        }

        AccountRenterFinancingQuery query = new AccountRenterFinancingQuery();
        query.setUid(uid);
        query.setRepaymentStatus(overdue.getCode());
        List<AccountRenterFinancing> list = accountRenterFinancingSlave.findAll(query);
        return CollectionUtils.isNotEmpty(list) ? list : Collections.EMPTY_LIST;
    }

    /**
     * 批量保存逾期的订单
     *
     * @param accountRenterOverdueDetails
     * @return
     */
    @Override
    public List<AccountRenterOverdueDetail> saveAccountRenterOverdueDetails(List<AccountRenterOverdueDetail> accountRenterOverdueDetails) {
        if (CollectionUtils.isEmpty(accountRenterOverdueDetails)) {
            return Collections.EMPTY_LIST;
        }
        //新的订单Id集合
        List<Long> newOrderIds = new ArrayList<>();
        accountRenterOverdueDetails.forEach(accountRenterOverdueDetail -> {
            newOrderIds.add(accountRenterOverdueDetail.getOrderId());
        });
        AccountRenterOverdueDetailQuery query = new AccountRenterOverdueDetailQuery();
        query.setOrderIds(newOrderIds);
        List<AccountRenterOverdueDetail> acounts = accountRenterOverdueDetailMaster.findAll(query);
        //获取已经存在的订单Id的集合
        List<Long> oldOrderIds = new ArrayList<>();
        acounts.forEach(accountRenterOverdueDetail -> {
            oldOrderIds.add(accountRenterOverdueDetail.getOrderId());
        });
        //需要去掉已经存在的数据

        Iterator<AccountRenterOverdueDetail> iterator = accountRenterOverdueDetails.iterator();

        while (iterator.hasNext()) {
            AccountRenterOverdueDetail detail = iterator.next();
            oldOrderIds.forEach(oldOrderId -> {
                if (detail.getOrderId().equals(oldOrderId)) {
                    iterator.remove();
                }
            });
        }


        if (CollectionUtils.isEmpty(accountRenterOverdueDetails)) {
            return Collections.EMPTY_LIST;
        }
        return accountRenterOverdueDetailMaster.saveAll(accountRenterOverdueDetails);
    }

    /**
     * 根据UID查询到账户
     *
     * @param uid
     * @return
     */
    @Override
    public Account getAccount(Long uid) {
        if (null == uid) {
            return null;
        }

        return accountMaster.findOne(uid);
    }

    /**
     * 更新Account
     *
     * @param acc
     * @return
     */
    @Override
    public Account updateAccount(Account acc) {
        if (null == acc || null == acc.getUid()) {
            return null;
        }

        List<String> updateFields = Lists.newArrayList();
        if (null != acc.getTotalAssets()) {
            updateFields.add("totalAssets");
        }
        if (null != acc.getUseableAmount()) {
            updateFields.add("useableAmount");
        }
        if (null != acc.getFrozenAssets()) {
            updateFields.add("frozenAssets");
        }
        if (null != acc.getMarginBalance()) {
            updateFields.add("marginBalance");
        }

        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);

        accountMaster.updateById(acc, acc.getUid(), array);

        return acc;
    }


}
