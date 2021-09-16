package nova.protocols.overlay.observers;

import java.util.HashMap;
import java.util.Map;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Linkable;
import peernet.core.Network;
import peernet.core.Node;
import peernet.reports.FileObserver;
import peernet.util.IncrementalFreq;

public class InDegreeObserver extends FileObserver {

	private int linkableProtoId;

	public InDegreeObserver(String prefix) {
		super(prefix);
		this.linkableProtoId = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}
	
	@Override
	public boolean execute() {
		System.err.println(CommonState.getTime() + ": " + this.getClass().getName() + " extracting in-degree distribution statistics.");

		startObservation();
		
		Map<Node,Integer> indegree = new HashMap<Node,Integer>();
		
		for(int i = 0; i < Network.size(); i++) {
			Node n = Network.get(i);
			if(!n.isUp()) continue;
			
			Linkable l = (Linkable) n.getProtocol(linkableProtoId);
			for(int j = 0; j < l.degree(); j++) {
				Node n2 = Network.getByID(l.getNeighbor(j).getID());
				if(n2 == null || !n2.isUp()) continue;
				if(indegree.containsKey(n2)) {
					indegree.put(n2, indegree.get(n2) + 1);
				} else {
					indegree.put(n2, 1);
				}
			}
		}
		
		IncrementalFreq stats = new IncrementalFreq();
		for(Node n: indegree.keySet()) {
			stats.add(indegree.get(n));
		}
		
		output(stats);
		
		stopObservation();	
		return false;
	}

}
