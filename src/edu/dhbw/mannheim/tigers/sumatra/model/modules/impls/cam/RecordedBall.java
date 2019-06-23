/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;


/**
 * Ball simulation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RecordedBall implements ISimulatedBall
{
	private static final Logger	log		= Logger.getLogger(RecordedBall.class.getName());
	
	private final List<CamBall>	balls;
	private int							ballIdx	= 0;
	private long						timestamp;
	
	
	/**
	 * @param file
	 */
	public RecordedBall(final String file)
	{
		List<CamBall> balls;
		try
		{
			Stream<String> lines = Files.lines(Paths.get(file));
			balls = lines.filter(line -> !line.startsWith("#"))
					.map(line -> line.split(","))
					.map(arr -> new CamBall(0, 0, Float.valueOf(arr[3]), Float.valueOf(arr[4]), 0, 0, 0, Long
							.valueOf(arr[1]), Integer.valueOf(arr[2])))
					.collect(Collectors.toList());
			lines.close();
		} catch (IOException err)
		{
			log.error("Could not open file: " + file, err);
			balls = new ArrayList<>(1);
		}
		this.balls = balls;
		if (!balls.isEmpty())
		{
			timestamp = balls.get(0).getTimestamp();
		} else
		{
			balls.add(new CamBall(0, 0, 0, 0, 0, 0, 0, System.nanoTime(), 0));
			timestamp = System.nanoTime();
		}
	}
	
	
	@Override
	public void step(final float dt)
	{
		timestamp += dt * 1e9;
		while ((ballIdx < (balls.size() - 1)) && (balls.get(ballIdx).getTimestamp() < timestamp))
		{
			// TODO multiple balls?!
			ballIdx++;
		}
	}
	
	
	/**
	 * @return
	 */
	@Override
	public CamBall getCamBall()
	{
		return balls.get(ballIdx);
	}
	
	
	@Override
	public void setVel(final IVector2 vel)
	{
	}
	
	
	@Override
	public void addVel(final IVector3 vector3)
	{
	}
	
	
	@Override
	public void setPos(final IVector3 pos)
	{
	}
	
	
	@Override
	public IVector3 getVel()
	{
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public IVector3 getPos()
	{
		return getCamBall().getPos();
	}
}
