package nova.controls.broadcast;

import java.util.HashMap;
import java.util.Map;

import nova.controls.broadcast.interfaces.BroadcastCapable;
import nova.controls.broadcast.interfaces.DeliveryRecord;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Network;
import peernet.core.Node;
import peernet.reports.FileObserver;
import peernet.util.IncrementalFreq;
import peernet.util.RandPermutation;

public class BroadcastControlAndObserver extends FileObserver {

	static final String NODES_TO_ACTIVATE = "activation";
	
	private int broadcastProtoId;
	private double fractionNodesToActivate;
	private RandPermutation r;
	
	public BroadcastControlAndObserver(String prefix) {
		super(prefix);
		this.broadcastProtoId = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		this.fractionNodesToActivate = Configuration.getDouble(prefix + "." + NODES_TO_ACTIVATE);
		this.r = new RandPermutation(CommonState.r);
	}

	@Override
	public boolean execute() {
		System.err.println(CommonState.getTime() + ": " + this.getClass().getName() + " extracting broadcast statistics.");
		startObservation();
		
		int aliveNodes = 0;
		Map<Long, BCastMessageStatistics> stats = new HashMap<>();
		
		for(int i = 0; i < Network.size(); i++) {
			Node n = Network.get(i);
			if(n.isUp()) aliveNodes++;
			BroadcastCapable b = (BroadcastCapable) n.getProtocol(broadcastProtoId);
			Map <Long,Long> sentMessages = b.getSentMessages();
			Map <Long,DeliveryRecord> deliveryMessages = b.getDeliveredMessages();
			for( Long msg: sentMessages.keySet() ) {
				if(!stats.containsKey(msg)) stats.put(msg, new BCastMessageStatistics());
				stats.get(msg).timeSent = sentMessages.get(msg);
			}
			for( Long msg: deliveryMessages.keySet()) {
				if(!stats.containsKey(msg)) stats.put(msg, new BCastMessageStatistics());
				BCastMessageStatistics bcms = stats.get(msg);
				DeliveryRecord dr = deliveryMessages.get(msg);
				bcms.countdelivery++;
				bcms.deliveryTimes.add((int) dr.getDeliveryTime());
				bcms.deliveryHops.add((int) dr.getDeliveryHop());
			}
			b.resetMetrics(); //Remove reception metrics from node
		}
		
		//OutputFormat
		//[observation-time,]msgid,timesent,maxlatency,averagelatency,maxhop,averagehop,nodesdelivered,reliability
		for(Long msgID: stats.keySet()) {
			BCastMessageStatistics bcms = stats.get(msgID);
			output(msgID+separator+
					bcms.timeSent+separator+
					(bcms.deliveryTimes.getMax() - bcms.timeSent)+separator+
					(bcms.deliveryTimes.getAverage() - bcms.timeSent)+separator+
					bcms.deliveryHops.getMax()+separator+
					bcms.deliveryHops.getAverage()+separator+
					bcms.countdelivery+separator+
					(((double)bcms.countdelivery) / aliveNodes * 100)
					);
		}
			
		stopObservation();
		
		//Initiate broadcast on nodes.
		this.r.reset(Network.size());

		int broadcasters = (int) Math.ceil(Network.size() * this.fractionNodesToActivate);
		
		System.err.println(CommonState.getTime() + ": " + this.getClass().getCanonicalName() + " triggering " + broadcasters + " message broadcasts concurrently.");
		
		for(int i = 0; i < broadcasters; i++) {
			((BroadcastCapable) Network.get(r.next()).getProtocol(this.broadcastProtoId)).triggerBCast();
		}
		return false;
	}

	
	private class BCastMessageStatistics {
		
		public int countdelivery;
		public long timeSent;
		public IncrementalFreq deliveryTimes;
		public IncrementalFreq deliveryHops;
		
		protected BCastMessageStatistics() {
			countdelivery = 0;
			timeSent = 0;
			deliveryTimes = new IncrementalFreq();
			deliveryHops = new IncrementalFreq();
		}
		
	}
}
