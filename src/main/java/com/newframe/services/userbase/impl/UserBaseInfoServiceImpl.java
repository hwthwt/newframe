package com.newframe.services.userbase.impl;

import com.newframe.entity.user.UserBaseInfo;
import com.newframe.repositories.dataMaster.user.UserBaseInfoMaster;
import com.newframe.repositories.dataQuery.user.UserBaseInfoQuery;
import com.newframe.repositories.dataSlave.user.UserBaseInfoSlave;
import com.newframe.services.userbase.UserBaseInfoService;
import com.newframe.utils.cache.IdGlobalGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *  用户的基本信息
 *
 * This class corresponds to the database table user_base_info
 *
 * @author WangBin
 * @mbggenerated do_not_delete_during_merge
 */
@Service
public class UserBaseInfoServiceImpl implements UserBaseInfoService {

    @Autowired
    private UserBaseInfoMaster userBaseInfoMaster;
    @Autowired
    private UserBaseInfoSlave userBaseInfoSlave;
    @Autowired
    private IdGlobalGenerator idGlobalGenerator;

    /**
     * 插入用户基本信息
     *
     * @param userBaseInfo
     * @return
     */
    @Override
    public UserBaseInfo insert(UserBaseInfo userBaseInfo) {
        if(userBaseInfo == null) {
            return null;
        }
        userBaseInfo.setUid(idGlobalGenerator.getSeqId(UserBaseInfo.class));
        return userBaseInfoMaster.save(userBaseInfo);
    }

    /**
     * 根据uid修改用户的基本信息
     *
     * @param userBaseInfo
     * @return
     */
    @Override
    public int updateByUid(UserBaseInfo userBaseInfo) {
        if(userBaseInfo == null){
            return 0;
        }
        UserBaseInfoQuery query = new UserBaseInfoQuery();
        query.setUid(userBaseInfo.getUid());
        List<String> updateFields = new ArrayList();
        if (StringUtils.isNotEmpty(userBaseInfo.getAvatar())){
            updateFields.add("avatar");
        }
        if (userBaseInfo.getGender() != null){
            updateFields.add("gender");
        }
        if (StringUtils.isNotEmpty(userBaseInfo.getUserName())){
            updateFields.add("userName");
        }
        if (userBaseInfo.getUserStatus() != null){
            updateFields.add("userStatus");
        }
        if(StringUtils.isNotEmpty(userBaseInfo.getPhoneNumber())){
            updateFields.add("phoneNumber");
        }
        String[] array = new String[updateFields.size()];
        updateFields.toArray(array);
        return userBaseInfoMaster.update(userBaseInfo, query, array);
    }

    /**
     * 根据uid查询用户基本信息
     *
     * @param uid
     * @return
     */
    @Override
    public UserBaseInfo findOne(Long uid) {
        if (null == uid){
            return null;
        }
        return userBaseInfoSlave.findOne(uid);
    }

    /**
     * 根据手机号查询
     *
     * @param mobile
     * @return
     */
    @Override
    public UserBaseInfo findOne(String mobile) {
        UserBaseInfoQuery query = new UserBaseInfoQuery();
        query.setMobile(mobile);
        return userBaseInfoSlave.findOne(query);
    }

    /**
     * 根据uid删除角色基本信息
     *
     * @param uid
     * @return
     */
    @Override
    public void removeByUid(Long uid) {
        userBaseInfoMaster.deleteById(uid);
    }

    /**
     * 检查手机号是否存在
     *
     * @param mobile
     * @return
     */
    @Override
    public boolean checkMmobileExists(String mobile) {

        UserBaseInfoQuery query = new UserBaseInfoQuery();
        query.setMobile(mobile);
        List<UserBaseInfo> infos = userBaseInfoSlave.findAll(query);
        return CollectionUtils.isNotEmpty(infos);
    }
}