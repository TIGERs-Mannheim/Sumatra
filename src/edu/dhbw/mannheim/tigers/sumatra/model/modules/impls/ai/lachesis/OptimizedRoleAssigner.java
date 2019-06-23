/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.PermutationGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.AScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.EScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.FeatureScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.PenaltyScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * <p>
 * The attempt of this {@link IRoleAssigner}-implementation is that it accumulates the 'cost' for every given bot for
 * reaching every given roles target position and then chooses the possibility with the lowest 'cost'.
 * </p>
 * <p>
 * The term 'cost' is defined by the function {@link #calcCost(ARole, IVector2, AthenaAiFrame)}.
 * </p>
 * 
 * Other things then distances between current and target position which are worth considering:
 * <ul>
 * <li>Rotation</li>
 * <li>current speed</li>
 * <li>(battery status)</li>
 * <li>(...)</li>
 * </ul>
 * 
 * @see OptimizedRoleAssigner#assignRoles(Collection, List, MetisAiFrame)
 * 
 * @author Gero, Oliver Steinbrecher
 * 
 */
public class OptimizedRoleAssigner implements IRoleAssigner
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log							= Logger.getLogger(OptimizedRoleAssigner.class.getName());
	
	
	private final Assigner			assigner;
	
	/** Maximum number of bots who have to receive a role (dynamically increased) */
	private static int				maxNumBots					= 0;
	
	/**
	 * 1.: Number of bots/roles<br />
	 * 2.: Permutations/paths/possibilities (normally 24 if {@link #maxNumBots} == 5 - 1 keeper...)<br />
	 * 3.: Bot->Role combination: The position is the bots position in the 'assignees'-list, and the content the roles
	 * position in the 'requiredRoles'-list<br />
	 * <br />
	 * As it's a static array, we have to make it at least as big as the biggest field, #5.
	 */
	private static int[][][]		paths;
	
	/** Threshold which leads to end of further calculations because the cost for the regroup seem to be small enough */
	private static final float		CANCEL_COST_THRESHOLD	= 1;
	
	/**
	 * This is the metric which is assigned to a role-bot pair if there is no role left for the bot (see
	 * 'lessRolesThenBots')
	 */
	private static final int		NO_ROLE_COST				= 1;
	
	private Map<EScore, AScore>	scores;
	
	
	/**
	 */
	public OptimizedRoleAssigner()
	{
		assigner = new Assigner();
		initPaths(6);
		scores = new HashMap<EScore, AScore>();
		
		scores.put(EScore.PENALTY, new PenaltyScore());
		// scores.put(EScore.BOT_ROLE_DISTANCE, new RoleToBotScore());
		// scores.put(EScore.BALL_ROLE_DISTANCE, new RoleToBallScore());
		scores.put(EScore.FEATURES, new FeatureScore());
	}
	
	
	// --------------------------------------------------------------
	// --- methods --------------------------------------------------
	// --------------------------------------------------------------
	/**
	 * Checks if paths is initialized for enough bots.
	 * If not, paths will be reinitialized with the given bot number
	 * 
	 * @param numBots
	 */
	private static void initPaths(int numBots)
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
	 * <p>
	 * <u>Idea:</u><br/>
	 * <ul>
	 * <li>First, the costs for all possible bot-role combinations are calculated, and stored in the
	 * 'combinationsCost'-array</li>
	 * <li>Second, the cumulative costs for all permutation-paths (which are stored in the {@link #paths}-array) through
	 * the bot-role-field (combinationsCost-array) are calculated, and the cheapest is stored</li>
	 * <li>Finally, the cheapest path is iterated, and the selected bot-role combinations are stored in the HashMap which
	 * is returned</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * The trickiest part is the usage of the arrays, but there are (hopefully extensively) explained where they are
	 * used.
	 * </p>
	 */
	@Override
	public void assignRoles(Collection<TrackedTigerBot> assignees, List<ARole> rolesToAssign, MetisAiFrame frame)
	{
		initPaths(frame.getWorldFrame().tigerBotsAvailable.size());
		
		// ##### Check for easy cases
		final int numTigers = assignees.size();
		final int numRoles = rolesToAssign.size();
		
		if ((assignees.isEmpty()) || (rolesToAssign.isEmpty()))
		{
			return;
		}
		
		if (numTigers == 1)
		{
			final TrackedTigerBot bot = assignees.toArray(new TrackedTigerBot[1])[0];
			if (numRoles != 1)
			{
				// 'numRoles > 1' !
				log.warn("More roles then bots in this assignment!");
				// Anyway, take first role in rolesToAssign
			}
			assigner.assign(bot, assignees, rolesToAssign.get(0));
			// That was easy!
			return;
		}
		
		boolean lessRolesThenBots = false;
		if (numRoles < numTigers)
		{
			lessRolesThenBots = true;
		} else if (numRoles > numTigers)
		{
			log.warn("More roles then bots in role-assignment! " + numRoles + ">" + numTigers);
		}
		
		
		// ##### Find cheapest possibility to regroup
		
		
		// Array that stores the costs a assignment of Tiger->Role would result in
		// No more roles then bots..
		final int[][] combinationsCost = new int[numTigers][numTigers];
		
		// Compare costs to find cheapest path
		int[] cheapestPath = null;
		int cheapestCost = Integer.MAX_VALUE;
		for (int c = 0; c < SumatraMath.faculty(numTigers); c++)
		{
			// Selected a combination path through our possible combinations, now step through this paths Tiger->Role
			// assignments and cumulate costs
			int cost = 0;
			int tigerI = 0;
			for (final TrackedTigerBot tBot : assignees)
			{
				// Retrieve the path through the possibilities from the paths-store...
				final int roleI = paths[numTigers][c][tigerI];
				
				// Get cost from cost-store
				int tempCost = combinationsCost[tigerI][roleI];
				
				// Not calculated yet!
				if (tempCost == 0)
				{
					// Less roles then bots...
					if (lessRolesThenBots && (roleI > (rolesToAssign.size() - 1)))
					{
						tempCost = NO_ROLE_COST;
					} else
					{
						tempCost = calcCost(tBot, rolesToAssign.get(roleI), frame);
					}
					// Store for reuse
					combinationsCost[tigerI][roleI] = tempCost;
				}
				cost += tempCost;
				
				tigerI++;
			}
			
			if (cost < cheapestCost)
			{
				cheapestCost = cost;
				// Return current path
				cheapestPath = paths[numTigers][c];
			}
			
			if (cheapestCost < CANCEL_COST_THRESHOLD)
			{
				break;
			}
		}
		
		if (cheapestPath == null)
		{
			throw new IllegalStateException("No cheapestPath found. This should not happen.");
		}
		
		
		// Apply cheapest bot<->role path
		int tiger = 0;
		// Needed because of the 'remove'-operation in assign!
		final Iterator<TrackedTigerBot> it = assignees.iterator();
		while (it.hasNext())
		{
			final TrackedTigerBot tBot = it.next();
			final int roleI = cheapestPath[tiger];
			
			if (lessRolesThenBots && (roleI > (rolesToAssign.size() - 1)))
			{
				tiger++;
				// No role for this bot!
				continue;
			}
			
			final ARole role = rolesToAssign.get(roleI);
			assigner.assign(tBot, it, role);
			
			tiger++;
		}
	}
	
	
	/**
	 * This is the point where you can add some deeper thoughts to the whole calculation very comfortably
	 * <p>
	 * Values have to be <u>between</u> 0 and {@link Integer#MAX_VALUE}.<br/>
	 * <strong>ATTENTION:</strong> Returning 0 is equivalent to "not yet calculated"!
	 * </p>
	 * 
	 * @param tiger
	 * @param role
	 * @param frame
	 * @return The cost this tiger would have to effort to take this role
	 */
	private int calcCost(TrackedTigerBot tiger, ARole role, MetisAiFrame frame)
	{
		// use this method to deactivate a score. there is not yet a gui interface TODO unassigned create gui
		// scores.get(EScore.BALL_ROLE_DISTANCE).setActive(true);
		int score = 0;
		for (AScore method : scores.values())
		{
			score += method.calcScore(tiger, role, frame);
		}
		return score;
	}
	
	
	/**
	 * This method can be used to assign a set of roles to a set of positions.
	 * The idea is, that you can use this method in your play, if you have a number
	 * of equal roles and want to assign them to some destinations.<br>
	 * Number of roles and positions must be equal!
	 * 
	 * @param roles
	 * @param positions
	 * @param frame
	 * @return
	 */
	public Map<ARole, IVector2> assign(List<ARole> roles, List<IVector2> positions, AthenaAiFrame frame)
	{
		Map<ARole, IVector2> resultMap = new HashMap<ARole, IVector2>();
		initPaths(roles.size());
		
		if ((roles.size() != positions.size()))
		{
			throw new IllegalArgumentException("number of roles and positions must be equal");
		}
		
		if (roles.isEmpty())
		{
			// empty map
			return resultMap;
		}
		
		// Array that stores the costs a assignment of Tiger->Role would result in
		// No more roles then bots..
		final int[][] combinationsCost = new int[roles.size()][roles.size()];
		
		// Compare costs to find cheapest path
		int[] cheapestPath = null;
		int cheapestCost = Integer.MAX_VALUE;
		for (int c = 0; c < SumatraMath.faculty(roles.size()); c++)
		{
			// Selected a combination path through our possible combinations, now step through this paths Tiger->Role
			// assignments and cumulate costs
			int cost = 0;
			int tigerI = 0;
			for (final ARole role : roles)
			{
				// Retrieve the path through the possibilities from the paths-store...
				final int posI = paths[roles.size()][c][tigerI];
				
				// Get cost from cost-store
				int tempCost = combinationsCost[tigerI][posI];
				
				// Not calculated yet!
				if (tempCost == 0)
				{
					// Less roles then bots...
					tempCost = calcCost(role, positions.get(posI), frame);
					// Store for reuse
					combinationsCost[tigerI][posI] = tempCost;
				}
				cost += tempCost;
				
				tigerI++;
			}
			
			if (cost < cheapestCost)
			{
				cheapestCost = cost;
				// Return current path
				cheapestPath = paths[roles.size()][c];
			}
			
			if (cheapestCost < CANCEL_COST_THRESHOLD)
			{
				break;
			}
		}
		
		if (cheapestPath == null)
		{
			throw new IllegalStateException("No cheapestPath found. This should not happen.");
		}
		
		
		// Apply cheapest bot<->role path
		int tiger = 0;
		// Needed because of the 'remove'-operation in assign!
		final Iterator<ARole> it = roles.iterator();
		while (it.hasNext())
		{
			final ARole role = it.next();
			final int posI = cheapestPath[tiger];
			
			resultMap.put(role, positions.get(posI));
			tiger++;
		}
		
		return resultMap;
	}
	
	
	/**
	 * Costs for role to get to position.
	 * This is just the distance between roles pos to position atm.
	 * 
	 * @param role
	 * @param position
	 * @param frame
	 * @return The cost this tiger would have to effort to take this role
	 */
	private int calcCost(final ARole role, final IVector2 position, final AthenaAiFrame frame)
	{
		AScore distScore = scores.get(EScore.BOT_ROLE_DISTANCE);
		return distScore.calcScoreOnPos(position, role, frame);
	}
}
