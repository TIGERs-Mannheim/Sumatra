/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.AttackerDoubleTouchedBall;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * <p>
 * This class detects a violation of the Double Touch Rule which can occur if the bot who performs a
 * kickoff/direct/indirect touches the ball a second time before any other bot touched it
 * -> according to rules from 2019: the bot is allowed to touch the ball more than ones before the ball moved 50mm
 * </p>
 * <p>
 * From the rules (as of 2019):
 * <h3>8.1.3. Double Touch</h3>
 * When the ball is brought into play following a kick-off, direct free kick, indirect free kick or penalty kick,
 * the kicker is not allowed to touch the ball until it has been touched by another robot or the game has been stopped.
 * The ball must have moved at least 0.05 meters to be considered as in play.
 * It is understood that the ball may be bumped by the robot multiple times over a short distance while the kick is
 * being taken.
 * This is why a distance of 0.05 meters is used to decide whether a robot violates this rule or not.
 * Remaining in contact with the ball for more than 0.05 meters also counts as double touch,
 * even though technically the robot only touched the ball once.
 * </p>
 */
public class DoubleTouchDetector extends AGameEventDetector
{
	private static final double MIN_BALL_MOVE_DISTANCE = 50.0;
	private static final Set<EGameState> ACTIVE_STATES = Collections.unmodifiableSet(EnumSet.of(
			EGameState.KICKOFF, EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE, EGameState.RUNNING));
	
	private boolean ballMoved;
	private BotID kickerID = null;
	private IVector2 initialBallPos;
	
	
	public DoubleTouchDetector()
	{
		super(EGameEventDetectorType.DOUBLE_TOUCH, ACTIVE_STATES);
		setDeactivateOnFirstGameEvent(true);
	}
	
	
	@Override
	protected void doPrepare()
	{
		ballMoved = false;
		kickerID = null;
		initialBallPos = getBall().getPos();
		
		if (frame.getGameState().isRunning())
		{
			// not coming from kickoff or freekick -> not relevant for this detector
			setInactive();
		}
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if (!ballMoved)
		{
			frame.getShapes().get(EAutoRefShapesLayer.ENGINE)
					.add(new DrawableLine(Lines.segmentFromPoints(initialBallPos, getBall().getPos()), Color.RED));
			kickerID = frame.getBotsTouchingBall().stream().findFirst().map(BotPosition::getBotID).orElse(kickerID);
			ballMoved = initialBallPos.distanceTo(getBall().getPos()) > MIN_BALL_MOVE_DISTANCE;
			return Optional.empty();
		}
		
		drawCurrentKickerBot();
		
		// at this point, the ball has moved. The kicker may not touch the ball anymore, even if it kept touching it
		// the whole time
		
		if (kickerID == null || frame.getBotsLastTouchedBall().stream().noneMatch(b -> b.getBotID().equals(kickerID)))
		{
			// The ball has been touched by another robot
			setInactive();
			return Optional.empty();
		}
		
		if (frame.getBotsTouchingBall().stream().anyMatch(b -> b.getBotID().equals(kickerID)))
		{
			return Optional.of(new AttackerDoubleTouchedBall(kickerID, getBall().getPos()));
		}
		
		// situation is not decided yet
		return Optional.empty();
	}
	
	
	private void drawCurrentKickerBot()
	{
		if (kickerID != null)
		{
			final ITrackedBot kicker = frame.getWorldFrame().getBot(kickerID);
			if (kicker != null)
			{
				frame.getShapes().get(EAutoRefShapesLayer.ENGINE)
						.add(new DrawableCircle(Circle.createCircle(kicker.getPos(), 130), Color.ORANGE));
			}
		}
	}
}
