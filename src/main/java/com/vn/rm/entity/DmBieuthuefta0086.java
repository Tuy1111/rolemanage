package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_BIEU_THUE_FTA_0086")
@Entity
public class DmBieuthuefta0086 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "MA")
    private String ma;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "THUE_SUAT", precision = 19, scale = 6)
    private BigDecimal thueSuat;

    @Column(name = "NAM_APDUNG")
    private Integer namApdung;

    @Column(name = "VALID")
    private Integer valid;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGUOI_TAO")
    private String nguoiTao;

    @Column(name = "MA_BIEU_THUE")
    private String maBieuThue;

    @Column(name = "TEN_BIEU_THUE")
    private String tenBieuThue;

    @Column(name = "GHI_CHU")
    private String ghiChu;


    public String getIdBanghi() {
        return idBanghi;
    }

    public void setIdBanghi(String idBanghi) {
        this.idBanghi = idBanghi;
    }

    public String getIdCol() {
        return idCol;
    }

    public void setIdCol(String idCol) {
        this.idCol = idCol;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public BigDecimal getThueSuat() {
        return thueSuat;
    }

    public void setThueSuat(BigDecimal thueSuat) {
        this.thueSuat = thueSuat;
    }

    public Integer getNamApdung() {
        return namApdung;
    }

    public void setNamApdung(Integer namApdung) {
        this.namApdung = namApdung;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public LocalDate getNgayHl() {
        return ngayHl;
    }

    public void setNgayHl(LocalDate ngayHl) {
        this.ngayHl = ngayHl;
    }

    public LocalDate getNgayKt() {
        return ngayKt;
    }

    public void setNgayKt(LocalDate ngayKt) {
        this.ngayKt = ngayKt;
    }

    public String getVanBanBh() {
        return vanBanBh;
    }

    public void setVanBanBh(String vanBanBh) {
        this.vanBanBh = vanBanBh;
    }

    public LocalDate getNgayVb() {
        return ngayVb;
    }

    public void setNgayVb(LocalDate ngayVb) {
        this.ngayVb = ngayVb;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public String getMaBieuThue() {
        return maBieuThue;
    }

    public void setMaBieuThue(String maBieuThue) {
        this.maBieuThue = maBieuThue;
    }

    public String getTenBieuThue() {
        return tenBieuThue;
    }

    public void setTenBieuThue(String tenBieuThue) {
        this.tenBieuThue = tenBieuThue;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
