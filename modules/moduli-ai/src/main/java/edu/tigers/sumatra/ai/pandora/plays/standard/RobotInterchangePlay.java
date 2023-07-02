/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.List;


/**
 * @author ArneS
 */
public class RobotInterchangePlay extends AMaintenancePlay
{
	private static final int LOOK_OUTSIDE_FIELD_ANGLE = 90;
	@Configurable(comment = "Distance between bots in interchange lineup", defValue = "270.0")
	private static double distanceBetweenBotsInInterchangePosition = 270;

	@Configurable(comment = "[deg] Max turning angle for interchange bots", defValue = "20.0")
	private static double maxTurnAngle = 20;

	@Configurable(comment = "Factor to reduce maxVelW for interchange behaviour.", defValue = "0.1")
	private static double maxVelWFactor = 0.1;

	@Configurable(comment = "Factor to reduce maxAccW for interchange behaviour", defValue = "0.1")
	private static double maxAccWFactor = 0.1;

	@Configurable(comment = "Distance from field line for interchangable bots (may be negative)", defValue = "-130.0")
	private static double distanceToFieldLine = -130.0;

	@Configurable(comment = "Position for interchangable bots in vision coordinates", defValue = "POS_Y")
	private static VisionSide positionInVision = VisionSide.POS_Y;

	@Configurable(comment = "Offset factor to centerLine", defValue = "0.0")
	private static double offsetFactorToCenterline = 0.0;

	private IVector2 lineupDirectionVector = Vector2.fromXY(1, 0)
			.scaleTo(distanceBetweenBotsInInterchangePosition);

	private int currentTurnFactor = 1;


	/**
	 * Create a new interchange play
	 */
	public RobotInterchangePlay()
	{
		super(EPlay.INTERCHANGE);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		calculateBotActions(computeSupportVectorForLineup(getRoles().size()), lineupDirectionVector,
				computeOrientation());
	}


	/**
	 * Compute bot actions based on a line defined by start pos and direction vector
	 *
	 * @param startPos
	 * @param direction
	 * @param orientation
	 */
	@Override
	protected void calculateBotActions(IVector2 startPos, IVector2 direction, double orientation)
	{
		IVector2 dest = startPos.subtractNew(direction);

		List<MoveRole> roles = findRoles(MoveRole.class);
		roles.sort(Comparator.comparing(ARole::getBotID));

		boolean destsReached = true;
		for (MoveRole role : roles)
		{
			role.setVelMaxW(role.getBot().getMoveConstraints().getVelMaxW() * maxVelWFactor);
			role.setAccMaxW(role.getBot().getMoveConstraints().getAccMaxW() * maxAccWFactor);
			do
			{
				dest = dest.addNew(direction);
			} while (!pointChecker.allMatch(getAiFrame().getBaseAiFrame(), dest, role.getBotID()));

			role.updateDestination(dest);
			double tmpOrientation = role.isDestinationReached() ?
					orientation :
					(LOOK_OUTSIDE_FIELD_ANGLE * positionInVision.factor * getMirrorFactor());
			role.updateTargetAngle(tmpOrientation * Math.PI / 180);

			destsReached = destsReached && role.isSkillStateSuccess();
		}
		if (destsReached)
		{
			currentTurnFactor *= -1;
		}
	}


	private int getMirrorFactor()
	{
		return getAiFrame().getRefereeMsg().getNegativeHalfTeam() != getAiFrame().getTeamColor() ? -1 : 1;
	}


	private double computeOrientation()
	{
		return (LOOK_OUTSIDE_FIELD_ANGLE + (maxTurnAngle * currentTurnFactor)) * positionInVision.factor
				* getMirrorFactor();
	}


	private IVector2 computeSupportVectorForLineup(final int nBots)
	{
		double y = (Geometry.getField().maxY() + distanceToFieldLine) * getMirrorFactor();
		double x = ((offsetFactorToCenterline * Geometry.getFieldLength()) / 2)
				+ ((nBots * distanceBetweenBotsInInterchangePosition) / 2);
		return Vector2.fromXY(-x, positionInVision.factor * y);
	}


	@AllArgsConstructor
	enum VisionSide
	{
		POS_Y(1),
		NEG_Y(-1),

		;
		private final int factor;
	}
}
