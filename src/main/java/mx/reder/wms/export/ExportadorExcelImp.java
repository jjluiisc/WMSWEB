package mx.reder.wms.export;

import com.atcloud.dao.engine.DatabaseRecordEntity;
import com.atcloud.dao.engine.DatabaseServices;
import com.atcloud.servlets.ExportadorClass;
import com.atcloud.util.Numero;
import com.atcloud.web.WebException;
import java.awt.Color;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx.reder.wms.dao.entity.InventarioConteoDAO;
import mx.reder.wms.dao.entity.InventarioConteoUbicacionDAO;
import mx.reder.wms.dao.entity.InventarioDAO;
import mx.reder.wms.dao.entity.InventarioDetalleDAO;
import mx.reder.wms.util.Constantes;
import java.util.HashMap;
import java.util.Map;
import mx.reder.wms.collection.CartasPorteCollection;
import mx.reder.wms.collection.OrdenesSurtidoPedidoCollection;
import mx.reder.wms.dao.entity.ClaveAlternaDAO;
import mx.reder.wms.to.InventarioConteoUbicacionAcumuladoTo;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportadorExcelImp implements ExportadorClass {
    static Logger log = Logger.getLogger(ExportadorExcelImp.class);
    private DatabaseServices ds = null;
    private DatabaseServices dsASPEL = null;

    @Override
    public void setConnection(Connection connection) {
    }

    @Override
    public void setDatabaseServices(DatabaseServices databaseServices) {
        this.ds = databaseServices;
    }

    public void setDatabaseServicesASPEL(DatabaseServices databaseServicesASPEL) {
        this.dsASPEL = databaseServicesASPEL;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
    }

    public void exportaCertificacionSurtidoTiempos(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"CertificacionSurtidoTiempos.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String fechainicial = request.getParameter("fechainicial");
        String fechafinal = request.getParameter("fechafinal");
        String pedido = request.getParameter("pedido");
        String cliente = request.getParameter("cliente");

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("certificacionsurtido");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        String[] headers = {
            "Surtido", "Pedido", "No.Cliente", "Cliente", "Surtidas", "Certificadas", "Usuario",
            "Inicio", "Fin", "Tiempo (seg)", "T.Promedio (piezas/seg)"
        };

        int rowNo = 0;

        row = sheet.createRow(rowNo++);

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        XSSFDataFormat df = wb.createDataFormat();

        XSSFCellStyle monedaStyle = wb.createCellStyle();
        monedaStyle.setDataFormat(df.getFormat("$#,##0.00;[Red]$-#,##0.00"));

        XSSFCellStyle numeroStyle = wb.createCellStyle();
        numeroStyle.setDataFormat(df.getFormat("#,##0;[Red]-#,##0"));

        XSSFCellStyle decimalStyle = wb.createCellStyle();
        decimalStyle.setDataFormat(df.getFormat("#,##0.00"));

        XSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(df.getFormat("d/m/yy"));

        XSSFCellStyle datetimeStyle = wb.createCellStyle();
        datetimeStyle.setDataFormat(df.getFormat("d/m/yy h:mm:ss"));

        XSSFCellStyle timeStyle = wb.createCellStyle();
        timeStyle.setDataFormat(df.getFormat("h:mm:ss"));

        StringBuilder whereOrdenSurtido = new StringBuilder();
        whereOrdenSurtido.append("osp.compania = '").append(compania).append("' AND osp.status != 'CA' ");
        if (fechainicial!=null)
            whereOrdenSurtido.append("AND osp.fecha >= '").append(fechainicial).append("' ");
        if (fechafinal!=null)
            whereOrdenSurtido.append("AND osp.fecha <= '").append(fechafinal).append(" 23:59:59' ");
        if (pedido!=null)
            whereOrdenSurtido.append("AND osp.pedido LIKE '%").append(pedido).append("%' ");
        if (cliente!=null)
            whereOrdenSurtido.append("AND osp.cliente LIKE '%").append(cliente).append("%' ");

        String status;
        OrdenesSurtidoPedidoCollection ordenesSurtidoPedidoCollection = new OrdenesSurtidoPedidoCollection();
        ArrayList<OrdenesSurtidoPedidoCollection> ordenessurtido = ds.collection(new OrdenesSurtidoPedidoCollection(),
                ordenesSurtidoPedidoCollection.getSQL(whereOrdenSurtido.toString()));

        for(OrdenesSurtidoPedidoCollection ordenesSurtido : ordenessurtido) {
            double tiempoSurtidoSegundos = ordenesSurtido.fechacertificando==null||ordenesSurtido.fechaconfirmada==null ? 0.0d :
                    (double)(ordenesSurtido.fechaconfirmada.getTime() - ordenesSurtido.fechacertificando.getTime()) / 1000.0d;

            double tiempoPromedio = ordenesSurtido.certificadas==0 ? 0.0d :
                    tiempoSurtidoSegundos / ordenesSurtido.certificadas;

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.flsurtido);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.pedido);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.cliente);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.nombrecliente);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.surtidas);
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.certificadas);
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.usuario);
            cell = row.createCell(colNo++);
            if (ordenesSurtido.fechacertificando!=null) {
                cell.setCellValue(ordenesSurtido.fechacertificando);
                cell.setCellStyle(datetimeStyle);
            }
            cell = row.createCell(colNo++);
            if (ordenesSurtido.fechaconfirmada!=null) {
                cell.setCellValue(ordenesSurtido.fechaconfirmada);
                cell.setCellStyle(datetimeStyle);
            }
            cell = row.createCell(colNo++);
            cell.setCellType(XSSFCell.CELL_TYPE_FORMULA);
            cell.setCellFormula("TIME(0,0,INT("+tiempoSurtidoSegundos+"))");
            cell.setCellStyle(timeStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(tiempoPromedio);
            cell.setCellStyle(decimalStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }

        //
        //
        //

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    public void exportaOrdenesSurtidoPedidoTiempos(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"OrdenesSurtidoPedidoTiempos.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String fechainicial = request.getParameter("fechainicial");
        String fechafinal = request.getParameter("fechafinal");
        String pedido = request.getParameter("pedido");
        String cliente = request.getParameter("cliente");

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("ordenessurtido");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        String[] headers = {
            "Surtido", "Pedido", "No.Cliente", "Cliente", "Cantidad", "Surtidas", "Equipo", "Surtidor",
            "Inicio", "Fin", "Tiempo (seg)", "T.Promedio (piezas/seg)"
        };

        int rowNo = 0;

        row = sheet.createRow(rowNo++);

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        XSSFDataFormat df = wb.createDataFormat();

        XSSFCellStyle monedaStyle = wb.createCellStyle();
        monedaStyle.setDataFormat(df.getFormat("$#,##0.00;[Red]$-#,##0.00"));

        XSSFCellStyle numeroStyle = wb.createCellStyle();
        numeroStyle.setDataFormat(df.getFormat("#,##0;[Red]-#,##0"));

        XSSFCellStyle decimalStyle = wb.createCellStyle();
        decimalStyle.setDataFormat(df.getFormat("#,##0.00"));

        XSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(df.getFormat("d/m/yy"));

        XSSFCellStyle datetimeStyle = wb.createCellStyle();
        datetimeStyle.setDataFormat(df.getFormat("d/m/yy h:mm:ss"));

        XSSFCellStyle timeStyle = wb.createCellStyle();
        timeStyle.setDataFormat(df.getFormat("h:mm:ss"));

        StringBuilder whereOrdenSurtido = new StringBuilder();
        whereOrdenSurtido.append("osp.compania = '").append(compania).append("' AND osp.status != 'CA' ");
        if (fechainicial!=null)
            whereOrdenSurtido.append("AND osp.fecha >= '").append(fechainicial).append("' ");
        if (fechafinal!=null)
            whereOrdenSurtido.append("AND osp.fecha <= '").append(fechafinal).append(" 23:59:59' ");
        if (pedido!=null)
            whereOrdenSurtido.append("AND osp.pedido LIKE '%").append(pedido).append("%' ");
        if (cliente!=null)
            whereOrdenSurtido.append("AND osp.cliente LIKE '%").append(cliente).append("%' ");

        String status;
        OrdenesSurtidoPedidoCollection ordenesSurtidoPedidoCollection = new OrdenesSurtidoPedidoCollection();
        ArrayList<OrdenesSurtidoPedidoCollection> ordenessurtido = ds.collection(new OrdenesSurtidoPedidoCollection(),
                ordenesSurtidoPedidoCollection.getSQL(whereOrdenSurtido.toString()));

        for(OrdenesSurtidoPedidoCollection ordenesSurtido : ordenessurtido) {
            double tiempoSurtidoSegundos = ordenesSurtido.fechainicio==null||ordenesSurtido.fechatermino==null ? 0.0d :
                    (double)(ordenesSurtido.fechatermino.getTime() - ordenesSurtido.fechainicio.getTime()) / 1000.0d;

            double tiempoPromedio = ordenesSurtido.surtidas==0 ? 0.0d :
                    tiempoSurtidoSegundos / ordenesSurtido.surtidas;

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.flsurtido);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.pedido);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.cliente);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.nombrecliente);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.cantidad);
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.surtidas);
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.equipo);
            cell = row.createCell(colNo++);
            cell.setCellValue(ordenesSurtido.surtidor);
            cell = row.createCell(colNo++);
            if (ordenesSurtido.fechainicio!=null) {
                cell.setCellValue(ordenesSurtido.fechainicio);
                cell.setCellStyle(datetimeStyle);
            }
            cell = row.createCell(colNo++);
            if (ordenesSurtido.fechatermino!=null) {
                cell.setCellValue(ordenesSurtido.fechatermino);
                cell.setCellStyle(datetimeStyle);
            }
            cell = row.createCell(colNo++);
            cell.setCellType(XSSFCell.CELL_TYPE_FORMULA);
            cell.setCellFormula("TIME(0,0,INT("+tiempoSurtidoSegundos+"))");
            cell.setCellStyle(timeStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(tiempoPromedio);
            cell.setCellStyle(decimalStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }

        //
        //
        //

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    public void exportaClavesAlternas(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"QR.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String clave = request.getParameter("clave");

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("qr");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        row = sheet.createRow(0);
        String[] headers = {
            "Clave", "Codigo"
        };

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        int rowNo = 1;

        StringBuilder where = new StringBuilder();
        where.append("compania = '").append(compania).append("' ");
        if (clave!=null&&!clave.isEmpty())
            where.append("AND clave = '").append(clave).append("' ");

        ArrayList productos = ds.select(new ClaveAlternaDAO(), where.toString(), "clave");

        for(Object object : productos) {
            ClaveAlternaDAO claveAlternaDAO = (ClaveAlternaDAO)object;

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(claveAlternaDAO.clave);
            cell = row.createCell(colNo++);
            cell.setCellValue(claveAlternaDAO.codigo);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    public void exportaDiferenciasInventarioConteo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"DiferenciasInventarioConteo.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String flinventario = request.getParameter("flinventario");
        int conteo = Numero.getIntFromString(request.getParameter("conteo"));

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("diferencias");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        row = sheet.createRow(0);
        String[] headers = {
            "Clave", "Descripcion", "Labortorio", "Existencia", "Conteo 1", "Conteo 2", "Conteo 3"
        };

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        int rowNo = 1;

        XSSFCellStyle monedaStyle = wb.createCellStyle();
        XSSFDataFormat dfMoneda = wb.createDataFormat();
        monedaStyle.setDataFormat(dfMoneda.getFormat("$#,##0.00;[Red]($#,##0.00)"));

        XSSFCellStyle numeroStyle = wb.createCellStyle();
        XSSFDataFormat dfNumero = wb.createDataFormat();
        numeroStyle.setDataFormat(dfMoneda.getFormat("#,##0.00;[Red](#,##0.00)"));

        ArrayList<DatabaseRecordEntity> arrayubicaciones = ds.collection(
                "SELECT iu.flinventario, iu.codigo, ("
                        +"  SELECT dist.ubicacion+' '"
                        +"  FROM (SELECT t.codigo, t.ubicacion FROM dbo.InventarioConteoUbicacion t "
                        +"  WHERE t.flinventario = iu.flinventario AND t.codigo = iu.codigo"
                        +"  GROUP BY t.codigo, t.ubicacion) dist "
                        +"  FOR XML PATH ('')) AS ubicaciones "
                        +"FROM InventarioConteoUbicacion iu "
                        +"WHERE iu.flinventario = "+flinventario+" "
                        +"GROUP BY iu.flinventario, iu.codigo;");
        HashMap<String, String> ubicaciones = new HashMap<>();
        for(DatabaseRecordEntity record : arrayubicaciones) {
            ubicaciones.put(record.getString("codigo"), record.getString("ubicaciones"));
        }
        
        // ArrayList productos = ds.select(new InventarioConteoDAO(), "compania = '"+compania+"' AND flinventario = "+flinventario, "descripcion");
        // Obtener productos
        ArrayList<DatabaseRecordEntity> arrayproductos = ds.collection(
                "SELECT ic.codigo, ic.descripcion, ISNULL(ic.laboratorio,'') AS laboratorio, ic.existencia, ic.existencia1, "+
                "ic.existencia2, ic.existencia3 "+
                "FROM InventarioConteo ic " +
                "WHERE ic.flinventario = " + flinventario + " AND ic.compania = '"+compania+"' "+
                "ORDER BY ic.descripcion ");       
        
        for(DatabaseRecordEntity record : arrayproductos) {            

            BigDecimal existencia = BigDecimal.ZERO;
            existencia = record.getBigDecimal("existencia");

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(record.getString("codigo"));
            cell = row.createCell(colNo++);
            cell.setCellValue(record.getString("descripcion"));
            cell = row.createCell(colNo++);
            cell.setCellValue(record.getString("laboratorio"));
            cell = row.createCell(colNo++);
            cell.setCellValue(existencia.doubleValue());
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(record.getBigDecimal("existencia1").doubleValue());
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(record.getBigDecimal("existencia2").doubleValue());
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(record.getBigDecimal("existencia3").doubleValue());
            cell.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }
        
        ////// Conteo 1 /////
        //
        //
        XSSFSheet sheet1 = wb.createSheet("conteo 1");
        sheet1.setSelected(true);

        XSSFRow row1;
        XSSFCell cell1;

        row1 = sheet1.createRow(0);
        headers = new String[] {
             "Clave", "Descripcion", "Laboratorio", "Ubicaciones", "Existencia", "Conteo", "Diferencia"
        };

        colNo = 0;
        for(String header : headers) {
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(header);
            cell1.setCellStyle(headerStyle);

            sheet1.setColumnWidth(0, (header.length()*1 * 450));
        }
        
        rowNo = 1;
        
        // obtiene conteo 1
        for(DatabaseRecordEntity record : arrayproductos) {

            BigDecimal existencia = BigDecimal.ZERO;
            BigDecimal conteoExistencia = BigDecimal.ZERO;
            
            existencia = record.getBigDecimal("existencia");
            conteoExistencia = record.getBigDecimal("existencia1");
            BigDecimal diferenciaExistencia = conteoExistencia.subtract(existencia);

            row1 = sheet1.createRow(rowNo);
            colNo = 0;
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(record.getString("codigo"));
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(record.getString("descripcion"));
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(record.getString("laboratorio"));
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(ubicaciones.get(record.getString("codigo")));
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(existencia.doubleValue());
            cell1.setCellStyle(numeroStyle);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(conteoExistencia.doubleValue());
            cell1.setCellStyle(numeroStyle);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(diferenciaExistencia.doubleValue());
            cell1.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet1.autoSizeColumn(colNo++);
        }
        //
        //
        //////
        
        ////// Conteo 2 /////
        //
        //
        XSSFSheet sheet2 = wb.createSheet("conteo 2");
        sheet2.setSelected(true);

        XSSFRow row2;
        XSSFCell cell2;

        row2 = sheet2.createRow(0);
        headers = new String[] {
             "Clave", "Descripcion", "Laboratorio", "Ubicaciones", "Existencia", "Conteo", "Diferencia"
        };

        colNo = 0;
        for(String header : headers) {
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(header);
            cell2.setCellStyle(headerStyle);

            sheet2.setColumnWidth(0, (header.length()*1 * 450));
        }
        
        rowNo = 1;
        
        // obtiene conteo 1
        for(DatabaseRecordEntity record : arrayproductos) {

            BigDecimal existencia = BigDecimal.ZERO;
            BigDecimal conteoExistencia = BigDecimal.ZERO;
            
            existencia = record.getBigDecimal("existencia");
            conteoExistencia = record.getBigDecimal("existencia2");
            BigDecimal diferenciaExistencia = conteoExistencia.subtract(existencia);

            row2 = sheet2.createRow(rowNo);
            colNo = 0;
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(record.getString("codigo"));
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(record.getString("descripcion"));
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(record.getString("laboratorio"));
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(ubicaciones.get(record.getString("codigo")));
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(existencia.doubleValue());
            cell2.setCellStyle(numeroStyle);
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(conteoExistencia.doubleValue());
            cell2.setCellStyle(numeroStyle);
            cell2 = row2.createCell(colNo++);
            cell2.setCellValue(diferenciaExistencia.doubleValue());
            cell2.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet2.autoSizeColumn(colNo++);
        }
        //
        //
        //////
        
        ////// Conteo 3 /////
        //
        //
        XSSFSheet sheet3 = wb.createSheet("conteo 3");
        sheet3.setSelected(true);

        XSSFRow row3;
        XSSFCell cell3;

        row3 = sheet3.createRow(0);
        headers = new String[] {
             "Clave", "Descripcion", "Laboratorio", "Ubicaciones", "Existencia", "Conteo", "Diferencia"
        };

        colNo = 0;
        for(String header : headers) {
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(header);
            cell3.setCellStyle(headerStyle);

            sheet3.setColumnWidth(0, (header.length()*1 * 450));
        }
        
        rowNo = 1;
        
        // obtiene conteo 1
        for(DatabaseRecordEntity record : arrayproductos) {

            BigDecimal existencia = BigDecimal.ZERO;
            BigDecimal conteoExistencia = BigDecimal.ZERO;
            
            existencia = record.getBigDecimal("existencia");
            conteoExistencia = record.getBigDecimal("existencia3");
            BigDecimal diferenciaExistencia = conteoExistencia.subtract(existencia);

            row3 = sheet3.createRow(rowNo);
            colNo = 0;
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(record.getString("codigo"));
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(record.getString("descripcion"));
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(record.getString("laboratorio"));
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(ubicaciones.get(record.getString("codigo")));
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(existencia.doubleValue());
            cell3.setCellStyle(numeroStyle);
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(conteoExistencia.doubleValue());
            cell3.setCellStyle(numeroStyle);
            cell3 = row3.createCell(colNo++);
            cell3.setCellValue(diferenciaExistencia.doubleValue());
            cell3.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet3.autoSizeColumn(colNo++);
        }
        //
        //
        //////

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    public void exportaValuacionInventarioConteo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"ValuacionInventarioConteo.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String flinventario = request.getParameter("flinventario");
        int conteo = Numero.getIntFromString(request.getParameter("conteo"));

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("valuacion");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        row = sheet.createRow(0);
        String[] headers = {
            "Clave", "Descripcion", "Ubicaciones", "Existencia"
        };

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        int rowNo = 1;

        XSSFCellStyle monedaStyle = wb.createCellStyle();
        XSSFDataFormat dfMoneda = wb.createDataFormat();
        monedaStyle.setDataFormat(dfMoneda.getFormat("$#,##0.00"));

        XSSFCellStyle numeroStyle = wb.createCellStyle();
        XSSFDataFormat dfNumero = wb.createDataFormat();
        numeroStyle.setDataFormat(dfNumero.getFormat("#,##0.00"));

        ArrayList<DatabaseRecordEntity> arrayubicaciones = ds.collection(
                "SELECT iu.flinventario, iu.codigo, ("
                        +"  SELECT dist.ubicacion+' '"
                        +"  FROM (SELECT t.codigo, t.ubicacion FROM dbo.InventarioConteoUbicacion t "
                        +"  WHERE t.flinventario = iu.flinventario AND t.codigo = iu.codigo"
                        +"  GROUP BY t.codigo, t.ubicacion) dist "
                        +"  FOR XML PATH ('')) AS ubicaciones "
                        +"FROM InventarioConteoUbicacion iu "
                        +"WHERE iu.flinventario = "+flinventario+" "
                        +"GROUP BY iu.flinventario, iu.codigo;");
        HashMap<String, String> ubicaciones = new HashMap<>();
        for(DatabaseRecordEntity record : arrayubicaciones) {
            ubicaciones.put(record.getString("codigo"), record.getString("ubicaciones"));
        }

        ArrayList productos = ds.select(new InventarioConteoDAO(), "compania = '"+compania+"' AND flinventario = "+flinventario, "descripcion");

        for(Object object : productos) {
            InventarioConteoDAO inventarioConteoDAO = (InventarioConteoDAO)object;

            BigDecimal existencia = BigDecimal.ZERO;
            switch(conteo) {
                case 1:
                    existencia = inventarioConteoDAO.existencia1;
                    break;
                case 2:
                    existencia = inventarioConteoDAO.existencia2;
                    break;
                case 3:
                    existencia = inventarioConteoDAO.existencia3;
                    break;
            }
            BigDecimal valor = existencia.multiply(inventarioConteoDAO.costo);

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoDAO.codigo);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoDAO.descripcion);
            cell = row.createCell(colNo++);
            cell.setCellValue(ubicaciones.get(inventarioConteoDAO.codigo));
            cell = row.createCell(colNo++);
            cell.setCellValue(existencia.doubleValue());
            cell.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    public void exportaValuacionInventario(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"ValuacionInventario.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String flinventario = request.getParameter("flinventario");

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("valuacion");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        row = sheet.createRow(0);
        String[] headers = {
            "Clave", "Descripcion", "Ubicacion", "Existencia", "Precio", "Valor", "Conteo1", "Vluacion1", "Conteo2", "Vluacion2", "Conteo3", "Vluacion3"
        };

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        int rowNo = 1;

        XSSFCellStyle monedaStyle = wb.createCellStyle();
        XSSFDataFormat dfMoneda = wb.createDataFormat();
        monedaStyle.setDataFormat(dfMoneda.getFormat("$#,##0.00"));

        XSSFCellStyle numeroStyle = wb.createCellStyle();
        XSSFDataFormat dfNumero = wb.createDataFormat();
        numeroStyle.setDataFormat(dfNumero.getFormat("#,##0.00"));

        ArrayList productos = ds.select(new InventarioDetalleDAO(), "compania = '"+compania+"' AND flinventario = "+flinventario, "descripcion");

        for(Object object : productos) {
            InventarioDetalleDAO inventarioDetalleDAO = (InventarioDetalleDAO)object;

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioDetalleDAO.codigo);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioDetalleDAO.descripcion);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioDetalleDAO.existencia.doubleValue());
            cell.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    public void exportaArchivoInventario(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"Inventario.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String flinventario = request.getParameter("flinventario");

        //
        InventarioDAO inventarioDAO = new InventarioDAO();
        inventarioDAO.compania = compania;
        inventarioDAO.flinventario = Integer.parseInt(flinventario);
        if (!ds.exists(inventarioDAO))
            throw new WebException("No existe este inventario ["+inventarioDAO+"].");

        if (inventarioDAO.fase==null)
            throw new WebException("No se ha realizado la Carga del Inventario ["+Constantes.ESTADO_INVENTARIO_CARGAINICIAL+"].");
        if (!(inventarioDAO.fase.compareTo(Constantes.ESTADO_INVENTARIO_AFECTADO)==0))
            throw new WebException("Solo se puede Finalizar el Inventario con el estado ["+Constantes.ESTADO_INVENTARIO_AFECTADO+"].");
        //

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("inventario");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        row = sheet.createRow(0);
        String[] headers = {
            "Codigo", "Descripcion", "Laboratorio", "Ubicaciones", "Lotes", "Conteo 1", "Conteo 2", "Conteo 3"
        };

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        int rowNo = 1;

        DecimalFormat df = new DecimalFormat("#0.00");

        DataFormat fmt = wb.createDataFormat();
        CellStyle textCellStyle = wb.createCellStyle();
        textCellStyle.setDataFormat(fmt.getFormat("@"));

        XSSFCellStyle numeroStyle = wb.createCellStyle();
        XSSFDataFormat dfNumero = wb.createDataFormat();
        numeroStyle.setDataFormat(dfNumero.getFormat("#,##0.00"));

        XSSFDataFormat xdf = wb.createDataFormat();
        XSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(xdf.getFormat("yyyy-mm-dd"));

        // Obtengo las Ubicaciones
        ArrayList<DatabaseRecordEntity> arrayubicaciones = ds.collection(
                "SELECT iu.flinventario, iu.codigo, ("
                        +"  SELECT dist.ubicacion+' '"
                        +"  FROM (SELECT t.codigo, t.ubicacion FROM dbo.InventarioConteoUbicacion t "
                        +"  WHERE t.flinventario = iu.flinventario AND t.codigo = iu.codigo"
                        +"  GROUP BY t.codigo, t.ubicacion) dist "
                        +"  FOR XML PATH ('')) AS ubicaciones "
                        +"FROM InventarioConteoUbicacion iu "
                        +"WHERE iu.flinventario = "+flinventario+" "
                        +"GROUP BY iu.flinventario, iu.codigo;");
        HashMap<String, String> ubicaciones = new HashMap<>();
        for(DatabaseRecordEntity record : arrayubicaciones) {
            ubicaciones.put(record.getString("codigo"), record.getString("ubicaciones"));
        }
        
        ArrayList<DatabaseRecordEntity> arraycodigolaboratorio = ds.collection(
                "SELECT icu.codigo, icu.descripcion, ic.laboratorio, icu.ubicacion, icu.lote, icu.existencia1, icu.existencia2, icu.existencia3 " 
                        + "FROM InventarioConteoUbicacion icu " 
                        + "INNER JOIN InventarioConteo ic ON icu.fldinventario = ic.fldinventario " 
                        + "WHERE icu.compania = '"+compania+"' AND icu.flinventario = " + flinventario);
        HashMap<String, InventarioConteoUbicacionAcumuladoTo> acumulados = new HashMap<>();
        for(DatabaseRecordEntity record : arraycodigolaboratorio) {
            String key = record.getString("codigo")+"_"+record.getString("laboratorio");
            InventarioConteoUbicacionAcumuladoTo buscar = acumulados.get(key);
            if(buscar == null){
                InventarioConteoUbicacionAcumuladoTo inventarioConteoUbicacionAcumuladoTo = new InventarioConteoUbicacionAcumuladoTo();
                inventarioConteoUbicacionAcumuladoTo.codigo = record.getString("codigo");
                inventarioConteoUbicacionAcumuladoTo.descripcion = record.getString("descripcion");
                inventarioConteoUbicacionAcumuladoTo.laboratorio = record.getString("laboratorio");
                inventarioConteoUbicacionAcumuladoTo.ubicaciones = record.getString("ubicacion");
                inventarioConteoUbicacionAcumuladoTo.lotes = record.getString("lote");
                inventarioConteoUbicacionAcumuladoTo.existencia1 = record.getBigDecimal("existencia1");
                inventarioConteoUbicacionAcumuladoTo.existencia2 = record.getBigDecimal("existencia2");
                inventarioConteoUbicacionAcumuladoTo.existencia3 = record.getBigDecimal("existencia3");
                acumulados.put(key, inventarioConteoUbicacionAcumuladoTo);
            }else{
                buscar.ubicaciones = buscar.ubicaciones + " " + record.getString("ubicacion");
                buscar.lotes = buscar.lotes + " " + record.getString("lote");
                buscar.existencia1 = buscar.existencia1.add(record.getBigDecimal("existencia1"));
                buscar.existencia2 = buscar.existencia2.add(record.getBigDecimal("existencia2"));
                buscar.existencia3 = buscar.existencia3.add(record.getBigDecimal("existencia3"));
                acumulados.put(key, buscar);
            }
        }

        // Obtengo los registros de inventario
        // ArrayList<InventarioConteoDAO> array = ds.select(new InventarioConteoDAO(), "compania = '"+compania+"' AND flinventario = "+flinventario);

        /*for(InventarioConteoDAO inventarioConteoDAO : array) {
            if (inventarioConteoDAO.existencia3.doubleValue()==0.0)
                continue;

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoDAO.codigo);
            cell = row.createCell(colNo++);
            cell.setCellValue(ubicaciones.get(inventarioConteoDAO.codigo));
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoDAO.existencia3.doubleValue());
            cell.setCellStyle(numeroStyle);

            rowNo++;
        }*/
        
        for(Map.Entry<String,InventarioConteoUbicacionAcumuladoTo> entry : acumulados.entrySet()) {
            
            InventarioConteoUbicacionAcumuladoTo  inventarioConteoUbicacionAcumuladoTo = entry.getValue();

            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.codigo);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.descripcion);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.laboratorio);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.ubicaciones);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.lotes);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.existencia1.doubleValue());
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.existencia2.doubleValue());
            cell.setCellStyle(numeroStyle);
            cell = row.createCell(colNo++);
            cell.setCellValue(inventarioConteoUbicacionAcumuladoTo.existencia3.doubleValue());
            cell.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }

        //
        //
        //

        XSSFSheet sheet1 = wb.createSheet("ubicaciones");
        sheet1.setSelected(true);

        XSSFRow row1;
        XSSFCell cell1;

        row1 = sheet1.createRow(0);
        headers = new String[] {
            "Codigo", "Ubicacion", "Lote", "Caducidad", "Conteo 1", "Conteo 2", "Conteo 3"
        };

        colNo = 0;
        for(String header : headers) {
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(header);
            cell1.setCellStyle(headerStyle);

            sheet1.setColumnWidth(0, (header.length()*1 * 450));
        }

        rowNo = 1;

        // Obtengo los registros de inventario por ubicacion
        ArrayList<InventarioConteoUbicacionDAO> arrayU = ds.select(new InventarioConteoUbicacionDAO(), "compania = '"+compania+"' AND flinventario = "+flinventario);

        for(InventarioConteoUbicacionDAO inventarioConteoUbicacionDAO : arrayU) {
            row1 = sheet1.createRow(rowNo);
            colNo = 0;
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(inventarioConteoUbicacionDAO.codigo);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(inventarioConteoUbicacionDAO.ubicacion);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(inventarioConteoUbicacionDAO.lote);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(inventarioConteoUbicacionDAO.fecaducidad);
            cell1.setCellStyle(dateStyle);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(inventarioConteoUbicacionDAO.existencia1.doubleValue());
            cell1.setCellStyle(numeroStyle);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(inventarioConteoUbicacionDAO.existencia2.doubleValue());
            cell1.setCellStyle(numeroStyle);
            cell1 = row1.createCell(colNo++);
            cell1.setCellValue(inventarioConteoUbicacionDAO.existencia3.doubleValue());
            cell1.setCellStyle(numeroStyle);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet1.autoSizeColumn(colNo++);
        }

        //
        //
        //

        try (ServletOutputStream out = response.getOutputStream()) {
            wb.write(out);
        }
    }
    
    public void exportaCartaPorte(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Disposition", "attachment;filename=\"CartaPorte.xlsx\"");
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String compania = request.getParameter("compania");
        String fechainicial = request.getParameter("fechainicial");
        String fechafinal = request.getParameter("fechafinal");

        XSSFWorkbook wb = new XSSFWorkbook();

        XSSFSheet sheet = wb.createSheet("cartas porte");
        sheet.setSelected(true);

        XSSFRow row;
        XSSFCell cell;

        XSSFCellStyle headerStyle = wb.createCellStyle();
        XSSFFont fontHeader = wb.createFont();
        fontHeader.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        fontHeader.setColor(new XSSFColor(Color.white));
        headerStyle.setFont(fontHeader);
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(Color.lightGray));

        row = sheet.createRow(0);
        String[] headers = {
            "Carta Porte", "Fecha Timbre", "UUID", "Factura"
        };

        int colNo = 0;
        for(String header : headers) {
            cell = row.createCell(colNo++);
            cell.setCellValue(header);
            cell.setCellStyle(headerStyle);

            sheet.setColumnWidth(0, (header.length()*1 * 450));
        }

        int rowNo = 1;

        XSSFDataFormat df = wb.createDataFormat();
        
        XSSFCellStyle datetimeStyle = wb.createCellStyle();
        datetimeStyle.setDataFormat(df.getFormat("d/m/yy h:mm:ss"));
        
        StringBuilder whereCartaPorte = new StringBuilder();
        whereCartaPorte.append("CPF.compania = '").append(compania).append("' ");
        if (fechainicial!=null)
            whereCartaPorte.append("AND CPCFDI.fechatimbre >= '").append(fechainicial).append("' ");
        if (fechafinal!=null)
            whereCartaPorte.append("AND CPCFDI.fechatimbre <= '").append(fechafinal).append(" 23:59:59' ");
        
        CartasPorteCollection cartasPorteCollection = new CartasPorteCollection();
        ArrayList<CartasPorteCollection> cartasporte = ds.collection(new CartasPorteCollection(),
                cartasPorteCollection.getSQL(whereCartaPorte.toString()));
        
        for(CartasPorteCollection cartaPorte : cartasporte) {
            row = sheet.createRow(rowNo);
            colNo = 0;
            cell = row.createCell(colNo++);
            cell.setCellValue(cartaPorte.idcartaporte);
            cell = row.createCell(colNo++);
            if (cartaPorte.fechatimbre!=null) {
                cell.setCellValue(cartaPorte.fechatimbre);
                cell.setCellStyle(datetimeStyle);
            }
            cell = row.createCell(colNo++);
            cell.setCellValue(cartaPorte.uuid);
            cell = row.createCell(colNo++);
            cell.setCellValue(cartaPorte.factura);

            rowNo++;
        }

        colNo = 0;
        for(String header : headers) {
            sheet.autoSizeColumn(colNo++);
        }

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }
}
