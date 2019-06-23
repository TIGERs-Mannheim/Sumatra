/**
 * 
 */
package edu.tigers.sumatra.network;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * This interface declares objects that are capable of sending something via network.
 * Together with its counterpart {@link IReceiver} and their different implementations, it
 * represents a small, passive, flexible network framework.
 * 
 * @author Gero
 * @param <D> The data type this transmitter is able to process
 */
public interface ITransmitter<D>
{
	
	/**  */
	static final int	UNDEFINED_PORT	= -1;
	
	
	/**
	 * Sends data of type {@code <D>} to the network
	 * 
	 * @param data
	 * @return success?
	 */
	boolean send(D data);
	
	
	/**
	 * May throw a {@link IOException}, as implementations often call {@link DatagramSocket#close()}
	 * 
	 * @throws IOException
	 */
	void cleanup() throws IOException;
	
	
	/**
	 * @return The local port the implementation ist bound to
	 */
	int getLocalPort();
	
	
	/**
	 * @return The address the implementation sends the content to
	 */
	InetAddress getLocalAddress();
	
	
	/**
	 * @return The port the implementation sends the content to
	 */
	int getTargetPort();
	
	
	/**
	 * @return Whether the implementation is ready to send content or not
	 */
	boolean isReady();
}
