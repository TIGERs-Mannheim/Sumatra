/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;

import java.awt.Color;


/**
 * Move straight to the ball and get the ball onto the dribbler.
 */
public class GetBallContactSkill extends AMoveToSkill
{
	@Configurable(defValue = "300", comment = "Max dist [mm] to push towards an invisible ball")
	private static double maxApproachBallExtraDist = 300;

	@Configurable(comment = "How fast to accelerate when getting ball contact", defValue = "1.0")
	private static double getContactAcc = 1.0;

	@Configurable(comment = "Min contact time [s] to reach before coming to a stop", defValue = "0.2")
	private static double minContactTime = 0.2;

	@Configurable(comment = "Min contact time [s] to reach before succeeding", defValue = "0.4")
	private static double minSuccessContactTime = 0.4;

	private double cachedOrientation;
	private double approachBallExtraDist;
	private IVector2 initBallPos;

	private final PositionValidator positionValidator = new PositionValidator();


	private double getTargetOrientation()
	{
		var dir = getBall().getPos().subtractNew(getPos());
		boolean ballNotOnCamera = !getBall().isOnCam(0.1);
		boolean insideBot = dir.getLength2() < getTBot().getCenter2DribblerDist();
		if (ballNotOnCamera || insideBot)
		{
			return cachedOrientation;
		}
		double currentDir = dir.getAngle(0);
		if (AngleMath.diffAbs(currentDir, cachedOrientation) > 0.3)
		{
			return currentDir;
		}
		return cachedOrientation;
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		positionValidator.setMarginToFieldBorder(Geometry.getBoundaryWidth() - Geometry.getBotRadius());
		cachedOrientation = getBall().getPos().subtractNew(getPos()).getAngle();
		approachBallExtraDist = 0;
		initBallPos = getBall().getPos();
		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));

		getMoveCon().physicalObstaclesOnly();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		if (getTBot().getBallContact().getContactDuration() > minSuccessContactTime)
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

		if (getTBot().getBallContact().getContactDuration() < minContactTime)
		{
			var dest = getDest();
			dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			updateDestination(dest);

			cachedOrientation = getTargetOrientation();
			updateTargetAngle(cachedOrientation);

			if (getPos().distanceTo(dest) < 15)
			{
				approachBallExtraDist += 10;
			}
		} else
		{
			approachBallExtraDist = 0;
		}

		getShapes().get(ESkillShapesLayer.GET_BALL_CONTACT).add(new DrawableLine(
				Lines.segmentFromOffset(getPos(), Vector2.fromAngle(cachedOrientation).scaleTo(500)),
				Color.red
		));

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
