/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 24, 2014
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.DoubleDefPointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.DriveOnLinePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.StopDefPointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public class DefensePlay extends APlay
{
	private final List<DefenderRole>				listDefender				= new ArrayList<DefenderRole>();
	
	private final IDefensePointCalc				defensePointCalculator	= new DoubleDefPointCalc(
																									new DriveOnLinePointCalc());
	private final IDefensePointCalc				stopCalculator				= new StopDefPointCalc(defensePointCalculator);
	private IDefensePointCalc						currentCalculator			= defensePointCalculator;
	
	private Map<DefenderRole, DefensePoint>	defDistribution			= new HashMap<DefenderRole, DefensePoint>();
	
	
	/**
	 * 
	 */
	public DefensePlay()
	{
		super(EPlay.DEFENSIVE);
	}
	
	
	/**
	 * @return
	 */
	public Map<DefenderRole, DefensePoint> getDefenderDistribution()
	{
		return defDistribution;
	}
	
	
	/**
	 * Logic cannot be moved to roles, because i have got to know which bots i got.
	 * 
	 * @param currentFrame
	 */
	@Override
	public void updateBeforeRoles(final AthenaAiFrame currentFrame)
	{
		
		List<FoeBotData> foeBotDataList = currentFrame.getTacticalField().getDangerousFoeBots();
		
		// other defenders
		Map<DefenderRole, DefensePoint> defenderDistribution = currentCalculator.getDefenderDistribution(
				currentFrame, listDefender,
				foeBotDataList);
		
		defDistribution = defenderDistribution;
		
		defenderDistribution.forEach(
				(defender, defensePoint) -> defender.setDefPoint(defensePoint));
	}
	
	
	@Override
	protected void onRoleRemoved(final ARole role)
	{
		listDefender.remove(role);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
		switch (gameState)
		{
			case PREPARE_PENALTY_THEY:
				assert false : "Play can not deal with PenaltyKick!";
			case PREPARE_PENALTY_WE:
			case STOPPED:
			case HALTED:
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
			case DIRECT_KICK_THEY:
			case DIRECT_KICK_WE:
			case CORNER_KICK_THEY:
			case CORNER_KICK_WE:
			case GOAL_KICK_THEY:
			case GOAL_KICK_WE:
			case THROW_IN_THEY:
			case THROW_IN_WE:
				currentCalculator = stopCalculator;
				break;
			default:
				currentCalculator = defensePointCalculator;
				break;
		}
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		if (listDefender.isEmpty())
		{
			throw new IllegalStateException("There is no role left to be deleted");
		}
		return listDefender.remove(listDefender.size() - 1);
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		DefenderRole def = new DefenderRole();
		listDefender.add(def);
		return def;
	}
	
	
}
