package org.apache.hyracks.prometheus;


import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;


public class Process {
    public static void main(String[] args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("PROMETHEUS VERSION");
                DefaultExports.initialize();
                try {
                    HTTPServer server = new HTTPServer(1234);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
