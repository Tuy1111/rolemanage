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
@Table(name = "DM_KIEMTOANVIEN_0041")
@Entity(name = "dmkiemtoanvien0041")
public class DmKiemtoanvien0041 {

    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Column(name = "CHUC_VU")
    private String chucVu;

    @Column(name = "GHI_CHU")
    private String ghiChu;

    @Column(name = "GIOI_TINH")
    private Integer gioiTinh;

    @Column(name = "HIEU_LUC")
    private Integer hieuLuc;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "BAN_THOIGIAN")
    private Integer banThoigian;

    @Column(name = "MA")
    private String ma;

    @Column(name = "ID_CTY")
    private String idCty;

    @Column(name = "MA_CTY")
    private String maCty;

    @Column(name = "NAM_SINH")
    private Integer namSinh;

    @Column(name = "NGAY_VB")
    private LocalDate ngayVb;

    @Column(name = "NGAY_CAP")
    private LocalDate ngayCap;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;

    @Column(name = "NGAY_PS")
    private LocalDate ngayPs;

    @Column(name = "NGAY_SD")
    private LocalDate ngaySd;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "QUE_QUAN")
    private String queQuan;

    @Column(name = "SO_THE")
    private String soThe;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "VAN_BAN_BH")
    private String vanBanBh;



    // Getters / Setters
    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Integer getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(Integer gioiTinh) { this.gioiTinh = gioiTinh; }

    public Integer getHieuLuc() { return hieuLuc; }
    public void setHieuLuc(Integer hieuLuc) { this.hieuLuc = hieuLuc; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public Integer getBanThoigian() { return banThoigian; }
    public void setBanThoigian(Integer banThoigian) { this.banThoigian = banThoigian; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getIdCty() { return idCty; }
    public void setIdCty(String idCty) { this.idCty = idCty; }

    public String getMaCty() { return maCty; }
    public void setMaCty(String maCty) { this.maCty = maCty; }

    public Integer getNamSinh() { return namSinh; }
    public void setNamSinh(Integer namSinh) { this.namSinh = namSinh; }

    public LocalDate getNgayVb() { return ngayVb; }
    public void setNgayVb(LocalDate ngayVb) { this.ngayVb = ngayVb; }

    public LocalDate getNgayCap() { return ngayCap; }
    public void setNgayCap(LocalDate ngayCap) { this.ngayCap = ngayCap; }

    public LocalDate getNgayHl() { return ngayHl; }
    public void setNgayHl(LocalDate ngayHl) { this.ngayHl = ngayHl; }

    public LocalDate getNgayKt() { return ngayKt; }
    public void setNgayKt(LocalDate ngayKt) { this.ngayKt = ngayKt; }

    public LocalDate getNgayPs() { return ngayPs; }
    public void setNgayPs(LocalDate ngayPs) { this.ngayPs = ngayPs; }

    public LocalDate getNgaySd() { return ngaySd; }
    public void setNgaySd(LocalDate ngaySd) { this.ngaySd = ngaySd; }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }

    public String getSoThe() { return soThe; }
    public void setSoThe(String soThe) { this.soThe = soThe; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getVanBanBh() { return vanBanBh; }
    public void setVanBanBh(String vanBanBh) { this.vanBanBh = vanBanBh; }
}
