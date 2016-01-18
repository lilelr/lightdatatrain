package FileAnalysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Nihe {
	private ArrayList<disandtime> arrayData=new ArrayList<disandtime>();
	private  ArrayList<String > array=null;
	private double A=0;
	private double B=0;
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	private ArrayList<Double> lengthStore = new ArrayList<Double>();
	private  ArrayList<Double> timeStore = new ArrayList<Double>();
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
	public ArrayList<String> getArray() {
		return array;
	}
	public void setArray(ArrayList<String> array) {
		this.array = array;
	}
	public double getA() {
		return A;
	}
	public void setA(double a) {
		A = a;
	}
	public double getB() {
		return B;
	}
	public void setB(double b) {
		B = b;
	}
	
	Nihe(ArrayList<String> a)
	{
		this.array=a;
	}
	Nihe()
	{
		
	}
	public  String regression()
	{
		ArrayList<String> data=this.array;
		if(data==null)
			return "error,error";
		lengthStore.clear();
		timeStore.clear();
		for(String s : data)
		{
			String [] items=s.split(",");
			double length=Double.parseDouble(items[3].trim());
			double time=Double.parseDouble(items[2].trim());
			lengthStore.add(length);
			timeStore.add(time);
		}
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
		for(int i=0;i<timeStore.size();i++)
		{
			disandtime d=new disandtime();
			d.dis=lengthStore.get(i);
			d.time=timeStore.get(i);
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
    	if (res == null)
    		{
    		System.out.println(array.size());
    			for(String s : array)
    			System.out.println(s);
    			return "error,error";
    		}
    	A=res[0];
    	B=res[1];
		}while(NiheBy10ResultJudge()&&counter<=20);//重复拟合，只有所有数据与拟合结果之间的误差，全部在100以内
		//System.out.println(counter+"     "+A+"    "+B);
		return A + "," + B;
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
 	public boolean NiheBy10ResultJudge()
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
}
