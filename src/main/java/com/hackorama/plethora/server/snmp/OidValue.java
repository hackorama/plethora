package com.hackorama.plethora.server.snmp;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

/**
 * Helper to return an OID and binding variable value for a metric request
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class OidValue {
    private final OID oid;
    private final Variable value;

    public OidValue(OID oid, Variable value) {
        this.oid = oid;
        this.value = value;
    }

    public OID getOid() {
        return oid;
    }

    public Variable getValue() {
        return value;
    }
}
