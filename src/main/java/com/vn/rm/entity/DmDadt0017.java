package com.vn.rm.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JmixEntity
@Table(name = "DM_DADT_0017")
@Entity
public class DmDadt0017 {

    @Column(name = "IDBANGHI")
    private String idBanghi;

    @Column(name = "ID")
    @Id
    private String idCol;

    @Lob
    @Column(name = "MA")
    private String ma;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "MA_CHA")
    private String maCha;

    @Column(name = "CQTC_MA")
    private String cqtcMa;

    @Column(name = "DVQLTT_MA")
    private String dvqlttMa;

    @Column(name = "DVQLTT_TEN")
    private String dvqlttTen;

    @Column(name = "CDT_MA")
    private String cdtMa;

    @Column(name = "CDT_TEN")
    private String cdtTen;

    @Column(name = "BQL_MA")
    private String bqlMa;

    @Column(name = "BQL_TEN")
    private String bqlTen;

    @Lob
    @Column(name = "SOQD_TL")
    private String soqdTl;

    @Column(name = "NGAY_TL")
    private LocalDate ngayTl;

    @Column(name = "COQUAN_TL")
    private String coquanTl;

    @Column(name = "LOAIDA_MA")
    private String loaidaMa;

    @Column(name = "LOAIDA_TEN")
    private String loaidaTen;

    @Column(name = "NHOMDA_MA")
    private String nhomdaMa;

    @Column(name = "NHOMDA_TEN")
    private String nhomdaTen;

    @Column(name = "CTMT_MA")
    private String ctmtMa;

    @Column(name = "CTMT_TEN")
    private String ctmtTen;

    @Column(name = "HTDA_MA")
    private String htdaMa;

    @Column(name = "HTDA_TEN")
    private String htdaTen;

    @Column(name = "HTQL_MA")
    private String htqlMa;

    @Column(name = "HTQL_TEN")
    private String htqlTen;

    @Column(name = "NGAY_TAO")
    private LocalDateTime ngayTao;

    @Column(name = "NGUOI_TAO")
    private String nguoiTao;

    @Column(name = "NGAY_SUA")
    private LocalDateTime ngaySua;

    @Column(name = "NGUOI_SUA")
    private String nguoiSua;

    @Column(name = "LY_DO_SUA")
    private String lyDoSua;

    @Column(name = "TRANGTHAI_MA")
    private String trangthaiMa;

    @Column(name = "TRANGTHAI_CU")
    private String trangthaiCu;

    @Column(name = "NGAY_DMO")
    private LocalDate ngayDmo;

    // 0/1
    @Column(name = "TRANGTHAI_DM")
    private Integer trangthaiDm;


    // Getters / Setters

    public String getIdBanghi() { return idBanghi; }
    public void setIdBanghi(String idBanghi) { this.idBanghi = idBanghi; }

    public String getIdCol() { return idCol; }
    public void setIdCol(String idCol) { this.idCol = idCol; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getTen() { return ten; }public void setTen(String ten) { this.ten = ten; }

    public String getMaCha() { return maCha; }
    public void setMaCha(String maCha) { this.maCha = maCha; }

    public String getCqtcMa() { return cqtcMa; }
    public void setCqtcMa(String cqtcMa) { this.cqtcMa = cqtcMa; }

    public String getDvqlttMa() { return dvqlttMa; }
    public void setDvqlttMa(String dvqlttMa) { this.dvqlttMa = dvqlttMa; }

    public String getDvqlttTen() { return dvqlttTen; }
    public void setDvqlttTen(String dvqlttTen) { this.dvqlttTen = dvqlttTen; }

    public String getCdtMa() { return cdtMa; }
    public void setCdtMa(String cdtMa) { this.cdtMa = cdtMa; }

    public String getCdtTen() { return cdtTen; }
    public void setCdtTen(String cdtTen) { this.cdtTen = cdtTen; }

    public String getBqlMa() { return bqlMa; }
    public void setBqlMa(String bqlMa) { this.bqlMa = bqlMa; }

    public String getBqlTen() { return bqlTen; }
    public void setBqlTen(String bqlTen) { this.bqlTen = bqlTen; }

    public String getSoqdTl() { return soqdTl; }
    public void setSoqdTl(String soqdTl) { this.soqdTl = soqdTl; }

    public LocalDate getNgayTl() { return ngayTl; }
    public void setNgayTl(LocalDate ngayTl) { this.ngayTl = ngayTl; }

    public String getCoquanTl() { return coquanTl; }
    public void setCoquanTl(String coquanTl) { this.coquanTl = coquanTl; }

    public String getLoaidaMa() { return loaidaMa; }
    public void setLoaidaMa(String loaidaMa) { this.loaidaMa = loaidaMa; }

    public String getLoaidaTen() { return loaidaTen; }
    public void setLoaidaTen(String loaidaTen) { this.loaidaTen = loaidaTen; }

    public String getNhomdaMa() { return nhomdaMa; }
    public void setNhomdaMa(String nhomdaMa) { this.nhomdaMa = nhomdaMa; }

    public String getNhomdaTen() { return nhomdaTen; }
    public void setNhomdaTen(String nhomdaTen) { this.nhomdaTen = nhomdaTen; }

    public String getCtmtMa() { return ctmtMa; }
    public void setCtmtMa(String ctmtMa) { this.ctmtMa = ctmtMa; }

    public String getCtmtTen() { return ctmtTen; }
    public void setCtmtTen(String ctmtTen) { this.ctmtTen = ctmtTen; }

    public String getHtdaMa() { return htdaMa; }
    public void setHtdaMa(String htdaMa) { this.htdaMa = htdaMa; }

    public String getHtdaTen() { return htdaTen; }
    public void setHtdaTen(String htdaTen) { this.htdaTen = htdaTen; }

    public String getHtqlMa() { return htqlMa; }
    public void setHtqlMa(String htqlMa) { this.htqlMa = htqlMa; }

    public String getHtqlTen() { return htqlTen; }
    public void setHtqlTen(String htqlTen) { this.htqlTen = htqlTen; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public LocalDateTime getNgaySua() { return ngaySua; }public void setNgaySua(LocalDateTime ngaySua) { this.ngaySua = ngaySua; }

    public String getNguoiSua() { return nguoiSua; }
    public void setNguoiSua(String nguoiSua) { this.nguoiSua = nguoiSua; }

    public String getLyDoSua() { return lyDoSua; }
    public void setLyDoSua(String lyDoSua) { this.lyDoSua = lyDoSua; }

    public String getTrangthaiMa() { return trangthaiMa; }
    public void setTrangthaiMa(String trangthaiMa) { this.trangthaiMa = trangthaiMa; }

    public String getTrangthaiCu() { return trangthaiCu; }
    public void setTrangthaiCu(String trangthaiCu) { this.trangthaiCu = trangthaiCu; }

    public LocalDate getNgayDmo() { return ngayDmo; }
    public void setNgayDmo(LocalDate ngayDmo) { this.ngayDmo = ngayDmo; }

    public Integer getTrangthaiDm() { return trangthaiDm; }
    public void setTrangthaiDm(Integer trangthaiDm) { this.trangthaiDm = trangthaiDm; }
}
