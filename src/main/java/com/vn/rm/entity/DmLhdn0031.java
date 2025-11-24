package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_LHDN_0031")
@Entity(name = "dmlhdn0031")
public class DmLhdn0031 {

    @Id
    @Column(name = "IDBANGHI")
    private String id;

    @Column(name = "MA_LHKT", nullable = false)
    private String maLhkt;

    @Column(name = "TEN_LHKT")
    private String tenLhkt;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_SUA")
    private LocalDate ngaySua;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGAY_HL_TU")
    private LocalDate ngayHlTu;

    @Column(name = "NGAY_HL_DEN")
    private LocalDate ngayHlDen;

    @Column(name = "PBAN")
    private String pban;


    // getters/setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaLhkt() {
        return maLhkt;
    }

    public void setMaLhkt(String maLhkt) {
        this.maLhkt = maLhkt;
    }

    public String getTenLhkt() {
        return tenLhkt;
    }

    public void setTenLhkt(String tenLhkt) {
        this.tenLhkt = tenLhkt;
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

    public LocalDate getNgaySua() {
        return ngaySua;
    }

    public void setNgaySua(LocalDate ngaySua) {
        this.ngaySua = ngaySua;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDate getNgayHlTu() {
        return ngayHlTu;
    }

    public void setNgayHlTu(LocalDate ngayHlTu) {
        this.ngayHlTu = ngayHlTu;
    }

    public LocalDate getNgayHlDen() {
        return ngayHlDen;
    }

    public void setNgayHlDen(LocalDate ngayHlDen) {
        this.ngayHlDen = ngayHlDen;
    }

    public String getPban() {
        return pban;
    }

    public void setPban(String pban) {
        this.pban = pban;
    }
}
