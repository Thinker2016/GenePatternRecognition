package bean;

/**
 * @author Wenzhao
 *
 */
public class GenePosInfo {

	private int refStart;
	private int refEnd;
	private int tarStart;
	private int tarEnd;

	public int getRefStart() {
		return refStart;
	}

	public void setRefStart(int refStart) {
		this.refStart = refStart;
	}

	public int getRefEnd() {
		return refEnd;
	}

	public void setRefEnd(int refEnd) {
		this.refEnd = refEnd;
	}

	public int getTarStart() {
		return tarStart;
	}

	public void setTarStart(int tarStart) {
		this.tarStart = tarStart;
	}

	public int getTarEnd() {
		return tarEnd;
	}

	public void setTarEnd(int tarEnd) {
		this.tarEnd = tarEnd;
	}

	public GenePosInfo(int refStart, int refEnd, int tarStart, int tarEnd) {
		this.refStart = refStart;
		this.refEnd = refEnd;
		this.tarStart = tarStart;
		this.tarEnd = tarEnd;
	}

	@Override
	public String toString() {
		return refStart + " " + refEnd + " " + tarStart + " " + tarEnd + "\n";
	}
}