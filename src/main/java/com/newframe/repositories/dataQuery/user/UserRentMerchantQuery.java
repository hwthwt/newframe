package com.newframe.repositories.dataQuery.user;

import com.newframe.entity.user.UserRentMerchant;
import com.newframe.utils.query.BaseQuery;
import com.newframe.utils.query.Where;
import com.newframe.utils.query.annotation.QBindAttrField;
import com.newframe.utils.query.annotation.QBindEntity;
import lombok.Data;

@Data
@QBindEntity(entityClass = UserRentMerchant.class)
public class UserRentMerchantQuery extends BaseQuery {

    @QBindAttrField(fieldName = "uid", where = Where.equal)
    private Long uid;
}