package nova.protocols.overlay.cyclon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nova.protocols.overlay.ContactBasedInitilizable;
import nova.protocols.overlay.cyclon.messages.JoinMessage;
import nova.protocols.overlay.cyclon.messages.JoinRandomWalk;
import nova.protocols.overlay.cyclon.messages.ShuffleMsg;
import nova.protocols.overlay.utils.AgedPeer;
import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Linkable;
import peernet.core.Peer;
import peernet.core.Protocol;
import peernet.transport.Address;
import peernet.transport.NetworkMessage;
import peernet.util.RandPermutation;

public class Cyclon extends Protocol implements Linkable, ContactBasedInitilizable {

	static final short SHUFFLE_MSG = 0;
	static final short SHUFFLE_REPLY_MSG = 1;
	static final short JOIN_MSG = 2;
	static final short JOIN_RWALK = 3;
	static final short JOIN_REPLY_MSG = 4;

	static final String VIEWSIZE = "viewsize";
	private static int viewSize;
	static final String SAMPLE = "samplesize";
	private static int sampleSize;
	static final String JOINTTL = "jointtl";
	private static int jointtl;

	private List<AgedPeer> neighbors;
	private List<AgedPeer> lastSampleSent;

	public Cyclon(String prefix) {
		super(prefix);
		viewSize = Configuration.getInt(prefix + "." + VIEWSIZE);
		sampleSize = Configuration.getInt(prefix + "." + SAMPLE);
		jointtl = Configuration.getInt(prefix + "." + JOINTTL);
		this.neighbors = new ArrayList<AgedPeer>(viewSize);
	}

	public Object clone() {
		Cyclon c = (Cyclon) super.clone();
		c.neighbors = new ArrayList<AgedPeer>(viewSize);
		return c;
	}
	
	@Override
	public void onKill() {
		this.neighbors = null;
	}

	@Override
	public int degree() {
		return this.neighbors.size();
	}

	@Override
	public Peer getNeighbor(int i) {
		return this.neighbors.get(i);
	}

