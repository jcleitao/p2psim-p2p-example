package nova.protocols.broadcast.messages;

import peernet.transport.Address;
import peernet.transport.NetworkMessage;

public class BroadcastMessage extends NetworkMessage implements Cloneable {

	private long msgId;
	private int hop;
	
	public BroadcastMessage(int protoId, short type, Address sender, Address destination, long msgId) {
		super(protoId, type, sender, destination);
		this.setMsgId(msgId);
		this.hop = 0;
	}

	public long getMsgId() {
		return msgId;
	}

	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	
	public int getHopCount() {
		return this.hop;
	}
	
	public BroadcastMessage incrementHop() {
		this.hop++;
		return this;
	}
	
	public BroadcastMessage clone() {
		return (BroadcastMessage) super.clone();
	}

	public BroadcastMessage updateHeader(Address address, Address dest) {
		this.setSender(address);
		this.setDestination(dest);
		return this;
	}

}
