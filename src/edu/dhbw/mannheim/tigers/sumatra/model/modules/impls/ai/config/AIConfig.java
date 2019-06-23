/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.LearnedBallModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.AConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;


/**
 * This holds some static variables to parameterize the AI
 * hard choices - null == usual procedures in classes
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte
 */
public final class AIConfig
{
	private final IConfigClient			geomClient	= new GeometryConfigClient();
	private volatile Geometry				geometry;
	private volatile LearnedBallModel	ballModel;
	private static final AIConfig			INSTANCE		= new AIConfig();
	
	
	private AIConfig()
	{
	}
	
	
	/**
	 * @return
	 */
	public static IConfigClient getGeometryClient()
	{
		return INSTANCE.geomClient;
	}
	
	
	private final class GeometryConfigClient extends AConfigClient
	{
		private GeometryConfigClient()
		{
			super("geometry", AAgent.GEOMETRY_CONFIG_PATH, AAgent.KEY_GEOMETRY_CONFIG, AAgent.VALUE_GEOMETRY_CONFIG, false);
		}
		
		
		@Override
		public void onLoad(final HierarchicalConfiguration config)
		{
			geometry = new Geometry(config);
			ballModel = new LearnedBallModel(geometry.getBallModelIdentifier());
		}
		
		
		@Override
		public boolean isRequired()
		{
			return true;
		}
	}
	
	
	/**
	 * @return geometry values
	 */
	public static Geometry getGeometry()
	{
		if (INSTANCE.geometry == null)
		{
			throw new IllegalStateException("geometry is null!");
		}
		return INSTANCE.geometry;
	}
	
	
	/**
	 * @param geom
	 */
	public static void setGeometry(final Geometry geom)
	{
		INSTANCE.geometry = geom;
	}
	
	
	/**
	 * @return geometry values
	 */
	public static LearnedBallModel getBallModel()
	{
		if (INSTANCE.ballModel == null)
		{
			throw new IllegalStateException("ballModel is null!");
		}
		return INSTANCE.ballModel;
	}
}
