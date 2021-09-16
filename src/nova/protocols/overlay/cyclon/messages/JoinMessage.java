package nova.protocols.overlay.cyclon.messages;

import nova.protocols.overlay.utils.AgedPeer;
import peernet.transport.Address;
import peernet.transport.NetworkMessage;

public class JoinMessage extends NetworkMessage {
	
	private AgedPeer p;
	
	public JoinMessage(int protoId, short type, Address sender, Address destination, AgedPeer p) {
		super(protoId, type, sender, destination);
		this.setPeer(p);
	}

	public AgedPeer getPeer() {
		return p;
	}

	public void setPeer(AgedPeer p) {
		this.p = p;
	}

}
