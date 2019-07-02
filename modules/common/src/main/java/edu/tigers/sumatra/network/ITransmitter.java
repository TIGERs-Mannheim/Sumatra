package edu.tigers.sumatra.network;


import java.io.Closeable;


/**
 * This interface declares objects that are capable of sending something via network.
 * Together with its counterpart {@link IReceiver} and their different implementations, it
 * represents a small, passive, flexible network framework.
 * 
 * @author Gero
 * @param <D> The data type this transmitter is able to process
 */
public interface ITransmitter<D> extends Closeable
{
	/**
	 * Sends data of type {@code <D>} to the network
	 * 
	 * @param data
	 * @return success?
	 */
	boolean send(D data);
}
