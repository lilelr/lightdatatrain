package FileAnalysis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	/*
	 * 工具类，用于保存项目中所编写的小工具
	 */
	////////////////////////TODO/////////////////////////
	
	//获取特征日
	public static int getDay(String date)
	{
		SimpleDateFormat df=new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date d=df.parse(date);
			int day=d.getDay();
			if(day==3||day==4)
				return 2;
			else return day;
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	/*
	 * 获取特征时间段
	 */
	public static int getHour(String date)
	{
		SimpleDateFormat df=new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Date d=df.parse(date);
			int hour=d.getHours();
			return hour;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
}
