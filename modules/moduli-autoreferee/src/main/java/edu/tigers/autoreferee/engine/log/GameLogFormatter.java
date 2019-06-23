/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.log;

import java.text.DecimalFormat;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.sumatra.referee.RefereeMsg;


/**
 * @author "Lukas Magel"
 */
public class GameLogFormatter
{
	private static final DecimalFormat	posFormat	= new DecimalFormat("###0.00");
	
	
	/**
	 * @param action
	 * @return
	 */
	public static String formatFollowUp(final FollowUpAction action)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(action.getActionType());
		builder.append(" | ");
		builder.append(action.getTeamInFavor());
		action.getNewBallPosition().ifPresent(ballPos -> {
			builder.append(" | (");
			builder.append(posFormat.format(ballPos.x()));
			builder.append(" ");
			builder.append(posFormat.format(ballPos.y()));
			builder.append(")");
		});
		return builder.toString();
	}
	
	
	/**
	 * @param cmd
	 * @return
	 */
	public static String formatCommand(final RefCommand cmd)
	{
		StringBuilder builder = new StringBuilder();
		switch (cmd.getType())
		{
			case CARD:
				builder.append(cmd.getCardType());
				builder.append(" | ");
				builder.append(cmd.getCardTeam());
				break;
			case COMMAND:
				builder.append(cmd.getCommand());
				cmd.getKickPos().ifPresent(pos -> {
					builder.append(" @Pos: ");
					builder.append(posFormat.format(pos.x()));
					builder.append(" | ");
					builder.append(posFormat.format(pos.y()));
				});
				break;
		}
		return builder.toString();
	}
	
	
	/**
	 * @param msg
	 * @return
	 */
	public static String formatRefMsg(final RefereeMsg msg)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(msg.getCommandCounter());
		builder.append(" ");
		builder.append(msg.getCommand());
		return builder.toString();
	}
}