	@Override
	public boolean addNeighbor(Peer neighbor) {
		int index = find(neighbor);
		if(index == -1) {
			if(! (neighbor instanceof AgedPeer) )
				this.neighbors.add(new AgedPeer(neighbor));
			else
				this.neighbors.add((AgedPeer) neighbor);
			return true;
		} else {
			if ( neighbor instanceof AgedPeer && this.neighbors.get(index).getAge() > ((AgedPeer) neighbor).getAge() ) {
				this.neighbors.remove(index);
				this.neighbors.add(index, (AgedPeer) neighbor);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(Peer neighbor) {
		return find(neighbor) != -1;
	}

	public int find(Peer neighbor) {
		int i;
		for(i = 0; i < neighbors.size(); i++) {
			if(neighbors.get(i).equals(neighbor))
				break;
		}
		return (i < neighbors.size() ? i : -1);
	}

	@Override
	public void nextCycle(int schedId) {	
		if (this.neighbors.size() == 0) {
			return; //we can's shuffle when we are alone
		}

		int sampleToBeSent = sampleSize > this.neighbors.size() ? this.neighbors.size() : sampleSize;
		this.lastSampleSent = new ArrayList<AgedPeer>(sampleToBeSent);
		this.lastSampleSent.add(new AgedPeer(myPeer()));
		AgedPeer oldest = increaseAgeAndRemoveOldestPeer();
		RandPermutation r = new RandPermutation(CommonState.r);
		r.reset(this.neighbors.size());
		while(this.lastSampleSent.size() < sampleToBeSent) {
			this.lastSampleSent.add(this.neighbors.get(r.next()));
		}
		send(oldest.address, myPid(), new ShuffleMsg(myPid(), SHUFFLE_MSG, myPeer().address, oldest.address, this.lastSampleSent));
	}

	private AgedPeer increaseAgeAndRemoveOldestPeer() {
		int indexOldest = -1;
		int ageOldest = -1;
		for(int i = 0; i < neighbors.size(); i++) {
			AgedPeer p = neighbors.get(i);
			p.increaseAge();
			if(p.getAge() > ageOldest) {
				ageOldest = p.getAge();
				indexOldest = i;
			}
		}
		return neighbors.remove(indexOldest);
	}

	private void MergeProcedure(Iterator<AgedPeer> remoteSample, int remoteSampleSize, Iterator<AgedPeer> sentSample, int sentSampleSize, RandPermutation r) {
		r.setPermutation(this.neighbors.size());
		int index = -1;
		while(remoteSample.hasNext()) {
			AgedPeer p = remoteSample.next();
			if(myPeer().equals(p)) continue; //we will not add ourselves
			index = find(p);
			if(index != -1) {
				if(p.getAge() < this.neighbors.get(index).getAge()) this.neighbors.get(index).setAge(p.getAge());
			} else {
				if(this.neighbors.size() == viewSize) {
					boolean removed = false;
					while(!removed && remoteSample.hasNext()) {
						AgedPeer t = remoteSample.next();
						index = find(t);
						if(index != -1) {
							removed = true;
							this.neighbors.remove(index);	
						}
					}
					if(!removed) this.neighbors.remove(r.next());
				}
					this.neighbors.add(p);
			}
		}
	}

	@Override
	public void processEvent(Address src, Object event) {
		RandPermutation r = new RandPermutation(CommonState.r);
		r.reset(this.neighbors.size());

		NetworkMessage m = (NetworkMessage) event;

		switch(m.getType()) {
		case SHUFFLE_MSG:
		case SHUFFLE_REPLY_MSG:
			ShuffleMsg msg = (ShuffleMsg) m;
			if(msg.getType() == SHUFFLE_MSG) {
				int sampleToBeSend = this.neighbors.size() < sampleSize ? this.neighbors.size() : sampleSize;
				List<AgedPeer> replySample = new ArrayList<AgedPeer>();

				while(replySample.size() < sampleToBeSend)
					replySample.add(this.neighbors.get(r.next()));
				send(msg.getSender(), myPid(), new ShuffleMsg(myPid(), SHUFFLE_REPLY_MSG, myPeer().address, msg.getSender(), replySample));
			} else if (msg.getType() == SHUFFLE_REPLY_MSG) {
				if(this.lastSampleSent != null)
					this.MergeProcedure(msg.getSampleIterator(), msg.getSampleSize(), this.lastSampleSent.iterator(), this.lastSampleSent.size(), r);
				else
					this.MergeProcedure(msg.getSampleIterator(), msg.getSampleSize(), Collections.emptyIterator(), 0, r);		
			}
			break;
		case JOIN_MSG:
		case JOIN_REPLY_MSG:
			JoinMessage jm = (JoinMessage) m;
			if(m.getType() == JOIN_MSG) {
				if(neighbors.size() > 0) {
					int randomwalks = 0;
					for(int i = 0; i < neighbors.size(); i++) {
						send(neighbors.get(i).address, myPid(), new JoinRandomWalk(myPid(), JOIN_RWALK, myPeer().address, neighbors.get(i).address, jm.getPeer().clone(), (short) jointtl));
						randomwalks++;
					}
					for(; randomwalks < viewSize; randomwalks++) {
						AgedPeer t = this.neighbors.get(CommonState.r.nextInt(this.neighbors.size()));
						send(t.address, myPid(), new JoinRandomWalk(myPid(), JOIN_RWALK, myPeer().address, t.address, jm.getPeer().clone(), (short) jointtl));
					}
				} else {
					this.neighbors.add(jm.getPeer());
				}
			} else if (jm.getType() == JOIN_REPLY_MSG) {
				if(find(jm.getPeer()) == -1 && !(myPeer().equals(jm.getPeer()))) {
					if(this.neighbors.size() == viewSize) {
						this.neighbors.remove(r.next());
					}
					this.neighbors.add(jm.getPeer());
				}
			}
			break;
		case JOIN_RWALK:
			JoinRandomWalk jrw = (JoinRandomWalk) m;
			int ttl = jrw.decrementTTL();
			boolean found = false;
			AgedPeer t = null;

			if(ttl > 0 && this.neighbors.size() > 0) {	
				while(!found && r.hasNext()) {
					t = this.neighbors.get(r.next());
					if(!t.equals(jrw.getPeer()) && !t.address.equals(jrw.getSender()))
						found = true;
				}
			}

			if(found) {
				jrw.setSender(myPeer().address);
				jrw.setDestination(t.address);
				send(t.address, myPid(), jrw);
			} else if(find(jrw.getPeer()) == -1 && !(myPeer().equals(jrw.getPeer()))){
				if(this.neighbors.size() > 0) {
					AgedPeer removed = this.neighbors.remove(CommonState.r.nextInt(this.neighbors.size()));
					send(jrw.getPeer().address, myPid(), new JoinMessage(myPid(), JOIN_REPLY_MSG, myPeer().address, jrw.getPeer().address, removed));
				} 

				this.neighbors.add(jrw.getPeer());
			}
			break;
		default:
			System.err.println("Invalid Message type: " + m.getType());
		}
	}


	/**
	 * Interface ContactBasedInitilizable
	 */
	@Override
	public void triggerJoinMechanism(Peer contact) {
		send(contact.address, myPid(), new JoinMessage(myPid(), JOIN_MSG, myPeer().address, contact.address, new AgedPeer(myPeer())));
	}

}
