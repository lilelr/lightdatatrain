package Accuracy;


import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");


    /**
     * 生成resultofLeast   String input = Constant.TrafficLightMidPath;
     String output = Constant.TrafficLightResultPath + dateStr + "/";
	 main3() 训练4张表
     * @param args
     */
	public static void main(String[] args)
	{
				Calendar cal = Calendar.getInstance();
				String today = sdf.format(cal.getTime());

						Regression.main3(today);
		System.out.println("success");

	}

}
