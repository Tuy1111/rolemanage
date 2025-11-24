package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.time.LocalDate;

@JmixEntity
@Entity
@Table(name = "DM_DBHC_0049")
public class DmDbhc0049 {

    @Id
    @Column(name = "ID", nullable = false, length = 20)
    private String id; // Khóa chính

    @Column(name = "IDBANGHI", length = 20)
    private String idbanghi;

    @Column(name = "LOAI", length = 10)
    private String loai;

    @Column(name = "ID_CHA", length = 20)
    private String idCha;

    @Column(name = "MA_CHA", length = 50)
    private String maCha;

    @Column(name = "HIEU_LUC", length = 5)
    private String hieuLuc;

    @Column(name = "MA_CU", length = 255)
    private String maCu;

    @Column(name = "MA_DB", length = 50)
    private String maDb;

    @Column(name = "MA_H", length = 50)
    private String maH;

    @Column(name = "MA_THAMCHIEU", length = 50)
    private String maThamChieu;

    @Column(name = "MA_T", length = 50)
    private String maT;

    @Column(name = "MA_V", length = 50)
    private String maV;

    @Column(name = "MA_X", length = 50)
    private String maX;

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

    @Column(name = "MA", length = 50)
    private String ma;

    @Column(name = "TEN", length = 255)
    private String ten;

    @Column(name = "VAN_BAN_BH", columnDefinition = "TEXT")
    private String vanBanBh;

    @Column(name = "LY_DO", columnDefinition = "TEXT")
    private String lyDo;

    @Column(name = "LOAI_HINH", length = 100)
    private String loaiHinh;

    @Column(name = "LOAI_DOTHI", length = 100)
    private String loaiDoThi;

    @Column(name = "VUNG", length = 100)
    private String vung;

    @Column(name = "TTHI_NTHON", length = 100)
    private String tthiNthon;

    @Column(name = "KHUVUC", length = 100)
    private String khuVuc;

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

    public String getHieuLuc() {
        return hieuLuc;
    }

    public void setHieuLuc(String hieuLuc) {
        this.hieuLuc = hieuLuc;
    }

    public String getMaCu() {
        return maCu;
    }

    public void setMaCu(String maCu) {
        this.maCu = maCu;
    }

    public String getMaDb() {
        return maDb;
    }

    public void setMaDb(String maDb) {
        this.maDb = maDb;
    }

    public String getMaH() {
        return maH;
    }

    public void setMaH(String maH) {
        this.maH = maH;
    }

    public String getMaThamChieu() {
        return maThamChieu;
    }

    public void setMaThamChieu(String maThamChieu) {
        this.maThamChieu = maThamChieu;
    }

    public String getMaT() {
        return maT;
    }

    public void setMaT(String maT) {
        this.maT = maT;
    }

    public String getMaV() {
        return maV;
    }

    public void setMaV(String maV) {
        this.maV = maV;
    }

    public String getMaX() {
        return maX;
    }

    public void setMaX(String maX) {
        this.maX = maX;
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

    public String getVanBanBh() {
        return vanBanBh;
    }

    public void setVanBanBh(String vanBanBh) {
        this.vanBanBh = vanBanBh;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getLoaiHinh() {
        return loaiHinh;
    }

    public void setLoaiHinh(String loaiHinh) {
        this.loaiHinh = loaiHinh;
    }

    public String getLoaiDoThi() {
        return loaiDoThi;
    }

    public void setLoaiDoThi(String loaiDoThi) {
        this.loaiDoThi = loaiDoThi;
    }

    public String getVung() {
        return vung;
    }

    public void setVung(String vung) {
        this.vung = vung;
    }

    public String getTthiNthon() {
        return tthiNthon;
    }

    public void setTthiNthon(String tthiNthon) {
        this.tthiNthon = tthiNthon;
    }

    public String getKhuVuc() {
        return khuVuc;
    }

    public void setKhuVuc(String khuVuc) {
        this.khuVuc = khuVuc;
    }
}
