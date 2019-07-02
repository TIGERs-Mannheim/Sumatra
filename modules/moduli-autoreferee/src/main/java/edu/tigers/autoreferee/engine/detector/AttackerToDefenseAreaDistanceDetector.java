/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil.ColorFilter;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.AttackerTooCloseToDefenseArea;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Monitors the distance between attackers and the defense area of the defending team when a free kick is performed.
 */
public class AttackerToDefenseAreaDistanceDetector extends AGameEventDetector
{
	@Configurable(comment = "[s] The grace period for the bots to position them after a the activation of the detector", defValue = "2.0")
	private static double gracePeriod = 2.0;
	
	private static final double INACCURACY_TOLERANCE = 15;
	
	
	/**
	 * Default constructor
	 */
	public AttackerToDefenseAreaDistanceDetector()
	{
		super(EGameEventDetectorType.ATTACKER_TO_DEFENSE_AREA_DISTANCE,
				EnumSet.of(EGameState.STOP, EGameState.INDIRECT_FREE, EGameState.DIRECT_FREE, EGameState.KICKOFF));
		setDeactivateOnFirstGameEvent(true);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if (!isActiveForAtLeast(gracePeriod))
		{
			return Optional.empty();
		}
		ETeamColor attackingTeam = getAttackingTeam();
		if (attackingTeam != ETeamColor.NEUTRAL)
		{
			return new Evaluator(frame, attackingTeam).evaluate();
		}
		return Optional.empty();
	}
	
	
	private ETeamColor getAttackingTeam()
	{
		ETeamColor attackingTeam;
		if (frame.getGameState().isStoppedGame())
		{
			attackingTeam = frame.getRefereeMsg().getTeamFromNextCommand();
		} else
		{
			attackingTeam = frame.getRefereeMsg().getTeamFromCommand();
		}
		return attackingTeam;
	}
	
	
	/**
	 * The actual evaluation of this detector is encapsulated in this class. Since this class is created when the rule is
	 * to be evaluated all required values can be stored as private final attributes. This makes the code cleaner because
	 * these values do not need to be passed around between different functions.
	 */
	private static class Evaluator
	{
		private final double requiredMargin = RuleConstraints.getBotToPenaltyAreaMarginStandard()
				+ Geometry.getBotRadius()
				- INACCURACY_TOLERANCE;
		
		private final ETeamColor attackerColor;
		private final IPenaltyArea defenderPenArea;
		
		private final Collection<ITrackedBot> bots;
		
		
		public Evaluator(final IAutoRefFrame frame, final ETeamColor attackerColor)
		{
			this.attackerColor = attackerColor;
			defenderPenArea = NGeometry.getPenaltyArea(attackerColor.opposite());
			bots = frame.getWorldFrame().getBots().values();
		}
		
		
		private Optional<IGameEvent> evaluate()
		{
			Optional<ITrackedBot> optOffender = bots.stream()
					.filter(ColorFilter.get(attackerColor))
					.filter(bot -> defenderPenArea.isPointInShape(bot.getPos(), requiredMargin))
					.filter(this::notBeingPushed)
					.findFirst();
			
			return optOffender.map(this::buildViolation);
		}
		
		
		private boolean notBeingPushed(ITrackedBot bot)
		{
			ETeamColor defenderColor = attackerColor.opposite();
			return !bots.stream()
					// bots from defending team
					.filter(ColorFilter.get(defenderColor))
					// that touch the attacker
					.filter(b -> bot.getPos().distanceTo(b.getPos()) <= Geometry.getBotRadius() * 2)
					// push in direction of penalty area
					.map(b -> Lines.halfLineFromPoints(b.getPos(), bot.getPos()))
					// find intersection that show that defenders pushes towards penArea
					.map(defenderPenArea::lineIntersections)
					.flatMap(List::stream)
					.findAny()
					// if any intersection is present, some defender pushes the attacker
					.isPresent();
		}
		
		
		private IGameEvent buildViolation(final ITrackedBot offender)
		{
			double distance = defenderPenArea.withMargin(requiredMargin).distanceToNearestPointOutside(offender.getPos());
			
			return new AttackerTooCloseToDefenseArea(offender.getBotId(), offender.getPos(), distance);
		}
	}
}
