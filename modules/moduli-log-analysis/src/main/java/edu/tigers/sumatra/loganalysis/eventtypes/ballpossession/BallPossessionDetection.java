/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.ballpossession;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.loganalysis.eventtypes.IEventTypeDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * class detects which team/bot is in possession of the ball
 */
public class BallPossessionDetection implements IEventTypeDetection<BallPossessionEventType>
{
	
	private BallPossessionEventType ballPossession;
	
	private SimpleWorldFrame previousSimpleWorldFrame = null;
	
	
	private List<IDrawableShape> possessionHistoryDraw = new LinkedList<>();
	
	private boolean ballContentionState = false;
	private Set<BotID> botsInvolvedInBallContention = null;
	
	@Configurable(comment = "distance of bots and ball necessary to leave ball contention state", defValue = "25.0")
	private double leaveBallContentionStateThresholdDistance = 25d;
	
	
	@Override
	public void nextFrameForDetection(TypeDetectionFrame frame)
	{
		SimpleWorldFrame currentSimpleWorldFrame = frame.getWorldFrameWrapper().getSimpleWorldFrame();
		List<IDrawableShape> shapes = frame.getShapeMap().get(ELogAnalysisShapesLayer.BALL_POSSESSION);
		
		GameState gameState = frame.getWorldFrameWrapper().getGameState();
		
		if (!gameState.isRunning())
		{
			ballPossession = new BallPossessionEventType(ETeamColor.NEUTRAL);
		}
		else if (previousSimpleWorldFrame != null)
		{

			checkIfBallContentionStateStillActive(currentSimpleWorldFrame, shapes);


			BotLastTouchedBallCalculator calculator = new BotLastTouchedBallCalculator(currentSimpleWorldFrame,
					previousSimpleWorldFrame);
			Set<BotID> currentlyTouchingBots = calculator.currentlyTouchingBots().stream()
					.map(currentSimpleWorldFrame::getBot)
					.map(ITrackedBot::getBotId)
					.collect(Collectors.toSet());

			if (ballContentionState)
			{
				ballPossession = new BallPossessionEventType(ETeamColor.NEUTRAL);
			}
			else
			{
				if (currentlyTouchingBots.size() == 1)
				{
					// exactly one player on ball
					
					BotID botOnBall = currentlyTouchingBots.stream().findAny().get();
					
					ballPossession = new BallPossessionEventType(botOnBall.getTeamColor(),
							currentSimpleWorldFrame.getBot(botOnBall));
				}
				else if (currentlyTouchingBots.size() > 1)
				{
					// more than one bot touching the ball
					
					ballPossession = new BallPossessionEventType(ETeamColor.NEUTRAL);
					ballContentionState = true;
					botsInvolvedInBallContention = currentlyTouchingBots;
				}
				else
				{
					// if the amount of currentlyTouchingBot is zero, no player is on ball.
					// So the last previous ball possession is kept

					ballPossession = new BallPossessionEventType(ballPossession.getPossessionState());
				}
			}
			
		}
		else
		{
			ballPossession = new BallPossessionEventType(ETeamColor.NEUTRAL);
		}

		drawDetectionForFrame(frame, shapes);


		previousSimpleWorldFrame = currentSimpleWorldFrame;
	}

	private void drawDetectionForFrame(final TypeDetectionFrame frame, final List<IDrawableShape> shapes) {
		IVector2 ballPos = frame.getWorldFrameWrapper().getSimpleWorldFrame().getBall().getPos();
		frame.getShapeMap().get(ELogAnalysisShapesLayer.BALL_POSSESSION)
				.add(new DrawableCircle(Circle.createCircle(ballPos, Geometry.getBallRadius()),
						ballPossession.getPossessionState().getColor()));

		possessionHistoryDraw.add(new DrawablePoint(ballPos, ballPossession.getPossessionState().getColor()));

		while(possessionHistoryDraw.size() > 2000 )
		{
			possessionHistoryDraw.remove(0);
		}

		shapes.addAll(possessionHistoryDraw);

		DrawableBorderText hud = new DrawableBorderText(Vector2.fromXY(318.0, 35),
				ballPossession.getPossessionState().toString(), Color.BLACK);
		hud.setFontSize(10);
		shapes.add(hud);
	}

	private void checkIfBallContentionStateStillActive(final SimpleWorldFrame currentSimpleWorldFrame, final List<IDrawableShape> shapes)
	{
		if (ballContentionState)
		{
			IVector2 ballPos = currentSimpleWorldFrame.getBall().getPos();

			botsInvolvedInBallContention.stream()
					.map(currentSimpleWorldFrame::getBot)
					.forEach(bot -> shapes.add(new DrawableCircle(Circle.createCircle(bot.getPos(),
							Geometry.getBotRadius() + leaveBallContentionStateThresholdDistance), Color.GRAY)));

			long amountBotsOutOfThreshold = botsInvolvedInBallContention.stream()
					.map(currentSimpleWorldFrame::getBot)
					.filter(bot -> bot.getPos().distanceTo(ballPos)
							- Geometry.getBotRadius() > leaveBallContentionStateThresholdDistance)
					.count();

			ballContentionState = amountBotsOutOfThreshold == 0;
		}
	}

	@Override
	public void resetDetection()
	{
		possessionHistoryDraw.clear();
		ballContentionState = false;
	}

	@Override
	public BallPossessionEventType getDetectedEventType()
	{
		return ballPossession;
	}
	
	
}

