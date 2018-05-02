package org.binas.station.ws;

/**
 * Class Tag that represents a Quorum Consensus tag.
 *
 */
public class Tag {
	private int seq;
	private int cid;

	/** Create a Tag with arguments given. */
	public Tag(int seq, int cid) {
		this.seq = seq;
		this.cid = cid;
	}

	/** Retrieve sequence number of tag. */
	public int getSeq() { return this.seq; }
	/** Retrieve the client identifier of tag. */
	public int getCid() { return this.cid; }
	/** Set new values of seq and cid. */
	public void setNewValues(int seq, int cid) {
		if (seq < this.seq) {
			System.out.println("The tag sequence number trying to be set is lower than the current one.");
			return;
		}
		this.seq = seq;
		this.cid = cid;
	}
}