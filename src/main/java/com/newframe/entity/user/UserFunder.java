package com.newframe.entity.user;

import com.newframe.enums.user.RoleStatusEnum;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 *  资金方
 *
 * This class was generated by MyBatis Generator.
 * This class corresponds to the database table user_funder
 *
 * @mbggenerated do_not_delete_during_merge
 */
@Data
@Entity
@Table(name = "user_funder")
public class UserFunder {
    /**
     * 用户ID
     * uid
     */
    @Id
    @Column(name = "uid")
    private Long uid;

    /**
     * 手机号
     * phone_number
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * 商家名称
     * merchant_name
     */
    @Column(name = "merchant_name")
    private String merchantName;

    /**
     * 法人名称
     * legal_entity
     */
    @Column(name = "legal_entity")
    private String legalEntity;

    /**
     * 法人身份证号
     * legal_entity_id_number
     */
    @Column(name = "legal_entity_id_number")
    private String legalEntityIdNumber;

    /**
     * 法人正面身份证
     * id_card_front_file
     */
    @Column(name = "id_card_front_file")
    private String idCardFrontFile;

    /**
     * 法人背面身份证
     * id_card_back_file
     */
    @Column(name = "id_card_back_file")
    private String idCardBackFile;

    /**
     * 联系人手机号
     * contacts_phone_number
     */
    @Column(name = "contacts_phone_number")
    private String contactsPhoneNumber;

    /**
     * 职位
     * job
     */
    @Column(name = "job")
    private String job;

    /**
     * 紧急联系人
     * top_contacts
     */
    @Column(name = "top_contacts")
    private String topContacts;

    /**
     * 紧急联系人手机号
     * top_contacts_phone_number
     */
    @Column(name = "top_contacts_phone_number")
    private String topContactsPhoneNumber;

    /**
     * 与紧急联系人关系
     * relationship
     */
    @Column(name = "relationship")
    private Integer relationship;

    /**
     * 营业职业编号
     * business_license_number
     */
    @Column(name = "business_license_number")
    private String businessLicenseNumber;

    /**
     * 营业执照文件
     * business_license_file
     */
    @Column(name = "business_license_file")
    private String businessLicenseFile;

    /**
     * 授权委托书文件  ,拼接的字符串
     * letter_of_attorney_file
     */
    @Column(name = "letter_of_attorney_file")
    private String letterOfAttorneyFile;

    /**
     * 经营资质文件   ,拼接的字符串
     * business_qualification_file
     */
    @Column(name = "business_qualification_file")
    private String businessQualificationFile;

    /**
     * 角色状态
     * role_status
     */
    @Column(name = "role_status")
    private Integer roleStatus;

    /**
     * 审核的后台uid
     * check_uid
     */
    @Column(name = "check_uid")
    private Long checkUid;

    /**
     * 后台审核人姓名
     * check_person
     */
    @Column(name = "check_person")
    private String checkPerson;

    /**
     * 是否在白名单
     * is_white
     */
    @Column(name = "is_white")
    private Boolean isWhite;

    /**
     * 创建时间
     * ctime
     */
    @Column(name = "ctime")
    private Integer ctime;

    /**
     * 更新时间
     * utime
     */
    @Column(name = "utime")
    private Integer utime;

    public UserFunder() {
    }

    public UserFunder(UserRoleApply userRoleApply) {
        this.uid = userRoleApply.getUid();
        this.phoneNumber = userRoleApply.getPhoneNumber();
        this.merchantName = userRoleApply.getMerchantName();
        this.legalEntity = userRoleApply.getLegalEntity();
        this.legalEntityIdNumber = userRoleApply.getLegalEntityIdNumber();
        this.idCardFrontFile = userRoleApply.getIdCardFrontFile();
        this.idCardBackFile = userRoleApply.getIdCardBackFile();
        this.contactsPhoneNumber = userRoleApply.getContactsPhoneNumber();
        this.job = userRoleApply.getJob();
        this.topContacts = userRoleApply.getTopContacts();
        this.topContactsPhoneNumber = userRoleApply.getTopContactsPhoneNumber();
        this.relationship = userRoleApply.getRelationship();
        this.businessLicenseNumber = userRoleApply.getBusinessLicenseNumber();
        this.businessLicenseFile = userRoleApply.getBusinessLicenseFile();
        this.letterOfAttorneyFile = userRoleApply.getLetterOfAttorneyFile();
        this.businessQualificationFile = userRoleApply.getBusinessQualificationFile();
        this.roleStatus = RoleStatusEnum.NORMAL.getRoleStatue();
        this.checkUid = userRoleApply.getCheckUid();
        this.checkPerson = userRoleApply.getCheckPerson();
        this.isWhite = false;
    }
}