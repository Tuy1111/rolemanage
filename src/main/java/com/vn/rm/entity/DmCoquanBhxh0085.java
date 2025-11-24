package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_COQUAN_BHXH_0085")
@Entity
public class DmCoquanBhxh0085 {

    @Id
    @Column(name = "IDBANGHI", nullable = false)
    private String idbanghi;

    @Column(name = "ID_BHXH")
    private String idBhxh;

    @Column(name = "MA_DVI", length = 20)
    private String maDvi;

    @Column(name = "TEN_DVI")
    private String tenDvi;

    @Column(name = "MA_CHA", length = 20)
    private String maCha;

    @Column(name = "DIA_CHI")
    private String diaChi;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TAI_KHOAN", length = 100)
    private String taiKhoan;

    @Column(name = "KY_HIEU_THU", length = 20)
    private String kyHieuThu;

    @Column(name = "DM_DBHC_ID", length = 20)
    private String dmDbhcId;

    @Column(name = "TEN_TAT", length = 100)
    private String tenTat;

    @Column(name = "TYPE", length = 10)
    private String type;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "HIEU_LUC", length = 10)
    private String hieuLuc;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    // ===== Getters & Setters =====
    public String getIdbanghi() {
        return idbanghi;
    }

    public void setIdbanghi(String idbanghi) {
        this.idbanghi = idbanghi;
    }

    public String getIdBhxh() {
        return idBhxh;
    }

    public void setIdBhxh(String idBhxh) {
        this.idBhxh = idBhxh;
    }

    public String getMaDvi() {
        return maDvi;
    }

    public void setMaDvi(String maDvi) {
        this.maDvi = maDvi;
    }

    public String getTenDvi() {
        return tenDvi;
    }

    public void setTenDvi(String tenDvi) {
        this.tenDvi = tenDvi;
    }

    public String getMaCha() {
        return maCha;
    }

    public void setMaCha(String maCha) {
        this.maCha = maCha;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getKyHieuThu() {
        return kyHieuThu;
    }

    public void setKyHieuThu(String kyHieuThu) {
        this.kyHieuThu = kyHieuThu;
    }

    public String getDmDbhcId() {
        return dmDbhcId;
    }

    public void setDmDbhcId(String dmDbhcId) {
        this.dmDbhcId = dmDbhcId;
    }

    public String getTenTat() {
        return tenTat;
    }

    public void setTenTat(String tenTat) {
        this.tenTat = tenTat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getHieuLuc() {
        return hieuLuc;
    }

    public void setHieuLuc(String hieuLuc) {
        this.hieuLuc = hieuLuc;
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
}
