/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 25, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.AugmWrapper;
import edu.dhbw.mannheim.tigers.sumatra.util.network.MulticastUDPTransmitter;


/**
 * Send data to augmented reality app
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AugmentedDataSender
{
	private static final Logger					log			= Logger.getLogger(AugmentedDataSender.class.getName());
	
	private static final int						LOCAL_PORT	= 42000;
	private static final int						PORT			= 42001;
	private static final String					ADDRESS		= "224.5.23.3";
	private static final int						PERIOD		= 20;
	
	private final MulticastUDPTransmitter		transmitter;
	
	private final BlockingDeque<IRecordFrame>	frameQueue	= new LinkedBlockingDeque<IRecordFrame>(1);
	
	private ScheduledExecutorService				service		= null;
	
	private ETeamColor								teamColor	= ETeamColor.BLUE;
	
	private final AugmentedDataTransformer		transformer	= new AugmentedDataTransformer();
	
	
	/**
	  * 
	  */
	public AugmentedDataSender()
	{
		transmitter = new MulticastUDPTransmitter(LOCAL_PORT, ADDRESS, PORT);
	}
	
	
	/**
	 */
	public void start()
	{
		if (service == null)
		{
			service = Executors
					.newSingleThreadScheduledExecutor(new NamedThreadFactory("AugmSender"));
			service.scheduleAtFixedRate(new Sender(), 0, PERIOD, TimeUnit.MILLISECONDS);
		}
	}
	
	
	/**
	 */
	public void stop()
	{
		if (service != null)
		{
			service.shutdown();
			try
			{
				service.awaitTermination(PERIOD * 2, TimeUnit.MILLISECONDS);
			} catch (InterruptedException err)
			{
			}
			service = null;
		}
	}
	
	
	/**
	 * @param frame
	 */
	public void addFrame(final IRecordFrame frame)
	{
		if (frame.getTeamColor() == teamColor)
		{
			frameQueue.pollLast();
			frameQueue.add(frame);
		}
	}
	
	
	/**
	 * @param frame
	 */
	private void sendFrame(final IRecordFrame frame)
	{
		AugmWrapper wrapper = transformer.createAugmWrapper(frame);
		byte[] data = wrapper.toByteArray();
		transmitter.send(data);
		
		// FileOutputStream fos;
		// try
		// {
		// fos = new FileOutputStream("out/frame_" + frame.getWorldFrame().getSystemTime().getTime() + ".bin");
		// fos.write(data);
		// fos.close();
		// } catch (FileNotFoundException err)
		// {
		// err.printStackTrace();
		// } catch (IOException err)
		// {
		// err.printStackTrace();
		// }
	}
	
	
	private class Sender implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				IRecordFrame frame = frameQueue.poll(PERIOD, TimeUnit.MILLISECONDS);
				if (frame != null)
				{
					sendFrame(frame);
				}
			} catch (InterruptedException err)
			{
				log.warn("Interrupted in Sender Thread");
			} catch (Exception err)
			{
				log.error("Error in AugmSender Thread", err);
			}
		}
	}
}
