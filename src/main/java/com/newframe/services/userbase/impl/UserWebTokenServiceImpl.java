package com.newframe.services.userbase.impl;

import com.newframe.entity.user.UserWebToken;
import com.newframe.repositories.dataMaster.user.UserWebTokenMaster;
import com.newframe.repositories.dataQuery.user.UserWebTokenQuery;
import com.newframe.repositories.dataSlave.user.UserWebTokenSlave;
import com.newframe.services.userbase.UserWebTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 *
 *  用户pc的token
 *
 * This class corresponds to the database table user_web_token
 *
 * app用户的token
 * @mbggenerated do_not_delete_during_merge
 */
@Service
public class UserWebTokenServiceImpl implements UserWebTokenService {

    @Autowired
    private UserWebTokenMaster userWebTokenMaster;
    @Autowired
    private UserWebTokenSlave userWebTokenSlave;

    /**
     * 根据uid查询用户的webtoken
     *
     * @param uid
     * @return
     */
    @Override
    public UserWebToken findOne(Long uid) {
        if (null == uid){
            return null;
        }
        return userWebTokenSlave.findOne(uid);
    }

    /**
     * 根据uid更新token
     *
     * @param uid
     * @return
     */
    @Override
    public int updateByUid(Long uid, String token) {
        if(uid == null){
            return 0;
        }
        UserWebToken userWebToken = new UserWebToken();
        userWebToken.setToken(token);
        UserWebTokenQuery query = new UserWebTokenQuery();
        query.setUid(userWebToken.getUid());
        String[] array = new String[]{"token"};
        return userWebTokenMaster.update(userWebToken, query, array);
    }

    /**
     * 根据uid删除记录
     *
     * @param uid
     * @return
     */
    @Override
    public void deleteByUid(Long uid) {
        userWebTokenMaster.deleteById(uid);
    }

    /**
     * 插入用户的token记录
     *
     * @param uid
     * @return
     */
    @Override
    public UserWebToken insert(Long uid) {
        UserWebToken userWebToken = new UserWebToken();
        userWebToken.setUid(uid);
        userWebToken.setToken(UUID.randomUUID().toString());
        return userWebTokenMaster.save(userWebToken);
    }
}