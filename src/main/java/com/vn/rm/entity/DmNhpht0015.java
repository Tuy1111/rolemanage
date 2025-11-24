package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_NHPHT_0015")
@Entity
public class DmNhpht0015 {

    @Id
    @Column(name = "ID", nullable = false, length = 20)
    private String id; // Khóa chính

    @Column(name = "IDBANGHI", length = 20)
    private String idbanghi;

    @Column(name = "MA_NH", length = 50)
    private String maNh;

    @Column(name = "MA_TINH", length = 10)
    private String maTinh;

    @Column(name = "MA_HUYEN", length = 10)
    private String maHuyen;

    @Column(name = "TEN", length = 255)
    private String ten;

    @Column(name = "TINH_TRANG", length = 5)
    private String tinhTrang;

    @Column(name = "START_DATE_ACTIVE")
    private LocalDate startDateActive;

    @Column(name = "END_DATE_ACTIVE")
    private LocalDate endDateActive;

    @Column(name = "CREATION_DATE")
    private LocalDate creationDate;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "LAST_UPDATE_DATE")
    private LocalDate lastUpdateDate;

    @Column(name = "LAST_UPDATED_BY", length = 100)
    private String lastUpdatedBy;

    @Column(name = "VAN_BAN_BH", length = 255)
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "MA_CU", length = 255)
    private String maCu;

    // ===== Getters & Setters =====

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

    public String getMaNh() {
        return maNh;
    }

    public void setMaNh(String maNh) {
        this.maNh = maNh;
    }

    public String getMaTinh() {
        return maTinh;
    }

    public void setMaTinh(String maTinh) {
        this.maTinh = maTinh;
    }

    public String getMaHuyen() {
        return maHuyen;
    }

    public void setMaHuyen(String maHuyen) {
        this.maHuyen = maHuyen;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public LocalDate getStartDateActive() {
        return startDateActive;
    }

    public void setStartDateActive(LocalDate startDateActive) {
        this.startDateActive = startDateActive;
    }

    public LocalDate getEndDateActive() {
        return endDateActive;
    }

    public void setEndDateActive(LocalDate endDateActive) {
        this.endDateActive = endDateActive;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
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

    public String getMaCu() {
        return maCu;
    }

    public void setMaCu(String maCu) {
        this.maCu = maCu;
    }
}
