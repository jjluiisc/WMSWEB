package mx.reder.wms.to;

import java.io.Serializable;
import java.util.Date;

public class CartaPorteFacturaTO implements Serializable {
    public String compania = "";
    public int flsurtido = 0;
    public int idcartaporte = 0;
    public int parada = 0;
    public String status = "";
    public Date fechastatus = new Date(0);
    public String usuario = "";
    public String serie = "";
    public String factura = null;
    public Date fechafacturacion = null;

    public double distancia = 0.0;
}
