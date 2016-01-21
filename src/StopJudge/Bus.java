package StopJudge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;

/**
 * 一个bus实例代表一辆公交车的数据集
 */
public class Bus {
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
	private String busID;  //
	private ArrayList<Date> timeStamps;
	private ArrayList<Double> diss; //到路口距离的集合
	private ArrayList<Double> disToStops;
	
	public Bus(String s) 
	{
		this.timeStamps = new ArrayList<Date>();
		this.diss = new ArrayList<Double>();
		this.disToStops = new ArrayList<Double>();
		
		String[] ss = s.split(",");
		double dis = 0;
		double disToStop = 0;
		char lightSE = ss[6].charAt(0);
		this.busID = ss[2];
		Date timeStamp = null;
		try {
			timeStamp = sdf.parse(ss[5]);
			if (lightSE == 'S') dis = Double.valueOf(ss[7]); //S表示朝红绿灯正向
			else dis = -Double.valueOf(ss[7]);  //距离为负，已过了红绿灯
			disToStop = Double.valueOf(ss[9]);  //到下一站距离
		} catch (ParseException e) {
			return;
		}
		
		if (lightSE == 'S') dis = Double.valueOf(ss[7]);
		else dis = -Double.valueOf(ss[7]);
		disToStop = Double.valueOf(ss[9]);
		if (dis < disToStop)
		{
		diss.add(dis);
		disToStops.add(disToStop);
		timeStamps.add(timeStamp);
		}
	}
	
