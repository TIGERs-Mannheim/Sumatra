/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2012
 * Author(s): Philipp
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ITacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.NoObjectWithThisIDException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Standard defense Play. Set the position of the defender after the {@link DefensePointsCalc}.
 * Keeper stands between ball and goal.
 * Requires: {@link DefenderRole Defender}
 */

public class DefensePlay extends APlay
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log								= Logger.getLogger(DefensePlay.class.getName());
	
	private List<DefenderRole>		listDefender					= null;
	
	@Configurable(comment = "Multiplicator for Distance between Defense Bots (default=2 for double BotRadius)")
	private static int				DISTANCE_FACTOR				= 2;
	@Configurable(comment = "vertical gap between defender and keeper, only important for backup")
	private static float				spaceBeforeKeeper				= 300;
	@Configurable(comment = "horizontal gap between defender and keeper, only important for backup")
	private static float				spaceBesidesKeeper			= 200;
	@Configurable
	private static float				blockRadius						= 400;
	@Configurable
	private static float				ballSpeed						= 1.0f;
	
	private static final int[]		POSITIONS						= { -3, 3, -6, 6, -9, 9, -2, 2, -5, 5, -8, 8, -1, 1, -4, 4,
																				-7, 7 };
	private int							secondRow						= 0;
	private static final int		ROW_STEP							= 200;
	private int							initPosCounter					= 0;
	@Configurable(comment = "Distance should stay away from ball if EGameState=STOPPED")
	private static float				secureDistanceStoppedMode	= 500;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DefensePlay()
	{
		super(EPlay.DEFENSIVE);
		listDefender = new ArrayList<DefenderRole>();
	}
	
	
	/**
	 * Logic must be moved to roles
	 * 
	 * @param currentFrame
	 */
	@Override
	protected void doUpdate(final AthenaAiFrame currentFrame)
	{
		List<DefensePoint> listDefPoint = new ArrayList<DefensePoint>();
		
		switch (currentFrame.getTacticalField().getGameState())
		{
			case PREPARE_PENALTY_THEY:
				listDefPoint.clear();
				for (int i = 0; i < (listDefender.size()); i++)
				{
					listDefPoint.add(new DefensePoint(getNextInitPosition(false)));
				}
				break;
			default:
				listDefPoint.addAll(getPointsToDefend(currentFrame.getWorldFrame(), currentFrame.getPrevFrame(),
						currentFrame.getTacticalField(),
						listDefender.size(),
						currentFrame.getWorldFrame().ball.getPos()));
				break;
		}
		
		// Zuweisung von DefensePoints
		if (listDefPoint.size() < listDefender.size())
		{
			log.info("Too few defense points!!! " + listDefPoint.size() + " def points for " + listDefender.size()
					+ " defenders");
			listDefPoint.addAll(fillDevPointsWithDummyPoints(currentFrame));
			if (listDefPoint.size() < listDefender.size())
			{
				log.warn("Filling with dummy points did not work." + listDefPoint.size() + " def points for "
						+ listDefender.size()
						+ " defenders");
			}
		}
		
		Collections.sort(listDefPoint, ValuePoint.Y_COMPARATOR);
		Collections.sort(listDefender, DefenderRole.Y_COMPARATOR);
		for (int j = 0; j < listDefender.size(); j++)
		{
			listDefender.get(j).setDefPoint(listDefPoint.get(j));
		}
		
	}
	
	
	/**
	 * dummy points if not enough valid defense points available
	 * points on line between goal center and ball
	 * 
	 * @param currentFrame
	 * @return
	 */
	private List<DefensePoint> fillDevPointsWithDummyPoints(final AthenaAiFrame currentFrame)
	{
		List<DefensePoint> listDefPoint = new ArrayList<DefensePoint>();
		
		int foundDefPoints = listDefPoint.size();
		for (int j = 0; j < (listDefender.size() - foundDefPoints); j++)
		{
			IVector2 ball = currentFrame.getWorldFrame().ball.getPos();
			Line l = new Line(ball, AIConfig.getGeometry().getGoalOur().getGoalCenter().subtractNew(ball));
			IVector2 backupBlockPos = new DefensePoint(
					-(AIConfig.getGeometry()
							.getFieldLength() / 2)
							+ AIConfig
									.getGeometry()
									.getPenaltyAreaOur()
									.getRadiusOfPenaltyArea()
							+ AIConfig.getGeometry()
									.getBotRadius(), 0);
			for (IVector2 intersection : GeoMath.lineCircleIntersections(l, new Circle(AIConfig.getGeometry()
					.getGoalOur().getGoalCenter(), AIConfig.getGeometry().getGoalOur().getSize() + 100)))
			{
				if (GeoMath.isInsideField(intersection))
				{
					backupBlockPos = GeoMath.stepAlongLine(intersection, AIConfig.getGeometry()
							.getGoalOur().getGoalCenter(), (-AIConfig.getGeometry().getBotRadius() * 3 * j));
				}
			}
			listDefPoint.add(new DefensePoint(backupBlockPos));
		}
		return listDefPoint;
	}
	
	
	/**
	 * positions during a penalty them
	 * 
	 * @param ourPenalty
	 * @return
	 */
	private IVector2 getNextInitPosition(final boolean ourPenalty)
	{
		final float halfFieldWidth = AIConfig.getGeometry().getFieldWidth() / 2;
		final float factor = halfFieldWidth / 10;
		IVector2 penaltyLine;
		
		initPosCounter++;
		if (initPosCounter > POSITIONS.length)
		{
			secondRow -= ROW_STEP;
			initPosCounter = 1;
		}
		final IVector2 changeVec = new Vector2(secondRow, factor * POSITIONS[initPosCounter - 1]);
		if (ourPenalty)
		{
			penaltyLine = AIConfig.getGeometry().getPenaltyLineTheir();
			return penaltyLine.addNew(changeVec);
		}
		penaltyLine = AIConfig.getGeometry().getPenaltyLineOur();
		return penaltyLine.subtractNew(changeVec);
	}
	
	
	/**
	 * @param wframe
	 * @param prevFrame
	 * @param currentTacticalField
	 * @param i - number of defensPoints where needed
	 * @param ballPosition -position of the ball
	 * @return
	 */
	public static List<DefensePoint> getPointsToDefend(final WorldFrame wframe, final AIInfoFrame prevFrame,
			final ITacticalField currentTacticalField, final int i,
			final IVector2 ballPosition)
	{
		final List<DefensePoint> vectordef = currentTacticalField.getDefGoalPoints();
		List<DefensePoint> returnList = new ArrayList<DefensePoint>();
		if (!vectordef.isEmpty())
		{
			DefensePoint defpoint = getFirstPointToDefend(currentTacticalField, ballPosition);
			if (defpoint != null)
			{
				returnList.add(defpoint);
			}
			for (DefensePoint defensePoint : vectordef)
			{
				// wenn die Liste leer ist, uebernehme ersten Punkt in die liste
				boolean isFarAway = true;
				for (ValuePoint point : returnList)
				{
					
					if (currentTacticalField.getGameState() != EGameState.STOPPED)
					{
						// only check if a bot fits in the near
						if (GeoMath.distancePP(point, defensePoint) < ((AIConfig.getGeometry().getBotRadius()) * DISTANCE_FACTOR))
						{
							isFarAway = false;
						}
					} else
					{
						// If EGameSTat=Stopped check also if the ball is near
						if ((GeoMath.distancePP(point, defensePoint) < ((AIConfig.getGeometry().getBotRadius()) *
								DISTANCE_FACTOR))
								|| (GeoMath.distancePP(defensePoint, ballPosition) < secureDistanceStoppedMode))
						{
							isFarAway = false;
						}
					}
				}
				boolean isPointFree = true;
				for (APlay activePlay : prevFrame.getPlayStrategy().getActivePlays())
				{
					if (!(activePlay instanceof DefensePlay))
					{
						for (ARole bot : activePlay.getRoles())
						{
							if (!bot.getBotID().isUninitializedID())
							{
								try
								{
									TrackedTigerBot newBot = wframe.getTiger(bot.getBotID());
									if (newBot.getPos().subtractNew(defensePoint).getLength2() < (AIConfig.getGeometry()
											.getBotRadius() * 2))
									{
										isPointFree = false;
									}
								} catch (NoObjectWithThisIDException err)
								{
									// ignore
								}
							} else
							{
								// log.warn("Bot is uninitialized in previous frame");
							}
						}
					}
				}
				if ((isFarAway == true) && (isPointFree == true))
				{
					// IVector2 goal2DefPoint = defensePoint.subtract(AIConfig.getGeometry().getGoalOur().getGoalCenter());
					// defensePoint.set(AIConfig.getGeometry().getGoalOur().getGoalCenter()
					// .addNew(goal2DefPoint.scaleToNew(100)));
					returnList.add(defensePoint);
				}
			}
			
		}
		
		for (int j = returnList.size() - 1; i <= j; j--)
		{
			returnList.remove(j);
		}
		
		
		// Sortiere listDefPoints nach y
		Collections.sort(returnList, ValuePoint.Y_COMPARATOR);
		return returnList;
	}
	
	
	/**
	 * Calculates the first defensPoints, if normal {@link EGameState} the first point returned. Is {@link EGameState}
	 * ==Stopped, it will find a point more than 500mm faraway
	 * 
	 * @param currentTacticalField
	 * @return
	 */
	private static DefensePoint getFirstPointToDefend(final ITacticalField currentTacticalField,
			final IVector2 ballPosition)
	{
		final List<DefensePoint> vectordef = currentTacticalField.getDefGoalPoints();
		// If stop modus choose a start defPoint 500mm away from the ball
		if (currentTacticalField.getGameState() == EGameState.STOPPED)
		{
			DefensePoint pointToDef = null;
			for (int j = 0; (pointToDef == null) && (j < vectordef.size()); j++)
			{
				if (GeoMath.distancePP(vectordef.get(j), ballPosition) > secureDistanceStoppedMode)
				{
					pointToDef = vectordef.get(j);
				}
			}
			return pointToDef;
		}
		return vectordef.get(0);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected ARole onRemoveRole()
	{
		if (listDefender.isEmpty())
		{
			throw new IllegalStateException("Two roles left to be deleted");
		}
		return listDefender.remove(listDefender.size() - 1);
	}
	
	
	@Override
	protected void onRoleRemoved(final ARole role)
	{
		listDefender.remove(role);
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		DefenderRole def = new DefenderRole();
		listDefender.add(def);
		return def;
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
		switch (gameState)
		{
			case CORNER_KICK_THEY:
				break;
			case CORNER_KICK_WE:
				break;
			case GOAL_KICK_THEY:
				break;
			case GOAL_KICK_WE:
				break;
			case DIRECT_KICK_THEY:
				break;
			case DIRECT_KICK_WE:
				break;
			case HALTED:
				break;
			case PREPARE_KICKOFF_THEY:
				break;
			case PREPARE_KICKOFF_WE:
				break;
			case PREPARE_PENALTY_THEY:
				break;
			case PREPARE_PENALTY_WE:
				break;
			case RUNNING:
				break;
			case STOPPED:
				break;
			case THROW_IN_THEY:
				break;
			case THROW_IN_WE:
				break;
			case TIMEOUT_THEY:
				break;
			case TIMEOUT_WE:
				break;
			case UNKNOWN:
				break;
			default:
				break;
		}
	}
	
}
