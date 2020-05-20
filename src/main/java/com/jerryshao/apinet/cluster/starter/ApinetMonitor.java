package com.jerryshao.apinet.cluster.starter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.jerryshao.apinet.cluster.Cluster;
import com.jerryshao.apinet.cluster.ClusterSettings;
import com.jerryshao.apinet.cluster.executor.ApTaskExecutor;
import com.jerryshao.apinet.cluster.local.LocalDaemonThread;
import com.jerryshao.apinet.cluster.local.LocalNode;
import com.jerryshao.apinet.server.ServerConfig;

public class ApinetMonitor {
	
	static private LocalNode localNode = null;
	static private LocalDaemonThread localDaemonThread = null;
	static private Cluster cluster = null;
	
	final private static int serverMonitorPort = ServerConfig.getServerMonitorPort();
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("usage [type]");
			System.exit(-1);
		}
		
		if (args[0].equals("cluster")) {//启动cluster
			cluster = new Cluster();
			cluster.start();
			new MonitorThread().start();
		} else if (args[0].equals("local")) {//启动local,以及它的守护进程
			localNode = new LocalNode(new ApTaskExecutor());
			LocalDaemonThread localDaemonThread = new LocalDaemonThread(localNode);
			localNode.start();
			localDaemonThread.start();
			new MonitorThread().start();
		} else {
			System.err.println("error type");
			return ;
		}
	}
	
	private static class MonitorThread extends Thread {
        private ServerSocket socket;

        public MonitorThread() {
            setDaemon(true);
            try {
                socket = new ServerSocket(serverMonitorPort, 1, InetAddress.getByName("0.0.0.0"));
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
        	Socket accept;
        	while (true) {
	            try {
	                accept = socket.accept();
	                BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
	                String command = reader.readLine();
	                
	                if ("stop".equals(command)) {
	                	if (localNode != null && localNode.isStarted()) {
	                		localDaemonThread.shutDown();
	                		localNode.stop();
	                	}
	                	if (cluster != null && cluster.isStarted()) {
	                		cluster.stop();
	                	}
	                	System.out.println("stop");
		                accept.close();
		                socket.close();
		                break;
	                } else if ("ping".equalsIgnoreCase(command)) {
	                	accept.getOutputStream().write("PONG\r\n".getBytes());
	                	accept.getOutputStream().flush();
	                } else if ("status".equalsIgnoreCase(command)) {
	                	if (localNode != null && localNode.isStarted()) {
	                		accept.getOutputStream().write("local\r\n".getBytes());
	                		String str = "address: " + localNode.getAddress().hashStr()+ "\r\n";
	                		accept.getOutputStream().write(str.getBytes());
	                		accept.getOutputStream().flush();
	                	}
	                	if (cluster != null && cluster.isStarted()) {
	                		accept.getOutputStream().write("cluster\r\n".getBytes());
	                		String str = "server address: " + ClusterSettings.getClusterAddress() + "\r\n";
	                		str += "accept address: " + ClusterSettings.getClusterRegisterAddress() + "\r\n";
	                		accept.getOutputStream().write(str.getBytes());
	                		accept.getOutputStream().flush();
	                	}
	                }
	                accept.close();
	            } catch(Exception e) {
	                throw new RuntimeException(e);
	            }
        	}
        }
    }
}
