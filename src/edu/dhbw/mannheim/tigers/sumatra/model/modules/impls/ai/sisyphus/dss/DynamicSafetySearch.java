/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2010
 * Author(s): Bernhard Perun
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.dss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;


/**
 * Dynamic Safety Search checks if a path can be executed safely.
 * @author Bernhard Perun
 */
public class DynamicSafetySearch
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- constants load from aiConfig ---
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final Vector2f		DEC_MAX					= Vector2f.ZERO;	//AIConfig.getDynamicSafetySearch().getMaxDeceleration();
	private final Vector2f		VEL_MAX					= Vector2f.ZERO;	//AIConfig.getDynamicSafetySearch().getMaxVelocity();
	private final float			CIRCLE_TIME				= 0;	//AIConfig.getDynamicSafetySearch().getRandomCirclesMax();
	private final float			EPSILON					= 0;	//AIConfig.getDynamicSafetySearch().getEpsilon();
	private final float			BOT_RADIUS_EXTRA		= 0;	//AIConfig.getDynamicSafetySearch().getBotRadiusExtra();
	private final int				RANDOM_CIRCLES_MAX	= 0;	//AIConfig.getDynamicSafetySearch().getRandomCirclesMax()
	// --- acceleration-variables ---
	private ArrayList<Float>	euklidDistanceAccs	= new ArrayList<Float>(13);
	private ArrayList<Vector2>	commandAccs				= new ArrayList<Vector2>(13);
	private ArrayList<Vector2>	desiredAccs				= new ArrayList<Vector2>(13);
	
	// --- logger ---
	// private Logger log = Logger.getLogger(this.getClass().getName());
	
	// --- current worldframe ---
	private WorldFrame			wFrame					= null;
	
	// --- random generator ---
	Random							randomGenerator		= new Random();
	
	
	// private int counter =0;
	// private long timeA[] = new long[5000];
	// private long maxC=1000;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public DynamicSafetySearch()
	{
		for (int i = 0; i < 13; i++)
		{
			commandAccs.add(null);
			desiredAccs.add(null);
			euklidDistanceAccs.add(null);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Main-method of dynamic safety search.
	 * 
	 * @param errtPath path which the ERRT-Planner returns
	 * @param wFrame current worldframe
	 * @param int botId
	 * @author Bernhard
	 */
	public Path doCalculation(Path errtPath, WorldFrame wFrame, int botId)
	{
		// --- some vars ---
		float botVelocity = 0;
		

		// ### STEP ONE: set deceleration for all tiger-bots ###
		
		// --- sync-block for the first step of DSS ---
		synchronized (this)
		{
			// --- check if a new frame is arriving ---
			if (this.wFrame == null || this.wFrame != wFrame)
			{
				// --- save frame ---
				this.wFrame = wFrame;
				
				// --- set origin deceleration for all tiger-bots ---
				for (ATrackedObject tigerBot : wFrame.tigerBots.values())
				{
					// --- v-vector ---
					botVelocity = AIMath.sqrt((tigerBot.vel.x * tigerBot.vel.x + tigerBot.vel.y * tigerBot.vel.y));
					
					// --- determine if bot is moving and set max deceleration for a hard stop ---
					if (botVelocity >= EPSILON)
					{
						// --- ||v_i_1|| - scaled v-vector ---
						Vector2 vVectorScaled = new Vector2();
						vVectorScaled.x = tigerBot.vel.x;
						vVectorScaled.y = tigerBot.vel.y;
						vVectorScaled.scaleTo(1);
						
						// --- h_i - stop-dec-vector ---
						Vector2 maxDec = new Vector2();
						maxDec.x = -DEC_MAX.x * vVectorScaled.x;
						maxDec.y = -DEC_MAX.y * vVectorScaled.y;
						
						commandAccs.set(botId, maxDec);
					} else
					{
						commandAccs.set(botId, new Vector2(0, 0));
					}
					

					// --- set desiredACC to current bot-ACC ---
					setDesiredBotAcc(wFrame.tigerBots.get(botId));
					

					// --- save euklid distance between desired and command acc ---
					euklidDistanceAccs.set(botId, (float) getEuklidDistance(desiredAccs.get(botId), commandAccs.get(botId)));
				}
				
			}
			
		}
		
		// ### STEP TWO: improve acceleration of the current bot ###
		if (improveAcceleration(botId))
		{
			// log.debug("new acc calculated from DSS-algo:" + commandAccs.get(botId));
			// --- change first point if it is not safe ---
			errtPath.changed = true;
			addPathPoint(wFrame.tigerBots.get(botId), errtPath.path, commandAccs.get(botId));
		}
		
		return errtPath;
	}
	

	/**
	 * Sets the current desired bot acceleration of a robot.
	 * 
	 * @param pathPoint next pathPoint
	 * @param v0 current velocity-vector of the robot
	 * @return next acc-vector
	 */
	private void setDesiredBotAcc(TrackedTigerBot bot)
	{
		
		// --- vars ---
		Vector2 accVector = new Vector2();
		
		// TODO: Einheiten ms/mm !!!
		// --- calculates the acceleration vector of a robot ---
		accVector.x = bot.vel.x / CIRCLE_TIME;
		accVector.y = bot.vel.y / CIRCLE_TIME;
		
		desiredAccs.set(bot.id, accVector);
	}
	

	// --------------------------------------------------------------------------
	// --- sub-methods (high-level) for DSS -------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Fills a pathPoint with the right values.
	 * 
	 * @param newAcc new safe acceleration
	 * @return PathPoint
	 */
	private void addPathPoint(TrackedBot bot, List<IVector2> path, IVector2 newAcc)
	{
		Vector2 pathPoint = new Vector2();
		pathPoint.x = f(bot.pos.x(), bot.vel.x, newAcc.x(), CIRCLE_TIME);
		pathPoint.y = f(bot.pos.y(), bot.vel.y, newAcc.y(), CIRCLE_TIME);
		path.add(0, pathPoint);
		

		List<Vector2> debugPoints = new Vector<Vector2>();
		debugPoints.add(pathPoint);
		
//		for (IAIObserver o1 : Sisyphus.aiObservers)
//		{
//			o1.onNewDebugPoints(debugPoints);
//		}
		
	}
	

	/**
	 * Checks if delivered acceleration is safe for the next time cycle.
	 * 
	 * @param bot object of the current bot
	 * @param acc acceleration which should be checked
	 * @return true=acc is safe; false= acc is NOT safe
	 */
	private boolean checkAcceleration(TrackedBot bot, Vector2 acc)
	{
		// --- check if movement is safe with respect to the configuration space ---
		if (checkSafetyObs(bot, acc) == false)
		{
			// log.info("checkSafetyObs:" + false);
			return false;
		}
		
		// --- check if movement is safe with respect to the robots ---
		for (ATrackedObject obstacle : getObstacleBots(wFrame, bot.id))
		{
			if (checkRobot(bot, acc, (TrackedBot) obstacle, new Vector2(obstacle.acc)) == false)
			{
				// log.debug("collision between bot " + bot.id + " and " + obstacle.id + "!!!");
				return false;
			}
		}
		
		return true;
		
	}
	

	/**
	 * Improves the acceleration-vector if necessary.
	 * 
	 * @param botId
	 * @return if acc-vector was improved for the robot: true ; else: false
	 */
	private boolean improveAcceleration(int botId)
	{
		// --- get robot-object for botId ---
		TrackedBot bot = wFrame.tigerBots.get(botId);
		
		// --- check acc for the given bot ---
		if (checkAcceleration(bot, desiredAccs.get(botId)))
		{
			// --- vector is ok - no improvement necessary -> set commanded acc = desired acc ---
			commandAccs.set(botId, desiredAccs.get(botId));
			euklidDistanceAccs.set(botId, 0.0f);
			// log.debug("acc NOT improved");
			return false;
		} else
		{
			// --- vector is not ok - get random velocity vector with RANDOM_CIRCLES_MAX tries ---
			for (int i = 0; i < RANDOM_CIRCLES_MAX; i++)
			{
				
				Vector2 randomAcc = calcRandomAcc(new Vector2(wFrame.tigerBots.get(botId).vel));
				float randomEuklidDistance = getEuklidDistance(randomAcc, commandAccs.get(botId));
				
				// --- check if randomAcc is better than the current commandAcc and random acc is safe
				if (randomEuklidDistance < euklidDistanceAccs.get(botId) && checkAcceleration(bot, randomAcc))
				{
					// --- set commandAcc = random acceleration + update euklidDistance ---
					commandAccs.set(botId, randomAcc);
					euklidDistanceAccs.set(botId, randomEuklidDistance);
				}
				
			}
			// log.debug("acc improved");
			return true;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- sub-methods (low-level) for DSS --------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Gets the euklid distance of two vectors.
	 * @param p1 vector 1
	 * @param p2 vector 2
	 * @return distance
	 */
	private float getEuklidDistance(Vector2 p1, Vector2 p2)
	{
		return (float) (Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
	}
	

	/**
	 * Calculates a random acc from a given velocity.
	 * Check-mechanism: circle-search around the botVelocity
	 * 
	 * @param botVelocity
	 * @return random acceleration
	 */
	private Vector2 calcRandomAcc(Vector2 botVelocity)
	{
		Vector2 randomVelocity = new Vector2();
		
		// --- generate random numbers between -VEL_MAX to VEL_MAX ---
		randomVelocity.x = randomGenerator.nextFloat();
		randomVelocity.y = randomGenerator.nextFloat();
		randomVelocity.x = (2 * randomVelocity.x * VEL_MAX.x) - VEL_MAX.x;
		randomVelocity.y = (2 * randomVelocity.y * VEL_MAX.y) - VEL_MAX.y;
		
		// --- check if random velocity is within the physical boundaries ---
		
		return randomVelocity;
	}
	

	/**
	 * Checks if a given velocity is within the v_max-boundaries.
	 * Check-mechanism: circle-search
	 * just check if there are performance-probs. Another method would be a lines-check with help of the v_max-rectangle.
	 * But is it really better considering performance???
	 * 
	 * @param velocity which should be checked
	 * @return true=yes, false=no
	 * 
	 *         private boolean checkVelocity(Vector2 centerPoint, Vector2 randomVelocity)
	 *         {
	 *         // --- if random x and y is within the circle => true ---
	 *         if ((Math.abs(centerPoint.x)+Math.abs(randomVelocity.x)) <= VEL_MAX.x &&
	 *         (Math.abs(centerPoint.y)+Math.abs(randomVelocity.y)) <= VEL_MAX.y)
	 *         {
	 *         return true;
	 *         }
	 *         else
	 *         {
	 *         return false;
	 *         }
	 *         }
	 */
	
	/**
	 * Checks if an acceleration is safe for a robot concerning the configuration space.
	 * 
	 * @param bot object of the current bot
	 * @param acc acceleration which should be checked
	 * @return true=acc is safe; false= acc is NOT safe
	 */
	private boolean checkSafetyObs(TrackedBot bot, Vector2 acc)
	{
		// not needed at the moment
		return true;
	}
	

	/**
	 * Checks if an acceleration is safe for a robot concerning to another (obstacle-)robot.
	 * 
	 * @param bot object of the current bot
	 * @param acc acc of the current bot
	 * @param obs object of the obstacle bot
	 * @param obsAcc acc of the obstacle bot
	 * @return true=acc is safe; false= acc is NOT safe
	 */
	private boolean checkRobot(TrackedBot bot, Vector2 acc, TrackedBot obs, Vector2 obsAcc)
	{
		DSSTrajectory trajectoryRobot1 = makeTrajectory(bot, acc);
		DSSTrajectory trajectoryRobot2 = makeTrajectory(obs, obsAcc);
		
		// --- debug - send trajectories ---
		// List<Vector2> debugPoints = new Vector<Vector2>();
		// List<Vector2> debugPoints2 = new Vector<Vector2>();
		
		// Path path = new Path(bot.id);
		
		// debugPoints.add(trajectoryRobot1.parabolic[1].x);
		// debugPoints2.add(trajectoryRobot1.parabolic[2].x);
		
		/*
		 * for (DSSParabolic p :trajectoryRobot1.parabolic)
		 * {
		 * 
		 * path.add(new PathPoint(p.x.x, p.x.y));
		 * }
		 */

		/*
		 * for (DSSParabolic p :trajectoryRobot2.parabolic)
		 * {
		 * debugPoints2.add(p.x);
		 * }
		 */


		/*
		 * for (IAIObserver o1 : Sisyphus.aiObservers)
		 * {
		 * o1.onNewDebugPoints(debugPoints);
		 * o1.onNewDebugPoints2(debugPoints2);
		 * //o1.onNewPath(path);
		 * }
		 */


		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				if (!checkParabolic(trajectoryRobot1.parabolic[i], trajectoryRobot2.parabolic[j], BOT_RADIUS))
				{
					// log.error("parabolic tiger:" + bot.id + " obstacle-bot " + obs.id + " FAILED ");
					return false;
				}
			}
		}
		
		return true;
	}
	

	/**
	 * Checks two parabolics if they are intersecting.
	 * 
	 * @param dssParabolic first parabolic
	 * @param dssParabolic2 second parabolic
	 * @param botRadius radius in mm
	 * @return true=safe; false=not safe
	 */
	private boolean checkParabolic(DSSParabolic dssParabolic1, DSSParabolic dssParabolic2, float botRadius)
	{
		
		// --- determine time interval ---
		float tMin = Math.max(dssParabolic1.time[0], dssParabolic2.time[0]);
		float tMax = Math.min(dssParabolic1.time[1], dssParabolic2.time[1]);
		float t = tMax - tMin;
		
		// log.info("t:" + t);
		
		// --- check if time-interval of parabolics are intersecting ---
		if (t < 0)
		{
			return true;
		}
		

		// --- calc new 0-vectors (points) of parabolics ---
		Vector2 p1 = new Vector2();
		Vector2 p2 = new Vector2();
		p1.x = f(dssParabolic1.x.x, dssParabolic1.v.x, dssParabolic1.a.x, t);
		p1.y = f(dssParabolic1.x.y, dssParabolic1.v.y, dssParabolic1.a.y, t);
		p2.x = f(dssParabolic2.x.x, dssParabolic2.v.x, dssParabolic2.a.x, t);
		p2.y = f(dssParabolic2.x.y, dssParabolic2.v.y, dssParabolic2.a.y, t);
		
		// --- check if a collision occurs at start- or end-time of the trajectory ---
		
		// log.error("d: " + d(p1,p2));
		// log.error("---------------");
		
		if (d(p1, p2) <= 0)
		{
			// log.fatal("d: " + d(p1,p2));
			// log.info("collision occurs");
			return false;
		}
		
		// --- check if a collision occurs from start- to end-time of the trajectory ---
		// REMARK: not like in original-paper -> solved with interpolation (10 values) - not with realRoots
		
		float tEnd = 0;
		for (float tStart = tMin; tStart < tMax - EPSILON; tStart = tStart + (t / 10))
		{
			tEnd = tStart + (t / 10);
			p1.x = f(dssParabolic1.x.x, dssParabolic1.v.x, dssParabolic1.a.x, tStart);
			p1.y = f(dssParabolic1.x.y, dssParabolic1.v.y, dssParabolic1.a.y, tStart);
			p2.x = f(dssParabolic2.x.x, dssParabolic2.v.x, dssParabolic2.a.x, tEnd);
			p2.y = f(dssParabolic2.x.y, dssParabolic2.v.y, dssParabolic2.a.y, tEnd);
			
			if (d(p1, p2) <= 0)
			{
				// log.fatal("d2: " + d(p1,p2));
				// log.info("collision occurs - interpol");
				return false;
			}
		}
		
		return true;
	}
	

	/**
	 * Calculates x after a move of the robot.
	 * 
	 * @param x s before move
	 * @param v vel
	 * @param a acc
	 * @param t time
	 * @return resulting pos-value on field in mm
	 */
	private float f(float x, float v, float a, float t)
	{
		t = t / 1000; // ms -> s
		return (float) (x / 1000 + v * t + 0.5 * a * t * t) * 1000;
	}
	

	/**
	 * Calculates vel after a move of the robot.
	 * 
	 * @param v velocity before move
	 * @param a acc
	 * @param t time
	 * @return resulting vel-value in m/s
	 */
	private float fDeriv(float v, float a, float t)
	{
		t = t / 1000; // ms -> s
		return (float) (v + a * t);
	}
	

	/**
	 * Calculates the distance between two points.
	 * 
	 * @param p1 bot-pos one
	 * @param p2 bot-pos two
	 * @return
	 */
	private float d(Vector2 p1, Vector2 p2)
	{
		return (float) (Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2) - Math.pow(
				2 * (BOT_RADIUS + BOT_RADIUS_EXTRA), 2));
	}
	

	/**
	 * Creates the parabolic-tuples for checkParabolic-calculation.
	 * 
	 * @param bot tracked bot
	 * @param acc desired acceleration
	 * @return DSSTrajectory
	 */
	private DSSTrajectory makeTrajectory(TrackedBot bot, Vector2 acc)
	{
		DSSParabolic[] parabolicArray = new DSSParabolic[3];
		
		// --- x_i_1 - resulting position ---
		Vector2 sVector = new Vector2();
		sVector.x = f(bot.pos.x, bot.vel.x, acc.x, CIRCLE_TIME);
		sVector.y = f(bot.pos.y, bot.vel.y, acc.y, CIRCLE_TIME);
		
		/*
		 * log.info("-----");
		 * log.info("pos:" + bot.pos);
		 * log.info("v:" + bot.vel);
		 * log.info("acc:" + acc);
		 * log.info("sVector:" + sVector);
		 * log.info("-----");
		 */

		// --- v_i_1 - resulting velocity ---
		Vector2 vVector = new Vector2();
		vVector.x = fDeriv(bot.vel.x, acc.x, CIRCLE_TIME);
		vVector.y = fDeriv(bot.vel.y, acc.y, CIRCLE_TIME);
		
		// --- ||v_i_1|| - scaled v-vector ---
		Vector2 vVectorScaled = new Vector2();
		vVectorScaled.x = vVector.x;
		vVectorScaled.y = vVector.y;
		vVectorScaled.scaleTo(1);
		
		// --- h_i - stop-dec-vector ---
		Vector2 stopDecVector = new Vector2();
		stopDecVector.x = -DEC_MAX.x * vVectorScaled.x;
		stopDecVector.y = -DEC_MAX.y * vVectorScaled.y;
		
		// --- t_s - relative time how long it takes till bot comes to v=0 ---
		/*
		 * if (bot.id == 1)
		 * {
		 * log.info("vVector.fAbs: " + vVector.fAbs());
		 * log.info("dec_max: " + DEC_MAX.fAbs());
		 * }
		 */

		// --- tStop in mSec ---
		float tStop = CIRCLE_TIME + 1000 * (vVector.getLength2() / DEC_MAX.getLength2());
		
		// --- x_i2 - distance traveled till bot comes to v=0 ---
		Vector2 sAfterStop = new Vector2();
		sAfterStop.x = f(sVector.x, vVector.x, stopDecVector.x, tStop - CIRCLE_TIME);
		sAfterStop.y = f(sVector.y, vVector.y, stopDecVector.y, tStop - CIRCLE_TIME);
		
		// --- null-vector ---
		Vector2 nullVector = new Vector2(0, 0);
		/*
		 * if (bot.id == 1)
		 * {
		 * log.info("sVector" + sVector);
		 * log.info("sAfterStop" + sAfterStop);
		 * log.info("t" + tStop);
		 * }
		 */
		// --- fill trajectoryArray ---
		parabolicArray[0] = new DSSParabolic(new Vector2(bot.pos), new Vector2(bot.vel), acc, 0, CIRCLE_TIME);
		parabolicArray[1] = new DSSParabolic(sVector, vVector, stopDecVector, CIRCLE_TIME, tStop);
		parabolicArray[2] = new DSSParabolic(sAfterStop, nullVector, nullVector, tStop, tStop + 10000); // tStop+10000 =
																																		// inf
		
		return new DSSTrajectory(parabolicArray);
	}
	

	// --------------------------------------------------------------------------
	// --- helper ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Gets all bots except the main-bot.
	 */
	private List<ATrackedObject> getObstacleBots(WorldFrame wFrame, int botId)
	{
		// --- list ---
		List<ATrackedObject> list = new ArrayList<ATrackedObject>();
		
		// --- get our bots except pf-bot ---
		for (Map.Entry<Integer, TrackedTigerBot> entry : wFrame.tigerBots.entrySet())
		{
			if (entry.getKey().intValue() != botId)
			{
				list.add(entry.getValue());
			}
		}
		
		// --- get foe bots ---
		for (Map.Entry<Integer, TrackedBot> entry : wFrame.foeBots.entrySet())
		{
			list.add(entry.getValue());
		}
		
		return list;
	}
	
}
