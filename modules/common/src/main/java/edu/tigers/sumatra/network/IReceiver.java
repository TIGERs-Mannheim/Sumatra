package edu.tigers.sumatra.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * This interface declares a object that is capable of receiving something via network.
 * Together with its counterpart {@link ITransmitter} and their different implementations, it
 * represents a small, passive, flexible network framework.
 * 
 * @author Gero
 */
public interface IReceiver
{
	/**
	 * Receives a {@link DatagramPacket} from the network
	 * 
	 * @param store
	 * @return The given packet {@code store} with filled buffer (or <code>null</code> if the receiver
	 *         <code>!isReady()</code>)
	 * @throws IOException
	 */
	DatagramPacket receive(DatagramPacket store) throws IOException;
	
	
	/**
	 * May throw a {@link IOException}, as implementations often call {@link DatagramSocket#close()}
	 * 
	 * @throws IOException
	 */
	void cleanup() throws IOException;
	
	
	/**
	 * @return Whether the implementation is ready to receive content or not
	 */
	boolean isReady();
}