	public boolean add(String s)
	{
		String[] ss = s.split(",");
		char lightSE = ss[6].charAt(0);
		String curBusID = ss[2];
		Date timeStamp = null;
		try {
			timeStamp = sdf.parse(ss[5]);
		} catch (ParseException e) {
			return true;
		}
		double dis = 0;
		if (lightSE == 'S') dis = Double.valueOf(ss[7]);
		else dis = -Double.valueOf(ss[7]);
		////////////////////////TODO/////////////////////
//		if(dis<0)
//			return true;
		/////////////
		double disToStop = Double.valueOf(ss[9]);
		if (!curBusID.equals(this.busID)) return false;
		if (this.timeStamps.size() > 0 &&
				Math.abs(this.timeStamps.get(this.timeStamps.size() - 1).getTime() 
						- timeStamp.getTime()) > 5 * 60 * 1000) 
			return false;
		if (dis < disToStop)
		{
		diss.add(dis);
		disToStops.add(disToStop);
		timeStamps.add(timeStamp);
		}
		return true;
	}
		
	
	public ArrayList<String> stopJudge1() //停车算法
	{
		if(disToStops.size()==0)
			return new ArrayList<String>();
		int[] stopJudge1 = new int[disToStops.size()];

		for (int i = 0; i < disToStops.size(); i++)
		{
			if (i > 0  && stopJudge1[i - 1] > 0)
				if (disToStops.get(stopJudge1[i - 1] - 1) - disToStops.get(i) <= 30) stopJudge1[i] = stopJudge1[i - 1];
			if (stopJudge1[i] == 0 && i < disToStops.size() - 1)
			{
				int k = i + 1;
				while (k < stopJudge1.length - 1 && timeStamps.get(k).getTime() - timeStamps.get(i).getTime() < 5 * 1000) k++;
				if (timeStamps.get(k).getTime() - timeStamps.get(i).getTime() >= 10 * 1000)
				{
					if (((disToStops.get(i) - disToStops.get(k))<= 30) &&( disToStops.get(i) - disToStops.get(k) < 1.5 * ( timeStamps.get(k).getTime()-timeStamps.get(i).getTime()) / 1000))
					//if (disToStops.get(i) - disToStops.get(k) <= 30 )
					{	
						//for (int j = k; j <= i; j++) 
						stopJudge1[i] = i + 1;
					}
				}
				else if (timeStamps.get(k).getTime() - timeStamps.get(i).getTime() >= 5 * 1000)
				{
					if (disToStops.get(i) - disToStops.get(k) < 0.1)
//						for (int j = i; j <= k; j++) 
						stopJudge1[i] = i + 1;
				}
			}
		}

		int pres = -1, pret = 0;
		ArrayList<Integer> rs = new ArrayList<Integer>();
		ArrayList<Integer> rt = new ArrayList<Integer>();
		for (int i = 0; i < stopJudge1.length; i++)
		{
			if (i > 0 && stopJudge1[i] != stopJudge1[i - 1])
			{
				if (stopJudge1[i - 1] > 0 &&pres>=0) 
				{
					rs.add(pres);
					rt.add(pret);
				}
				pres = -1; pret = 0;
			}
			if (pres < 0) pres = i;
			pret = i;
		}
		if (pres >= 0 && stopJudge1[stopJudge1.length - 1] > 0)
		{
			rs.add(pres);
			rt.add(pret);
		}
		int stopTimes = 0;
		ArrayList<String> res = new ArrayList<String>();
		for (int i = rs.size() - 1; i >= 0; i--)
		{
			int ss = rs.get(i);
			int ts = rt.get(i);
			if (ss > 0 && timeStamps.get(ss).getTime() - timeStamps.get(ss - 1).getTime() > 35 * 1000) continue;
			if (ts < timeStamps.size() - 1 && timeStamps.get(ts + 1).getTime() - timeStamps.get(ts).getTime() > 35 * 1000) continue;
			double disAvg = 0;
			for (int j = ss; j <= ts; j++) disAvg += diss.get(j);
			disAvg /= (1 + ts - ss);
			if (disAvg < 0) continue;
			String timeStr = sdf.format(timeStamps.get(ss));
			double time = (timeStamps.get(ts).getTime() - timeStamps.get(ss).getTime()) / 1000;
			res.add(timeStr + "," + time + "," + disAvg + "," + busID + "," + stopTimes);
            //中间结果每行数据 lightID,时间戳，通过红绿灯时间，离路口距离，公交车ID,停车次数
			stopTimes++;
		}
		return res;
	}

	
	public ArrayList<String> stopJudge()
	{
		int[] stopJudge = new int[disToStops.size()];
		int[] stopJudge1 = new int[disToStops.size()];
		
		for (int i = disToStops.size() - 1; i >= 0; i--)
		{
			if (i + 1 < stopJudge.length && stopJudge[i + 1] > 0)
				if (disToStops.get(i) - disToStops.get(stopJudge[i + 1]) <= 35) stopJudge[i] = stopJudge[i + 1];
			if (stopJudge[i] == 0 && i > 0)
			{
				int k = i - 1;
				while (k > 0 && timeStamps.get(i).getTime() - timeStamps.get(k).getTime() < 5 * 1000) k--;
				if (timeStamps.get(i).getTime() - timeStamps.get(k).getTime() >= 10 * 1000)
				{
					if (disToStops.get(k) - disToStops.get(i) <= 35 && disToStops.get(k) - disToStops.get(i) < 1.5 * (timeStamps.get(i).getTime() - timeStamps.get(k).getTime()) / 1000)
						for (int j = k; j <= i; j++) stopJudge[j] = i;
				}
				else if (timeStamps.get(i).getTime() - timeStamps.get(k).getTime() >= 5 * 1000)
				{
					if (disToStops.get(k) - disToStops.get(i) < 0.1)
						for (int j = k; j <= i; j++) stopJudge[j] = i;
				}
			}
		}
		for (int i = 0; i < disToStops.size(); i++)
		{
			if (i > 0  && stopJudge1[i - 1] > 0)
				if (disToStops.get(stopJudge1[i - 1] - 1) - disToStops.get(i) <= 35) stopJudge1[i] = stopJudge1[i - 1];
			if (stopJudge1[i] == 0 && i < disToStops.size() - 1)
			{
				int k = i + 1;
				while (k < stopJudge1.length - 1 && timeStamps.get(k).getTime() - timeStamps.get(i).getTime() < 5 * 1000) k++;
				if (timeStamps.get(k).getTime() - timeStamps.get(i).getTime() >= 10 * 1000)
				{
					if (disToStops.get(i) - disToStops.get(k) <= 35 && disToStops.get(i) - disToStops.get(k) < 1.5 * (timeStamps.get(i).getTime() - timeStamps.get(k).getTime()) / 1000)
						for (int j = k; j <= i; j++) stopJudge1[j] = i + 1;
				}
				else if (timeStamps.get(k).getTime() - timeStamps.get(i).getTime() >= 5 * 1000)
				{
					if (disToStops.get(i) - disToStops.get(k) < 0.1)
						for (int j = i; j <= k; j++) stopJudge1[j] = i + 1;
				}
			}

		}
		int pres = 0, pret = 0;
		ArrayList<Integer> rs = new ArrayList<Integer>();
		ArrayList<Integer> rt = new ArrayList<Integer>();
		for (int i = 0; i < stopJudge.length; i++)
			if (stopJudge[i] > 0 && stopJudge1[i] > 0 && stopJudge[i] > pret && stopJudge1[i] > pres)
			{
				rs.add(stopJudge1[i] - 1);
				rt.add(stopJudge[i]);
				pret = stopJudge[i];
				pres = stopJudge1[i] - 1;
			}
		int stopTimes = 0;
		ArrayList<String> res = new ArrayList<String>();
		for (int i = rs.size() - 1; i >= 0; i--)
		{
			int ss = rs.get(i);
			int ts = rt.get(i);
			if (ss > 0 && timeStamps.get(ss).getTime() - timeStamps.get(ss - 1).getTime() > 35 * 1000) continue;
			if (ts < timeStamps.size() - 1 && timeStamps.get(ts + 1).getTime() - timeStamps.get(ts).getTime() > 35 * 1000) continue;
			double disAvg = 0;
			for (int j = ss; j <= ts; j++) disAvg += diss.get(j);
			disAvg /= (1 + ts - ss);
			String timeStr = sdf.format(timeStamps.get(ss));
			double time = (timeStamps.get(ts).getTime() - timeStamps.get(ss).getTime()) / 1000;
			res.add(timeStr + "," + time + "," + disAvg + "," + busID + "," + stopTimes);
			stopTimes++;
		}
		return res;
	}

