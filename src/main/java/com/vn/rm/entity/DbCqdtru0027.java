package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.time.LocalDate;

@JmixEntity
@Entity
@Table(name = "DB_CQDTRU_0027")
public class DbCqdtru0027 {

    @Id
    @Column(name = "ID", nullable = false, length = 20)
    private String id; // Khóa chính

    @Column(name = "IDBANGHI", length = 20)
    private String idbanghi;

    @Column(name = "MA", length = 50)
    private String ma;

    @Column(name = "TEN", length = 512)
    private String ten;

    @Column(name = "MA_CQT", length = 50)
    private String maCqt;

    @Column(name = "LOAIDV_MA", length = 50)
    private String loaiDvMa;

    @Column(name = "LOAIDV_TEN", length = 255)
    private String loaiDvTen;

    @Column(name = "CAPDT", length = 10)
    private String capDt;

    @Column(name = "CHUONG", length = 50)
    private String chuong;

    @Column(name = "CCH_TEN", length = 255)
    private String cchTen;

    @Column(name = "LHDV_MA", length = 50)
    private String lhdvMa;

    @Column(name = "LHDV_TEN", length = 255)
    private String lhdvTen;

    @Column(name = "SOQD_TL", length = 100)
    private String soQdTl;

    @Column(name = "NGAY_TL")
    private LocalDate ngayTl;

    @Column(name = "COQUAN_TL", length = 255)
    private String coQuanTl;

    @Column(name = "DVCT_MA", length = 50)
    private String dvctMa;

    @Column(name = "DVCT_TEN", length = 255)
    private String dvctTen;

    @Column(name = "DVQLTT_MA", length = 50)
    private String dvqlttMa;

    @Column(name = "DVQLTT_TEN", length = 255)
    private String dvqlttTen;

    @Column(name = "DBN_MA", length = 50)
    private String dbnMa;

    @Column(name = "DBN_TEN", length = 255)
    private String dbnTen;

    @Column(name = "DIACHI", length = 512)
    private String diaChi;

    @Column(name = "DVDTCT", length = 5)
    private String dvdTct;

    @Column(name = "DVDTCD", length = 5)
    private String dvdTcd;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGUOI_TAO", length = 255)
    private String nguoiTao;

    @Column(name = "NGAY_SUA")
    private LocalDate ngaySua;

    @Column(name = "NGUOI_SUA", length = 255)
    private String nguoiSua;

    @Column(name = "TRANGTHAI_MA", length = 10)
    private String trangThaiMa;

    @Column(name = "TRANGTHAI_CU", length = 10)
    private String trangThaiCu;

    @Column(name = "NGAY_DMO")
    private LocalDate ngayDmo;

    @Column(name = "TRANGTHAI_DM", length = 10)
    private String trangThaiDm;

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

    public String getMaCqt() {
        return maCqt;
    }

    public void setMaCqt(String maCqt) {
        this.maCqt = maCqt;
    }

    public String getLoaiDvMa() {
        return loaiDvMa;
    }

    public void setLoaiDvMa(String loaiDvMa) {
        this.loaiDvMa = loaiDvMa;
    }

    public String getLoaiDvTen() {
        return loaiDvTen;
    }

    public void setLoaiDvTen(String loaiDvTen) {
        this.loaiDvTen = loaiDvTen;
    }

    public String getCapDt() {
        return capDt;
    }

    public void setCapDt(String capDt) {
        this.capDt = capDt;
    }

    public String getChuong() {
        return chuong;
    }

    public void setChuong(String chuong) {
        this.chuong = chuong;
    }

    public String getCchTen() {
        return cchTen;
    }

    public void setCchTen(String cchTen) {
        this.cchTen = cchTen;
    }

    public String getLhdvMa() {
        return lhdvMa;
    }

    public void setLhdvMa(String lhdvMa) {
        this.lhdvMa = lhdvMa;
    }

    public String getLhdvTen() {
        return lhdvTen;
    }

    public void setLhdvTen(String lhdvTen) {
        this.lhdvTen = lhdvTen;
    }

    public String getSoQdTl() {
        return soQdTl;
    }

    public void setSoQdTl(String soQdTl) {
        this.soQdTl = soQdTl;
    }

    public LocalDate getNgayTl() {
        return ngayTl;
    }

    public void setNgayTl(LocalDate ngayTl) {
        this.ngayTl = ngayTl;
    }

    public String getCoQuanTl() {
        return coQuanTl;
    }

    public void setCoQuanTl(String coQuanTl) {
        this.coQuanTl = coQuanTl;
    }

    public String getDvctMa() {
        return dvctMa;
    }

    public void setDvctMa(String dvctMa) {
        this.dvctMa = dvctMa;
    }

    public String getDvctTen() {
        return dvctTen;
    }

    public void setDvctTen(String dvctTen) {
        this.dvctTen = dvctTen;
    }

    public String getDvqlttMa() {
        return dvqlttMa;
    }

    public void setDvqlttMa(String dvqlttMa) {
        this.dvqlttMa = dvqlttMa;
    }

    public String getDvqlttTen() {
        return dvqlttTen;
    }

    public void setDvqlttTen(String dvqlttTen) {
        this.dvqlttTen = dvqlttTen;
    }

    public String getDbnMa() {
        return dbnMa;
    }

    public void setDbnMa(String dbnMa) {
        this.dbnMa = dbnMa;
    }

    public String getDbnTen() {
        return dbnTen;
    }

    public void setDbnTen(String dbnTen) {
        this.dbnTen = dbnTen;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getDvdTct() {
        return dvdTct;
    }

    public void setDvdTct(String dvdTct) {
        this.dvdTct = dvdTct;
    }

    public String getDvdTcd() {
        return dvdTcd;
    }

    public void setDvdTcd(String dvdTcd) {
        this.dvdTcd = dvdTcd;
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

    public String getTrangThaiMa() {
        return trangThaiMa;
    }

    public void setTrangThaiMa(String trangThaiMa) {
        this.trangThaiMa = trangThaiMa;
    }

    public String getTrangThaiCu() {
        return trangThaiCu;
    }

    public void setTrangThaiCu(String trangThaiCu) {
        this.trangThaiCu = trangThaiCu;
    }

    public LocalDate getNgayDmo() {
        return ngayDmo;
    }

    public void setNgayDmo(LocalDate ngayDmo) {
        this.ngayDmo = ngayDmo;
    }

    public String getTrangThaiDm() {
        return trangThaiDm;
    }

    public void setTrangThaiDm(String trangThaiDm) {
        this.trangThaiDm = trangThaiDm;
    }
}
