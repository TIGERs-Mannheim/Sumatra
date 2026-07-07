package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableQuadrilateral;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * Class for calculating the next keeper state.
 */
public class DefendingKeeper
{
	@Configurable(comment = "Ball Speed wher Keeper react on his direction", defValue = "0.1", unit = EConfigUnit.VELOCITY_M)
	private static double blockDecisionVelocity = 0.1;

	@Configurable(comment = "offset to the Sides of the goalposts (BalVelIsDirToGoal State)", defValue = "180.0", unit = EConfigUnit.DISTANCE_MM)
	private static double goalAreaOffset = 180.0;

	@Configurable(comment = "Distance from Opponent to Ball where Keeper beliefs Opponent could be in possession of the ball", defValue = "250.0", unit = EConfigUnit.DISTANCE_MM)
	private static double opponentBotBallPossessionDistance = 250.0;

	@Configurable(comment = "Speed limit of ball of ChipKickState", defValue = "0.8", unit = EConfigUnit.VELOCITY_M)
	private static double chipKickDecisionVelocity = 0.8;

	@Configurable(comment = "Additional area around PE where Opponent with Ball are very dangerous (GoOutState is triggered)", defValue = "300.0", unit = EConfigUnit.DISTANCE_MM)
	private static double ballDangerZone = 300.0;

	@Configurable(comment = "Additional distance to allow until ball reach goal line", defValue = "1000.0", unit = EConfigUnit.DISTANCE_MM)
	private static double distToIntersectionTolerance = 1000;

	@Configurable(comment = "Angle of the search triangle to look for opponent redirectors", defValue = "0.4", unit = EConfigUnit.ANGLE_RAD)
	private static double opponentRedirectorSearchAngle = 0.4;

	static
	{
		ConfigRegistration.registerClass("skills", DefendingKeeper.class);
	}

	private final ASkill drawingSkill;
	private WorldFrame worldFrame;
	private BotID keeperID;


	protected DefendingKeeper(final ASkill drawingSkill)
	{
		this.drawingSkill = drawingSkill;
	}


	protected void update(WorldFrame worldFrame, BotID keeperID)
	{
		this.worldFrame = worldFrame;
		this.keeperID = keeperID;
	}


	private ITrackedBall getBall()
	{
		return worldFrame.getBall();
	}


	private ITrackedBot getBot()
	{
		return worldFrame.getBot(keeperID);
	}


	protected ECriticalKeeperStates calcNextKeeperState()
	{
		// The order in if else represents the priority of the states
		ECriticalKeeperStates nextState;
		if (hasBallContact())
		{
			nextState = ECriticalKeeperStates.HAS_CONTACT;
		} else if (isSomeoneShootingAtOurGoal())
		{
			nextState = ECriticalKeeperStates.INTERCEPT_BALL;
		} else if (isOpponentRedirecting())
		{
			nextState = ECriticalKeeperStates.DEFEND_REDIRECT;
		} else if (isGoOutFeasible())
		{
			nextState = ECriticalKeeperStates.GO_OUT;
		} else
		{
			nextState = ECriticalKeeperStates.NORMAL;
		}
		return nextState;
	}


	private boolean hasBallContact()
	{
		return getBot().getBallContact().getContactDuration() > 0.1;
	}


	private boolean isSomeoneShootingAtOurGoal()
	{
		ILineSegment goalLine = Geometry.getGoalOur()
				.withMargin(0, Geometry.getGoalOur().getWidth() / 2.0).getLineSegment();
		Optional<IVector2> intersect = getBall().getTrajectory().getTravelLineSegments().stream()
				.map(line -> line.intersect(goalLine))
				.flatMap(IIntersections::stream)
				.findAny();

		if (intersect.isPresent() && (getBall().getVel().x() < 0))
		{
			double distToIntersection = intersect.get().distanceTo(getBall().getPos());
			double distWithTolerance = Math.max(0, distToIntersection - distToIntersectionTolerance);
			boolean isBallFastEnough =
					getBall().getTrajectory().getAbsVelByDist(distWithTolerance) > blockDecisionVelocity;
			boolean isBallOnOurSite = getBall().getPos().x() < 0;
			boolean isBallVelocityIntersectingTheGoalLine = Math
					.abs(intersect.get().y()) < ((Geometry.getGoalOur().getWidth() / 2) + goalAreaOffset);

			return isBallOnOurSite
					&& isBallVelocityIntersectingTheGoalLine
					&& isBallFastEnough;
		}
		return false;
	}


