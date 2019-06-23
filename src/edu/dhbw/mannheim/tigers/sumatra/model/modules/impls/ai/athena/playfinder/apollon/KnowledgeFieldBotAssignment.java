/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.PermutationGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * This KnowledgeField implementation uses the OptimizedRoleAssigner algorithm:
 * find the best bot->bot(role) mapping and sum up all distances.
 * the comparison ratio is: shortestDistSum / (fieldDiagonal * numBots)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Entity
public class KnowledgeFieldBotAssignment extends AKnowledgeField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** distance between to diagonal edges off the field (biggest possible distance) */
	private final transient double	fieldDiagonalDist;
	
	/** max bots to compare (per team), will be increased automatically */
	private transient int				maxNumBots					= 0;
	
	/**
	 * 1.: Number of bots<br />
	 * 2.: Permutations/paths/possibilities (normally 24 if {@link #maxNumBots} == 5 - 1 keeper...)<br />
	 * 3.: Bot->Role combination: The position is the bots position in the 'assignees'-list, and the content the roles
	 * position in the 'requiredRoles'-list<br />
	 * <br />
	 * As it's a static array, we have to make it at least as big as the biggest field, #5.
	 */
	private static int[][][]			paths;
	
	/** Threshold which leads to end of further calculations because the cost for the regroup seem to be small enough */
	private static final float			CANCEL_COST_THRESHOLD	= 1;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param tigerBots
	 * @param foeBots
	 * @param ball
	 * @param ballPossession
	 */
	public KnowledgeFieldBotAssignment(BotIDMapConst<TrackedTigerBot> tigerBots, BotIDMapConst<TrackedBot> foeBots,
			TrackedBall ball, BallPossession ballPossession)
	{
		super(tigerBots, foeBots, ball, ballPossession);
		fieldDiagonalDist = Math.sqrt(Math.pow(AIConfig.getGeometry().getFieldLength(), 2.0)
				+ Math.pow(AIConfig.getGeometry().getFieldLength(), 2.0));
		checkPaths(Math.max(tigerBots.size(), foeBots.size()));
	}
	
	
	/**
	 * Compare the two knowledgeFields
	 * 
	 * Current approach:
	 * median of distances from tigerBots, foeBots and ball
	 * 
	 * @param knowledgeField
	 * @return
	 */
	@Override
	public IComparisonResult compare(AKnowledgeField knowledgeField)
	{
		if (!(knowledgeField instanceof KnowledgeFieldBotAssignment))
		{
			throw new IllegalArgumentException("knowledgeField must be of Type KnowledgeFieldBotAssignment");
		}
		final KnowledgeFieldBotAssignment kfba = (KnowledgeFieldBotAssignment) knowledgeField;
		double result = 0;
		result += compareBotPositions(botMapToList(getTigerBots()), botMapToList(kfba.getTigerBots()));
		result += compareBotPositions(botMapToList(getFoeBots()), botMapToList(kfba.getFoeBots()));
		result += compareBallPosition(getBall(), kfba.getBall());
		result /= 3;
		return new ComparisonResult(result);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Checks if paths is initialized for enough bots.
	 * If not, paths will be reinitialized with the given bot number
	 * 
	 * @param numBots
	 */
	private void checkPaths(int numBots)
	{
		if (numBots > maxNumBots)
		{
			maxNumBots = numBots;
			// MAX_NUM_BOTS + 1 as we are indexing by 1 - 5 here...
			paths = new int[maxNumBots + 1][(int) SumatraMath.faculty(maxNumBots)][maxNumBots];
			for (int k = 2; k <= maxNumBots; k++)
			{
				final PermutationGenerator gen = new PermutationGenerator(k);
				int i = 0;
				while (gen.hasMore())
				{
					final int[] next = gen.getNext();
					paths[k][i] = next;
					i++;
				}
			}
		}
	}
	
	
	/**
	 * Current approach:
	 * score of distance between the two balls
	 * 
	 * @param ball1
	 * @param ball2
	 * @return equality factor (0-1)
	 */
	private double compareBallPosition(TrackedBall ball1, TrackedBall ball2)
	{
		return transformDistanceToScore(GeoMath.distancePP(ball1.getPos(), ball2.getPos()), fieldDiagonalDist);
	}
	
	
	/**
	 * <p>
	 * <u>Idea:</u><br/>
	 * <ul>
	 * <li>First, the costs for all possible bot-bot combinations are calculated, and stored in the
	 * 'combinationsCost'-array</li>
	 * <li>Second, the cumulative costs for all permutation-paths (which are stored in the {@link #paths}-array) through
	 * the bot-role-field (combinationsCost-array) are calculated, and the cheapest is stored</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The trickiest part is the usage of the arrays, but there are (hopefully extensively) explained where they are
	 * used.
	 * </p>
	 * @param bots1
	 * @param bots2
	 * @return
	 */
	public double compareBotPositions(List<TrackedBot> bots1, List<TrackedBot> bots2)
	{
		checkPaths(Math.max(bots1.size(), bots2.size()));
		
		// ##### Check for easy cases
		if (bots1.isEmpty())
		{
			// empty field is exactly identical
			return 1;
		}
		
		if (bots1.size() != bots2.size())
		{
			// we do not compare Fields with different numbers of bots
			return 0;
		}
		
		if ((bots1.size() == 1) && (bots2.size() == 1))
		{
			return transformDistanceToScore(GeoMath.distancePP(bots1.get(0).getPos(), bots2.get(0).getPos()),
					fieldDiagonalDist);
		}
		
		// ##### Find cheapest possibility to regroup
		// Array that stores the costs an assignment of Bot1->Bot2 would result in
		final int[][] combinationsCost = new int[bots1.size()][bots2.size()];
		
		// Compare costs to find cheapest path
		int cheapestCost = Integer.MAX_VALUE;
		final int facSize = (int) SumatraMath.faculty(bots1.size());
		for (int c = 0; c < facSize; c++)
		{
			// Selected a combination path through our possible combinations, now step through this paths Tiger->Role
			// assignments and cumulate costs
			int cost = 0;
			int tigerI = 0;
			for (final TrackedBot tBot : bots1)
			{
				// Retrieve the path through the possibilities from the paths-store...
				final int roleI = paths[bots1.size()][c][tigerI];
				
				// Get cost from cost-store
				int tempCost = combinationsCost[tigerI][roleI];
				// Not calculated yet!
				if (tempCost == 0)
				{
					tempCost = calcCost(tBot, bots2.get(roleI));
					// Store for reuse
					combinationsCost[tigerI][roleI] = tempCost;
				}
				cost += tempCost;
				
				tigerI++;
			}
			
			if (cost < cheapestCost)
			{
				cheapestCost = cost;
			}
			
			if (cheapestCost < CANCEL_COST_THRESHOLD)
			{
				break;
			}
		}
		
		return transformDistanceToScore(cheapestCost, fieldDiagonalDist * bots1.size());
	}
	
	
	/**
	 * This is the point where you can add some deeper thoughts to the whole calculation very comfortably
	 * <p>
	 * Values have to be <u>between</u> 0 and {@link Integer#MAX_VALUE}.<br/>
	 * <strong>ATTENTION:</strong> Returning 0 is equivalent to "not yet calculated"!
	 * </p>
	 * 
	 * @param bot1
	 * @param bot2
	 * @return The cost this tiger would have to effort to take this role
	 */
	private int calcCost(TrackedBot bot1, TrackedBot bot2)
	{
		return Math.round(GeoMath.distancePP(bot1.getPos(), bot2.getPos()));
	}
	
	
	private double transformDistanceToScore(double distance, double total)
	{
		return 1.0 - (distance / total);
	}
	
	
	/**
	 * Transform botmap to botlist
	 * 
	 * @param map
	 * @return
	 */
	private <T> List<TrackedBot> botMapToList(IBotIDMap<T> map)
	{
		final List<TrackedBot> list = new LinkedList<TrackedBot>();
		final Iterator<Map.Entry<BotID, T>> it = map.iterator();
		while (it.hasNext())
		{
			list.add((TrackedBot) it.next().getValue());
		}
		return list;
	}
	
	
	@Override
	public void initialize()
	{
		// nothing to do
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
