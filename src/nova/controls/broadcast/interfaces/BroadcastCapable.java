package nova.controls.broadcast.interfaces;

import java.util.Map;

public interface BroadcastCapable {

	public void triggerBCast();
	
	public Map<Long,Long> getSentMessages();
	
	public Map<Long,DeliveryRecord> getDeliveredMessages();
	
	public void resetMetrics();
}
