/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 20, 2013
 * Author(s): geforce
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.persistance;

import java.io.File;

import org.apache.log4j.Logger;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.Converter;
import com.sleepycat.persist.evolve.Deleter;
import com.sleepycat.persist.evolve.Mutations;
import com.sleepycat.persist.evolve.Renamer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.RecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.HermiteSplineTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.DrawableTree;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.kd.KDTree;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.simple.SimpleTree;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathNode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.GrSimBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictionInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.converter.HermiteSplineConverter;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.converter.NullConversion;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.converter.VectorToValuePointConversion;


/**
 * This environment class manages a berkeley database.
 * It will be used to open und close a entity store
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("deprecation")
public class BerkeleyEnv
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(BerkeleyEnv.class.getName());
	private Environment				myEnv;
	private EntityStore				store;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public BerkeleyEnv()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @see <a
	 *      href="http://docs.oracle.com/cd/E17277_02/html/java/com/sleepycat/persist/evolve/package-summary.html">http://docs.oracle.com/cd/E17277_02/html/java/com/sleepycat/persist/evolve/package-summary.html</a>
	 * @return
	 */
	private Mutations createMutations()
	{
		Mutations mutations = new Mutations();
		mutations.addDeleter(new Deleter(RecordWfFrame.class.getName(), 0, "time"));
		mutations.addDeleter(new Deleter(RecordFrame.class.getName(), 0, "assigendRoles"));
		mutations.addDeleter(new Deleter(RecordFrame.class.getName(), 0, "paths"));
		mutations.addDeleter(new Deleter(RecordFrame.class.getName(), 3, "refereeCmd"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "activeCalculators"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "ballInOurPenArea"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "botNotAllowedToTouchBall"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "debugShapes"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "enemyClosestToBall"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "forceStartAfterKickoffEnemies"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "offCarrierPoints"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "offLeftReceiverPoints"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "offRightReceiverPoints"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "opponentApproximateScoringChance"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "opponentBallGetter"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "opponentScoringChance"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "otherMixedTeamTouchedBall"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "teamClosestToBall"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "tigerClosestToBall"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "tigersApproximateScoringChance"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "tigersScoringChance"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 0, "valueOfBestDirectShootTarget"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 2, "playPattern"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 2, "dangerousOpponents"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 2, "opponentPassReceiver"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 2, "opponentKeeper"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 8, "playPattern"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 8, "dangerousOpponents"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 8, "opponentPassReceiver"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 8, "opponentKeeper"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 9, "supportIntersections"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 13, "botBuffer"));
		mutations.addDeleter(new Deleter(BotID.class.getName(), 1, "team"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "assignedERoles"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "changedPlay"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "changedPlayLock"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "controlState"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "forceNewDecision"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "matchBehavior"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "matchBehaviorLock"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "stateChanged"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "debugShapes"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 3, "debugShapes"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "botConnection"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 3, "botConnection"));
		mutations.addDeleter(new Deleter(DefensePoint.class.getName(), 0, "kindOfshoot"));
		mutations.addConverter(new Converter(TacticalField.class.getName(), 0, "bestDirectShootTarget",
				new VectorToValuePointConversion()));
		mutations.addConverter(new Converter(HermiteSpline.class.getName(), 0, new HermiteSplineConverter()));
		mutations
				.addRenamer(new Renamer(
						"edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.WorldFramePrediction",
						0, WorldFramePrediction.class.getName()));
		mutations
				.addRenamer(new Renamer(
						"edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction.FieldPredictionInformation",
						0, FieldPredictionInformation.class.getName()));
		mutations.addRenamer(new Renamer(TacticalField.class.getName(), 0, "bestDirectShootTarget",
				"bestDirectShotTarget"));
		mutations.addConverter(new Converter(TacticalField.class.getName(), 5, "shooterReceiverStraightLines",
				new NullConversion()));
		mutations.addConverter(new Converter(TacticalField.class.getName(), 5, "ballReceiverStraightLines",
				new NullConversion()));
		
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 7, "bestPassTargets"));
		
		
		// mutations.addRenamer(new Renamer(Path.class.getName(), 0, "target", "destination"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "pathGuiFeatures"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "timestamp"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "botID"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "changed"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "destOrient"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "firstCollision"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "hermiteSpline"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "old"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "target"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "destination"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "tree"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "pathDebugShapes"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 1, "pathDebugShapes"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 2, "pathDebugShapes"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 3, "pathDebugShapes"));
		mutations.addRenamer(new Renamer(Path.class.getName(), 0, "path", "pathPoints"));
		mutations.addDeleter(new Deleter(HermiteSpline2D.class.getName(), 0, "length"));
		mutations.addDeleter(new Deleter(HermiteSplineTrajectory2D.class.getName(), 0, "totalLength"));
		mutations.addDeleter(new Deleter(HermiteSplineTrajectory2D.HermiteSplineTrajectoryPart2D.class.getName(), 0,
				"endWay"));
		mutations.addDeleter(new Deleter(HermiteSplineTrajectory2D.HermiteSplineTrajectoryPart2D.class.getName(), 0,
				"startWay"));
		
		// Moving ERRT classes from sisyphus.errt to sisyphus.finder.errt and PathPlanning refactoring
		mutations
				.addRenamer(new Renamer("edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.Node",
						0, Node.class.getName()));
		mutations
				.addRenamer(new Renamer("edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.ITree",
						0, KDTree.class.getName()));
		mutations
				.addRenamer(new Renamer(
						"edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.kd.KDTree",
						0, KDTree.class.getName()));
		mutations
				.addRenamer(new Renamer(
						"edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.simple.SimpleTree",
						0, SimpleTree.class.getName()));
		mutations
				.addRenamer(new Renamer(
						"edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.tree.DrawableTree",
						0, DrawableTree.class.getName()));
		
		mutations.addDeleter(new Deleter(OffensiveStrategy.class.getName(), 0, "helperDestinations"));
		mutations.addDeleter(new Deleter(OffensiveStrategy.class.getName(), 0, "passTarget"));
		mutations.addDeleter(new Deleter(OffensiveStrategy.class.getName(), 0, "primaryOffensiveStrategy"));
		mutations.addDeleter(new Deleter(OffensiveStrategy.class.getName(), 0, "strategies"));
		mutations.addDeleter(new Deleter(OffensiveStrategy.class.getName(), 0, "specialMoveDestinations"));
		mutations.addDeleter(new Deleter(OffensiveStrategy.class.getName(), 1, "specialMoveDestinations"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 11, "debugPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 12, "debugPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 13, "debugPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 14, "debugPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 15, "debugPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 16, "debugPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 17, "debugPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 11, "advancedPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 12, "advancedPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 13, "advancedPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 14, "advancedPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 15, "advancedPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 16, "advancedPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 17, "advancedPassTargets"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 17, "advancedPassTargetBotMapping"));
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 18, "advancedPassTargetBotMapping"));
		mutations.addDeleter(new Deleter(RecordWfFrame.class.getName(), 4, "visionBalls"));
		mutations.addDeleter(new Deleter(RecordWfFrame.class.getName(), 5, "visionBalls"));
		for (int i = 2; i < 22; i++)
		{
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "ballReceiverStraightLines"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "bestDirectShotTargetBots"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "defGoalPoints"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "shooterReceiverStraightLines"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "bestPassTarget"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "supportPositions"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "supportRedirectPositions"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "supportTargets"));
			mutations.addDeleter(new Deleter(TacticalField.class.getName(), i, "offenseMovePositions"));
		}
		mutations
				.addRenamer(new Renamer(
						"edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictorNEW.constructionSite.MergedCamDetectionFrame",
						0, MergedCamDetectionFrame.class.getName()));
		mutations.addDeleter(new Deleter(RecordWfFrame.class.getName(), 6, "camFrame"));
		mutations.addDeleter(new Deleter(RecordWfFrame.class.getName(), 7, "camFrame"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 0, "nearest2Goal"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 0, "nearest2Marker"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 0, "passIntersections"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 0, "vectorGoalOurMidBot"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 1, "nearest2Goal"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 1, "nearest2Marker"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 1, "passIntersections"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 1, "vectorGoalOurMidBot"));
		mutations.addDeleter(new Deleter(GrSimBot.class.getName(), 0, "duration2SpeedFn"));
		mutations.addDeleter(new Deleter(GrSimBot.class.getName(), 1, "duration2SpeedFn"));
		for (int i = 0; i < 5; i++)
		{
			mutations.addDeleter(new Deleter(ABot.class.getName(), i, "name"));
			mutations.addDeleter(new Deleter(ABot.class.getName(), i, "controlledBy"));
			mutations.addDeleter(new Deleter(ABot.class.getName(), i, "hideFromAi"));
			mutations.addDeleter(new Deleter(ABot.class.getName(), i, "hideFromRcm"));
			mutations.addDeleter(new Deleter(ABot.class.getName(), i, "baseStationKey"));
			mutations.addDeleter(new Deleter(ABot.class.getName(), i, "mcastDelegateKey"));
			mutations.addDeleter(new Deleter(ABot.class.getName(), i, "center2DribblerDist"));
			mutations.addDeleter(new Deleter(GrSimBot.class.getName(), i, "kickerDeadtimeActive"));
			mutations.addDeleter(new Deleter(GrSimBot.class.getName(), i, "lastDest"));
			mutations.addDeleter(new Deleter(GrSimBot.class.getName(), i, "lastTargetAngle"));
		}
		mutations.addDeleter(new Deleter(TigerBotV3.class.getName(), 0, "txStats"));
		mutations.addDeleter(new Deleter(TigerBotV3.class.getName(), 0, "rxStats"));
		mutations.addDeleter(new Deleter(TigerBotV3.class.getName(), 0, "matchCmdMemory"));
		mutations.addDeleter(new Deleter(TigerBotV3.class.getName() + "$MatchCmdMemory", 0, "cheer"));
		mutations.addDeleter(new Deleter(TigerBotV3.class.getName() + "$MatchCmdMemory", 0, "controllerOn"));
		mutations.addDeleter(new Deleter(TigerBotV3.class.getName() + "$MatchCmdMemory", 0, "pos"));
		mutations.addDeleter(new Deleter(TigerBotV3.class.getName() + "$MatchCmdMemory", 0, "vel"));
		
		mutations.addDeleter(new Deleter(TrajPath.class.getName(), 0, "orientation"));
		mutations.addDeleter(new Deleter(TrajPath.class.getName(), 2, "lastTargetAngle"));
		mutations.addDeleter(new Deleter(TrajPath.class.getName(), 2, "lastDest"));
		mutations.addDeleter(new Deleter(TrajPath.class.getName(), 2, "lastId"));
		mutations.addDeleter(new Deleter(TrajPath.class.getName(), 3, "lastTargetAngle"));
		mutations.addDeleter(new Deleter(TrajPath.class.getName(), 3, "lastDest"));
		mutations.addDeleter(new Deleter(TrajPath.class.getName(), 3, "lastId"));
		mutations.addDeleter(new Deleter(TrajPathNode.class.getName(), 0, "traj"));
		
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 0, "foeBot"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 1, "foeBot"));
		mutations.addDeleter(new Deleter(FoeBotData.class.getName(), 2, "foeBot"));
		return mutations;
	}
	
	
	/**
	 * The setup() method opens the environment and store
	 * for us.
	 * 
	 * @param envHome
	 * @param readOnly
	 * @throws DatabaseException
	 */
	public void setup(final File envHome, final boolean readOnly)
	{
		
		EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		
		myEnvConfig.setReadOnly(readOnly);
		storeConfig.setReadOnly(readOnly);
		
		// If the environment is opened for write, then we want to be
		// able to create the environment and entity store if
		// they do not exist.
		myEnvConfig.setAllowCreate(!readOnly);
		storeConfig.setAllowCreate(!readOnly);
		
		// set mutations for conversions of the db
		storeConfig.setMutations(createMutations());
		
		// Open the environment and entity store
		myEnv = new Environment(envHome, myEnvConfig);
		store = new EntityStore(myEnv, "EntityStore", storeConfig);
	}
	
	
	/**
	 * Return a handle to the entity store
	 * 
	 * @return
	 */
	public EntityStore getEntityStore()
	{
		return store;
	}
	
	
	/**
	 * Return a handle to the environment
	 * 
	 * @return
	 */
	public Environment getEnv()
	{
		return myEnv;
	}
	
	
	/**
	 * Close the store and environment
	 */
	public void close()
	{
		if (store != null)
		{
			try
			{
				store.close();
				store = null;
			} catch (DatabaseException dbe)
			{
				log.error("Error closing store", dbe);
			}
		}
		
		if (myEnv != null)
		{
			try
			{
				// Finally, close the store and environment.
				myEnv.close();
				myEnv = null;
			} catch (DatabaseException dbe)
			{
				log.error("Error closing myEnv", dbe);
			}
		}
	}
	
	
	/**
	 * @return
	 */
	public boolean isOpen()
	{
		return (myEnv != null) && (store != null);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
