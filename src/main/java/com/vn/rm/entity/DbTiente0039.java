package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@JmixEntity
@Table(name = "DB_TIENTE_0039")
@Entity
public class DbTiente0039 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Column(name = "GHICHU")
    private String ghiChu;

    @Column(name = "HIEU_LUC")
    private Integer hieuLuc;

    @Column(name = "ID")
    @Id
    private String idCol;

    @Column(name = "MA_DANG_SO")
    private String maDangSo;

    @Column(name = "MA")
    private String ma;

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

    @Column(name = "TEN_QG")
    private String tenQg;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "TEN_TA")
    private String tenTa;

    @Column(name = "TYGIA")
    private BigDecimal tyGia;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;


    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Integer getHieuLuc() { return hieuLuc; }
    public void setHieuLuc(Integer hieuLuc) { this.hieuLuc = hieuLuc; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMaDangSo() { return maDangSo; }
    public void setMaDangSo(String maDangSo) { this.maDangSo = maDangSo; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public LocalDate getNgayVb() { return ngayVb; }
    public void setNgayVb(LocalDate ngayVb) { this.ngayVb = ngayVb; }

    public LocalDate getNgayHl() { return ngayHl; }
    public void setNgayHl(LocalDate ngayHl) { this.ngayHl = ngayHl; }

    public LocalDate getNgayKt() { return ngayKt; }
    public void setNgayKt(LocalDate ngayKt) { this.ngayKt = ngayKt; }

    public LocalDate getNgaySd() { return ngaySd; }
    public void setNgaySd(LocalDate ngaySd) { this.ngaySd = ngaySd; }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public String getTenQg() { return tenQg; }
    public void setTenQg(String tenQg) { this.tenQg = tenQg; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getTenTa() { return tenTa; }
    public void setTenTa(String tenTa) { this.tenTa = tenTa; }

    public BigDecimal getTyGia() { return tyGia; }
    public void setTyGia(BigDecimal tyGia) { this.tyGia = tyGia; }

    public String getVanBanBh() { return vanBanBh; }
    public void setVanBanBh(String vanBanBh) { this.vanBanBh = vanBanBh; }
}
