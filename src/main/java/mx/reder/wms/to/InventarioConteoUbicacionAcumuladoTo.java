/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.to;

import java.math.BigDecimal;

/**
 *
 * @author Luis
 */
public class InventarioConteoUbicacionAcumuladoTo {
    public String codigo = "";
    public String descripcion = "";
    public String laboratorio = "";
    public String ubicaciones = "";
    public String lotes = "";    
    public BigDecimal existencia1 = BigDecimal.ZERO;
    public BigDecimal existencia2 = BigDecimal.ZERO;
    public BigDecimal existencia3 = BigDecimal.ZERO;
}
