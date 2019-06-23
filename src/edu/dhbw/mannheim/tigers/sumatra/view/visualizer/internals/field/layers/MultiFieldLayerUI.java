/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2011
 * Author(s): Oliver Steinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel.EFieldTurn;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.Recorder;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.Recorder.ERecordMode;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay.ReplayWindow;


/**
 * This is a multilayerUI which can handle the painting for multiple {@link javax.swing.JLayer}.
 * Layers will be painted in the order they have been added.
 * 
 * @author Oliver Steinbrecher
 */
public class MultiFieldLayerUI
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private final transient List<AFieldLayer>	aiLayerList			= new CopyOnWriteArrayList<AFieldLayer>();
	private final transient List<AFieldLayer>	swfLayerList		= new CopyOnWriteArrayList<AFieldLayer>();
	private final transient List<AFieldLayer>	allLayerList		= new CopyOnWriteArrayList<AFieldLayer>();
	
	private final List<Layer>						layers				= new LinkedList<Layer>();
	
	private boolean									fancyPainting		= false;
	
	private boolean									recording			= false;
	private static final int						MAX_FRAMES			= 3600;
	private static final int						FRAME_BUFFER_SIZE	= 1000;
	private Recorder									recorder				= null;
	
	private SimpleWorldFrame						wFrame				= null;
	
	private IRecordFrame								aiFrameYellow		= null;
	private IRecordFrame								aiFrameBlue			= null;
	private Set<ETeamColor>							displayColors		= new HashSet<ETeamColor>();
	
	
	private static class Layer
	{
		private final AFieldLayer	layer;
		private final boolean		paintSwf;
		private final boolean		paintAif;
		
		
		/**
		 * @param layer
		 * @param paintSwf
		 * @param paintAif
		 */
		public Layer(final AFieldLayer layer, final boolean paintSwf, final boolean paintAif)
		{
			super();
			this.layer = layer;
			this.paintSwf = paintSwf;
			this.paintAif = paintAif;
		}
	}
	
	
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
	public void update(final IRecordFrame aiFrame)
	{
		if (aiFrame.getTeamColor() == ETeamColor.YELLOW)
		{
			aiFrameYellow = aiFrame;
		} else
		{
			aiFrameBlue = aiFrame;
		}
		if (recording)
		{
			RecordFrame recFrame = new RecordFrame(aiFrame);
			recorder.addRecordFrame(recFrame);
		}
	}
	
	
	/**
	 * Updates all layers with the actual {@link AIInfoFrame}.
	 * 
	 * @param frame
	 */
	public void updateWf(final SimpleWorldFrame frame)
	{
		wFrame = frame;
	}
	
	
	/**
	 * Paint layers
	 * 
	 * @param g
	 */
	public void paint(final Graphics2D g)
	{
		if (fancyPainting)
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
		
		for (Layer layer : layers)
		{
			if (layer.paintSwf)
			{
				layer.layer.paintSimpleWorldFrame(wFrame, g);
			}
			if (layer.paintAif)
			{
				for (ETeamColor color : displayColors)
				{
					switch (color)
					{
						case YELLOW:
							layer.layer.paintAiFrame(aiFrameYellow, g);
							break;
						case BLUE:
							layer.layer.paintAiFrame(aiFrameBlue, g);
							break;
						default:
							throw new IllegalArgumentException("The color " + color + " is not valid");
					}
				}
			}
		}
	}
	
	
	/**
	 * Add Layer to paint.
	 * <strong>ATTENTION:</strong> Add layers in painting order! First in first paint.
	 * 
	 * @param layer LayerUI to add
	 * @param fieldPanel
	 */
	public void addSwfLayer(final AFieldLayer layer, final IFieldPanel fieldPanel)
	{
		layer.setFieldPanel(fieldPanel);
		swfLayerList.add(layer);
		allLayerList.add(layer);
		layers.add(new Layer(layer, true, false));
	}
	
	
	/**
	 * Add Layer to paint.
	 * <strong>ATTENTION:</strong> Add layers in painting order! First in first paint.
	 * 
	 * @param layer LayerUI to add
	 * @param fieldPanel
	 */
	public void addAiLayer(final AFieldLayer layer, final IFieldPanel fieldPanel)
	{
		layer.setFieldPanel(fieldPanel);
		aiLayerList.add(layer);
		allLayerList.add(layer);
		layers.add(new Layer(layer, false, true));
	}
	
	
	/**
	 * @param color
	 */
	public void addTeamColor(final ETeamColor color)
	{
		displayColors.add(color);
	}
	
	
	/**
	 * @param color
	 */
	public void removeTeamColor(final ETeamColor color)
	{
		displayColors.remove(color);
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
	public void setVisibility(final EFieldLayer name, final boolean visible)
	{
		for (final Layer layer : layers)
		{
			if (layer.layer.getType() == name)
			{
				layer.layer.setVisible(visible);
				return;
			}
		}
	}
	
	
	/**
	 * Resets the visibility of all added layers to the initial value.
	 */
	public void setInitialVisibility()
	{
		for (final Layer layer : layers)
		{
			layer.layer.setInitialVisibility();
		}
	}
	
	
	/**
	 * Draws special DEBUG information on all layer.
	 * 
	 * @param debugInformationVisible
	 */
	public void setDebugInformationVisible(final boolean debugInformationVisible)
	{
		for (final Layer layer : layers)
		{
			layer.layer.setDebugInformationVisible(debugInformationVisible);
		}
	}
	
	
	/**
	 * Returns specified layer.
	 * 
	 * @param name
	 * @return layer or null when layer cannot be found.
	 */
	public AFieldLayer getFieldLayer(final EFieldLayer name)
	{
		for (final Layer layer : layers)
		{
			if (layer.layer.getType() == name)
			{
				return layer.layer;
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
	public void setFancyPainting(final boolean fancyPainting)
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
	public final void setRecording(final boolean record, final boolean saving)
	{
		// if we are currently recording, but stop is requested, we stop and show new window with recording result
		if (recording && !record && (recorder.getRecordFrames().size() > 0))
		{
			recorder.close();
			if (!saving)
			{
				ReplayWindow replaceWindow = new ReplayWindow(recorder.getRecordFrames(), recorder.getLogEvents());
				replaceWindow.activate();
			}
			
		} else if (!recording && record)
		{
			if (saving)
			{
				recorder = new Recorder(ERecordMode.DATABASE, FRAME_BUFFER_SIZE);
			} else
			{
				recorder = new Recorder(ERecordMode.LIMITED_BUFFER, MAX_FRAMES);
			}
		}
		recording = record;
	}
	
	
	/**
	 * Set fieldTurn in all layers
	 * 
	 * @param fieldTurn
	 */
	public void setFieldTurn(final EFieldTurn fieldTurn)
	{
		for (final Layer layer : layers)
		{
			layer.layer.setFieldTurn(fieldTurn);
		}
	}
	
	
	/**
	 */
	public void clearField()
	{
		wFrame = null;
		aiFrameBlue = null;
		aiFrameYellow = null;
	}
}
