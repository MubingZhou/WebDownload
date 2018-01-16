package test_no_use;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Test_UDP_Server {

	public static void main(String[] args) {
		try {
			System.out.println("--------- this is server -------------");
			
			//新建一个DatagramSocket
	        DatagramSocket server = new DatagramSocket(5050);
	        
	        //接收客户端发送来的消息
	        byte[] recvBuf = new byte[120];
	        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
	        server.receive(recvPacket);
	        String recvStr = new String(recvPacket.getData(), 0, recvPacket    .getLength());
	        System.out.println("客户端传来消息:" + recvStr);
	        
	        //往客户端发送消息
	        int port = recvPacket.getPort();
	        InetAddress addr = recvPacket.getAddress();
	        String sendStr = "Hello ! I'm Server";
	        byte[] sendBuf;
	        sendBuf = sendStr.getBytes();
	        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length,addr, port);
	        server.send(sendPacket);
	        
	        //关闭DatagramSocket
	        //server.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
