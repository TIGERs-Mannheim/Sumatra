/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.data.AutomatedThrowInInfo;
import edu.tigers.sumatra.ai.data.AutomatedThrowInInfo.EPrepareThrowInAction;
import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author MarkG
 */

public class PrepareThrowInCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private long startTime = 0;
	private int offset = 0;
	
	private AutomatedThrowInInfo info = null;
	
	private boolean init = true;
	
	private long finishedTimer = 0;
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		info = null;
		offset = 0;
		
		GameState gameState = baseAiFrame.getGamestate();
		
		if (gameState.isBallPlacementForUs())
		{
			calcPlacementParameters(newTacticalField, baseAiFrame);
		} else
		{
			init = true;
			startTime = getWFrame().getTimestamp();
			finishedTimer = 0;
		}
		
		newTacticalField.setThrowInInfo(info);
		if (info != null && !info.getDesiredBots().isEmpty())
		{
			newTacticalField.addDesiredBots(EPlay.AUTOMATED_THROW_IN, info.getDesiredBots());
		}
	}
	
	
	private void calcPlacementParameters(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		drawShapes(newTacticalField);
		GameState gameState = baseAiFrame.getGamestate();
		
		
		info = new AutomatedThrowInInfo();
		info.setAction(EPrepareThrowInAction.PASS_TO_RECEIVER_DIRECTLY);
		info.setPos(gameState.getBallPlacementPositionForUs());
		
		info.setFinished(false);
		if (getBall().getPos()
				.distanceTo(info.getPos()) < (OffensiveConstants.getAutomatedThrowInFinalTolerance() - 3))
		{
			if (finishedTimer == 0)
			{
				finishedTimer = getWFrame().getTimestamp();
			} else if ((getWFrame().getTimestamp() - finishedTimer) > OffensiveConstants
					.getAutomatedThrowInClearMoveTime())
			{
				info.setFinished(true);
			}
		} else
		{
			finishedTimer = 0;
		}
		if (init)
		{
			init = false;
			List<ITrackedBot> sortedBotList = new ArrayList<>();
			sortedBotList.addAll(getWFrame().getTigerBotsAvailable().values().stream()
					.filter(bot -> bot.getId() != getAiFrame().getKeeperId()).collect(Collectors.toList()));
			
			
			IVector2 ballPos = getBall().getPos();
			sortedBotList.sort(Comparator.comparingDouble(bot -> bot.getPos().distanceTo(ballPos)));
			info.getDesiredBots().add(sortedBotList.get(0).getBotId());
			sortedBotList.remove(0);
			sortedBotList.sort(Comparator.comparingDouble(bot -> bot.getPos().distanceTo(info.getPos())));
			info.getDesiredBots().add(sortedBotList.get(0).getBotId());
		} else
		{
			AutomatedThrowInInfo oldInfo = baseAiFrame.getPrevFrame().getTacticalField().getThrowInInfo();
			info.setDesiredBots(oldInfo.getDesiredBots());
		}
		
	}
	
	
	private void drawShapes(final TacticalField newTacticalField)
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		String msg = "PREPARE_TIME: "
				+ (((int) (((getWFrame().getTimestamp() - startTime) * 1e-9) * 100)) / 100.0) + "s";
		
		GameState gameState = getAiFrame().getGamestate();
		showText(shapes, msg);
		if (gameState.isBallPlacementForUs())
		{
			showText(shapes, gameState.getBallPlacementPositionForUs().toString());
		}
		DrawableCircle distToBallCircle = new DrawableCircle(
				Circle.createCircle(getBall().getPos(), OffensiveConstants.getAutomatedThrowInPushDistance()),
				new Color(255, 0, 0, 100));
		distToBallCircle.setFill(true);
		shapes.add(distToBallCircle);
		
		DrawableCircle targetCircle = new DrawableCircle(
				Circle.createCircle(gameState.getBallPlacementPositionForUs(),
						OffensiveConstants.getAutomatedThrowInFinalTolerance()),
				new Color(20, 255, 255, 120));
		targetCircle.setFill(true);
		shapes.add(targetCircle);
		newTacticalField.getDrawableShapes().get(EAiShapesLayer.AUTOMATED_THROW_IN).addAll(shapes);
	}
	
	
	private void showText(List<IDrawableShape> shapes, final String msg)
	{
		DrawableBorderText text = new DrawableBorderText(Vector2.fromXY(450.0 + offset, 12.0), msg, Color.white);
		text.setFontSize(10);
		shapes.add(text);
		offset = offset + 130;
	}
}
