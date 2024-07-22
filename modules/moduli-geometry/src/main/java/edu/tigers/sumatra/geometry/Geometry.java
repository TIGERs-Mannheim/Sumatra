/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.google.protobuf.TextFormat;
import edu.tigers.sumatra.ball.trajectory.BallFactory;
import edu.tigers.sumatra.cam.SSLVisionCamGeometryTranslator;
import edu.tigers.sumatra.cam.data.CamFieldSize;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.SslVisionGeometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.penaltyarea.PenaltyArea;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**
 * Configuration object for geometry parameters.
 */
@Log4j2
public class Geometry
{
	private static final Path CONFIG_PATH = Path.of("config", "geometry");

	@Configurable(defValue = "85.0")
	private static double opponentCenter2DribblerDist = 85;
	private static Geometry instance = defaultInstance();
	private static ETeamColor negativeHalfTeam = ETeamColor.BLUE;

	static
	{
		ConfigRegistration.registerClass("geom", Geometry.class);
	}

	private final IRectangle field;
	private final IRectangle fieldWBorders;
	private final Goal goalOur;
	private final Goal goalTheir;
	private final IPenaltyArea penaltyAreaOur;
	private final IPenaltyArea penaltyAreaTheir;
	private final ICircle centerCircle;
	private final IRectangle ourHalf;
	private final IRectangle theirHalf;
	private final BallFactory ballFactory;
	private final BallParameters ballParameters;

	private CamGeometry lastCamGeometry;


	private Geometry(CamGeometry camGeometry)
	{
		lastCamGeometry = camGeometry;
		CamFieldSize fieldSize = lastCamGeometry.getFieldSize();

		field = Rectangle.fromCenter(Vector2f.ZERO_VECTOR, fieldSize.getFieldLength(), fieldSize.getFieldWidth());
		fieldWBorders = field.withMargin(fieldSize.getBoundaryWidth());
		goalOur = new Goal(
				Vector2f.fromXY(-fieldSize.getFieldLength() / 2, 0), fieldSize.getGoalWidth(),
				fieldSize.getGoalDepth(),
				fieldSize.getFieldWidth()
		);
		goalTheir = new Goal(
				Vector2f.fromXY(fieldSize.getFieldLength() / 2, 0), fieldSize.getGoalWidth(),
				fieldSize.getGoalDepth(),
				fieldSize.getFieldWidth()
		);
		penaltyAreaOur = new PenaltyArea(
				goalOur.getCenter(),
				fieldSize.getPenaltyAreaDepth(),
				fieldSize.getPenaltyAreaWidth()
		);
		penaltyAreaTheir = new PenaltyArea(
				goalTheir.getCenter(),
				fieldSize.getPenaltyAreaDepth(),
				fieldSize.getPenaltyAreaWidth()
		);
		centerCircle = Circle.createCircle(Vector2f.ZERO_VECTOR, fieldSize.getCenterCircleRadius());
		ourHalf = Rectangle.fromCenter(
				field.center().subtractNew(Vector2.fromXY(this.field.xExtent() / 4, 0)),
				field.xExtent() / 2,
				field.yExtent()
		);
		theirHalf = Rectangle.fromCenter(
				field.center().addNew(Vector2.fromXY(this.field.xExtent() / 4, 0)),
				field.xExtent() / 2,
				field.yExtent()
		);

		ballParameters = createBallParameters(lastCamGeometry);
		var params = edu.tigers.sumatra.ball.BallParameters.builder()
				.withBallRadius(fieldSize.getBallRadius())
				.withAccSlide(ballParameters.getAccSlide())
				.withAccRoll(ballParameters.getAccRoll())
				.withInertiaDistribution(ballParameters.getInertiaDistribution())
				.withChipDampingXYFirstHop(ballParameters.getChipDampingXYFirstHop())
				.withChipDampingXYOtherHops(ballParameters.getChipDampingXYOtherHops())
				.withChipDampingZ(ballParameters.getChipDampingZ())
				.withMinHopHeight(ballParameters.getMinHopHeight())
				.withMaxInterceptableHeight(ballParameters.getMaxInterceptableHeight())
				.build();

		ballFactory = new BallFactory(params);
	}


