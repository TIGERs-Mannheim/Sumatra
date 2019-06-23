/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.DefenseMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseGroup;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.math.Hysterese;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DefenderAssignerCalc extends ACalculator
{
	@Configurable(comment = "Lower ball distance threshold", defValue = "4300.0")
	private static double ballDistanceThresholdLower = 4300;
	
	@Configurable(comment = "Upper ball distance threshold", defValue = "4800.0")
	private static double ballDistanceThresholdUpper = 4800;
	
	@Configurable(comment = "check to use ManToManMarker instead of CenterBack", spezis = { "", "YELLOW",
			"BLUE" }, defValueSpezis = { "false", "false", "false" })
	private boolean useManToManMarker = false;
	
	@Configurable(comment = "Force crucial defenders to stay at penalty area at all times", defValue = "false")
	private static boolean forceCrucialDefendersToPenaltyArea = false;
	
	@Configurable(comment = "Boundary for reducing to one crucial defenders", defValue = "0.9")
	private static double angleThresholdOneCrucialDefenderLower = 0.9;
	
	@Configurable(comment = "Boundary for reducing to one crucial defenders", defValue = "1.0")
	private static double angleThresholdOneCrucialDefenderUpper = 1.0;
	
	@Configurable(comment = "Boundary for reducing to zero crucial defenders", defValue = "1.2")
	private static double angleThresholdZeroCrucialDefenderLower = 1.2;
	
	@Configurable(comment = "Boundary for reducing to zero crucial defenders", defValue = "1.3")
	private static double angleThresholdZeroCrucialDefenderUpper = 1.3;
	
	@Configurable(comment = "Degrade one crucial defender to a standard defender during indirect for them", defValue = "true")
	private static boolean minusOneCrucialDuringIndirect = true;
	
	@Configurable(comment = "Number of bots for covering the ball (at max).", defValue = "2")
	private static int numBotsForBallThreat = 2;
	
	private final Hysterese ballPosHysterese = new Hysterese(ballDistanceThresholdLower, ballDistanceThresholdUpper);
	
	private final Hysterese angleOneCrucialDefenderHysteresis = new Hysterese(angleThresholdOneCrucialDefenderLower,
			angleThresholdOneCrucialDefenderUpper);
	private final Hysterese angleZeroCrucialDefenderHysteresis = new Hysterese(angleThresholdZeroCrucialDefenderLower,
			angleThresholdZeroCrucialDefenderUpper);
	
	
	@Override
	@SuppressWarnings("squid:S1871") // identical branch code blocks (order matters here though!)
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame aiFrame)
	{
		final DefenseBallThreat ballThreat = newTacticalField.getDefenseBallThreat();
		
		List<DefenseThreatAssignment> defenseThreatAssignments = new ArrayList<>();
		
		int numDefenders = newTacticalField.getNumDefender();
		
		IVector2 ballPos = ballThreat.getPos();
		
		int numBotsForBall = getNumBotsForBall(newTacticalField.getGameState(), numDefenders, ballPos);
		
		final EDefenseGroup ballDefenseGroup;
		if (usePenaltyAreaRole(aiFrame, BallID.instance(), ballPos))
		{
			ballDefenseGroup = EDefenseGroup.PENALTY_AREA;
		} else if (newTacticalField.getGameState().isKickoffOrPrepareKickoffForThem())
		{
			ballDefenseGroup = EDefenseGroup.CENTER_BACK;
		} else if (forceCrucialDefendersToPenaltyArea && !newTacticalField.getOpponentPassReceiver().isPresent())
		{
			ballDefenseGroup = EDefenseGroup.PENALTY_AREA;
		} else
		{
			ballDefenseGroup = EDefenseGroup.CENTER_BACK;
		}
		
		DefenseThreatAssignment ballAssignment = new DefenseThreatAssignment(
				getBall().getId(),
				ballThreat,
				numBotsForBall,
				ballDefenseGroup, true);
		defenseThreatAssignments.add(ballAssignment);
		
		numDefenders -= numBotsForBall;
		
		List<DefenseBotThreat> threats = new ArrayList<>(newTacticalField.getDefenseBotThreats());
		
		// pass receiver is covered by ball defenders
		threats.removeIf(t -> ballThreat.getPassReceiver().isPresent()
				&& (ballThreat.getPassReceiver().get().getBotId() == t.getBotID()));
		
		
		List<BotDistance> closeDistances = newTacticalField.getEnemiesToBallDist().stream()
				.filter(d -> d.getDist() < 250)
				.collect(Collectors.toList());
		
		if (closeDistances.size() == 1)
		{
			// if there is a single close opponent to the ball this threat is covered by ball defenders
			threats.removeIf(t -> t.getBotID() == closeDistances.get(0).getBot().getBotId());
		}
		
		
		for (DefenseBotThreat threat : threats)
		{
			int numBotsForBotThreat = 1;
			EDefenseGroup botDefenseGroup = useManToManMarker ? EDefenseGroup.MAN_TO_MAN_MARKER
					: EDefenseGroup.CENTER_BACK;
			
			if (usePenaltyAreaRole(aiFrame, threat.getBotID(), threat.getPos()))
			{
				botDefenseGroup = EDefenseGroup.PENALTY_AREA;
			}
			
			if (numDefenders <= 0)
			{
				botDefenseGroup = EDefenseGroup.UNASSIGNED;
			}
			
			DefenseThreatAssignment botThreatAssignment = new DefenseThreatAssignment(
					threat.getBotID(),
					threat,
					numBotsForBotThreat,
					botDefenseGroup, false);
			defenseThreatAssignments.add(botThreatAssignment);
			
			numDefenders -= numBotsForBotThreat;
		}
		
		newTacticalField.setDefenseThreatAssignments(defenseThreatAssignments);
	}
	
	
	private int getNumBotsForBall(final GameState gameState, final int numDefenders, final IVector2 ballPos)
	{
		int numBotsForBall = numBotsForBallThreat;
		
		ballPosHysterese.setLowerThreshold(ballDistanceThresholdLower);
		ballPosHysterese.setUpperThreshold(ballDistanceThresholdUpper);
		ballPosHysterese.update(ballPos.distanceTo(Geometry.getGoalOur().getCenter()));
		
		if (ballPosHysterese.isUpper())
		{
			numBotsForBall = Math.min(1, numBotsForBall);
		}
		
		angleOneCrucialDefenderHysteresis.setLowerThreshold(angleThresholdOneCrucialDefenderLower);
		angleOneCrucialDefenderHysteresis.setUpperThreshold(angleThresholdOneCrucialDefenderUpper);
		angleOneCrucialDefenderHysteresis
				.update(Math.abs(ballPos.subtractNew(DefenseMath.getBisectionGoal(ballPos)).getAngle()));
		
		angleZeroCrucialDefenderHysteresis.setLowerThreshold(angleThresholdZeroCrucialDefenderLower);
		angleZeroCrucialDefenderHysteresis.setUpperThreshold(angleThresholdZeroCrucialDefenderUpper);
		angleZeroCrucialDefenderHysteresis
				.update(Math.abs(ballPos.subtractNew(DefenseMath.getBisectionGoal(ballPos)).getAngle()));
		
		if (angleZeroCrucialDefenderHysteresis.isUpper())
		{
			numBotsForBall = 0;
		} else if (angleOneCrucialDefenderHysteresis.isUpper())
		{
			numBotsForBall = Math.min(1, numBotsForBall);
		}
		
		if (gameState.isIndirectFreeForThem() && minusOneCrucialDuringIndirect)
		{
			numBotsForBall = Math.max(0, numBotsForBall - 1);
		}
		
		return Math.min(numDefenders, numBotsForBall);
	}
	
	
	private boolean goToPenaltyArea(final AObjectID objectID, final IVector2 threatPos)
	{
		double goOutOffset = (Geometry.getBotRadius() * 2)
				+ (isSameGroupAsLastFrame(objectID, EDefenseGroup.PENALTY_AREA) ? 50.0 : 0.0);
		
		return Geometry.getPenaltyAreaOur().withMargin(DefenseConstants.getMinGoOutDistance() + goOutOffset)
				.isPointInShape(threatPos);
	}
	
	
	private boolean isSameGroupAsLastFrame(final AObjectID objectID, final EDefenseGroup group)
	{
		List<DefenseThreatAssignment> lastAssignments = getAiFrame().getPrevFrame().getTacticalField()
				.getDefenseThreatAssignments();
		
		Optional<DefenseThreatAssignment> assignment = lastAssignments.stream()
				.filter(a -> a.getObjectID().equals(objectID))
				.findFirst();
		return assignment.isPresent() && (assignment.get().getDefenseGroup() == group);
	}
	
	
	private boolean usePenaltyAreaRole(final BaseAiFrame aiFrame, final AObjectID id, final IVector2 pos)
	{
		return aiFrame.getGamestate().isStoppedGame() ||
				getAiFrame().getGamestate().isStandardSituationForThem() ||
				goToPenaltyArea(id, pos);
	}
}
