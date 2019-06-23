/*
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 03.09.2010
 * Authors: MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.tigers.sumatra.ai.data.AutomatedThrowInInfo;
import edu.tigers.sumatra.ai.data.AutomatedThrowInInfo.EPrepareThrowInAction;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author MarkG
 */
public class PrepareThrowinCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private long						startTime		= 0;
	private int							yOffset			= 0;
	
	private AutomatedThrowInInfo	info				= null;
	
	private boolean					init				= true;
	
	private long						finishedTimer	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		info = null;
		yOffset = 0;
		switch (newTacticalField.getGameState())
		{
			case BALL_PLACEMENT_THEY:
				calcPlacementParameters(newTacticalField, baseAiFrame, false);
				break;
			case BALL_PLACEMENT_WE:
				calcPlacementParameters(newTacticalField, baseAiFrame, true);
				break;
			default:
				init = true;
				startTime = baseAiFrame.getWorldFrame().getTimestamp();
				finishedTimer = 0;
				break;
		}
		newTacticalField.setThrowInInfo(info);
	}
	
	
	private void calcPlacementParameters(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final boolean placementWe)
	{
		String msg = "Prepare_time: "
				+ (((int) (((baseAiFrame.getWorldFrame().getTimestamp() - startTime) * 1e-9) * 100)) / 100.0) + "s";
		showText(newTacticalField, msg);
		
		if ((baseAiFrame.getRefereeMsg() != null) && (baseAiFrame.getRefereeMsg().getBallPlacementPos() != null))
		{
			showText(newTacticalField, baseAiFrame.getRefereeMsg().getBallPlacementPos().toString());
		}
		
		DrawableCircle distToBallCircle = new DrawableCircle(
				new Circle(baseAiFrame.getWorldFrame().getBall().getPos(),
						OffensiveConstants.getAutomatedThrowInPushDinstance()),
				new Color(255, 0, 0, 100));
		distToBallCircle.setFill(true);
		newTacticalField.getDrawableShapes().get(EShapesLayer.AUTOMATED_THROW_IN).add(distToBallCircle);
		
		if (placementWe)
		{
			info = new AutomatedThrowInInfo();
			info.setAction(EPrepareThrowInAction.PASS_TO_RECEIVER_DIRECTLY);
			info.setPos(baseAiFrame.getRefereeMsg().getBallPlacementPos());
			
			DrawableCircle dtargetCircle = new DrawableCircle(
					new Circle(info.getPos(), OffensiveConstants.getAutomatedThrowInFinalTolerance()),
					new Color(20, 255, 255, 120));
			dtargetCircle.setFill(true);
			newTacticalField.getDrawableShapes().get(EShapesLayer.AUTOMATED_THROW_IN).add(dtargetCircle);
			
			info.setReceiverReady(false);
			info.setFinished(false);
			for (ARole role : baseAiFrame.getPrevFrame().getPlayStrategy()
					.getActiveRoles(ERole.SECONDARY_AUTOMATED_THROW_IN))
			{
				IVector2 kickerToBot = role.getBot().getBotKickerPos().subtractNew(role.getPos());
				IVector2 dest = info.getPos().subtractNew(kickerToBot);
				if (GeoMath.distancePP(dest, role.getPos()) < 100)
				{
					info.setReceiverReady(true);
				}
			}
			if (GeoMath.distancePP(baseAiFrame.getWorldFrame().getBall().getPos(), info.getPos()) < (OffensiveConstants
					.getAutomatedThrowInFinalTolerance() - 3))
			{
				if (finishedTimer == 0)
				{
					finishedTimer = baseAiFrame.getWorldFrame().getTimestamp();
				} else if ((baseAiFrame.getWorldFrame().getTimestamp() - finishedTimer) > OffensiveConstants
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
				List<ITrackedBot> sortedBotList = new ArrayList<ITrackedBot>();
				for (BotID id : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
				{
					ITrackedBot bot = baseAiFrame.getWorldFrame().getTigerBotsAvailable().get(id);
					sortedBotList.add(bot);
				}
				IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
				Collections.sort(sortedBotList,
						Comparator.comparing(e -> GeoMath.distancePP(e.getPos(), ballPos)));
				for (ITrackedBot bot : sortedBotList)
				{
					info.getDesiredBots().add(bot.getBotId());
				}
			} else
			{
				AutomatedThrowInInfo oldInfo = baseAiFrame.getPrevFrame().getTacticalField().getThrowInInfo();
				info.setDesiredBots(oldInfo.getDesiredBots());
			}
		}
	}
	
	
	private void showText(final TacticalField newTacticalField, final String msg)
	{
		DrawableBorderText text = new DrawableBorderText(new Vector2(10, 20 + yOffset), msg, Color.white);
		text.setFontSize(12);
		newTacticalField.getDrawableShapes().get(EShapesLayer.AUTOMATED_THROW_IN).add(text);
		yOffset = yOffset + 20;
	}
}