	private static Geometry defaultInstance()
	{
		return new Geometry(
				CamGeometry.builder()
						.fieldSize(CamFieldSize.builder()
								.fieldLength(12000)
								.fieldWidth(9000)
								.goalWidth(1800)
								.goalDepth(300)
								.penaltyAreaDepth(1800)
								.penaltyAreaWidth(3600)
								.centerCircleRadius(500)
								.lineThickness(10)
								.goalCenterToPenaltyMark(8000)
								.goalHeight(155)
								.ballRadius(21.5)
								.robotRadius(90)
								.build())
						.build()
		);
	}


	/**
	 * Update geometry with the given data.
	 * This will merge the geometry with the existing one.
	 *
	 * @param geometry the new geometry data to merge
	 */
	public static synchronized void update(final CamGeometry geometry)
	{
		if (!instance.lastCamGeometry.equalBallModels(geometry))
		{
			log.info("New Ball Models received from SSL vision!");
		}
		instance = new Geometry(instance.lastCamGeometry.merge(geometry));
	}


	/**
	 * Update parameters according to environment
	 */
	public static void refresh()
	{
		String id = SumatraModel.getInstance().getGeometry();
		try
		{
			instance = new Geometry(readGeometryFromFile(id));
		} catch (IOException e)
		{
			log.warn("Could not load geometry '{}'", id, e);
		}
	}


	/**
	 * Load geometry from a file.
	 *
	 * @param id of the geometry file
	 * @return parsed Geometry
	 * @throws IOException when file not readable or parsable.
	 */
	private static CamGeometry readGeometryFromFile(String id) throws IOException
	{
		Path path = CONFIG_PATH.resolve(id + ".txt");
		byte[] bytes = Files.readAllBytes(path);
		SslVisionGeometry.SSL_GeometryData data = TextFormat.parse(new String(bytes),
				SslVisionGeometry.SSL_GeometryData.class);
		SSLVisionCamGeometryTranslator translator = new SSLVisionCamGeometryTranslator();
		return translator.fromProtobuf(data);
	}


	/**
	 * @return depth of the penaltyArea
	 */
	public static double getPenaltyAreaDepth()
	{
		return getLastCamGeometry().getFieldSize().getPenaltyAreaDepth();
	}


	/**
	 * @return length of the penaltyArea front line
	 */
	public static double getPenaltyAreaWidth()
	{
		return getLastCamGeometry().getFieldSize().getPenaltyAreaWidth();
	}


	/**
	 * Returns our goal.
	 *
	 * @return goal object
	 */
	public static Goal getGoalOur()
	{
		return instance.goalOur;
	}


	/**
	 * Returns their goal.
	 *
	 * @return goal object
	 */
	public static Goal getGoalTheir()
	{
		return instance.goalTheir;
	}


	/**
	 * Return both goals
	 *
	 * @return list of goals
	 */
	public static List<Goal> getGoals()
	{
		return List.of(getGoalOur(), getGoalTheir());
	}


	/**
	 * @return [mm] height from field bottom where goal bar starts
	 */
	public static double getGoalHeight()
	{
		return getLastCamGeometry().getFieldSize().getGoalHeight();
	}


	/**
	 * @return the ballRadius [mm]
	 */
	public static double getBallRadius()
	{
		return getLastCamGeometry().getFieldSize().getBallRadius();
	}


	/**
	 * @return the botRadius [mm]
	 */
	public static double getBotRadius()
	{
		return getLastCamGeometry().getFieldSize().getRobotRadius();
	}


	/**
	 * OPPONENT center to dribbler distance. Distance for own bots can be found in tracked bot.
	 *
	 * @return
	 */
	public static double getOpponentCenter2DribblerDist()
	{
		return opponentCenter2DribblerDist;
	}


	/**
	 * Returns the field length [mm].
	 *
	 * @return returns field length (x-axis).
	 */
	public static double getFieldLength()
	{
		return getLastCamGeometry().getFieldSize().getFieldLength();
	}


	/**
	 * Returns the field xExtent [mm].
	 *
	 * @return returns field length (y-axis).
	 */
	public static double getFieldWidth()
	{
		return getLastCamGeometry().getFieldSize().getFieldWidth();
	}


