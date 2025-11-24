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
@Table(name = "DM_MA_KBAC_NN_0008")
@Entity(name = "dmkabacnn0008")
public class DmKabacnn0008 {

    @Column(name = "IDBANGHI")
    private String idBanghi;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "MA")
    private String ma;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "GHI_CHU")
    private String ghiChu;

    @Column(name = "TINHTRANG")
    private Integer tinhTrang;

    @Column(name = "MA_CHA")
    private String maCha;

    @Column(name = "ID_CHA")
    private String idCha;

    @Column(name = "LOAI")
    private Integer loai;

    @Column(name = "MA_T")
    private String maT;

    @Column(name = "MA_H")
    private String maH;

    @Column(name = "CAP")
    private Integer cap;

    @Column(name = "START_DATE_ACTIVE")
    private LocalDate startDateActive;

    @Column(name = "END_DATE_ACTIVE")
    private LocalDate endDateActive;

    @Column(name = "CREATION_DATE")
    private LocalDate creationDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "LAST_UPDATE_DATE")
    private LocalDate lastUpdateDate;

    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "MA_CU")
    private String maCu;


    // Getters / Setters

    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Integer getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(Integer tinhTrang) { this.tinhTrang = tinhTrang; }

    public String getMaCha() { return maCha; }
    public void setMaCha(String maCha) { this.maCha = maCha; }

    public String getIdCha() { return idCha; }
    public void setIdCha(String idCha) { this.idCha = idCha; }

    public Integer getLoai() { return loai; }
    public void setLoai(Integer loai) { this.loai = loai; }

    public String getMaT() { return maT; }
    public void setMaT(String maT) { this.maT = maT; }

    public String getMaH() { return maH; }
    public void setMaH(String maH) { this.maH = maH; }

    public Integer getCap() { return cap; }
    public void setCap(Integer cap) { this.cap = cap; }

    public LocalDate getStartDateActive() { return startDateActive; }
    public void setStartDateActive(LocalDate startDateActive) { this.startDateActive = startDateActive; }

    public LocalDate getEndDateActive() { return endDateActive; }
    public void setEndDateActive(LocalDate endDateActive) { this.endDateActive = endDateActive; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDate getLastUpdateDate() { return lastUpdateDate; }
    public void setLastUpdateDate(LocalDate lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    public String getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }

    public String getVanBanBh() { return vanBanBh; }
    public void setVanBanBh(String vanBanBh) { this.vanBanBh = vanBanBh; }

    public LocalDate getNgayVb() { return ngayVb; }
    public void setNgayVb(LocalDate ngayVb) { this.ngayVb = ngayVb; }

    public String getMaCu() { return maCu; }
    public void setMaCu(String maCu) { this.maCu = maCu; }
}
