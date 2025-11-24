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
@Table(name = "DM_NGANHKTQD_0050")
@Entity(name = "dmnganhktqd0050")
public class DmNganhktqd0050 {

    @Column(name = "IDBANGHI")
    private String idBanghi;

    @Column(name = "CAP1")
    private String cap1;

    @Column(name = "CAP2")
    private String cap2;

    @Column(name = "CAP3")
    private String cap3;

    @Column(name = "CAP4")
    private String cap4;

    @Column(name = "CAP5")
    private String cap5;

    @Column(name = "GHI_CHU")
    private String ghiChu;

    @Column(name = "HIEU_LUC")
    private Integer hieuLuc;

    @Column(name = "STT")
    private Integer stt;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "LOAI")
    private String loai;

    @Column(name = "ID_CHA")
    private String idCha;

    @Column(name = "MA_CHA")
    private String maCha;

    @Column(name = "MA_GFS")
    private String maGfs;

    @Column(name = "MA")
    private String ma;

    @Column(name = "MA_THAM_CHIEU")
    private String maThamChieu;

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

    @Column(name = "TEN")
    private String ten;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;


    // Getters / Setters

    public String getIdBanghi() {
        return idBanghi;
    }

    public void setIdBanghi(String idBanghi) {
        this.idBanghi = idBanghi;
    }

    public String getCap1() {
        return cap1;
    }

    public void setCap1(String cap1) {
        this.cap1 = cap1;
    }

    public String getCap2() {
        return cap2;
    }

    public void setCap2(String cap2) {
        this.cap2 = cap2;
    }

    public String getCap3() {
        return cap3;
    }

    public void setCap3(String cap3) {
        this.cap3 = cap3;
    }

    public String getCap4() {
        return cap4;
    }

    public void setCap4(String cap4) {
        this.cap4 = cap4;
    }

    public String getCap5() {
        return cap5;
    }

    public void setCap5(String cap5) {
        this.cap5 = cap5;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Integer getHieuLuc() {
        return hieuLuc;
    }

    public void setHieuLuc(Integer hieuLuc) {
        this.hieuLuc = hieuLuc;
    }

    public Integer getStt() {
        return stt;
    }

    public void setStt(Integer stt) {
        this.stt = stt;
    }

    public String getIdCol() {
        return idCol;
    }

    public void setIdCol(String idCol) {
        this.idCol = idCol;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getIdCha() {
        return idCha;
    }

    public void setIdCha(String idCha) {
        this.idCha = idCha;
    }

    public String getMaCha() {
        return maCha;
    }

    public void setMaCha(String maCha) {
        this.maCha = maCha;
    }

    public String getMaGfs() {
        return maGfs;
    }

    public void setMaGfs(String maGfs) {
        this.maGfs = maGfs;
    }

    public String getMa() {
        return ma;
    }

    public void setMa(String ma) {
        this.ma = ma;
    }

    public String getMaThamChieu() {
        return maThamChieu;
    }

    public void setMaThamChieu(String maThamChieu) {
        this.maThamChieu = maThamChieu;
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
}
