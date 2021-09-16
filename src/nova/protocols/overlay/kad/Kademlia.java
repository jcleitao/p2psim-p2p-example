package nova.protocols.overlay.kad;

import java.util.Arrays;
import java.util.Comparator;

import nova.controls.overlay.DhtIdInitializable;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Node;
import peernet.core.Peer;
import peernet.core.Protocol;
import peernet.transport.Address;

public class Kademlia extends Protocol implements DhtIdInitializable {

	public final static String PAR_ALPHA = "a";
	public final static String PAR_BETA = "b";
	public final static String PAR_K = "k";
	public final static String PAR_N_BUCKETS = "nbuckets";
	public final static String PAR_REFRESH_TIME = "refresh";
	public final static String PAR_MSG_TIMEOUT = "timeout";
	public final static String PAR_ID_BYTE_SIZE = "idbytesize";
	public final static String PAR_USE_REPLACEMENT_CACHE = "replacementcache";
	
	//Node identifier
	byte[] dhtID;
	
	//Parameters
	static private int alpha;
	static private int beta;
	static private int k;
	static private int n_buckets;
	static private int refresh_time;
	static private int msg_timeout;
	static private int id_byte_size;
	static private boolean use_replacement_cache;
	
	//Protocol state
	private Bucket[] buckets;
	private int bucketCount;
	private KadmeliaPeer[] replacementCache;
	
	public Kademlia(String prefix) {
		super(prefix);
		Kademlia.alpha = Configuration.getInt(prefix + "." + PAR_ALPHA);
		Kademlia.beta = Configuration.getInt(prefix + "." + PAR_BETA);
		Kademlia.k = Configuration.getInt(prefix + "." + PAR_K);
		Kademlia.n_buckets = Configuration.getInt(prefix + "." + PAR_N_BUCKETS);
		Kademlia.refresh_time = Configuration.getInt(prefix + "." + PAR_REFRESH_TIME);
		Kademlia.msg_timeout = Configuration.getInt(prefix + "." + PAR_MSG_TIMEOUT);
		Kademlia.id_byte_size = Configuration.getInt(prefix + "." + PAR_ID_BYTE_SIZE, 20);
		Kademlia.use_replacement_cache = Configuration.getBoolean(prefix + "." + PAR_USE_REPLACEMENT_CACHE, true);
		CommonState.r.nextBytes(new byte[Kademlia.id_byte_size]);
		this.buckets = new Bucket[Kademlia.n_buckets];
		this.bucketCount = 0;
		if(use_replacement_cache) {
			this.replacementCache = new KadmeliaPeer[Kademlia.k * 2];
		}
	}

	public Kademlia clone() {
		Kademlia n = (Kademlia) super.clone();
		CommonState.r.nextBytes(n.dhtID);
		n.buckets = new Bucket[Kademlia.n_buckets];
		n.bucketCount = 0;
		if(use_replacement_cache) {
			n.replacementCache = new KadmeliaPeer[Kademlia.k * 2];
		}
		return n;
	}

	@Override
	public void setDhtID(byte[] id) {
		System.arraycopy(id, 0, this.dhtID, 0, Kademlia.id_byte_size);
	}
	
	@Override
	public void nextCycle(int schedId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processEvent(Address src, Object event) {
		// TODO Auto-generated method stub

	}

	//Local classes that materialize the routing table
	public class KadmeliaPeer extends Peer implements Comparable<KadmeliaPeer>{
		
		/**
		 * Generated serialVersionUID
		 */
		private static final long serialVersionUID = 1438375691531426274L;
		
		public byte[] dhtID;
		public long timestamp;
		
		public KadmeliaPeer(Node node, int pid, byte[] dhtid) {
			super(node, pid);
			this.dhtID = dhtid;
			this.timestamp = CommonState.getTime();
		}

		public KadmeliaPeer(Address addr, long id, byte[] dhtid) {
			super(addr, id);
			this.dhtID = dhtid;
			this.timestamp = CommonState.getTime();
		}
		
		public KadmeliaPeer(Peer p, byte[] dhtid) {
			super(p.address, p.ID);
			this.dhtID = dhtid;
			this.timestamp = CommonState.getTime();
		}
		
		public KadmeliaPeer(Node node, int pid, byte[] dhtid, long ts) {
			super(node, pid);
			this.dhtID = dhtid;
			this.timestamp = ts;
		}

		public KadmeliaPeer(Address addr, long id, byte[] dhtid, long ts) {
			super(addr, id);
			this.dhtID = dhtid;
			this.timestamp = ts;
		}
		
		public KadmeliaPeer(Peer p, byte[] dhtid, long ts) {
			super(p.address, p.ID);
			this.dhtID = dhtid;
			this.timestamp = ts;
		}

		@Override
		public int compareTo(KadmeliaPeer arg0) {
			return Arrays.compare(this.dhtID, arg0.dhtID);
		}
		
		public class TimestampComparable implements Comparator<KadmeliaPeer> {

			@Override
			public int compare(KadmeliaPeer arg0, KadmeliaPeer arg1) {
				return Long.compare(arg0.timestamp, arg1.timestamp);
			}

		}
		
		public boolean equals(Object o) {
			return o instanceof KadmeliaPeer && this.ID == ((KadmeliaPeer) o).ID;
		}
	}
	
	protected class Bucket {
		
		public KadmeliaPeer[] lruPeers;
		public int occupancy;
		
		protected Bucket() {
			this.lruPeers = new KadmeliaPeer[Kademlia.k];
			for(int i = 0; i < lruPeers.length; i++) {
				this.lruPeers[i] = null;
			}
			this.occupancy = 0;
		}
		
		public KadmeliaPeer addToBucket(KadmeliaPeer p) {
			if(occupancy < lruPeers.length) {
				this.lruPeers[occupancy++] = p;
				return null;
			}
			return p;
		}
	}

}
