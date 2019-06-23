/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2011
 * Author(s): Oliver Steinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel.EFieldTurn;


/**
 * Base class for field layers which should be drawn upon the field visualization.
 * On default the DEBUG information is not visible. To draw additional DEBUG
 * information the method {@link AFieldLayer#paintDebugInformation(Graphics2D, IRecordFrame)} has to
 * be override in sub classes.
 * 
 * @author Oliver Steinbrecher
 */
public abstract class AFieldLayer
{
	private static final Stroke		BOT_CIRCLE_STROKE			= new BasicStroke(1);
	
	private final EFieldLayer			type;
	
	/** if true this layer is visible on the field */
	private boolean						visible;
	private final boolean				initialVisibility;
	
	private boolean						debugInformationVisible	= false;
	
	private EFieldTurn					fieldTurn					= EFieldTurn.NORMAL;
	
	private IFieldPanel					fieldPanel					= null;
	
	private static final List<Color>	colors						= new ArrayList<Color>();
	
	static
	{
		colors.add(new Color(0xF7181D));
		colors.add(new Color(0xF73E18));
		colors.add(new Color(0xF86A19));
		colors.add(new Color(0xF9951A));
		colors.add(new Color(0xF9C11B));
		colors.add(new Color(0xFAEC1B));
		colors.add(new Color(0xDEFB1C));
		colors.add(new Color(0xB4FB1D));
		colors.add(new Color(0x8AFC1E));
		colors.add(new Color(0x60FD1F));
		colors.add(new Color(0x37FE20));
	}
	
	
	/**
	 * Base class for a field layer. On default a layer is visible.
	 * 
	 * @param name identifier for this layer
	 */
	public AFieldLayer(final EFieldLayer name)
	{
		type = name;
		visible = true;
		initialVisibility = true;
	}
	
	
	/**
	 * @param name
	 * @param visible
	 */
	public AFieldLayer(final EFieldLayer name, final boolean visible)
	{
		type = name;
		this.visible = visible;
		initialVisibility = visible;
	}
	
	
	/**
	 * @param frame
	 * @param g
	 */
	public final void paintAiFrame(final IRecordFrame frame, final Graphics2D g)
	{
		if ((visible || isForceVisible()) && (frame != null))
		{
			paintLayerAif(g, frame);
			if (debugInformationVisible)
			{
				paintDebugInformation(g, frame);
			}
		}
	}
	
	
	/**
	 * @param frame
	 * @param g
	 */
	public final void paintSimpleWorldFrame(final SimpleWorldFrame frame, final Graphics2D g)
	{
		if ((visible || isForceVisible()) && (frame != null))
		{
			paintLayerSwf(g, frame);
		}
	}
	
	
	/**
	 * @param msg
	 * @param g
	 */
	public final void paintRefereeCmd(final RefereeMsg msg, final Graphics2D g)
	{
		if ((visible || isForceVisible()) && (msg != null))
		{
			paintLayerReferee(g, msg);
		}
	}
	
	
	/**
	 * Do the painting of this layer for AI
	 * 
	 * @param g to paint on
	 * @param frame
	 */
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
	}
	
	
	/**
	 * Do the painting of this layer for World
	 * 
	 * @param g to paint on
	 * @param frame
	 */
	protected void paintLayerSwf(final Graphics2D g, final SimpleWorldFrame frame)
	{
	}
	
	
	/**
	 * Do the painting of this layer for referee
	 * 
	 * @param g to paint on
	 * @param msg
	 */
	protected void paintLayerReferee(final Graphics2D g, final RefereeMsg msg)
	{
	}
	
	
	/**
	 * Draws additional DEBUG information.
	 * 
	 * @param g
	 */
	protected void paintDebugInformation(final Graphics2D g, final IRecordFrame frame)
	{
		// override this in sub classes. On default no DEBUG information is available.
	}
	
	
	/**
	 * Called when mouse clicked into field
	 * 
	 * @param aiPos
	 */
	protected void onFieldClick(final IVector2 aiPos)
	{
		
	}
	
	
	protected final void drawBotCircle(final Graphics2D g, final IVector2 pos, final boolean invert, final int ring)
	{
		float botRadius = AIConfig.getGeometry().getBotRadius();
		int dx = (fieldPanel.scaleXLength(botRadius) * 2) + ring;
		int dy = (fieldPanel.scaleYLength(botRadius) * 2) + ring;
		if ((dx % 2) != 0)
		{
			dx += 1;
		}
		if ((dy % 2) != 0)
		{
			dy += 1;
		}
		IVector2 tPos = fieldPanel.transformToGuiCoordinates(pos, invert);
		g.setStroke(BOT_CIRCLE_STROKE);
		g.drawOval((int) tPos.x() - (dx / 2), (int) tPos.y() - (dy / 2), dx, dy);
	}
	
	
	protected final void drawDrawableShapes(final Graphics2D g, final List<IDrawableShape> shapes, final boolean invert)
	{
		for (IDrawableShape shape : shapes)
		{
			shape.paintShape(g, fieldPanel, invert);
		}
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		
		if (obj == this)
		{
			return true;
		}
		
		if (!obj.getClass().equals(getClass()))
		{
			return false;
		}
		
		final AFieldLayer fieldObj = (AFieldLayer) obj;
		return (type == fieldObj.getType());
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		return (prime * result) + ((type == null) ? 0 : type.hashCode());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the name
	 */
	public final EFieldLayer getType()
	{
		return type;
	}
	
	
	/**
	 * @return true when this layer visible
	 */
	public final boolean isVisible()
	{
		return visible;
	}
	
	
	/**
	 * @param visible
	 */
	public final void setVisible(final boolean visible)
	{
		this.visible = visible;
	}
	
	
	/**
	 * @return true when the layers DEBUG information is visible.
	 */
	public final boolean isDebugInformationVisible()
	{
		return debugInformationVisible;
	}
	
	
	/**
	 * Draws special DEBUG information on this layer.
	 * 
	 * @param debugInformationVisible
	 */
	public final void setDebugInformationVisible(final boolean debugInformationVisible)
	{
		this.debugInformationVisible = debugInformationVisible;
	}
	
	
	/**
	 * Resets the visibility of this layer to the initial value.
	 */
	public void setInitialVisibility()
	{
		visible = initialVisibility;
		debugInformationVisible = false;
	}
	
	
	/**
	 * @return the fieldTurn
	 */
	public final EFieldTurn getFieldTurn()
	{
		return fieldTurn;
	}
	
	
	/**
	 * @param fieldTurn the fieldTurn to set
	 */
	public final void setFieldTurn(final EFieldTurn fieldTurn)
	{
		this.fieldTurn = fieldTurn;
	}
	
	
	/**
	 * @return the fieldPanel
	 */
	public final IFieldPanel getFieldPanel()
	{
		return fieldPanel;
	}
	
	
	/**
	 * @param fieldPanel the fieldPanel to set
	 */
	public final void setFieldPanel(final IFieldPanel fieldPanel)
	{
		this.fieldPanel = fieldPanel;
	}
	
	
	protected boolean isForceVisible()
	{
		return false;
	}
	
	
	/**
	 * Get a color depending on given value (0-1)
	 * 
	 * @param value
	 * @return
	 */
	public static Color getColorByValue(final float value)
	{
		float step = 1f / colors.size();
		for (int i = 0; i < colors.size(); i++)
		{
			float val = (i + 1) * step;
			if (value <= val)
			{
				return colors.get(colors.size() - i - 1);
			}
		}
		return Color.black;
	}
}
