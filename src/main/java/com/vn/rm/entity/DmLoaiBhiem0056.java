package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.time.LocalDate;

@JmixEntity
@Entity
@Table(name = "DM_LOAI_BHIEM_0056")
public class DmLoaiBhiem0056 {

    @Id
    @Column(name = "IDBANGHI", nullable = false, length = 20)
    private String idBanghi; // 🔑 Khóa chính

    @Column(name = "MALOAIHINHBH", length = 20)
    private String maLoaiHinhBh;

    @Column(name = "TENLOAIHINHBH", length = 255)
    private String tenLoaiHinhBh;

    @Column(name = "TENVIETTAT", length = 100)
    private String tenVietTat;

    @Column(name = "VAN_BAN_BH", columnDefinition = "TEXT")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    @Column(name = "HIEU_LUC", length = 5)
    private String hieuLuc;

    public String getIdBanghi() {
        return idBanghi;
    }

    public void setIdBanghi(String idBanghi) {
        this.idBanghi = idBanghi;
    }

    public String getMaLoaiHinhBh() {
        return maLoaiHinhBh;
    }

    public void setMaLoaiHinhBh(String maLoaiHinhBh) {
        this.maLoaiHinhBh = maLoaiHinhBh;
    }

    public String getTenLoaiHinhBh() {
        return tenLoaiHinhBh;
    }

    public void setTenLoaiHinhBh(String tenLoaiHinhBh) {
        this.tenLoaiHinhBh = tenLoaiHinhBh;
    }

    public String getTenVietTat() {
        return tenVietTat;
    }

    public void setTenVietTat(String tenVietTat) {
        this.tenVietTat = tenVietTat;
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

    public String getHieuLuc() {
        return hieuLuc;
    }

    public void setHieuLuc(String hieuLuc) {
        this.hieuLuc = hieuLuc;
    }
}
