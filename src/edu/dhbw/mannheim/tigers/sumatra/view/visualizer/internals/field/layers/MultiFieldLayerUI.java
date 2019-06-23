/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.record.RecordPersistance;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel.EFieldTurn;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.ReplayWindow;


/**
 * This is a multilayerUI which can handle the painting for multiple {@link javax.swing.JLayer}.
 * Layers will be painted in the order they have been added.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class MultiFieldLayerUI
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger					log				= Logger.getLogger(MultiFieldLayerUI.class.getName());
	
	/**  */
	private final transient List<AFieldLayer>	layerList		= new CopyOnWriteArrayList<AFieldLayer>();
	
	
	private boolean									fancyPainting	= false;
	
	private boolean									recording		= false;
	private static final int						MAX_FRAMES		= 7200;
	private List<IRecordFrame>						aiFrameBuffer	= new ArrayList<IRecordFrame>(MAX_FRAMES);
	private RecordPersistance						persistance;
	
	private List<Path>								paths				= new ArrayList<Path>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public MultiFieldLayerUI()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 */
	public void update(AIInfoFrame aiFrame)
	{
		for (final AFieldLayer layer : layerList)
		{
			layer.update(aiFrame);
		}
		if (recording)
		{
			List<Path> pathsCopy = new ArrayList<Path>();
			for (Path path : paths)
			{
				if (path == null)
				{
					pathsCopy.add(null);
				} else
				{
					pathsCopy.add(path.copyLight());
				}
			}
			RecordFrame recFrame = new RecordFrame(aiFrame);
			recFrame.setPaths(pathsCopy);
			aiFrame.setPaths(pathsCopy);
			
			if (aiFrameBuffer.size() >= MAX_FRAMES)
			{
				aiFrameBuffer.remove(0);
			}
			aiFrameBuffer.add(aiFrame);
			// TODO conditionally persist
			persistance.addRecordFrame(recFrame);
		}
	}
	
	
	/**
	 * Updates all layers with the actual {@link AIInfoFrame}.
	 * 
	 * @param recFrame
	 */
	public void updateRecord(IRecordFrame recFrame)
	{
		for (final AFieldLayer layer : layerList)
		{
			layer.update(recFrame);
		}
	}
	
	
	/**
	 * Paint layers
	 * 
	 * @param g2
	 */
	public void paint(Graphics2D g2)
	{
		if (fancyPainting)
		{
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
		
		for (final AFieldLayer layer : layerList)
		{
			layer.paint(g2);
		}
	}
	
	
	/**
	 * Add Layer to paint.
	 * <strong>ATTENTION:</strong> Add layers in painting order! First in first paint.
	 * @param layer LayerUI to add
	 */
	public void addLayer(AFieldLayer layer)
	{
		layerList.add(layer);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Sets the visibility for a specified layer. No check whether layer is available!
	 * 
	 * @param name the layer
	 * @param visible
	 */
	public void setVisibility(EFieldLayer name, boolean visible)
	{
		for (final AFieldLayer layer : layerList)
		{
			if (layer.getType() == name)
			{
				layer.setVisible(visible);
				return;
			}
		}
	}
	
	
	/**
	 * Resets the visibility of all added layers to the initial value.
	 */
	public void setInitialVisibility()
	{
		for (final AFieldLayer layer : layerList)
		{
			layer.setInitialVisibility();
		}
	}
	
	
	/**
	 * Draws special DEBUG information on all layer.
	 * 
	 * @param debugInformationVisible
	 */
	public void setDebugInformationVisible(boolean debugInformationVisible)
	{
		for (final AFieldLayer layer : layerList)
		{
			layer.setDebugInformationVisible(debugInformationVisible);
		}
	}
	
	
	/**
	 * Returns specified layer.
	 * 
	 * @param name
	 * @return layer or null when layer cannot be found.
	 */
	public AFieldLayer getFieldLayer(EFieldLayer name)
	{
		for (final AFieldLayer layer : layerList)
		{
			if (layer.getType() == name)
			{
				return layer;
			}
		}
		return null;
	}
	
	
	/**
	 * @return the fancyPainting
	 */
	public boolean isFancyPainting()
	{
		return fancyPainting;
	}
	
	
	/**
	 * @param fancyPainting the fancyPainting to set
	 */
	public void setFancyPainting(boolean fancyPainting)
	{
		this.fancyPainting = fancyPainting;
	}
	
	
	/**
	 * @return the record
	 */
	public final boolean isRecord()
	{
		return recording;
	}
	
	
	/**
	 * @param record the record to set
	 * @param saving the saving to set
	 */
	public final void setRecording(boolean record, boolean saving)
	{
		// if we are currently recording, but stop is requested, we stop and show new window with recording result
		if (recording && !record && (aiFrameBuffer.size() > 0))
		{
			ReplayWindow replaceWindow = new ReplayWindow(aiFrameBuffer);
			replaceWindow.activate();
			// create a new list, because the old one will be used by the replayWindow
			aiFrameBuffer = new LinkedList<IRecordFrame>();
			if (saving)
			{
				savePersistance();
			}
			
		} else if (!recording && record)
		{
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
			String dbname = dt.format(new Date());
			persistance = new RecordPersistance("record_" + dbname);
		}
		recording = record;
	}
	
	
	/**
	 * saving Persistance
	 */
	public void savePersistance()
	{
		log.trace("Start: Last Persistance Save");
		persistance.save();
		log.trace("End: Last Persistance Save");
		
	}
	
	
	/**
	 * @param paths
	 */
	public void setPaths(List<Path> paths)
	{
		this.paths = paths;
	}
	
	
	/**
	 * Set fieldTurn in all layers
	 * 
	 * @param fieldTurn
	 */
	public void setFieldTurn(EFieldTurn fieldTurn)
	{
		for (AFieldLayer layer : layerList)
		{
			layer.setFieldTurn(fieldTurn);
		}
	}
}
