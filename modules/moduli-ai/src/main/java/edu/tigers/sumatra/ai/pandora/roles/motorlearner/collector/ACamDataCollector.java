/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.INumberListable;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <ResultType>
 */
public abstract class ACamDataCollector<ResultType extends INumberListable> extends ADataCollector<ResultType>
		implements
		IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(ACamDataCollector.class.getName());
	
	
	/**
	 * @param type
	 */
	protected ACamDataCollector(final EDataCollector type)
	{
		super(type);
	}
	
	
	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		onNewCamFrame(frame);
	}
	
	
	protected abstract void onNewCamFrame(ExtendedCamDetectionFrame frame);
	
	
	@Override
	public void start()
	{
		super.start();
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not get WP");
		}
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not get WP");
		}
	}
	
	
	protected Optional<CamRobot> getCamRobot(final ExtendedCamDetectionFrame frame, final BotID botId)
	{
		if (frame == null)
		{
			return Optional.empty();
		}
		final Optional<CamRobot> bot;
		switch (botId.getTeamColor())
		{
			case BLUE:
				bot = frame.getRobotsBlue().stream().filter(b -> b.getRobotID() == botId.getNumber())
						.findAny();
				break;
			case YELLOW:
				bot = frame.getRobotsYellow().stream()
						.filter(b -> b.getRobotID() == botId.getNumber()).findAny();
				break;
			default:
				bot = Optional.empty();
				break;
		}
		return bot;
	}
	
	
	protected IVector3 getVel(final List<CamRobot> robots)
	{
		assert robots.size() > 2;
		
		// List<WeightedObservedPoint> obs = new ArrayList<>();
		// for (int i = 1; i < robots.size(); i++)
		// {
		// double dt = (robots.get(i).getTimestamp() - robots.get(0).getTimestamp()) / 1e9;
		// double angleDiff = AngleMath.difference(robots.get(i).getOrientation(), robots.get(0).getOrientation());
		// obs.add(new WeightedObservedPoint(1, dt, angleDiff));
		// }
		//
		// final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
		// final double[] coeff = fitter.fit(obs);
		// double aVel = coeff[1];
		
		List<Double> aVels = new ArrayList<>(robots.size() - 1);
		for (int i = 1; i < robots.size(); i++)
		{
			double dt2 = (robots.get(i).getTimestamp() - robots.get(i - 1).getTimestamp()) / 1e9;
			double aVel = AngleMath.normalizeAngle(robots.get(i).getOrientation() - robots.get(i - 1).getOrientation())
					/ dt2;
			aVels.add(aVel);
		}
		Collections.sort(aVels);
		double aVel = aVels.get(aVels.size() / 2);
		
		CamRobot newBot = robots.get(robots.size() - 1);
		CamRobot preBot = robots.get(robots.size() / 2);
		CamRobot prePreBot = robots.get(0);
		
		IVector2 velLocal;
		double dt = (newBot.getTimestamp() - prePreBot.getTimestamp()) / 1e9;
		assert dt > 0;
		
		try
		{
			// ILine line = Line.newLine(newBot.getPos(), prePreBot.getPos());
			// double distLine = GeoMath.distancePL(preBot.getPos(), line);
			// double distp0p2 = GeoMath.distancePP(newBot.getPos(), prePreBot.getPos());
			// double rel = distLine / distp0p2;
			
			IVector2 p0p2 = newBot.getPos().subtractNew(prePreBot.getPos());
			IVector2 p0p1 = newBot.getPos().subtractNew(preBot.getPos());
			double angle = GeoMath.angleBetweenVectorAndVector(p0p2, p0p1);
			if (angle < 0.1)
			{
				throw new MathException();
			}
			
			ICircle circle = Circle.circleFromNPoints(robots.stream().map(r -> r.getPos()).collect(Collectors.toList()));
			double angleDiff = AngleMath.difference(newBot.getOrientation(), prePreBot.getOrientation());
			// double angleDiff = aVel * dt;
			double arcLen = circle.radius() * angleDiff;
			
			IVector2 startToCenter = circle.center().subtractNew(prePreBot.getPos());
			IVector2 dir1 = startToCenter.turnNew(AngleMath.PI_HALF).scaleTo(arcLen);
			IVector2 dir2 = startToCenter.turnNew(-AngleMath.PI_HALF).scaleTo(arcLen);
			
			IVector2 startToEnd = newBot.getPos().subtractNew(prePreBot.getPos());
			
			IVector2 dir = dir2;
			if (dir1.subtractNew(startToEnd).getLength2() < dir2.subtractNew(startToEnd).getLength2())
			{
				dir = dir1;
			}
			
			IVector2 compensatedVelGlob = dir.multiplyNew(1.0 / (dt * 1000));
			velLocal = GeoMath.convertGlobalBotVector2Local(compensatedVelGlob,
					prePreBot.getOrientation());
			
			// System.out.println(circle.radius() + " " + angleDiff2 + " " + angleDiff + " " + arcLen + " " + velLocal.x()
			// + " " + aVel);
		} catch (MathException err)
		{
			IVector2 velGlob = newBot.getPos().subtractNew(prePreBot.getPos()).multiply(1.0 / (dt * 1000));
			velLocal = GeoMath.convertGlobalBotVector2Local(velGlob, newBot.getOrientation());
		}
		
		if (velLocal.isFinite() && Double.isFinite(aVel))
		{
			return new Vector3(velLocal, aVel);
		}
		log.warn("infinite values! " + velLocal + " " + aVel);
		return new Vector3(velLocal, aVel);
	}
	
	
	protected IVector3 getVel(final CamRobot prePreBot, final CamRobot preBot, final CamRobot newBot)
	{
		IVector2 velLocal;
		double dt = (newBot.getTimestamp() - prePreBot.getTimestamp()) / 1e9;
		assert dt > 0;
		double aVel = AngleMath.difference(newBot.getOrientation(), prePreBot.getOrientation()) / dt;
		
		try
		{
			ILine line = Line.newLine(newBot.getPos(), prePreBot.getPos());
			double dist = GeoMath.distancePL(preBot.getPos(), line);
			if (dist < 10)
			{
				throw new MathException();
			}
			
			ICircle circle = Circle.circleFrom3Points(prePreBot.getPos(), preBot.getPos(), newBot.getPos());
			double angleDiff = AngleMath.difference(newBot.getOrientation(), prePreBot.getOrientation());
			double arcLen = circle.radius() * angleDiff;
			
			IVector2 startToCenter = circle.center().subtractNew(prePreBot.getPos());
			IVector2 dir1 = startToCenter.turnNew(AngleMath.PI_HALF).scaleTo(arcLen);
			IVector2 dir2 = startToCenter.turnNew(-AngleMath.PI_HALF).scaleTo(arcLen);
			
			IVector2 startToEnd = newBot.getPos().subtractNew(prePreBot.getPos());
			
			IVector2 dir = dir2;
			if (dir1.subtractNew(startToEnd).getLength2() < dir2.subtractNew(startToEnd).getLength2())
			{
				dir = dir1;
			}
			
			IVector2 compensatedVelGlob = dir.multiplyNew(1.0 / (dt * 1000));
			velLocal = GeoMath.convertGlobalBotVector2Local(compensatedVelGlob,
					prePreBot.getOrientation());
		} catch (MathException err)
		{
			IVector2 velGlob = newBot.getPos().subtractNew(prePreBot.getPos()).multiply(1.0 / (dt * 1000));
			velLocal = GeoMath.convertGlobalBotVector2Local(velGlob, newBot.getOrientation());
		}
		
		if (velLocal.isFinite() && Double.isFinite(aVel))
		{
			return new Vector3(velLocal, aVel);
		}
		log.warn("infinite values! " + velLocal + " " + aVel);
		return new Vector3(velLocal, aVel);
	}
}
