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
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
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
		mutations.addDeleter(new Deleter(BotID.class.getName(), 1, "team"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "assignedERoles"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "changedPlay"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "changedPlayLock"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "controlState"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "forceNewDecision"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "matchBehavior"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "matchBehaviorLock"));
		mutations.addDeleter(new Deleter(PlayStrategy.class.getName(), 1, "stateChanged"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "pathGuiFeatures"));
		mutations.addDeleter(new Deleter(Path.class.getName(), 0, "timestamp"));
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
		mutations.addRenamer(new Renamer(Path.class.getName(), 0, "target", "destination"));
		mutations.addConverter(new Converter(TacticalField.class.getName(), 5, "shooterReceiverStraightLines",
				new NullConversion()));
		mutations.addConverter(new Converter(TacticalField.class.getName(), 5, "ballReceiverStraightLines",
				new NullConversion()));
		
		mutations.addDeleter(new Deleter(TacticalField.class.getName(), 7, "bestPassTargets"));
		
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
