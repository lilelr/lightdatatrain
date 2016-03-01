package Accuracy;

import StopJudge.Constant;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * main3 函数
 * 	String input = Constant.TrafficLightMidPath;
 String output = Constant.TrafficLightResultPath + dateStr + "/";
 */
public class Regression {

	private class disandtime
	{
//		public String timekey;
//		public int stopCount;
		public double dis;
		public double time;
//		public String carId;
		public double error;
		public int state=0;
	}
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	private ArrayList<disandtime> arrayData=new ArrayList<disandtime>();
	private HashMap<String, ArrayList<Double>> lengthStore = null; //key: 特征日+时间段， value:到路口距离
	private HashMap<String, ArrayList<Double>> timeStore = null; //key:特征日+时间段， value:到路口时间

	private String lightID = null;
	private double maxLength = 0;
	private double sumWaitTime = 0;
	private int sumNumber = 0;
	private HashMap<String, double[]> meantime;
	public double A=0;
	public double B=0;
	
	public Regression(String _l)
	{
		this.lightID = _l;
		lengthStore = new HashMap<String, ArrayList<Double>>(125);
		timeStore = new HashMap<String, ArrayList<Double>>(125);
		meantime = new HashMap<String, double[]>(125);
	}

    /**
     * 计算特征日+时间段
     * @param s 时间戳
     * @return  特征日，时间段
     */
	private String getTZR(String s)
	{
		Calendar cal = Calendar.getInstance();
		try {
			Date d = sdf.parse(s);
			cal.setTime(d);
		} catch (ParseException e) {
			return null;
		}
		
		int tzr = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (tzr == 3 || tzr == 4) tzr = 2;
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		return tzr + "," + String.format("%02d", hour);
	}

	/**
     *
     * @param timeStamp
     * @param st  通过路口时间
     * @param sl  离路口距离
     */

	public void add(String timeStamp, String st, String sl)
	{
		String tzr = getTZR(timeStamp);  //特征日,时间段
		if (tzr == null) return;
		double t = Double.parseDouble(st);
		double l = Double.parseDouble(sl);
		if (!lengthStore.containsKey(tzr)) lengthStore.put(tzr, new ArrayList<Double>(500));
		if (!timeStore.containsKey(tzr)) timeStore.put(tzr, new ArrayList<Double>(500));
		lengthStore.get(tzr).add(l);
		timeStore.get(tzr).add(t);
		this.maxLength = Math.max(maxLength, l);
		if (!meantime.containsKey(tzr)) meantime.put(tzr, new double[2]);
		double[] nums = meantime.get(tzr);
		nums[0]++;
		nums[1] += t;
	}
	
	public ArrayList<String> getDetail()
	{
		ArrayList<String> r = new ArrayList<String>();
		r.add(String.valueOf(this.lightID + "," + this.maxLength));
		for (int i = 0; i < 5; i++)
		{
			int index = i;
			if (i > 2) index += 2;
			StringBuffer sb = new StringBuffer(300);
			sb.append(index);
			for (int j = 0; j < 24; j++)
			{
				String tzr = i + "," + String.format("%02d", j);
				if (!meantime.containsKey(tzr)) sb.append(",-1"); 
				else
				{
					double[] nums = meantime.get(tzr);
					if (nums[0] > 0) sb.append("," + (nums[1] / nums[0]));
					else sb.append(",-1");
				}
			}
			r.add(this.lightID + "," + sb.toString());
		}
		
		return r;
	}
	
	public double getMaxLength()
	{
		return this.maxLength;
	}
	
	public double getMeanTime()
	{
		if (sumNumber == 0) return -1;
		else return this.sumWaitTime / sumNumber;
	}
	
