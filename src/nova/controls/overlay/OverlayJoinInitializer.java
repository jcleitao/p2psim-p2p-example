package nova.controls.overlay;

import nova.protocols.overlay.ContactBasedInitilizable;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import peernet.core.Peer;
import peernet.dynamics.NodeInitializer;

public class OverlayJoinInitializer implements NodeInitializer, Control {

	enum SELECTOR {SINGLE, RANDOM, LINE};

	static final String PAR_PROTOCOL = "protocol";
	static final String CONTACT_SELECTION = "selector";

	private int protocolID;
	private SELECTOR selector;

	public OverlayJoinInitializer(String prefix) {
		this.protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);

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
	}

	@Override
	public void initialize(Node n) {
		Peer contact = null;
		if(selector == SELECTOR.SINGLE)
			contact = new Peer(Network.get(0), 0);
		else if(selector == SELECTOR.RANDOM) 
			contact = new Peer(Network.get(CommonState.r.nextInt(Network.size())), 0);
		else if(selector == SELECTOR.LINE) 
			contact = new Peer(Network.get(Network.size()-1), 0);
		((ContactBasedInitilizable) n.getProtocol(protocolID)).triggerJoinMechanism(contact);
	}
	
	@Override
	public boolean execute() {
		Peer contact = null;
		if(selector == SELECTOR.SINGLE)
			contact = new Peer(Network.get(0), 0);
		for(int activated = 1; activated < Network.size(); activated++) {
			if(selector == SELECTOR.RANDOM) 
				contact = new Peer(Network.get(CommonState.r.nextInt(activated)), 0);
			else if(selector == SELECTOR.LINE) 
				contact = new Peer(Network.get(activated-1), 0);
			((ContactBasedInitilizable) Network.get(activated).getProtocol(protocolID)).triggerJoinMechanism(contact);
		}
		return false;
	}
}
