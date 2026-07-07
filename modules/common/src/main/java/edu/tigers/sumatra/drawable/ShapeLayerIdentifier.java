package edu.tigers.sumatra.drawable;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;


@Value
@Builder(toBuilder = true, builderMethodName = "")
public class ShapeLayerIdentifier implements IShapeLayerIdentifier
{
	String id;
	String layerName;
	@Singular
	List<String> categories;
	@Builder.Default
	ShapeMap.EShapeLayerPersistenceType persistenceType = ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
	boolean visibleByDefault;
	int orderId;


	/**
	 * Package-private builder to protect it from external callers.
	 * Should only be used by the ShapeLayerIdentifierFactory.
	 *
	 * @return
	 */
	static ShapeLayerIdentifierBuilder builder()
	{
		return new ShapeLayerIdentifierBuilder();
	}


	@Override
	public String getCategory()
	{
		throw new IllegalStateException();
	}


	@Override
	public String toString()
	{
		return layerName;
	}
}
