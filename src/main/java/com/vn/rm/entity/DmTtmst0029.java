package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_TTMST_0029")
@Entity
public class DmTtmst0029 {

    @Id
    @Column(name = "IDBANGHI", nullable = false, length = 10)
    private String idbanghi;

    @Column(name = "MA_TTHAI", length = 10)
    private String maTthai;

    @Column(name = "TEN_TTHAI", length = 255)
    private String tenTthai;

    @Column(name = "VAN_BAN_BH", length = 255)
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGUOI_TAO", length = 100)
    private String nguoiTao;

    @Column(name = "NGAY_SUA")
    private LocalDate ngaySua;

    @Column(name = "NGUOI_SUA", length = 100)
    private String nguoiSua;

    @Column(name = "NGAY_DBO")
    private LocalDate ngayDbo;

    @Column(name = "NGAY_HLUC_TU")
    private LocalDate ngayHlucTu;

    @Column(name = "NGAY_HLUC_DEN")
    private LocalDate ngayHlucDen;

    // Getters và Setters
    public String getIdbanghi() {
        return idbanghi;
    }

    public void setIdbanghi(String idbanghi) {
        this.idbanghi = idbanghi;
    }

    public String getMaTthai() {
        return maTthai;
    }

    public void setMaTthai(String maTthai) {
        this.maTthai = maTthai;
    }

    public String getTenTthai() {
        return tenTthai;
    }

    public void setTenTthai(String tenTthai) {
        this.tenTthai = tenTthai;
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

    public LocalDate getNgaySua() {
        return ngaySua;
    }

    public void setNgaySua(LocalDate ngaySua) {
        this.ngaySua = ngaySua;
    }

    public String getNguoiSua() {
        return nguoiSua;
    }

    public void setNguoiSua(String nguoiSua) {
        this.nguoiSua = nguoiSua;
    }

    public LocalDate getNgayDbo() {
        return ngayDbo;
    }

    public void setNgayDbo(LocalDate ngayDbo) {
        this.ngayDbo = ngayDbo;
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
}
