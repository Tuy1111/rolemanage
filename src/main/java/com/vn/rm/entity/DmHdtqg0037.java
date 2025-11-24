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
@Table(name = "DM_HDTQG_0037")
@Entity(name = "dmhdtqg0037")
public class DmHdtqg0037 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "MA_HANG")
    private String maHang;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "HL_TU")
    private LocalDate hlTu;

    @Column(name = "HL_DEN")
    private LocalDate hlDen;

    @Column(name = "NGAY_SD")
    private LocalDate ngaySd;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGUOI_TAO")
    private String nguoiTao;

    @Column(name = "TEN_HANG")
    private String tenHang;

    @Column(name = "VALID")
    private Integer valid;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;

    @Column(name = "TCVN")
    private String tcvn;

    @Column(name = "QCVN")
    private String qcvn;

    @Column(name = "MA_NHOM")
    private String maNhom;

    @Column(name = "CAP")
    private Integer cap;

    @Column(name = "MA_DONVI_QL")
    private String maDonviQl;



    // Getters / Setters

    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }

    public LocalDate getNgayVb() { return ngayVb; }
    public void setNgayVb(LocalDate ngayVb) { this.ngayVb = ngayVb; }

    public LocalDate getHlTu() { return hlTu; }
    public void setHlTu(LocalDate hlTu) { this.hlTu = hlTu; }

    public LocalDate getHlDen() { return hlDen; }
    public void setHlDen(LocalDate hlDen) { this.hlDen = hlDen; }

    public LocalDate getNgaySd() { return ngaySd; }
    public void setNgaySd(LocalDate ngaySd) { this.ngaySd = ngaySd; }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }

    public Integer getValid() { return valid; }
    public void setValid(Integer valid) { this.valid = valid; }

    public String getVanBanBh() { return vanBanBh; }
    public void setVanBanBh(String vanBanBh) { this.vanBanBh = vanBanBh; }

    public String getTcvn() { return tcvn; }
    public void setTcvn(String tcvn) { this.tcvn = tcvn; }

    public String getQcvn() { return qcvn; }
    public void setQcvn(String qcvn) { this.qcvn = qcvn; }

    public String getMaNhom() { return maNhom; }
    public void setMaNhom(String maNhom) { this.maNhom = maNhom; }

    public Integer getCap() { return cap; }
    public void setCap(Integer cap) { this.cap = cap; }

    public String getMaDonviQl() { return maDonviQl; }
    public void setMaDonviQl(String maDonviQl) { this.maDonviQl = maDonviQl; }
}
