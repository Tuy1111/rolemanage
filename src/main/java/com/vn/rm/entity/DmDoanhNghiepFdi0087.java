package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@JmixEntity
@Table(name = "DM_DOANH_NGHIEP_FDI_0087")
@Entity
public class DmDoanhNghiepFdi0087 {

    @Id
    @Column(name = "ID", nullable = false, length = 30)
    private String id; // Khóa chính

    @Column(name = "IDBANGHI", length = 30)
    private String idbanghi;

    @Column(name = "MST", length = 50)
    private String mst;

    @Column(name = "TEN_NNT", length = 512)
    private String tenNnt;

    @Column(name = "TEN_CQT_QLY", length = 512)
    private String tenCqtQly;

    @Column(name = "TEN_LOAI_NNT", length = 255)
    private String tenLoaiNnt;

    @Column(name = "TRANG_THAI", length = 10)
    private String trangThai;

    @Column(name = "TEN_TRANG_THAI", length = 255)
    private String tenTrangThai;

    @Column(name = "NGAY_HLUC_TU")
    private LocalDate ngayHlucTu;

    @Column(name = "NGAY_HLUC_DEN")
    private LocalDate ngayHlucDen;

    @Column(name = "NNKD", columnDefinition = "TEXT")
    private String nnkd;

    @Column(name = "TEN_CHU_DN", length = 255)
    private String tenChuDn;

    @Column(name = "CHUC_VU", length = 255)
    private String chucVu;

    @Column(name = "NGAY_CAP_MST")
    private LocalDate ngayCapMst;

    @Column(name = "VON_DLE_VN")
    private String vonDleVn;

    @Column(name = "VON_DLE_NN")
    private String vonDleNn;

    @Column(name = "LOAI_VON_DLE_VN", length = 50)
    private String loaiVonDleVn;

    @Column(name = "LOAI_VON_DLE_NN", length = 50)
    private String loaiVonDleNn;

    // --- DKT_GIAY_TO ---
    @Column(name = "DKT_GT_QUOC_GIA", length = 50)
    private String dktGtQuocGia;

    @Column(name = "DKT_GT_MA_TINH", length = 50)
    private String dktGtMaTinh;

    @Column(name = "DKT_GT_NGAY_CAP")
    private LocalDate dktGtNgayCap;

    @Column(name = "DKT_GT_LOAI", length = 50)
    private String dktGtLoai;

    @Column(name = "DKT_GT_LOAI_GIAYTO", length = 255)
    private String dktGtLoaiGiayTo;

    @Column(name = "DKT_GT_SO_GIAY_TO", length = 255)
    private String dktGtSoGiayTo;

    @Column(name = "DKT_GT_NOI_CAP", length = 255)
    private String dktGtNoiCap;

    // --- DKT_DIA_CHI ---
    @Column(name = "DKT_DC_MA_HUYEN", length = 50)
    private String dktDcMaHuyen;

    @Column(name = "DKT_DC_QUAN_HUYEN", length = 255)
    private String dktDcQuanHuyen;

    @Column(name = "DKT_DC_QUOC_GIA", length = 50)
    private String dktDcQuocGia;

    @Column(name = "DKT_DC_TINH_TP", length = 255)
    private String dktDcTinhTp;

    @Column(name = "DKT_DC_MA_TINH", length = 50)
    private String dktDcMaTinh;

    @Column(name = "DKT_DC_LOAI", length = 50)
    private String dktDcLoai;

    @Column(name = "DKT_DC_DIA_CHI", columnDefinition = "TEXT")
    private String dktDcDiaChi;

    @Column(name = "DKT_DC_MA_XA", length = 50)
    private String dktDcMaXa;

    @Column(name = "DKT_DC_DIEN_THOAI", length = 100)
    private String dktDcDienThoai;

    @Column(name = "DKT_DC_PHUONG_XA", length = 255)
    private String dktDcPhuongXa;

    @Column(name = "DKT_DC_EMAIL", length = 255)
    private String dktDcEmail;

    @Column(name = "DKT_DC_LOAI_DIA_CHI", length = 255)
    private String dktDcLoaiDiaChi;

    // ===== Getters & Setters =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdbanghi() { return idbanghi; }
    public void setIdbanghi(String idbanghi) { this.idbanghi = idbanghi; }

    public String getMst() { return mst; }
    public void setMst(String mst) { this.mst = mst; }

    public String getTenNnt() { return tenNnt; }
    public void setTenNnt(String tenNnt) { this.tenNnt = tenNnt; }

    public String getTenCqtQly() { return tenCqtQly; }
    public void setTenCqtQly(String tenCqtQly) { this.tenCqtQly = tenCqtQly; }

