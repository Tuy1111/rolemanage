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
@Table(name = "DM_CTQLQ_0036")
@Entity(name = "dmctqlq0036")
public class DmCtqlq0036 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Column(name = "ID")
    @Id
    private String idCol;

    @Column(name = "MA")
    private String ma;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "TEN_TA")
    private String tenTa;

    @Column(name = "TEN_VIET_TAT")
    private String tenVietTat;

    @Column(name = "DIA_CHI_CTY")
    private String diaChiCty;

    @Column(name = "SDT")
    private String sdt;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "SO_FAX")
    private String soFax;

    @Column(name = "LOAI_HINH_DN")
    private String loaiHinhDn;

    @Column(name = "MA_HUYEN")
    private String maHuyen;

    @Column(name = "MA_TINH")
    private String maTinh;

    @Column(name = "MA_XA")
    private String maXa;

    @Column(name = "MENH_GIA_1CP", precision = 19, scale = 2)
    private BigDecimal menhGia1cp;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_CAP_GPKD")
    private LocalDate ngayCapGpkd;

    @Column(name = "START_DATE_ACTIVE")
    private LocalDate startDateActive;

    @Column(name = "END_DATE_ACTIVE")
    private LocalDate endDateActive;

    @Column(name = "LAST_UPDATE_DATE")
    private LocalDate lastUpdateDate;

    @Column(name = "CREATION_DATE")
    private LocalDate creationDate;

    @Column(name = "NGHIEP_VU")
    private String nghiepVu;

    @Column(name = "NGUOI_DAI_DIEN")
    private String nguoiDaiDien;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "NOI_CAP_GPKD")
    private String noiCapGpkd;

    @Column(name = "SO_GPKD")
    private String soGpkd;

    @Column(name = "SO_GPKD_DC")
    private String soGpkdDc;

    @Column(name = "TGD")
    private String tgd;

    @Column(name = "TONG_SO_CP")
    private Long tongSoCp;

    @Column(name = "TINHTRANG")
    private Integer tinhTrang;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;

    @Column(name = "VON_DIEULE", precision = 19, scale = 2)
    private BigDecimal vonDieuLe;

    @Column(name = "WEBSITE")
    private String website;


    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getTenTa() { return tenTa; }
    public void setTenTa(String tenTa) { this.tenTa = tenTa; }

    public String getTenVietTat() { return tenVietTat; }
    public void setTenVietTat(String tenVietTat) { this.tenVietTat = tenVietTat; }

    public String getDiaChiCty() { return diaChiCty; }
    public void setDiaChiCty(String diaChiCty) { this.diaChiCty = diaChiCty; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSoFax() { return soFax; }
    public void setSoFax(String soFax) { this.soFax = soFax; }

    public String getLoaiHinhDn() { return loaiHinhDn; }
    public void setLoaiHinhDn(String loaiHinhDn) { this.loaiHinhDn = loaiHinhDn; }

    public String getMaHuyen() { return maHuyen; }
    public void setMaHuyen(String maHuyen) { this.maHuyen = maHuyen; }

    public String getMaTinh() { return maTinh; }
    public void setMaTinh(String maTinh) { this.maTinh = maTinh; }

    public String getMaXa() { return maXa; }
    public void setMaXa(String maXa) { this.maXa = maXa; }

    public BigDecimal getMenhGia1cp() { return menhGia1cp; }
    public void setMenhGia1cp(BigDecimal menhGia1cp) { this.menhGia1cp = menhGia1cp; }

    public LocalDate getNgayVb() { return ngayVb; }
    public void setNgayVb(LocalDate ngayVb) { this.ngayVb = ngayVb; }

    public LocalDate getNgayCapGpkd() { return ngayCapGpkd; }
    public void setNgayCapGpkd(LocalDate ngayCapGpkd) { this.ngayCapGpkd = ngayCapGpkd; }

    public LocalDate getStartDateActive() { return startDateActive; }
    public void setStartDateActive(LocalDate startDateActive) { this.startDateActive = startDateActive; }

    public LocalDate getEndDateActive() { return endDateActive; }
    public void setEndDateActive(LocalDate endDateActive) { this.endDateActive = endDateActive; }

    public LocalDate getLastUpdateDate() { return lastUpdateDate; }
    public void setLastUpdateDate(LocalDate lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public String getNghiepVu() { return nghiepVu; }
    public void setNghiepVu(String nghiepVu) { this.nghiepVu = nghiepVu; }

    public String getNguoiDaiDien() { return nguoiDaiDien; }
    public void setNguoiDaiDien(String nguoiDaiDien) { this.nguoiDaiDien = nguoiDaiDien; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getNoiCapGpkd() { return noiCapGpkd; }
    public void setNoiCapGpkd(String noiCapGpkd) { this.noiCapGpkd = noiCapGpkd; }

    public String getSoGpkd() { return soGpkd; }
    public void setSoGpkd(String soGpkd) { this.soGpkd = soGpkd; }

    public String getSoGpkdDc() { return soGpkdDc; }
    public void setSoGpkdDc(String soGpkdDc) { this.soGpkdDc = soGpkdDc; }

    public String getTgd() { return tgd; }
    public void setTgd(String tgd) { this.tgd = tgd; }

    public Long getTongSoCp() { return tongSoCp; }
    public void setTongSoCp(Long tongSoCp) { this.tongSoCp = tongSoCp; }

    public Integer getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(Integer tinhTrang) { this.tinhTrang = tinhTrang; }

    public String getVanBanBh() { return vanBanBh; }
    public void setVanBanBh(String vanBanBh) { this.vanBanBh = vanBanBh; }

    public BigDecimal getVonDieuLe() { return vonDieuLe; }
    public void setVonDieuLe(BigDecimal vonDieuLe) { this.vonDieuLe = vonDieuLe; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
}
