package nova.controls.overlay;

import nova.protocols.overlay.ContactBasedInitilizable;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Peer;

public class OverlayJoinControl implements Control {

	enum SELECTOR {SINGLE, RANDOM, LINE};
	
	static final String NODES_TO_ACTIVATE = "nodes";
	static final String CONTACT_SELECTION = "selector";
	
	private int protocolID;
	private int nodes;
	private SELECTOR selector;
	
	private int activated;
	
	public OverlayJoinControl(String prefix) {
		this.protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		this.nodes = Configuration.getInt(prefix + "." + NODES_TO_ACTIVATE, 1);
		String selectorType = Configuration.getString(prefix + "." + CONTACT_SELECTION, "single");
		switch (selectorType) {
		case "single":
			this.selector = SELECTOR.SINGLE;
			break;
		case "random": 
			this.selector = SELECTOR.RANDOM;
			break;
		case "line":
			this.selector = SELECTOR.LINE;
			break;
			default:
				System.err.println("Invalid selector type: " + selectorType);
		}
		this.activated = 1; //we do not activate the first node in the network ever :)
	}

	@Override
	public boolean execute() {
		int target = activated + nodes;
		Peer contact = null;
		if(selector == SELECTOR.SINGLE)
			contact = new Peer(Network.getByID(0), 0);
		for(; activated < target; activated++) {
			if(selector == SELECTOR.RANDOM) 
				contact = new Peer(Network.getByID(CommonState.r.nextInt(activated)), 0);
			else if(selector == SELECTOR.LINE) 
				contact = new Peer(Network.getByID(activated-1), 0);
			((ContactBasedInitilizable) Network.getByID(activated).getProtocol(protocolID)).triggerJoinMechanism(contact);
		}
		return false;
	}

}
