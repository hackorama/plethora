package com.hackorama.plethora.server.data.jmx;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.hackorama.plethora.common.Log;

final class ConnectionChecker {

    private ConnectionChecker() {
        // no instances
    }

    static boolean isReachable(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            Log.getLogger().fine(host + ":" + port + " is not ready to connect ...");
            return true;
        } catch (UnknownHostException e) {
            Log.getLogger().fine(host + ":" + port + " is unknown host  to connect");
        } catch (IOException e) {
            Log.getLogger().fine(host + ":" + port + " is not ready to connect " + e.getMessage());
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.getLogger().fine(host + ":" + port + " error on closing socket" + e.getMessage());
                }
            }
        }
        return false;
    }

}
