package bean;

/**
 * 基因序列信息
 *
 * @author Wenzhao
 *
 */
public class GeneStr {

	private String content;
	private int start;
	private int end;

	public GeneStr() {
		// TODO Auto-generated constructor stub
	}

	public GeneStr(String content, int start, int end) {
		this.content = content;
		this.start = start;
		this.end = end;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}
