package nova.protocols.broadcast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
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

public class LogFileFloodBroadcast extends Protocol implements BroadcastCapable {

	/**
	   * The protocol to operate on.
	   * 
	   * @config
	   */
	static final String LINKABLE = "linkable";
	static final String FILENAME = "filename";
	static final short BROADCAST_MSG = 0;
	private static int linkableProtocolID;
	
	private Set<Long> received;
	private PrintStream out;
	
	public LogFileFloodBroadcast(String prefix) throws FileNotFoundException {
		super(prefix);
		LogFileFloodBroadcast.linkableProtocolID = Configuration.getPid(prefix +"."+LINKABLE);
		
		String file = Configuration.getString(prefix + "." + FILENAME);
		
		this.received = new TreeSet<Long>();
		this.out = new PrintStream(new File(CommonState.getExperienceName() + "-" + file + ".txt"));
	}

	public Object clone() {
		LogFileFloodBroadcast fb = (LogFileFloodBroadcast) super.clone();
		fb.received = new TreeSet<Long>();
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
			this.out.println(bm.getMsgId() + ",node" + myNode().getID() + ",RECEIVED," + CommonState.getEndTime() + "," + bm.getHopCount());
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
		BroadcastMessage bm = new BroadcastMessage(myPid(), BROADCAST_MSG, myPeer().address, myPeer().address, msgId);
		this.out.println(bm.getMsgId() + ",node" + myNode().getID() + ",SEND," + CommonState.getEndTime() + "," + bm.getHopCount());
		for(int i = 0; i < overlay.degree(); i++) {
			Address dest = overlay.getNeighbor(i).address;
			BroadcastMessage mb = bm.clone();
			mb.setDestination(dest);
			send(dest, myPid(), mb);
			this.out.println(mb.getMsgId() + ",node" + myNode().getID() + ",RECEIVED," + CommonState.getEndTime() + "," + mb.getHopCount());
		}
	}

	@Override
	public Map<Long, Long> getSentMessages() {
		return Collections.emptyMap();
	}

	@Override
	public Map<Long, DeliveryRecord> getDeliveredMessages() {
		return Collections.emptyMap();
	}

	@Override
	public void resetMetrics() {
		this.received.clear();
	}

}
