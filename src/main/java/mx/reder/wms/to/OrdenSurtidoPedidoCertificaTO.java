package mx.reder.wms.to;

import java.util.Date;

public class OrdenSurtidoPedidoCertificaTO {
    public String compania = "";
    public int flsurtido = 0;
    public int partida = 0;
    public int idlote = 0;
    public int idcontenedor = 0;
    public String codigo = "";
    public String descripcion = "";
    public String contenedor = "";
    public String lote = "";
    public Date fecaducidad = new Date(0);
    public double certificadas = 0.0;
    public double preciopublico = 0.0;
    public double precio = 0.0;
    public double total = 0.0;
    public double iva = 0.0;
}
