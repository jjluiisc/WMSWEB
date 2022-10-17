package mx.reder.wms.cfdi.entity;

import java.util.Calendar;
import mx.gob.sat.cartaPorte20.CartaPorteDocument.CartaPorte;
import mx.gob.sat.sitioInternet.cfd.catalogos.cartaPorte.CTipoEstacion;

public interface CartaPorteUbicacionCFD {
    public CartaPorte.Ubicaciones.Ubicacion.TipoUbicacion.Enum getTipoUbicacion();
    public String getIDUbicacion();
    public String getRFCRemitenteDestinatario();
    public String getNombreRFC();
    public Calendar getFechaHoraSalidaLlegada();
    public Double getDistanciaRecorrida();
    public CTipoEstacion.Enum getTipoEstacion();
    public CartaPorteDomicilioCFD getDomicilio();
}
