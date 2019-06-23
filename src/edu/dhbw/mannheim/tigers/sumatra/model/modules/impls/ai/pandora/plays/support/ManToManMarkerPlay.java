/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * This Play handles n {@link ManToManMarkerRole}s to mark the most dangerous enemy bots (without ball carrier)
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 * @author Simon Sander <mail@simon-sander.de>
 */
public class ManToManMarkerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger				log				= Logger.getLogger(ManToManMarkerPlay.class.getName());
	
	private static final long					TIME_TO_CHANGE	= 100;
	
	private final List<ManToManMarkerRole>	roles				= new ArrayList<ManToManMarkerRole>();
	
	private List<BotID>							foeBotsSorted	= null;
	
	private List<BotID>							foeBots			= new ArrayList<BotID>();
	
	private long									time				= System.currentTimeMillis();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public ManToManMarkerPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		// create ArrayList<BotID> including all enemy bots.
		for (Entry<BotID, TrackedBot> foeBot : aiFrame.worldFrame.foeBots)
		{
			foeBots.add(foeBot.getKey());
		}
		
		foeBotsSorted = sortFoeBotsOrderedByGradeOfDanger(aiFrame);
		
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			ManToManMarkerRole role = new ManToManMarkerRole(new Vector2());
			roles.add(role);
			if (i < foeBotsSorted.size())
			{
				// each role gets a foe
				TrackedBot foeBot = aiFrame.worldFrame.foeBots.get(foeBotsSorted.get(i));
				addCreativeRole(role, foeBot.getPos());
			} else
			{
				// just for security. Too less enemies for num roles, play will be canceled soon
				addCreativeRole(role, new Vector2(0, 0));
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if (foeBotsSorted.equals(sortFoeBotsOrderedByGradeOfDanger(frame)))
		{
			time = System.currentTimeMillis();
		}
		
		if ((System.currentTimeMillis() - time) > TIME_TO_CHANGE)
		{
			time = System.currentTimeMillis();
			
			foeBotsSorted = sortFoeBotsOrderedByGradeOfDanger(frame);
			
			// Finish play, if more roles, than potential enemies.
			if (foeBotsSorted.size() < getNumAssignedRoles())
			{
				log.warn("ManToManMarkerPlay is selected with more roles than foes");
				log.warn("Not enaugh enemies for this Play! tiger roles: " + getNumAssignedRoles()
						+ (" enemies: " + foeBotsSorted.size()));
				changeToFinished();
				return;
			}
			
			final int roleOverflow = foeBotsSorted.size() - getNumAssignedRoles();
			// make size of foeBotsSorted equal to numAssignedRoles
			for (int i = 0; i < roleOverflow; i++)
			{
				// delete from not dangerous to extremly dangerous
				foeBotsSorted.remove(foeBotsSorted.size() - 1);
			}
		}
		
		// each role gets a foe
		// role nearest to the enemy, that has to be marked gets the enemy bot assigned.
		
		IBotIDMap<TrackedBot> rolesWaiting = BotIDMap.createBotIDMapFoes(frame.worldFrame.foeBots, foeBotsSorted);
		for (ManToManMarkerRole role : roles)
		{
			if (rolesWaiting.size() < 1)
			{
				break;
			}
			TrackedBot foeBot = AiMath.getNearestBot(rolesWaiting, role.getPos());
			role.updateTarget(foeBot);
			rolesWaiting.remove(foeBot.getId());
		}
	}
	
	
	private List<BotID> sortFoeBotsOrderedByGradeOfDanger(AIInfoFrame frame)
	{
		List<BotID> sortedAscendingFoeBot = null;
		TreeMap<Float, BotID> botToDanger = new TreeMap<Float, BotID>();
		
		final BotID ballCarrierID = frame.tacticalInfo.getBallPossession().getOpponentsId();
		BotIDMap<TrackedBot> foeBotsUnsorted = new BotIDMap<TrackedBot>(frame.worldFrame.foeBots);
		
		// delete the ball carrier, if there is one.
		if (ballCarrierID.getNumber() != -1)
		{
			foeBotsUnsorted.remove(ballCarrierID);
		}
		
		for (Entry<BotID, TrackedBot> foeBot : foeBotsUnsorted)
		{
			float danger = determinDangerOfBot(foeBot.getValue(), frame);
			botToDanger.put(danger, foeBot.getKey());
		}
		
		// sotiert aufsteigend
		sortedAscendingFoeBot = new ArrayList<BotID>(botToDanger.values());
		
		return sortedAscendingFoeBot;
		
	}
	
	
	private float determinDangerOfBot(TrackedBot enemy, AIInfoFrame frame)
	{
		// Bots with the lowest dangerValue are the most dangerous ones.
		float value = 0;
		
		float distanceValue = determinDistanceDangerOfBot(enemy, frame);
		float angleValue = determinAngleDangerOfBot(enemy, frame);
		float visibilityValue = determinVisibilityToGoal(enemy, frame);
		float viewValue = determinViewAngleDanger(enemy, frame);
		
		int distanceWeighting = 3;
		int angleWeighting = 1;
		int visibilityWeighting = 2;
		int viewValueWeighting = 2;
		
		// danger values bewerten
		value = (distanceValue * distanceWeighting) + (angleValue * angleWeighting)
				+ (visibilityValue * visibilityWeighting) + (viewValue * viewValueWeighting);
		return value;
	}
	
	
	private float determinDistanceDangerOfBot(TrackedBot enemy, AIInfoFrame frame)
	{
		float value = 0;
		float length = (AIConfig.getGeometry().getFieldLength());
		Goal ourGoal = AIConfig.getGeometry().getGoalOur();
		
		// calculate distance
		float distance_foebot_goal_center = GeoMath.distancePP(enemy, ourGoal.getGoalCenter());
		// check if distamce to small
		if (distance_foebot_goal_center >= (AIConfig.getGeometry().getDistanceToPenaltyArea() + 600))
		{
			value = map(distance_foebot_goal_center, 0, length / 2, 0, 100);
		} else
		{
			value = 100;
		}
		return value;
	}
	
	
	private float determinAngleDangerOfBot(TrackedBot enemy, AIInfoFrame frame)
	{
		float value = 0;
		float dangerAngle = 90;
		float angleFoeBotGoalCenter = 0;
		float deltaToMax = 0;
		float deltaAngle = 0;
		
		Goal ourGoal = AIConfig.getGeometry().getGoalOur();
		Vector2 vecGoalLine;
		Vector2 vecPosGoal = new Vector2(ourGoal.getGoalCenter().x(), ourGoal.getGoalCenter().y());
		Vector2 vecEnemyToGoal;
		
		// calculate angle
		vecEnemyToGoal = vecPosGoal.subtractNew(enemy.getPos());
		vecGoalLine = ourGoal.getGoalPostLeft().subtractNew(ourGoal.getGoalPostRight());
		
		angleFoeBotGoalCenter = AngleMath.rad2deg(GeoMath.angleBetweenVectorAndVector(vecEnemyToGoal, vecGoalLine));
		
		// calculate Delta
		deltaAngle = Math.abs(Math.abs(Math.abs(90 - angleFoeBotGoalCenter) - 90) - dangerAngle);
		deltaToMax = SumatraMath.max(Math.abs(90 - dangerAngle), Math.abs(0 + dangerAngle));
		
		// determin danger
		value = map(deltaAngle, 0, deltaToMax, 0, 100);
		return value;
	}
	
	
	private float determinVisibilityToGoal(TrackedBot enemy, AIInfoFrame frame)
	{
		float value = 0;
		float ballDelta = 10;
		float raySize = 0;
		boolean freeWayToGoalCenter;
		
		Goal ourGoal = AIConfig.getGeometry().getGoalOur();
		
		// fill List ignoredBots with the bots from the play
		List<BotID> ignoredBots = foeBots;
		for (ManToManMarkerRole key : roles)
		{
			ignoredBots.add(key.getBotID());
		}
		
		// calculate Visibility
		raySize = (AIConfig.getGeometry().getBallRadius() * 2) + ballDelta;
		freeWayToGoalCenter = GeoMath.p2pVisibility(frame.worldFrame, enemy.getPos(), ourGoal.getGoalCenter(), raySize,
				ignoredBots);
		
		if (freeWayToGoalCenter)
		{
			value = 25;
		} else
		{
			value = 75;
		}
		
		return value;
	}
	
	
	/**
	 * 
	 * @param enemy
	 * @param frame
	 * 
	 * @return return 0 if enemy bot could recieve a pass and shoot directly and 100 if
	 *         enemy bot is facing in the wrong direction.
	 */
	private float determinViewAngleDanger(TrackedBot enemy, AIInfoFrame frame)
	{
		if (enemy.getPos().x() > 0)
		{
			// if bot is not on our have, then hes not dangerous!
			return 100;
		}
		Vector2 ballCarrier = null;
		try
		{
			ballCarrier = new Vector2(frame.worldFrame.foeBots
					.get(frame.tacticalInfo.getBallPossession().getOpponentsId()).getPos());
			
		} catch (Exception e)
		{
			// our team has the ball, so this method is useless.
			return 100;
		}
		
		Vector2 posEnemy = new Vector2(enemy.getPos());
		Vector2 ourGoal = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalCenter());
		Vector2 enemyBotToBalLCarrier = ballCarrier.subtractNew(posEnemy);
		Vector2 enemyBotViewAngle = new Vector2(enemy.getAngle());
		Vector2 intersectionEnemyBallCarrierAndEnemyBot = null;
		Vector2 intersectionEnemyViewAndOurGoal = null;
		
		Line goalLine = new Line(Vector2.ZERO_VECTOR,
				Vector2.X_AXIS.multiplyNew(-AIConfig.getGeometry().getFieldLength()));
		
		if (enemy.getPos().y() < 0)
		{
			if ((AngleMath.rad2deg(enemy.getAngle()) < 180) && (AngleMath.rad2deg(enemy.getAngle()) > 0))
			{
				
			} else
			{
				// enemyBot is facing to a wrong direction
				return 100;
			}
		} else
		{
			if ((AngleMath.rad2deg(enemy.getAngle()) > -180) && (AngleMath.rad2deg(enemy.getAngle()) < 0))
			{
				
			} else
			{
				// enemyBot is facing to a wrong direction
				return 100;
			}
		}
		
		try
		{
			intersectionEnemyBallCarrierAndEnemyBot = GeoMath.intersectionPoint(goalLine, new Line(posEnemy,
					enemyBotToBalLCarrier));
			intersectionEnemyViewAndOurGoal = GeoMath.intersectionPoint(goalLine,
					new Line(posEnemy, enemyBotViewAngle.multiplyNew(AIConfig.getGeometry().getFieldWidth())));
			
		} catch (MathException err)
		{
			// one of the intersection points does not exist,
			// either the line to the ballCarrier is parallel to the
			// x-axis or the enemy bot is standing x-axis and is looking to
			// our goal.
			return 100;
		}
		if (enemy.getId().getNumber() == 1)
		{
			frame.addDebugShape(new DrawablePoint(intersectionEnemyBallCarrierAndEnemyBot, Color.CYAN));
			frame.addDebugShape(new DrawablePoint(intersectionEnemyViewAndOurGoal, Color.CYAN));
			
			frame.addDebugShape(new DrawableLine(new Line(posEnemy, enemyBotToBalLCarrier), Color.GREEN));
			frame.addDebugShape(new DrawableLine(new Line(posEnemy, enemyBotViewAngle.multiplyNew(AIConfig.getGeometry()
					.getFieldWidth())), Color.GREEN));
		}
		
		if ((intersectionEnemyViewAndOurGoal.x() < intersectionEnemyBallCarrierAndEnemyBot.x())
				&& (intersectionEnemyViewAndOurGoal.x() > ourGoal.x()))
		{
			return 0;
		}
		// enemy is not facing in a "dangerous" angle, but not completely wrong.
		return 75;
	}
	
	
	/**
	 * map�s input to a a value between out_min and out_max
	 * 
	 * @author Simon Sander
	 * 
	 * @param x
	 * @param in_min
	 * @param in_max
	 * @param out_min
	 * @param out_max
	 * @return
	 */
	private float map(float x, float in_min, float in_max, float out_min, float out_max)
	{
		return (((x - in_min) * (out_max - out_min)) / (in_max - in_min)) + out_min;
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		
		/*
		 * // Move Condition �berpr�fen und wenn blockiert, dann
		 * // eine wand bilden, nicht hintereinander.
		 * // roles.get(0).checkMovementCondition() <-- enum, blocked.
		 * 
		 * for (ManToManMarkerRole key : roles)
		 * {
		 * Vector2 firstBotPos = new Vector2(key.getBot().getPos());
		 * 
		 * // nur eigene Bots checken.
		 * // erkennen, dass wenn 2 feindliche bots hintereinander.
		 * if (key.getMoveCon().checkCondition(currentFrame.worldFrame, key.getBotID()) == EConditionState.BLOCKED)
		 * {
		 * 
		 * Circle firstBotcircle = new Circle(firstBotPos, 300);
		 * currentFrame.addDebugShape(new DrawableCircle(firstBotcircle, Color.RED));
		 * 
		 * // log.error("Bot is being blocked " + key.getBotID());
		 * // currentFrame.addDebugShape(new DrawableCircle(new Circlef(key.getBot().getPos(), 300)));
		 * for (ManToManMarkerRole key2 : roles)
		 * {
		 * Vector2 secoundBotPOS = new Vector2(key2.getBot().getPos());
		 * 
		 * Circle secoundBotBotcircle = new Circle(secoundBotPOS, 300);
		 * Circle BigBotcircle = new Circle(firstBotPos, 600);
		 * 
		 * Vector2 distance = secoundBotPOS.subtractNew(firstBotPos);
		 * 
		 * if ((distance.getLength2() < 600) && (BigBotcircle.isPointInShape(secoundBotPOS)))
		 * {
		 * 
		 * // currentFrame.addDebugShape(new DrawableCircle(secoundBotBotcircle, Color.GREEN));
		 * currentFrame.addDebugShape(new DrawableCircle(BigBotcircle, Color.ORANGE));
		 * }
		 * 
		 * 
		 * }
		 * }
		 * }
		 */
	}
}