 	private double[] calculate(ArrayList<Double> xData, ArrayList<Double> yData)
	{
		if (xData.size() < 2) return null;
		double X = 0, Y = 0, XX = 0, XY = 0;
		for (int i = 0; i < xData.size(); i++)
		{
			double xd = xData.get(i);
			double yd = yData.get(i);
			X += xd;
			Y += yd;
			XX += xd * xd;
			XY += xd * yd;
		}
		int sum = xData.size();
//		System.out.println(sum);
		if (XX - X * X / sum == 0) return null;
		double a = (XY - X * Y / sum)/(XX - X * X / sum);
		double b = (Y - a * X) / sum;
		double[] res = new double[2];
		res[0] = a;
		res[1] = b;
		return res;
	}
	
//	private String regression(String tzr)
//	{
//		ArrayList<Double> lens = lengthStore.get(tzr); // 排队长度
//		ArrayList<Double> times = timeStore.get(tzr); // 等待时间
//		ArrayList<Double> error = new ArrayList<Double>(times.size());
//		double[] res = calculate(lens, times);
//		if (res == null) return "Error,Error";
//		double a = res[0];
//		double b = res[1];
//		for (int i = 0; i < lens.size(); i++)
//			error.add(Math.abs(lens.get(i) * a + b - times.get(i)));
//		return a + "," + b;
//	}

    /**
     *
     * @param tzr
     * @return  A,B
     */
	private String regression(String tzr)
	{
		int counter=0;
		arrayData.clear();
		do{
			counter++;
//			int length=0;
//			for(disandtime d:arrayData)
//			{
//				if(d.state==0)
//				length++;
//			}
		ArrayList<Double> x=new ArrayList<Double>();
		ArrayList<Double> y=new ArrayList<Double>();
		ArrayList<Double> lens = lengthStore.get(tzr); 
		ArrayList<Double> times = timeStore.get(tzr); 
		for(int i=0;i<lens.size();i++)
		{
			disandtime d=new disandtime();
			d.dis=lens.get(i);
			d.time=times.get(i);
			d.state=0;
			arrayData.add(d);
		}
//		int post=0;
    	for(int i=0;i<arrayData.size();i++)
    	{
    		if(arrayData.get(i).state==1)
    			continue;
    		else
    		{
	    		x.add(arrayData.get(i).dis);
	    		y.add(arrayData.get(i).time);
    		}
    	}
    	double[] res = calculate(x, y);
    	if (res == null) return "Error,Error"; 
    	A=res[0];
    	B=res[1];
		}while(NiheBy20ResultJudge()&&counter<=20);//重复拟合，只有所有数据与拟合结果之间的误差，全部在100以内
		//System.out.println(counter+"     "+A+"    "+B);
		caculateAccuracy(tzr,A+","+B,Constant.midPathForError);
		return A + "," + B;
	}
	
