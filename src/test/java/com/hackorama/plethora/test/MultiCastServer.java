package com.hackorama.plethora.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MultiCastServer {

	private static void validateNetworkInterfaces() throws IOException {

		Enumeration nis = NetworkInterface.getNetworkInterfaces();
		List<NetworkInterface> nics = new ArrayList<NetworkInterface>();
		int count = 0;
		while (nis.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) nis.nextElement();

			System.out.println(count++);
			System.out.println("nic name: " + ni.getDisplayName());
			System.out.println("nic isLoopback(): " + ni.isLoopback());
			System.out.println("nic isPointToPoint(): " + ni.isPointToPoint());
			System.out.println("nic isVirtual(): " + ni.isVirtual());
			System.out.println("nic isUp(): " + ni.isUp());
			System.out.println("nic supportsMulticast(): "
					+ ni.supportsMulticast());
			List<InterfaceAddress> ifads = ni.getInterfaceAddresses();
			for (InterfaceAddress ifad : ifads) {
				System.out.println("\t" + ifad.getAddress().getHostAddress());
			}

			System.out.println("-");

			if (!ni.isLoopback() && !ni.isPointToPoint() && !ni.isVirtual()
					&& ni.isUp() && ni.supportsMulticast()) {
				System.out.println("adding nic: " + ni.getDisplayName());
				nics.add(ni);
			}

		}

		// check to make sure at least one network interface was found that
		// supports multicast.
		if (nics.size() == 0) {
			throw new SocketException(
					"No network interfaces were found that support multicast.");
		}

		// make sure the network interface can be set on a multicast socket
		/*
		 * for (NetworkInterface nic : nics) {
		 * System.out.println("attempting to set network interface on nic: " +
		 * nic.getDisplayName()); MulticastSocket ms1 = new
		 * MulticastSocket(45599); ms1.setNetworkInterface(nic); }
		 */

		MulticastSocket ms1 = new MulticastSocket(45599);
		ms1.setNetworkInterface(nics.get(0));
		newStartServer(ms1, nics.get(0));
	}

	private static void newStartServer(MulticastSocket server,
			NetworkInterface nic)
					throws IOException {
		List<InterfaceAddress> ifads = nic.getInterfaceAddresses();
		for (InterfaceAddress ifad : ifads) {
			System.out.println("\t" + ifad.getAddress().getHostAddress());
		}
		InetAddress group = InetAddress.getByName("224.0.0.1");
		server.joinGroup(group);
		boolean infinite = true;
		while (infinite) {
			System.out.print(".");
			byte buf[] = new byte[1024];
			DatagramPacket data = new DatagramPacket(buf, buf.length);
			server.receive(data);
			String msg = new String(data.getData()).trim();
			System.out.println(msg);
		}
		server.close();
	}

	private static void startServer() throws IOException {
		MulticastSocket server = new MulticastSocket(1234);
		InetAddress group = InetAddress.getByName("224.0.0.1");
		// getByName - returns IP address of the given host
		SocketAddress socketAddress = new InetSocketAddress("224.0.0.1", 1234);
		NetworkInterface networkInterface = NetworkInterface
				.getByName("224.0.0.1");
		server.joinGroup(group);
		// server.joinGroup(socketAddress, networkInterface);
		boolean infinite = true;
		/* Server continually receives data and prints them */
		System.out.println("started");
		while (infinite) {
			System.out.print(".");
			byte buf[] = new byte[1024];
			DatagramPacket data = new DatagramPacket(buf, buf.length);
			server.receive(data);
			String msg = new String(data.getData()).trim();
			System.out.println(msg);
		}
		server.close();

	}

	public static void main(String args[]) throws Exception {

		validateNetworkInterfaces();

	}
}