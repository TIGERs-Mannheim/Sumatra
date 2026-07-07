package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.persistence.PersistenceTable;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;


/**
 * Entity for vision data
 */
@Data
@RequiredArgsConstructor
public class PersistenceCamDetectionFrame implements PersistenceTable.IEntry<PersistenceCamDetectionFrame>
{
	private final long timestamp;

	private final Map<Integer, ExtendedCamDetectionFrame> camFrames;


	@Override
	public long getKey()
	{
		return timestamp;
	}


	@Override
	public void merge(PersistenceCamDetectionFrame other)
	{
		camFrames.putAll(other.camFrames);
	}
}
