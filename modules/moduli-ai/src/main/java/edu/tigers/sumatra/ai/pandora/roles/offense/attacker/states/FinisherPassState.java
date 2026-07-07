package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.PassInterceptionMovingRobotRater;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DragBallSkill;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DribbleKickSkill;

import java.awt.Color;
import java.util.Collections;
import java.util.stream.Stream;


public class FinisherPassState extends AAttackerRoleState<DribbleKickSkill>
{
	@Configurable(defValue = "2.0", comment = "if a dribbling violation is imminent and we do not roughly look towards opponent goal", unit = EConfigUnit.VELOCITY_M)
	private static double violationImminentKickSpeed = 2.0;


	public FinisherPassState(AttackerRole role)
	{
		super(DribbleKickSkill::new, role, EAttackerState.FINISHER_PASS);
	}


	@Override
	protected void doStandardUpdate()
	{
		assert getRole().getAction().getDribbleToPos() != null;
		assert getRole().getAction().getPass() != null;
		var pass = getRole().getAction().getPass();

		getRole().getShapes(EAiShapesLayer.OFFENSE_FINISHER_PASS)
				.add(DrawableArrow.fromPositions(
						pass.getPass().getKick().getSource(),
						pass.getPass().getKick().getTarget()
				).setColor(Color.MAGENTA).setStrokeWidth(4));

		skill.setDestination(getRole().getAction().getDribbleToPos().getDribbleToDestination());
		skill.setTarget(getRole().getAction().getPass().getPass().getKick().getTarget());
		skill.setDefaultKickSpeed(pass.getPass().getKick().getKickParams().getKickSpeed());
		skill.setKickIfTargetOrientationReached(pass.getScore(EPassRating.INTERCEPTION) > 0.2);

		skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.SAFE));

		// handle dribbling violations
		var dribblingInformation = getRole().getAiFrame().getTacticalField().getDribblingInformation();
		boolean needToKick = dribblingInformation.isDribblingInProgress() && dribblingInformation.isViolationImminent();
		if (needToKick)
		{
			skill.setForceKickSpeed(violationImminentKickSpeed);
			skill.setForceDribblerOff(true);
		}
	}
}