    public String getTenLoaiNnt() { return tenLoaiNnt; }
    public void setTenLoaiNnt(String tenLoaiNnt) { this.tenLoaiNnt = tenLoaiNnt; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenTrangThai() { return tenTrangThai; }
    public void setTenTrangThai(String tenTrangThai) { this.tenTrangThai = tenTrangThai; }

    public LocalDate getNgayHlucTu() { return ngayHlucTu; }
    public void setNgayHlucTu(LocalDate ngayHlucTu) { this.ngayHlucTu = ngayHlucTu; }

    public LocalDate getNgayHlucDen() { return ngayHlucDen; }
    public void setNgayHlucDen(LocalDate ngayHlucDen) { this.ngayHlucDen = ngayHlucDen; }

    public String getNnkd() { return nnkd; }
    public void setNnkd(String nnkd) { this.nnkd = nnkd; }

    public String getTenChuDn() { return tenChuDn; }
    public void setTenChuDn(String tenChuDn) { this.tenChuDn = tenChuDn; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public LocalDate getNgayCapMst() { return ngayCapMst; }
    public void setNgayCapMst(LocalDate ngayCapMst) { this.ngayCapMst = ngayCapMst; }

    public String getVonDleVn() { return vonDleVn; }
    public void setVonDleVn(String vonDleVn) { this.vonDleVn = vonDleVn; }

    public String getVonDleNn() { return vonDleNn; }
    public void setVonDleNn(String vonDleNn) { this.vonDleNn = vonDleNn; }

    public String getLoaiVonDleVn() { return loaiVonDleVn; }
    public void setLoaiVonDleVn(String loaiVonDleVn) { this.loaiVonDleVn = loaiVonDleVn; }

    public String getLoaiVonDleNn() { return loaiVonDleNn; }
    public void setLoaiVonDleNn(String loaiVonDleNn) { this.loaiVonDleNn = loaiVonDleNn; }

    public String getDktGtQuocGia() { return dktGtQuocGia; }
    public void setDktGtQuocGia(String dktGtQuocGia) { this.dktGtQuocGia = dktGtQuocGia; }

    public String getDktGtMaTinh() { return dktGtMaTinh; }
    public void setDktGtMaTinh(String dktGtMaTinh) { this.dktGtMaTinh = dktGtMaTinh; }

    public LocalDate getDktGtNgayCap() { return dktGtNgayCap; }
    public void setDktGtNgayCap(LocalDate dktGtNgayCap) { this.dktGtNgayCap = dktGtNgayCap; }

    public String getDktGtLoai() { return dktGtLoai; }
    public void setDktGtLoai(String dktGtLoai) { this.dktGtLoai = dktGtLoai; }

    public String getDktGtLoaiGiayTo() { return dktGtLoaiGiayTo; }
    public void setDktGtLoaiGiayTo(String dktGtLoaiGiayTo) { this.dktGtLoaiGiayTo = dktGtLoaiGiayTo; }

    public String getDktGtSoGiayTo() { return dktGtSoGiayTo; }
    public void setDktGtSoGiayTo(String dktGtSoGiayTo) { this.dktGtSoGiayTo = dktGtSoGiayTo; }

    public String getDktGtNoiCap() { return dktGtNoiCap; }
    public void setDktGtNoiCap(String dktGtNoiCap) { this.dktGtNoiCap = dktGtNoiCap; }

    public String getDktDcMaHuyen() { return dktDcMaHuyen; }
    public void setDktDcMaHuyen(String dktDcMaHuyen) { this.dktDcMaHuyen = dktDcMaHuyen; }

    public String getDktDcQuanHuyen() { return dktDcQuanHuyen; }
    public void setDktDcQuanHuyen(String dktDcQuanHuyen) { this.dktDcQuanHuyen = dktDcQuanHuyen; }

    public String getDktDcQuocGia() { return dktDcQuocGia; }
    public void setDktDcQuocGia(String dktDcQuocGia) { this.dktDcQuocGia = dktDcQuocGia; }

    public String getDktDcTinhTp() { return dktDcTinhTp; }
    public void setDktDcTinhTp(String dktDcTinhTp) { this.dktDcTinhTp = dktDcTinhTp; }

    public String getDktDcMaTinh() { return dktDcMaTinh; }
    public void setDktDcMaTinh(String dktDcMaTinh) { this.dktDcMaTinh = dktDcMaTinh; }

    public String getDktDcLoai() { return dktDcLoai; }
    public void setDktDcLoai(String dktDcLoai) { this.dktDcLoai = dktDcLoai; }

    public String getDktDcDiaChi() { return dktDcDiaChi; }
    public void setDktDcDiaChi(String dktDcDiaChi) { this.dktDcDiaChi = dktDcDiaChi; }

    public String getDktDcMaXa() { return dktDcMaXa; }
    public void setDktDcMaXa(String dktDcMaXa) { this.dktDcMaXa = dktDcMaXa; }

    public String getDktDcDienThoai() { return dktDcDienThoai; }
    public void setDktDcDienThoai(String dktDcDienThoai) { this.dktDcDienThoai = dktDcDienThoai; }

    public String getDktDcPhuongXa() { return dktDcPhuongXa; }
    public void setDktDcPhuongXa(String dktDcPhuongXa) { this.dktDcPhuongXa = dktDcPhuongXa; }

    public String getDktDcEmail() { return dktDcEmail; }
    public void setDktDcEmail(String dktDcEmail) { this.dktDcEmail = dktDcEmail; }

    public String getDktDcLoaiDiaChi() { return dktDcLoaiDiaChi; }
    public void setDktDcLoaiDiaChi(String dktDcLoaiDiaChi) { this.dktDcLoaiDiaChi = dktDcLoaiDiaChi; }
}
