package edu.tigers.sumatra.network;

import java.io.IOException;
import java.net.DatagramPacket;


public interface UDPReceiver extends AutoCloseable
{
	void receive(final DatagramPacket store) throws IOException;


	void addObserver(final IReceiverObserver observer);


	void removeObserver(final IReceiverObserver observer);
}