	/**
	 *  进一步处理treemap中的数据
	 * @param fw  fw 为数据文件读写指针
	 * @param ts 有序key的treemap key 为公交车id+时间戳，value为为每一行数据ls,按key排序
	 * @param lightID
	 */
	public static void process(FileWriter fw, TreeMap<String, String> ts, String lightID)
	{
		Bus bus = null;
		for (String skey : ts.keySet())
		{
			String s = ts.get(skey); //获取每一行数据
			if (bus == null) bus = new Bus(s);
			else 
			{
				if (!bus.add(s))
				{
					ArrayList<String> stopDetail = bus.stopJudge1();
					for (String stop : stopDetail)
						try {
							fw.write(lightID + "," + stop + "\n");
						} catch (IOException e) {
							continue;
						}
					bus = new Bus(s);  //初始化每一个bus实例
				}
			}
		}
		if (bus != null)
		{
			ArrayList<String> stopDetail = bus.stopJudge1();
			for (String stop : stopDetail)
				try {
					fw.write(lightID + "," + stop + "\n");
				} catch (IOException e) {
					continue;
				}
			bus = null;
		}
		
	}


	/**
	 * 首先读取zip压缩文件中的数据
	 * @param zf
	 * @param outpath
	 * @param dateSet
	 */

	public static void process(File zf, String outpath, Set<String> dateSet)  //解压读取数据
	{
		String filename = zf.getName();
		if (!filename.endsWith(".zip")) return;
		System.out.println(filename);
		String dateStr = filename.substring(filename.indexOf('_') + 1, filename.indexOf('.'));
		if (dateSet.contains(dateStr)) return;
		else (new File(outpath + dateStr)).mkdirs();
		FileWriter fw = null;
		ZipData zd = null;
		try {
			zd = new ZipData(zf.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		while (zd.hasNextEntry())
		{
			ZipEntry ze = zd.getNextEntry();
			if (ze.isDirectory()) continue;
			InputStream is = null;
			String lightID = null;
			try {
				is = zd.getInStream(ze);
				lightID = ze.getName();
				lightID = lightID.substring(lightID.lastIndexOf("/") + 1, lightID.indexOf('.'));
				///////////TODO///////
				try{
				if(lightID.contains("\\"))
					lightID = lightID.substring(lightID.lastIndexOf('\\') + 1, lightID.length());
//					System.out.println(lightID);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
//				if(!lightID.equals("59566303622_59566302845_59566302320"))
//					continue;
//				System.out.println(lightID);
				File fileout = new File(outpath + dateStr + "/" + lightID + ".csv"); //中间输出结果文件路径
				
				fw = new FileWriter(fileout,true);//中间输出结果文件读写指针
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String ls;
			TreeMap<String, String> ts = new TreeMap<String, String>(); //有序key的treemap
			//key 为公交车id+时间戳，value为为每一行数据ls,按key排序
			try {
				while ((ls = reader.readLine()) != null)
				{
					if (ls.contains("?")) continue;
					String[] ss = ls.split(",");
					ts.put(ss[2] + " " + ss[5], ls); //key 为公交车id+时间戳，value为为每一行数据ls
				}
			} catch (IOException e) {
			}
			
			try {
				reader.close();
				//进一步处理treemap中的数据
				process(fw, ts, lightID);
				fw.close();
			} catch (IOException e) {
				continue;
			}
		}
	}
	
	public static void main(String[] args)
	{
//		main3("D:/TrafficLightData/TrafficLightDataBACKUP/","D:/TrafficLightData/mid2/");
	}
	
	
	
	public static void main2(String input,String midpath)
	{
		File outfile = new File(midpath);
		Set<String> dateSet = Main.getDate(outfile);
		//		System.out.println(111);
		File infile = new File(input);
		File[] files = infile.listFiles();
		String[] filename = infile.list();
		HashMap<String, FileWriter> writers = new HashMap<String, FileWriter>(2500);
		for (int ic = 0; ic < filename.length; ic++)
		{
			if (!filename[ic].endsWith(".zip")) continue;
			System.out.println(filename[ic]);
			String dateStr = filename[ic].substring(filename[ic].indexOf('_') + 1, filename[ic].indexOf('.'));
			if (dateSet.contains(dateStr)) continue;
			else (new File(midpath + dateStr)).mkdirs();
			FileWriter fw = null;
			ZipData zd = null;
			try {
				zd = new ZipData(files[ic].getAbsolutePath());
			} catch (IOException e) {
				continue;
			}
			
			while (zd.hasNextEntry())
			{
				ZipEntry ze = zd.getNextEntry();
				if (ze.isDirectory()) continue;
				InputStream is = null;
				String lightID = null;
				try {
					is = zd.getInStream(ze);
					lightID = ze.getName();
					lightID = lightID.substring(lightID.indexOf('\\') + 1, lightID.indexOf('.'));
					if (!writers.containsKey(lightID)) 
					{
						fw = new FileWriter(midpath + lightID + ".csv");
						writers.put(lightID, fw);
					}
					else fw = writers.get(lightID);
				} catch (IOException e) {
					continue;
				}
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String ls;
				TreeMap<String, String> ts = new TreeMap<String, String>();
				try {
					while ((ls = reader.readLine()) != null)
					{
						if (ls.contains("?")) continue;
						String[] ss = ls.split(",");
						ts.put(ss[2] + " " + ss[5], ls); 
					}
				} catch (IOException e) {
				}
				
				try {
					reader.close();
					process(fw, ts, lightID);
					
				} catch (IOException e) {
					continue;
				}
			}
//			fw.close();
			zd.close();
		}
		for (String s : writers.keySet())
		try {
			 writers.get(s).close();
			
		} catch (IOException e) {
			continue;
		}
	}
}
