package com.hackorama.plethora.config;

public interface SnmpConfiguration {
    String getSnmpHostname();

    int getSnmpPort();

    String getSnmpEnterpriseSubTreeNumbers();

    String getSnmpEnterpriseSubTreeNames();

    String getSnmpSysDescr();

    String getSnmpSysContact();

    String getSnmpSysName();

    String getSnmpSysLocation();

    MetaConfiguration getMetaConfiguration();

    String getWebRoot();

    String getSnmpMibUrl();

    String getSnmpMibFilePath();

    String getSnmpMibUrlPath();

}
