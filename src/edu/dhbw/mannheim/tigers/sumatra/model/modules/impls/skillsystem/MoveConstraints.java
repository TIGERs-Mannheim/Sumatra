/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.07.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;


/**
 * Simple constraint holder
 * 
 * @author Gero
 */
public class MoveConstraints
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private boolean			isGoalie			= false;
	private boolean			isBallObstacle	= AIConfig.getSkills().getBallAsObstacle();
	private EGameSituation	gameSituation	= EGameSituation.GAME;
	private boolean			fastMove			= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public MoveConstraints()
	{
		
	}
	

	/**
	 * @param isGoalie
	 * @param isBallObstacle
	 * @param gameSituation
	 */
	public MoveConstraints(boolean isGoalie, boolean isBallObstacle, EGameSituation gameSituation)
	{
		this.setIsGoalie(isGoalie);
		this.setIsBallObstacle(isBallObstacle);
		this.setGameSituation(gameSituation);
	}
	

	public void setIsGoalie(boolean isGoalie)
	{
		this.isGoalie = isGoalie;
	}
	

	public boolean isGoalie()
	{
		return isGoalie;
	}
	

	public void setIsBallObstacle(boolean isBallObstacle)
	{
		this.isBallObstacle = isBallObstacle;
	}
	

	public boolean isBallObstacle()
	{
		return isBallObstacle;
	}
	

	public void setGameSituation(EGameSituation gameSituation)
	{
		this.gameSituation = gameSituation;
	}
	

	public EGameSituation getGameSituation()
	{
		return gameSituation;
	}
	

	public boolean isFastMove()
	{
		return fastMove;
	}
	

	public void setFastMove(boolean fastMove)
	{
		this.fastMove = fastMove;
	}
}
