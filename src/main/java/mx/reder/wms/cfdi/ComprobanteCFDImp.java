package mx.reder.wms.cfdi;

import com.atcloud.util.Fecha;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import mx.reder.wms.cfdi.entity.ComprobanteCFD;
import mx.reder.wms.cfdi.entity.DireccionCFD;
import mx.reder.wms.cfdi.entity.EmisorCFD;
import mx.reder.wms.cfdi.entity.EntregarEnCFD;
import mx.reder.wms.cfdi.entity.ReceptorCFD;
import mx.reder.wms.dao.entity.ASPELClienteDAO;
import mx.reder.wms.dao.entity.ASPELFacturaDAO;
import mx.reder.wms.dao.entity.ASPELInformacionEnvioDAO;
import mx.reder.wms.dao.entity.CompaniaDAO;
import mx.reder.wms.dao.entity.DireccionDAO;
import mx.reder.wms.to.ASPELFacturaDetalleTO;
import mx.gob.sat.sitioInternet.cfd.catalogos.CFormaPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMetodoPago;
import mx.gob.sat.sitioInternet.cfd.catalogos.CMoneda;
import mx.gob.sat.sitioInternet.cfd.catalogos.CRegimenFiscal;
import mx.gob.sat.sitioInternet.cfd.catalogos.CTipoDeComprobante;
import mx.gob.sat.sitioInternet.cfd.catalogos.CUsoCFDI;

/**
 *
 * @author joelbecerramiranda
 */
public class ComprobanteCFDImp implements ComprobanteCFD {
    private CompaniaDAO companiaDAO;
    private DireccionDAO direccionDAO;
    private ASPELFacturaDAO aspelFacturaDAO;
    private ASPELClienteDAO aspelClienteDAO;
    private ASPELInformacionEnvioDAO aspelInformacionEnvioPDAO;
    private ArrayList<ASPELFacturaDetalleTO> detalles;

    public ComprobanteCFDImp(CompaniaDAO companiaDAO, DireccionDAO direccionDAO, ASPELFacturaDAO aspelFacturaDAO,
            ASPELClienteDAO aspelClienteDAO, ASPELInformacionEnvioDAO aspelInformacionEnvioPDAO,
            ArrayList<ASPELFacturaDetalleTO> detalles) {
        this.companiaDAO = companiaDAO;
        this.direccionDAO = direccionDAO;
        this.aspelFacturaDAO = aspelFacturaDAO;
        this.aspelClienteDAO = aspelClienteDAO;
        this.aspelInformacionEnvioPDAO = aspelInformacionEnvioPDAO;
        this.detalles = detalles;
    }

    @Override
    public String getSerie() {
        return aspelFacturaDAO.SERIE;
    }

    @Override
    public String getFolio() {
        return String.valueOf(aspelFacturaDAO.FOLIO);
    }

    @Override
    public Calendar getFecha() {
        return Fecha.getCalendar(aspelFacturaDAO.FECHAELAB);
    }

    @Override
    public CFormaPago.Enum getFormaDePago() {
        return CFormaPago.Enum.forString(aspelClienteDAO.FORMADEPAGOSAT);
    }

    @Override
    public CMetodoPago.Enum getMetodoDePago() {
        return CMetodoPago.Enum.forString(aspelClienteDAO.METODODEPAGO);
    }

    @Override
    public String getNumCtaPago() {
        return aspelClienteDAO.NUMCTAPAGO;
    }

    @Override
    public CTipoDeComprobante.Enum getTipoDeComprobante() {
        return CTipoDeComprobante.I;
    }

    @Override
    public CMoneda.Enum getMoneda() {
        return aspelFacturaDAO.NUM_MONED==2 ? CMoneda.USD : CMoneda.MXN;
    }

    @Override
    public String getLugarExpedicion() {
        return direccionDAO.codigopostal;
    }

    @Override
    public String getCfdiRelacionados() {
        return null;
    }

    class EmisorCFDImp implements EmisorCFD {
        @Override
        public String getNombre() {
            return companiaDAO.razonsocial;
        }

        @Override
        public String getRfc() {
            return companiaDAO.rfc;
        }
    }

    private EmisorCFDImp emisorCFD = new EmisorCFDImp();

    @Override
    public EmisorCFD getEmisor() {
        return emisorCFD;
    }

