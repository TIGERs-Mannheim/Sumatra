/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;


/**
 * Move straight to the ball and get the ball onto the dribbler.
 */
public class GetBallContactSkill extends AMoveToSkill
{
	@Configurable(defValue = "300", comment = "Max dist [mm] to push towards an invisible ball")
	private static double maxApproachBallExtraDist = 300;

	@Configurable(comment = "Dribbler speed", defValue = "10000.0")
	private static double dribblerSpeed = 10000;

	@Configurable(comment = "How fast to accelerate when getting ball contact", defValue = "1.5")
	private static double getContactAcc = 1.5;

	@Configurable(comment = "Min contact time [s] to reach before moving with ball", defValue = "0.15")
	private static double minContactTime = 0.15;

	private double cachedOrientation;
	private double approachBallExtraDist;
	private IVector2 initBallPos;

	private final BallStabilizer ballStabilizer = new BallStabilizer();
	private final PositionValidator positionValidator = new PositionValidator();


	private double getTargetOrientation(final IVector2 dest)
	{
		var dir = getBall().getPos().subtractNew(dest);
		if (!getBall().isOnCam(0.1) || dir.getLength2() < getTBot().getCenter2DribblerDist())
		{
			return cachedOrientation;
		}
		return dir.getAngle(0);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		positionValidator.setMarginToFieldBorder(Geometry.getBoundaryWidth() - Geometry.getBotRadius());
		cachedOrientation = getBall().getPos().subtractNew(getPos()).getAngle();
		approachBallExtraDist = 0;
		initBallPos = getBall().getPos();
		setKickParams(KickParams.disarm().withDribbleSpeed(dribblerSpeed));

		getMoveCon().physicalObstaclesOnly();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		if (getTBot().getBallContact().getContactDuration() > minContactTime)
		{
			setSkillState(ESkillState.SUCCESS);
		} else if (!getTBot().getBallContact().hadContact(0.1)
				&& (approachBallExtraDist > maxApproachBallExtraDist
				|| initBallPos.distanceTo(getBall().getPos()) > 100))
		{
			approachBallExtraDist = maxApproachBallExtraDist;
			setSkillState(ESkillState.FAILURE);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}

		if (getVel().getLength2() <= getMoveConstraints().getVelMax())
		{
			getMoveConstraints().setAccMax(getContactAcc);
		}

		positionValidator.update(getWorldFrame(), getMoveCon());
		ballStabilizer.setBotBrakeAcc(getContactAcc);
		ballStabilizer.update(getBall(), getTBot());

		if (getTBot().getBallContact().hasNoContact())
		{
			var dest = getDest();
			dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			updateDestination(dest);

			cachedOrientation = getTargetOrientation(getPos());
			updateTargetAngle(cachedOrientation);

			if (getPos().distanceTo(dest) < 5)
			{
				approachBallExtraDist += 10;
			}
		} else
		{
			approachBallExtraDist = 0;
		}

		super.doUpdate();
	}


	private IVector2 getDest()
	{
		return getBall().getPos().subtractNew(
				Vector2.fromAngle(cachedOrientation).scaleTo(
						getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() - approachBallExtraDist
				)
		);
	}
}
