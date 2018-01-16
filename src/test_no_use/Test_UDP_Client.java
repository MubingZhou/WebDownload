package test_no_use;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Test_UDP_Client {
	public static void main(String[] args) {
		try {
			System.out.println("--------- this is client -------------");
			//新建一个DatagramSocket
	        DatagramSocket client = new DatagramSocket();

	        //往服务端发送消息
	        String sendStr_5050 = "Hello! I'm Client port = 5050";
	        byte[] sendBuf;
	        sendBuf = sendStr_5050.getBytes();
	        InetAddress addr_5050 = InetAddress.getByName("127.0.0.1");
	        int port = 5050;
	        DatagramPacket sendPacket_5050 = new DatagramPacket(sendBuf, sendBuf.length,    addr_5050, port);
	        client.send(sendPacket_5050);
	        
	        //往服务端发送消息
	        String sendStr_5051 = "Hello! I'm Client port = 5051";
	        sendBuf = sendStr_5051.getBytes();
	        InetAddress addr_5051 = InetAddress.getByName("127.0.0.1");
	        int port_5051 = 5051;
	        DatagramPacket sendPacket_5051 = new DatagramPacket(sendBuf, sendBuf.length,    addr_5051, port_5051);
	        client.send(sendPacket_5051);
	        
	        
	        
	        //接受服务端传来的消息
	        byte[] recvBuf = new byte[100];
	        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
	        client.receive(recvPacket);
	        String recvStr = new String(recvPacket.getData(), 0,
	                recvPacket.getLength());
	        System.out.println("服务端传来消息:" + recvStr);
	        
	        //关闭DatagramSocket
	        client.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
