package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@JmixEntity
@Entity
@Table(name = "DM_KETOANVIEN_0046")
public class DmKetoanvien0046 {

    @Id
    @Column(name = "ID", nullable = false, length = 20)
    private String id;  // Khóa chính

    @Column(name = "IDBANGHI", length = 20)
    private String idbanghi;

    @Column(name = "MA", length = 50)
    private String ma;

    @Column(name = "ID_CHA", length = 20)
    private String idCha;

    @Column(name = "DIA_CHI", length = 512)
    private String diaChi;

    @Column(name = "GHI_CHU", length = 512)
    private String ghiChu;

    @Column(name = "HIEU_LUC", length = 5)
    private String hieuLuc;

    @Column(name = "MA_CHA", length = 50)
    private String maCha;

    @Column(name = "MASOTHUE", length = 50)
    private String maSoThue;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    @Column(name = "NGAY_SD")
    private LocalDate ngaySd;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "TEN", length = 512)
    private String ten;

    @Column(name = "TEN_VTAT", length = 255)
    private String tenVtat;

    @Column(name = "VAN_BAN_BH", columnDefinition = "TEXT")
    private String vanBanBh;

    public String getVanBanBh() {
        return vanBanBh;
    }

    public void setVanBanBh(String vanBanBh) {
        this.vanBanBh = vanBanBh;
    }

    public String getTenVtat() {
        return tenVtat;
    }

    public void setTenVtat(String tenVtat) {
        this.tenVtat = tenVtat;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDate getNgaySd() {
        return ngaySd;
    }

    public void setNgaySd(LocalDate ngaySd) {
        this.ngaySd = ngaySd;
    }

    public LocalDate getNgayKt() {
        return ngayKt;
    }

    public void setNgayKt(LocalDate ngayKt) {
        this.ngayKt = ngayKt;
    }

    public LocalDate getNgayHl() {
        return ngayHl;
    }

    public void setNgayHl(LocalDate ngayHl) {
        this.ngayHl = ngayHl;
    }

    public LocalDate getNgayVb() {
        return ngayVb;
    }

    public void setNgayVb(LocalDate ngayVb) {
        this.ngayVb = ngayVb;
    }

    public String getMaSoThue() {
        return maSoThue;
    }

    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }

    public String getMaCha() {
        return maCha;
    }

    public void setMaCha(String maCha) {
        this.maCha = maCha;
    }

    public String getHieuLuc() {
        return hieuLuc;
    }

    public void setHieuLuc(String hieuLuc) {
        this.hieuLuc = hieuLuc;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getIdCha() {
        return idCha;
    }

    public void setIdCha(String idCha) {
        this.idCha = idCha;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getIdbanghi() {
        return idbanghi;
    }

    public void setIdbanghi(String idbanghi) {
        this.idbanghi = idbanghi;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
