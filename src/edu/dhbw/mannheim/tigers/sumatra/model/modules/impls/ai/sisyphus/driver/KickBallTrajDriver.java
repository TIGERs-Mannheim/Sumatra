/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.CatchBallInput;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableText;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.CatchBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.SimpleTimeAwareBallObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.AsyncExecution;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickBallTrajDriver extends TrajPathDriver implements IKickPathDriver
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(KickBallTrajDriver.class.getName());
	
	private final DynamicPosition	receiver;
	private CatchBallInput			inputKick;
	private CatchBallInput			inputStop;
	private boolean					receiving		= false;
	private float						shootSpeed		= 8;
	
	private AsyncExecution			asynExecution	= null;
	private boolean					penAreaAllowed	= false;
	
	
	/**
	 * @param receiver
	 */
	public KickBallTrajDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
	}
	
	
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		if (asynExecution == null)
		{
			asynExecution = new AsyncExecution(bot.getTeamColor());
		}
		asynExecution.executeAsynchronously(() -> calcPath(bot, wFrame));
		setPath(bot.getBot().getPathFinder().getCurPath());
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.KICK_TRAJ;
	}
	
	
	@Override
	public boolean isReceiving()
	{
		return receiving;
	}
	
	
	@Override
	public void setShootSpeed(final float shootSpeed)
	{
		this.shootSpeed = shootSpeed;
	}
	
	
	private void calcPath(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		if (inputKick == null)
		{
			inputKick = new CatchBallInput(bot, wFrame.getBall(), receiver);
		}
		if (inputStop == null)
		{
			inputStop = new CatchBallInput(bot, wFrame.getBall(), receiver);
		}
		
		// we do not want to force a path, as we do not want to tackle the ball. We rather wait a bit.
		inputKick.getFinderInput().setForcePathAfter(0.5f);
		inputKick.getFinderInput().setMaxSubPoints(2);
		inputKick.getFinderInput().setNumNodes2TryPerIteration(5);
		inputKick.getFinderInput().setNumPoints2TryOnTraj(10);
		inputKick.getFinderInput().setTrajOffset(1f);
		inputKick.setReceiver(receiver);
		inputKick.getObsGen().setUsePenAreaOur(penAreaAllowed);
		
		// we do not want to force a path, as we do not want to tackle the ball. We rather wait a bit.
		inputStop.getFinderInput().setForcePathAfter(0.1f);
		inputStop.getObsGen().setUsePenAreaOur(penAreaAllowed);
		inputStop.getFinderInput().setTrajOffset(0.2f);
		inputStop.getFinderInput().setMaxSubPoints(2);
		inputStop.getFinderInput().setNumNodes2TryPerIteration(5);
		inputStop.getFinderInput().setNumPoints2TryOnTraj(10);
		inputStop.getFinderInput().setTrajOffset(1f);
		
		// inputKick.getFinderInput().setDebug(true);
		
		List<IDrawableShape> shapes = new ArrayList<>();
		List<IDrawableShape> debugShapes = new ArrayList<>();
		
		float vBallCur = wFrame.getBall().getVel().getLength2();
		boolean validAngle = vBallCur < 0.5f;
		for (float vBall = 0; !validAngle && (vBall < wFrame.getBall().getVel().getLength2()); vBall += 0.5f)
		{
			IVector2 pBall = wFrame.getBall().getPosByVel(vBall);
			float angle = AiMath.calcRedirectAngle(pBall, receiver, wFrame.getBall().getVel(), shootSpeed);
			
			
			float approxOrient = 0;
			if (!pBall.equals(receiver))
			{
				approxOrient = receiver.subtractNew(pBall).getAngle();
			}
			float targetAngle = AiMath.calcRedirectOrientation(pBall, approxOrient, wFrame.getBall().getVel(), receiver,
					shootSpeed);
			
			float ballAngle;
			if (wFrame.getBall().getVel().isZeroVector())
			{
				ballAngle = targetAngle;
			} else
			{
				ballAngle = AngleMath.normalizeAngle(wFrame.getBall().getVel().getAngle() + AngleMath.PI);
			}
			
			debugShapes.add(new DrawableText(pBall.addNew(new Vector2(-50, 0)), angle + ">?"
					+ inputKick.getMaxAngle(), Color.magenta));
			debugShapes.add(new DrawableBot(pBall.addNew(new Vector2(targetAngle + AngleMath.PI).scaleTo(90)),
					targetAngle,
					Color.red));
			debugShapes.add(new DrawableLine(new Line(pBall, new Vector2(ballAngle).scaleTo(200)), Color.blue));
			debugShapes.add(new DrawableLine(new Line(pBall, wFrame.getBall().getVel().multiplyNew(1000)), Color.RED));
			debugShapes.add(new DrawableLine(Line.newLine(pBall, receiver), Color.black, false));
			
			if (angle < inputKick.getMaxAngle())
			{
				validAngle = true;
				break;
			}
		}
		
		final CatchBallInput input;
		if (!validAngle)
		{
			// ball is rolling and angles are bad for redirecting, so try to stop the ball first by getting behind it.
			input = inputStop;
			// ball is receiver. This helps in having correct orientation in catchBall.
			// small hack: set it a little bit behind ball pos to avoid having dest==receiver which results in errors for
			// angle calculation
			IVector2 receiverBall = GeoMath.stepAlongLine(wFrame.getBall().getPos(), bot.getPos(), -100);
			input.setReceiver(new DynamicPosition(receiverBall));
			receiving = true;
		}
		else
		{
			input = inputKick;
			receiving = false;
		}
		
		input.setDebug(false);
		input.setBot(bot);
		input.setBall(wFrame.getBall());
		input.setShapes(debugShapes);
		input.getFinderInput().setDebugShapes(debugShapes);
		
		debugShapes.add(new DrawableText(bot.getPos().addNew(new Vector2(200, 0)), receiving ? "Receiving"
				: "Redirecting",
				Color.RED));
		
		TrajPath path = null;
		
		// if the ball is out of field, do nothing. This helps during tests and is more secure
		if (AIConfig.getGeometry().getField().isPointInShape(wFrame.getBall().getPos()))
		{
			path = AiMath.catchBallTrajPath(input);
			
			if ((path == null))
			{
				log.debug("No path found in catchBallTrajPath!");
				float toBallDist = (AIConfig.getGeometry().getBallRadius() + bot.getBot().getCenter2DribblerDist()) - 10;
				final IVector2 dest;
				if (receiving && (wFrame.getBall().getVel().getLength2() > 0.5f))
				{
					// position behind ball, because may stand in the way otherwise
					dest = GeoMath.stepAlongLine(wFrame.getBall().getPosByVel(0), wFrame.getBall().getPos(),
							-toBallDist);
				} else
				{
					// redirect ball at the end.
					dest = GeoMath.stepAlongLine(wFrame.getBall().getPosByVel(0.5f), receiver, -toBallDist);
				}
				// calculate a path to our destination, if possible
				List<IObstacle> obstacles = input.getObsGen().generateStaticObstacles();
				CatchBallObstacle catchBallObs = new CatchBallObstacle(wFrame.getBall(), toBallDist - 10, receiver,
						shootSpeed);
				obstacles.add(catchBallObs);
				obstacles.add(new SimpleTimeAwareBallObstacle(wFrame.getBall(), toBallDist));
				input.getFinderInput().setTrackedBot(bot);
				input.getFinderInput().setDest(dest);
				// input.getFinderInput().setObstacles(obstacles);
				input.getFinderInput().setForcePathAfter(0);
				bot.getBot().getPathFinder().calcPath(input.getFinderInput());
				input.getFinderInput().setForcePathAfter(Float.MAX_VALUE);
			}
		}
		
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 ballVel = wFrame.getBall().getVel();
		debugShapes.add(new DrawableLine(Line.newLine(ballPos.subtractNew(ballVel.scaleToNew(3000)),
				ballPos.addNew(ballVel.scaleToNew(3000))
				), Color.red, false));
		
		setShapes(shapes);
		setShapesDebug(debugShapes);
		
		// if ((path != null) && ((curPath == null) || (curPath.getRndId() != path.getRndId())))
		// {
		// log.info("Kick path: \n" + path);
		// }
	}
	
	
	@Override
	public void setPenAreaAllowed(final boolean allowed)
	{
		penAreaAllowed = allowed;
	}
	
	
	@Override
	public boolean armKicker()
	{
		return true;
	}
}
