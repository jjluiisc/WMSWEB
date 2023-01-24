package mx.reder.wms.to;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class CorreoFacturaTO implements Serializable {
    public String email = "";
    public String titulo = "";
    public String mensaje = "";
    public ArrayList<File> attachments = new ArrayList<>();
}
