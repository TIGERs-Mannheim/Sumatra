/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.remote;


import edu.tigers.sumatra.SslGameControllerCommon;


public class GameEventResponse
{
	private final Response response;
	private final String reason;
	private final Verification verification;
	
	
	public GameEventResponse(SslGameControllerCommon.ControllerReply reply)
	{
		response = getResponse(reply);
		verification = getVerification(reply);
		
		if (reply.hasReason())
		{
			reason = reply.getReason();
		} else
		{
			reason = "";
		}
	}
	
	
	private Verification getVerification(final SslGameControllerCommon.ControllerReply reply)
	{
		Verification res = Verification.NO_REPLY;
		if (reply.hasVerification())
		{
			if (reply.getVerification() == SslGameControllerCommon.ControllerReply.Verification.VERIFIED)
			{
				res = Verification.VERIFIED;
			} else
			{
				res = Verification.UNVERIFIED;
			}
		}
		
		return res;
	}
	
	
	private Response getResponse(final SslGameControllerCommon.ControllerReply reply)
	{
		Response res = Response.NO_REPLY;
		if (reply.hasStatusCode())
		{
			if (reply.getStatusCode() == SslGameControllerCommon.ControllerReply.StatusCode.OK)
			{
				res = Response.OK;
			} else if (reply.getStatusCode() == SslGameControllerCommon.ControllerReply.StatusCode.REJECTED)
			{
				res = Response.REJECT;
			} else
			{
				res = Response.UNKNOWN;
			}
		}
		
		return res;
	}
	
	
	@Override
	public String toString()
	{
		return String.format("SSL-Game-Controller Response: %s with Reason: %s [Verification: %s]", response, reason,
				verification);
	}
	
	
	public Response getResponse()
	{
		return response;
	}
	
	public enum Response
	{
		UNKNOWN,
		OK,
		REJECT,
		NO_REPLY
	}
	
	public enum Verification
	{
		UNKNOWN,
		VERIFIED,
		UNVERIFIED,
		NO_REPLY
	}
}
