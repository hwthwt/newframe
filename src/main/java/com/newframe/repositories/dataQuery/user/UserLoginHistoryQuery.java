package com.newframe.repositories.dataQuery.user;

import com.newframe.entity.user.UserLoginHistory;
import com.newframe.utils.query.BaseQuery;
import com.newframe.utils.query.annotation.QBindEntity;
import lombok.Data;

@Data
@QBindEntity(entityClass = UserLoginHistory.class)
public class UserLoginHistoryQuery extends BaseQuery {
}