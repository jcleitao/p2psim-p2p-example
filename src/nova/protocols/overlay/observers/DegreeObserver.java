package nova.protocols.overlay.observers;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Linkable;
import peernet.core.Network;
import peernet.reports.FileObserver;
import peernet.util.IncrementalFreq;

public class DegreeObserver extends FileObserver {

	private int linkableProtoId;

	public DegreeObserver(String prefix) {
		super(prefix);
		this.linkableProtoId = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}
	
	@Override
	public boolean execute() {
		System.err.println(CommonState.getTime() + ": " + this.getClass().getName() + " extracting degree distribution statistics.");

		startObservation();
		IncrementalFreq stats = new IncrementalFreq();
		for(int i = 0; i < Network.size(); i++) {
			Linkable l = (Linkable) Network.get(i).getProtocol(linkableProtoId);
			stats.add(l.degree());
		}
		output(stats);
		stopObservation();	
		return false;
	}

}
