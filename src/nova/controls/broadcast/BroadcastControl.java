package nova.controls.broadcast;

import nova.controls.broadcast.interfaces.BroadcastCapable;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.util.RandPermutation;

public class BroadcastControl implements Control {

	/**
	   * The protocol to operate on.
	   * 
	   * @config
	   */
	static final String NODES_TO_ACTIVATE = "activation";
	
	private int protocolID;
	private double fractionNodesToActivate;
	private RandPermutation r;
	
	public BroadcastControl(String prefix) {
		this.protocolID = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		this.fractionNodesToActivate = Configuration.getDouble(prefix + "." + NODES_TO_ACTIVATE);
		this.r = new RandPermutation(CommonState.r);
	}
	
	@Override
	public boolean execute() {
		this.r.reset(Network.size());

		int broadcasters = (int) Math.ceil(Network.size() * this.fractionNodesToActivate);
		
		System.err.println(CommonState.getTime() + ": " + this.getClass().getCanonicalName() + "triggering " + broadcasters + " message broadcasts concurrently.");
		
		for(int i = 0; i < broadcasters; i++) {
			((BroadcastCapable) Network.get(r.next()).getProtocol(this.protocolID)).triggerBCast();
		}
		
		return false;
	}

}
