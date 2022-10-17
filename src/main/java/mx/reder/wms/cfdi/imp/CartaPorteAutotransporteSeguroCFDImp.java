package mx.reder.wms.cfdi.imp;

import mx.reder.wms.cfdi.entity.CartaPorteAutotransporteSeguroCFD;

public class CartaPorteAutotransporteSeguroCFDImp implements CartaPorteAutotransporteSeguroCFD {
    public String aseguraRespCivil;
    public String polizaRespCivil;
    public String aseguraMedAmbiente;
    public String polizaMedAmbiente;
    public String aseguraCarga;
    public String polizaCarga;
    public double primaSeguro;

    @Override
    public String getAseguraRespCivil() {
        return aseguraRespCivil;
    }

    @Override
    public String getPolizaRespCivil() {
        return polizaRespCivil;
    }

    @Override
    public String getAseguraMedAmbiente() {
        return aseguraMedAmbiente;
    }

    @Override
    public String getPolizaMedAmbiente() {
        return polizaMedAmbiente;
    }

    @Override
    public String getAseguraCarga() {
        return aseguraCarga;
    }

    @Override
    public String getPolizaCarga() {
        return polizaCarga;
    }

    @Override
    public double getPrimaSeguro() {
        return primaSeguro;
    }
}
