package gnpr;

import java.util.*;
import java.io.*;
import bean.*;

/**
 * 基因序列匹配算法
 *
 * @author Wenzhao
 */
public class PatternRec {

	private Map<Integer, List<GenePosInfo>> genePosInfoMap;
	private File refFile;
	private File tarFile;
	private File outputFile;
	private int fragLen;
	private int subRefLen;
	private String unknownStr;
	private int matchSize;
	private String refStr;
	private String tarStr;
	private int refStart;

	/**
	 * 计算基因序列匹配信息
	 */
	public void calculate() {
		genePosInfoMap = new TreeMap<>();
		GeneStr refGeneStr = readFromGeneFile(refFile);
		GeneStr tarGeneStr = readFromGeneFile(tarFile);
		refStr = refGeneStr.getContent();
		tarStr = tarGeneStr.getContent();
		refStart = refGeneStr.getStart();
		int len = new Double(Math.ceil((refStr.length() / new Double(subRefLen)))).intValue();
		System.out.println(len);
		matchSize = 0;
		for (int i = 0; i < len; i++) {
			int start = i * subRefLen;
			int end = i == len - 1 ? refStr.length() : (i + 1) * subRefLen;
			String subRefStr = refStr.substring(start, end);
			HashMap<Integer, List<Fragment>> refMap = createRefMap(subRefStr, refStart + start);
			String subTarStr = tarStr.substring(start, end);
			calculateGenePosInfo(refMap, subTarStr, refStart + start);
		}
		System.out.println(matchSize);
	}

	/**
	 * 为源序列的子序列建立基因片段的哈系索引
	 *
	 * @param subRefStr
	 *            源序列的子序列
	 * @param startIndex
	 *            源序列子序列的起始位置
	 * @return
	 */
	public HashMap<Integer, List<Fragment>> createRefMap(String subRefStr, int startIndex) {
		HashMap<Integer, List<Fragment>> refMap = new HashMap<>();
		int length = subRefStr.length() - fragLen + 1;
		for (int i = 0; i < length; i++) {
			int start = i;
			int end = i == length - 1 ? subRefStr.length() : i + fragLen;
			String fragStr = subRefStr.substring(start, end);
			if (fragStr.equals(unknownStr))
				continue;
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

	/**
	 * 查找目标序列子序列与源序列子序列的匹配片段
	 *
	 * @param refMap
	 *            源序列子序列的哈希索引
	 * @param subTarStr
	 *            目标序列的子序列
	 * @param startIndex
	 *            目标序列子序列的起始位置
	 */
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
							if (!adFragStr.equals(adTarFragStr) || adFragStr.equals(unknownStr) || fragReachEnd
									|| tarReachEnd) {
								matching = false;
								int length;
								List<GenePosInfo> list;
								length = nextFragEnd - elem.getStartIndex();
								int infoFragEnd, infoTarFragEnd;
								if (!adFragStr.equals(adTarFragStr) || adFragStr.equals(unknownStr)) {
									length = fragEnd - elem.getStartIndex();
									infoFragEnd = fragEnd;
									infoTarFragEnd = tarFragEnd;
								} else {
									length = nextFragEnd - elem.getStartIndex();
									infoFragEnd = nextFragEnd;
									infoTarFragEnd = nextTarFragEnd;
								}
								GenePosInfo info = new GenePosInfo(elem.getStartIndex(), infoFragEnd,
										startIndex + start, startIndex + infoTarFragEnd);
								if (genePosInfoMap.containsKey(length)) {
									list = genePosInfoMap.get(length);
									list.add(info);
								} else {
									list = new ArrayList<>();
									list.add(info);
									genePosInfoMap.put(length, list);
								}
								if (fragEnd - elem.getStartIndex() > fragLen * 2)
									System.out.println(elem.getStartIndex() + " " + infoFragEnd + " "
											+ (startIndex + start) + " " + (startIndex + infoTarFragEnd));
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

	/**
	 * 从基因序列文件中读取字符串
	 *
	 * @param file
	 *            基因序列文件
	 * @return 序列信息
	 */
	public GeneStr readFromGeneFile(File file) {
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
		// String str = buffer.toString().replaceAll("[^ACGTacgt]", "");
		String bufferStr = buffer.toString();
		char[] charArr = bufferStr.toCharArray();
		Set<Character> charSet = new HashSet<>(Arrays.asList('A', 'C', 'G', 'T', 'a', 'c', 'g', 't'));
		int start = 0, end = 0;
		for (int i = 0; i < charArr.length; i++)
			if (charSet.contains(charArr[i])) {
				start = i;
				break;
			}
		for (int i = charArr.length - 1; i > -1; i--)
			if (charSet.contains(charArr[i])) {
				end = i + 1;
				break;
			}
		String str = bufferStr.substring(start, end);
		return new GeneStr(str, start, end);
	}

	/**
	 * 将匹配信息输出至文件
	 */
	public void writeToOutput() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			Set<Integer> set = genePosInfoMap.keySet();
			Integer[] keyArr = set.toArray(new Integer[set.size()]);
			for (int i = keyArr.length - 1; i > -1; i--) {
				bw.write("长度为" + keyArr[i] + "的匹配串有：\n");
				List<GenePosInfo> genePosInfoList = genePosInfoMap.get(keyArr[i]);
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

	/**
	 * 调用程序逻辑函数
	 *
	 * @param refPath
	 *            源文件的路径
	 * @param tarPath
	 *            目标文件的路径
	 * @param outputPath
	 *            输出文件的路径
	 * @param fragLen
	 *            片段长度
	 * @param subRefLen
	 *            子序列长度
	 */
	public PatternRec(String refPath, String tarPath, String outputPath, int fragLen, int subRefLen) {
		refFile = new File(refPath);
		tarFile = new File(tarPath);
		outputFile = new File(outputPath);
		this.fragLen = fragLen;
		this.subRefLen = subRefLen;
		char[] unknown = new char[fragLen];
		for (int i = 0; i < unknown.length; i++)
			unknown[i] = 'n';
		this.unknownStr = new String(unknown);
		calculate();
		writeToOutput();
		System.out.println(genePosInfoMap.size());
	}

	/**
	 *
	 * @param args
	 *            根目录、源文件路径、目标文件路径、输出文件路径、片段长度、子序列长度
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String baseDir = args[0];
		String refPath = baseDir + File.separator + args[1];
		String targetPath = baseDir + File.separator + args[2];
		String outputPath = baseDir + File.separator + args[3];
		new PatternRec(refPath, targetPath, outputPath, Integer.parseInt(args[4]), Integer.parseInt(args[5]));
	}

}
