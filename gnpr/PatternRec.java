package gnpr;

import java.util.*;
import java.io.*;
import bean.*;

/**
 * @author Wenzhao
 *
 */
public class PatternRec {

	private Map<Integer, List<GenePosInfo>> genePosInfoMap;
	private File refFile;
	private File tarFile;
	private File outputFile;
	private int fragLen;
	private int subRefLen;
	private String refStr;
	private String tarStr;
	private int matchSize;

	public void calculate() {
		genePosInfoMap = new TreeMap<>();
		refStr = readFromGeneFile(refFile);
		tarStr = readFromGeneFile(tarFile);
		int len = new Double(Math.ceil((refStr.length() / new Double(subRefLen)))).intValue();
		System.out.println(len);
		matchSize = 0;
		for (int i = 0; i < 1; i++) {
			int start = i * subRefLen;
			int end = i == len - 1 ? refStr.length() : (i + 1) * subRefLen;
			String subRefStr = refStr.substring(start, end);
			HashMap<Integer, List<Fragment>> refMap = createRefMap(subRefStr, start);
			String subTarStr = tarStr.substring(start, end);
			calculateGenePosInfo(refMap, subTarStr, start);
		}
		System.out.println(matchSize);
	}

	public HashMap<Integer, List<Fragment>> createRefMap(String subRefStr, int startIndex) {
		HashMap<Integer, List<Fragment>> refMap = new HashMap<>();
		int length = subRefStr.length() - fragLen + 1;
		for (int i = 0; i < length; i++) {
			int start = i;
			int end = i == length - 1 ? subRefStr.length() : i + fragLen;
			String fragStr = subRefStr.substring(start, end);
			Fragment frag = new Fragment();
			frag.setContent(fragStr);
			frag.setStartIndex(startIndex + start);
			List<Fragment> fragList;
			int hashcode = fragStr.hashCode();
			if (refMap.containsKey(hashcode)) {
				fragList = refMap.get(hashcode);
				fragList.add(frag);
			} else {
				fragList = new ArrayList<>();
				fragList.add(frag);
				refMap.put(hashcode, fragList);
			}
		}
		return refMap;
	}

	public void calculateGenePosInfo(HashMap<Integer, List<Fragment>> refMap, String subTarStr, int startIndex) {
		int len = new Double(Math.ceil((subTarStr.length() / new Double(fragLen)))).intValue();
		for (int i = 0; i < len; i++) {
			int start = i * fragLen;
			int end = i == len - 1 ? subTarStr.length() : (i + 1) * fragLen;
			String tarFragStr = subTarStr.substring(start, end);
			int hashcode = tarFragStr.hashCode();
			List<Fragment> fragList = refMap.get(hashcode);
			if (fragList != null) {
				for (Fragment elem : fragList) {
					String fragStr = elem.getContent();
					if (fragStr.equals(tarFragStr)) {
						boolean matching = true;
						for (int k = elem.getStartIndex() + fragLen, t = start
								+ fragLen, fragEnd = k, tarFragEnd = t; matching && k < refStr.length()
										&& t < tarStr.length(); k += fragLen, t += fragLen) {
							boolean fragReachEnd = k > refStr.length() - fragLen ? true : false;
							boolean tarReachEnd = t > tarStr.length() - fragLen ? true : false;
							int nextFragEnd = fragReachEnd ? refStr.length() : k + fragLen;
							int nextTarFragEnd = tarReachEnd ? tarStr.length() : t + fragLen;
							String adFragStr = refStr.substring(k, nextFragEnd);
							String adTarFragStr = tarStr.substring(t, nextTarFragEnd);
							if (!adFragStr.equals(adTarFragStr) || fragReachEnd || tarReachEnd) {
								matching = false;
								int length = fragEnd - elem.getStartIndex();
								List<GenePosInfo> list;
								GenePosInfo info = new GenePosInfo(elem.getStartIndex(), fragEnd, startIndex + start,
										startIndex + tarFragEnd);
								if (genePosInfoMap.containsKey(length)) {
									list = genePosInfoMap.get(length);
									list.add(info);
								} else {
									list = new ArrayList<>();
									list.add(info);
									genePosInfoMap.put(length, list);
								}
								if (fragEnd - elem.getStartIndex() > 16)
									System.out.println(elem.getStartIndex() + " " + fragEnd + " " + (startIndex + start)
											+ " " + (startIndex + tarFragEnd));
								if (fragEnd - elem.getStartIndex() > matchSize)
									matchSize = fragEnd - elem.getStartIndex();
							} else {
								fragEnd = nextFragEnd;
								tarFragEnd = nextTarFragEnd;
							}
						}
					}
				}
			}
		}
	}

	public String readFromGeneFile(File file) {
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			br.readLine();
			while ((line = br.readLine()) != null) {
				buffer.append(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String str = buffer.toString().replaceAll("[^ACGTacgt]", "");
		return str;
	}

	public void writeToOutput() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			Set<Integer> set = genePosInfoMap.keySet();
			for (int elem : set) {
				bw.write("长度为" + elem + "的匹配串有：\n");
				List<GenePosInfo> genePosInfoList = genePosInfoMap.get(elem);
				for (GenePosInfo info : genePosInfoList)
					bw.write(info.toString());
				bw.write('\n');
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PatternRec(String refPath, String tarPath, String outputPath, int fragLen, int subRefLen) {
		refFile = new File(refPath);
		tarFile = new File(tarPath);
		outputFile = new File(outputPath);
		this.fragLen = fragLen;
		this.subRefLen = subRefLen;
		calculate();
		writeToOutput();
		System.out.println(genePosInfoMap.size());
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String baseDir = "/home/wenzhao/SoftwareFactory/Biology";
		String refPath = baseDir + File.separator + "ko-131-22.fa";
		String targetPath = baseDir + File.separator + "ko-224-22.fa";
		String outputPath = baseDir + File.separator + "output";
		new PatternRec(refPath, targetPath, outputPath, 8, 30000);
	}

}
