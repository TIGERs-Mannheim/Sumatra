/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValueBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Visualize some tactical infos from metis
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TacticsLayer extends AValuePointLayer
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public TacticsLayer()
	{
		super(EFieldLayer.TACTICS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		drawBestDirectShot(g, frame);
		drawBallPossession(g, frame);
		drawBotLastTouchedBall(g, frame);
		drawBestDirectShotBots(g, frame);
		drawShooterReceiverStraightLines(g, frame);
		drawBallReceiverStraightLines(g, frame);
		drawOffenseMovePositions(g, frame);
	}
	
	
	private void drawBestDirectShot(final Graphics2D g, final IRecordFrame frame)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		ValuePoint bestDirectShot = frame.getTacticalField().getBestDirectShootTarget();
		float value = bestDirectShot.getValue();
		drawValueLine(g, ballPos, bestDirectShot, frame.getWorldFrame().isInverted(), value);
	}
	
	
	private void drawBestDirectShotBots(final Graphics2D g, final IRecordFrame frame)
	{
		for (Map.Entry<BotID, ValuePoint> entry : frame.getTacticalField().getBestDirectShotTargetBots().entrySet())
		{
			TrackedTigerBot bot = frame.getWorldFrame().getTigerBotsVisible().getWithNull(entry.getKey());
			if (bot == null)
			{
				continue;
			}
			IVector2 botPos = bot.getPos();
			ValuePoint bestDirectShot = entry.getValue();
			
			float value = bestDirectShot.getValue();
			drawValueLine(g, botPos, bestDirectShot, frame.getWorldFrame().isInverted(), value);
		}
	}
	
	
	private void drawShooterReceiverStraightLines(final Graphics2D g, final IRecordFrame frame)
	{
		for (Map.Entry<BotID, List<ValueBot>> entry : frame.getTacticalField().getShooterReceiverStraightLines()
				.entrySet())
		{
			TrackedTigerBot bot = frame.getWorldFrame().getTigerBotsVisible().getWithNull(entry.getKey());
			if (bot == null)
			{
				continue;
			}
			IVector2 botPos = bot.getPos();
			for (ValueBot receiver : entry.getValue())
			{
				float value = receiver.getValue();
				IVector2 receiverPos = frame.getWorldFrame().getTigerBotsVisible().get(receiver.getBotID()).getPos();
				drawValueLine(g, botPos, receiverPos, frame.getWorldFrame().isInverted(), value);
			}
		}
	}
	
	
	private void drawBallReceiverStraightLines(final Graphics2D g, final IRecordFrame frame)
	{
		for (Map.Entry<BotID, ValueBot> entry : frame.getTacticalField().getBallReceiverStraightLines().entrySet())
		{
			IVector2 receiverPos = frame.getWorldFrame().getTigerBotsVisible().get(entry.getValue().getBotID()).getPos();
			drawValueLine(g, frame.getWorldFrame().getBall().getPos(), receiverPos, frame.getWorldFrame().isInverted(),
					entry.getValue()
							.getValue());
		}
	}
	
	
	private void drawBallPossession(final Graphics2D g, final IRecordFrame frame)
	{
		BotID tiger = frame.getTacticalField().getBallPossession().getTigersId();
		BotID opponent = frame.getTacticalField().getBallPossession().getOpponentsId();
		g.setColor(Color.black);
		if (tiger.isBot())
		{
			IVector2 pos = frame.getWorldFrame().getTigerBotsVisible().get(tiger).getPos();
			drawBotCircle(g, pos, frame.getWorldFrame().isInverted(), 2);
		}
		if (opponent.isBot())
		{
			IVector2 pos = frame.getWorldFrame().getFoeBots().get(opponent).getPos();
			drawBotCircle(g, pos, frame.getWorldFrame().isInverted(), 2);
		}
	}
	
	
	private void drawBotLastTouchedBall(final Graphics2D g, final IRecordFrame frame)
	{
		BotID botId = frame.getTacticalField().getBotLastTouchedBall();
		if (botId.isBot())
		{
			TrackedBot bot = frame.getWorldFrame().getTigerBotsVisible().getWithNull(botId);
			if (bot == null)
			{
				bot = frame.getWorldFrame().getFoeBots().getWithNull(botId);
			}
			if (bot != null)
			{
				g.setColor(Color.magenta);
				drawBotCircle(g, bot.getPos(), frame.getWorldFrame().isInverted(), 0);
			}
		}
	}
	
	
	private void drawOffenseMovePositions(final Graphics2D g, final IRecordFrame frame)
	{
		Map<BotID, ValuePoint> positions = frame.getTacticalField().getOffenseMovePositions();
		
		for (BotID key : positions.keySet())
		{
			TrackedBot bot = frame.getWorldFrame().getTigerBotsVisible().getWithNull(key);
			if (bot != null)
			{
				g.setColor(Color.cyan);
				drawBotCircle(g, positions.get(key), frame.getWorldFrame().isInverted(), 0);
			}
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
