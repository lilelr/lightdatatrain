package Accuracy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import FileAnalysis.Util;
import StopJudge.Constant;

public class LightAccuracy {
	public static HashMap<String , String> resultLeast = null;
	public static void main(String[] args)
	{
//		getresultOfLeast("/Users/yuxiao/项目/毕设/文件/2016/four_result/resultOfLeast/");
        getresultOfLeast(Constant.ResultOfLeast);
		System.out.println("Catch A and B ready!");
		AccuracyOfAllDays(Constant.TrafficLightMidPath, Constant.AllDaysAccuracyCSV);
        System.out.println("end");

	}
	public static void getresultOfLeast(String input)
	{
		resultLeast = new HashMap<String,String> ();
		try{
			File file1 = new File(input);
			if(!file1.exists())
			{
				System.out.println("File of FourTable not found! please check!");
				return;
			}
			else
			{
				File[] files = file1.listFiles();
				for(File file2 : files)
				{
					String lightId = file2.getName().substring(0, file2.getName().indexOf('.'));
					BufferedReader reader = new BufferedReader(new FileReader(file2));
					String line = "";
					while((line = reader.readLine()) != null)
					{
						String[] items = line.split(",");
						String key = lightId+"_"+items[0]+"_"+items[1];
						resultLeast.put(key, items[2]+","+items[3]);
					}
					reader.close();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}





	//增加数据天数
	public static void AccuracyOfAllDays(String input,String output)
	{
		try{
			HashMap<String, ArrayList<File>> map = new HashMap<String,ArrayList<File>>(); //key为lightId,value 为这个路口的数据集合
			File file1 = new File(input);
			if(!file1.exists())
			{
				System.out.println("Your Path is null!");
				return;
			}
			else
			{
				if(file1.isDirectory())
				{
					File[] files1 = file1.listFiles(); //读取每一天的文件夹
					for(File file2 : files1)
					{
						File[] files2 = file2.listFiles();//每一天的红绿灯路口数据集
						for(File file3 :files2) //读取一个路口的数据
						{
							String lightId = file3.getName().substring(0, file3.getName().indexOf('.'));
							if(map.containsKey(lightId))
							{
								map.get(lightId).add(file3);
							}
							else{
								ArrayList<File> fileMap = new ArrayList<File>();
								fileMap.add(file3);
								map.put(lightId, fileMap);
							}
						}
					}
				}
			}
		//遍历map, <信号灯Id,所有文件>
			FileWriter fw = new FileWriter(new File(output));
			Iterator iter = map.entrySet().iterator();
			while(iter.hasNext())
			{
				Map.Entry entry =(Map.Entry) iter.next();
				String keyID = entry.getKey().toString();
				ArrayList<File> fileMap = map.get(keyID);
				System.out.println(keyID+"   File counter:"+fileMap.size()+" 个");
				// 暂时规定误差的标准为30S， 误差在30S以内，则认为是准确的
				int standard = 20;
				int counter1=0, counter2 = 0;
				int count =0 ; // 总数计数器，需要找出这天每个红绿灯的停车次数
				double totalRate = 0;
				for(File file4 : fileMap) //分析每个路口的数据集
				{
					BufferedReader reader = new BufferedReader(new FileReader(file4));
					String line = "";

					while((line = reader.readLine()) != null)
					{
						String[] items = line.split(",");
						//过滤停车次数
						if(!items[5].equals("0"))
						{
							continue;
						}
						else if(Double.parseDouble(items[2].trim()) < 60) //60真实停车时间
						{
							continue;
						}
						/////TODO/////
//						else if(!inTime(items[1].trim(),7,10,17,20))
//						{
//							continue;
//						}
						else
						{  //只对有停车的数据计算准确率
							String time = items[1].trim();
							int day = Util.getDay(time);
							int hour = Util.getHour(time);
							String key = items[0]+"_"+day+"_"+hour;
							if(!resultLeast.containsKey(key))
								continue;
							String result = resultLeast.get(key);
							if(result.split(",")[0].equals("Error")||result.split(",")[1].equals("Error"))
								continue;
							double A = Double.parseDouble(result.split(",")[0]);
							double B = Double.parseDouble(result.split(",")[1]);
							double error = Math.abs((Double.parseDouble(items[3]) * A + B) - Double.parseDouble(items[2]));
							count++;
							double tempRate = 0;
							if((Math.abs((Double.parseDouble(items[3]) * A + B) - Double.parseDouble(items[2]))) <= 1)
							{
								tempRate = 1;
							}
							else
							{
								tempRate = 1 - (Math.abs((Double.parseDouble(items[3]) * A + B) - Double.parseDouble(items[2])) / Double.parseDouble(items[2]));
							}
								if(tempRate < 0)
								{
									totalRate += 0;
								}
								else totalRate += tempRate;
							if(error > standard)
								counter2++;
							else counter1++;
						}
					}
					reader.close();
				}

					double rate = counter1 * 1.00000 / count;
					String line2 = "";
//					line2 += lightId+","+counter1+","+count+","+rate+","+totalRate / count;
					line2 += keyID+","+count+","+totalRate / count;
					fw.write(line2+"\r\n");
					fw.flush();
				}

			fw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