    class DireccionCFDImp implements DireccionCFD {
        @Override
        public String getCalle() {
            return direccionDAO.calle;
        }

        @Override
        public String getNoExterior() {
            return direccionDAO.noexterior;
        }

        @Override
        public String getNoInterior() {
            return direccionDAO.nointerior;
        }

        @Override
        public String getColonia() {
            return direccionDAO.colonia;
        }

        @Override
        public String getMunicipio() {
            return direccionDAO.poblacion;
        }

        @Override
        public String getEstado() {
            return direccionDAO.entidadfederativa;
        }

        @Override
        public String getPais() {
            return direccionDAO.pais;
        }

        @Override
        public String getCodigoPostal() {
            return direccionDAO.codigopostal;
        }
    }

    private DireccionCFDImp direccionCFD = new DireccionCFDImp();

    @Override
    public DireccionCFD getDireccionFiscal() {
        return direccionCFD;
    }

    @Override
    public DireccionCFD getExpedidoEn() {
        return direccionCFD;
    }

    @Override
    public CRegimenFiscal.Enum getRegimenFiscal() {
        return CRegimenFiscal.Enum.forString(companiaDAO.regimenfiscal);
    }

    class ReceptorCFDImp implements ReceptorCFD {
        @Override
        public String getNombre() {
            return aspelClienteDAO.NOMBRE;
        }

        @Override
        public String getRfc() {
            return aspelClienteDAO.RFC.replaceAll("-", "");
        }

        @Override
        public CUsoCFDI.Enum getUsoCFDI() {
            return CUsoCFDI.Enum.forString(aspelClienteDAO.USO_CFDI);
        }
    }

    ReceptorCFDImp receptorCFD = new ReceptorCFDImp();

    @Override
    public ReceptorCFD getReceptor() {
        return receptorCFD;
    }

    class DireccionCFDImp2 implements DireccionCFD {
        @Override
        public String getCalle() {
            return aspelClienteDAO.CALLE;
        }

        @Override
        public String getNoExterior() {
            return aspelClienteDAO.NUMEXT;
        }

        @Override
        public String getNoInterior() {
            return aspelClienteDAO.NUMINT;
        }

        @Override
        public String getColonia() {
            return aspelClienteDAO.COLONIA;
        }

        @Override
        public String getMunicipio() {
            return aspelClienteDAO.MUNICIPIO;
        }

        @Override
        public String getEstado() {
            return aspelClienteDAO.ESTADO;
        }

        @Override
        public String getPais() {
            return aspelClienteDAO.PAIS;
        }

        @Override
        public String getCodigoPostal() {
            return aspelClienteDAO.CODIGO;
        }
    }

    private DireccionCFDImp2 direccionCFD2 = new DireccionCFDImp2();

    @Override
    public DireccionCFD getDomicilio() {
        return direccionCFD2;
    }

    class EntregarEnCFDImp implements EntregarEnCFD {
        @Override
        public String getNombre() {
            return aspelClienteDAO.NOMBRE;
        }

        @Override
        public String getCalle() {
            return aspelInformacionEnvioPDAO.CALLE;
        }

        @Override
        public String getNoExterior() {
            return aspelInformacionEnvioPDAO.NUMEXT;
        }

        @Override
        public String getNoInterior() {
            return aspelInformacionEnvioPDAO.NUMINT;
        }

        @Override
        public String getColonia() {
            return aspelInformacionEnvioPDAO.COLONIA;
        }

        @Override
        public String getMunicipio() {
            return aspelInformacionEnvioPDAO.MUNICIPIO;
        }

        @Override
        public String getEstado() {
            return aspelInformacionEnvioPDAO.ESTADO;
        }

        @Override
        public String getPais() {
            return aspelInformacionEnvioPDAO.PAIS;
        }

        @Override
        public String getCodigoPostal() {
            return aspelInformacionEnvioPDAO.CODIGO;
        }
    }

    private EntregarEnCFDImp entregarEnCFD = new EntregarEnCFDImp();

    @Override
    public EntregarEnCFD getEntregarEn() {
        return entregarEnCFD;
    }

    @Override
    public List getConceptos() {
        return detalles;
    }

    @Override
    public List getImpuestosTrasladados() {
        return null;
    }
}