	public void caculateAccuracy(String tzr, String ab, String midPathForError)
	{
		try
		{
			FileWriter fw = new FileWriter(new File(midPathForError),true);
			String lightId = this.lightID;
			Double A = Double.parseDouble(ab.split(",")[0]);
			Double B = Double.parseDouble(ab.split(",")[1]);
			ArrayList<Double> lens = lengthStore.get(tzr);
			ArrayList<Double> times = timeStore.get(tzr);
			double result = 0.0;
			if(lens == null || times == null)
			{
				return;
			}
			for(int i=0; i < lens.size(); i++)
			{
//				double predictTime = A * lens.get(i) + B;
//				double error = Math.abs(times.get(i) - predictTime);
				double tempRate = 0;
				
					tempRate = 1 - (Math.abs((lens.get(i)* A + B) - times.get(i)) / times.get(i));
				
					if(tempRate < 0)
					{
						result += 0;
					}
					else result += tempRate;
//				result += error;
			}
			result /= lens.size();
			String line = lightId+","+tzr+","+result+","+lens.size()+"\r\n";
			fw.write(line);
			fw.flush();
			fw.close();
		}
		catch(Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public boolean NiheBy20ResultJudge()
	{
		boolean state=false;
		ArrayList<Integer> errorArray=new ArrayList<Integer>();
		double[] error=new double[arrayData.size()+1];
			
		for(int i=0;i<arrayData.size();i++)
	    {
	    	double distance=arrayData.get(i).dis;
	    	double time=arrayData.get(i).time;
	    	double niheTime=distance*A + B;
	    	double errorTime=Math.abs(niheTime-time);
	   		arrayData.get(i).error=errorTime;
	   		error[i]=errorTime;
	   	}
		Arrays.sort(error);
			
		double errorStandard=error[(int)(error.length * 0.9)];
			
		for(int i=0;i<arrayData.size();i++)
		{
			disandtime d=arrayData.get(i);
			if(d.error>errorStandard)
			{	
				errorArray.add(i);
			}
		}
		//用于判断是否是第一次拟合，这时所有的state都是0
		int post=0;
		for(int j=0;j<arrayData.size();j++)
		{
			if(arrayData.get(j).state==1)
				{
					post=1;
					break;
				}
		}
		if(post==0)
		{
			state=true;
		}
		for(int i=0;i<arrayData.size();i++)
		{
			disandtime d=arrayData.get(i);
				
			if(d.state==1)
			{
				if(!errorArray.contains(i))
				{
					state=true;
					break;
				}
			}
		}
		if(state==true)
		{
			for(disandtime d:arrayData)
			{
				d.state=0;
			}
			for(int i:errorArray)
			{
				arrayData.get(i).state=1;
			}
		}
		return state;
	}

	//result of Least 输出
	public void leastOutput(String path)
	{
		FileWriter fw = null, tfw = null;
		try {
			fw = new FileWriter(path);
//			tfw = new FileWriter(path + "TEMP");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String s : lengthStore.keySet())  //s:432 特征日，时间段   res: a,b
		{
			try {
				String res = regression(s); //res: A,B
				fw.write(s + "," + res + "\n");
//				tfw.write(s + "," + res + "\n");
			} catch (IOException e) {
				continue;
			}
		}
		try {
			fw.close();
//			tfw.close();
		} catch (IOException e) {
			
		}
	}
	
	
	public static void copy(String fpath, String tpath)
	{
//		fpath = Constant.TrafficLightResultPath + dateStr + "/";
//		tpath = "E:/Traffic/";
		System.out.println("Copy start..");
		ArrayList<String> r = new ArrayList<String>();
		r.add("maxqueue.csvTEMP");
		r.add("meanTime.csvTEMP");
		
		File fFile = new File(fpath + "resultOfLeast");
		File tFile = new File(tpath + "resultOfLeast");
		
		String[] ffs = fFile.list();
		File[] tfs = tFile.listFiles();
		for (String s : ffs)
			if (s.endsWith("TEMP")) r.add("resultOfLeast/" + s);
		for (File f : tfs) f.delete();
		for (String s : r)
		{
			if (!s.endsWith("TEMP")) continue;
			String fn = s.substring(0, s.length() - 4);
			//System.out.println(fn);
			File ff = new File(fpath + s);
			File tf = new File(tpath + fn);
			if (tf.exists() && tf.isFile()) tf.delete();
			if (!tf.exists()) 
				ff.renameTo(tf);
//			if (ff.exists()) ff.delete();
		}
//		FileUtil.RemoveDir(fpath, true);
//		return !fFile.exists();
	}
	////////TODO////////////////
	//后期需求，临时将数据从D盘复制到E盘，后期应该适当整改
	public static void copyFromEdiskToDdisk(String fpath,String tpath)
	{
		try{
		System.out.println("Copy start..");
		ArrayList<String> r = new ArrayList<String>();
		r.add("maxqueue.csv");
		r.add("meanTime.csv");
		File fFile = new File(fpath + "resultOfLeast");
		File tFile = new File(tpath + "resultOfLeast");
		
		String[] ffs = fFile.list();
		File[] tfs = tFile.listFiles();
		for (String s : ffs)
		{
			r.add("resultOfLeast/" + s);
		}	
		for (String s : r)
		{
			//System.out.println(fn);
			File ff = new File(fpath + s);
			File tf = new File(tpath + s);
			if (tf.exists() && tf.isFile()) tf.delete();
			BufferedReader reader = new BufferedReader(new FileReader(ff));
			FileWriter fw = new FileWriter(tf);
			String line = "";
			while((line = reader.readLine()) != null)
			{
				fw.write(line+"\r\n");
				fw.flush();
			}
			reader.close();
			fw.close();
//			if (ff.exists()) ff.delete();
		}
//		FileUtil.RemoveDir(fpath, true);
//		return !fFile.exists();
		System.out.println("Copy compished!");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
//	public static void main(String[] args)
//	{
////		String output = Constant.TrafficLightResultPath + 20150812 + "/";
////		String copyPath = "E:/Traffic/";
////		copy(output, copyPath);
//		main3("20150817");
//	}
	
	public static void main3(String dateStr)//xun lian honglvdeng d huigui canshu paidujuli,pingjun tingche shijian
	{
		System.out.println("Regression start..");
		String input = Constant.TrafficLightMidPath;
		String output = Constant.TrafficLightResultPath + dateStr + "/";
//		String copyPathE = Constant.TrafficLightDataCopyE;
//		String copyPathD = Constant.TrafficLightDataCopyD;
		File outfile = new File(output);
		if (!outfile.exists()) outfile.mkdirs();
		outfile = new File(output + "resultOfLeast"); //生成输出resultOfLeast输出目录
		if (!outfile.exists()) outfile.mkdirs();
		
		String maxQueuePath = output + "maxqueue.csv";
		String meantimePath = output + "meanTime.csv";
		
		File inFile = new File(input);
		HashMap<String, ArrayList<File>> set = new HashMap<String, ArrayList<File>>(2000);
		//key为灯id,value为这个灯的所有数据文件集合
		
		File[] datefiles = inFile.listFiles();
		
		for (File df :datefiles)
		{
			File[] lights = df.listFiles();
			for (File lt : lights)  //遍历每天的每个灯的数据csv文件，完成set的初始化
			{
				String light = lt.getName();
				System.out.println("light"+light);
				light = light.substring(0, light.indexOf('.'));
				if (!set.containsKey(light)) set.put(light, new ArrayList<File>(90));
				set.get(light).add(lt);
			}
		}
		FileWriter mt = null, mq = null, tmt = null, tmq = null; 
		try
		{
			mq = new FileWriter(maxQueuePath);
			mt = new FileWriter(meantimePath);
			tmq = new FileWriter(maxQueuePath + "TEMP");
			tmt = new FileWriter(meantimePath + "TEMP");
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		for (String light : set.keySet()) //遍历每个灯的数据集合
		{
			ArrayList<File> fs = set.get(light);
			String outpath = output + "resultOfLeast/" + light + ".csv";
			ArrayList<String> r = process(fs, outpath, light);
			try
			{
				mq.write(r.get(0) + "\n");
				tmq.write(r.get(0) + "\n");
				for (int i = 1; i < r.size(); i++)
				{
					mt.write(r.get(i) + "\n");
					tmt.write(r.get(i) + "\n");
				}
					
			} catch (IOException e)
			{
			}
		}
		try
		{
			mq.close();
			mt.close();
			tmq.close();
			tmt.close();
		} catch (IOException e)
		{
		}
//		copy(output, copyPathD);  临时数据转移复制
//		copyFromEdiskToDdisk(copyPathD, copyPathE);
	}
	
//	public static void main(String[] args) throws IOException
//	{
//		String input = "F:/LightData/preExcuete1/";
//		String output = "C:/Users/JCJ/Desktop/preExcuete1/";
//		File fin = new File(input);
//		File[] fs = fin.listFiles();
//		for (File f : fs)
//		{
//			String[] t = new String[2];
//			t[0] = f.getAbsolutePath();
//			t[1] = output + f.getName() + ".csv";
//			main1(t);
//		}
//	}

	/**
	 *
	 * @param fs 每个灯的数据集合ArrayList
	 * @param output 输出文件csv
	 * @param light  灯号
	 * @return
	 */
	private static ArrayList<String> process(ArrayList<File> fs, String output, String light)
	{
//		String input = "F:/LightData/preExcuete/59566102649_59566103571_59566103575/";
//		String output = "C:/Users/JCJ/Desktop/preExcuete1/1_59565200965_59565200867_59565200830.csv";
//		String input = args[0];
//		String output = args[1];
//		File infile = new File(input);
//		File[] fs = infile.listFiles();
		Regression regs = new Regression(light);
		for (File f : fs)
		{
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(f));
			} catch (FileNotFoundException e) {
				continue;
			}
			String ls;
			try {
				while ((ls = reader.readLine()) != null)
				{
					String[] lss = ls.split(",");
					//中间结果每行数据 0 lightID,1 时间戳，2 通过红绿灯时间，3 离路口距离，4 公交车ID,5 停车次数
					if (lss[5].equals("0"))
					regs.add(lss[1], lss[2], lss[3]);
				}
				reader.close();
			} catch (IOException e) {
			}
		}
		regs.leastOutput(output);  //result of Least 输出
		return regs.getDetail();
	}
	
}
