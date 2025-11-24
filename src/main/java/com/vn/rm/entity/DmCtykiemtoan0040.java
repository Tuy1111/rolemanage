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
@Table(name = "DM_CTYKIEMTOAN_0040")
@Entity(name = "dmctykiemtoan0040")
public class DmCtykiemtoan0040 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Column(name = "DIA_CHI")
    private String diaChi;

    @Column(name = "GHI_CHU")
    private String ghiChu;

    @Column(name = "HIEU_LUC")
    private Integer hieuLuc;

    @Column(name = "ID")
    @Id
    private String idCol;

    @Column(name = "MA")
    private String ma;

    @Column(name = "ID_CHA")
    private String idCha;

    @Column(name = "MA_CHA")
    private String maCha;

    @Column(name = "MASOTHUE")
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

    @Column(name = "TEN")
    private String ten;

    @Column(name = "TEN_VTAT")
    private String tenVtat;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;


    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Integer getHieuLuc() { return hieuLuc; }
    public void setHieuLuc(Integer hieuLuc) { this.hieuLuc = hieuLuc; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getIdCha() { return idCha; }
    public void setIdCha(String idCha) { this.idCha = idCha; }

    public String getMaCha() { return maCha; }
    public void setMaCha(String maCha) { this.maCha = maCha; }

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }

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

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getTenVtat() { return tenVtat; }
    public void setTenVtat(String tenVtat) { this.tenVtat = tenVtat; }

    public String getVanBanBh() { return vanBanBh; }
    public void setVanBanBh(String vanBanBh) { this.vanBanBh = vanBanBh; }
}
