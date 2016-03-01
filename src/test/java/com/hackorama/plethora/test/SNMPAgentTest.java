package com.hackorama.plethora.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.Util;

public class SNMPAgentTest implements CommandResponder {

	private Snmp snmp;
	private final String host;
	private final int port;
	// iso(1).org(3).dod(6).internet(1).mgmt(2).eneterprise(1).hackorama(25).plethora(1).cluster.(1).module(1).metric(1)
	private final String ROOT_OID = "1.3.6.1.2.1";
	private final String HP_ROOT_OID = ROOT_OID + ".25";
	private final String PLETHORA_ROOT_OID = HP_ROOT_OID + ".1";

	private final List<OID> oidList = new ArrayList<OID>();
	private final Map<OID, Object> oidMap = new LinkedHashMap<OID, Object>();

	public SNMPAgentTest(String host, int port) {
		if (!Util.validPort(port)) {
			throw new IllegalArgumentException("port out of range:" + port);
		}
		if (host == null) {
			throw new IllegalArgumentException("hostname can't be null");
		}
		initOid();
		this.host = host;
		this.port = port;
		try {
			snmp = initSnmp(getUdpAddress(host, port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void initOid() {
		oidList.add(new OID(ROOT_OID));
		oidList.add(new OID(HP_ROOT_OID));
		oidList.add(new OID(PLETHORA_ROOT_OID));
		oidList.add(new OID(PLETHORA_ROOT_OID + ".1"));
		oidList.add(new OID(PLETHORA_ROOT_OID + ".1.1"));
		oidList.add(new OID(PLETHORA_ROOT_OID + ".1.2"));

		oidMap.put(new OID(ROOT_OID), "root");
		oidMap.put(new OID(HP_ROOT_OID), "hackorama");
		oidMap.put(new OID(PLETHORA_ROOT_OID), "CLOUDY");
		oidMap.put(new OID(PLETHORA_ROOT_OID + ".1"), "unomodule");
		oidMap.put(new OID(PLETHORA_ROOT_OID + ".1.1"), "unometric");
		oidMap.put(new OID(PLETHORA_ROOT_OID + ".1.2"), "duometric");
	}

	@Override
	public void processPdu(CommandResponderEvent event) {
		if (event == null) {
			Log.getLogger().info("SNMP Invalid event");
			return;
		}
		PDU eventPdu = event.getPDU();
		if (eventPdu == null) {
			System.out.println("invalid event pdu");
			return;
		}
		PDU responsePdu = new PDU(eventPdu);
		responsePdu.setType(PDU.RESPONSE);
		switch (eventPdu.getType()) {
		case PDU.GET:
			handleGET(eventPdu, responsePdu);
		case PDU.GETNEXT:
			handleGETNEXT(eventPdu, responsePdu);
			break;
		case PDU.GETBULK:
			handleGETBULK(eventPdu, responsePdu);
			break;
		default:
			System.out.println("unsupported event type");
			return;
		}

		event.getStateReference().setTransportMapping(
				event.getTransportMapping());
		try {
			event.getMessageDispatcher().returnResponsePdu(
					event.getMessageProcessingModel(),
					event.getSecurityModel(), event.getSecurityName(),
					event.getSecurityLevel(), responsePdu,
					event.getMaxSizeResponsePDU(), event.getStateReference(),
					new StatusInformation());
		} catch (MessageException e) {
			e.printStackTrace();
		}
	}

	public boolean start() {
		if (snmp == null) {
			System.out.println("No valid snmp object");
			return false;
		}
		snmp.addCommandResponder(this);
		try {
			snmp.listen();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("SNMP agent listening " + host + ":" + port + "...");
		return true;
	}

	public void stop() {
		if (snmp != null) {
			try {
				snmp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Snmp initSnmp(UdpAddress udpAddress) {
		try {
			return new Snmp(new DefaultUdpTransportMapping(udpAddress));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private UdpAddress getUdpAddress(String host, int port)
			throws UnknownHostException {
		return new UdpAddress(InetAddress.getByName(host), port);
	}

	private void handleGET(PDU eventPdu, PDU responsePdu) {
		for (VariableBinding binding : responsePdu.toArray()) {
			OID oid = binding.getOid();
			System.out.println("GET " + oid.toString());
			binding.setVariable(objectToVariable(getValueforOID(oid)));
		}
	}

	private Object getValueforOID(OID oid) {
		if (oidMap.containsKey(oid)) {
			return oidMap.get(oid);
		}
		return null;
	}

	private void handleGETNEXT(PDU eventPdu, PDU responsePdu) {
		System.out.println("GET NEXT");
		for (VariableBinding binding : responsePdu.toArray()) {
			OID oid = binding.getOid();
			System.out.println("	GET NEXT " + oid.toString());
			OID nextOid = getNextOid(oid);
			if (nextOid == null) {
				binding.setOid(new OID());
				binding.setVariable(null);
				// binding.setVariable(Null.endOfMibView); //TODO FIXME
				// responsePdu.setErrorStatus();
			} else {
				binding.setOid(nextOid);
				binding.setVariable(objectToVariable(getValueforOID(nextOid)));
			}
		}
	}

	private OID getNextOid(OID oid){
		int index = oidList.lastIndexOf(oid);
		if (index < 0 || index == oidList.size() - 1) {
			return null;
		}
		return oidList.get(index+1);
	}

	private void handleGETBULK(PDU eventPdu, PDU responsePdu) {
		System.out.println("GET BULK");
	}


	public static void main(String[] args) {
		SNMPAgentTest agent = new SNMPAgentTest("localhost", 9997);
		agent.start();
		agent.waitForExit();
	}

	private void waitForExit() {
		try {
			System.out.println("Press any key to exit ...");
			System.in.read();
			System.out.println("SNMP agent stopped");
			stop();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Variable objectToVariable(Object value) {
		if (value == null) {
			return new Null();
		}
		String type = value.getClass().getName();
		if (typeIsString(type)) {
			return new OctetString((String) value);
		} else if (typeIsNumber(type)) {
			Number number = (Number) value;
			return new Counter64(number.longValue());
		} else if (typeIsBoolean(type)) {
			Boolean bool = (Boolean) value;
			return new Integer32(bool?1:0);
		} else {
			return new OctetString("Unsupported object type " + type);
		}
	}

	private boolean typeIsString(String type){
		return type.equals("java.lang.String")
				|| type.equals("java.lang.Character");
	}

	private boolean typeIsNumber(String type) {
		return type.equals("java.lang.Short") || type.equals("java.lang.Short")
				|| type.equals("java.lang.Double")
				|| type.equals("java.lang.Float");
	}
	private boolean typeIsBoolean(String type) {
		return 	type.equals("java.lang.Boolean");
	}

}
