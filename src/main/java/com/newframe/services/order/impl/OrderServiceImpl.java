package com.newframe.services.order.impl;

import com.google.common.collect.Lists;
import com.newframe.common.exception.AccountOperationException;
import com.newframe.controllers.JsonResult;
import com.newframe.controllers.PageJsonResult;
import com.newframe.dto.OperationResult;
import com.newframe.dto.common.ExpressInfo;
import com.newframe.dto.order.request.*;
import com.newframe.dto.order.response.*;
import com.newframe.entity.account.Account;
import com.newframe.entity.order.*;
import com.newframe.entity.user.*;
import com.newframe.enums.SystemCode;
import com.newframe.enums.order.*;
import com.newframe.repositories.dataMaster.order.*;
import com.newframe.repositories.dataQuery.order.*;
import com.newframe.repositories.dataSlave.account.AccountFundingFinanceAssetSlave;
import com.newframe.repositories.dataSlave.order.*;
import com.newframe.repositories.dataSlave.user.ProductLessorSlave;
import com.newframe.repositories.dataSlave.user.ProductSupplierSlave;
import com.newframe.repositories.dataSlave.user.UserFunderSlave;
import com.newframe.services.block.BlockChainService;
import com.newframe.services.common.AliossService;
import com.newframe.services.common.CommonService;
import com.newframe.services.http.OkHttpService;
import com.newframe.services.order.OrderBaseService;
import com.newframe.services.order.OrderBlockChainService;
import com.newframe.services.order.OrderService;
import com.newframe.services.userbase.UserHirerService;
import com.newframe.services.userbase.UserRentMerchantService;
import com.newframe.services.userbase.UserSupplierService;
import com.newframe.utils.log.GwsLogger;
import com.newframe.utils.query.QueryToSpecification;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租赁商订单
 *
 * @author kfm
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderRenterMaser orderRenterMaser;
    @Autowired
    OrderRenterSlave orderRenterSlave;

    @Autowired
    OrderFunderMaser orderFunderMaser;
    @Autowired
    OrderFunderSlave orderFunderSlave;

    @Autowired
    OrderHirerMaser orderHirerMaser;
    @Autowired
    OrderHirerSlave orderHirerSlave;

    @Autowired
    OrderFunderEvidenceMaster orderFunderEvidenceMaster;
    @Autowired
    OrderFunderEvidenceSlave orderFunderEvidenceSlave;

    @Autowired
    OrderSuppMaster orderSuppMaster;
    @Autowired
    OrderSupplierSlave orderSupplierSlave;

    @Autowired
    ExpressCompanyMaster expressCompanyMaster;
    @Autowired
    ExpressCompanySlave expressCompanySlave;

    @Autowired
    HirerDeliverMaster hirerDeliverMaster;
    @Autowired
    HirerDeliverSlave hirerDeliverSlave;

    @Autowired
    LessorProductPriceSlave lessorProductPriceSlave;

    @Autowired
    OrderRenterAccountSlave orderRenterAccountSlave;
    @Autowired
    ProductSupplierSlave productSupplierSlave;
    @Autowired
    ProductLessorSlave productLessorSlave;

    @Autowired
    CommonService commonService;
    @Autowired
    OkHttpService okHttpService;
    @Autowired
    private AliossService aliossService;
    @Autowired
    UserSupplierService userSupplierService;
    @Autowired
    UserHirerService userHirerService;
    @Autowired
    UserRentMerchantService userRentMerchantService;
    @Autowired
    AccountFundingFinanceAssetSlave accountFundingFinanceAssetSlave;

    @Autowired
    OrderBlockChainService orderBlockChainService;
    @Autowired
    OrderBaseService orderBaseService;

    @Autowired
    OrderAccountOperationServiceImpl accountOperation;

    @Autowired
    BlockChainService blockChainService;

    @Autowired
    UserFunderSlave userFunderSlave;

    @Value("${order.financing.max.times}")
    private Integer maxOrderFinancingTimes;
    @Value("${order.renting.max.times}")
    private Integer maxOrderRentingTimes;
    @Value("${api.basetool.bucket}")
    private String bucket;
    @Value("${api.besetool.service}")
    private String baseUrl;
    @Value("${api.basetool.singleFileUpload}")
    private String fileUploadUrl;
    @Value("${residual.value.protection.scheme}")
    private BigDecimal residualValue;

    @Override
    public JsonResult getRenterOrder(QueryOrderDTO param, Long uid) {
        if (null == param.getPageSize() || null == param.getCurrentPage()) {
            return new JsonResult(SystemCode.NO_PAGE_PARAM);
        }
        // 查询排序规则
        Sort sort;
        if (param.getSort() == null || OrderSort.DESC.getValue().equals(param.getSort())) {
            sort = new Sort(Sort.Direction.DESC, OrderRenter.CTIME);
        } else {
            sort = new Sort(Sort.Direction.ASC, OrderRenter.CTIME);
        }
        // 设置查询条件
        OrderRenterQuery orderRenterQuery = new OrderRenterQuery();
        // 只查询此会话租赁商的订单
        orderRenterQuery.setRenterId(uid);
        orderRenterQuery.setDeleteStatus(OrderRenter.NO_DELETE_STATUS);
        if (null != param.getOrderStatus()) {
            orderRenterQuery.setOrderStatuses(OrderRenterStatus.getStatuses(param.getOrderStatus()));
        }
        // 设置分页参数
        Pageable pageable = PageRequest.of(param.getCurrentPage() - 1, param.getPageSize(), sort);
        // 包装查询参数
        Specification specification = new QueryToSpecification(orderRenterQuery);
        Page<OrderRenter> orderRenterPage = orderRenterSlave.findAll(specification, pageable);
        // 封装DTO
        List<OrderRenter> orderRenters = orderRenterPage.getContent();
        List<OrderRenterDTO> orderRenterDTOS = new ArrayList<>();
        for (OrderRenter orderRenter : orderRenters) {
            OrderRenterDTO orderRenterDTO = new OrderRenterDTO();
            BeanUtils.copyProperties(orderRenter, orderRenterDTO);
            orderRenterDTO.setConsumerUid(orderRenter.getUid());
            orderRenterDTO.setOrderTime(orderRenter.getCtime());
            orderRenterDTO.setConsumerOrderTime(orderRenter.getCtime());
            orderRenterDTO.setConsumerName(orderRenter.getUserRealname());
            orderRenterDTO.setConsumerPhone(orderRenter.getUserMobile());
            orderRenterDTO.setConsumerIdentityNumber(orderRenter.getUserIdNumber());
            orderRenterDTO.setConsumerCreditScore(orderRenter.getUserCreditScore());
            orderRenterDTO.setRentDeadlineMonth(orderRenter.getNumberOfPayments());
            orderRenterDTO.setRentDeadlineDay(orderRenter.getNumberOfPayments() * 30);
            orderRenterDTO.setConsumerCreditLine(orderRenter.getUserCreditLine());
            orderRenterDTO.setConsumerAddress(orderRenter.getUserAddress());
            orderRenterDTO.setMachineNumber(1);
            orderRenterDTO.setOrderFinancingTimes(orderBaseService.getOrderFinancingTimes(orderRenter.getOrderId()));
            orderRenterDTO.setOrderRentTimes(orderBaseService.getOrderRentTimes(orderRenter.getOrderId()));
            orderRenterDTOS.add(orderRenterDTO);
        }
        return new PageJsonResult(SystemCode.SUCCESS, orderRenterDTOS, orderRenterPage.getTotalElements());
    }

    /**
     * 获取未完成的租赁商订单数量
     *
     * @param uid 租赁商uid
     * @return 数量
     */
    @Override
    public int getNotFininishRenterOrder(Long uid) {
        List<Integer> finishstatuses = OrderRenterStatus.getStatuses(5);
        finishstatuses.add(OrderRenterStatus.FUNDER_AUDIT_REFUSE.getCode());
        finishstatuses.add(OrderRenterStatus.ORDER_FINANCING_OVER_THREE.getCode());
        finishstatuses.add(OrderRenterStatus.LESSOR_AUDIT_REFUSE.getCode());
        finishstatuses.add(OrderRenterStatus.ORDER_RENT_OVER_THREE.getCode());
        List<Integer> statuses = Lists.newArrayList();
        for (OrderRenterStatus renterStatus : OrderRenterStatus.values()) {
            statuses.add(renterStatus.getCode());
        }
        List<Integer> notFinishStatuses = statuses.stream().filter(item -> !finishstatuses.contains(item)).collect(Collectors.toList());
        OrderRenterQuery query = new OrderRenterQuery();
        query.setRenterId(uid);
        query.setOrderStatuses(notFinishStatuses);
        List<OrderRenter> all = orderRenterSlave.findAll(query);
        return CollectionUtils.isNotEmpty(all)?all.size():0;
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public JsonResult renterFinancingBuy(Long uid, Long orderId, Long supplierId,
                                         BigDecimal financingAmount, Integer financingDeadline, Integer residualScheme) throws AccountOperationException {
        // 参数校验
        if (orderId == null || supplierId == null || residualScheme == null) {
            return new JsonResult(SystemCode.BAD_REQUEST, false);
        }
        String renterName = orderBaseService.getRenterName(uid);
        // todo 查询供应商是否存在

        // todo 查询资金方uid（目前资金方较少，随便查出一个资金方）
        List<UserFunder> funders = userFunderSlave.findAll();
        Long funderId = 1535433927623092L;
//        if(funders!= null && funders.size() > 0){
//            Integer size = funders.size();
//            Random random = new Random();L
//            funderId = funders.get(random.nextInt(size)).getUid();
//        }else{
//            return new JsonResult(OrderResultEnum.NO_HAVE_FUNDER);
//        }

        // 查询此订单号是否已经在进行资金方审核，防止一个订单提交给多个资金方

        //查询订单融资是否超过3次
        Integer times = orderFunderSlave.getOrderFinancingTimes(orderId);
        // 如果查出来融资次数是0次，则是首次融资
        if (times == null) {
            times = 0;
        }
        if (times >= maxOrderFinancingTimes) {
            // 将租赁商状态改为不允许融资状态
            orderRenterMaser.updateOrderStatus(OrderRenterStatus.ORDER_FINANCING_OVER_THREE.getCode(), orderId);
            return new JsonResult(SystemCode.ORDER_FINANCING_FAIL, false);
        }
        // 查询租赁商订单信息，生成资金方订单数据
        Optional<OrderRenter> optional = orderRenterSlave.findById(orderId);
        if (optional.isPresent()) {
            OrderRenter orderRenter = optional.get();
            // 判断订单是否在审核中
            if(OrderRenterStatus.WATIING_FUNDER_AUDIT.getCode().equals(orderRenter.getOrderStatus())
                    || OrderRenterStatus.WAITING_LESSOR_AUDIT.getCode().equals(orderRenter.getOrderStatus())){
                return new JsonResult(OrderResultEnum.ORDER_AUDITTING,false);
            }
            // 检查订单状态是否是不可融资状态
            if (OrderRenterStatus.ORDER_FINANCING_OVER_THREE.getCode().equals(orderRenter.getOrderStatus())) {
                return new JsonResult(SystemCode.ORDER_FINANCING_FAIL, false);
            }
            OrderFunder orderFunder = new OrderFunder();
            BeanUtils.copyProperties(orderRenter, orderFunder);
            // 此处操作用户是租赁商，所以资金方的商家id应该是租赁商的id
            orderFunder.setMerchantId(uid);
            orderFunder.setCtime(null);
            orderFunder.setUtime(null);
            orderFunder.setMerchantName(renterName);
            // 资金方的uid是从数据库中查出来的，暂时使用随机数代替
            orderFunder.setFunderId(funderId);
            orderFunder.setOrderStatus(OrderFunderStatus.WAITING_AUDIT.getCode());
            orderFunder.setDispatchTimes(times + 1);
            orderFunder.setSupplierId(supplierId);

            orderFunder.setDeposit(getDeposit(orderId, supplierId));
            orderFunder.setFinancingAmount(financingAmount);
            orderFunder.setNumberOfPeriods(financingDeadline);
            short withhold = 2;
            orderFunder.setWithhold(withhold);
            orderFunder.setOrderAmount(orderRenter.getAccidentBenefit()
                    .add(orderRenter.getMonthlyPayment()
                            .multiply(new BigDecimal(orderRenter.getNumberOfPayments()))));
            orderFunder.setDeposit(getDeposit(orderId, supplierId));
            orderFunder.setResidualScheme(residualScheme);

            // 修改租赁商订单状态和订单类型
            updateOrderRenterStatusType(OrderRenterStatus.WATIING_FUNDER_AUDIT,OrderType.FUNDER_ORDER, orderId);
            // 生成资金方订单
            orderFunderMaser.save(orderFunder);
            // 账户操作，冻结保证金
            OperationResult<Boolean> accountOperationResult = accountOperation.financing(orderRenter,orderFunder);
            if (!accountOperationResult.getEntity()){
                // 账户操作不成功抛出异常回滚
                throw new AccountOperationException(accountOperationResult);
            }
            orderBlockChainService.financeApply(orderRenter,orderFunder);
            // 推送消息
            orderBaseService.messagePush(funderId,orderId,orderRenter.getPartnerOrderId(),MessagePushEnum.FINANCING_APPLY);

        }
        GwsLogger.getLogger().info("租赁商" + uid + "的订单" + orderId + "已派发给资金方" + funderId);

        // 返回订单融资成功
        return new JsonResult(SystemCode.SUCCESS, true);
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public JsonResult renterRent(Long uid, Long orderId, Long lessorId, Integer tenancyTerm,
                                 BigDecimal monthlyPayment, BigDecimal accidentBenefit, Integer patternPayment,
                                 BigDecimal fullRepayAmount) throws AccountOperationException {
        // 参数校验
        if (orderId == null || lessorId == null
                || tenancyTerm == null || !PatternPaymentEnum.isDefined(patternPayment)) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }

        // todo 根据lessorId查询出租方是否存在

        String renterName = orderBaseService.getRenterName(uid);
        String renterMobile = orderBaseService.getRenterPhone(uid);

        Optional<OrderRenter> optional = orderRenterSlave.findById(orderId);
        if (optional.isPresent()) {
            OrderRenter orderRenter = optional.get();
            // 判断订单状态是否可以再次租赁
            if(OrderRenterStatus.ORDER_RENT_OVER_THREE.getCode().equals(orderRenter.getOrderStatus())){
                return new JsonResult(OrderResultEnum.ORDER_RENTING_FAIL,false);
            }
            // 判断订单是否在审核中
            if(OrderRenterStatus.WATIING_FUNDER_AUDIT.getCode().equals(orderRenter.getOrderStatus())
                    || OrderRenterStatus.WAITING_LESSOR_AUDIT.getCode().equals(orderRenter.getOrderStatus())){
                return new JsonResult(OrderResultEnum.ORDER_AUDITTING,false);
            }
            // 判断订单租赁次数是否大于最大租赁次数
            OrderHirer orderHirerHistory = orderHirerSlave.findOne(orderId);
            Integer orderRentTimes ;
            if(orderHirerHistory != null){
                if(maxOrderRentingTimes <= orderHirerHistory.getDispatchTimes()){
                    return new JsonResult(OrderResultEnum.ORDER_RENTING_FAIL,false);
                }
                orderRentTimes = orderHirerHistory.getDispatchTimes()+1;
            }else{
                orderRentTimes = 1;
            }
            OrderHirer orderHirer = new OrderHirer();
            BeanUtils.copyProperties(orderRenter, orderHirer);
            orderHirer.setMerchantId(uid);
            orderHirer.setMerchantName(renterName);
            orderHirer.setMerchantMobile(renterMobile);
            orderHirer.setDispatchTimes(orderRentTimes);
            orderHirer.setCtime(null);
            orderHirer.setUtime(null);
            // 出租方订单为 待出租方审核
            orderHirer.setOrderStatus(OrderLessorStatus.WATIING_LESSOR_AUDIT.getCode());

            //修改为出租方租赁期限
            orderHirer.setPatternPayment(patternPayment);
            orderHirer.setLessorId(lessorId);

            orderHirer.setAccidentBenefit(accidentBenefit);
            orderHirer.setNumberOfPeriods(tenancyTerm);
            if(PatternPaymentEnum.INSTALMENT_PAYMENT.getValue().equals(patternPayment)) {
                // 出租方订单的租机价格，意外保险等由平台指定
                orderHirer.setDownPayment(monthlyPayment.add(accidentBenefit));
                orderHirer.setMonthlyPayment(monthlyPayment);
                orderHirer.setOrderAmount(monthlyPayment.multiply(BigDecimal.valueOf(tenancyTerm)));
            }else{
                // 全款支付的首付、月租金等都为0
                orderHirer.setDownPayment(BigDecimal.ZERO);
                orderHirer.setMonthlyPayment(BigDecimal.ZERO);
                orderHirer.setOrderAmount(fullRepayAmount);
            }


            // 生成出租方订单
            orderHirerMaser.save(orderHirer);
            OperationResult<Boolean> operationResult = accountOperation.frozenRentDownPayment(orderRenter,orderHirer);
            if(!operationResult.getEntity()){
                throw new AccountOperationException(operationResult);
            }
            orderBlockChainService.rentApply(orderRenter,orderHirer);
            orderBaseService.messagePush(lessorId,orderId,orderRenter.getPartnerOrderId(),MessagePushEnum.RENT_APPLY);
            // 修改租赁商订单状态
            updateOrderRenterStatusType(OrderRenterStatus.WAITING_LESSOR_AUDIT,OrderType.LESSOR_ORDER, orderId);
        }

        GwsLogger.getLogger().info("租赁商" + uid + "的订单" + orderId + "已派发给出租方：" + lessorId);
        return new JsonResult(SystemCode.SUCCESS, true);
    }

    @Override
    //@Transactional(rollbackFor = RuntimeException.class)
    public JsonResult cancelOrder(List<Long> orders, Long uid) {
        // 参数校验
        if (orders == null || orders.size() == 0) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        // 遍历取消订单
        for (Long orderId : orders) {
            Optional<OrderRenter> optional = orderRenterSlave.findById(orderId);
            if (optional.isPresent()) {
                OrderRenter orderRenter = optional.get();
                orderRenter.setOrderStatus(OrderRenterStatus.ORDER_CANCEL.getCode());
                OrderRenterQuery query = new OrderRenterQuery();
                query.setOrderId(orderRenter.getOrderId());
                Integer row = orderRenterMaser.update(orderRenter, query, OrderRenter.ORDER_STATUS);
                // 如果修改订单状态失败，说明存在异常，抛出异常回滚
                if (row != 1) {
                    throw new RuntimeException(SystemCode.ORDER_CANCEL_FAIL.getMessage());
                }
            }
        }

        return new JsonResult(SystemCode.SUCCESS, true);
    }

    @Override
    public JsonResult renterViewDetail(Long orderId, Long renterId) {
        if (orderId == null) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        OrderRenterQuery query = new OrderRenterQuery();
        query.setRenterId(renterId);
        query.setDeleteStatus(OrderRenter.NO_DELETE_STATUS);
        query.setOrderId(orderId);
        List<OrderRenter> orderRenters = orderRenterSlave.findAll(query);
        if (orderRenters != null && orderRenters.size() == 1) {
            OrderRenter orderRenter = orderRenters.get(0);
            OrderRenterDTO orderRenterDTO = new OrderRenterDTO();
            BeanUtils.copyProperties(orderRenter, orderRenterDTO);
            orderRenterDTO.setConsumerUid(orderRenter.getUid());
            orderRenterDTO.setOrderTime(orderRenter.getCtime());
            orderRenterDTO.setConsumerOrderTime(orderRenter.getCtime());
            orderRenterDTO.setConsumerName(orderRenter.getUserRealname());
            orderRenterDTO.setConsumerPhone(orderRenter.getUserMobile());
            orderRenterDTO.setConsumerIdentityNumber(orderRenter.getUserIdNumber());
            orderRenterDTO.setConsumerCreditScore(orderRenter.getUserCreditScore());
            orderRenterDTO.setRentDeadlineMonth(orderRenter.getNumberOfPayments());
            orderRenterDTO.setRentDeadlineDay(orderRenter.getNumberOfPayments() * 30);
            orderRenterDTO.setConsumerCreditLine(orderRenter.getUserCreditLine());
            orderRenterDTO.setConsumerAddress(orderRenter.getUserAddress());
            // todo 查出用户坏账次数
            orderRenterDTO.setConsumerBedDebtTimes(0);
            orderRenterDTO.setMachineNumber(1);
            orderRenterDTO.setOrderFinancingTimes(orderBaseService.getOrderFinancingTimes(orderRenter.getOrderId()));
            orderRenterDTO.setOrderRentTimes(orderBaseService.getOrderRentTimes(orderRenter.getOrderId()));
            return new JsonResult(SystemCode.SUCCESS, orderRenterDTO);
        }
        return new JsonResult(SystemCode.BAD_REQUEST);
    }

    @Override
    public JsonResult getSupplierList(ProductInfoDTO productInfo,Long orderId) {
        if(orderId == null){
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        OrderProductSupplierQuery query = new OrderProductSupplierQuery();
        query.setProductBrand(productInfo.getProductBrand());
        query.setProductColor(productInfo.getProductColor());
        query.setProductStorage(productInfo.getProductStorage());
        query.setProductName(productInfo.getProductName());
        List<ProductSupplier> products = productSupplierSlave.findAll(query);
        List<SupplierInfoDTO> dtos = new ArrayList<>();
        for (ProductSupplier product : products) {
            UserSupplier userSupplier = userSupplierService.findOne(product.getSupplierId());
            if (userSupplier != null) {
                SupplierInfoDTO dto = new SupplierInfoDTO();
                dto.setSupplierId(product.getSupplierId());
                dto.setSupplierName(userSupplier.getMerchantName());
                BigDecimal financingAmount = getFinancingAmount(orderId, userSupplier.getUid());
                dto.setFinancingAmount(financingAmount);
                dto.setAccidentBenefit(residualValue);
                Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
                if(financingAmount != null && orderRenterOptional.isPresent()){
                    OrderRenter orderRenter = orderRenterOptional.get();
                    dto.setDownPayment(financingAmount
                            .divide(BigDecimal.valueOf(orderRenter.getNumberOfPayments()),2,RoundingMode.HALF_UP)
                            .add(residualValue));
                    dto.setMonthPayment(financingAmount
                            .divide(BigDecimal.valueOf(orderRenter.getNumberOfPayments()),2,RoundingMode.HALF_UP));
                    dto.setDeposit(getDeposit(orderId,userSupplier.getUid() ));
                }

                dtos.add(dto);
            }
        }
        return new JsonResult(SystemCode.SUCCESS, dtos);
    }

    @Override
    public JsonResult getLessorList(ProductInfoDTO productInfo) {
        OrderProductLessorQuery query = new OrderProductLessorQuery();
        query.setProductBrand(productInfo.getProductBrand());
        query.setProductColor(productInfo.getProductColor());
        query.setProductStorage(productInfo.getProductStorage());
        query.setProductName(productInfo.getProductName());
        query.setProductRandomMemory(productInfo.getProductRandomMemory());
        List<ProductLessor> products = productLessorSlave.findAll(query);
        if(products == null || products.size() == 0){
            return new JsonResult(OrderResultEnum.LESSOR_NOT_EXIST,false);
        }
        List<LessorInfoDTO> dtos = new ArrayList<>();
        for (ProductLessor product : products) {
            UserHirer userHirer = userHirerService.findOne(product.getSupplierId());
            if (userHirer != null) {
                LessorInfoDTO dto = new LessorInfoDTO();
                dto.setLessorId(product.getSupplierId());
                dto.setLessorName(userHirer.getMerchantName());
                dto.setMonthlyPayment(orderBaseService.getRentPrice(product.getSupplyPrice(),new BigDecimal("0.15"),24));
                dto.setNumberOfPeriods(24);
                dto.setFullRepayAmount(product.getSupplyPrice());
                dto.setAccidentBenefit(BigDecimal.ZERO);
                dtos.add(dto);
            }
        }
        return new JsonResult(SystemCode.SUCCESS, dtos);
    }

    @Override
    public JsonResult getFunderOrder(FunderQueryOrderDTO param, Long uid) {
        if (null == param.getPageSize() || null == param.getCurrentPage()) {
            return new JsonResult(SystemCode.NO_PAGE_PARAM);
        }
        // 包装查询条件
        OrderFunderQuery orderFunderQuery = new OrderFunderQuery();
        if (StringUtils.isNotEmpty(param.getRenterName())) {
            orderFunderQuery.setMerchantName(param.getRenterName());
        }
        orderFunderQuery.setFunderId(uid);
        if (param.getOrderStatus() != null) {
            orderFunderQuery.setOrderStatuses(OrderFunderStatus.getStatuses(param.getOrderStatus()));
        }
        // 封装排序
        Sort sort;
        if (OrderSort.DESC.getValue().equals(param.getSort())) {
            sort = new Sort(Sort.Direction.DESC, OrderFunder.CTIME);
        } else {
            sort = new Sort(Sort.Direction.ASC, OrderFunder.CTIME);
        }
        // 设置分页
        Pageable pageable = PageRequest.of(param.getCurrentPage() - 1, param.getPageSize(), sort);
        Specification specification = new QueryToSpecification(orderFunderQuery);
        // 查询数据
        Page<OrderFunder> page = orderFunderSlave.findAll(specification, pageable);
        List<OrderFunder> orderFunders = page.getContent();
        // 封装dto
        List<OrderFunderDTO> orders = new ArrayList<>();
        for (OrderFunder orderFunder : orderFunders) {
            OrderFunderDTO orderFunderDTO = wrapOrderFunder2DTO(orderFunder);
            orders.add(orderFunderDTO);
        }
        return new PageJsonResult(SystemCode.SUCCESS, orders, page.getTotalElements());
    }

    @Override
    public JsonResult funderViewDetail(Long orderId, Long uid) {
        if (orderId == null) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        OrderFunderQuery query = new OrderFunderQuery();
        query.setOrderId(orderId);
        query.setFunderId(uid);
        query.setDeleteStatus(OrderFunder.NO_DELETE_STATUS);
        List<OrderFunder> orderFunders = orderFunderSlave.findAll(query);
        if (orderFunders != null && orderFunders.size() == 1) {
            OrderFunder orderFunder = orderFunders.get(0);
            OrderFunderDTO orderFunderDTO = wrapOrderFunder2DTO(orderFunder);
            return new JsonResult(SystemCode.SUCCESS, orderFunderDTO);
        }
        return new JsonResult(SystemCode.BAD_REQUEST);
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public JsonResult funderRefuse(Long orderId, Long uid) throws AccountOperationException {
        if (orderId == null) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        // 查询资金方订单
        OrderFunderQuery query = new OrderFunderQuery();
        query.setOrderId(orderId);
        query.setFunderId(uid);
        query.setDeleteStatus(OrderFunder.NO_DELETE_STATUS);
        List<OrderFunder> orderFunders = orderFunderSlave.findAll(query);
        if (orderFunders != null && orderFunders.size() == 1) {
            OrderFunder orderFunder = orderFunders.get(0);
            // 查询租赁商订单，将状态改为资金方已拒绝
            Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
            if (orderRenterOptional.isPresent()) {
                OrderRenter orderRenter = orderRenterOptional.get();
                // 如果 订单融资次数 >= 3 ,租赁商订单状态设为不可融资状态
                if(maxOrderFinancingTimes.compareTo(orderFunder.getDispatchTimes()) <= 0){
                    orderRenter.setOrderStatus(OrderRenterStatus.ORDER_FINANCING_OVER_THREE.getCode());
                    orderRenterMaser.updateById(orderRenter,orderId,OrderRenter.ORDER_STATUS);
                }else{
                    orderRenter.setOrderStatus(OrderRenterStatus.FUNDER_AUDIT_REFUSE.getCode());
                    orderRenterMaser.updateById(orderRenter,orderId,OrderRenter.ORDER_STATUS);
                }
                orderFunder.setOrderStatus(OrderFunderStatus.AUDIT_REFUSE.getCode());
                orderFunderMaser.save(orderFunder);
                // 操作租赁商账户退还保证金
                OperationResult<Boolean> operationResult = accountOperation.releaseMarginBalance(orderRenter,orderFunder);
                if(!operationResult.getEntity()){
                    throw new AccountOperationException(operationResult);
                }
                orderBlockChainService.financeRefuse(orderId,uid);
                return new JsonResult(SystemCode.SUCCESS, true);
            }
        }
        return new JsonResult(SystemCode.BAD_REQUEST);
    }

    @Override
    public JsonResult offlineLoan(LoanDTO loanDTO, Long uid) {
        if (loanDTO.getLoanAmount() == null
                || loanDTO.getLoanModel() == null || loanDTO.getOrderId() == null) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        // 查询资金方订单，为了保障安全，只能查出本人的订单
        OrderFunderQuery query = new OrderFunderQuery();
        query.setOrderId(loanDTO.getOrderId());
        query.setFunderId(uid);
        query.setDeleteStatus(OrderFunder.NO_DELETE_STATUS);
        List<OrderFunder> orderFunders = orderFunderSlave.findAll(query);
        // 只查出一条数据，避免get(0)出错
        if (orderFunders != null && orderFunders.size() == 1) {
            OrderFunder orderFunder = orderFunders.get(0);
            // 查询租赁商订单
            Optional<OrderRenter> optional = orderRenterSlave.findById(loanDTO.getOrderId());
            if (optional.isPresent()) {
                OrderRenter orderRenter = optional.get();
                // 只允许租赁商订单状态为待资金方审核的订单放款
                if (OrderRenterStatus.WATIING_FUNDER_AUDIT.getCode().equals(orderRenter.getOrderStatus())) {
                    boolean success;
                    // 线下付款
                    success = offlineLoan(loanDTO, orderFunder);
                    if (success) {
                        return new JsonResult(SystemCode.SUCCESS);
                    }
                    return new JsonResult(SystemCode.LOAN_FAIL);
                }
                return new JsonResult(SystemCode.LOAN_ORDER_STATUS_ERROR, false);
            }
            orderFunder.setLoanModel(loanDTO.getLoanModel());
            orderFunderMaser.save(orderFunder);
            return new JsonResult(SystemCode.SUCCESS);
        }
        return new JsonResult(SystemCode.BAD_REQUEST);
    }

    //@Transactional(rollbackFor = Exception.class)
    @Override
    public JsonResult funderUploadEvidence(Long uid, Long orderId, MultipartFile file) throws AccountOperationException {
        if (orderId == null) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        String imgUrl = null;
        if (file != null) {
            try {
                // 上传文件
                imgUrl = aliossService.uploadFileStreamToBasetool(file, bucket);
            } catch (IOException e) {
                e.printStackTrace();
                return new JsonResult(SystemCode.FILE_UPLOAD_FAIL);
            }
        }

        // 查询资金方订单，为了保障安全，只能查出本人的订单
        OrderFunderQuery query = new OrderFunderQuery();
        query.setOrderId(orderId);
        query.setFunderId(uid);
        query.setDeleteStatus(OrderFunder.NO_DELETE_STATUS);
        List<OrderFunder> orderFunders = orderFunderSlave.findAll(query);
        if (orderFunders != null && orderFunders.size() == 1) {
            OrderFunder orderFunder = orderFunders.get(0);
            // 查询租赁商订单
            Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
            if (orderRenterOptional.isPresent()) {
                OrderRenter orderRenter = orderRenterOptional.get();
                // 保存线下放款凭证
                if(imgUrl != null){
                    OrderFunderEvidence evidence = new OrderFunderEvidence();
                    evidence.setOrderId(orderId);
                    evidence.setEvidenceUrl(imgUrl);
                    evidence.setRenterId(orderRenter.getRenterId());
                    evidence.setFunderId(orderFunder.getFunderId());
                    orderFunderEvidenceMaster.save(evidence);
                    // 修改资金方订单状态为,线下放款，有凭证，待发货
                    orderFunder.setOrderStatus(OrderFunderStatus.WAITING_DELIVER_EVIDENCE.getCode());
                }else{
                    // 修改资金方订单状态为,线下放款，无凭证，待发货
                    orderFunder.setOrderStatus(OrderFunderStatus.WAITING_DELIVER_NO_EVIDENCE.getCode());
                }
                // 修改租赁商订单状态为资金方线下放款成功
                orderRenter.setOrderStatus(OrderRenterStatus.FUNDER_OFFLINE_LOAN_SUCCESS.getCode());
                orderRenterMaser.save(orderRenter);
                orderFunderMaser.save(orderFunder);
                OrderSupplier orderSupplier = generateSupplyOrder(orderRenter, orderFunder,OrderSupplierStatus.WAITING_DELIVER.getCode());
                // 如果此线下放款订单还未上传凭证，则是确认已放款操作，去操作资金方账户和生成租赁商还款计划
                // 这一步操作要判断此操作是确认已放款还是上传凭证，通过orderSupplier是否存在判断
                if(!orderSupplierSlave.findById(orderRenter.getOrderId()).isPresent()){
                    orderBaseService.renterFunderAccountOperation(orderRenter,orderFunder);
                    // 第一次操作供应商订单，操作账户线下放款
                    OperationResult<Boolean> operationResult = accountOperation.offlineLoan(orderRenter,orderSupplier,orderFunder);
                    if(!operationResult.getEntity()){
                        throw new AccountOperationException(operationResult);
                    }
                    orderBlockChainService.fundSupplier(orderFunder,orderSupplier,null);
                }
                return new JsonResult(SystemCode.GENERATE_SUPPLY_ORDER_SUCCESS, true);
            }
        }

        return new JsonResult(SystemCode.BAD_REQUEST, false);
    }

    @Override
    public JsonResult getsSupplierOrder(QueryOrderDTO param, Long uid) {
        if (null == param.getPageSize() || null == param.getCurrentPage()) {
            return new JsonResult(SystemCode.NO_PAGE_PARAM);
        }
        // 查询排序规则
        Sort sort;
        if (param.getSort() == null || OrderSort.DESC.getValue().equals(param.getSort())) {
            sort = new Sort(Sort.Direction.DESC, OrderRenter.CTIME);
        } else {
            sort = new Sort(Sort.Direction.ASC, OrderRenter.CTIME);
        }
        // 设置分页条件
        Pageable pageable = PageRequest.of(param.getCurrentPage() - 1, param.getPageSize(), sort);
        // 设置查询条件
        OrderSupplierQuery query = new OrderSupplierQuery();
        query.setDeleteStatus(OrderSupplier.NO_DELETE_STATUS);
        query.setSupplierId(uid);
        if(null != param.getOrderStatus()){
            query.setOrderStatuses(OrderSupplierStatus.getStatuses(param.getOrderStatus()));
        }
        Page<OrderSupplier> page = orderSupplierSlave.findAll(query, pageable);
        List<OrderSupplier> suppliers = page.getContent();
        List<OrderSupplierDTO> orderSupplierDTOS = new ArrayList<>();
        for (OrderSupplier orderSupplier : suppliers) {
            OrderSupplierDTO orderSupplierDTO = wrapOrderSupplier2DTO(orderSupplier);
            orderSupplierDTOS.add(orderSupplierDTO);
        }
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotalElements());
        pageResult.setList(orderSupplierDTOS);
        return new JsonResult(SystemCode.SUCCESS, pageResult);
    }

    @Override
    public JsonResult supplierDeliver(Long uid, DeliverInfoDTO deliverInfo) {
        // 参数校验
        if (StringUtils.isEmpty(deliverInfo.getExpressName()) || StringUtils.isEmpty(deliverInfo.getDeliverId())
                || StringUtils.isEmpty(deliverInfo.getSerialNumber()) || deliverInfo.getDeliverTime() == null
                || deliverInfo.getOrderId() == null || deliverInfo.getDeliverCode() == null) {
            return new JsonResult(SystemCode.BAD_REQUEST, false);
        }
        OrderSupplierQuery query = new OrderSupplierQuery();
        query.setOrderId(deliverInfo.getOrderId());
        query.setSupplierId(uid);
        OrderSupplier orderSupplier = orderSupplierSlave.findOne(query);
        if (orderSupplier == null) {
            return new JsonResult(SystemCode.BAD_REQUEST, false);
        }
        orderSupplier.setExpressCompany(deliverInfo.getExpressName());
        orderSupplier.setExpressNumber(deliverInfo.getDeliverId());
        orderSupplier.setExpressTime(deliverInfo.getDeliverTime());
        orderSupplier.setSerialNumber(deliverInfo.getSerialNumber());
        orderSupplier.setExpressCode(deliverInfo.getDeliverCode());
        // 待收货状态
        orderSupplier.setOrderStatus(OrderSupplierStatus.WAITING_RECEIVE.getCode());
        orderSuppMaster.save(orderSupplier);
        // 修改资金方订单为待收货状态
        Optional<OrderFunder> orderFunderOptional = orderFunderSlave.findById(deliverInfo.getOrderId());
        if (orderFunderOptional.isPresent()) {
            OrderFunder orderFunder = orderFunderOptional.get();
            orderFunder.setOrderStatus(OrderFunderStatus.WAITING_RECEIVE.getCode());
            orderFunderMaser.save(orderFunder);
        }
        // 修改租赁商订单状态
        Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(deliverInfo.getOrderId());
        if (orderRenterOptional.isPresent()) {
            OrderRenter orderRenter = orderRenterOptional.get();
            orderRenter.setOrderStatus(OrderRenterStatus.WAITING_SUPPLIER_RECEIVE.getCode());
            orderRenterMaser.save(orderRenter);
        }
        orderBlockChainService.supplierDeliver(orderSupplier);
        return new JsonResult(SystemCode.SUCCESS, true);
    }

    @Override
    public OperationResult<DeliverDTO> supplierGetLogistics(Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        Optional<OrderSupplier> orderSupplierOptional = orderSupplierSlave.findById(orderId);
        if (orderSupplierOptional.isPresent()) {
            OrderSupplier orderSupplier = orderSupplierOptional.get();
            String expressNumber = orderSupplier.getExpressNumber();
            if (StringUtils.isEmpty(expressNumber)) {
                return new OperationResult<>(OrderResultEnum.NO_EXPRESS_INFO);
            }
            String expressCode = orderSupplier.getExpressCode();
            OperationResult<ExpressInfo> result = commonService.getExpressMessage(expressCode, expressNumber);
            ExpressInfo expressInfo = result.getEntity();
            String expressData = expressInfo.getExpStatus();
            if (!StringUtils.isEmpty(expressData)) {
                expressData = "[" + expressData + "]";
                DeliverDTO deliverDTO = new DeliverDTO();
                deliverDTO.setDeliverInfo(expressData);
                deliverDTO.setExpressCompany(orderSupplier.getExpressCompany());
                deliverDTO.setExpressNumber(orderSupplier.getExpressNumber());
                deliverDTO.setExpressTime(orderSupplier.getExpressTime());
                deliverDTO.setSerialNumber(orderSupplier.getSerialNumber());
                return new OperationResult<>(SystemCode.SUCCESS, deliverDTO);

            }
        }
        return new OperationResult<>(OrderResultEnum.NO_EXPRESS_INFO);
    }

    @Override
    public JsonResult supplierViewDetail(Long orderId, Long uid) {
        if (null == orderId) {
            return new JsonResult(SystemCode.BAD_REQUEST);
        }
        OrderSupplierQuery query = new OrderSupplierQuery();
        query.setOrderId(orderId);
        query.setSupplierId(uid);
        query.setDeleteStatus(OrderSupplier.NO_DELETE_STATUS);
        OrderSupplier orderSupplier = orderSupplierSlave.findOne(query);
        if (null != orderSupplier) {
            OrderSupplierDTO orderSupplierDTO = wrapOrderSupplier2DTO(orderSupplier);
            return new JsonResult(SystemCode.SUCCESS, orderSupplierDTO);
        }
        return new JsonResult(SystemCode.BAD_REQUEST, false);
    }

    @Override
    public JsonResult getLessorOrder(Long uid, QueryOrderDTO param) {
        if (null == param.getPageSize() || null == param.getCurrentPage()) {
            return new JsonResult(SystemCode.NO_PAGE_PARAM);
        }
        // 查询排序规则
        Sort sort;
        if (param.getSort() == null || OrderSort.DESC.getValue().equals(param.getSort())) {
            sort = new Sort(Sort.Direction.DESC, OrderRenter.CTIME);
        } else {
            sort = new Sort(Sort.Direction.ASC, OrderRenter.CTIME);
        }
        // 设置查询条件
        OrderHirerQuery query = new OrderHirerQuery();
        query.setLessorId(uid);
        query.setDeleteStatus(OrderHirer.NO_DELETE_STATUS);
        if (param.getOrderStatus() != null) {
            query.setOrderStatuses(OrderLessorStatus.getStatuses(param.getOrderStatus()));
        }

        // 设置分页参数
        Pageable pageable = PageRequest.of(param.getCurrentPage() - 1, param.getPageSize(), sort);
        Page<OrderHirer> page = orderHirerSlave.findAll(query, pageable);
        PageResult result = new PageResult();

        List<OrderHirer> orderHirers = page.getContent();
        List<OrderHirerDTO> orderHirerDTOS = new ArrayList<>();
        for (OrderHirer orderHirer : orderHirers) {
            OrderHirerDTO orderHirerDTO = wrapOrderHirer2DTO(orderHirer);
            orderHirerDTOS.add(orderHirerDTO);
        }
        result.setTotal(page.getTotalElements());
        result.setList(orderHirerDTOS);
        return new JsonResult(SystemCode.SUCCESS, result);
    }

    @Override
    public JsonResult lessorViewDetail(Long uid, Long orderId) {
        if (orderId == null) {
            return new JsonResult(SystemCode.BAD_REQUEST, false);
        }
        OrderHirerQuery query = new OrderHirerQuery();
        query.setLessorId(uid);
        query.setOrderId(orderId);
        OrderHirer orderHirer = orderHirerSlave.findOne(query);
        if (orderHirer != null) {
            OrderHirerDTO orderHirerDTO = wrapOrderHirer2DTO(orderHirer);
            return new JsonResult(SystemCode.SUCCESS, orderHirerDTO);
        }
        return new JsonResult(SystemCode.BAD_REQUEST, false);
    }

    @Override
    //@Transactional(rollbackFor = RuntimeException.class)
    public JsonResult lessorLogistics(Long uid, DeliverInfoDTO deliverInfo) throws AccountOperationException {
        // 参数校验
        if (StringUtils.isEmpty(deliverInfo.getExpressName()) || StringUtils.isEmpty(deliverInfo.getDeliverId())
                || StringUtils.isEmpty(deliverInfo.getSerialNumber()) || deliverInfo.getDeliverTime() == null
                || deliverInfo.getOrderId() == null || deliverInfo.getDeliverCode() == null) {
            return new JsonResult(SystemCode.BAD_REQUEST, false);
        }
        HirerDeliver hirerDeliver = new HirerDeliver();
        hirerDeliver.setExpressName(deliverInfo.getExpressName());
        hirerDeliver.setExpressNumber(deliverInfo.getDeliverId());
        hirerDeliver.setExpressTime(deliverInfo.getDeliverTime());
        hirerDeliver.setLessorId(uid);
        hirerDeliver.setOrderId(deliverInfo.getOrderId());
        hirerDeliver.setSerialNumber(deliverInfo.getSerialNumber());
        hirerDeliver.setExpressCode(deliverInfo.getDeliverCode());
        hirerDeliverMaster.save(hirerDeliver);
        OrderHirer orderHirer = null;
        OrderRenter orderRenter = null;
        // 修改出租方订单状态为待收货
        Optional<OrderHirer> optionalOrderHirer = orderHirerSlave.findById(deliverInfo.getOrderId());
        if (optionalOrderHirer.isPresent()) {
            orderHirer = optionalOrderHirer.get();
            orderHirer.setOrderStatus(OrderLessorStatus.WAITING_RECEIVE.getCode());
            orderHirerMaser.save(orderHirer);
        }
        // 修改资金方订单状态为待收货
        Optional<OrderRenter> optionalOrderRenter = orderRenterSlave.findById(deliverInfo.getOrderId());
        if (optionalOrderRenter.isPresent()) {
            orderRenter = optionalOrderRenter.get();
            orderRenter.setOrderStatus(OrderRenterStatus.WAITING_LESSOR_RECEIVE.getCode());
            orderRenterMaser.save(orderRenter);

        }
        // 操作租赁商账户
        orderBaseService.renterRentAccountOperation(orderRenter,orderHirer);
        OperationResult<Boolean> operationResult = accountOperation.payDownPayment(orderRenter,orderHirer);
        if(!operationResult.getEntity()){
            throw new AccountOperationException(operationResult);
        }
        orderBlockChainService.payLessor(orderRenter,orderHirer);
        orderBlockChainService.lessorDeliver(orderHirer,hirerDeliver);
        return new JsonResult(SystemCode.SUCCESS, true);
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public JsonResult lessorRefuse(Long orderId, Long uid) throws AccountOperationException {
        if (orderId == null) {
            return new JsonResult(SystemCode.BAD_REQUEST, false);
        }
        // 修改出租方订单状态为出租方拒绝
        Optional<OrderHirer> optionalOrderHirer = orderHirerSlave.findById(orderId);
        if (optionalOrderHirer.isPresent()) {
            OrderHirer orderHirer = optionalOrderHirer.get();
            orderHirer.setOrderStatus(OrderLessorStatus.LESSOR_AUDIT_REFUSE.getCode());
            orderHirerMaser.save(orderHirer);
            // 如果 最大次数租赁次数 <= 订单租赁次数，将租赁商订单改为 不可再租赁状态
            if(maxOrderRentingTimes.compareTo(orderHirer.getDispatchTimes()) <= 0){
                Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
                if(orderRenterOptional.isPresent()){
                    OrderRenter orderRenter = orderRenterOptional.get();
                    orderRenter.setOrderStatus(OrderRenterStatus.ORDER_RENT_OVER_THREE.getCode());
                    orderRenterMaser.updateById(orderRenter,orderId,OrderRenter.ORDER_STATUS);
                }
            }else{
                // 修改租赁商订单为出租方拒绝
                orderRenterMaser.updateOrderStatus(OrderRenterStatus.LESSOR_AUDIT_REFUSE.getCode(),orderId);
            }
            OperationResult<Boolean> operationResult = accountOperation.unfrozenRentDownPayment(orderRenterSlave.getOne(orderId),orderHirer);
            if(!operationResult.getEntity()){
                throw new AccountOperationException(operationResult);
            }
            orderBlockChainService.rentalRefuse(orderId,uid);
        }
        return new JsonResult(SystemCode.SUCCESS, true);
    }

    @Override
    public OperationResult<List<ExpressCompanyDTO>> getExpressList() {
        List<ExpressCompany> expressCompanies = expressCompanySlave.findAll();
        List<ExpressCompanyDTO> dtos = new ArrayList<>();
        for (ExpressCompany expressCompany : expressCompanies) {
            ExpressCompanyDTO dto = new ExpressCompanyDTO();
            dto.setExpressCode(expressCompany.getCompanyCode());
            dto.setExpressName(expressCompany.getCompanyName());
            dtos.add(dto);
        }
        return new OperationResult<>(OrderResultEnum.SUCCESS, dtos);
    }

    @Override
    public OperationResult<DeliverDTO> lessorGetLogistics(Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        HirerDeliver hirerDeliver = hirerDeliverSlave.findOne(orderId);
        if (hirerDeliver == null) {
            return new OperationResult<>(OrderResultEnum.NO_EXPRESS_INFO);
        }
        OperationResult<ExpressInfo> result = commonService.getExpressMessage(hirerDeliver.getExpressCode(),
                hirerDeliver.getExpressNumber());
        ExpressInfo expressInfo = result.getEntity();
        String expressData = expressInfo.getExpStatus();
        if (!StringUtils.isEmpty(expressData)) {
            expressData = "[" + expressData + "]";
            DeliverDTO deliverDTO = new DeliverDTO();
            deliverDTO.setDeliverInfo(expressData);
            deliverDTO.setExpressCompany(hirerDeliver.getExpressName());
            deliverDTO.setExpressNumber(hirerDeliver.getExpressNumber());
            deliverDTO.setExpressTime(hirerDeliver.getExpressTime());
            deliverDTO.setSerialNumber(hirerDeliver.getSerialNumber());
            return new OperationResult<>(OrderResultEnum.SUCCESS, deliverDTO);
        }
        return new OperationResult<>(OrderResultEnum.NO_EXPRESS_INFO);
    }

    @Override
    public OperationResult<DeliverDTO> renterGetLogistics(Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
        if (orderRenterOptional.isPresent()) {
            OrderRenter orderRenter = orderRenterOptional.get();
            if (OrderType.FUNDER_ORDER.getCode().equals(orderRenter.getOrderType())) {
                return supplierGetLogistics(orderId);
            }
            if (OrderType.LESSOR_ORDER.getCode().equals(orderRenter.getOrderType())) {
                return lessorGetLogistics(orderId);
            }

        }
        return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
    }

    @Override
    public OperationResult<Boolean> renterDeleteOrder(Long uid, Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        OrderRenterQuery query = new OrderRenterQuery();
        query.setOrderId(orderId);
        query.setRenterId(uid);
        OrderRenter orderRenter = orderRenterSlave.findOne(query);
        if (orderRenter != null) {
            if (OrderRenterStatus.ORDER_CANCEL.getCode().equals(orderRenter.getOrderStatus())) {
                orderRenter.setDeleteStatus(OrderRenter.DELETE_STATUS);
                orderRenterMaser.save(orderRenter);
                return new OperationResult<>(OrderResultEnum.SUCCESS, true);
            }
            return new OperationResult<>(OrderResultEnum.ORDER_UNDERWAY);
        }
        return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
    }

    @Override
    public OperationResult<Boolean> orderFinancingable(Long uid, Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        Account renterAccount = accountOperation.getAccount(uid);
        Optional<OrderRenter> optionalOrderRenter = orderRenterSlave.findById(orderId);
        if (renterAccount != null && optionalOrderRenter.isPresent()) {
            OrderRenter orderRenter = optionalOrderRenter.get();
            // 获取账户余额
            BigDecimal amount = renterAccount.getUseableAmount();
            // 计算融资金额
            BigDecimal financingAmount = getFinancingAmount(orderId, null);
            if (financingAmount != null) {
                BigDecimal benefit = new BigDecimal(0.15);
                // 要满足可用余额大于（保证金加融资还款首付）
                BigDecimal deposit = financingAmount.multiply(benefit)
                        .add(
                                financingAmount.divide(new BigDecimal(orderRenter.getNumberOfPayments()),2,RoundingMode.HALF_UP)
                        );
                if (amount.compareTo(deposit) >= 0) {
                    return new OperationResult<>(OrderResultEnum.FINANCINGABLE, true);
                }
            }
        }
        return new OperationResult<>(OrderResultEnum.NO_FINANCINGABLE);
    }

    @Override
    public OperationResult<RenterInfo> getRenterInfo(Long renterId) {
        if (renterId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        UserRentMerchant renter = userRentMerchantService.findOne(renterId);
        RenterInfo info = new RenterInfo();
        info.setRenterId(renterId);
        info.setRenterName(renter.getMerchantName());
        info.setRenterPhone(renter.getMerchantPhoneNumber());
        info.setAddress(renter.getRentMerchantAddress());
        info.setBadDeptTimes(0);
        info.setOverdueTimes(0);
        info.setFinancingAmount(new BigDecimal(12593));
        info.setOverdueAmount(new BigDecimal(2549));
        return new OperationResult<>(OrderResultEnum.SUCCESS, info);
    }

    @Override
    public OperationResult<RenterInfo> getRenterInfoByOrderId(Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        OrderRenter orderRenter = orderRenterSlave.findOne(orderId);
        if(orderRenter == null){
            return new OperationResult<>(OrderResultEnum.ORDER_NO_EXIST);
        }
        Long renterId = orderRenter.getRenterId();
        UserRentMerchant renter = userRentMerchantService.findOne(renterId);
        RenterInfo info = new RenterInfo();
        info.setRenterId(renterId);
        info.setRenterName(renter.getMerchantName());
        info.setRenterPhone(renter.getMerchantPhoneNumber());
        info.setAddress(renter.getRentMerchantAddress());
        info.setBadDeptTimes(0);
        info.setOverdueTimes(0);
        info.setFinancingAmount(new BigDecimal(12593));
        info.setOverdueAmount(new BigDecimal(2549));
        return new OperationResult<>(OrderResultEnum.SUCCESS, info);
    }

    @Override
    public OperationResult<FinancingInfo> getFinancingInfo(Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        Optional<OrderFunder> optionalOrderFunder = orderFunderSlave.findById(orderId);
        if (optionalOrderFunder.isPresent()) {
            OrderFunder orderFunder = optionalOrderFunder.get();
            FinancingInfo info = new FinancingInfo();
            info.setAccidentBenefit(orderFunder.getAccidentBenefit());
            info.setDeposit(orderFunder.getDeposit());
            info.setFinancingAmount(orderFunder.getFinancingAmount());
            info.setFinancingDeadline(orderFunder.getNumberOfPayments());
            info.setFinancingTime(orderFunder.getCtime());
            info.setSupplierId(orderFunder.getSupplierId());
            info.setResidualScheme(orderFunder.getResidualScheme());
            UserSupplier userSupplier = userSupplierService.findOne(orderFunder.getSupplierId());
            if (userSupplier != null) {
                info.setSupplierName(userSupplier.getMerchantName());
            }

            return new OperationResult<>(OrderResultEnum.SUCCESS, info);
        }
        return new OperationResult<>(OrderResultEnum.SUCCESS,null);
    }

    @Override
    public OperationResult<RentInfo> getRentInfo(Long orderId) {
        if (orderId == null) {
            return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
        }
        Optional<OrderHirer> optionalOrderHirer = orderHirerSlave.findById(orderId);
        if (optionalOrderHirer.isPresent()) {
            OrderHirer orderHirer = optionalOrderHirer.get();
            RentInfo info = new RentInfo();
            info.setAccidentBenefit(orderHirer.getAccidentBenefit());
            info.setDownPayment(orderHirer.getDownPayment());
            info.setLessorId(orderHirer.getLessorId());
            UserHirer userHirer = userHirerService.findOne(orderHirer.getLessorId());
            if (userHirer != null) {
                info.setLessorName(userHirer.getMerchantName());
            }
            info.setMonthPayment(orderHirer.getMonthlyPayment());
            info.setRentDeadline(orderHirer.getNumberOfPeriods());
            info.setPatternPayment(orderHirer.getPatternPayment());
            info.setPaymentAmount(orderHirer.getMonthlyPayment()
                    .multiply(new BigDecimal(orderHirer.getNumberOfPayments()))
                    .add(orderHirer.getAccidentBenefit()));
            info.setRentTime(orderHirer.getCtime());
            return new OperationResult<>(OrderResultEnum.SUCCESS, info);
        }
        return new OperationResult<>(OrderResultEnum.PARAM_ERROR);
    }

    @Override
    public OperationResult getProductPrice(ProductInfoDTO productInfoDTO, Integer paymentNumber) {
        LessorProductPriceQuery query = new LessorProductPriceQuery();
        BeanUtils.copyProperties(productInfoDTO, query);
        query.setPaymentNumber(paymentNumber);
        Sort sort = new Sort(Sort.Direction.ASC, LessorProductPrice.PAYMENT_NUMBER);
        List<LessorProductPrice> lessorProductPrices = lessorProductPriceSlave.findAll(query, sort);
        if(lessorProductPrices == null || lessorProductPrices.size() == 0){
            return new OperationResult(OrderResultEnum.LESSOR_PRICE_NOT_EXIST);
        }
        List<LessorProductPriceDTO> dtos = new ArrayList<>();
        for (LessorProductPrice lessorProductPrice : lessorProductPrices) {
            LessorProductPriceDTO dto = new LessorProductPriceDTO();
            BeanUtils.copyProperties(lessorProductPrice, dto);
            dtos.add(dto);
        }
        return new OperationResult<>(OrderResultEnum.SUCCESS, dtos);
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public JsonResult onlineLoan(LoanDTO loanDTO, Long uid) throws AccountOperationException {
        if(loanDTO.getOrderId() == null || loanDTO.getLoanModel() == null || loanDTO.getLoanAmount() == null){
            return new JsonResult(OrderResultEnum.PARAM_ERROR,false);
        }
        OrderFunderQuery query = new OrderFunderQuery();
        query.setOrderId(loanDTO.getOrderId());
        query.setFunderId(uid);
        OrderFunder orderFunder = orderFunderSlave.findOne(query);
        OrderRenter orderRenter = orderRenterSlave.findOne(loanDTO.getOrderId());
        if(orderFunder != null && orderRenter != null){
            if (!OrderRenterStatus.WATIING_FUNDER_AUDIT.getCode().equals(orderRenter.getOrderStatus())) {
                return new JsonResult(OrderResultEnum.LOAN_ORDER_STATUS_ERROR,false);
            }
            orderFunder.setLoanModel(loanDTO.getLoanModel());
            orderFunder.setFinancingAmount(loanDTO.getLoanAmount());
            // 资金方线上付款成功
            orderFunder.setOrderStatus(OrderFunderStatus.WAITING_DELIVER.getCode());
            orderFunderMaser.save(orderFunder);

            // 修改租赁商订单状态
            orderRenter.setOrderStatus(OrderRenterStatus.FUNDER_ONLINE_LOAN_SUCCESS.getCode());
            orderRenterMaser.save(orderRenter);
            OrderSupplier orderSupplier = generateSupplyOrder(orderRenter, orderFunder,OrderSupplierStatus.WAITING_DELIVER.getCode());
            orderBaseService.renterFunderAccountOperation(orderRenter,orderFunder);
            // 线上放款操作账户
            OperationResult<Boolean> operationResult = accountOperation.onlineLoan(orderRenter,orderFunder,orderSupplier);
            if(!operationResult.getEntity()){
                throw new AccountOperationException(operationResult);
            }
            orderBlockChainService.fundSupplier(orderFunder,orderSupplier,null);
            return new JsonResult(OrderResultEnum.SUCCESS,true);
        }else{
            return new JsonResult(OrderResultEnum.ORDER_NO_EXIST,false);
        }
    }

    /**
     * 计算融资金额
     * 融资金额 = 手机的供应价 - 用户租机首付
     *
     * @param orderId 订单id
     * @param supplierId
     * @return 保证金金额
     */
    private BigDecimal getFinancingAmount(Long orderId, Long supplierId) {
        Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
        if (!orderRenterOptional.isPresent()) {
            return null;
        }
        OrderRenter orderRenter = orderRenterOptional.get();
        OrderProductSupplierQuery query = new OrderProductSupplierQuery();
        query.setProductBrand(orderRenter.getProductBrand());
        query.setProductColor(orderRenter.getProductColor());
        query.setProductStorage(orderRenter.getProductStorage());
        query.setProductName(orderRenter.getProductName());
        query.setSupplierId(supplierId);
        List<ProductSupplier> products = productSupplierSlave.findAll(query);
        if (products == null || products.size() == 0) {
            return null;
        }
        ProductSupplier product = products.get(0);
        // 拿到供应商的供应价格
        BigDecimal supplyPrice = product.getSupplyPrice();
        BigDecimal accidentBenefit = orderRenter.getAccidentBenefit();
        return supplyPrice.subtract(accidentBenefit);
    }

    /**
     * 计算保证金金额
     * 融资金额 = 手机的供应价 - 用户租机首付 -（手机的供应价 - 用户租机首付）*15%
     *
     * @param orderId 订单id
     * @param supplierId
     * @return 保证金金额
     */
    private BigDecimal getDeposit(Long orderId, Long supplierId) {
        Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
        if (!orderRenterOptional.isPresent()) {
            return null;
        }
        OrderRenter orderRenter = orderRenterOptional.get();
        OrderProductSupplierQuery query = new OrderProductSupplierQuery();
        query.setProductBrand(orderRenter.getProductBrand());
        query.setProductColor(orderRenter.getProductColor());
        query.setProductStorage(orderRenter.getProductStorage());
        query.setProductName(orderRenter.getProductName());
        query.setSupplierId(supplierId);
        List<ProductSupplier> products = productSupplierSlave.findAll(query);
        if (products == null || products.size() == 0) {
            return null;
        }
        ProductSupplier product = products.get(0);
        BigDecimal benefit = new BigDecimal("0.15");
        return product.getSupplyPrice().multiply(benefit);
    }

    /**
     * @param orderHirer 出租方订单
     * @return dto
     */
    private OrderHirerDTO wrapOrderHirer2DTO(OrderHirer orderHirer) {
        OrderHirerDTO dto = new OrderHirerDTO();
        BeanUtils.copyProperties(orderHirer, dto);
        dto.setConsumerName(orderHirer.getUserRealname());
        dto.setConsumerPhone(orderHirer.getUserMobile());
        dto.setConsumerIdentityNumber(orderHirer.getUserIdNumber());
        dto.setConsumerCreditLine(orderHirer.getUserCreditLine());
        dto.setConsumerCreditScore(orderHirer.getUserCreditScore());
        dto.setConsumerUid(orderHirer.getUid());
        dto.setProductStorage(orderHirer.getProductStorage());
        dto.setProductRandomMemory(orderHirer.getProductRandomMemory());
        dto.setRentDeadlineMonth(orderHirer.getNumberOfPayments());
        dto.setRentDeadlineDay(orderHirer.getNumberOfPayments() * 30);
        dto.setOrderTime(orderHirer.getCtime());
        dto.setRenterId(orderHirer.getMerchantId());
        dto.setRenterName(orderHirer.getMerchantName());
        dto.setRenterPhone(orderHirer.getMerchantMobile());
        dto.setMachineNumber(1);
        dto.setUid(orderHirer.getLessorId());
        dto.setConsumerAddress(orderHirer.getUserAddress());
        dto.setConsumerBedDebtTimes(0);
        OrderRenter orderRenter = orderRenterSlave.findOne(orderHirer.getOrderId());
        if(orderRenter != null){
            dto.setConsumerOrderTime(orderRenter.getCtime());
        }
        return dto;
    }

    /**
     * 将orderFunder转换成DTO返回给前端
     *
     * @param orderFunder 从数据库中查出的orderFunder
     * @return dto
     */
    private OrderFunderDTO wrapOrderFunder2DTO(OrderFunder orderFunder) {
        OrderFunderDTO orderFunderDTO = new OrderFunderDTO();
        BeanUtils.copyProperties(orderFunder, orderFunderDTO);
        orderFunderDTO.setOrderTime(orderFunder.getCtime());
        orderFunderDTO.setConsumerName(orderFunder.getUserRealname());
        orderFunderDTO.setConsumerPhone(orderFunder.getUserMobile());
        orderFunderDTO.setConsumerIdentityNumber(orderFunder.getUserIdNumber());
        orderFunderDTO.setConsumerCreditScore(orderFunder.getUserCreditScore());
        orderFunderDTO.setRentDeadlineMonth(orderFunder.getNumberOfPayments());
        orderFunderDTO.setRentDeadlineDay(orderFunder.getNumberOfPayments() * 30);
        orderFunderDTO.setConsumerCreditLine(orderFunder.getUserCreditLine());
        orderFunderDTO.setRenterId(orderFunder.getMerchantId());
        orderFunderDTO.setRenterName(orderFunder.getMerchantName());
        orderFunderDTO.setFinancingAmount(orderFunder.getFinancingAmount());
        orderFunderDTO.setFinancingDeadline(orderFunder.getNumberOfPeriods());
        orderFunderDTO.setMachineNumber(1);
        orderFunderDTO.setRenterPhone(orderBaseService.getRenterPhone(orderFunder.getMerchantId()));
        orderFunderDTO.setDeposit(orderFunder.getDeposit());
        orderFunderDTO.setSupplierId(orderFunder.getSupplierId());
        orderFunderDTO.setSupplierName(orderBaseService.getSupplierName(orderFunder.getSupplierId()));
        orderFunderDTO.setConsumerBedDebtTimes(0);
        orderFunderDTO.setUid(orderFunder.getFunderId());
        Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderFunder.getOrderId());
        if(orderRenterOptional.isPresent()){
            OrderRenter orderRenter = orderRenterOptional.get();
            orderFunderDTO.setConsumerAddress(orderRenter.getUserAddress());
            orderFunderDTO.setConsumerOrderTime(orderRenter.getCtime());
        }
        return orderFunderDTO;
    }

    /**
     * 将供应商订单转化为dto
     *
     * @param orderSupplier 供应商订单
     * @return dto
     */
    private OrderSupplierDTO wrapOrderSupplier2DTO(OrderSupplier orderSupplier) {

        OrderSupplierDTO orderSupplierDTO = new OrderSupplierDTO();
        BeanUtils.copyProperties(orderSupplier, orderSupplierDTO);
        orderSupplierDTO.setConsumerPhone(orderSupplier.getReceiverMobile());
        orderSupplierDTO.setConsumerAddress(orderSupplier.getReceiverAddress());
        orderSupplierDTO.setConsumerName(orderSupplier.getReceiverName());

        // 默认机器数量为1
        orderSupplierDTO.setMachineNumber(1);
        orderSupplierDTO.setOrderTime(orderSupplier.getCtime());
        Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderSupplier.getOrderId());
        if (orderRenterOptional.isPresent()) {
            OrderRenter orderRenter = orderRenterOptional.get();
            orderSupplierDTO.setRenterId(orderRenter.getRenterId());
            orderSupplierDTO.setRenterName(orderRenter.getRenterName());
            orderSupplierDTO.setRenterPhone(orderBaseService.getRenterPhone(orderRenter.getRenterId()));
            orderSupplierDTO.setRentDeadlineMonth(orderRenter.getNumberOfPayments());
            orderSupplierDTO.setMonthlyPayment(orderRenter.getMonthlyPayment());
            orderSupplierDTO.setDownPayment(orderRenter.getDownPayment());
            orderSupplierDTO.setAccidentBenefit(orderRenter.getAccidentBenefit());
            orderSupplierDTO.setRentDeadlineDay(orderRenter.getNumberOfPayments() * 30);
            orderSupplierDTO.setConsumerUid(orderRenter.getUid());
            orderSupplierDTO.setConsumerOrderTime(orderRenter.getCtime());
        }
        // todo 查询用户坏账次数
        orderSupplierDTO.setConsumerBedDebtTimes(1);
        Optional<OrderFunder> orderFunderOptional = orderFunderSlave.findById(orderSupplier.getOrderId());
        if (orderFunderOptional.isPresent()) {
            OrderFunder orderFunder = orderFunderOptional.get();
            orderSupplierDTO.setFunderId(orderFunder.getFunderId());
            orderSupplierDTO.setLoanTime(orderSupplier.getCtime());
        }
        orderSupplierDTO.setDeliverCompany(orderSupplier.getExpressCompany());
        orderSupplierDTO.setDeliverTime(orderSupplier.getExpressTime());
        orderSupplierDTO.setDeliverCode(orderSupplier.getExpressNumber());
        orderSupplierDTO.setSerialNumber(orderSupplier.getSerialNumber());
        return orderSupplierDTO;
    }


    /**
     * 修改租赁商订单的类型，融资为融资订单，租赁为租赁订单
     *
     * @param type    订单类型
     * @param orderId 订单id
     */
    private void updateOrderRenterStatusType(OrderRenterStatus status,OrderType type, Long orderId) {
        Optional<OrderRenter> orderRenterOptional = orderRenterSlave.findById(orderId);
        if (orderRenterOptional.isPresent()) {
            OrderRenter orderRenter = orderRenterOptional.get();
            orderRenter.setOrderType(type.getCode());
            orderRenter.setOrderStatus(status.getCode());
            orderRenterMaser.updateById(orderRenter,orderId,OrderRenter.ORDER_STATUS,OrderRenter.ORDER_TYPE);
        }
    }

    /**
     * 资金方线下放款
     * 线下付款，将资金方订单状态变为付款中
     *
     * @param loanDTO     放款信息
     * @param orderFunder 资金方订单
     * @return 是否成功
     */
    private boolean offlineLoan(LoanDTO loanDTO, OrderFunder orderFunder) {
        orderFunder.setOrderStatus(OrderFunderStatus.PAYMENTING.getCode());
        orderFunder.setFinancingAmount(loanDTO.getLoanAmount());
        orderFunder.setLoanModel(loanDTO.getLoanModel());
        orderFunderMaser.save(orderFunder);
        return true;
    }

    /**
     * 生成供应商订单并保存到数据库
     *
     * @param orderRenter 租赁商订单
     * @param orderFunder 资金方订单
     */
    private OrderSupplier generateSupplyOrder(OrderRenter orderRenter, OrderFunder orderFunder,Integer supplierOrderStatus) {

        OrderSupplier orderSupplier = new OrderSupplier();
        orderSupplier.setOrderId(orderRenter.getOrderId());
        orderSupplier.setPartnerId(orderRenter.getPartnerId());
        orderSupplier.setPartnerOrderId(orderRenter.getPartnerOrderId());
        orderSupplier.setSupplierId(orderFunder.getSupplierId());
        orderSupplier.setMerchantId(orderFunder.getMerchantId());
        orderSupplier.setMerchantName(orderFunder.getMerchantName());
        orderSupplier.setUid(orderFunder.getUid());
        orderSupplier.setReceiverName(orderFunder.getUserRealname());
        orderSupplier.setReceiverAddress(orderRenter.getUserAddress());
        orderSupplier.setReceiverMobile(orderRenter.getUserMobile());
        orderSupplier.setProductBrand(orderRenter.getProductBrand());
        orderSupplier.setProductName(orderRenter.getProductName());
        orderSupplier.setProductColor(orderRenter.getProductColor());
        orderSupplier.setProductRandomMemory(orderRenter.getProductRandomMemory());
        orderSupplier.setProductStorage(orderRenter.getProductStorage());
        orderSupplier.setDeleteStatus(OrderSupplier.NO_DELETE_STATUS);
        // 供应商还未确认收款，待发货状态
        orderSupplier.setOrderStatus(supplierOrderStatus);
        orderSupplier.setCtime(null);
        orderSupplier.setUtime(null);
        orderSupplier.setPlatformCreditScore(orderRenter.getPlatformCreditScore());
        // 查询供应商商品的供应价格
        OrderProductSupplierQuery query = new OrderProductSupplierQuery();
        query.setProductBrand(orderRenter.getProductBrand());
        query.setProductColor(orderRenter.getProductColor());
        query.setProductStorage(orderRenter.getProductStorage());
        query.setProductName(orderRenter.getProductName());
        List<ProductSupplier> products = productSupplierSlave.findAll(query);
        if (CollectionUtils.isNotEmpty(products)) {
            ProductSupplier product = products.get(0);
            // 拿到供应商的供应价格
            orderSupplier.setTotalAccount(product.getSupplyPrice());
        }
        orderSuppMaster.save(orderSupplier);
        orderBaseService.messagePush(orderSupplier.getSupplierId(),orderSupplier.getOrderId(),orderRenter.getPartnerOrderId(),MessagePushEnum.DELIVER_APPLY);
        return orderSupplier;
    }
}
