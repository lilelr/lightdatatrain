package FileAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class preExcuete {
	public static ArrayList<String> lightIdList=new ArrayList<String> ();
	public static HashMap<String , ArrayList<String>> lightIdDataByDayHour=new HashMap<String,ArrayList<String>>();
	public static void main(String[] args)
	{
		getLightIdList("F:\\LightData\\A&BTraining\\lightData_mid");
		DenoisingBy10Per("F:\\LightData\\A&BTraining\\lightData_mid", "F:\\LightData\\A&BTraining\\lightData_result");
	}
	//获取该路劲中包含的所有红绿灯ID号
	public static void getLightIdList(String input)
	{
		File file1=new File(input);
		if(!file1.exists())
		{
			System.out.println("File not found!");
			return;
		}
		File [] files1=file1.listFiles();
		for(File f : files1)
		{
			File [] files2=f.listFiles();
			for(File f2 : files2)
			{
				String lightID=f2.getName().substring(0, f2.getName().indexOf('.'));
				if(!lightIdList.contains(lightID))
					lightIdList.add(lightID);
				else continue;
			}
		}
	}
	
	/*
	 * 迭代去噪算法，得到拟合结果，并计算预测时间以及误差，输出
	 * 
	 */
	public static void DenoisingBy10Per(String input,String output)
	{
		try {
			for(String lightId : lightIdList)
			{
				//BufferedReader reader = new BufferedReader(new FileReader(new File(input)));
				File file1=new File(input);
				if(!file1.exists()) {
					System.out.println("File not Found!!!!");
					return;
				}
				File[] files1 = file1.listFiles();
				for(File f : files1)
				{
					File[] files2=f.listFiles();
					for(File f2 : files2)
					{
						if(f2.getName().substring(0, f2.getName().indexOf('.')).equals(lightId))
						{
							//System.out.println(lightId);
							BufferedReader reader = new BufferedReader(new FileReader(f2));
							String line="";
							while((line=reader.readLine())!=null)
							{
								//System.out.println(line);
								String []items = line.split(",");
								String day=Util.getDay(items[1])+"";
								String hour=Util.getHour(items[1])+"";
								String key=lightId+"_"+day+"_"+hour;
								//System.out.println(key+"       "+items[1]);
								if(!lightIdDataByDayHour.containsKey(key))
								{
									ArrayList<String> array = new ArrayList<String >();
									array.add(line);
									lightIdDataByDayHour.put(key, array);
								}
								else {
									ArrayList<String> array=lightIdDataByDayHour.get(key); 
									array.add(line);
									//lightIdDataByDayHour.remove(key);
									lightIdDataByDayHour.put(key, array);								
								}
							}
						}
						else continue;
					}
				}
				int[] days = {0,1,2,5,6};
				int[] hours = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
				for(int day : days)
					for(int hour : hours)
					{
						String key=lightId+"_"+day+"_"+hour;
						ArrayList<String > array=lightIdDataByDayHour.get(key);
						if(array == null)
							continue;
						Nihe nihe=new Nihe(array);
						String ABresult=nihe.regression();
						if(ABresult.equals("error,error"))
							continue;
						double A=Double.parseDouble(ABresult.split(",")[0]);
						double B=Double.parseDouble(ABresult.split(",")[1]);

						String dirPath = output+"\\"+lightId+"\\"+day;
						File outFile=new File(dirPath);
						if(!outFile.exists())
							outFile.mkdirs();
						File outputFile = new File(dirPath+"\\"+hour+".csv");
						if(!outputFile.exists())
							outputFile.createNewFile();
						FileWriter fw = new FileWriter(outputFile);
						for( String s : array)
						{
							String[] items = s.split(",");
							double error =Math.abs( (A * Double.parseDouble(items[3].trim()) 
									+B) - Double.parseDouble(items[2].trim())); 
							/////////////////TODO///////////////////////////////////
							//是否去除噪点数据
								String line="";
								line+=items[0]+","+items[1]+","+items[3]+","+items[2];
								line+=","+(A * Double.parseDouble(items[3].trim()) +B);
								line+=","+error+","+items[4]+","+items[5]+"\r\n";
								fw.write(line);
								fw.flush();
						}
						fw.close();
				}
				lightIdDataByDayHour.clear();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
