package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_KETOANVIEN_0043")
@Entity
public class DmKetoanvien0043 {

    @Id
    @Column(name = "ID", nullable = false, length = 20)
    private String id; // Khóa chính

    @Column(name = "IDBANGHI", length = 50)
    private String idbanghi;

    @Column(name = "CHUC_VU", length = 255)
    private String chucVu;

    @Column(name = "GHI_CHU", length = 512)
    private String ghiChu;

    @Column(name = "GIOI_TINH", length = 5)
    private String gioiTinh;

    @Column(name = "HIEU_LUC", length = 5)
    private String hieuLuc;

    @Column(name = "BAN_THOIGIAN", length = 10)
    private String banThoigian;

    @Column(name = "MA", length = 50)
    private String ma;

    @Column(name = "ID_CTY", length = 50)
    private String idCty;

    @Column(name = "MA_CTY", length = 50)
    private String maCty;

    @Column(name = "NAM_SINH", length = 10)
    private String namSinh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    @Column(name = "NGAY_PS")
    private LocalDate ngayPs;

    @Column(name = "NGAY_SD")
    private LocalDate ngaySd;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "QUE_QUAN", length = 255)
    private String queQuan;

    @Column(name = "SO_THE", length = 50)
    private String soThe;

    @Column(name = "TEN", length = 255)
    private String ten;

    @Column(name = "VAN_BAN_BH", length = 255)
    private String vanBanBh;

    @Column(name = "NGAY_CAP")
    private LocalDate ngayCap;

    // ===== Getters & Setters =====

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

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getHieuLuc() {
        return hieuLuc;
    }

    public void setHieuLuc(String hieuLuc) {
        this.hieuLuc = hieuLuc;
    }

    public String getBanThoigian() {
        return banThoigian;
    }

    public void setBanThoigian(String banThoigian) {
        this.banThoigian = banThoigian;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getIdCty() {
        return idCty;
    }

    public void setIdCty(String idCty) {
        this.idCty = idCty;
    }

    public String getMaCty() {
        return maCty;
    }

    public void setMaCty(String maCty) {
        this.maCty = maCty;
    }

    public String getNamSinh() {
        return namSinh;
    }

    public void setNamSinh(String namSinh) {
        this.namSinh = namSinh;
    }

    public LocalDate getNgayVb() {
        return ngayVb;
    }

    public void setNgayVb(LocalDate ngayVb) {
        this.ngayVb = ngayVb;
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

    public LocalDate getNgayPs() {
        return ngayPs;
    }

    public void setNgayPs(LocalDate ngayPs) {
        this.ngayPs = ngayPs;
    }

    public LocalDate getNgaySd() {
        return ngaySd;
    }

    public void setNgaySd(LocalDate ngaySd) {
        this.ngaySd = ngaySd;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }

    public String getSoThe() {
        return soThe;
    }

    public void setSoThe(String soThe) {
        this.soThe = soThe;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getVanBanBh() {
        return vanBanBh;
    }

    public void setVanBanBh(String vanBanBh) {
        this.vanBanBh = vanBanBh;
    }

    public LocalDate getNgayCap() {
        return ngayCap;
    }

    public void setNgayCap(LocalDate ngayCap) {
        this.ngayCap = ngayCap;
    }
}
