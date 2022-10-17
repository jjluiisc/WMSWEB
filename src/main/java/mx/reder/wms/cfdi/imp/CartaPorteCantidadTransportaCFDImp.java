package mx.reder.wms.cfdi.imp;

import mx.reder.wms.cfdi.entity.CartaPorteCantidadTransportaCFD;

/**
 *
 * @author joelbecerramiranda
 */
public class CartaPorteCantidadTransportaCFDImp implements CartaPorteCantidadTransportaCFD {
    public String idOrigen;
    public String idDestino;
    public double cantidad;

    @Override
    public String getIDOrigen() {
        return idOrigen;
    }

    @Override
    public String getIDDestino() {
        return idDestino;
    }

    @Override
    public double getCantidad() {
        return cantidad;
    }
}
