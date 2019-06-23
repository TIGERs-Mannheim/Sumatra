/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 6, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.EBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotSkillEncTrain extends ABotSkill
{
	
	@SerialData(type = ESerialDataType.INT16)
	private int	startPos[]			= new int[2];
	
	@SerialData(type = ESerialDataType.INT16)
	private int	endPos[]				= new int[2];
	
	@SerialData(type = ESerialDataType.UINT8)
	private int	firstAngleSteps	= 0;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int	secondAngleSteps	= 0;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int	xyRepeat				= 0;
	
	
	/**
	 * 
	 */
	public BotSkillEncTrain()
	{
		super(EBotSkill.ENC_TRAIN);
	}
	
	
	/**
	 * @param startPos
	 * @param endPos
	 * @param firstAngleSteps
	 * @param secondAngleSteps
	 * @param xyRepeat
	 */
	public BotSkillEncTrain(final IVector2 startPos, final IVector2 endPos, final int firstAngleSteps,
			final int secondAngleSteps, final int xyRepeat)
	{
		super(EBotSkill.ENC_TRAIN);
		setStartPos(startPos);
		setEndPos(endPos);
		setFirstAngleSteps(firstAngleSteps);
		setSecondAngleSteps(secondAngleSteps);
		setXyRepeat(xyRepeat);
	}
	
	
	/**
	 * @param startPos the startPos to set
	 */
	public final void setStartPos(final IVector2 startPos)
	{
		this.startPos[0] = (int) startPos.x();
		this.startPos[1] = (int) startPos.y();
	}
	
	
	/**
	 * @param endPos the endPos to set
	 */
	public final void setEndPos(final IVector2 endPos)
	{
		this.endPos[0] = (int) endPos.x();
		this.endPos[1] = (int) endPos.y();
	}
	
	
	/**
	 * @param firstAngleSteps the firstAngleSteps to set
	 */
	public final void setFirstAngleSteps(final int firstAngleSteps)
	{
		this.firstAngleSteps = firstAngleSteps;
	}
	
	
	/**
	 * @param secondAngleSteps the secondAngleSteps to set
	 */
	public final void setSecondAngleSteps(final int secondAngleSteps)
	{
		this.secondAngleSteps = secondAngleSteps;
	}
	
	
	/**
	 * @param xyRepeat the xyRepeat to set
	 */
	public final void setXyRepeat(final int xyRepeat)
	{
		this.xyRepeat = xyRepeat;
	}
}
