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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;


/**
 * <p>
 * The attempt of this {@link IRoleAssigner}-implementation is that it accumulates the 'cost' for every given bot for
 * reaching every given roles target position and then chooses the possibility with the lowest 'cost'.
 * </p>
 * <p>
 * The term 'cost' is defined by the function {@link #calcCost(TrackedTigerBot, ARole, WorldFrame)}.
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
 * @see #assignRoles(AIInfoFrame, Map)
 * 
 * @author Gero, Oliver Steinbrecher
 * 
 */
public class OptimizedRoleAssigner implements IRoleAssigner
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger						log							= Logger.getLogger(getClass());
	
	
	private final Assigner					assigner;

	/** Maximum number of bots who have to receive a role */
	private static final int				MAX_NUM_BOTS				= 5;
	
	/**
	 * 1.: Number of bots/roles<br />
	 * 2.: Permutations/paths/possibilities (normally 24 if {@link #MAX_NUM_BOTS} == 5 - 1 keeper...)<br />
	 * 3.: Bot->Role combination: The position is the bots position in the 'assignees'-list, and the content the roles
	 * position in the 'requiredRoles'-list<br />
	 * <br />
	 * As it's a static array, we have to make it at least as big as the biggest field, #5.
	 */
	private final int[][][]					paths;
	
	/** Threshold which leads to end of further calculations because the cost for the regroup seem to be small enough */
	private static final float				CANCEL_COST_THRESHOLD	= 1;
	
	/**
	 * This is the metric which is assigned to a role-bot pair if there is no role left for the bot (see
	 * 'lessRolesThenBots')
	 */
	private static final int				NO_ROLE_COST				= 1;
	

//	/** The role-assignments from last frame */
//	private final Map<Integer, ARole>	roleCache					= new HashMap<Integer, ARole>();
	
	
	public OptimizedRoleAssigner()
	{
		this.assigner = new Assigner();
		
		// MAX_NUM_BOTS + 1 as we are indexing by 1 - 5 here...
		this.paths = new int[MAX_NUM_BOTS + 1][(int) AIMath.faculty(MAX_NUM_BOTS)][MAX_NUM_BOTS];
		for (int k = 2; k <= MAX_NUM_BOTS; k++)
		{
			PermutationGenerator gen = new PermutationGenerator(k);
			int i = 0;
			while (gen.hasMore())
			{
				int[] next = gen.getNext();
				this.paths[k][i] = next;
				i++;
			}
		}
	}
	

	// --------------------------------------------------------------
	// --- methods --------------------------------------------------
	// --------------------------------------------------------------
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
	public void assignRoles(Collection<TrackedTigerBot> assignees, List<ARole> rolesToAssign,
			Map<Integer, ARole> assignments, AIInfoFrame frame)
	{
//		log.info("roleassigner called");
//		log.debug("assignees:" + assignees);
//		log.debug("rolesToAssign:" + rolesToAssign);
//		log.debug("assignments:" + assignments);
		
		// ##### Check for easy cases
		final int numTigers = assignees.size();
		final int numRoles = rolesToAssign.size();
		
		if (numTigers == 0 || numRoles == 0)
		{
//			log.warn("No roles or tigers left, cannot assign!");
			return; // Return silently, as this is normal
		}
		// 'numRoles > 0'
		

		if (numTigers == 1)
		{
			TrackedTigerBot bot = assignees.toArray(new TrackedTigerBot[1])[0];
			if (numRoles != 1)
			{
				// 'numRoles > 1' !
				log.warn("More roles then bots in this assignment!");
				// Anyway, take first role in rolesToAssign
			}
			assigner.assign(bot, assignees, rolesToAssign.get(0), assignments);
			return; // That was easy!
		}
		
		boolean lessRolesThenBots = false;
		if (numRoles < numTigers)
		{
			lessRolesThenBots = true;
			// Stay silent. No perfect situation, but... who cares =)
//			log.warn("Less roles then bots, OptimizedRoleAssigner may be inefficient!");
		} else if (numRoles > numTigers)
		{
			log.warn("More roles then bots in role-assignment!");
		}
		

		// ##### Find cheapest possibility to regroup
		

		// Array that stores the costs a assignment of Tiger->Role would result in
		int[][] combinationsCost = new int[numTigers][numTigers]; // No more roles then bots...
		
		// Compare costs to find cheapest path
		int[] cheapestPath = null;
		int cheapestCost = Integer.MAX_VALUE;
		for (int c = 0; c < AIMath.faculty(numTigers); c++)
		{
			// Selected a combination path through our possible combinations, now step through this paths Tiger->Role
			// assignments and cumulate costs
			int cost = 0;
			int tigerI = 0;
			for (TrackedTigerBot tBot : assignees)
			{
				// Retrieve the path through the possibilities from the paths-store...
				int roleI = paths[numTigers][c][tigerI];
				
				int tempCost = combinationsCost[tigerI][roleI]; // Get cost from cost-store
				
				if (tempCost == 0) // Not calculated yet!
				{
					// Less roles then bots...
					if (lessRolesThenBots && roleI > rolesToAssign.size() - 1)
					{
						tempCost = NO_ROLE_COST;
					} else
					{
						tempCost = calcCost(tBot, rolesToAssign.get(roleI), frame.worldFrame);
					}
					combinationsCost[tigerI][roleI] = tempCost; // Store for reuse
				}
				cost += tempCost;
				
				tigerI++;
			}
			
			if (cost < cheapestCost)
			{
				cheapestCost = cost;
				cheapestPath = paths[numTigers][c]; // Return current path
			}
			
			if (cheapestCost < CANCEL_COST_THRESHOLD)
			{
				break;
			}
		}
		

		// Apply cheapest bot<->role path
		int tiger = 0;
		Iterator<TrackedTigerBot> it = assignees.iterator(); // Needed because of the 'remove'-operation in assign!
		while (it.hasNext())
		{
			TrackedTigerBot tBot = it.next();
			int roleI = cheapestPath[tiger];
			
			if (lessRolesThenBots && roleI > rolesToAssign.size() - 1)
			{
				tiger++;
				continue; // No role for this bot!
			}
			
			ARole role = rolesToAssign.get(roleI);
			assigner.assign(tBot, it, role, assignments);
			
			tiger++;
		}
		

//		// Update roleCache
//		roleCache.putAll(assignments);
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
	 * @param wFrame
	 * @return The cost this tiger would have to effort to take this role
	 */
	private int calcCost(TrackedTigerBot tiger, ARole role, WorldFrame wFrame)
	{
		IVector2 dest = role.getDestination();
		if (dest == null)
		{
			log.fatal("Role doesn't contain a destination!");
		}
		return Math.round(AIMath.distancePP(tiger, dest));
	}
}
