package nova.protocols.broadcast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import nova.controls.broadcast.interfaces.BroadcastCapable;
import nova.controls.broadcast.interfaces.DeliveryRecord;
import nova.protocols.broadcast.messages.BroadcastMessage;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Linkable;
import peernet.core.Protocol;
import peernet.transport.Address;

public class FloodBroadcast extends Protocol implements BroadcastCapable {

	/**
	   * The protocol to operate on.
	   * 
	   * @config
	   */
	static final String LINKABLE = "linkable";
	static final short BROADCAST_MSG = 0;
	private static int linkableProtocolID;
	
	private Set<Long> received;
	private Map<Long,Long> sentMessages;
	private Map<Long,DeliveryRecord> deliveredMessages;
	
	public FloodBroadcast(String prefix) {
		super(prefix);
		FloodBroadcast.linkableProtocolID = Configuration.getPid(prefix +"."+LINKABLE);
		
		this.received = new TreeSet<Long>();
		this.sentMessages = new HashMap<Long,Long>();
		this.deliveredMessages = new HashMap<Long,DeliveryRecord>();
	}

	public Object clone() {
		FloodBroadcast fb = (FloodBroadcast) super.clone();
		fb.received = new TreeSet<Long>();
		fb.sentMessages = new HashMap<Long,Long>();
		fb.deliveredMessages = new HashMap<Long, DeliveryRecord>();
		return fb;
	}
	
	@Override
	public void nextCycle(int schedId) {
		//nothing to be done
	}

	@Override
	public void processEvent(Address src, Object event) {
		BroadcastMessage bm = (BroadcastMessage) event; //Deviamos fazer cast para o NetworkMessage e checkar o type.
		if(!this.received.contains(bm.getMsgId())) {
			this.received.add(bm.getMsgId());
			bm.incrementHop();
			this.deliveredMessages.put(bm.getMsgId(), new DeliveryRecord(CommonState.getTime(), bm.getHopCount()));
			Linkable overlay = ((Linkable) myNode().getProtocol(linkableProtocolID));
			for(int i = 0; i < overlay.degree(); i++) {
				Address dest = overlay.getNeighbor(i).address;
				if(!dest.equals(src))
					send(dest, myPid(), bm.clone().updateHeader(myPeer().address, dest));
			}
		}
	}
	
	/**
	 * Interface BroadcastCapable
	 */
	@Override
	public void triggerBCast() {
		long msgId = Math.abs(CommonState.r.nextLong());
		Linkable overlay = ((Linkable) myNode().getProtocol(linkableProtocolID));
		for(int i = 0; i < overlay.degree(); i++) {
			Address dest = overlay.getNeighbor(i).address;
			send(dest, myPid(), new BroadcastMessage(myPid(), BROADCAST_MSG, myPeer().address, dest, msgId));
			this.sentMessages.put(msgId, CommonState.getTime());
			this.deliveredMessages.put(msgId, new DeliveryRecord(CommonState.getTime(), 0));
		}
	}

	@Override
	public Map<Long, Long> getSentMessages() {
		return this.sentMessages;
	}

	@Override
	public Map<Long, DeliveryRecord> getDeliveredMessages() {
		return this.deliveredMessages;
	}

	@Override
	public void resetMetrics() {
		this.sentMessages.clear();
		this.deliveredMessages.clear();
	}

}
