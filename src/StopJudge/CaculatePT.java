package StopJudge;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by yuxiao on 3/1/16.
 * 根据lightData_Stopmid 计算距离标志和两次停车的平均时间间隔
 */
public class CaculatePT {
    private Bus bus;
    private static Constant constant;

    private void testbus(Constant constant){
         System.out.println(Constant.AllDaysAccuracyCSV);
    }

    public static void main(String[] args){

        statistic(Constant.StopMidDataPath, Constant.SecondStopDataPath);
        System.out.println("SecondStopData Success");
    }


    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    // 统计距离标志
    public static void statistic(String inputPath,String outPath)  {

        File inputFile=new File(inputPath);
        HashMap<String,ArrayList<File>> lightFilesMap= new HashMap<>(2000);

        File[] dateFiles = inputFile.listFiles();
        for (File df: dateFiles){
            File[] daylightsFile=df.listFiles();
            for(File dlf: daylightsFile){
                String lightID=dlf.getName();
                lightID=lightID.substring(0,lightID.indexOf('.'));
                if(!lightFilesMap.containsKey(lightID)) lightFilesMap.put(lightID,new ArrayList<>(90));
                lightFilesMap.get(lightID).add(dlf);
            }
        }

        /**
         * key:lightID_特征日_时间段  value:数据集合
         */
        HashMap<String,ArrayList<String>> lightIDRecordMap=new HashMap<>();

        //region 读取文件，生成lightIDRecordMap
        for (String tlightID: lightFilesMap.keySet()){
            ArrayList<File> tightFiles=lightFilesMap.get(tlightID);

            for(File tf: tightFiles){
                BufferedReader reader=null;
                try{
                    reader=new BufferedReader(new FileReader(tf));
                }catch (FileNotFoundException e){
                    continue;
                }
                String line;
                try{
                    while((line=reader.readLine())!=null){
                        //region lightIDRecordMap 添加数据
                        String[] lineItems=line.split(",");
                        if(lineItems.length<=2) continue; //异常数据，忽略
//                        System.out.println(lineItems[2]);
                        String tzrInfo=getTZR(lineItems[2]); //获取特征日+时间段信息
                        if(tzrInfo==null) continue;

                        String tempkey=tlightID+"_"+tzrInfo;
                        if(!lightIDRecordMap.containsKey(tempkey)){
                            lightIDRecordMap.put(tempkey,new ArrayList<String>());
                        }
                        lightIDRecordMap.get(tempkey).add(line);
                        //endregion
                    }
                    reader.close();
                }catch (IOException e){
                        e.printStackTrace();
                }
            }
            //对lightIDRecordMap的数据计算处理，并输出
            sumupPT(outPath,lightIDRecordMap);
            lightIDRecordMap.clear();
        }
        //endregion


    }

    //计算距离标志并输出到文件中
    private static  void sumupPT(String outputPath,HashMap<String,ArrayList<String>> lightIDData)  {

        try {
            FileWriter fileWriter=new FileWriter(outputPath,true);

            for(String key: lightIDData.keySet()){
                ArrayList<String> lDD=lightIDData.get(key);
                double sumToCrossingDis=0;
                int sumTimeInterval=0;
                int count=0;

                for(String line:lDD){
                    String[] lineItems=line.split(",");
                    if(lineItems.length<=7) continue;//异常数据，忽略
                    if(lineItems[5].equals("1")){
                        sumToCrossingDis+=Double.parseDouble(lineItems[3]);
                        count++;
                    }else if(lineItems[5].equals("2")){
                        sumTimeInterval+=Integer.parseInt(lineItems[6]);
                        count++;
                    }else{
                        continue;
                    }

                }
                double avgToCrossingDis=sumToCrossingDis/count;
                double avgTimeInterval=(double)sumTimeInterval/(double)count;
                // 到信号灯停止线的距离（距离标志值） + 两次停车间平均间隔
                String outPutLine= key+","+avgToCrossingDis+","+avgTimeInterval;
                if(avgToCrossingDis == 0 || avgTimeInterval ==0)
                    continue;
                fileWriter.append(outPutLine + "\n");

            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }


    /**
     * 计算特征日+时间段
     * @param s 时间戳
     * @return  特征日_时间段
     */
    private static String getTZR(String s)
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
        return tzr + "_" + String.format("%02d", hour);
    }
}
