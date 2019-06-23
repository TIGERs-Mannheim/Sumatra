/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.defense.DefenseConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.util.redirect.RedirectBallModel;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class ADefenseRole extends ARole implements IDefenderRole
{
	
	@Configurable(comment = "Dribbling speed for defenders", defValue = "10000.0")
	private static double dribblerSpeed = 10_000;
	@Configurable(comment = "Kicker speed to chip the ball", defValue = "4.0")
	private static double configurableKickSpeed = 4;
	
	
	/**
	 * @param type of the role
	 */
	public ADefenseRole(final ERole type)
	{
		super(type);
	}
	
	
	@Override
	public ILineSegment getProtectionLine(final ILineSegment threatPos)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector2 getValidPositionByIcing(final IVector2 pos)
	{
		if (getAiFrame().getTacticalField().isOpponentWillDoIcing())
		{
			ITube forbiddenZone = Tube.fromLine(getBall().getTrajectory().getTravelLine(),
					Geometry.getBotToBallDistanceStop());
			if (forbiddenZone.isPointInShape(pos))
			{
				IVector2 possiblePos = forbiddenZone.nearestPointOutside(pos);
				if (Geometry.getPenaltyAreaOur().withMargin(DefenseConstants.getMinGoOutDistance())
						.isPointInShape(possiblePos))
				{
					IVector2 leadPoint = Line.fromPoints(forbiddenZone.startCenter(), forbiddenZone.endCenter())
							.nearestPointOnLine(possiblePos);
					possiblePos = LineMath.stepAlongLine(possiblePos, leadPoint, forbiddenZone.radius() * 2);
				}
				return possiblePos;
			}
		}
		return pos;
	}
	
	
	protected void armDefenders(AMoveSkill skill)
	{
		final double kickSpeed = calculateArmChipSpeedDuringDefense();
		skill.getMoveCon().setArmChip(kickSpeed);
		if (kickSpeed > 0)
		{
			skill.getMoveCon().setDribblerSpeed(dribblerSpeed);
		}
	}
	
	
	private double calculateArmChipSpeedDuringDefense()
	{
		IVector2 resultingKick = RedirectBallModel.getDefaultInstance().kickerRedirect(getBall().getVel(),
				Vector2.fromAngle(getBot().getOrientation()), 8);
		IHalfLine ballTravel = Lines.halfLineFromDirection(getBot().getPos(), resultingKick);
		
		Optional<IVector2> target;
		target = ballTravel.intersectSegment(Lines.segmentFromLine(Geometry.getGoalTheir().getLine()));
		if (target.isPresent())
		{
			return 8;
		}
		target = ballTravel.intersectSegment(getGoalLineTheir());
		if (target.isPresent())
		{
			return configurableKickSpeed;
		}
		return 0;
	}
	
	
	private ILineSegment getGoalLineTheir()
	{
		IVector2 goalCenter = Geometry.getGoalTheir().getCenter();
		double fieldWidthHalfY = Geometry.getFieldWidth() / 2;
		IVector2 halfField = Vector2.fromXY(0, fieldWidthHalfY);
		return Lines.segmentFromPoints(goalCenter.subtractNew(halfField), goalCenter.addNew(halfField));
	}
}
