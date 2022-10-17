package mx.reder.wms.cfdi.imp;

import com.atcloud.util.Fecha;
import java.util.Calendar;
import java.util.Date;
import mx.reder.wms.cfdi.entity.CartaPorteUbicacionCFD;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CTipoEstacion;
import mx.reder.wms.cfdi.entity.CartaPorteDomicilioCFD;
import mx.gob.sat.cartaPorte20.CartaPorteDocument.CartaPorte;

/**
 *
 * @author joelbecerramiranda
 */
public class CartaPorteUbicacionCFDImp implements CartaPorteUbicacionCFD {
    public String tipoUbicacion;
    public String idUbicacion;
    public String rfcRemitenteDestinatario;
    public String nombreRFC;
    public Date fechaHoraSalidaLlegada;
    public Double distanciaRecorrida;
    public String tipoEstacion;
    public CartaPorteDomicilioCFDImp domicilio;

    @Override
    public CartaPorte.Ubicaciones.Ubicacion.TipoUbicacion.Enum getTipoUbicacion() {
        return CartaPorte.Ubicaciones.Ubicacion.TipoUbicacion.Enum.forString(tipoUbicacion);
    }

    @Override
    public String getIDUbicacion() {
        return idUbicacion;
    }

    @Override
    public String getRFCRemitenteDestinatario() {
        return rfcRemitenteDestinatario;
    }

    @Override
    public String getNombreRFC() {
        return nombreRFC;
    }

    @Override
    public Calendar getFechaHoraSalidaLlegada() {
        return Fecha.getCalendar(fechaHoraSalidaLlegada);
    }

    @Override
    public Double getDistanciaRecorrida() {
        return distanciaRecorrida;
    }

    @Override
    public CTipoEstacion.Enum getTipoEstacion() {
        return CTipoEstacion.Enum.forString(tipoEstacion);
    }

    @Override
    public CartaPorteDomicilioCFD getDomicilio() {
        return domicilio;
    }
}
