/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.penaltyshootout;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ARoleState;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.DribbleSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class DribbleState extends ARoleState
{
	@Configurable(comment = "[deg] Max angle the dribbling may vary from target", defValue = "20.0")
	private static double maxOffAngleDeg = 20.0;
	@Configurable(comment = "[mm] Distance to count the ball arrived at target", defValue = "800.0")
	private static double arriveDistThreshold = 800.0;
	@Configurable(comment = "[m/s] Speed to count the ball arrived at target", defValue = "1.0")
	private static double arriveSpeedThreshold = 1.0;
	@Configurable(comment = "The factor to apply the opponent2Ball distance to the safeDistance of the skill", defValue = "0.8")
	private static double factorOpponent2BallForSafeDistance = 0.8;
	@Configurable(comment = "If ball is threatened with this ProtectionDistanceGain a DRIBBLING_FAILED event is called", defValue = "0.5")
	private static double protectDistanceGainForFailed = 0.5;
	@Configurable(comment = "If ball is threatened with this ProtectionDistanceGain the safeDistance for the skill will be adapted", defValue = "5.0")
	private static double protectDistanceGainForDangered = 5.0;

	static
	{
		ConfigRegistration.registerClass("roles", DribbleState.class);
	}

	private final boolean penaltyOurIsObstacle;
	private final boolean penaltyTheirIsObstacle;

	private DynamicPosition target;
	private DribbleSkill skill;

	private IVector2 initTarget;
	private IVector2 initBallPosition;


	public DribbleState(final ARole role, DynamicPosition target, boolean penaltyOurIsObstacle,
			boolean penaltyTheirIsObstacle)
	{
		super(role);
		this.target = target;
		this.penaltyOurIsObstacle = penaltyOurIsObstacle;
		this.penaltyTheirIsObstacle = penaltyTheirIsObstacle;
	}


	@Override
	public void doEntryActions()
	{
		initTarget = target.getPos();
		initBallPosition = getBall().getPos();

		skill = new DribbleSkill();
		skill.getMoveCon().setPenaltyAreaOurObstacle(penaltyOurIsObstacle);
		skill.getMoveCon().setPenaltyAreaTheirObstacle(penaltyTheirIsObstacle);
		setNewSkill(skill);
	}


	@Override
	public void doUpdate()
	{
		if (ballPossessionIsThreatened(protectDistanceGainForFailed))
		{
			// Bot twice as far away from ball than opponent
			triggerEvent(EBallDribbleEvent.DRIBBLING_FAILED);
		} else if (ballIsAtTarget() && getBot().hasBallContact() && getBall().getVel().getLength() < arriveSpeedThreshold)
		{
			triggerEvent(EBallDribbleEvent.DRIBBLING_FINISHED);
		}
		updateDribbleTarget();

		BotDistance closestOpponent = getAiFrame().getTacticalField().getOpponentClosestToBall();
		final double distOpponent2Ball = closestOpponent.getDist();
		final double distTarget2Ball = getBall().getPos().distanceTo(target.getPos());


		double safeDistanceToSet = distTarget2Ball;
		if (ballPossessionIsThreatened(protectDistanceGainForDangered))
		{
			skill.setSafeDistance(Math.min(distOpponent2Ball * factorOpponent2BallForSafeDistance, safeDistanceToSet));
		} else
		{
			skill.setSafeDistance(safeDistanceToSet);
		}
	}


	private boolean ballPossessionIsThreatened(final double protectionDistanceGain)
	{
		var opponentToBallDist = getAiFrame().getTacticalField().getOpponentClosestToBall().getDist();
		double protectorToBallDist = getPos().distanceTo(getBall().getPos());
		return protectorToBallDist * protectionDistanceGain > opponentToBallDist;
	}


	private void updateDribbleTarget()
	{
		final IVector2 dirInitBall2InitTarget = initTarget.subtractNew(initBallPosition).normalize();
		final ILine targetLine = Lines.lineFromDirection(initBallPosition, dirInitBall2InitTarget);

		// Make sure the final destination is always further away than the ball, so the bot won't change directions
		// completely
		final double supportVectorDist2InitBallPosition = Math.max(initTarget.distanceTo(initBallPosition),
				targetLine.closestPointOnLine(getBall().getPos()).distanceTo(initBallPosition) + arriveDistThreshold / 5.);

		IVector2 supportVector = initBallPosition
				.addNew(dirInitBall2InitTarget.scaleToNew(supportVectorDist2InitBallPosition));


		// Create the base line as orthogonal to the initTarget-initBallPosition-line through the initTarget
		final ILine targetBaseLine = Lines.lineFromDirection(supportVector, dirInitBall2InitTarget.getNormalVector());

		// Get new updated target by getting the closest point on the line
		IVector2 newTarget = targetBaseLine.closestPointOnLine(getBall().getPos());

		// Take care the maxOffAngleDeg is considered
		final double maxDistanceToInitTarget = Math.tan(AngleMath.deg2rad(maxOffAngleDeg))
				* initTarget.distanceTo(initBallPosition);
		IVector2 shiftVector = newTarget.subtractNew(initTarget);
		if (shiftVector.getLength() > maxDistanceToInitTarget)
		{
			shiftVector = shiftVector.scaleToNew(maxDistanceToInitTarget);
		}

		// Update the target
		skill.setTargetPos(new DynamicPosition(initTarget.addNew(shiftVector)));
	}


	private boolean ballIsAtTarget()
	{
		return target.getPos().distanceTo(getBall().getPos()) < arriveDistThreshold;
	}


}
