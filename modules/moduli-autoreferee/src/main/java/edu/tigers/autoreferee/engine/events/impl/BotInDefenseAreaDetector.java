/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.CardPenalty;
import edu.tigers.autoreferee.engine.events.DistanceViolation;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.CardInfo.CardType;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * This rule detects attackers/defenders that touch the ball while inside the defense area of the defending/their own
 * team.
 * 
 * @author Lukas Magel
 */
public class BotInDefenseAreaDetector extends APreparingGameEventDetector
{
	private static final int PRIORITY = 1;
	
	@Configurable(comment = "[ms] The cooldown time before registering a ball touch with the same bot again in ms", defValue = "3000")
	private static int cooldownTimeMs = 3_000;
	
	@Configurable(comment = "[mm] Distance from the defense line that is considered a partial violation", defValue = "20.0")
	private static double partialTouchMargin = 20;
	
	private long entryTime = 0;
	private Map<BotID, BotPosition> lastViolators = new HashMap<>();
	
	static
	{
		AGameEventDetector.registerClass(BotInDefenseAreaDetector.class);
	}
	
	
	/**
	 * 
	 */
	public BotInDefenseAreaDetector()
	{
		super(EGameEventDetectorType.BOT_IN_DEFENSE_AREA, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		entryTime = frame.getTimestamp();
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame)
	{
		if (frame.getBotsTouchingBall().size() > 1
				&& frame.getBotsTouchingBall().stream().anyMatch(b -> b.getBotID().getTeamColor() == ETeamColor.YELLOW)
				&& frame.getBotsTouchingBall().stream().anyMatch(b -> b.getBotID().getTeamColor() == ETeamColor.BLUE))
		{
			// two teams fighting for the ball, most likely being pushed by each other
			return Optional.empty();
		}
		
		for (BotPosition curKicker : frame.getBotsTouchingBall())
		{
			final Optional<IGameEvent> gameEvent = checkBotPosition(frame, curKicker);
			if (gameEvent.isPresent())
			{
				return gameEvent;
			}
		}
		
		return Optional.empty();
	}
	
	
	private Optional<IGameEvent> checkBotPosition(final IAutoRefFrame frame, final BotPosition curKicker)
	{
		if (curKicker.getTimestamp() < entryTime)
		{
			/*
			 * The ball was last touched before the game state changed to RUNNING
			 */
			return Optional.empty();
		}
		if (curKicker.getBotID().isUninitializedID())
		{
			return Optional.empty();
		}
		if ((frame.getTimestamp() - entryTime) / 1e9 < 0.5)
		{
			// wait some time before starting the detection
			// this is mainly for penalty kicks where the game state switches to running in the moment of ball movement
			return Optional.empty();
		}
		
		BotPosition lastViolationOfCurKicker = lastViolators.get(curKicker.getBotID());
		
		if (lastViolationOfCurKicker != null)
		{
			if (curKicker.getTimestamp() == lastViolationOfCurKicker.getTimestamp())
			{
				// The offense has already been reported
				return Optional.empty();
			}
			
			if (curKicker.getBotID().equals(lastViolationOfCurKicker.getBotID()))
			{
				// Wait a certain amount of time before reporting the offense again for the same bot
				long timeDiff = curKicker.getTimestamp() - lastViolationOfCurKicker.getTimestamp();
				if (timeDiff < TimeUnit.MILLISECONDS.toNanos(cooldownTimeMs))
				{
					return Optional.empty();
				}
			}
		}
		
		Set<BotID> keepers = new HashSet<>();
		keepers.add(frame.getRefereeMsg().getKeeperBotID(ETeamColor.BLUE));
		keepers.add(frame.getRefereeMsg().getKeeperBotID(ETeamColor.YELLOW));
		if (keepers.contains(curKicker.getBotID()))
		{
			return Optional.empty();
		}
		
		return checkPenaltyAreas(frame, curKicker);
	}
	
	
	private Optional<IGameEvent> checkPenaltyAreas(final IAutoRefFrame frame, final BotPosition curKicker)
	{
		ETeamColor curKickerColor = curKicker.getBotID().getTeamColor();
		BotID curKickerId = curKicker.getBotID();
		
		IPenaltyArea opponentPenArea = NGeometry.getPenaltyArea(curKickerColor.opposite());
		IPenaltyArea ownPenArea = NGeometry.getPenaltyArea(curKickerColor);
		
		if (opponentPenArea.isPointInShape(curKicker.getPos(), getPartialTouchMargin()))
		{
			/*
			 * Attacker touched the ball while being located partially/fully inside the opponent's penalty area
			 */
			lastViolators.put(curKickerId, curKicker);
			
			double distance = AutoRefMath.distanceToNearestPointOutside(opponentPenArea, Geometry.getBotRadius(),
					curKicker.getPos());
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, curKickerColor.opposite(),
					AutoRefMath.getClosestFreekickPos(curKicker.getPos(), curKickerColor.opposite()));
			
			GameEvent violation = new DistanceViolation(EGameEvent.ATTACKER_IN_DEFENSE_AREA, frame.getTimestamp(),
					curKickerId, followUp, distance);
			
			return Optional.of(violation);
		} else if (defenderIsPushed(frame, curKickerId, curKicker.getPos()))
		{
			return Optional.empty();
		} else if (ownPenArea.isPointInShape(curKicker.getPos(), -Geometry.getBotRadius()))
		{
			/*
			 * Multiple Defender:
			 * Defender touched the ball while being located entirely inside the own defense area
			 */
			lastViolators.put(curKickerId, curKicker);
			
			double distance = AutoRefMath
					.distanceToNearestPointOutside(ownPenArea, Geometry.getBotRadius(), curKicker.getPos());
			FollowUpAction followUp = new FollowUpAction(EActionType.PENALTY, curKickerColor.opposite(),
					NGeometry.getPenaltyMark(curKickerColor));
			GameEvent violation = new DistanceViolation(EGameEvent.MULTIPLE_DEFENDER, frame.getTimestamp(),
					curKickerId, followUp, distance);
			
			return Optional.of(violation);
		} else if (ownPenArea.isPointInShape(curKicker.getPos(), getPartialTouchMargin()))
		{
			/*
			 * Multiple Defender:
			 * Defender touched the ball while being located partially inside his own defense area
			 */
			lastViolators.put(curKickerId, curKicker);
			
			double distance = AutoRefMath.distanceToNearestPointOutside(ownPenArea, Geometry.getBotRadius(),
					curKicker.getPos());
			IVector2 freekickPos = AutoRefMath.getClosestFreekickPos(curKicker.getPos(), curKickerColor.opposite());
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, curKickerColor.opposite(),
					freekickPos);
			CardPenalty cardPenalty = new CardPenalty(CardType.CARD_YELLOW, curKickerColor);
			GameEvent violation = new DistanceViolation(EGameEvent.MULTIPLE_DEFENDER_PARTIALLY, frame.getTimestamp(),
					curKickerId, followUp, cardPenalty, distance);
			return Optional.of(violation);
		}
		return Optional.empty();
	}
	
	
	private boolean defenderIsPushed(final IAutoRefFrame frame, final BotID defender, final IVector2 botPos)
	{
		ETeamColor attackerColor = defender.getTeamColor().opposite();
		
		IPenaltyArea defenderPenaltyArea = NGeometry.getPenaltyArea(defender.getTeamColor());
		return frame.getWorldFrame().getBots().values().stream()
				// bots from attacking team
				.filter(AutoRefUtil.ColorFilter.get(attackerColor))
				// that touch the defender
				.filter(b -> botPos.distanceTo(b.getPos()) <= Geometry.getBotRadius() * 2)
				// push in direction of penalty area
				.map(b -> Lines.halfLineFromPoints(b.getPos(), botPos))
				// find intersection that show that attacker pushs towards penArea
				.map(defenderPenaltyArea::lineIntersections)
				.flatMap(List::stream)
				.findAny()
				// if any intersection is present, some attacker pushes the defender
				.isPresent();
	}
	
	
	private double getPartialTouchMargin()
	{
		return Geometry.getBotRadius() - partialTouchMargin;
	}
	
	
	@Override
	public void doReset()
	{
		lastViolators.clear();
	}
}
