/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CatchBallCalc
{
	@Configurable(comment = "for balls moving towards me -> switchToRedirect", defValue = "0.5")
	private static double minBallVel = 0.5;
	
	static
	{
		ConfigRegistration.registerClass("skills", CatchBallCalc.class);
	}
	
	private final BotID botID;
	private MovementCon moveCon;
	
	private ITrackedBot trackedBot;
	private WorldFrame wFrame;
	
	private Function<IVector2, Double> calcTargetOrientation = this::targetOrientationReceive;
	private IVector2 curDesDest = null;
	
	private IVector2 desiredDestination = null;
	private TimestampTimer stillTimer = new TimestampTimer(0.2);
	
	
	/**
	 * @param botID the bot to use for catching the ball
	 */
	public CatchBallCalc(final BotID botID)
	{
		this.botID = botID;
	}
	
	
	/**
	 * Update with a new frame
	 *
	 * @param wFrame the current world frame
	 */
	public void update(final WorldFrame wFrame)
	{
		trackedBot = wFrame.getBot(botID);
		this.wFrame = wFrame;
		if (curDesDest == null)
		{
			curDesDest = trackedBot.getBotKickerPos();
		}
	}
	
	
	/**
	 * reset state
	 */
	public void reset()
	{
		curDesDest = null;
		desiredDestination = null;
		stillTimer.reset();
	}
	
	
	/**
	 * @return the result that describes how the robot should catch the ball
	 */
	public CatchBallResult calculate()
	{
		calcKickerDest();
		double targetOrientation = calcTargetOrientation();
		IVector2 destination = calcBotDestination(targetOrientation);
		
		CatchBallResult result = new CatchBallResult();
		result.kickerDest = curDesDest;
		result.targetOrientation = targetOrientation;
		result.botDest = destination;
		moveCon.setMinDistToBall(Geometry.getBotRadius() + Geometry.getBallRadius() + 20);
		moveCon.updateTargetAngle(targetOrientation);
		moveCon.updateDestination(destination);
		return result;
	}
	
	
	private void calcKickerDest()
	{
		if (isBallVerySlow())
		{
			curDesDest = getKickerDestinationForLyingBall();
		} else
		{
			getKickerDestinationForMovingBall();
			
		}
		
		validateDestination();
	}
	
	
	private void validateDestination()
	{
		curDesDest = validateKickerPosNoCollision(curDesDest, getBallTravelLine());
		curDesDest = SkillUtil.movePosInsideFieldWrtBall(curDesDest, getBall().getPos());
	}
	
	
	private void getKickerDestinationForMovingBall()
	{
		IVector2 chipDest = handleChippedBall();
		IVector2 straightDest = handleStraightBall();
		IVector2 newDest = chipDestIsUnreachable(chipDest, straightDest) ? straightDest : chipDest;
		
		stillTimer.reset();
		IVector2 desiredKickerDest = getKickerDestination(getBallTravelLine(), newDest);
		IVector2 botDest = desiredKickerDest
				.addNew(getBallTravelLine().directionVector().scaleToNew(getTBot().getCenter2DribblerDist()));
		if (getBallTravelLine().isPointInFront(botDest))
		{
			curDesDest = desiredKickerDest;
		}
	}
	
	
	private boolean chipDestIsUnreachable(final IVector2 chipDest, final IVector2 straightDest)
	{
		return getBall().getPos().distanceTo(straightDest) > getBall().getPos().distanceTo(chipDest);
	}
	
	
	private IVector2 handleChippedBall()
	{
		if (!getBall().isChipped())
		{
			return getBall().getPos();
		}
		final IVector2 newDest;
		List<IVector2> touchdowns = getBall().getTrajectory().getTouchdownLocations();
		IVector2 touchdownLocation = touchdowns.stream().limit(4).reduce((first, second) -> second).orElse(null);
		if (touchdownLocation != null)
		{
			double dist = Geometry.getBallRadius() + getTBot().getCenter2DribblerDist();
			newDest = touchdownLocation.addNew(getBallTravelLine().directionVector().scaleToNew(-dist));
		} else
		{
			newDest = handleStraightBall();
		}
		return newDest;
	}
	
	
	private IVector2 handleStraightBall()
	{
		final IVector2 newDest;
		if (isBehindBall())
		{
			// outrun ball by driving to the end of the ball travel line
			newDest = getBall().getTrajectory().getPosByVel(0);
		} else
		{
			Optional<Double> interceptTime = BallInterceptor.aBallInterceptor()
					.withBallTrajectory(getBall().getTrajectory())
					.withMoveConstraints(getMoveCon().getMoveConstraints())
					.withTrackedBot(getTBot())
					.build()
					.optimalTimeIfReasonable();
			
			if (!interceptTime.isPresent() && !getBallTravelLine().isPointOnLineSegment(curDesDest))
			{
				newDest = getBallTravelLine().nearestPointOnLineSegment(getTBot().getBotKickerPos());
			} else
			{
				// intercept the ball normally
				// catch the nearby ball
				newDest = interceptTime.map(aDouble -> getBall().getTrajectory().getPosByTime(aDouble))
						.orElseGet(this::getKickerDestinationForCatchBall);
			}
		}
		return newDest;
	}
	
	
	private double calcTargetOrientation()
	{
		final double targetOrientation;
		if (isBallVerySlow())
		{
			getMoveCon().setBallObstacle(false);
			if (getBall().getVel().getLength2() < 0.1)
			{
				targetOrientation = getBall().getPos().subtractNew(getTBot().getPos()).getAngle();
			} else
			{
				targetOrientation = calcTargetOrientation.apply(curDesDest);
			}
		} else
		{
			getMoveCon().setBallObstacle(isBallObstacle(curDesDest));
			targetOrientation = calcTargetOrientation.apply(curDesDest);
		}
		return targetOrientation;
	}
	
	
	private boolean isBallVerySlow()
	{
		return getBall().getVel().getLength2() < minBallVel;
	}
	
	
	private IVector2 calcBotDestination(final double targetOrientation)
	{
		IVector2 destination = getDestination(curDesDest, targetOrientation);
		destination = SkillUtil.movePosOutOfPenAreaWrtBall(destination, getBall(),
				Geometry.getPenaltyAreaTheir().withMargin(getTBot().getCenter2DribblerDist()),
				Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2));
		return destination;
	}
	
	
	private IVector2 getDestination(IVector2 destKickerPos, double targetAngle)
	{
		return BotShape.getCenterFromKickerPos(destKickerPos, targetAngle,
				getTBot().getCenter2DribblerDist() + Geometry.getBallRadius());
	}
	
	
	/**
	 * Note for future changes: Do not make the destination depend on the current robot position! This will
	 * cause oscillation!
	 *
	 * @param ballLine
	 * @param newKickerDest
	 * @return
	 */
	private IVector2 getKickerDestination(ILine ballLine, final IVector2 newKickerDest)
	{
		IVector2 lp = ballLine.leadPointOf(newKickerDest);
		if (desiredDestination != null && ballLine.isPointInFront(desiredDestination)
				&& ballLine.distanceTo(desiredDestination) < 200)
		{
			IVector2 lpDesired = ballLine.leadPointOf(desiredDestination);
			if (lp.distanceTo(getBall().getPos()) < lpDesired.distanceTo(getBall().getPos()))
			{
				return lpDesired;
			}
		}
		return lp;
	}
	
	
	private IVector2 getKickerDestinationForCatchBall()
	{
		return getBallTravelLine().leadPointOf(curDesDest);
	}
	
	
	private boolean isBehindBall()
	{
		IVector2 bot2Ball = getTBot().getPos().subtractNew(getBall().getPos());
		double angle = bot2Ball.angleToAbs(getBall().getVel()).orElse(0.0);
		return angle > AngleMath.PI_HALF;
	}
	
	
	private boolean isBallObstacle(IVector2 desiredKickerDest)
	{
		return getBall().getVel().getLength2() > 0.1
				&& isBehindBall()
				// destination far from ball -> else we want to allow the ball to hit us ;)
				&& desiredKickerDest.distanceTo(getBall().getPos()) > 120;
	}
	
	
	private IVector2 validateKickerPosNoCollision(IVector2 desiredKickerDest, ILine ballLine)
	{
		Optional<ITrackedBot> collidingBot = getWorldFrame().getFoeBots().values().stream()
				.filter(tBot -> tBot.getPos().distanceTo(desiredKickerDest) < Geometry.getBotRadius() * 2)
				.findFirst();
		if (collidingBot.isPresent())
		{
			IVector2 lp = ballLine.leadPointOf(collidingBot.get().getPos());
			return LineMath.stepAlongLine(lp, ballLine.getStart(), Geometry.getBotRadius() * 2);
		}
		return desiredKickerDest;
	}
	
	
	public MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	public void setMoveCon(final MovementCon moveCon)
	{
		this.moveCon = moveCon;
	}
	
	
	public void setCalcTargetOrientation(final Function<IVector2, Double> calcTargetOrientation)
	{
		this.calcTargetOrientation = calcTargetOrientation;
	}
	
	
	public void setDesiredDestination(final IVector2 desiredDestination)
	{
		this.desiredDestination = desiredDestination;
	}
	
	
	private double targetOrientationReceive(final IVector2 kickerPos)
	{
		return getBall().getPos().subtractNew(kickerPos).getAngle(0);
	}
	
	
	private ILine getBallTravelLine()
	{
		ITrackedBall ball = getBall();
		if (ball.getVel().getLength() > 0.1)
		{
			return ball.getTrajectory().getTravelLinesInterceptable().get(0);
		}
		return Line.fromPoints(ball.getPos(), curDesDest);
	}
	
	
	private IVector2 getKickerDestinationForLyingBall()
	{
		stillTimer.update(getWorldFrame().getTimestamp());
		if (desiredDestination != null && stillTimer.isTimeUp(getWorldFrame().getTimestamp()))
		{
			curDesDest = desiredDestination;
		}
		return curDesDest;
	}
	
	
	public static double getMinBallVel()
	{
		return minBallVel;
	}
	
	
	private ITrackedBot getTBot()
	{
		return trackedBot;
	}
	
	
	private ITrackedBall getBall()
	{
		return wFrame.getBall();
	}
	
	
	private WorldFrame getWorldFrame()
	{
		return wFrame;
	}
	
	
	/**
	 * Data structure representing the result of this calculator
	 */
	public static class CatchBallResult
	{
		private IVector2 kickerDest;
		private IVector2 botDest;
		private double targetOrientation;
		
		
		public IVector2 getKickerDest()
		{
			return kickerDest;
		}
		
		
		public IVector2 getBotDest()
		{
			return botDest;
		}
		
		
		public double getTargetOrientation()
		{
			return targetOrientation;
		}
	}
}
