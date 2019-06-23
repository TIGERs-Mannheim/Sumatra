/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.remote.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;


/**
 * @author "Lukas Magel"
 */
public class RefboxRemoteSocket
{
	private static final Logger log = Logger.getLogger(RefboxRemoteSocket.class);
	private static final Encoder b64Encoder = Base64.getEncoder();
	
	private static final int INT_FIELD_SIZE = 4;
	
	private SocketChannel socketChannel;
	private ByteBuffer intBuffer;
	
	
	/**
	 * Create new instance
	 */
	public RefboxRemoteSocket()
	{
		intBuffer = ByteBuffer.allocate(INT_FIELD_SIZE);
		intBuffer.order(ByteOrder.BIG_ENDIAN);
	}
	
	
	/**
	 * @param hostname
	 * @param port
	 * @throws IOException
	 */
	public synchronized void connect(final String hostname, final int port) throws IOException
	{
		socketChannel = SocketChannel.open();
		InetSocketAddress addr = new InetSocketAddress(hostname, port);
		socketChannel.connect(addr);
	}
	
	
	/**
	 * Close the remote socket
	 */
	public synchronized void close()
	{
		try
		{
			socketChannel.close();
		} catch (IOException e)
		{
			log.warn("Error while closing socket channel", e);
		}
	}
	
	
	/**
	 * @param request
	 * @return
	 * @throws IOException if an I/O error occurs
	 * @throws InvalidProtocolBufferException If the reply could not be correctly read and parsed from the socket. This
	 *            usually indicates that sender and receiver are out of sync and the connection should be closed and
	 *            reopened.
	 */
	public SSL_RefereeRemoteControlReply sendRequest(final SSL_RefereeRemoteControlRequest request)
			throws IOException
	{
		writeRequest(request);
		return readReply();
	}
	
	
	private SSL_RefereeRemoteControlReply readReply() throws IOException
	{
		prepareBuf(INT_FIELD_SIZE);
		while (intBuffer.hasRemaining())
		{
			socketChannel.read(intBuffer);
		}
		intBuffer.flip();
		int msgLength = intBuffer.getInt();
		log.debug("Attempting to receive remote reply with size: " + msgLength);
		
		prepareBuf(msgLength);
		while (intBuffer.hasRemaining())
		{
			socketChannel.read(intBuffer);
		}
		intBuffer.flip();
		
		
		byte[] binData = Arrays.copyOf(intBuffer.array(), msgLength);
		
		try
		{
			SSL_RefereeRemoteControlReply reply = SSL_RefereeRemoteControlReply.parseFrom(binData);
			log.debug("Received reply: " + reply.toString());
			return reply;
		} catch (InvalidProtocolBufferException e)
		{
			log.error("Unable to parse following reply with binary size " + msgLength + " (Base64): "
					+ b64Encoder.encodeToString(binData), e);
			throw e;
		}
	}
	
	
	private void writeRequest(final SSL_RefereeRemoteControlRequest req) throws IOException
	{
		log.debug("Sending command with size (" + req.getSerializedSize() + "): " + req.toString());
		
		int totalSize = req.getSerializedSize() + INT_FIELD_SIZE;
		prepareBuf(totalSize);
		
		intBuffer.putInt(req.getSerializedSize());
		intBuffer.put(req.toByteArray());
		intBuffer.flip();
		
		while (intBuffer.hasRemaining())
		{
			socketChannel.write(intBuffer);
		}
	}
	
	
	private void ensureSize(final int size)
	{
		if (intBuffer.capacity() < size)
		{
			intBuffer = ByteBuffer.allocate(size);
		}
	}
	
	
	private void prepareBuf(final int size)
	{
		ensureSize(size);
		intBuffer.clear();
		intBuffer.limit(size);
	}
}
