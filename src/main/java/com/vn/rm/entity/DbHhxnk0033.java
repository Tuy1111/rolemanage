package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.time.LocalDate;

@JmixEntity
@Entity
@Table(name = "DB_HHXNK_0033")
public class DbHhxnk0033 {

    @Id
    @Column(name = "ID", nullable = false, length = 20)
    private String id; // Khóa chính

    @Column(name = "IDBANGHI", length = 20)
    private String idbanghi;

    @Column(name = "MA_HS", length = 50)
    private String maHs;

    @Column(name = "MO_TA_HH", length = 1024)
    private String moTaHh;

    @Column(name = "MO_TA_HH_TA", length = 1024)
    private String moTaHhTa;

    @Column(name = "DON_VI", length = 100)
    private String donVi;

    @Column(name = "VALID", length = 5)
    private String valid;

    @Column(name = "NGAY_SD")
    private LocalDate ngaySd;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    @Column(name = "VAN_BAN_BH", columnDefinition = "TEXT")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGUOI_TAO", length = 100)
    private String nguoiTao;

    @Column(name = "NAM_APDUNG", length = 10)
    private String namApDung;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdbanghi() {
        return idbanghi;
    }

    public void setIdbanghi(String idbanghi) {
        this.idbanghi = idbanghi;
    }

    public String getMaHs() {
        return maHs;
    }

    public void setMaHs(String maHs) {
        this.maHs = maHs;
    }

    public String getMoTaHh() {
        return moTaHh;
    }

    public void setMoTaHh(String moTaHh) {
        this.moTaHh = moTaHh;
    }

    public String getMoTaHhTa() {
        return moTaHhTa;
    }

    public void setMoTaHhTa(String moTaHhTa) {
        this.moTaHhTa = moTaHhTa;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public LocalDate getNgaySd() {
        return ngaySd;
    }

    public void setNgaySd(LocalDate ngaySd) {
        this.ngaySd = ngaySd;
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

    public String getNamApDung() {
        return namApDung;
    }

    public void setNamApDung(String namApDung) {
        this.namApDung = namApDung;
    }
}
