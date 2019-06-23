/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.tigers.sumatra.data.collector.IExportable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ExportableFrameInfo implements IExportable
{
	private long frameId;
	private int camId;
	private long tCapture;
	private long tSent;
	private long tRecorded;
	private long camFrameId;
	
	
	/**
	 *
	 */
	public ExportableFrameInfo()
	{
	}
	
	
	/**
	 * @param frameId
	 * @param camId
	 * @param tCapture
	 * @param tSent
	 * @param tRecorded
	 * @param camFrameId
	 */
	public ExportableFrameInfo(final long frameId, final int camId, final long tCapture, final long tSent,
			final long tRecorded, final long camFrameId)
	{
		super();
		this.frameId = frameId;
		this.camId = camId;
		this.tCapture = tCapture;
		this.tSent = tSent;
		this.tRecorded = tRecorded;
		this.camFrameId = camFrameId;
	}
	
	
	/**
	 * @param list
	 * @return
	 */
	public static ExportableFrameInfo fromNumberList(final List<? extends Number> list)
	{
		return new ExportableFrameInfo(list.get(0).longValue(),
				list.get(1).intValue(),
				list.get(2).longValue(),
				list.get(3).longValue(),
				list.get(4).longValue(),
				list.get(5).longValue());
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(frameId);
		numbers.add(camId);
		numbers.add(tCapture);
		numbers.add(tSent);
		numbers.add(tRecorded);
		numbers.add(camFrameId);
		return numbers;
	}
	
	
	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("frameId", "camId", "tCapture", "tSent", "tRecorded", "camFrameId");
	}
	
	
	/**
	 * @return the frameId
	 */
	public long getFrameId()
	{
		return frameId;
	}
	
	
	/**
	 * @param frameId the frameId to set
	 */
	public void setFrameId(final long frameId)
	{
		this.frameId = frameId;
	}
	
	
	/**
	 * @return the camId
	 */
	public int getCamId()
	{
		return camId;
	}
	
	
	/**
	 * @param camId the camId to set
	 */
	public void setCamId(final int camId)
	{
		this.camId = camId;
	}
	
	
	/**
	 * @return the tCapture
	 */
	public long gettCapture()
	{
		return tCapture;
	}
	
	
	/**
	 * @param tCapture the tCapture to set
	 */
	public void settCapture(final long tCapture)
	{
		this.tCapture = tCapture;
	}
	
	
	/**
	 * @return the tSent
	 */
	public long gettSent()
	{
		return tSent;
	}
	
	
	/**
	 * @param tSent the tSent to set
	 */
	public void settSent(final long tSent)
	{
		this.tSent = tSent;
	}
	
	
	/**
	 * @return the tRecorded
	 */
	public long gettRecorded()
	{
		return tRecorded;
	}
	
	
	/**
	 * @param tRecorded the tRecorded to set
	 */
	public void settRecorded(final long tRecorded)
	{
		this.tRecorded = tRecorded;
	}
	
	
	/**
	 * @return the camFrameId
	 */
	public long getCamFrameId()
	{
		return camFrameId;
	}
	
	
	/**
	 * @param camFrameId the camFrameId to set
	 */
	public void setCamFrameId(final long camFrameId)
	{
		this.camFrameId = camFrameId;
	}
}
