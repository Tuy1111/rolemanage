package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_NDKT_0003")
@Entity(name = "dmndkt0003")
public class DmNdkt0003 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Id
    @Column(name = "ID", nullable = false)
    private String idCol;

    @Column(name = "MA")
    private String ma;

    @Lob
    @Column(name = "TEN")
    private String ten;

    @Column(name = "ID_CHA")
    private String idCha;

    @Column(name = "MA_CHA")
    private String maCha;

    @Column(name = "ID_NHOM")
    private String idNhom;

    @Column(name = "MA_NHOM")
    private String maNhom;

    @Column(name = "ID_TNHOM")
    private String idTnhom;

    @Column(name = "MA_TNHOM")
    private String maTnhom;

    @Column(name = "TINH_TRANG")
    private Integer tinhTrang;

    @Column(name = "MA_MUC")
    private String maMuc;

    @Column(name = "MA_TMUC")
    private String maTmuc;

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

    @Lob
    @Column(name = "MA_CU")
    private String maCu;




    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getIdCha() { return idCha; }
    public void setIdCha(String idCha) { this.idCha = idCha; }

    public String getMaCha() { return maCha; }
    public void setMaCha(String maCha) { this.maCha = maCha; }

    public String getIdNhom() { return idNhom; }
    public void setIdNhom(String idNhom) { this.idNhom = idNhom; }

    public String getMaNhom() { return maNhom; }
    public void setMaNhom(String maNhom) { this.maNhom = maNhom; }

    public String getIdTnhom() { return idTnhom; }
    public void setIdTnhom(String idTnhom) { this.idTnhom = idTnhom; }

    public String getMaTnhom() { return maTnhom; }
    public void setMaTnhom(String maTnhom) { this.maTnhom = maTnhom; }

    public Integer getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(Integer tinhTrang) { this.tinhTrang = tinhTrang; }

    public String getMaMuc() { return maMuc; }
    public void setMaMuc(String maMuc) { this.maMuc = maMuc; }

    public String getMaTmuc() { return maTmuc; }
    public void setMaTmuc(String maTmuc) { this.maTmuc = maTmuc; }

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
