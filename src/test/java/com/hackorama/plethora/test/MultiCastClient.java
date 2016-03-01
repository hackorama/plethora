package com.hackorama.plethora.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class MultiCastClient {

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
		startClient(ms1, nics.get(0));
	}

	private static void startClient(MulticastSocket chat, NetworkInterface nic)
			throws IOException {
		InetAddress group = InetAddress.getByName("224.0.0.1");
		chat.joinGroup(group);

		String msg = "";
		System.out.println("Type a message for the server:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		msg = br.readLine();
		DatagramPacket data = new DatagramPacket(msg.getBytes(), 0,
				msg.length(), group, 45599);
		chat.send(data);
		chat.close();

	}

	private static void oldClient() throws IOException {
		NetworkInterface networkInterface = NetworkInterface
				.getByName("192.168.0.106");
		MulticastSocket chat = new MulticastSocket(45599);
		chat.setNetworkInterface(networkInterface);
		InetAddress group = InetAddress.getByName("224.0.0.1");

		SocketAddress socketAddress = new InetSocketAddress("224.0.0.1", 45599);

		chat.joinGroup(group);
		// chat.joinGroup(socketAddress, networkInterface);
		String msg = "";
		System.out.println("Type a message for the server:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		msg = br.readLine();
		DatagramPacket data = new DatagramPacket(msg.getBytes(), 0,
				msg.length(), group, 1234);
		chat.send(data);
		chat.close();

	}

	public static void main(String args[]) throws Exception {
		validateNetworkInterfaces();

	}
}
