package cgi.ib.avat;

import com.ib.client.EReaderSignal;

public class MyEReaderSignal implements EReaderSignal{

	@Override
	public void issueSignal() {
		// TODO Auto-generated method stub
		System.out.println("issue signal");
	}

	@Override
	public void waitForSignal() {
		// TODO Auto-generated method stub
		System.out.println("wait For signal");
	}

}
