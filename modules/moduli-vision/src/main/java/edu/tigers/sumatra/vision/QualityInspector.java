/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.tracker.RobotTracker;


/**
 * The quality inspector checks various vision data and reports quality issues.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class QualityInspector
{
	private List<CamCalibration>			problematicCams				= new ArrayList<>();
	private List<RobotDeviationIssue>	deviationIssues				= new ArrayList<>();
	private List<RobotInvisibleIssue>	invisibleIssues				= new ArrayList<>();
	private List<FilteredVisionBot>		filteredBots					= new ArrayList<>();
	
	@Configurable(defValue = "true", comment = "Draw camera issues in quality layer.")
	private static boolean					drawCameraIssues				= true;
	
	@Configurable(defValue = "true", comment = "Draw robot deviation issues in quality layer.")
	private static boolean					drawRobotDeviationIssues	= true;
	
	@Configurable(defValue = "true", comment = "Draw invisible robot issues in quality layer.")
	private static boolean					drawRobotInvisibleIssues	= true;
	
	@Configurable(defValue = "true", comment = "Draw robot vision quality issues in quality layer.")
	private static boolean					drawRobotQualityIssues		= true;
	
	@Configurable(defValue = "300", comment = "Maximum allowed camera height difference to median height. [mm]")
	private static double					maxHeightDifference			= 300;
	
	@Configurable(defValue = "50", comment = "Maximum allowed distance between a robot on two cameras. [mm]")
	private static double					maxOverlapDist					= 50;
	
	@Configurable(defValue = "10", comment = "Time until issues are removed. [s]")
	private static double					issueTimeout					= 10;
	
	@Configurable(defValue = "1", comment = "Robots faster than this will not be checked for position deviation. [m/s]")
	private static double					maxSpeedForDeviation			= 1;
	
	@Configurable(defValue = "0.02", comment = "Maximum difference between different camera timestamps. [s]")
	private static double					maxTimeDiffForDeviation		= 0.02;
	
	@Configurable(defValue = "1.5", comment = "Robots faster than this will be checked for invisibility. [m/s]")
	private static double					minSpeedForInvisibleCheck	= 1.5;
	
	@Configurable(defValue = "0.1", comment = "Time until an invisible fast robot is reported. [s]")
	private static double					minInvisibleTime				= 0.1;
	
	static
	{
		ConfigRegistration.registerClass("vision", QualityInspector.class);
	}
	
	
	/**
	 * Inspect robots for issues.
	 * 
	 * @param camFilters
	 * @param timestamp
	 */
	public void inspectRobots(final Collection<CamFilter> camFilters, final long timestamp)
	{
		// remove old issues
		deviationIssues.removeIf(i -> (timestamp - i.lastOccurence) > (issueTimeout * 1e9));
		invisibleIssues.removeIf(i -> (timestamp - i.lastOccurence) > (issueTimeout * 1e9));
		
		// just get all RobotTracker in one list
		List<RobotTracker> allTrackers = camFilters.stream()
				.flatMap(f -> f.getValidRobots().values().stream())
				.collect(Collectors.toList());
		
		for (CamFilter filter : camFilters)
		{
			if (!filter.getViewport().isPresent())
			{
				continue;
			}
			
			IRectangle viewport = filter.getViewport().get();
			checkInvisibleRobot(timestamp, filter, viewport);
		}
		
		// group trackers by BotID
		Map<BotID, List<RobotTracker>> trackersById = allTrackers.stream()
				.filter(t -> (timestamp - t.getLastUpdateTimestamp()) < (maxTimeDiffForDeviation * 1e9))
				.filter(t -> t.getVelocity().getLength2() < (maxSpeedForDeviation * 1e3))
				.collect(Collectors.groupingBy(RobotTracker::getBotId));
		
		// go through all grouped trackers
		for (List<RobotTracker> trackers : trackersById.values())
		{
			// robots on a single camera cannot be judged
			if (trackers.size() == 1)
			{
				continue;
			}
			
			// create all possible combinations of trackers in this group
			List<Pair<RobotTracker, RobotTracker>> allPairs = new ArrayList<>();
			for (int i = 0; i < trackers.size(); i++)
			{
				for (int j = i; j < trackers.size(); j++)
				{
					allPairs.add(new Pair<>(trackers.get(i), trackers.get(j)));
				}
			}
			
			// investigate tracker distances
			checkPositionDeviation(timestamp, allPairs);
		}
	}
	
	
	private void checkInvisibleRobot(final long timestamp, final CamFilter filter, final IRectangle viewport)
	{
		for (RobotTracker tracker : filter.getValidRobots().values())
		{
			if (viewport.isPointInShape(tracker.getPosition(timestamp))
					&& (tracker.getVelocity().getLength2() > (minSpeedForInvisibleCheck * 1e3))
					&& ((timestamp - tracker.getLastUpdateTimestamp()) > (minInvisibleTime * 1e9)))
			{
				Optional<RobotInvisibleIssue> sameBotIssue = invisibleIssues.stream()
						.filter(i -> i.botId.equals(tracker.getBotId()))
						.findFirst();
				
				if (!sameBotIssue.isPresent())
				{
					RobotInvisibleIssue issue = new RobotInvisibleIssue();
					issue.botId = tracker.getBotId();
					issue.firstPos = tracker.getPosition(timestamp);
					issue.lastPos = issue.firstPos;
					issue.lastOccurence = timestamp;
					
					invisibleIssues.add(issue);
					
					continue;
				}
				
				if (sameBotIssue.get().lastPos.distanceTo(tracker.getPosition(timestamp)) < 1000)
				{
					sameBotIssue.get().lastOccurence = timestamp;
					sameBotIssue.get().lastPos = tracker.getPosition(timestamp);
				} else
				{
					sameBotIssue.get().botId = tracker.getBotId();
					sameBotIssue.get().firstPos = tracker.getPosition(timestamp);
					sameBotIssue.get().lastPos = sameBotIssue.get().firstPos;
					sameBotIssue.get().lastOccurence = timestamp;
				}
			}
		}
	}
	
	
	private void checkPositionDeviation(final long timestamp, final List<Pair<RobotTracker, RobotTracker>> allPairs)
	{
		for (Pair<RobotTracker, RobotTracker> pair : allPairs)
		{
			double dist = pair.getFirst().getPosition(timestamp).distanceTo(pair.getSecond().getPosition(timestamp));
			
			if (dist > maxOverlapDist)
			{
				// that's bad
				Optional<RobotDeviationIssue> sameBotIssue = deviationIssues.stream()
						.filter(i -> i.botId.equals(pair.getFirst().getBotId()))
						.findFirst();
				
				if (sameBotIssue.isPresent())
				{
					// update same issue
					sameBotIssue.get().firstPos = pair.getFirst().getPosition(timestamp);
					sameBotIssue.get().secondPos = pair.getSecond().getPosition(timestamp);
					sameBotIssue.get().lastOccurence = timestamp;
					sameBotIssue.get().firstCam = pair.getFirst().getCamId();
					sameBotIssue.get().secondCam = pair.getSecond().getCamId();
					continue;
				}
				
				RobotDeviationIssue issue = new RobotDeviationIssue();
				issue.firstPos = pair.getFirst().getPosition(timestamp);
				issue.secondPos = pair.getSecond().getPosition(timestamp);
				issue.botId = pair.getFirst().getBotId();
				issue.lastOccurence = timestamp;
				issue.firstCam = pair.getFirst().getCamId();
				issue.secondCam = pair.getSecond().getCamId();
				
				long nearByIssues = deviationIssues.stream()
						.filter(i -> i.getCenterPos().distanceTo(issue.getCenterPos()) < 200)
						.count();
				
				if (nearByIssues == 0)
				{
					deviationIssues.add(issue);
				}
			}
		}
	}
	
	private static class RobotDeviationIssue
	{
		private IVector2	firstPos;
		private IVector2	secondPos;
		private int			firstCam;
		private int			secondCam;
		private BotID		botId;
		private long		lastOccurence;
		
		
		private IVector2 getCenterPos()
		{
			return firstPos.addNew(secondPos).multiply(0.5);
		}
	}
	
	private static class RobotInvisibleIssue
	{
		private IVector2	firstPos;
		private IVector2	lastPos;
		private BotID		botId;
		private long		lastOccurence;
		
		
		private IVector2 getCenterPos()
		{
			return firstPos.addNew(lastPos).multiply(0.5);
		}
	}
	
	
	/**
	 * Inspect new filtered vision frame.
	 * 
	 * @param frame
	 */
	public void inspectFilteredVisionFrame(final FilteredVisionFrame frame)
	{
		filteredBots = frame.getBots();
	}
	
	
	/**
	 * Inspect camera geometry for issues.
	 * 
	 * @param geometry
	 */
	public void inspectCameraGeometry(final CamGeometry geometry)
	{
		checkCameraHeights(geometry);
	}
	
	
	private void checkCameraHeights(final CamGeometry geometry)
	{
		problematicCams.clear();
		
		if (geometry.getCalibrations().size() < 2)
		{
			return;
		}
		
		List<Double> camHeights = geometry.getCalibrations().values().stream()
				.map(c -> c.getCameraPosition().z())
				.sorted(Double::compare)
				.collect(Collectors.toList());
		
		double median = StatisticsMath.median(camHeights);
		
		List<CamCalibration> calibrations = new ArrayList<>();
		calibrations.addAll(geometry.getCalibrations().values());
		
		problematicCams = calibrations.stream()
				.filter(c -> Math.abs(median - c.getCameraPosition().z()) > maxHeightDifference)
				.collect(Collectors.toList());
	}
	
	
	/**
	 * Quality info and warning shapes.
	 * 
	 * @return
	 */
	public List<IDrawableShape> getInfoShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		
		if (drawCameraIssues)
		{
			// mark problematic cameras (height)
			for (CamCalibration calib : problematicCams)
			{
				DrawableCircle circle = new DrawableCircle(calib.getCameraPosition().getXYVector(), 200, Color.RED);
				circle.setStrokeWidth(20);
				shapes.add(circle);
				
				DrawableAnnotation warn = new DrawableAnnotation(
						calib.getCameraPosition().getXYVector().addNew(Vector2.fromXY(0, 280)), "CAM_HEIGHT", true);
				warn.setColor(Color.RED);
				warn.setFontHeight(100);
				warn.setBold(true);
				shapes.add(warn);
			}
		}
		
		if (drawRobotDeviationIssues)
		{
			// draw problematic robot deviations
			for (RobotDeviationIssue issue : deviationIssues)
			{
				DrawableCircle circle = new DrawableCircle(issue.getCenterPos(), 160, Color.ORANGE);
				circle.setStrokeWidth(20);
				shapes.add(circle);
				
				DrawableLine line = new DrawableLine(Line.fromPoints(issue.firstPos, issue.secondPos), Color.ORANGE);
				line.setStrokeWidth(10);
				shapes.add(line);
				
				DrawableAnnotation botId = new DrawableAnnotation(issue.getCenterPos().addNew(Vector2.fromXY(0, 240)),
						issue.botId.toString(), true);
				botId.setColor(Color.ORANGE);
				botId.setFontHeight(100);
				botId.setBold(true);
				shapes.add(botId);
				
				DrawableAnnotation camIds = new DrawableAnnotation(issue.getCenterPos().addNew(Vector2.fromXY(0, -240)),
						issue.firstCam + " <-> " + issue.secondCam, true);
				camIds.setColor(Color.ORANGE);
				camIds.setFontHeight(100);
				camIds.setBold(true);
				shapes.add(camIds);
				
				DrawableAnnotation dist = new DrawableAnnotation(issue.getCenterPos().addNew(Vector2.fromXY(0, -340)),
						String.format("%.1fmm", issue.firstPos.distanceTo(issue.secondPos)), true);
				dist.setColor(Color.ORANGE);
				dist.setFontHeight(80);
				dist.setBold(true);
				shapes.add(dist);
			}
		}
		
		if (drawRobotInvisibleIssues)
		{
			// draw invisible fast robots
			for (RobotInvisibleIssue issue : invisibleIssues)
			{
				DrawableCircle circle = new DrawableCircle(issue.getCenterPos(), 160, Color.YELLOW);
				circle.setStrokeWidth(20);
				shapes.add(circle);
				
				DrawableLine line = new DrawableLine(Line.fromPoints(issue.firstPos, issue.lastPos), Color.YELLOW);
				line.setStrokeWidth(20);
				shapes.add(line);
				
				DrawableAnnotation botId = new DrawableAnnotation(issue.getCenterPos().addNew(Vector2.fromXY(0, 240)),
						issue.botId.toString(), true);
				botId.setColor(Color.YELLOW);
				botId.setFontHeight(100);
				botId.setBold(true);
				shapes.add(botId);
			}
		}
		
		if (drawRobotQualityIssues)
		{
			for (FilteredVisionBot bot : filteredBots)
			{
				if (bot.getQuality() > 0.8)
				{
					continue;
				}
				
				DrawableAnnotation unc = new DrawableAnnotation(bot.getPos(),
						String.format("%.0f", bot.getQuality() * 100), true);
				unc.setOffset(Vector2.fromY(120));
				unc.setColor(bot.getBotID().getTeamColor().getColor());
				unc.setFontHeight(60);
				shapes.add(unc);
			}
		}
		
		return shapes;
	}
}
