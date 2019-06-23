/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Data structure for bot output data
 */
public class ExportableBotOutput implements IExportable
{
	private final int id;
	private final ETeamColor color;
	private final long tReceive;
	private final TigerSystemMatchFeedback feedback;
	
	
	/**
	 * Create data structure
	 * 
	 * @param id
	 * @param color
	 * @param tReceive
	 * @param feedback
	 */
	public ExportableBotOutput(final int id, final ETeamColor color, final long tReceive,
			final TigerSystemMatchFeedback feedback)
	{
		this.id = id;
		this.color = color;
		this.tReceive = tReceive;
		this.feedback = feedback;
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(id);
		numbers.addAll(color.getNumberList());
		numbers.add(tReceive);
		numbers.addAll(feedback.getNumberList());
		return numbers;
	}
	
	
	@Override
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<>();
		headers.addAll(Arrays.asList("id", "color", "timestamp"));
		headers.addAll(feedback.getHeaders());
		return headers;
	}
}
