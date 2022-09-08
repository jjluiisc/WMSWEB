/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.reder.wms.dao.entity;

import com.atcloud.dao.engine.DatabaseRecord;

/**
 *
 * @author Luis
 */
public class CVES_ALTERDAO implements DatabaseRecord{
    public String CVE_ART = "";
    public String CVE_ALTER = "";
    public String compania = "";
    public String TIPO = "";
    public String CVE_CLPV = "";
        
    public CVES_ALTERDAO() {
    }

    public CVES_ALTERDAO(String compania, String CVE_ART) {
        this.compania = compania;
        this.CVE_ART = CVE_ART;
    }
        
    @Override
    public String getTable() {
        return "CVES_ALTER";
    }

    @Override
    public String getOrder() {
        return "CVE_ART";
    }

    @Override
    public String getWhere() {
        return "CVE_ART = '"+CVE_ART +"' AND compania = '"+compania+"'";
    }

    @Override
    public String toString() {
        return CVE_ART+";"+compania;
    }
}