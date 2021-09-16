package nova.protocols.overlay.cyclon.messages;

import nova.protocols.overlay.utils.AgedPeer;
import peernet.transport.Address;
import peernet.transport.NetworkMessage;

public class JoinRandomWalk extends NetworkMessage {

	private AgedPeer p;
	private short ttl;
	
	public JoinRandomWalk(int protoId, short type, Address sender, Address destination, AgedPeer p, short ttl) {
		super(protoId, type, sender, destination);
		this.setPeer(p);
		this.setTTL(ttl);
	}

	public AgedPeer getPeer() {
		return p;
	}

	public void setPeer(AgedPeer p) {
		this.p = p;
	}

	public short getTTL() {
		return ttl;
	}

	public void setTTL(short ttl) {
		this.ttl = ttl;
	}
	
	public short decrementTTL() {
		return --this.ttl;
	}

}
