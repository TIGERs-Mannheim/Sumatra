/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Assigner;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.NewRoleAssigner;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.FeatureScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.PenaltyScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.DestinationCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.DestinationFreeCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.ViewAngleCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.TargetVisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.VisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.FieldInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.TuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline.BotNotOnSplineDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline.CollisionDetectionDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline.DestinationChangedDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline.NewPathShorterDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline.SplineEndGoalNotReachedDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.AConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.TigerV2Interpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFramePacker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigAnnotationProcessor;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.SupportPosLayer;


/**
 * This holds some static variables to parameterize the AI
 * hard choices - null == usual procedures in classes
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte
 */
public final class AIConfig
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// Geometry
	private final IConfigClient	geomClient	= new GeometryConfigClient();
	private volatile Geometry		geometry;
	
	private final AiConfigClient	skillsClient;
	private final AiConfigClient	playsClient	= new AiConfigClient("plays", getAllClasses(EPlay.class));
	private final AiConfigClient	rolesClient	= new AiConfigClient("roles", getAllClasses(ERole.class));
	private final AiConfigClient	conditionsClient;
	private final AiConfigClient	sisyphusClient;
	private final AiConfigClient	metisClient	= new AiConfigClient("metis", getAllClasses(ECalculator.class));
	private final AiConfigClient	rcmClient;
	private final AiConfigClient	lachesisClient;
	private final AiConfigClient	layerClient;
	private final AiConfigClient	miscClient;
	
	private String						botSpezi		= "";
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class AIConfigHolder
	{
		private static final AIConfig	CONFIG	= new AIConfig();
	}
	
	
	private AIConfig()
	{
		Set<Class<?>> skillsClasses = getAllClasses(ESkillName.class);
		skillsClasses.add(TigerDevices.class);
		skillsClient = new AiConfigClient("skills", skillsClasses);
		
		Set<Class<?>> conditionClasses = new LinkedHashSet<Class<?>>();
		conditionClasses.add(DestinationCondition.class);
		conditionClasses.add(DestinationFreeCondition.class);
		conditionClasses.add(MovementCon.class);
		conditionClasses.add(ViewAngleCondition.class);
		conditionClasses.add(TargetVisibleCon.class);
		conditionClasses.add(VisibleCon.class);
		conditionsClient = new AiConfigClient("conditions", conditionClasses);
		
		Set<Class<?>> sisyphusClasses = new LinkedHashSet<Class<?>>();
		sisyphusClasses.add(Sisyphus.class);
		sisyphusClasses.add(TuneableParameter.class);
		sisyphusClasses.add(DestinationChangedDecisionMaker.class);
		sisyphusClasses.add(BotNotOnSplineDecisionMaker.class);
		sisyphusClasses.add(NewPathShorterDecisionMaker.class);
		sisyphusClasses.add(SplineEndGoalNotReachedDecisionMaker.class);
		sisyphusClasses.add(CollisionDetectionDecisionMaker.class);
		sisyphusClasses.add(FieldInformation.class);
		sisyphusClient = new AiConfigClient("sisyphus", sisyphusClasses);
		
		Set<Class<?>> rcmClasses = new LinkedHashSet<Class<?>>();
		rcmClasses.add(TigerV2Interpreter.class);
		rcmClient = new AiConfigClient("rcm", rcmClasses);
		
		Set<Class<?>> lachesisClasses = new LinkedHashSet<Class<?>>();
		lachesisClasses.add(NewRoleAssigner.class);
		lachesisClasses.add(RoleFinder.class);
		lachesisClasses.add(Assigner.class);
		lachesisClasses.add(Lachesis.class);
		lachesisClasses.add(PenaltyScore.class);
		lachesisClasses.add(FeatureScore.class);
		lachesisClient = new AiConfigClient("lachesis", lachesisClasses);
		
		Set<Class<?>> layerClasses = new LinkedHashSet<Class<?>>();
		layerClasses.add(SupportPosLayer.class);
		layerClient = new AiConfigClient("layer", layerClasses);
		
		Set<Class<?>> miscClasses = new LinkedHashSet<Class<?>>();
		miscClasses.add(WorldFramePacker.class);
		miscClient = new AiConfigClient("misc", miscClasses);
	}
	
	
	/**
	 * @return
	 */
	public static AIConfig getInstance()
	{
		return AIConfigHolder.CONFIG;
	}
	
	
	/**
	 * @return
	 */
	public static List<IConfigClient> getConfigClients()
	{
		List<IConfigClient> clients = new ArrayList<IConfigClient>();
		clients.add(getInstance().geomClient);
		clients.add(getInstance().skillsClient);
		clients.add(getInstance().rolesClient);
		clients.add(getInstance().playsClient);
		clients.add(getInstance().conditionsClient);
		clients.add(getInstance().sisyphusClient);
		clients.add(getInstance().metisClient);
		clients.add(getInstance().rcmClient);
		clients.add(getInstance().lachesisClient);
		clients.add(getInstance().layerClient);
		clients.add(getInstance().miscClient);
		return clients;
	}
	
	
	// --------------------------------------------------------------------------
	// --- IConfigClients -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
		}
	}
	
	
	/**
	 * This general ai config client serves as creator for configs based on {@link ConfigAnnotationProcessor}
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public static class AiConfigClient extends AConfigClient
	{
		private final ConfigAnnotationProcessor	cap	= new ConfigAnnotationProcessor();
		private final String								name;
		private final Set<Class<?>>					classes;
		
		
		private AiConfigClient(final String name, final Set<Class<?>> classes)
		{
			super(name, AAgent.AI_CONFIG_PATH, AIConfig.class.getName() + "." + name, name + ".xml", true);
			this.name = name;
			this.classes = classes;
		}
		
		
		@Override
		public void onLoad(final HierarchicalConfiguration newConfig)
		{
			cap.loadConfiguration(newConfig);
			cap.apply();
			applySpezis();
		}
		
		
		@Override
		public HierarchicalConfiguration getDefaultConfig()
		{
			return cap.getDefaultConfig(classes, name);
		}
		
		
		/**
		 * Apply all config values with given spezi. If spezi=="", apply default values.
		 * 
		 * @param obj The instance where all fields should be set.
		 * @param spezi
		 */
		public void applyConfigToObject(final Object obj, final String spezi)
		{
			cap.apply(obj, spezi);
		}
		
		
		/**
		 * Apply all know spezis
		 */
		public void applySpezis()
		{
			cap.apply(AIConfig.getBotSpezi());
		}
	}
	
	
	private Set<Class<?>> getAllClasses(final Class<? extends Enum<? extends IInstanceableEnum>> enumClazz)
	{
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		Enum<? extends IInstanceableEnum> values[] = enumClazz.getEnumConstants();
		IInstanceableEnum valInstancable[] = (IInstanceableEnum[]) values;
		for (IInstanceableEnum en : valInstancable)
		{
			classes.add(en.getInstanceableClass().getImpl());
		}
		Set<Class<?>> superClasses = new LinkedHashSet<Class<?>>();
		for (Class<?> clazz : classes)
		{
			if (clazz.getSuperclass() != null)
			{
				superClasses.add(clazz.getSuperclass());
			}
		}
		classes.addAll(superClasses);
		return classes;
	}
	
	
	// --------------------------------------------------------------------------
	// --- accessors ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return geometry values
	 */
	public static Geometry getGeometry()
	{
		if (AIConfig.getInstance().geometry == null)
		{
			throw new IllegalStateException("geometry is null!");
		}
		return AIConfig.getInstance().geometry;
	}
	
	
	/**
	 * @return the skillsClient
	 */
	public static AiConfigClient getSkillsClient()
	{
		return getInstance().skillsClient;
	}
	
	
	/**
	 * @return the playsClient
	 */
	public static AiConfigClient getPlaysClient()
	{
		return getInstance().playsClient;
	}
	
	
	/**
	 * @return the rolesClient
	 */
	public static AiConfigClient getRolesClient()
	{
		return getInstance().rolesClient;
	}
	
	
	/**
	 * @return the conditionsClient
	 */
	public static AiConfigClient getConditionsClient()
	{
		return getInstance().conditionsClient;
	}
	
	
	/**
	 * @return the sisyphusClient
	 */
	public static AiConfigClient getSisyphusClient()
	{
		return getInstance().sisyphusClient;
	}
	
	
	/**
	 * @return the metisClient
	 */
	public static AiConfigClient getMetisClient()
	{
		return getInstance().metisClient;
	}
	
	
	/**
	 * @return the layerClient
	 */
	public static AiConfigClient getLayerClient()
	{
		return getInstance().layerClient;
	}
	
	
	/**
	 * @return the botSpezi
	 */
	public static String getBotSpezi()
	{
		return getInstance().botSpezi;
	}
	
	
	/**
	 * @param botSpezi the botSpezi to set
	 */
	public static void setBotSpezi(final String botSpezi)
	{
		getInstance().botSpezi = botSpezi;
		getSkillsClient().applySpezis();
		getSisyphusClient().applySpezis();
		getInstance().rcmClient.applySpezis();
	}
}
