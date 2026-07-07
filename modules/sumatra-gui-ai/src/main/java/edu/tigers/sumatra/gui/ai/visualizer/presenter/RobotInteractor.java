package edu.tigers.sumatra.gui.ai.visualizer.presenter;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.util.Objects;


public class RobotInteractor
{
	@Configurable(comment = "Consider physical obstacles only", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean physicalObstacles = true;

	@Configurable(comment = "Ball is obstacle for point'n click", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean ballObstacle = true;

	@Configurable(comment = "Bots are obstacle for point'n click", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean botsObstacle = true;

	@Configurable(comment = "Ball is obstacle for point'n click", defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean penaltyAreaObstacle = false;

	static
	{
		ConfigRegistration.registerClass("user", RobotInteractor.class);
	}

	private boolean mouseMoveUpdateDestinationMode = false;
	private MoveToSkill skill;


	public void onMouseMove(BotID selectedRobotId, IVector2 pos)
	{
		if (mouseMoveUpdateDestinationMode && skill != null && Objects.equals(skill.getBotId(), selectedRobotId))
		{
			if (!Geometry.getNegativeHalfTeam().equals(skill.getBotId().getTeamColor()))
			{
				skill.updateDestination(Vector2.fromXY(-pos.x(), -pos.y()));
			} else
			{
				skill.updateDestination(pos);
			}
		}
	}

	public void onRobotMove(BotID selectedRobotId, IVector2 pos)
	{
		skill = createNewMoveToSkill();
		IVector2 adjustedPos = adjustPosForTeam(selectedRobotId, pos);
		mouseMoveUpdateDestinationMode = false;
		skill.updateDestination(adjustedPos);
		execute(selectedRobotId, skill);
	}

	public void onFieldClick(BotID selectedRobotId, IVector2 pos, MouseEvent mouseEvent)
	{
		if (selectedRobotId.isUninitializedID() || !SwingUtilities.isLeftMouseButton(mouseEvent))
		{
			return;
		}

		skill = createNewMoveToSkill();
		IVector2 adjustedPos = adjustPosForTeam(selectedRobotId, pos);
		mouseMoveUpdateDestinationMode = false;

		boolean ctrl = mouseEvent.isControlDown();
		boolean shift = mouseEvent.isShiftDown();
		boolean alt = mouseEvent.isAltDown();
		if (ctrl && shift)
		{
			mouseMoveUpdateDestinationMode = true;
			skill.updateDestination(adjustedPos);
			execute(selectedRobotId, skill);
		} else if (ctrl)
		{
			// move there and look at the ball
			skill.updateDestination(adjustedPos);
			skill.updateLookAtTarget(new DynamicPosition(BallID.instance()));
			execute(selectedRobotId, skill);
		} else if (shift)
		{
			KickParams kickParams = alt ? KickParams.maxChip() : KickParams.maxStraight();
			SingleTouchKickSkill kickSkill = new SingleTouchKickSkill(adjustedPos, kickParams);
			execute(selectedRobotId, kickSkill);
		} else
		{
			skill.updateDestination(adjustedPos);
			execute(selectedRobotId, skill);
		}
	}


	private MoveToSkill createNewMoveToSkill()
	{
		MoveToSkill moveToSkill = MoveToSkill.createMoveToSkill();
		MovementCon moveCon = moveToSkill.getMoveCon();
		if (physicalObstacles)
		{
			moveCon = moveCon.withPhysicalObstaclesOnly();
		}
		moveToSkill.setMoveCon(moveCon
				.withPenaltyAreaOurObstacle(penaltyAreaObstacle)
				.withPenaltyAreaTheirObstacle(penaltyAreaObstacle)
				.withBallObstacle(ballObstacle)
				.withBotsObstacle(botsObstacle));
		return moveToSkill;
	}


	private IVector2 adjustPosForTeam(BotID botID, IVector2 pos)
	{
		if (!Geometry.getNegativeHalfTeam().equals(botID.getTeamColor()))
		{
			return pos.multiplyNew(-1);
		}
		return pos;
	}


	private void execute(BotID botID, ISkill skill)
	{
		SumatraModel.getInstance().getModuleOpt(ASkillSystem.class).ifPresent(
				skillSystem -> skillSystem.execute(botID, skill)
		);
	}
}
