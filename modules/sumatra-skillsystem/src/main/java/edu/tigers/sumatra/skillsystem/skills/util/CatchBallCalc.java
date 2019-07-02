/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.MovementCon;
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
	private IVector2 curKickerDest = null;
	private IVector2 lastInterceptionDest = null;
	
	private IVector2 externallySetDestination = null;
	
	private CachedBallInterception cachedBallInterception;
	
	
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
		if (curKickerDest == null)
		{
			curKickerDest = trackedBot.getBotKickerPos();
		}
		if (cachedBallInterception == null)
		{
			cachedBallInterception = new CachedBallInterception(botID);
		}
	}
	
	
	/**
	 * reset state
	 */
	public void reset()
	{
		curKickerDest = null;
		externallySetDestination = null;
		cachedBallInterception = null;
	}
	
	
	/**
	 * @return the result that describes how the robot should catch the ball
	 */
	public CatchBallResult calculate()
	{
		curKickerDest = calcKickerDest();
		double targetOrientation = calcTargetOrientation.apply(curKickerDest);
		IVector2 botDest = kickerDestToBotDest(curKickerDest, targetOrientation);
		
		CatchBallResult result = new CatchBallResult();
		result.kickerDest = curKickerDest;
		result.targetOrientation = targetOrientation;
		result.botDest = botDest;
		
		moveCon.setMinDistToBall(Geometry.getBotRadius() + Geometry.getBallRadius() + 20);
		moveCon.updateTargetAngle(targetOrientation);
		moveCon.updateDestination(botDest);
		moveCon.setBallObstacle(isBallObstacle(curKickerDest));
		
		return result;
	}
	
	
	private IVector2 calcKickerDest()
	{
		IVector2 dest = isBallVerySlow() ? getKickerDestinationForLyingBall() : getKickerDestinationForMovingBall();
		return validatedDestination(dest);
	}
	
	
	private IVector2 validatedDestination(IVector2 destIn)
	{
		IVector2 dest = validateKickerPosNoCollision(destIn, getBallTravelLine());
		return SkillUtil.movePosInsideFieldWrtBall(dest, getBall().getPos());
	}
	
	
	private IVector2 getKickerDestinationForMovingBall()
	{
		double ballToBotDist = getBall().getPos().distanceTo(getTBot().getPos());
		if (ballToBotDist > Geometry.getBotRadius())
		{
			if (lastInterceptionDest == null || ballToBotDist > 500)
			{
				lastInterceptionDest = calcChipInterception().orElseGet(this::calcStraightInterception);
			}
			IVector2 desiredKickerDest = getKickerDestination(getBallTravelLine(), lastInterceptionDest);
			IVector2 botDest = desiredKickerDest
					.addNew(getBallTravelLine().directionVector().scaleToNew(getTBot().getCenter2DribblerDist()));
			if (getBallTravelLine().toHalfLine().isPointInFront(botDest))
			{
				return desiredKickerDest;
			}
		}
		return curKickerDest;
	}
	
	
	private boolean chipDestReachable(final IVector2 chipDest)
	{
		double timeToDest = getBall().getTrajectory().getTimeByPos(chipDest);
		if (cachedBallInterception.getOptimalBallInterception() != null)
		{
			double slackTime = cachedBallInterception.getOptimalBallInterception().slackTime(timeToDest);
			return slackTime > -0.2;
		}
		return false;
	}
	
	
	private Optional<IVector2> calcChipInterception()
	{
		if (!getBall().isChipped())
		{
			return Optional.empty();
		}
		List<IVector2> touchdowns = getBall().getTrajectory().getTouchdownLocations();
		IVector2 touchdownLocation = getTouchdownLocationAfterAtMostTouchdowns(touchdowns, 4);
		if (touchdownLocation != null)
		{
			double dist = Geometry.getBallRadius() + getTBot().getCenter2DribblerDist();
			IVector2 newDest = touchdownLocation.addNew(getBallTravelLine().directionVector().scaleToNew(-dist));
			if (chipDestReachable(newDest))
			{
				return Optional.of(newDest);
			}
		}
		return Optional.empty();
	}
	
	
	private IVector2 getTouchdownLocationAfterAtMostTouchdowns(final List<IVector2> touchdowns, int numTouchdowns)
	{
		return touchdowns.stream().limit(numTouchdowns).reduce((first, second) -> second).orElse(null);
	}
	
	
	private IVector2 calcStraightInterception()
	{
		final IVector2 newDest;
		if (isBehindBall())
		{
			// outrun ball by driving to the end of the ball travel line
			newDest = getBall().getTrajectory().getPosByVel(0).getXYVector();
		} else
		{
			cachedBallInterception.update(getWorldFrame(), getMoveCon().getMoveConstraints());
			double interceptionTime = externallySetDestinationIfReachable()
					.orElse(cachedBallInterception.getInterceptionTime());
			newDest = getBall().getTrajectory().getPosByTime(interceptionTime).getXYVector();
		}
		return newDest;
	}
	
	
	private Optional<Double> externallySetDestinationIfReachable()
	{
		if (externallySetDestination != null && getBallTravelLine().distanceTo(externallySetDestination) < 200)
		{
			double timeToExternallySet = getBall().getTrajectory().getTimeByPos(externallySetDestination);
			double slackTimeAtExternallySet = cachedBallInterception.getOptimalBallInterception()
					.slackTime(timeToExternallySet);
			if (cachedBallInterception.isAcceptableSlackTime(slackTimeAtExternallySet) || slackTimeAtExternallySet > 0)
			{
				return Optional.of(timeToExternallySet);
			}
		}
		return Optional.empty();
	}
	
	
	private boolean isBallVerySlow()
	{
		return getBall().getVel().getLength2() < minBallVel;
	}
	
	
	private IVector2 kickerDestToBotDest(final IVector2 kickerDest, final double targetOrientation)
	{
		IVector2 destination = getDestination(kickerDest, targetOrientation);
		List<IPenaltyArea> penAreas = new ArrayList<>(2);
		if (moveCon.isPenaltyAreaForbiddenOur())
		{
			penAreas.add(Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() * 2));
		}
		if (moveCon.isPenaltyAreaForbiddenTheir())
		{
			penAreas.add(Geometry.getPenaltyAreaTheir().withMargin(getTBot().getCenter2DribblerDist()));
		}
		destination = SkillUtil.movePosOutOfPenAreaWrtBall(destination, getBall(), penAreas);
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
	private IVector2 getKickerDestination(ILineSegment ballLine, final IVector2 newKickerDest)
	{
		IVector2 lp = ballLine.closestPointOnLine(newKickerDest);
		if (externallySetDestination != null && ballLine.toHalfLine().isPointInFront(externallySetDestination)
				&& ballLine.distanceTo(externallySetDestination) < 200)
		{
			IVector2 lpDesired = ballLine.closestPointOnLine(externallySetDestination);
			if (lp.distanceTo(getBall().getPos()) < lpDesired.distanceTo(getBall().getPos()))
			{
				return lpDesired;
			}
		}
		return lp;
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
	
	
	private IVector2 validateKickerPosNoCollision(IVector2 desiredKickerDest, ILineSegment ballLine)
	{
		Optional<ITrackedBot> collidingBot = getWorldFrame().getFoeBots().values().stream()
				.filter(tBot -> tBot.getPos().distanceTo(desiredKickerDest) < Geometry.getBotRadius() * 2)
				.findFirst();
		if (collidingBot.isPresent())
		{
			IVector2 lp = ballLine.closestPointOnLine(collidingBot.get().getPos());
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
	
	
	public void setExternallySetDestination(final IVector2 externallySetDestination)
	{
		this.externallySetDestination = externallySetDestination;
	}
	
	
	private double targetOrientationReceive(final IVector2 kickerPos)
	{
		return getBall().getPos().subtractNew(kickerPos).getAngle(0);
	}
	
	
	private ILineSegment getBallTravelLine()
	{
		ITrackedBall ball = getBall();
		if (ball.getVel().getLength() > 0.1)
		{
			return ball.getTrajectory().getTravelLineSegment();
		}
		return Lines.segmentFromPoints(ball.getPos(), curKickerDest);
	}
	
	
	private IVector2 getKickerDestinationForLyingBall()
	{
		double distanceToBall = curKickerDest.distanceTo(getBall().getPos());
		if (distanceToBall > 300)
		{
			return externallySetDestination != null ? externallySetDestination : curKickerDest;
		} else if (distanceToBall > Geometry.getBallRadius() + 10)
		{
			return LineMath.stepAlongLine(getBall().getPos(), getTBot().getBotKickerPos(),
					Geometry.getBallRadius());
		}
		return curKickerDest;
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
	
	
	public CachedBallInterception getCachedBallInterception()
	{
		return cachedBallInterception;
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
