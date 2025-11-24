package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.time.LocalDate;

@JmixEntity
@Entity
@Table(name = "DM_NGANHKT_0002")

public class DmNganhkt0002 {

    @Id
    @Column(name = "ID", nullable = false, length = 20)
    private String id;

    @Column(name = "IDBANGHI", length = 20)
    private String idBanghi;

    @Column(name = "MA", length = 20)
    private String ma;

    @Column(name = "TEN", length = 512)
    private String ten;

    @Column(name = "TINHTRANG", length = 5)
    private String tinhTrang;

    @Column(name = "ID_CHA", length = 20)
    private String idCha;

    @Column(name = "MA_CHA", length = 20)
    private String maCha;

    @Column(name = "LOAI", length = 5)
    private String loai;

    @Column(name = "START_DATE_ACTIVE")
    private LocalDate startDateActive;

    @Column(name = "END_DATE_ACTIVE")
    private LocalDate endDateActive;

    @Column(name = "CREATION_DATE")
    private LocalDate creationDate;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "LAST_UPDATE_DATE")
    private LocalDate lastUpdateDate;

    @Column(name = "LAST_UPDATED_BY", length = 50)
    private String lastUpdatedBy;

    @Column(name = "VAN_BAN_BH", columnDefinition = "TEXT")
    private String vanBanBh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "MA_CU", length = 512)
    private String maCu;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdBanghi() {
        return idBanghi;
    }

    public void setIdBanghi(String idBanghi) {
        this.idBanghi = idBanghi;
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

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
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

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
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
