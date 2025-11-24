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
@Table(name = "DM_CDTU_0019")
@Entity(name = "dmcdtu0019")
public class DmCdtu0019 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Column(name = "MA")
    private String ma;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "MA_CQT")
    private String maCqt;

    @Column(name = "LOAIDV_MA")
    private String loaidvMa;

    @Column(name = "LOAIDV_TEN")
    private String loaidvTen;

    @Column(name = "CAPDT")
    private Integer capdt;

    @Column(name = "CHUONG")
    private String chuong;

    @Column(name = "CCH_TEN")
    private String cchTen;

    @Column(name = "LHDV_MA")
    private String lhdvMa;

    @Column(name = "LHDV_TEN")
    private String lhdvTen;

    @Column(name = "SOQD_TL")
    private String soqdTl;

    @Column(name = "NGAY_TL")
    private LocalDate ngayTl;

    @Column(name = "COQUAN_TL")
    private String coquanTl;

    @Column(name = "DVCT_MA")
    private String dvctMa;

    @Column(name = "DVCT_TEN")
    private String dvctTen;

    @Column(name = "DVQLTT_MA")
    private String dvqlttMa;

    @Column(name = "DVQLTT_TEN")
    private String dvqlttTen;

    @Column(name = "DBN_MA")
    private String dbnMa;

    @Column(name = "DBN_TEN")
    private String dbnTen;

    @Column(name = "DIACHI")
    private String diachi;

    @Column(name = "DVDTCT")
    private Integer dvdtct;

    @Column(name = "DVDTCD")
    private Integer dvdtcd;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGUOI_TAO")
    private String nguoiTao;

    @Column(name = "NGAY_SUA")
    private LocalDate ngaySua;

    @Column(name = "NGUOI_SUA")
    private String nguoiSua;

    @Column(name = "TRANGTHAI_MA")
    private String trangthaiMa;

    @Column(name = "TRANGTHAI_CU")
    private String trangthaiCu;

    @Column(name = "NGAY_DMO")
    private LocalDate ngayDmo;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "TRANGTHAI_DM")
    private Integer trangthaiDm;


    public String getIdBanghi() {
        return idBanghi;
    }

    public void setIdBanghi(String idBanghi) {
        this.idBanghi = idBanghi;
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

    public String getLoaidvMa() {
        return loaidvMa;
    }

    public void setLoaidvMa(String loaidvMa) {
        this.loaidvMa = loaidvMa;
    }

    public String getLoaidvTen() {
        return loaidvTen;
    }

    public void setLoaidvTen(String loaidvTen) {
        this.loaidvTen = loaidvTen;
    }

    public Integer getCapdt() {
        return capdt;
    }

    public void setCapdt(Integer capdt) {
        this.capdt = capdt;
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

    public String getSoqdTl() {
        return soqdTl;
    }

    public void setSoqdTl(String soqdTl) {
        this.soqdTl = soqdTl;
    }

    public LocalDate getNgayTl() {
        return ngayTl;
    }

    public void setNgayTl(LocalDate ngayTl) {
        this.ngayTl = ngayTl;
    }

    public String getCoquanTl() {
        return coquanTl;
    }

    public void setCoquanTl(String coquanTl) {
        this.coquanTl = coquanTl;
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

    public String getDiachi() {
        return diachi;
    }

    public void setDiachi(String diachi) {
        this.diachi = diachi;
    }

    public Integer getDvdtct() {
        return dvdtct;
    }

    public void setDvdtct(Integer dvdtct) {
        this.dvdtct = dvdtct;
    }

    public Integer getDvdtcd() {
        return dvdtcd;
    }

    public void setDvdtcd(Integer dvdtcd) {
        this.dvdtcd = dvdtcd;
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

    public String getTrangthaiMa() {
        return trangthaiMa;
    }

    public void setTrangthaiMa(String trangthaiMa) {
        this.trangthaiMa = trangthaiMa;
    }

    public String getTrangthaiCu() {
        return trangthaiCu;
    }

    public void setTrangthaiCu(String trangthaiCu) {
        this.trangthaiCu = trangthaiCu;
    }

    public LocalDate getNgayDmo() {
        return ngayDmo;
    }

    public void setNgayDmo(LocalDate ngayDmo) {
        this.ngayDmo = ngayDmo;
    }

    public String getIdCol() {
        return idCol;
    }

    public void setIdCol(String idCol) {
        this.idCol = idCol;
    }

    public Integer getTrangthaiDm() {
        return trangthaiDm;
    }

    public void setTrangthaiDm(Integer trangthaiDm) {
        this.trangthaiDm = trangthaiDm;
    }
}
