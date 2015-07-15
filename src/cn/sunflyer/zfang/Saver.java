package cn.sunflyer.zfang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Date;

import cn.sunflyer.zfang.anno.Invoker;
import cn.sunflyer.zfang.obj.IDataInfo;
import cn.sunflyer.zfang.obj.UserInfo;

public class Saver {

	/**
	 * Print As Html
	 * */
	public static void saveGrade(UserInfo i) {
		long currentTime = System.currentTimeMillis();
		String name="教务系统查询结果 - " + i.no + " - " + i.name;
		File pFile = new File(name + ".html");
		
		
		if (!pFile.exists())
			try {
				pFile.createNewFile();
			} catch (IOException e1) {

				e1.printStackTrace();
				return;
			}

		StringBuffer pSb = new StringBuffer("<!DOCTYPE html><html><head>");
		// TODO : remove the following one line if webpage encoding failed.
		pSb.append("<meta charset=\"UTF-8\">");

		pSb.append("<style>body{font-family:\"Microsoft Yahei\";text-align:center;}.gradeinfo{margin:auto;width:90%;border-bottom:1px dashed #808080;}.conclusion{font-style:italic;color:purple;}.part_title{text-decoration:underline;}table{margin:auto;min-width:90%;}td{min-width:100px;}table{border:1px solid black;}.tr_head{background-color:#0043ff;color:white;}.tr_info{border-bottom:1px solid black;}.tr_back{background-color:#F0F0F0;}td{padding:3px;}</style><title>"
				+ name + "</title></head><body><h2>" + name + "</h2>");

		//TODO : 修改要执行操作的类
		String clsList[] = {"GradeEx","Grade","BandRanking","SelectedClass"};
		
		int partnum = 1;
		for(String x:clsList){
			String clsName = "cn.sunflyer.zfang." + x + "Grabber";			
			
			try {
				Class<?> cls = Class.forName(clsName);
				Method[] method = cls.getMethods();
				for(Method m : method){
					Invoker inv = m.getAnnotation(Invoker.class);
					if(inv != null){
						Object resObj =  m.invoke(null, i);
						if(resObj != null){
							IDataInfo[] res = (IDataInfo[]) resObj;
							String conclusionData = "";
							if(inv.hasConclusion()){
								try{
									Method getConclusionMethod = cls.getMethod(inv.conclusionMethod(), Object.class);
									conclusionData = String.valueOf(getConclusionMethod.invoke(null, resObj));
								}catch(Exception e){
									System.out.println("抓取概括信息出现错误 : " + e.getMessage());
									e.printStackTrace();
								}
							}
							pSb.append(getInfoToTable(getPartTitle(partnum + "", inv.name()), res, inv.nodata(), conclusionData));
							partnum ++;
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//结尾
		pSb.append("<p>Total Time Used For Generation : " + ((System.currentTimeMillis() - currentTime) / 1000.0) + "(s). Click <a target=\"_blank\" href=\"" + i.addr + "xs_main.aspx?xh=" + i.no + "\">Here</a> to Login your Education System</p><p>Automatic Generated By CQUTEduSystemGrabber @ "
				+ ((new Date()).toString())
				+ " <a target=\"_blank\" href=\"https://github.com/sunflyer/CQUTEduSystemGrabber\">GitHub</a></p></body></html>");
		try {
			OutputStreamWriter pW = new OutputStreamWriter(new FileOutputStream(pFile),"UTF-8");
			pW.write(pSb.toString());
			pW.close();
			Runtime.getRuntime().exec("explorer " + name + ".html");

		} catch (IOException e) {

			e.printStackTrace();
		}

	}
	
	public static String getPartTitle(String part,String t){
		return "<p class=\"part_title\">Part " + part + " . " + t +"</p>";
	}
	
	public static String getConclusion(String c){
		return "<p class=\"conclusion\">" + c + "</p>";
	}
	
	public static final String CLASS_TR_HEAD = "tr_head",
			CLASS_TR_INFO = "tr_info",
			CLASS_TR_BACK = "tr_back";
	
	/**
	 * 获取表格的行
	 * @param cls 指定给这个tr的class属性
	 * @param data 包含的列数据
	 * */
	public static String getTableRow(String[] cls,String[] data){
		StringBuffer p = new StringBuffer("<tr");
		if(cls != null && cls.length > 0){
			p.append(" class=\"");
			for(String x:cls){
				if(x == null)
					continue;
				p.append(x + " ");
			}
			p.append("\"");
		}
		p.append(">");
		
		if(data != null && data.length > 0){
			for(String x:data){
				p.append("<td>" + x + "</td>");
			}
		}
		
		p.append("</tr>");
		
		return p.toString();
	}

	/**
	 * @param title 区块标题
	 * @param data 数据内容
	 * @param nodata 如果没有数据时显示的内容
	 * @param conclusion 附加内容
	 * */
	public static String getInfoToTable(String title,IDataInfo[] data,String nodata,String conclusion){
		StringBuffer pSb = new StringBuffer("<div class=\"gradeinfo\">");
		if(title != null)
			pSb.append(title);
		if(data != null && data.length > 0){
			pSb.append("<table>");
			pSb.append(getTableRow(new String[]{CLASS_TR_HEAD},data[0].getRequiredDataName()));
			boolean d = false;
			for(IDataInfo x:data){
				pSb.append(getTableRow(new String[]{CLASS_TR_INFO , d ? CLASS_TR_BACK : null},x.getRequiredData()));
				d = !d;
			}
			pSb.append("</table>");
			
		}else{
			pSb.append(nodata == null ? "这块内容没有数据" : nodata);
		}
		
		if(conclusion != null)
			pSb.append(getConclusion(conclusion));
		
		return pSb.append("</div>").toString();
	}
	
}
