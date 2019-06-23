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
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel.EFieldTurn;


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
	private final transient List<AFieldLayer>	aiLayerList		= new CopyOnWriteArrayList<AFieldLayer>();
	private final transient List<AFieldLayer>	swfLayerList	= new CopyOnWriteArrayList<AFieldLayer>();
	private final transient List<AFieldLayer>	allLayerList	= new CopyOnWriteArrayList<AFieldLayer>();
	
	private final List<Layer>						layers			= new LinkedList<Layer>();
	
	private boolean									fancyPainting	= false;
	
	private SimpleWorldFrame						wFrame			= null;
	private IRecordFrame								aiFrameYellow	= null;
	private IRecordFrame								aiFrameBlue		= null;
	private RefereeMsg								refereeMsg		= null;
	
	private Set<ETeamColor>							displayColors	= new HashSet<ETeamColor>();
	
	
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
		if (aiFrame == null)
		{
			aiFrameBlue = null;
			aiFrameYellow = null;
		} else
		{
			if (aiFrame.getTeamColor() == ETeamColor.YELLOW)
			{
				aiFrameYellow = aiFrame;
			} else
			{
				aiFrameBlue = aiFrame;
			}
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
	 * Set new referee msg
	 * 
	 * @param msg
	 */
	public void updateRefereeMsg(final RefereeMsg msg)
	{
		refereeMsg = msg;
	}
	
	
	/**
	 * @param aiPoint
	 */
	public void onFieldClick(final IVector2 aiPoint)
	{
		for (Layer layer : layers)
		{
			layer.layer.onFieldClick(aiPoint);
		}
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
			layer.layer.paintRefereeCmd(refereeMsg, g);
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
