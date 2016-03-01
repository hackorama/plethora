package com.hackorama.plethora.server.snmp;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.server.MetricService;

public final class OidToMetricBridge {

    private final MetricService metricService;
    private final OID vendorSubTreeOid;

    public OidToMetricBridge(OID sysOid, MetricService metricService) {
        this.vendorSubTreeOid = sysOid;
        this.metricService = metricService;
    }

    public Variable getMetricOidVariable(OID oid) {
        int moduleIndex = vendorSubTreeOid.size();
        int metricIndex = moduleIndex + 1;
        int[] intArray = oid.toIntArray();
        if (oid.size() > metricIndex) {
            return objectToVariable(getMetric(intArray[moduleIndex], intArray[metricIndex]));
        } else if (oid.size() > moduleIndex) {
            return objectToVariable(getModule(intArray[moduleIndex]));
        }
        Log.getLogger().warning("SNMP oid " + oid + " not mapped to a metric");
        return Null.noSuchObject;
    }

    public OidValue getFirstMetricOidValue() {
        // Use 0, 0 to get the next 1, 1 which is the first one
        return getNextMetricOidValue(0, 0);
    }

    public OidValue getNextMetricOidValue(int moduleIndex, int metricIndex) {
        Object value = null;
        OID oid = null;
        // try next metric on the module
        if (isValidModuleMetricIndex(moduleIndex, metricIndex + 1)) {
            value = getMetric(moduleIndex, metricIndex + 1);
            oid = buildOid(moduleIndex, metricIndex + 1);
            return new OidValue(oid, objectToVariable(value));
        }
        // if not try to get next module
        value = getModule(moduleIndex + 1);
        if (value != null) {
            oid = buildOid(moduleIndex + 1);
            // TODO better appropriate value for module OID
            return new OidValue(oid, objectToVariable(value));
        }
        return null;
    }

    private boolean isValidModuleMetricIndex(int moduleIndex, int metricIndex) {
        // offset for zero indexing
        int index = metricIndex - 1;
        return index >= 0 && index < getMetricCount(moduleIndex);

    }

    private int getMetricCount(int moduleIndex) {
        // offset for zero indexing
        return metricService.getModuleMetricCount(moduleIndex - 1);
    }

    private Object getMetric(int moduleIndex, int metricIndex) {
        // offset for zero indexing
        return metricService.getMetric(moduleIndex - 1, metricIndex - 1);
    }

    private Object getModule(int moduleIndex) {
        // offset for zero indexing
        return metricService.getModule(moduleIndex - 1);
    }

    private OID buildOid(int moduleIndex) {
        return new OID(vendorSubTreeOid + "." + moduleIndex);
    }

    private OID buildOid(int moduleIndex, int metricIndex) {
        return new OID(vendorSubTreeOid + "." + moduleIndex + "." + metricIndex);
    }

    private Variable objectToVariable(Object value) {
        if (value == null) {
            Log.getLogger().warning("Null value for SNMP variable");
            return Null.noSuchObject;
        }
        String type = value.getClass().getName();
        if (Util.typeIsString(type)) {
            return new OctetString((String) value);
        } else if (Util.typeIsNumber(type)) {
            Number number = (Number) value;
            return new Integer32(number.intValue());
        } else if (Util.typeIsBoolean(type)) {
            Boolean bool = (Boolean) value;
            return new Integer32(bool ? 1 : 0);
        } else {
            Log.getLogger().info("Unsupported SNMP object type " + type);
            return new OctetString("Unsupported object type " + type);
        }
    }

}
