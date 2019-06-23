/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2011
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;


/**
 * Save data of a flying things in a file.
 * 
 * @author Birgit
 * 
 */
public final class WriteFlyData
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log			= Logger.getLogger(WriteFlyData.class.getName());
	
	private static volatile WriteFlyData	instance;
	
	private final double[][]					posOrig;
	private final double[][]					posCorr;
	private final double[]						distance;
	private final double[]						heightIntern;
	private final double[]						heightOut;
	private final boolean[]						isFlying;
	
	private static final int					SAVED_ITEMS	= 10000;
	private int										count;
	
	private FileOutputStream					fop;
	private final File							f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private WriteFlyData()
	{
		f = new File(WPConfig.FLYING_DEBUGFILE);
		
		posOrig = new double[SAVED_ITEMS][2];
		posCorr = new double[SAVED_ITEMS][2];
		distance = new double[SAVED_ITEMS];
		heightIntern = new double[SAVED_ITEMS];
		heightOut = new double[SAVED_ITEMS];
		isFlying = new boolean[SAVED_ITEMS];
		
		count = 0;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static WriteFlyData getInstance()
	{
		if (instance == null)
		{
			instance = new WriteFlyData();
		}
		return instance;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 *
	 */
	public void clear()
	{
		
		for (int i = 0; i < SAVED_ITEMS; i++)
		{
			posOrig[i][0] = 0;
			posOrig[i][1] = 0;
			posCorr[i][0] = 0;
			posCorr[i][1] = 0;
			heightIntern[i] = 0;
			heightOut[i] = 0;
			distance[i] = 0;
			isFlying[i] = false;
		}
		count = 0;
	}
	
	
	/**
	 * 
	 * @param posOrigX
	 * @param posOrigY
	 * @param posCorrX
	 * @param posCorrY
	 * @param heightIntern
	 * @param heightOut
	 * @param distance
	 * @param isFlying
	 */
	@SuppressWarnings(value = { "unused" })
	public void addDataSet(final double posOrigX, final double posOrigY, final double posCorrX, final double posCorrY,
			final double heightIntern, final double heightOut, final double distance, final boolean isFlying)
	{
		if (!WPConfig.DEBUG_FLYING)
		{
			return;
		}
		if (isFull())
		{
			write();
			clear();
		}
		
		posOrig[count][0] = posOrigX;
		posOrig[count][1] = posOrigY;
		posCorr[count][0] = posCorrX;
		posCorr[count][1] = posCorrY;
		this.heightIntern[count] = heightIntern;
		this.heightOut[count] = heightOut;
		this.distance[count] = distance;
		this.isFlying[count] = isFlying;
		
		count++;
	}
	
	
	/**
	 *
	 */
	public void write()
	{
		String str;
		
		for (int i = 0; i < count; i++)
		{
			str = "" + i;
			str += "\t " + posOrig[i][0];
			str += "\t " + posOrig[i][1];
			str += "\t " + posCorr[i][0];
			str += "\t " + posCorr[i][1];
			str += "\t " + heightIntern[i];
			str += "\t " + heightOut[i];
			str += "\t " + distance[i];
			
			if (isFlying[i])
			{
				str += " 1";
			} else
			{
				str += " 0";
			}
			
			str += " \n";
			try
			{
				fop.write(str.getBytes());
			} catch (final IOException err)
			{
				log.error("IOException", err);
			}
		}
		
	}
	
	
	/**
	 *
	 */
	public void close()
	{
		try
		{
			fop.close();
		} catch (final IOException err)
		{
			log.error("IOException", err);
		}
		
	}
	
	
	/**
	 *
	 */
	public void open()
	{
		try
		{
			fop = new FileOutputStream(f);
			
		} catch (final FileNotFoundException err)
		{
			log.error("IOException", err);
		}
		
		String header = "#";
		
		header += " posOrigX \t|";
		header += " posOrigY \t|";
		header += " posCorrX \t|";
		header += " posCorrY \t|";
		header += " heightIntern \t|";
		header += " heightOut \t|";
		header += " distance \t|";
		header += " isFlying \t|";
		header += " \n";
		try
		{
			fop.write(header.getBytes());
		} catch (final IOException err)
		{
			log.error("IOException", err);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean isFull()
	{
		return ((count) > SAVED_ITEMS);
	}
}
