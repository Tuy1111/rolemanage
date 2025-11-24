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
@Table(name = "DM_CTCKHOAN_0035")
@Entity(name = "dmctckhoan0035")
public class DmCtckhoan0035 {


    @Column(name = "IDBANGHI", nullable = false)
    private String idBanghi;

    @Id
    @Column(name = "ID")
    private String idCol;

    @Column(name = "TEN_CONG_TY")
    private String tenCongTy;

    @Column(name = "TEN_TIENG_ANH")
    private String tenTiengAnh;

    @Column(name = "TEN_VIET_TAT")
    private String tenVietTat;

    @Column(name = "SO_GPKD")
    private String soGpkd;

    @Column(name = "NGAY_CAP_GPKD")
    private LocalDate ngayCapGpkd;

    @Column(name = "NOI_CAP_GPKD")
    private String noiCapGpkd;

    @Column(name = "DIA_CHI")
    private String diaChi;

    @Column(name = "DM_PHUONG_XA_ID")
    private String dmPhuongXaId;

    @Column(name = "DM_QUAN_HUYEN_ID")
    private String dmQuanHuyenId;

    @Column(name = "DM_TINH_THANH_ID")
    private String dmTinhThanhId;

    @Column(name = "DIEN_THOAI")
    private String dienThoai;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "WEBSITE")
    private String website;

    @Column(name = "VON_DIEU_LE", precision = 19, scale = 2)
    private BigDecimal vonDieuLe;

    @Column(name = "NGUOI_DAI_DIEN_PL")
    private String nguoiDaiDienPl;

    @Column(name = "TONG_GIAM_DOC")
    private String tongGiamDoc;

    @Column(name = "CAN_BO_CBTT")
    private String canBoCbtt;

    @Column(name = "EMAIL_CBTT")
    private String emailCbtt;

    @Column(name = "NIEM_YET")
    private Integer niemYet;

    @Column(name = "NGAY_NIEM_YET")
    private LocalDate ngayNiemYet;

    @Column(name = "THI_TRUONG")
    private String thiTruong;

    @Column(name = "MA_CHUNG_KHOAN")
    private String maChungKhoan;

    @Column(name = "DAI_CHUNG")
    private Integer daiChung;

    @Column(name = "NGAY_DK_DAI_CHUNG")
    private LocalDate ngayDkDaiChung;

    @Column(name = "NGAY_CHAM_DUT_DAI_CHUNG")
    private LocalDate ngayChamDutDaiChung;

    @Column(name = "TRANG_THAI")
    private Integer trangThai;

    @Column(name = "LOAI_HINH_DN")
    private String loaiHinhDn;

    @Column(name = "NGHIEP_VU_KD")
    private String nghiepVuKd;

    @Column(name = "NGAY_CAP_NHAT")
    private LocalDate ngayCapNhat;

    @Column(name = "NGAY_TAO")
    private LocalDate ngayTao;

    @Column(name = "NGUOI_TAO")
    private String nguoiTao;

    @Column(name = "NGAY_HL")
    private LocalDate ngayHl;

    @Column(name = "NGAY_KT")
    private LocalDate ngayKt;


    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getTenCongTy() { return tenCongTy; }
    public void setTenCongTy(String tenCongTy) { this.tenCongTy = tenCongTy; }

    public String getTenTiengAnh() { return tenTiengAnh; }
    public void setTenTiengAnh(String tenTiengAnh) { this.tenTiengAnh = tenTiengAnh; }

    public String getTenVietTat() { return tenVietTat; }
    public void setTenVietTat(String tenVietTat) { this.tenVietTat = tenVietTat; }

    public String getSoGpkd() { return soGpkd; }
    public void setSoGpkd(String soGpkd) { this.soGpkd = soGpkd; }

    public LocalDate getNgayCapGpkd() { return ngayCapGpkd; }
    public void setNgayCapGpkd(LocalDate ngayCapGpkd) { this.ngayCapGpkd = ngayCapGpkd; }

    public String getNoiCapGpkd() { return noiCapGpkd; }
    public void setNoiCapGpkd(String noiCapGpkd) { this.noiCapGpkd = noiCapGpkd; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getDmPhuongXaId() { return dmPhuongXaId; }
    public void setDmPhuongXaId(String dmPhuongXaId) { this.dmPhuongXaId = dmPhuongXaId; }

    public String getDmQuanHuyenId() { return dmQuanHuyenId; }
    public void setDmQuanHuyenId(String dmQuanHuyenId) { this.dmQuanHuyenId = dmQuanHuyenId; }

    public String getDmTinhThanhId() { return dmTinhThanhId; }
    public void setDmTinhThanhId(String dmTinhThanhId) { this.dmTinhThanhId = dmTinhThanhId; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public BigDecimal getVonDieuLe() { return vonDieuLe; }
    public void setVonDieuLe(BigDecimal vonDieuLe) { this.vonDieuLe = vonDieuLe; }

    public String getNguoiDaiDienPl() { return nguoiDaiDienPl; }
    public void setNguoiDaiDienPl(String nguoiDaiDienPl) { this.nguoiDaiDienPl = nguoiDaiDienPl; }

    public String getTongGiamDoc() { return tongGiamDoc; }
    public void setTongGiamDoc(String tongGiamDoc) { this.tongGiamDoc = tongGiamDoc; }

    public String getCanBoCbtt() { return canBoCbtt; }
    public void setCanBoCbtt(String canBoCbtt) { this.canBoCbtt = canBoCbtt; }

    public String getEmailCbtt() { return emailCbtt; }
    public void setEmailCbtt(String emailCbtt) { this.emailCbtt = emailCbtt; }

    public Integer getNiemYet() { return niemYet; }
    public void setNiemYet(Integer niemYet) { this.niemYet = niemYet; }

    public LocalDate getNgayNiemYet() { return ngayNiemYet; }
    public void setNgayNiemYet(LocalDate ngayNiemYet) { this.ngayNiemYet = ngayNiemYet; }

    public String getThiTruong() { return thiTruong; }
    public void setThiTruong(String thiTruong) { this.thiTruong = thiTruong; }

    public String getMaChungKhoan() { return maChungKhoan; }
    public void setMaChungKhoan(String maChungKhoan) { this.maChungKhoan = maChungKhoan; }

    public Integer getDaiChung() { return daiChung; }
    public void setDaiChung(Integer daiChung) { this.daiChung = daiChung; }

    public LocalDate getNgayDkDaiChung() { return ngayDkDaiChung; }
    public void setNgayDkDaiChung(LocalDate ngayDkDaiChung) { this.ngayDkDaiChung = ngayDkDaiChung; }

    public LocalDate getNgayChamDutDaiChung() { return ngayChamDutDaiChung; }
    public void setNgayChamDutDaiChung(LocalDate ngayChamDutDaiChung) { this.ngayChamDutDaiChung = ngayChamDutDaiChung; }

    public Integer getTrangThai() { return trangThai; }
    public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }

    public String getLoaiHinhDn() { return loaiHinhDn; }
    public void setLoaiHinhDn(String loaiHinhDn) { this.loaiHinhDn = loaiHinhDn; }

    public String getNghiepVuKd() { return nghiepVuKd; }
    public void setNghiepVuKd(String nghiepVuKd) { this.nghiepVuKd = nghiepVuKd; }

    public LocalDate getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDate ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public LocalDate getNgayHl() { return ngayHl; }
    public void setNgayHl(LocalDate ngayHl) { this.ngayHl = ngayHl; }

    public LocalDate getNgayKt() { return ngayKt; }
    public void setNgayKt(LocalDate ngayKt) { this.ngayKt = ngayKt; }
}
