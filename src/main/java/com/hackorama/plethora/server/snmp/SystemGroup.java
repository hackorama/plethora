package com.hackorama.plethora.server.snmp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.config.SnmpConfiguration;

public final class SystemGroup {

    private static final long START_TIME = System.currentTimeMillis();
    private static final OID SYSTEM_GROUP_OID = new OID("1.3.6.1.2.1.1");
    private static final OID SYS_UPTIME_OID = new OID(SYSTEM_GROUP_OID + ".3.0");
    private final SnmpConfiguration configuration;

    private final Map<OID, Variable> systemGroupMap = new TreeMap<OID, Variable>();
    private final List<OID> systemGroupList = new ArrayList<OID>();

    public SystemGroup(SnmpConfiguration configuration, OID vendorSubTreeOid, String mibUrl) {
        this.configuration = configuration;
        initSysOidDefinitions(vendorSubTreeOid, mibUrl);
    }

    public boolean isSysOID(OID oid) {
        return systemGroupMap.containsKey(oid);
    }

    public boolean hasNext(OID oid) {
        // the list contains the oid and it is not the last item
        return isSysOID(oid) && systemGroupList.indexOf(oid) < systemGroupList.size() - 1;
    }

    public Variable getSysOidVariable(OID oid) {
        if (SYS_UPTIME_OID.equals(oid)) { // sysUptime is dynamic
            return getSysUptime();
        }
        return systemGroupMap.get(oid);
    }

    public OID getNextSysOid(OID oid) {
        if (hasNext(oid)) {
            return systemGroupList.get(systemGroupList.indexOf(oid) + 1);
        }
        return null;
    }

    public Variable getNextSysOidVariable(OID oid) {
        if (!isSysOID(oid)) {
            Log.getLogger().warning("Not a valid SNMP system group oid");
            return null;
        }
        int index = systemGroupList.indexOf(oid);
        if (index < systemGroupList.size() - 1) { // last item cannot have next
            OID nextOid = systemGroupList.get(index + 1);
            if (SYS_UPTIME_OID.equals(nextOid)) { // sysUptime is dynamic
                return getSysUptime();
            }
            return systemGroupMap.get(nextOid);
        }
        Log.getLogger().warning("No more SNMP system group oid to walk");
        return null;
    }

    private void initSysOidDefinitions(OID vendorSubTreeOid, String mibUrl) {
        /* System information As defined by RFC 1213 MIB-II ASN.1 */
        // SysDescr
        systemGroupMap.put(new OID("1.3.6.1.2.1.1.1.0"), new OctetString(getSysDescr(mibUrl)));
        // sysObjectID
        systemGroupMap.put(new OID("1.3.6.1.2.1.1.2.0"), vendorSubTreeOid);
        // sysUpTime
        systemGroupMap.put(new OID("1.3.6.1.2.1.1.3.0"), new TimeTicks(0));
        // sysContact
        systemGroupMap.put(new OID("1.3.6.1.2.1.1.4.0"), new OctetString(configuration.getSnmpSysContact()));
        // sysName
        systemGroupMap.put(new OID("1.3.6.1.2.1.1.5.0"), new OctetString(getSysName()));
        // sysLocation
        systemGroupMap.put(new OID("1.3.6.1.2.1.1.6.0"), new OctetString(configuration.getSnmpSysLocation()));
        // sysServices 2^7-1
        systemGroupMap.put(new OID("1.3.6.1.2.1.1.7.0"), new Integer32(64));
        // ifNumber ( SNMP MIB-2 Interfaces )
        systemGroupMap.put(new OID(" 1.3.6.1.2.1.2.1.0"), new Integer32(1));
        systemGroupList.addAll(systemGroupMap.keySet());
    }

    private String getSysDescr(String mibUrl) {
        if (mibUrl == null) {
            return configuration.getSnmpSysDescr();
        }
        return configuration.getSnmpSysDescr() + " MIB file URL : " + mibUrl;
    }

    private TimeTicks getSysUptime() {
        /*
         * MIB-II sysUpTime: "The time (in hundredths of a second) since the network management portion of the system
         * was last re-initialized."
         */
        return new TimeTicks((System.currentTimeMillis() - START_TIME) / 100);
    }

    private String getSysName() {
        /*
         * MIB-II sysName : "An administratively-assigned name for this managed node. By convention, this is the node's
         * fully-qualified domain name. If the name is unknown, the value is the zero-length string."
         */
        String sysName = configuration.getSnmpSysName();
        if (sysName == null) { // no name provided
            try {
                // use the hostanme
                sysName = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                Log.getLogger().log(Level.WARNING, "Could not get the domain name for SNMP sysName", e);
                sysName = ""; // zero length string
            }
        }
        return sysName;
    }
}
