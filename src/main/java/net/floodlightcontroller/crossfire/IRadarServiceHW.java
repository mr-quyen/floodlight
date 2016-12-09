package net.floodlightcontroller.crossfire;

import java.util.List;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface IRadarServiceHW extends IFloodlightService {
	
	public enum CollectionMode {
		COLLECTION_MODE_TRIGGERIN,
		COLLECTION_MODE_ACTIVATED
	}
	
	public U64 register(String name, IRadarListener radarListerner, RadarConfiguration config);
	
	public void locate(U64 vectorId, DatapathId dpid,
					   List<IPv4AddressWithMask> splitted, List<IPv4AddressWithMask> merged, List<IPv4AddressWithMask> blocked);
	
	public U64 addDetectionVector(U64 cookie, DatapathId dpid, DetectionVectorDescriptor vector);
}
