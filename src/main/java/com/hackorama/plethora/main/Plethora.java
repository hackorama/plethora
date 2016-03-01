package com.hackorama.plethora.main;

import com.hackorama.plethora.config.ConfigProtectException;
import com.hackorama.plethora.config.ConfigProtector;
import com.hackorama.plethora.config.ServerConfiguration;

public final class Plethora {

    private Plethora() {
        // the main container, no public instances
    }

    public static void main(String[] argv) throws SecurityException, ConfigProtectException {
        usage(argv);
        Controller controller = new Controller(new ServerConfiguration(argv, new ConfigProtector()));
        controller.initModules();
        controller.startModuleConnectionService();
        controller.startDataServices();
        controller.startJMXService();
        controller.startWebService();
        controller.startSNMPService();
        controller.reportStatus();
    }

    private static void usage(String[] argv) {
        if (argv.length <= 0) {
            System.out.println("Usage : Plethora <property_file>");
            System.exit(1);
        }
    }

}
