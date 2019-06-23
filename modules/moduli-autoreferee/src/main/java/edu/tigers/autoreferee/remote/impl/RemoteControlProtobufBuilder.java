/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.remote.impl;

import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardTeam;
import edu.tigers.sumatra.Referee.SSL_Referee.Point;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author "Lukas Magel"
 */
public class RemoteControlProtobufBuilder
{
	private static final String AUTO_REF_IMPL_ID = "TIGERs AutoRef";
	private int nextMsgId = 0;
	
	
	/**
	 * @param cmd
	 * @return
	 */
	public SSL_RefereeRemoteControlRequest buildRequest(final RefboxRemoteCommand cmd)
	{
		SSL_RefereeRemoteControlRequest.Builder reqBuilder = SSL_RefereeRemoteControlRequest.newBuilder();
		reqBuilder.setMessageId(nextMsgId++);
		reqBuilder.setImplementationId(AUTO_REF_IMPL_ID);
		if (cmd.getGameEvent() != null)
		{
			reqBuilder.setGameEvent(cmd.getGameEvent());
		}
		switch (cmd.getType())
		{
			case CARD:
				handleCardCommand(cmd, reqBuilder);
				break;
			case COMMAND:
				handleRegularCommand(cmd, reqBuilder);
				break;
			case GAME_EVENT_ONLY:
				break;
			default:
				throw new IllegalStateException("unexpected type: " + cmd.getType());
		}
		return reqBuilder.build();
	}
	
	
	private void handleCardCommand(final RefboxRemoteCommand cmd, final SSL_RefereeRemoteControlRequest.Builder builder)
	{
		CardInfo.Builder cardBuilder = CardInfo.newBuilder();
		cardBuilder.setType(cmd.getCardType());
		cardBuilder.setTeam(cmd.getCardTeam() == ETeamColor.BLUE ? CardTeam.TEAM_BLUE : CardTeam.TEAM_YELLOW);
		builder.setCard(cardBuilder.build());
	}
	
	
	private void handleRegularCommand(final RefboxRemoteCommand cmd,
			final SSL_RefereeRemoteControlRequest.Builder builder)
	{
		builder.setCommand(cmd.getCommand());
		cmd.getKickPos().ifPresent(point -> setPoint(builder, point));
	}
	
	
	private static void setPoint(final SSL_RefereeRemoteControlRequest.Builder reqBuilder, final IVector2 point)
	{
		Point.Builder pointBuilder = Point.newBuilder();
		pointBuilder.setX((float) point.x());
		pointBuilder.setY((float) point.y());
		reqBuilder.setDesignatedPosition(pointBuilder.build());
	}
}
