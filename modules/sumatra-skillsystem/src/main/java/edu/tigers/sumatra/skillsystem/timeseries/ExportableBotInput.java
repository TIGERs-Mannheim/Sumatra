/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * Data sent to a (real) bot
 */
public class ExportableBotInput implements IExportable
{
	private int id;
	private ETeamColor color;
	private long timestamp;
	private long tSent = 0;
	private IVector3 trajVel = Vector3f.UNINITIALIZED;
	private IVector3 trajPos = Vector3f.UNINITIALIZED;
	private IVector3 setVel = Vector3f.UNINITIALIZED;
	private IVector3 setPos = Vector3f.UNINITIALIZED;
	private IVector3 localVel = Vector3f.UNINITIALIZED;
	
	private double dribbleRpm = 0;
	private double kickSpeed = 0;
	private int kickDevice = EKickerDevice.STRAIGHT.getValue();
	private int kickMode = EKickerMode.DISARM.getId();
	private MoveConstraints moveConstraints = new MoveConstraints();
	
	
	/**
	 * @param id
	 * @param color
	 * @param timestamp
	 */
	public ExportableBotInput(final int id, final ETeamColor color, final long timestamp)
	{
		super();
		this.id = id;
		this.color = color;
		this.timestamp = timestamp;
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(id);
		numbers.addAll(color.getNumberList());
		numbers.add(timestamp);
		numbers.add(tSent);
		numbers.addAll(trajVel.getNumberList());
		numbers.addAll(trajPos.getNumberList());
		numbers.addAll(setVel.getNumberList());
		numbers.addAll(setPos.getNumberList());
		numbers.addAll(localVel.getNumberList());
		
		numbers.add(dribbleRpm);
		numbers.add(kickSpeed);
		numbers.add(kickDevice);
		numbers.add(kickMode);
		numbers.addAll(moveConstraints.getNumberList());
		return numbers;
	}
	
	
	@Override
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<>();
		headers.addAll(Arrays.asList("id", "color", "timestamp", "tSent",
				"trajVel_x", "trajVel_y", "trajVel_z",
				"trajPos_x", "trajPos_y", "trajPos_z",
				"setVel_x", "setVel_y", "setVel_z",
				"setPos_x", "setPos_y", "setPos_z",
				"localVel_x", "localVel_y", "localVel_z",
				"dribbleRpm", "kickSpeed", "kickDevice", "kickMode"));
		headers.addAll(moveConstraints.getHeaders());
		return headers;
	}
	
	
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(final int id)
	{
		this.id = id;
	}
	
	
	/**
	 * @return the color
	 */
	public ETeamColor getColor()
	{
		return color;
	}
	
	
	/**
	 * @param color the color to set
	 */
	public void setColor(final ETeamColor color)
	{
		this.color = color;
	}
	
	
	/**
	 * @return the timestamp
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(final long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	
	/**
	 * @param tSent
	 */
	public void settSent(final long tSent)
	{
		this.tSent = tSent;
	}
	
	
	/**
	 * @return the trajVel
	 */
	public IVector3 getTrajVel()
	{
		return trajVel;
	}
	
	
	/**
	 * @param trajVel the trajVel to set
	 */
	public void setTrajVel(final IVector3 trajVel)
	{
		this.trajVel = trajVel;
	}
	
	
	/**
	 * @return the trajPos
	 */
	public IVector3 getTrajPos()
	{
		return trajPos;
	}
	
	
	/**
	 * @param trajPos the trajPos to set
	 */
	public void setTrajPos(final IVector3 trajPos)
	{
		this.trajPos = trajPos;
	}
	
	
	/**
	 * @return the setVel
	 */
	public IVector3 getSetVel()
	{
		return setVel;
	}
	
	
	/**
	 * @param setVel the setVel to set
	 */
	public void setSetVel(final IVector3 setVel)
	{
		this.setVel = setVel;
	}
	
	
	/**
	 * @return the setPos
	 */
	public IVector3 getSetPos()
	{
		return setPos;
	}
	
	
	/**
	 * @param setPos the setPos to set
	 */
	public void setSetPos(final IVector3 setPos)
	{
		this.setPos = setPos;
	}
	
	
	/**
	 * @return the localVel
	 */
	public IVector3 getLocalVel()
	{
		return localVel;
	}
	
	
	/**
	 * @param localVel the localVel to set
	 */
	public void setLocalVel(final IVector3 localVel)
	{
		this.localVel = localVel;
	}
	
	
	public double getDribbleRpm()
	{
		return dribbleRpm;
	}
	
	
	public void setDribbleRpm(final double dribbleRpm)
	{
		this.dribbleRpm = dribbleRpm;
	}
	
	
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	public void setKickSpeed(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
	}
	
	
	public int getKickDevice()
	{
		return kickDevice;
	}
	
	
	public void setKickDevice(final int kickDevice)
	{
		this.kickDevice = kickDevice;
	}
	
	
	public int getKickMode()
	{
		return kickMode;
	}
	
	
	public void setKickMode(final int kickMode)
	{
		this.kickMode = kickMode;
	}
	
	
	public MoveConstraints getMoveConstraints()
	{
		return moveConstraints;
	}
	
	
	public void setMoveConstraints(final MoveConstraints moveConstraints)
	{
		this.moveConstraints = moveConstraints;
	}
}
