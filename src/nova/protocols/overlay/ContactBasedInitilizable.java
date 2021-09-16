package nova.protocols.overlay;

import peernet.core.Peer;

public interface ContactBasedInitilizable {

	void triggerJoinMechanism(Peer contact);
}
