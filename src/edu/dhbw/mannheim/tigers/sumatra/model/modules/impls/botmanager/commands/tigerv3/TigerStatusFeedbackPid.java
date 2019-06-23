/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 26, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.INumberListable;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerStatusFeedbackPid extends ACommand implements INumberListable
{
	@SerialData(type = ESerialDataType.INT16)
	private int	t		= 0;
	
	/** [mm/s^2], [mrad/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	in[]	= new int[3];
	/** [mm/s^2], [mrad/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	out[]	= new int[3];
	/** [mm/s^2], [mrad/s^2] */
	@SerialData(type = ESerialDataType.INT16)
	private int	set[]	= new int[3];
	
	
	/**
	 * 
	 */
	public TigerStatusFeedbackPid()
	{
		super(ECommand.CMD_STATUS_FEEDBACK_PID, false);
	}
	
	
	/**
	 * @return in
	 */
	public IVector3 getIn()
	{
		return new Vector3(in[0] / 1000.0f, in[1] / 1000.0f, in[2] / 1000.0f);
	}
	
	
	/**
	 * @return out
	 */
	public IVector3 getOut()
	{
		return new Vector3(out[0] / 1000.0f, out[1] / 1000.0f, out[2] / 1000.0f);
	}
	
	
	/**
	 * @return set
	 */
	public IVector3 getSet()
	{
		return new Vector3(set[0] / 1000.0f, set[1] / 1000.0f, set[2] / 1000.0f);
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> nbrs = new ArrayList<>(12);
		nbrs.add(t / 1000f);
		for (int i = 0; i < 3; i++)
		{
			nbrs.add(in[i] / 1000.0f);
		}
		for (int i = 0; i < 3; i++)
		{
			nbrs.add(out[i] / 1000.0f);
		}
		for (int i = 0; i < 3; i++)
		{
			nbrs.add(set[i] / 1000.0f);
		}
		return nbrs;
	}
	
	
	/**
	 * @return the t
	 */
	public final float getT()
	{
		return t / 1000f;
	}
}
