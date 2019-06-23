/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.data.math.SteinhausJohnsonTrotter;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableValuePoints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Support play manages the support roles. This are all roles that are not offensive or defensive.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 *         Simon Sander <Simon.Sander@dlr.de>
 *         ChrisC
 */
public class SupportPlay extends APlay
{
	@Configurable(comment = "Weight of bot distance to the globalPos")
	private static double	distanceBotWeight		= 2;
	
	@Configurable(comment = "Weight of ball distance to gobalPos")
	private static double	distanceBallWeight	= 2;
	
	@Configurable(comment = "Weight of GoalScoreChance")
	private static double	goalScoreWeight		= 1;
	
	@Configurable(comment = "first startYPos at kickoff")
	private static double	startYPos				= 2000;
	
	@Configurable(comment = "Radius where globalGridPositions are removed around a selected position to avoid nearly supporter")
	private static double	removeRadius			= 1500;
	
	@Configurable(comment = "Radius of own bots affected the global grid")
	private static double	distanceBotRadius		= 1000;
	
	@Configurable(comment = "Precision (number of dezimal numbers) of the cpuGrid")
	private static int		exponent					= 2;
	
	
	/**
	 */
	public SupportPlay()
	{
		super(EPlay.SUPPORT);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame currentFrame)
	{
		final ITacticalField tacticalField = currentFrame.getTacticalField();
		final EGameStateTeam gameState = tacticalField.getGameState();
		List<SupportRole> supportRoles = getRoles().stream().map(r -> (SupportRole) r).collect(Collectors.toList());
		List<ValuePoint> cpuGrid = new ArrayList<>();
		
		cpuGrid = calcCpuGrid(tacticalField.getBallDistancePoints(), tacticalField.getScoreChancePoints(), currentFrame,
				supportRoles);
		
		Map<BotID, IVector2> supportDistr = calcSupporterPositionMapping(cpuGrid);
		
		switch (gameState)
		{
			case STOPPED:
				break;
			case PREPARE_KICKOFF_THEY:
				updateSupportPositionsForKickOff(supportDistr, supportRoles);
				break;
			case PREPARE_KICKOFF_WE:
				updateSupportPositionsForKickOff(supportDistr, supportRoles);
				break;
			default:
				
				break;
		}
		
		for (SupportRole r : supportRoles)
		{
			r.setGlobalPos(supportDistr.get(r.getBotID()));
		}
	}
	
	
	/**
	 * Cause the roleassignemt is firstly clear in a play, we need to recalculate the supportgrid.
	 * In this calculation the supporterbots are ignored to get the best points in the grid
	 * 
	 * @param ballDistancePoints
	 * @param scoreChancePoints
	 * @param currentFrame - the current AiFrame
	 * @param supportRoles - List of Supporter
	 * @author ChrisC
	 */
	private List<ValuePoint> calcCpuGrid(final List<ValuePoint> ballDistancePoints,
			final List<ValuePoint> scoreChancePoints,
			final AthenaAiFrame currentFrame,
			final List<SupportRole> supportRoles)
	{
		List<ValuePoint> cpuGrid = new LinkedList<>();
		List<ValuePoint> distancePoints = new LinkedList<>();
		ballDistancePoints.stream().forEach(c -> distancePoints.add(new ValuePoint(c.getXYVector())));
		
		// For better field distribution calculate and value the distance to other bots
		calcDistanceToBotValues(currentFrame.getWorldFrame(),
				distancePoints, distanceBotRadius, supportRoles);
		
		// Calculate all different values to one
		for (int i = 0; i < distancePoints.size(); i++)
		{
			double value = (ballDistancePoints.get(i).value * distanceBallWeight) +
					(scoreChancePoints.get(i).value * goalScoreWeight) +
					(distancePoints.get(i).value * distanceBotWeight);
			value /= (distanceBallWeight + goalScoreWeight + distanceBotWeight);
			cpuGrid.add(new ValuePoint(ballDistancePoints.get(i).getXYVector(), value));
		}
		
		// To avoid ignoring slightly worser values round
		int decimalNumber = (int) Math.pow(10, exponent);
		cpuGrid.stream().forEach(p -> p.setValue(Math.round(p.getValue() * decimalNumber)));
		cpuGrid.sort(ValuePoint.VALUE_HIGH_COMPARATOR);
		
		// Draw cpuGrid
		Color color = new Color(200, 50, 50, 125);
		double highestValue = cpuGrid.get(0).value;
		for (ValuePoint point : cpuGrid)
		{
			if (point.value == highestValue)
			{
				DrawableCircle c = new DrawableCircle(point, Geometry.getBotRadius(), color);
				c.setFill(true);
				currentFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
						.add(c);
			}
		}
		
		DrawableValuePoints field = new DrawableValuePoints(cpuGrid);
		currentFrame.getTacticalField().getDrawableShapes().get(EShapesLayer.CPU_GRID).add(field);
		
		return cpuGrid;
	}
	
	
	/**
	 * Calculate for each point in the grid the distance to the nearest bot
	 * 
	 * @param distancePoints
	 * @param distanceBotRadius
	 * @author ChrisC
	 */
	private static void calcDistanceToBotValues(final WorldFrame wf,
			final List<ValuePoint> distancePoints, final double distanceBotRadius, final List<SupportRole> supporter)
	{
		List<BotID> supporterIDs = supporter.stream().map(s -> s.getBotID()).collect(Collectors.toList());
		List<ITrackedBot> otherBots = new ArrayList<>();
		otherBots.addAll(wf.getBots().values());
		otherBots.removeIf(o -> supporterIDs.contains(o.getBotId()));
		
		for (ValuePoint point : distancePoints)
		{
			IVector2 nearestPoint = GeoMath
					.nearestPointInList(
							otherBots.stream().map(b -> b.getPos()).collect(Collectors.toList()),
							point.getXYVector());
			
			double value = GeoMath.distancePP(nearestPoint, point.getXYVector());
			
			if (value >= distanceBotRadius)
			{
				point.value = 1;
			} else
			{
				point.value = (value / distanceBotRadius);
			}
			
		}
	}
	
	
	/**
	 * Maps the best cpuGridPositions with the supporterroles, to find the minimum time for all supporter to the targets
	 * 
	 * @param cpuGrid grid of valuePoints (unsorted)
	 * @author ChrisC, JulianT
	 */
	private Map<BotID, IVector2> calcSupporterPositionMapping(final List<ValuePoint> cpuGrid)
	{
		Map<BotID, IVector2> supportPositions = new HashMap<>();
		List<ITrackedBot> supporter = getRoles().stream().map(r -> r.getBot()).collect(Collectors.toList());
		
		// This should not happen, but if use the static positions of Kickoff
		if (cpuGrid.isEmpty())
		{
			updateSupportPositionsForKickOff(supportPositions,
					getRoles().stream().map(r -> (SupportRole) r).collect(Collectors.toList()));
			return supportPositions;
		}
		
		// Search for num supporter highest values in the grid.
		// if there are more highest values than num supporter take all highest points
		double highestValue;
		List<ValuePoint> highestPoints = new ArrayList<ValuePoint>();
		cpuGrid.sort(ValuePoint.VALUE_HIGH_COMPARATOR);
		highestValue = cpuGrid.get(0).value;
		
		List<IVector2> possiblePositions = new ArrayList<>();
		possiblePositions.addAll(cpuGrid);
		for (ValuePoint point : cpuGrid)
		{
			if ((point.value == highestValue) && possiblePositions.contains(point))
			{
				highestPoints.add(point);
				possiblePositions.removeIf(p -> GeoMath.distancePP(point, p) < removeRadius);
			} else
			{
				if (highestPoints.size() < supporter.size())
				{
					if (possiblePositions.contains(point))
					{
						highestValue = point.value;
						possiblePositions.removeIf(p -> GeoMath.distancePP(point, p) < removeRadius);
						highestPoints.add(point);
					}
				} else
				{
					break;
				}
			}
		}
		
		
		List<ValuePoint> rest = new ArrayList<>();
		TrajectoryGenerator generator = new TrajectoryGenerator();
		rest.addAll(highestPoints);
		
		// Special case of one Supporter -> take the best highest point referring to the time to the targets
		if (supporter.size() == 1)
		{
			ValuePoint best = rest.get(0);
			BangBangTrajectory2D trajBest = generator.generatePositionTrajectory(supporter.get(0), best);
			
			for (ValuePoint point : rest)
			{
				BangBangTrajectory2D traj = generator.generatePositionTrajectory(supporter.get(0), point);
				
				if ((point.value == best.value) && (traj.getTotalTime() < trajBest.getTotalTime()))
				{
					best = point;
					trajBest = traj;
				}
			}
			
			supportPositions.put(supporter.get(0).getBotId(), best);
			return supportPositions;
		}
		
		// Permute all possible mappings of highest cpuGrid Points and Supporter to find shortest way overall
		SteinhausJohnsonTrotter<ITrackedBot> permutator = new SteinhausJohnsonTrotter<ITrackedBot>(supporter);
		double bestTimeSum = Double.MAX_VALUE;
		List<ITrackedBot> bestPermutation = new ArrayList<ITrackedBot>();
		
		while (permutator.hasNext())
		{
			List<ITrackedBot> permutation = permutator.next();
			
			int timeSum = 0;
			for (int i = 0; (i < permutation.size()) && (i < highestPoints.size()); i++)
			{
				BangBangTrajectory2D traj = generator.generatePositionTrajectory(permutation.get(i), highestPoints.get(i));
				timeSum += traj.getTotalTime();
			}
			
			if (timeSum < bestTimeSum)
			{
				bestTimeSum = timeSum;
				bestPermutation = permutation;
			}
		}
		
		for (int i = 0; (i < bestPermutation.size()) && (i < highestPoints.size()); i++)
		{
			supportPositions.put(bestPermutation.get(i).getBotId(), highestPoints.get(i));
		}
		
		return supportPositions;
	}
	
	
	/**
	 * Use static positions for kickoff, cause there are no valid positions in the cpuGrid for kickoff
	 * 
	 * @param supportPositions
	 * @param supportRoles
	 * @author ChrisC
	 */
	private static void updateSupportPositionsForKickOff(final Map<BotID, IVector2> supportPositions,
			final List<SupportRole> supportRoles)
	{
		double yPos = startYPos;
		
		for (ARole curSupportRole : supportRoles)
		{
			BotID curBotId = curSupportRole.getBotID();
			IVector2 newSupportPosition = new Vector2(-250, yPos);
			yPos *= -1;
			if (yPos < 0)
			{
				yPos -= 500;
			} else
			{
				yPos += 500;
			}
			
			supportPositions.put(curBotId, newSupportPosition);
		}
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		return new SupportRole();
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
}
