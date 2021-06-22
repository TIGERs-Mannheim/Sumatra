/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * The viewport architect inspects all camera geometries and aligns their viewports to a predefined overlap.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class ViewportArchitect
{
	private Map<Integer, Viewport> viewports = new ConcurrentSkipListMap<>();
	private final List<IViewportArchitect> observers = new CopyOnWriteArrayList<>();
	private Viewport field;


	@Configurable(defValue = "400.0", comment = "Maximum camera overlap. [mm]")
	private static double maxViewportOverlap = 400.0;

	@Configurable(defValue = "DYNAMICALLY", comment = "Method to be used to construct viewports.")
	private static EViewportConstruction viewportConstruction = EViewportConstruction.DYNAMICALLY;

	@Configurable(defValue = "50.0", comment = "Update rate for viewport changes to base station (per camera). [Hz]")
	private static double reportRate = 50.0;

	static
	{
		ConfigRegistration.registerClass("vision", ViewportArchitect.class);
	}

	private enum EViewportConstruction
	{
		DYNAMICALLY,
		FROM_FIELD_SIZE,
		FROM_CAM_POS,
		FROM_CAM_PROJECTION,
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
			int camId = calib.getCameraId();

			// check if the camera calibration changed => someone playing around with vision
			if (viewports.containsKey(camId) && !viewports.get(camId).calib.similarTo(calib))
			{
				viewports.remove(camId);
			}

			viewports.computeIfAbsent(camId, id -> new Viewport(calib.getCameraPosition().getXYVector(),
					calib.imageToField(calib.getPrincipalPoint(), 0), calib));
		}

		field = new Viewport(Vector2f.ZERO_VECTOR, geometry.getField().getFieldWithBoundary());
	}


	/**
	 * Update with new detection frame. Adjusts dynamic viewports.
	 *
	 * @param frame
	 */
	public void newDetectionFrame(final CamDetectionFrame frame)
	{
		Viewport viewport = viewports.get(frame.getCameraId());
		if (viewport == null)
		{
			return;
		}

		List<IVector2> allPositions = frame.getRobots().stream()
				.map(CamRobot::getPos)
				.collect(Collectors.toList());

		allPositions.add(viewport.dynamicMax);
		allPositions.add(viewport.dynamicMin);

		double maxX = allPositions.stream().mapToDouble(IVector2::x).max().orElse(viewport.dynamicMax.x());
		double maxY = allPositions.stream().mapToDouble(IVector2::y).max().orElse(viewport.dynamicMax.y());
		double minX = allPositions.stream().mapToDouble(IVector2::x).min().orElse(viewport.dynamicMin.x());
		double minY = allPositions.stream().mapToDouble(IVector2::y).min().orElse(viewport.dynamicMin.y());

		viewport.dynamicMax.setX(maxX);
		viewport.dynamicMax.setY(maxY);
		viewport.dynamicMin.setX(minX);
		viewport.dynamicMin.setY(minY);

		if (field == null)
		{
			return;
		}

		switch (viewportConstruction)
		{
			case DYNAMICALLY:
				adjustViewportsDynamically(field);
				break;
			case FROM_CAM_POS:
				adjustViewportsFromCameraPos(field);
				break;
			case FROM_CAM_PROJECTION:
				adjustViewportsFromPrincipalProjection(field);
				break;
			case FROM_FIELD_SIZE:
				adjustViewportsFromFieldSize(field);
				break;
			default:
				break;
		}

		for (Entry<Integer, Viewport> entry : viewports.entrySet())
		{
			double timeSinceLastReport = (frame.gettCapture() - entry.getValue().lastReportTimestamp) * 1e-9;

			if (timeSinceLastReport > (1.0 / reportRate))
			{
				notifyViewportUpdated(entry.getKey(), entry.getValue().getRectangle());
				entry.getValue().lastReportTimestamp = frame.gettCapture();
			}
		}
	}


	public void updateCameras(Set<Integer> cameraIds)
	{
		viewports.keySet().removeIf(id -> !cameraIds.contains(id));
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IViewportArchitect observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IViewportArchitect observer)
	{
		observers.remove(observer);
	}


	private void notifyViewportUpdated(final int cameraId, final IRectangle viewport)
	{
		observers.forEach(o -> o.onViewportUpdated(cameraId, viewport));
	}


	private void adjustViewportsFromFieldSize(final Viewport field)
	{
		if (viewports.isEmpty())
		{
			return;
		}

		Viewport start = viewports.values().iterator().next();

		int numCamerasUp = countViewports(0, start, EDirection.UP);
		int numCamerasDown = countViewports(0, start, EDirection.DOWN);
		int numCamerasLeft = countViewports(0, start, EDirection.LEFT);
		int numCamerasRight = countViewports(0, start, EDirection.RIGHT);

		int numCamsY = 1 + numCamerasUp + numCamerasDown;
		int numCamsX = 1 + numCamerasLeft + numCamerasRight;

		IVector2 fieldSize = field.max.subtractNew(field.min);
		IVector2 step = fieldSize.multiplyNew(Vector2.fromXY(1.0 / numCamsX, 1.0 / numCamsY));

		for (Viewport primary : viewports.values())
		{
			double column = Math.floor(primary.center.x() / step.x());
			double row = Math.floor(primary.center.y() / step.y());

			primary.min.setX((column * step.x()) - (maxViewportOverlap / 2.0));
			primary.min.setY((row * step.y()) - (maxViewportOverlap / 2.0));
			primary.max.setX(((column + 1) * step.x()) + (maxViewportOverlap / 2.0));
			primary.max.setY(((row + 1) * step.y()) + (maxViewportOverlap / 2.0));
		}
	}


	private void adjustViewportsFromCameraPos(final Viewport field)
	{
		for (Viewport primary : viewports.values())
		{
			Optional<Viewport> up = nextViewport(primary, EDirection.UP);
			Optional<Viewport> down = nextViewport(primary, EDirection.DOWN);
			Optional<Viewport> left = nextViewport(primary, EDirection.LEFT);
			Optional<Viewport> right = nextViewport(primary, EDirection.RIGHT);

			if (up.isPresent())
			{
				double center = (primary.center.y() + up.get().center.y()) / 2.0;

				primary.max.setY(center + (maxViewportOverlap / 2.0));
			} else
			{
				primary.max.setY(field.max.y());
			}

			if (down.isPresent())
			{
				double center = (primary.center.y() + down.get().center.y()) / 2.0;

				primary.min.setY(center - (maxViewportOverlap / 2.0));
			} else
			{
				primary.min.setY(field.min.y());
			}

			if (left.isPresent())
			{
				double center = (primary.center.x() + left.get().center.x()) / 2.0;

				primary.min.setX(center - (maxViewportOverlap / 2.0));
			} else
			{
				primary.min.setX(field.min.x());
			}

			if (right.isPresent())
			{
				double center = (primary.center.x() + right.get().center.x()) / 2.0;

				primary.max.setX(center + (maxViewportOverlap / 2.0));
			} else
			{
				primary.max.setX(field.max.x());
			}
		}
	}


	private void adjustViewportsFromPrincipalProjection(final Viewport field)
	{
		for (Viewport primary : viewports.values())
		{
			Optional<Viewport> up = nextViewport(primary, EDirection.UP);
			Optional<Viewport> down = nextViewport(primary, EDirection.DOWN);
			Optional<Viewport> left = nextViewport(primary, EDirection.LEFT);
			Optional<Viewport> right = nextViewport(primary, EDirection.RIGHT);

			if (up.isPresent())
			{
				double center = (primary.principalProjection.y() + up.get().principalProjection.y()) / 2.0;

				primary.max.setY(center + (maxViewportOverlap / 2.0));
			} else
			{
				primary.max.setY(field.max.y());
			}

			if (down.isPresent())
			{
				double center = (primary.principalProjection.y() + down.get().principalProjection.y()) / 2.0;

				primary.min.setY(center - (maxViewportOverlap / 2.0));
			} else
			{
				primary.min.setY(field.min.y());
			}

			if (left.isPresent())
			{
				double center = (primary.principalProjection.x() + left.get().principalProjection.x()) / 2.0;

				primary.min.setX(center - (maxViewportOverlap / 2.0));
			} else
			{
				primary.min.setX(field.min.x());
			}

			if (right.isPresent())
			{
				double center = (primary.principalProjection.x() + right.get().principalProjection.x()) / 2.0;

				primary.max.setX(center + (maxViewportOverlap / 2.0));
			} else
			{
				primary.max.setX(field.max.x());
			}
		}
	}


	private void adjustViewportsDynamically(final Viewport field)
	{
		for (Viewport primary : viewports.values())
		{
			Optional<Viewport> up = nextViewport(primary, EDirection.UP);
			Optional<Viewport> down = nextViewport(primary, EDirection.DOWN);
			Optional<Viewport> left = nextViewport(primary, EDirection.LEFT);
			Optional<Viewport> right = nextViewport(primary, EDirection.RIGHT);

			if (up.isPresent())
			{
				double center = (primary.dynamicMax.y() + up.get().dynamicMin.y()) / 2.0;

				primary.max.setY(center + (maxViewportOverlap / 2.0));
			} else
			{
				primary.max.setY(field.max.y());
			}

			if (down.isPresent())
			{
				double center = (primary.dynamicMin.y() + down.get().dynamicMax.y()) / 2.0;

				primary.min.setY(center - (maxViewportOverlap / 2.0));
			} else
			{
				primary.min.setY(field.min.y());
			}

			if (left.isPresent())
			{
				double center = (primary.dynamicMin.x() + left.get().dynamicMax.x()) / 2.0;

				primary.min.setX(center - (maxViewportOverlap / 2.0));
			} else
			{
				primary.min.setX(field.min.x());
			}

			if (right.isPresent())
			{
				double center = (primary.dynamicMax.x() + right.get().dynamicMin.x()) / 2.0;

				primary.max.setX(center + (maxViewportOverlap / 2.0));
			} else
			{
				primary.max.setX(field.max.x());
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

		return viewport.getRectangle();
	}


	private enum EDirection
	{
		UP,
		DOWN,
		LEFT,
		RIGHT
	}

	private static class Viewport
	{
		private final IVector2 center;
		private final CamCalibration calib;
		private final IVector2 principalProjection;
		private Vector2 min;
		private Vector2 max;
		private Vector2 dynamicMin;
		private Vector2 dynamicMax;
		private long lastReportTimestamp;


		private Viewport(final IVector2 center, final IVector2 principalProjection, final CamCalibration calib)
		{
			this.center = center;
			this.calib = calib;
			this.principalProjection = principalProjection;
			min = Vector2.copy(center);
			max = Vector2.copy(center);
			dynamicMin = Vector2.copy(center);
			dynamicMax = Vector2.copy(center);
		}


		private Viewport(final IVector2 center, final IRectangle rect)
		{
			this.center = center;
			calib = null;
			principalProjection = center;
			min = Vector2.fromXY(rect.minX(), rect.minY());
			max = Vector2.fromXY(rect.maxX(), rect.maxY());
			dynamicMin = Vector2.copy(center);
			dynamicMax = Vector2.copy(center);
		}


		private IRectangle getRectangle()
		{
			return Rectangle.fromPoints(min, max);
		}


		private IRectangle getDynamicRectangle()
		{
			return Rectangle.fromPoints(dynamicMin, dynamicMax);
		}
	}


	private Optional<Viewport> nextViewport(final Viewport origin, final EDirection direction)
	{
		final IVector2 start = origin.center;
		List<Viewport> closestFour = viewports.values().stream()
				.filter(v -> !v.equals(origin))
				.sorted(Comparator.comparingDouble(v -> v.center.distanceToSqr(start)))
				.limit(4)
				.collect(Collectors.toList());

		switch (direction)
		{
			case UP:
				return closestFour.stream()
						.filter(v -> Vector2.fromPoints(start, v.center).angleToAbs(Vector2f.Y_AXIS)
								.orElse(AngleMath.PI) < AngleMath.PI_QUART)
						.findFirst();
			case DOWN:
				return closestFour.stream()
						.filter(v -> Vector2.fromPoints(start, v.center).angleToAbs(Vector2f.Y_AXIS.multiplyNew(-1.0))
								.orElse(AngleMath.PI) < AngleMath.PI_QUART)
						.findFirst();
			case RIGHT:
				return closestFour.stream()
						.filter(v -> Vector2.fromPoints(start, v.center).angleToAbs(Vector2f.X_AXIS)
								.orElse(AngleMath.PI) < AngleMath.PI_QUART)
						.findFirst();
			case LEFT:
				return closestFour.stream()
						.filter(v -> Vector2.fromPoints(start, v.center).angleToAbs(Vector2f.X_AXIS.multiplyNew(-1.0))
								.orElse(AngleMath.PI) < AngleMath.PI_QUART)
						.findFirst();
			default:
				return Optional.empty();
		}
	}


	private int countViewports(final int startValue, final Viewport origin, final EDirection direction)
	{
		Optional<Viewport> next = nextViewport(origin, direction);
		return next.map(viewport -> countViewports(startValue + 1, viewport, direction)).orElse(startValue);
	}


	/**
	 * Viewport info shapes.
	 *
	 * @return
	 */
	public List<IDrawableShape> getInfoShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		for (Viewport viewport : viewports.values())
		{
			DrawableRectangle drawRect = new DrawableRectangle(viewport.getRectangle(), Color.GREEN);
			shapes.add(drawRect);

			DrawableRectangle drawRectDyn = new DrawableRectangle(viewport.getDynamicRectangle(), Color.GRAY);
			shapes.add(drawRectDyn);

			DrawableCircle center = new DrawableCircle(viewport.center, 20, Color.GREEN);
			shapes.add(center);
		}

		return shapes;
	}


	/**
	 * ViewportArchitect Observer interface.
	 *
	 * @author AndreR
	 */
	public interface IViewportArchitect
	{
		void onViewportUpdated(int cameraId, IRectangle viewport);
	}
}
