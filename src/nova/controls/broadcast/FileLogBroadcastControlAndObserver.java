package nova.controls.broadcast;

import nova.controls.broadcast.interfaces.BroadcastCapable;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.core.Network;
import peernet.core.Node;
import peernet.util.RandPermutation;

public class FileLogBroadcastControlAndObserver implements Control {

	static final String NODES_TO_ACTIVATE = "activation";
	
	private int broadcastProtoId;
	private double fractionNodesToActivate;
	private RandPermutation r;
	
	public FileLogBroadcastControlAndObserver(String prefix) {
		this.broadcastProtoId = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		this.fractionNodesToActivate = Configuration.getDouble(prefix + "." + NODES_TO_ACTIVATE);
		this.r = new RandPermutation(CommonState.r);
	}

	@Override
	public boolean execute() {		
		for(int i = 0; i < Network.size(); i++) {
			Node n = Network.get(i);
			BroadcastCapable b = (BroadcastCapable) n.getProtocol(broadcastProtoId);
			b.resetMetrics(); //Remove reception metrics from node
		}
		
		//Initiate broadcast on nodes.
		this.r.reset(Network.size());

		int broadcasters = (int) Math.ceil(Network.size() * this.fractionNodesToActivate);
		
		System.err.println(CommonState.getTime() + ": " + this.getClass().getCanonicalName() + " triggering " + broadcasters + " message broadcasts concurrently.");
		
		for(int i = 0; i < broadcasters; i++) {
			((BroadcastCapable) Network.get(r.next()).getProtocol(this.broadcastProtoId)).triggerBCast();
		}
		return false;
	}

}
