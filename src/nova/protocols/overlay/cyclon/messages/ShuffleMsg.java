package nova.protocols.overlay.cyclon.messages;

import java.util.Iterator;
import java.util.List;

import nova.protocols.overlay.utils.AgedPeer;
import peernet.transport.Address;
import peernet.transport.NetworkMessage;

public class ShuffleMsg extends NetworkMessage {

	private List<AgedPeer> sample;
	
	public ShuffleMsg(int protoId, short type, Address sender, Address destination, List<AgedPeer> sample) {
		super(protoId, type, sender, destination);
		this.sample = sample;
	}

	public int getSampleSize() {
		return this.sample.size();
	}
	
	public Iterator<AgedPeer> getSampleIterator() {
		return this.sample.iterator();
	}
}
