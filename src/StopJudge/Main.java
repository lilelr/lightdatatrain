package StopJudge;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;



public class Main {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	/**
	 *      * 根据源数据lightdata生成中间处理结果lightData_mid
	 main2()  计算停车行为
	 * @param args
	 */
	public static void main(String[] args)
	{

		main2();
	}

    /**
     * 根据源数据lightdata生成中间处理结果lightData_mid
     */
	public static void main2()// jisuan honglvdeng fujing de tingche xingwei
	{
		String input = Constant.TrafficLightDataBak;
		String midpath = Constant.TrafficLightMidPath;
		String mid_stoppath = Constant.TrafficLightMidStopPath;
		File outfile = new File(midpath);
		Set<String> dateSet = getDate(outfile);
		for (String s : dateSet) System.out.println(s);
		File infile = new File(input);
		File[] files = infile.listFiles();
		String[] filename = infile.list();
		for (int ic = 0; ic < filename.length; ic++)
		{
			Bus.zipprocess( files[ic], midpath,mid_stoppath, dateSet);
		}	
	}
	

	public static Set<String> getDays(String path)
	{
		TreeMap<String, File> st = new TreeMap<String, File>();
		File fin = new File(path);
		File[] fs = fin.listFiles();
		for (File f : fs)
		{
			String s = f.getName();
			if (!s.endsWith(".zip")) continue;
			st.put(s.substring(s.indexOf('_') + 1, s.indexOf('.')), f);
		}
		String[] sts = st.keySet().toArray(new String[0]);
		for (String s : sts)
		{
			if (st.size() <= 90) break;
			File f = st.remove(s);
			if (f.isFile()) f.delete();
		}
		return st.keySet();
	}
	
	static Set<String> getDate(File outfile)
	{
		if (outfile == null) return new HashSet<String>();
		if (!outfile.exists()) outfile.mkdirs();
		File[] fs = outfile.listFiles();
		TreeMap<String, File> set = new TreeMap<String, File>();
		for (File f : fs)
		{
			String s = f.getName();
			set.put(s, f);
		}
		String[] keys = set.keySet().toArray(new String[0]);
		for (String s : keys)
		{
			if (set.size() <= 90) break;
		}
		return set.keySet();
	}
	
}
