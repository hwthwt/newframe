package com.newframe.entity.user;

import com.newframe.dto.user.request.ProductModifyDTO;
import com.newframe.utils.BigDecimalUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 *
 * This class was generated by MyBatis Generator.
 * This class corresponds to the database table product_supplier
 *
 * @mbggenerated do_not_delete_during_merge
 */
@Data
@Entity
@Table(name = "product_supplier")
public class ProductSupplier {
    /**
     * id
     * id
     */
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 供应商id
     * supplier_id
     */
    @Column(name = "supplier_id")
    private Long supplierId;

    /**
     * 品牌
     * brand
     */
    @Column(name = "brand")
    private String brand;

    /**
     * 型号
     * model
     */
    @Column(name = "model")
    private String model;

    /**
     * 内存
     * ram
     */
    @Column(name = "ram")
    private String ram;

    /**
     * 机身容量
     * rom
     */
    @Column(name = "rom")
    private String rom;

    /**
     * 颜色
     * color
     */
    @Column(name = "color")
    private String color;

    /**
     * 市场指导价
     * guide_price
     */
    @Column(name = "guidePrice")
    private BigDecimal guidePrice;

    /**
     * 供应价
     * supply_price
     */
    @Column(name = "supplyPrice")
    private BigDecimal supplyPrice;

    /**
     * 剩余库存
     * surplus_stock
     */
    @Column(name = "surplusStock")
    private Integer surplusStock;

    /**
     * 冻结库存
     * frozen_stock
     */
    @Column(name = "frozenStock")
    private Integer frozenStock;

    /**
     * ctime
     * ctime
     */
    @Column(name = "ctime")
    private Integer ctime;

    /**
     * utime
     * utime
     */
    @Column(name = "utime")
    private Integer utime;

    /**
     * 删除标记（1：删除，0：未删除，默认0）
     * is_delated
     */
    @Column(name = "is_delated")
    private Integer isDelated;

    public ProductSupplier() {
    }

    public ProductSupplier(Long uid, ProductModifyDTO condition) {
        this.id = condition.getProductId();
        this.supplierId = uid;
        this.brand = condition.getBrand();
        this.model = condition.getModel();
        this.ram = StringUtils.upperCase(StringUtils.trim(condition.getRam()));
        this.rom = StringUtils.upperCase(StringUtils.trim(condition.getRom()));
        this.color = condition.getColor();
        this.guidePrice = BigDecimalUtils.compareTo(condition.getGuidePrice()) ? condition.getGuidePrice() : BigDecimal.ZERO;
        this.supplyPrice = BigDecimalUtils.compareTo(condition.getSupplyPrice()) ? condition.getSupplyPrice() : BigDecimal.ZERO;
        this.surplusStock = condition.getSurplusStock() != null && condition.getSurplusStock() > 0 ? condition.getSurplusStock(): 0;
        this.frozenStock = 0;
        this.isDelated = 0;
    }
}