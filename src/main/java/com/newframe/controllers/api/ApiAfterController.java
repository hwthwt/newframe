package com.newframe.controllers.api;

import com.newframe.controllers.BaseController;
import com.newframe.controllers.JsonResult;
import com.newframe.dto.OperationResult;
import com.newframe.dto.after.request.DrawAssetSearchDTO;
import com.newframe.dto.after.request.FunderSearchDTO;
import com.newframe.dto.after.request.ModifyFunderDTO;
import com.newframe.dto.after.request.RoleListSearchDTO;
import com.newframe.dto.after.response.*;
import com.newframe.enums.SystemCode;
import com.newframe.enums.user.RequestResultEnum;
import com.newframe.services.after.AfterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author WangBin
 */
@RestController
@RequestMapping("/rent/api/inner/after/")
public class ApiAfterController extends BaseController {

    @Autowired
    private AfterService afterService;

    /**
     * 后台登陆
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public JsonResult login(String username, String password){
        OperationResult<UserDTO> result = afterService.login(username, password);
        if (result.getEntity() == null){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 获取角色列表
     * @param condition
     * @return
     */
    @PostMapping("merchantList")
    public JsonResult getMerchantList(RoleListSearchDTO condition){
        OperationResult<RoleListDTO> result = afterService.getMerchantList(condition);
        return success(result.getEntity());
    }

    /**
     * 获取角色申请详细信息
     * @param roleApplyId
     * @return
     */
    @PostMapping("merchantInfo")
    public JsonResult getMerchantInfo(Long roleApplyId){
        OperationResult<RoleApplyInfoDTO> result = afterService.getMerchantInfo(roleApplyId);
        return success(result.getEntity());
    }

    /**
     * 审核通过
     * @param uid
     * @param roleApplyId
     * @return
     */
    @PostMapping("passCheck")
    public JsonResult passCheck(Long uid, Long roleApplyId){
        OperationResult<Boolean> result = afterService.passCheck(uid, roleApplyId);
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 审核失败
     * @param uid
     * @param roleApplyId
     * @param remarks
     * @return
     */
    @PostMapping("failCheck")
    public JsonResult failCheck(Long uid, Long roleApplyId, String remarks){
        OperationResult<Boolean> result = afterService.failCheck(uid, roleApplyId, remarks);
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 获取白名单列表
     * @param condiiton
     * @return
     */
    @PostMapping("whiteList")
    public JsonResult getWhiteList(FunderSearchDTO condiiton){
        OperationResult<WhiteFunderListDTO> result = afterService.getWhiteList(condiiton);
        return success(result.getEntity());
    }

    /**
     * 获取非白名单列表
     * @return
     */
    @PostMapping("blackFunderList")
    public JsonResult getBlackFunderList(){
        OperationResult<List<FunderDTO>> result = afterService.getBlackFunderList();
        return success(result.getEntity());
    }

    /**
     * 获取资金方信息
     * @param funderUid
     * @return
     */
    @PostMapping("getFunderInfo")
    public JsonResult getFunderInfo(Long funderUid){
        OperationResult<FunderDTO> result = afterService.getFunderInfo(funderUid);
        return success(result.getEntity());
    }

    /**
     * 添加资金方
     * @return
     */
    @PostMapping("addFunder")
    public JsonResult addFunder(ModifyFunderDTO modifyFunderDTO){
        OperationResult<Boolean> result = afterService.addFunder(modifyFunderDTO.getUid(), modifyFunderDTO.getFunderUids());
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 删除资金方
     * @return
     */
    @PostMapping("removeFunder")
    public JsonResult removeFunder(Long uid, Long funderUid){
        OperationResult<Boolean> result = afterService.removeFunder(uid, funderUid);
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 修改资金方
     * @param uid
     * @param funderUid
     * @return
     */
    @PostMapping("modifyFunder")
    public JsonResult modifyFunder(Long uid, List<Long> funderUid){
        OperationResult<Boolean> result = afterService.modifyFunder(uid, funderUid);
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 获取资金提取列表
     * @param uid
     * @param drawAssetSearchDTO
     * @return
     */
    @PostMapping("getDrawAssetList")
    public JsonResult getDrawAssetList(Long uid, DrawAssetSearchDTO drawAssetSearchDTO){
        if(uid == null){
            return error(SystemCode.NEED_LOGIN);
        }
        OperationResult<DrawAssetListDTO> result = afterService.getDrawAssetList(uid, drawAssetSearchDTO);
        return success(result.getEntity());
    }

    /**
     * 提款审核通过
     * @param uid
     * @param orderId
     * @return
     */
    @PostMapping("passDrawAssetCheck")
    public  JsonResult passDrawAssetCheck(Long uid, Long orderId){
        if(uid == null){
            return error(SystemCode.NEED_LOGIN);
        }
        OperationResult<Boolean> result = afterService.passDrawAssetCheck(uid, orderId);
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 提款审核不通过
     * @param uid
     * @param orderId
     * @return
     */
    @PostMapping("failDrawAssetCheck")
    public  JsonResult failDrawAssetCheck(Long uid, Long orderId, String remarks){
        if(uid == null){
            return error(SystemCode.NEED_LOGIN);
        }
        OperationResult<Boolean> result = afterService.failDrawAssetCheck(uid, orderId, remarks);
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }

    /**
     * 设置利率
     * @param rate
     * @return
     */
    @GetMapping("setRate")
    public JsonResult setRate(BigDecimal rate){
        OperationResult<Boolean> result = afterService.setRate(rate);
        if(!result.getEntity()){
            return error(result.getErrorCode());
        }
        return success(result.getEntity());
    }
}
