/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.support.SupportPosition;
import edu.tigers.sumatra.ai.metis.support.SupportPositionGenerationCalc;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Support play manages the support roles. This are all roles that are not offensive or defensive.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 *         Simon Sander <Simon.Sander@dlr.de>
 *         ChrisC
 */
public class SupportPlay extends APlay
{
	@Configurable(comment = "Supporter which stay close to their global position", defValue = "5")
	private static int maxFixedGlobalPositions = 5;
	
	@Configurable(comment = "This is the time which a position of supporters is usually kept", defValue = "100")
	private static int msToKeepPositions = 100;
	
	@Configurable(comment = "This is proportion of our half since just pass targets are used", defValue = "0.15")
	private static double ballPosBiasWe = 0.15;
	
	@Configurable(comment = "This is proportion of their half since just shot targets are used", defValue = "0.15")
	private static double ballPosBiasThey = 0.15;
	
	private AthenaAiFrame currentFrame;
	
	private Random rnd;
	
	private Map<ARole, PositionTimeoutPair> rolePositionTimeouts = new HashMap<>();
	
	
	/**
	 * Default constructor
	 */
	public SupportPlay()
	{
		super(EPlay.SUPPORT);
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame currentFrame)
	{
		if (rnd == null)
		{
			rnd = new Random(currentFrame.getWorldFrame().getTimestamp());
		}
		
		this.currentFrame = currentFrame;
		
		clearAllGlobalPositions();
		
		PositionChooser positionChooser = new PositionChooser();
		List<SupportPosition> supportPositions = positionChooser.selectSupportPositions();
		
		SupporterAssigner supporterAssigner = new SupporterAssigner();
		supporterAssigner.assignSupporter(supportPositions);
	}
	
	
	private void clearAllGlobalPositions()
	{
		getRoles().stream().map(r -> (SupportRole) r).forEach(s -> s.setGlobalPosition(null));
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		return new SupportRole();
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected void onRoleRemoved(final ARole role)
	{
		super.onRoleRemoved(role);
		
		rolePositionTimeouts.remove(role);
	}
	
	private class PositionTimeoutPair
	{
		private SupportPosition pos;
		private long timeUntilNoChange = 0;
	}
	
	private class PositionChooser
	{
		List<SupportPosition> passPositions;
		List<SupportPosition> shootPositions;
		
		
		private PositionChooser()
		{
			passPositions = getPassPositionsSorted();
			shootPositions = getShootPositionsSorted();
		}
		
		
		private List<SupportPosition> getPassPositionsSorted()
		{
			return currentFrame.getTacticalField()
					.getSelectedSupportPositions().stream()
					.sorted(SupportPosition::comparePassScoreWith)
					.collect(Collectors.toList());
		}
		
		
		private List<SupportPosition> getShootPositionsSorted()
		{
			return currentFrame.getTacticalField()
					.getSelectedSupportPositions().stream()
					.filter(SupportPosition::isShootPosition)
					.sorted(SupportPosition::compareShootScoreWith)
					.collect(Collectors.toList());
		}
		
		
		private void removeInvalidRolePositionPairs()
		{
			List<ARole> rolesToRemove = new ArrayList<>();
			
			for (ARole role : rolePositionTimeouts.keySet())
			{
				final boolean rolesPositionIsStillActive = isRolePositionStillActive((SupportRole) role);
				
				if (!rolesPositionIsStillActive)
				{
					rolesToRemove.add(role);
				}
			}
			
			rolePositionTimeouts.keySet().removeAll(rolesToRemove);
		}
		
		
		private boolean isRolePositionStillActive(final SupportRole role)
		{
			if (!rolePositionTimeouts.containsKey(role))
			{
				return false;
			}
			
			final boolean timeStillValid = rolePositionTimeouts.get(role).timeUntilNoChange > currentFrame.getWorldFrame()
					.getTimestamp();
			
			final SupportPosition position = rolePositionTimeouts.get(role).pos;
			final boolean positionIsCandidate = passPositions.contains(position) || shootPositions.contains(position);
			
			return positionIsCandidate && timeStillValid;
		}
		
		
		private List<SupportPosition> selectSupportPositions()
		{
			removeInvalidRolePositionPairs();
			
			PassWeightCalculator passWeightCalculator = new PassWeightCalculator();
			final float currentPassWeight = passWeightCalculator.calculateCurrentPassWeight();
			
			List<SupportPosition> supportPositions = new ArrayList<>();
			
			for (ARole role : getRoles())
			{
				final boolean newPositionNecessary = !rolePositionTimeouts.containsKey(role);
				
				if (newPositionNecessary)
				{
					final SupportPosition pos = selectBestSupportPositionAccordingToWeight(currentPassWeight, role.getBot());
					
					if (pos != null)
					{
						supportPositions.add(pos);
						pos.setCovered(true);
					}
				} else
				{
					supportPositions.add(rolePositionTimeouts.get(role).pos);
					rolePositionTimeouts.get(role).pos.setCovered(true);
				}
			}
			
			return supportPositions;
		}
		
		
		private SupportPosition selectBestSupportPositionAccordingToWeight(final float currentPassWeight, ITrackedBot bot)
		{
			final double randomValue = rnd.nextDouble();
			
			SupportPosition result = null;
			
			if ((randomValue < currentPassWeight || shootPositions.isEmpty()) && !passPositions.isEmpty())
			{
				result = findBestAndFastestPositionInList(passPositions, bot, true);
				passPositions.remove(result);
				shootPositions.remove(result);
			} else
			{
				if (!shootPositions.isEmpty())
				{
					result = findBestAndFastestPositionInList(shootPositions, bot, false);
					passPositions.remove(result);
					shootPositions.remove(result);
				}
			}
			return result;
		}
		
		
		private SupportPosition findBestAndFastestPositionInList(List<SupportPosition> positions, ITrackedBot bot,
				boolean passScore)
		{
			SupportPosition bestComparingPosition = positions.get(0);
			double bestTime = TrajectoryGenerator.generatePositionTrajectory(bot, bestComparingPosition.getPos())
					.getTotalTime();
			
			for (SupportPosition pos : positions)
			{
				double diff;
				if (passScore)
				{
					diff = pos.getPassScore() - bestComparingPosition.getPassScore();
				} else
				{
					diff = pos.getShootScore() - bestComparingPosition.getShootScore();
				}
				if (diff < 0.001)
				{
					double time = TrajectoryGenerator.generatePositionTrajectory(bot, pos.getPos()).getTotalTime();
					if (time < bestTime)
					{
						bestTime = time;
						bestComparingPosition = pos;
					}
				}
			}
			return bestComparingPosition;
		}
	}
	
	
	private class PassWeightCalculator
	{
		private float calculateCurrentPassWeight()
		{
			float passWeight;
			passWeight = calculateBallPosFactorForPassWeight();
			
			return passWeight;
		}
		
		
		private float calculateBallPosFactorForPassWeight()
		{
			final double defensiveMinimumPosition = Geometry.getGoalOur().getCenter().x() * ballPosBiasWe;
			final double offensiveMaximumPosition = Geometry.getGoalTheir().getCenter().x() * ballPosBiasThey;
			
			final double ballPosX = currentFrame.getWorldFrame().getBall().getPos().x();
			
			if (ballPosX < defensiveMinimumPosition)
			{
				return 1;
			} else if (ballPosX > offensiveMaximumPosition)
			{
				return 0;
			}
			
			final double newCenter = (defensiveMinimumPosition + offensiveMaximumPosition) / 2;
			final double passWeightShift = ballPosX / newCenter;
			
			float passWeight = 0.5f;
			passWeight += passWeightShift * 0.5;
			
			return passWeight;
		}
	}
	
	private class SupporterAssigner
	{
		private void assignSupporter(List<SupportPosition> supportPositions)
		{
			List<SupportRole> supportRoles = getRoles().stream().map(s -> (SupportRole) s).limit(maxFixedGlobalPositions)
					.collect(Collectors.toList());
			
			for (SupportPosition position : supportPositions)
			{
				final SupportRole bestSupporter = findFastestBot(position.getPos(), supportRoles);
				final SupportRole coveringBot = findCoveringBot(position);
				
				final boolean otherBotFaster = coveringBot != bestSupporter;
				
				if (otherBotFaster)
				{
					rolePositionTimeouts.remove(coveringBot);
				}
				
				supportRoles.remove(bestSupporter);
				
				if (bestSupporter == null)
				{
					break;
				}
				
				final boolean botHasPositionAssigned = rolePositionTimeouts.containsKey(bestSupporter);
				
				if (!botHasPositionAssigned)
				{
					final boolean rolePresent = rolePositionTimeouts.containsKey(bestSupporter);
					final boolean roleReadyForChange = rolePresent
							&& (rolePositionTimeouts.get(bestSupporter).timeUntilNoChange < currentFrame.getWorldFrame()
									.getTimestamp());
					
					// This is intended to refresh the timer if the position changed. Not necessary if there werde no changes
					// to a given role
					if (!rolePresent || roleReadyForChange)
					{
						bestSupporter.setGlobalPosition(position.getPos());
						position.setCovered(true);
						
						PositionTimeoutPair pair = new PositionTimeoutPair();
						pair.pos = position;
						pair.timeUntilNoChange = currentFrame.getWorldFrame().getTimestamp() + msToKeepPositions;
						
						rolePositionTimeouts.put(bestSupporter, pair);
					}
				}
				
				
			}
		}
		
		
		private SupportRole findFastestBot(IVector2 destination, List<SupportRole> supporter)
		{
			SupportRole bestSupporter = null;
			double fastestTime = Double.MAX_VALUE;
			for (SupportRole role : supporter)
			{
				BangBangTrajectory2D trajectory = TrajectoryGenerator.generatePositionTrajectory(role.getBot(),
						destination);
				if (trajectory.getTotalTime() < fastestTime)
				{
					fastestTime = trajectory.getTotalTime();
					bestSupporter = role;
				}
			}
			return bestSupporter;
		}
		
		
		private SupportRole findCoveringBot(SupportPosition position)
		{
			final double distanceForNearness = SupportPositionGenerationCalc.getMinSupporterDistance();
			
			for (Map.Entry<ARole, PositionTimeoutPair> entry : rolePositionTimeouts.entrySet())
			{
				if (entry.getValue() == null)
				{
					continue;
				}
				
				final boolean positionCovered = position.isNearTo(entry.getValue().pos, distanceForNearness);
				final boolean coverageValid = entry.getValue().timeUntilNoChange > currentFrame.getWorldFrame()
						.getTimestamp();
				
				if (positionCovered && coverageValid)
				{
					return (SupportRole) entry.getKey();
				}
			}
			
			return null;
		}
	}
}
