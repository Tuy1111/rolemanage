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
@Table(name = "DM_KCX_KCN_0088")
@Entity(name = "dmkcxkcn0088")
public class DmKcxkcn0088 {

    @Column(name = "IDBANGHI")
    private String idBanghi;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "MA")
    private String ma;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "HIEU_LUC")
    private Integer hieuLuc;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGAY_SD")
    private LocalDate ngaySd;

    @Column(name = "MOTA_DIACHI")
    private String motaDiachi;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;



    // Getters / Setters

    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public Integer getHieuLuc() { return hieuLuc; }
    public void setHieuLuc(Integer hieuLuc) { this.hieuLuc = hieuLuc; }

    public LocalDate getNgayKt() { return ngayKt; }
    public void setNgayKt(LocalDate ngayKt) { this.ngayKt = ngayKt; }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public LocalDate getNgaySd() { return ngaySd; }
    public void setNgaySd(LocalDate ngaySd) { this.ngaySd = ngaySd; }

    public String getMotaDiachi() { return motaDiachi; }
    public void setMotaDiachi(String motaDiachi) { this.motaDiachi = motaDiachi; }

    public LocalDate getNgayHl() { return ngayHl; }
    public void setNgayHl(LocalDate ngayHl) { this.ngayHl = ngayHl; }
}
