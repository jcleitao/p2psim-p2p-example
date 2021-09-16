package nova.protocols.overlay.utils;

import peernet.core.Node;
import peernet.core.Peer;

public class AgedPeer extends Peer implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9084313087486445226L;
	private int age;

	public AgedPeer(Node node, int pid) {
		super(node, pid);
		this.age = 0;
	}

	public AgedPeer(Peer neighbor) {
		super(neighbor.address, neighbor.ID);
		this.age = 0;
	}

	public AgedPeer clone() {
		return (AgedPeer) super.shalowClone();
	}

	public void increaseAge() {
		this.age++;
	}

	public int getAge() {
		return this.age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean equals(Object o) {
		if(!(o instanceof Peer) || this.ID != ((Peer)o).ID)
			return false;
		return true;
	}
}
