package edu.tigers.sumatra.referee;

import java.net.URI;

import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tigers.sumatra.referee.control.Event;


public class SslGameControllerClient extends WebSocketClient
{
	private static final Logger log = Logger.getLogger(SslGameControllerClient.class.getName());
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	
	public SslGameControllerClient(final URI serverUri)
	{
		super(serverUri);
	}
	
	
	@Override
	public void onOpen(final ServerHandshake handshakedata)
	{
		// empty
	}
	
	
	@Override
	public void onMessage(final String message)
	{
		// empty - we are not interested in the message currently
	}
	
	
	@Override
	public void onClose(final int code, final String reason, final boolean remote)
	{
		log.debug("WS closed: " + code + " " + reason + " remote=" + remote);
	}
	
	
	@Override
	public void onError(final Exception ex)
	{
		log.warn("WS error: " + ex.getMessage(), ex);
	}
	
	
	public void sendEvent(Event event)
	{
		try
		{
			send(objectMapper.writeValueAsString(event));
		} catch (JsonProcessingException e)
		{
			log.warn("Could not serialize game controller event.", e);
		}
	}
}
