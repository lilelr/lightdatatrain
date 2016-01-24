package Accuracy;


import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	

	
	public static void main(String[] args)
	{
				Calendar cal = Calendar.getInstance();
				String today = sdf.format(cal.getTime());

						Regression.main3(today);
		System.out.println("success");

	}

}
