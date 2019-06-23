/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2012
 * Author(s): Philipp
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint.EShootKind;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.MixedTeamHelper;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.KeeperAvailableCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderKNDWDPRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloRole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


/**
 * Standard defense Play. Set the position of the defender after the {@link DefensePointsCalculator}.
 * Keeper stands between ball and goal.
 * 
 * 
 * Requires: 1 {@link KeeperSoloRole Keeper} and 1 to 2 {@link DefenderKNDWDPRole Defender}
 * */

public class NDefenderWDPPlay extends APlay
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private static final int			DISTANCE_FACTOR		= 2;
	
	private List<DefenderKNDWDPRole>	listDefender			= new ArrayList<DefenderKNDWDPRole>();
	
	private TrackedBot					trackedKeeper			= null;
	
	/** Keeper Start Position: In the Middle of the Goal on the goalline */
	private final Vector2f				goalieStartPos			= AIConfig.getGeometry().getGoalOur().getGoalCenter();
	/** 'vertical' gap between defender and keeper */
	private final float					SPACE_BEFORE_KEEPER	= AIConfig.getRoles().getDefenderK2D().getSpaceBeforeKeeper();
	
	/** 'horizontal' gap between defender and keeper */
	private final float					SPACE_BESIDE_KEEPER	= AIConfig.getRoles().getDefenderK2D().getSpaceBesideKeeper();
	
	private final float					BLOCK_RADIUS			= AIConfig.getPlays().getnDefenderPlay().getBlockRadius();
	private final float					BALL_SPEED				= AIConfig.getPlays().getnDefenderPlay().getAllowedBallSpeed();
	private final boolean				ALLOWED_BLOCK			= AIConfig.getPlays().getnDefenderPlay().isAllowedBlockModus();
	
	
	private final Goal					goal						= AIConfig.getGeometry().getGoalOur();
	
	private boolean						keeperRoleSet			= false;
	
	private BotID							botID;
	
	
	// private Logger logger = Logger.getLogger(NDefenderWDPPlay.class);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public NDefenderWDPPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		setTimeout(Integer.MAX_VALUE);
		addCriterion(new KeeperAvailableCrit());
		
		final IVector2 initPos = AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyAreaFrontLine().supportVector()
				.addNew(new Vector2(200, 0));
		
		for (int i = getRoleCount(); i < getNumAssignedRoles(); i++)
		{
			DefenderKNDWDPRole def = new DefenderKNDWDPRole();
			listDefender.add(def);
			addDefensiveRole(def, initPos);
		}
	}
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		if ((frame.refereeMsgCached != null))
		{
			botID = new BotID(frame.refereeMsgCached.getTeamInfoTigers().getGoalie());
		} else
		{
			botID = TeamConfig.getInstance().getTeam().getKeeperId();
		}
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		keeperRoleSet = checkKeeperPlayAndKeeperVisibility(currentFrame);
		trackedKeeper = getTrackedKeeper(currentFrame);
		// ---------------------------------------------
		IVector2 interceptPoint;
		try
		{
			interceptPoint = GeoMath.intersectionPoint(currentFrame.worldFrame.ball.getPos(),
					currentFrame.worldFrame.ball.getVel(), goalieStartPos, AVector2.Y_AXIS);
		} catch (final MathException err)
		{
			interceptPoint = null;
		}
		
		// If the ball velocity is zero, the intersection point is null.
		if (ALLOWED_BLOCK)
		{
			if ((interceptPoint != null) && (interceptPoint.y() > goal.getGoalPostRight().y())
					&& (interceptPoint.y() < goal.getGoalPostLeft().y())
					&& (currentFrame.worldFrame.ball.getVel().x() < BALL_SPEED))
			{
				// w�hlt ein bot aus der ziemlich nah am ball (config einstellung ist und
				DefenderKNDWDPRole role = chooseRoleToBlock(currentFrame, interceptPoint);
				if (role != null)
				{
					role.setIntersectionPoint(interceptPoint, true);
				}
			}
		}
		
		
		List<IDrawableShape> shapes = new ArrayList<IDrawableShape>();
		List<DefensePoint> listDefPoint = getPointsToDefend(currentFrame, listDefender.size());
		// --------------------------------------BEGIN BACKUP SOLUTION----------------------
		bachkUpSolution(currentFrame, listDefPoint);
		// --------------------------------------EDN BACKUP SOLUTION----------------------
		
		Collections.sort(listDefPoint, ValuePoint.YCOMPARATOR);
		Collections.sort(listDefender, DefenderKNDWDPRole.YCOMPARATOR);
		// Zuweissung von DefensePoints
		for (int i = 0; i < listDefender.size(); i++)
		{
			DefenderKNDWDPRole defender = listDefender.get(i);
			
			if (listDefPoint.size() > i)
			{
				DefensePoint defPoint = listDefPoint.get(i);
				if (defPoint != null)
				{
					defender.setDefPoint(defPoint);
					if (defPoint.getProtectAgainst() != null)
					{
						defender.updateLookAtTarget(defPoint.getProtectAgainst().getPos());
					} else
					{
						if (!(currentFrame.worldFrame.ball.getPos().x() < (defender.getPos().x() + AIConfig.getGeometry()
								.getBotRadius())))
						{
							defender.updateLookAtTarget(currentFrame.worldFrame.ball.getPos());
						} else
						{
							defender.updateLookAtTarget(new Vector2(0, 0));
						}
					}
				}
			}
			
		}
		
		// -----------Shapes hinzuefgen------------------
		for (IDrawableShape shape : shapes)
		{
			currentFrame.addDebugShape(shape);
		}
	}
	
	
	/**
	 * Backup solution
	 * TODO
	 */
	private void bachkUpSolution(AIInfoFrame currentFrame, List<DefensePoint> listDefPoint)
	{
		if (getDefPointSizeWithoutDefault(listDefPoint) < listDefender.size())
		{
			int difference = listDefender.size() - getDefPointSizeWithoutDefault(listDefPoint);
			
			if (difference == 1)
			{
				IVector2 destination = getPositionForBackupsituation(EWAI.LEFT, currentFrame);
				if (destination == null)
				{
					// TODO an dieste stelle intelligentere Punkte erzeugen
					DefensePoint temp = new DefensePoint(-2150, 200);
					listDefPoint.add(temp);
				} else
				{
					listDefPoint.add(new DefensePoint(destination));
				}
			} else if (difference == 2)
			{
				IVector2 destination = getPositionForBackupsituation(EWAI.LEFT, currentFrame);
				if (destination == null)
				{ // TODO an dieste stelle intelligentere Punkte erzeugen
					DefensePoint temp = new DefensePoint(-2150, 200);
					listDefPoint.add(temp);
				} else
				{
					listDefPoint.add(new DefensePoint(destination));
				}
				destination = getPositionForBackupsituation(EWAI.RIGHT, currentFrame);
				if (destination == null)
				{
					DefensePoint temp = new DefensePoint(-2150, -200);
					listDefPoint.add(temp);
				} else
				{
					listDefPoint.add(new DefensePoint(destination));
				}
			} else if (difference == 3)
			{
				IVector2 destination = getPositionForBackupsituation(EWAI.LEFT, currentFrame);
				if (destination == null)
				{ // TODO an dieste stelle intelligentere Punkte erzeugen
					DefensePoint temp = new DefensePoint(-2150, 200);
					listDefPoint.add(temp);
				} else
				{
					listDefPoint.add(new DefensePoint(destination));
				}
				destination = getPositionForBackupsituation(EWAI.RIGHT, currentFrame);
				if (destination == null)
				{
					DefensePoint temp = new DefensePoint(-2150, -200);
					listDefPoint.add(temp);
				} else
				{
					listDefPoint.add(new DefensePoint(destination));
				}
				listDefPoint.add(new DefensePoint(-2250, -500));
			}
		}
		
	}
	
	
	/**
	 * Returns the acutal keeper
	 * @param aiFrame
	 * @return
	 */
	private TrackedBot getTrackedKeeper(AIInfoFrame aiFrame)
	{
		return aiFrame.worldFrame.tigerBotsVisible.getWithNull(botID);
	}
	
	
	/**
	 * Choose a role to block the ball like the keeper
	 * 
	 * @param intersectPoint
	 * @return
	 */
	private DefenderKNDWDPRole chooseRoleToBlock(AIInfoFrame currentFrame, IVector2 intersectPoint)
	{
		// TODO Philipp: Auto-generated method stub
		float distance = BLOCK_RADIUS;
		
		DefenderKNDWDPRole shortesBot = null;
		for (DefenderKNDWDPRole defender : listDefender)
		{
			IVector2 tempIntersectionPoint = GeoMath.leadPointOnLine(defender.getPos(),
					currentFrame.worldFrame.ball.getPos(), intersectPoint);
			float distanceBotPoint = GeoMath.distancePP(defender.getPos(), tempIntersectionPoint);
			if (distance >= distanceBotPoint)
			{
				distance = distanceBotPoint;
				shortesBot = defender;
			}
		}
		
		return shortesBot;
		
	}
	
	
	/**
	 * Get a position on the Left or Right side of the keeper
	 * 
	 * @param type
	 * @param currentFrame
	 */
	private IVector2 getPositionForBackupsituation(EWAI type, AIInfoFrame currentFrame)
	{
		IVector2 destination = null;
		if (trackedKeeper != null)
		{
			if (keeperRoleSet && AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(trackedKeeper.getPos()))
			{
				
				Vector2 tempDestination = new Vector2(trackedKeeper.getPos());
				
				final IVector2 protectAgainst = currentFrame.worldFrame.ball.getPos();
				Vector2 direction = protectAgainst.subtractNew(trackedKeeper.getPos());
				direction.scaleTo(SPACE_BEFORE_KEEPER);
				tempDestination.add(direction);
				direction.turn(AngleMath.PI_HALF);
				direction.scaleTo(SPACE_BESIDE_KEEPER);
				if (EWAI.RIGHT == type)
				{
					tempDestination.subtract(direction);
				} else
				{
					tempDestination.add(direction);
				}
				destination = AIConfig.getGeometry().getPenaltyAreaOur().nearestPointOutside(tempDestination);
				return destination;
			}
		}
		
		return null;
	}
	
	
	// --------------------------------------------------------------------------
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	/**
	 * Counts the indirect and direct defensPoints
	 * 
	 * @param listDefPoint
	 * @return
	 */
	private int getDefPointSizeWithoutDefault(List<DefensePoint> listDefPoint)
	{
		int i = 0;
		for (DefensePoint defensePoint : listDefPoint)
		{
			if (defensePoint.kindOfshoot != EShootKind.DEFAULT)
			{
				i++;
			}
		}
		return i;
	}
	
	
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param i - number of defensPoints where needed
	 * @return
	 */
	private List<DefensePoint> getPointsToDefend(AIInfoFrame currentFrame, int i)
	{
		final List<DefensePoint> vectordef = currentFrame.tacticalInfo.getDefGoalPoints();
		List<DefensePoint> returnList = new ArrayList<DefensePoint>();
		
		if (!vectordef.isEmpty())
		{
			
			for (DefensePoint defensePoint : vectordef)
			{
				// wenn die Liste leer ist, �bernehme ersten Punkt in die liste
				if (returnList.size() == 0)
				{
					returnList.add(defensePoint);
				} else
				{
					boolean isFarAway = true;
					for (ValuePoint point : returnList)
					{
						if (!(GeoMath.distancePP(point, defensePoint) > ((AIConfig.getGeometry().getBotRadius()) * DISTANCE_FACTOR)))
						{
							isFarAway = false;
						}
					}
					if (isFarAway == true)
					{
						returnList.add(defensePoint);
					}
				}
				
			}
		}
		
		for (int j = returnList.size() - 1; i <= j; j--)
		{
			returnList.remove(j);
		}
		
		
		// Sortiere listDefPoints nach y
		Collections.sort(returnList, ValuePoint.YCOMPARATOR);
		return returnList;
	}
	
	
	/**
	 * Chekc if we initiatet a Keeper play or if there is a keeper on the field
	 * 
	 * @param frame
	 * @return
	 * 
	 */
	private boolean checkKeeperPlayAndKeeperVisibility(AIInfoFrame frame)
	{
		List<APlay> listaPlay = frame.playStrategy.getActivePlays();
		
		for (APlay aPlay : listaPlay)
		{
			if (aPlay.getType() == EPlay.KEEPER_SOLO)
			{
				return true;
			}
		}
		MixedTeamHelper helper = new MixedTeamHelper(frame.getControlState() == EAIControlState.MIXED_TEAM_MODE);
		return helper.needsKeeper(frame);
		
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
