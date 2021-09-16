package nova.controls.broadcast.interfaces;

public class DeliveryRecord {

	private final long deliveryTime;
	private final long deliveryHop;
	
	public DeliveryRecord(long deliveryTime, long deliveryHop) {
		this.deliveryTime = deliveryTime;
		this.deliveryHop = deliveryHop;
	}

	public long getDeliveryTime() {
		return deliveryTime;
	}

	public long getDeliveryHop() {
		return deliveryHop;
	}

}