	private boolean isOpponentNearBall()
	{
		return worldFrame.getOpponentBots().values().stream()
				.anyMatch(b -> b.getPos().distanceTo(getBall().getPos()) < opponentBotBallPossessionDistance);
	}


	private boolean isOpponentRedirecting()
	{
		return getBestRedirector(worldFrame.getOpponentBots()).isPresent()
				&& getBall().getVel().getLength() > chipKickDecisionVelocity;
	}


	protected Optional<BotID> getBestRedirector(final Map<BotID, ITrackedBot> bots)
	{
		IVector2 ballPos = getBall().getPos();
		IVector2 endPos = getBall().getTrajectory().getPosByVel(0.0).getXYVector();
		var penaltyAreaOur = Geometry.getPenaltyAreaOur().withMargin(-2 * Geometry.getBotRadius());

		return getPotentialRedirectors(bots, endPos).stream()
				.map(bots::get)
				.filter(Objects::nonNull)
				.filter(b -> !penaltyAreaOur.isPointInShapeOrBehind(getRedirectPosition(b.getPos())))
				.min(Comparator.comparingDouble(b -> b.getPos().distanceToSqr(ballPos)))
				.map(ITrackedBot::getBotId);
	}


	protected IVector2 getRedirectPosition(IVector2 redirectorPosition)
	{
		return Lines.halfLineFromDirection(getBall().getPos(), getBall().getVel()).closestPointOnPath(redirectorPosition);
	}


	private List<BotID> getPotentialRedirectors(final Map<BotID, ITrackedBot> bots, final IVector2 endPos)
	{
		List<BotID> filteredBots = new ArrayList<>();
		final double redirectTol = 350.;
		IVector2 ballPos = getBall().getPos();


		IVector2 ballVel = endPos.subtractNew(ballPos);
		if (ballVel.getLength() < 0.4)
		{
			return Collections.emptyList();
		}

		double dist = Math.max(VectorMath.distancePP(ballPos, endPos) - redirectTol, 10);

		var w1 = 2 * 160;
		var w2 = 2 * (SumatraMath.sin(opponentRedirectorSearchAngle / 2) * dist + 260);
		var quad = Quadrilateral.isoscelesTrapezoid(ballPos, w1,
				ballPos.addNew(ballVel.scaleToNew(SumatraMath.cos(opponentRedirectorSearchAngle / 2) * dist)), w2);

		for (Map.Entry<BotID, ITrackedBot> entry : bots.entrySet())
		{
			BotID botID = entry.getKey();
			ITrackedBot tBot = entry.getValue();
			if (tBot == null)
			{
				continue;
			}
			IVector2 pos = tBot.getPos();
			if (quad.isPointInShape(pos))
			{
				filteredBots.add(botID);
			}
			if ((drawingSkill != null) && filteredBots.contains(botID))
			{
				drawingSkill.getShapes().get(ESkillShapesLayer.KEEPER).add(new DrawableCircle(pos, 150, Color.CYAN));
			}
		}

		if (drawingSkill != null)
		{
			drawingSkill.getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableQuadrilateral(quad, new Color(255, 0, 0, 20)).setFill(true));
		}
		return filteredBots;
	}


	public boolean isGoOutFeasible()
	{
		return (isPositionCloseToPenaltyArea(getBall().getPos()) && isOpponentNearBall());
	}


	public boolean isPositionCloseToPenaltyArea(IVector2 position)
	{
		return Geometry.getPenaltyAreaOur().withMargin(ballDangerZone).isPointInShape(position);
	}


	/**
	 * This enum represents the states, the keeper can be in.
	 * They do the following
	 */
	protected enum ECriticalKeeperStates implements IEvent
	{
		/**
		 * Nothing requires the keepers immediate attention.
		 * The keeper should reposition itself between ball and goal to prevent a direct shot.
		 */
		NORMAL,

		/**
		 * A direct shot at the goal has been detected. The keeper shall directly intercept it.
		 */
		INTERCEPT_BALL,

		/**
		 * An offensive redirect has been detected. The shot after this one is intended for the goal.
		 * The keeper shall move to defend this next shot by predicting where it will come from.
		 */
		DEFEND_REDIRECT,

		/**
		 * The opponent has bypassed all other defenses. The keeper is now the only thing standing between goal, opponent and ball.
		 * Standing inside the goal now is not feasible because it cannot defend the entire goal area.
		 * The keeper should instead rush the opponent to force a steep angle for the shot.
		 */
		GO_OUT,

		/**
		 * The keeper has caught the ball.
		 * It should now stand still to prevent the ball from rolling in accidentally
		 */
		HAS_CONTACT,
	}
}