	/**
	 * @return the distance from the goal center to the penalty mark
	 */
	public static double getGoalCenterToPenaltyMark()
	{
		return getLastCamGeometry().getFieldSize().getGoalCenterToPenaltyMark();
	}


	/**
	 * @return the field
	 */
	public static IRectangle getField()
	{
		return instance.field;
	}


	/**
	 * Field with border margin, but without referee area
	 *
	 * @return the fieldWBorders
	 */
	public static IRectangle getFieldWBorders()
	{
		return instance.fieldWBorders;
	}


	/**
	 * @return
	 */
	public static IVector2 getCenter()
	{
		return Vector2f.ZERO_VECTOR;
	}


	/**
	 * @return
	 */
	public static ICircle getCenterCircle()
	{
		return instance.centerCircle;
	}


	/**
	 * @return
	 */
	public static IPenaltyArea getPenaltyAreaOur()
	{
		return instance.penaltyAreaOur;
	}


	/**
	 * @return
	 */
	public static IPenaltyArea getPenaltyAreaTheir()
	{
		return instance.penaltyAreaTheir;
	}


	/**
	 * @return the ourHalf
	 */
	public static IRectangle getFieldHalfOur()
	{
		return instance.ourHalf;
	}


	/**
	 * @return their half of the field
	 */
	public static IRectangle getFieldHalfTheir()
	{
		return instance.theirHalf;
	}


	/**
	 * @return the penaltyMark from us
	 */
	public static IVector2 getPenaltyMarkOur()
	{
		return Vector2f.fromXY((-getFieldLength() / 2) + getGoalCenterToPenaltyMark(), 0.0);
	}


	/**
	 * @return the penaltyMark from their
	 */
	public static IVector2 getPenaltyMarkTheir()
	{
		return Vector2f.fromXY((getFieldLength() / 2) - getGoalCenterToPenaltyMark(), 0.0);
	}


	/**
	 * @return The xExtent of the border around the field
	 */
	public static double getBoundaryWidth()
	{
		return getLastCamGeometry().getFieldSize().getBoundaryWidth();
	}


	/**
	 * @return the boundaryLength
	 */
	public static double getBoundaryLength()
	{
		return getBoundaryWidth();
	}


	public static double getLineWidth()
	{
		return getLastCamGeometry().getFieldSize().getLineThickness();
	}


	/**
	 * Returns the last received camera geometry frame.
	 *
	 * @return cam geometry
	 */
	public static CamGeometry getLastCamGeometry()
	{
		return instance.lastCamGeometry;
	}


	public static BallParameters getBallParameters()
	{
		return instance.ballParameters;
	}


	public static BallFactory getBallFactory()
	{
		return instance.ballFactory;
	}


	public static ETeamColor getNegativeHalfTeam()
	{
		return negativeHalfTeam;
	}


	public static void setNegativeHalfTeam(ETeamColor negativeHalfTeam)
	{
		Geometry.negativeHalfTeam = negativeHalfTeam;
	}


	private BallParameters createBallParameters(CamGeometry geometry)
	{
		BallParameters newBallParameters = new BallParameters();

		String env = SumatraModel.getInstance().getEnvironment();
		ConfigRegistration.applySpezis(newBallParameters, "geom", "");
		ConfigRegistration.applySpezis(newBallParameters, "geom", env);

		var models = geometry.getBallModels();
		if (models.hasStraightTwoPhase())
		{
			newBallParameters.setAccSlide(models.getStraightTwoPhase().getAccSlide() * 1000);
			newBallParameters.setAccRoll(models.getStraightTwoPhase().getAccRoll() * 1000);
			newBallParameters.setKSwitch(models.getStraightTwoPhase().getKSwitch());
		}
		if (models.hasChipFixedLoss())
		{
			newBallParameters.setChipDampingXYFirstHop(models.getChipFixedLoss().getDampingXyFirstHop());
			newBallParameters.setChipDampingXYOtherHops(models.getChipFixedLoss().getDampingXyOtherHops());
			newBallParameters.setChipDampingZ(models.getChipFixedLoss().getDampingZ());
		}
		return newBallParameters;
	}
}
