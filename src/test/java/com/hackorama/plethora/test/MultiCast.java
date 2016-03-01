package com.hackorama.plethora.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class ChatThread extends Thread {

	private final MulticastSocket msocket;
	private DatagramPacket recv;

	public ChatThread(MulticastSocket msock, InetAddress group_no, int Port_no) {
		msocket = msock;
		start(); // start calls run
	}

	@Override
	public void run() {
		byte[] buf = new byte[1000];
		String tmp;
		try {
			for (;;) {
				// Handle the incoming data and print it to stnd output.
				recv = new DatagramPacket(buf, buf.length);
				msocket.receive(recv);
				tmp = new String(recv.getData(), 0, recv.getLength());
				System.out.println("\n\nRecived: \"" + tmp
						+ "\"\nMessage Length is: " + tmp.length());
			}

		} catch (IOException e) {
			System.out.println("Exit...");
			e.printStackTrace();
		} finally {
			msocket.close();
		}
	}

}

public class MultiCast {

	public static void main(String[] args) throws IOException {
		String strin;
		int flag = 1;
		int port;

		if (args.length != 2) {
			System.out.println("Usage: java mchat  mip port");
			System.exit(0);
		}

		port = Integer.parseInt(args[1]); // Convert the Port No. to an integer.

		InetAddress group = InetAddress.getByName(args[0]);
		if (!group.isMulticastAddress()) {
			System.out.println("The address: " + group
					+ " is not multicast address");
			System.exit(0);

		}

		try {

			MulticastSocket s = new MulticastSocket(port);

			s.setReuseAddress(false);
			System.out.println("ReuseAddress is: " + s.getReuseAddress());

			s.setLoopbackMode(false);
			System.out.println("LoopbackMode is: " + s.getLoopbackMode());

			s.setTimeToLive(2);
			System.out.println("TimeToLive is: " + s.getTimeToLive());

			s.joinGroup(group);

			System.out.println("Joined Group: " + args[0] + " Port:" + args[1]);

			// ChatThread will handle the incoming Data and print it out to STDN
			// output.
			new ChatThread(s, group, port);

			// Now read from STDN input and send the Data to the Group.

			BufferedReader myinput = new BufferedReader(new InputStreamReader(
					System.in));

			System.out
			.println("Type anything followed by RETURN, or Ctrl+D to  terminate the program.");
			for (;;) {

				// Read from the STDN input
				strin = myinput.readLine();

				if (strin == null) {
					break; // User Hit Ctrl+D
				}
				DatagramPacket dp = new DatagramPacket(strin.getBytes(),
						strin.length(), group, port);

				s.send(dp);

			}

			System.out.println("Leaving the Group...");
			s.leaveGroup(group);
			s.close();
		} catch (Exception err) {
			System.err.println("ERR: Can not join the group " + err);
			err.printStackTrace();
			System.exit(1);
		}
	}

}
