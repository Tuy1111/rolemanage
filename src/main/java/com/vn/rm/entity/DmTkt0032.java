package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.time.LocalDate;

@JmixEntity
@Entity
@Table(name = "DM_TKT_0032")
public class DmTkt0032 {

    @Id
    @Column(name = "IDBANGHI", nullable = false, length = 20)
    private String idBanghi; // Khóa chính

    @Column(name = "MA_TOKHAI", length = 50)
    private String maToKhai;

    @Column(name = "TEN_TOKHAI", length = 255)
    private String tenToKhai;

    @Column(name = "VAN_BAN_BH", columnDefinition = "TEXT")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_SUA")
    private LocalDate ngaySua;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGAY_HLUC_TU")
    private LocalDate ngayHlucTu;

    @Column(name = "NGAY_HLUC_DEN")
    private LocalDate ngayHlucDen;

    @Column(name = "PBAN", length = 255)
    private String pban;

    public String getIdBanghi() {
        return idBanghi;
    }

    public void setIdBanghi(String idBanghi) {
        this.idBanghi = idBanghi;
    }

    public String getMaToKhai() {
        return maToKhai;
    }

    public void setMaToKhai(String maToKhai) {
        this.maToKhai = maToKhai;
    }

    public String getTenToKhai() {
        return tenToKhai;
    }

    public void setTenToKhai(String tenToKhai) {
        this.tenToKhai = tenToKhai;
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

    public LocalDate getNgayHlucTu() {
        return ngayHlucTu;
    }

    public void setNgayHlucTu(LocalDate ngayHlucTu) {
        this.ngayHlucTu = ngayHlucTu;
    }

    public LocalDate getNgayHlucDen() {
        return ngayHlucDen;
    }

    public void setNgayHlucDen(LocalDate ngayHlucDen) {
        this.ngayHlucDen = ngayHlucDen;
    }

    public String getPban() {
        return pban;
    }

    public void setPban(String pban) {
        this.pban = pban;
    }
}
