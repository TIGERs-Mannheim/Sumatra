/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationCameraViewport;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * The viewport architect inspects all camera geometries and aligns their viewports to a predefined overlap.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ViewportArchitect
{
	private final List<IBaseStation> baseStations;
	private Map<Integer, CamCalibration> calibrations = new HashMap<>();
	private Map<Integer, Viewport> viewports = new HashMap<>();
	
	@Configurable(defValue = "200", comment = "Maximum camera overlap. [mm]")
	private static double maxViewportOverlap = 200;
	
	@Configurable(defValue = "780,580", comment = "Pixel size of camera sensor")
	private static IVector2 sensorPixelSize = Vector2.fromXY(780, 580);
	
	@Configurable(defValue = "false", comment = "Add debug shapes")
	private static boolean debugShapes = false;
	
	static
	{
		ConfigRegistration.registerClass("vision", ViewportArchitect.class);
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param baseStations
	 */
	public ViewportArchitect(final List<IBaseStation> baseStations)
	{
		this.baseStations = baseStations;
	}
	
	
	/**
	 * Update architect with new geometry.
	 * 
	 * @param geometry
	 */
	public void newCameraGeometry(final CamGeometry geometry)
	{
		// insert calibrations into a map. This is important for multiple vision computers as not all geometry will come
		// from a single source.
		for (CamCalibration calib : geometry.getCalibrations().values())
		{
			calibrations.put(calib.getCameraId(), calib);
		}
		
		viewports.clear();
		for (CamCalibration calib : calibrations.values())
		{
			viewports.put(calib.getCameraId(), new Viewport(calib.getCameraPosition().getXYVector(),
					calib.getApproximatedViewport(sensorPixelSize)));
		}
		
		// adjust all viewports
		Viewport field = new Viewport(AVector2.ZERO_VECTOR, geometry.getField().getFieldWithBoundary());
		adjustViewports(field);
		
		// send viewports to BS
		for (CamCalibration calib : calibrations.values())
		{
			IRectangle viewport = getViewport(calib.getCameraId());
			if (viewport == null)
			{
				continue;
			}
			
			BaseStationCameraViewport cmd = new BaseStationCameraViewport(calib.getCameraId(), viewport);
			for (IBaseStation bs : baseStations)
			{
				bs.enqueueCommand(cmd);
			}
		}
	}
	
	
	private void adjustViewports(final Viewport field)
	{
		for (Viewport primary : viewports.values())
		{
			if (primary.min.x() < field.min.x())
			{
				primary.min.setX(field.min.x());
			}
			
			if (primary.max.x() > field.max.x())
			{
				primary.max.setX(field.max.x());
			}
			
			if (primary.min.y() < field.min.y())
			{
				primary.min.setY(field.min.y());
			}
			
			if (primary.max.y() > field.max.y())
			{
				primary.max.setY(field.max.y());
			}
			
			Optional<Viewport> right = nextViewportRight(primary);
			if (right.isPresent())
			{
				double center = (primary.max.x() + right.get().min.x()) / 2.0;
				
				primary.max.setX(center + (maxViewportOverlap / 2.0));
				right.get().min.setX(center - (maxViewportOverlap / 2.0));
			}
			
			Optional<Viewport> up = nextViewportUp(primary);
			if (up.isPresent())
			{
				double center = (primary.max.y() + up.get().min.y()) / 2.0;
				
				primary.max.setY(center + (maxViewportOverlap / 2.0));
				up.get().min.setY(center - (maxViewportOverlap / 2.0));
			}
		}
	}
	
	
	/**
	 * Get viewport of a specific camera.
	 * 
	 * @param camId
	 * @return
	 */
	public IRectangle getViewport(final int camId)
	{
		Viewport viewport = viewports.get(camId);
		if (viewport == null)
		{
			return null;
		}
		
		return viewports.get(camId).getRectangle();
	}
	
	private static class Viewport
	{
		private IVector2 center;
		private Vector2 min;
		private Vector2 max;
		
		
		private Viewport(final IVector2 center, final IRectangle rect)
		{
			this.center = center;
			min = Vector2.fromXY(rect.minX(), rect.minY());
			max = Vector2.fromXY(rect.maxX(), rect.maxY());
		}
		
		
		private IRectangle getRectangle()
		{
			return Rectangle.fromPoints(min, max);
		}
	}
	
	
	private Optional<Viewport> nextViewportRight(final Viewport left)
	{
		IVector2 start = left.center;
		return viewports.values().stream()
				.sorted((v1, v2) -> Double.compare(v1.center.x(), v2.center.x()))
				.filter(v -> v.center.x() > (start.x() + 50.0))
				.filter(v -> (v.center.y() < (start.y() + 1000.0))
						&& (v.center.y() > (start.y() - 1000.0)))
				.findFirst();
	}
	
	
	private Optional<Viewport> nextViewportUp(final Viewport down)
	{
		IVector2 start = down.center;
		return viewports.values().stream()
				.sorted((v1, v2) -> Double.compare(v1.center.y(), v2.center.y()))
				.filter(v -> v.center.y() > (start.y() + 50.0))
				.filter(v -> (v.center.x() < (start.x() + 1000.0))
						&& (v.center.x() > (start.x() - 1000.0)))
				.findFirst();
	}
	
	
	/**
	 * Viewport info shapes.
	 * 
	 * @return
	 */
	public List<IDrawableShape> getInfoShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		
		for (CamCalibration calib : calibrations.values())
		{
			shapes.addAll(getCalibrationShapes(calib));
		}
		
		for (Viewport viewport : viewports.values())
		{
			DrawableRectangle drawRect = new DrawableRectangle(viewport.getRectangle(), Color.GREEN);
			shapes.add(drawRect);
		}
		
		return shapes;
	}
	
	
	private List<IDrawableShape> getCalibrationShapes(final CamCalibration calibration)
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		
		// draw projected camera center
		IVector2 pos = calibration.imageToField(Vector2.fromXY(sensorPixelSize.x() / 2, sensorPixelSize.y() / 2), 150);
		shapes.add(new DrawableCircle(pos, 20, Color.CYAN));
		
		// draw inner camera rectangles (axes-aligned)
		IRectangle rect = calibration.getApproximatedViewport(sensorPixelSize);
		DrawableRectangle drawRect = new DrawableRectangle(rect, new Color(50 + (calibration.getCameraId() * 25), 0, 0));
		shapes.add(drawRect);
		
		if (debugShapes)
		{
			// Draw camera viewport projected to ground with distortion
			for (int x = 0; x <= sensorPixelSize.x(); x += 20)
			{
				pos = calibration.imageToField(Vector2.fromXY(x, 0), 0);
				DrawableCircle c = new DrawableCircle(pos, 10, Color.CYAN);
				shapes.add(c);
				pos = calibration.imageToField(Vector2.fromXY(x, 580), 0);
				c = new DrawableCircle(pos, 10, Color.CYAN);
				shapes.add(c);
				
				pos = calibration.imageToField(Vector2.fromXY(x, 0), 150);
				c = new DrawableCircle(pos, 5, Color.CYAN);
				shapes.add(c);
				pos = calibration.imageToField(Vector2.fromXY(x, 580), 150);
				c = new DrawableCircle(pos, 5, Color.CYAN);
				shapes.add(c);
			}
			
			for (int y = 0; y <= sensorPixelSize.y(); y += 20)
			{
				pos = calibration.imageToField(Vector2.fromXY(0, y), 0);
				DrawableCircle c = new DrawableCircle(pos, 10, Color.CYAN);
				shapes.add(c);
				pos = calibration.imageToField(Vector2.fromXY(780, y), 0);
				c = new DrawableCircle(pos, 10, Color.CYAN);
				shapes.add(c);
				
				pos = calibration.imageToField(Vector2.fromXY(0, y), 150);
				c = new DrawableCircle(pos, 5, Color.CYAN);
				shapes.add(c);
				pos = calibration.imageToField(Vector2.fromXY(780, y), 150);
				c = new DrawableCircle(pos, 5, Color.CYAN);
				shapes.add(c);
			}
		}
		
		return shapes;
	}
}
