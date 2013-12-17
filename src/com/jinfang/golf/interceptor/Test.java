package com.jinfang.golf.interceptor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Test {

	public static void main(String[] args) throws IOException {
		 Map<String, File> files = new HashMap<String,File>();
		 Map<String, String> params = new HashMap<String, String>();
		 files.put("logo.jpg", new File("/Users/zenglvlin/Documents/1.jpg"));
		 uploadFileClient(params,files,"http://127.0.0.1:8080/v1/team/uploadPhoto");
	}
	
	/**
	  * 上传文件
	  * @param params上传参数
	  * @param files上传文件
	  * @return 返回结果JSON
	  * @throws IOException
	  */
	 public static String uploadFileClient(Map<String, String> params,
	     Map<String, File> files, String url) throws IOException {

	     String BOUNDARY = java.util.UUID.randomUUID().toString();
	     String PREFIX = "--", LINEND = "\r\n";
	     String MULTIPART_FROM_DATA = "multipart/form-data";
	     String CHARSET = "UTF-8";

	     URL uri = new URL(url);
	     HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
	     conn.setReadTimeout(20 * 1000);
	     conn.setDoInput(true);
	     conn.setDoOutput(true);
	     conn.setUseCaches(false);
	     conn.setRequestMethod("POST");
	     conn.setRequestProperty("Connection", "keep-alive");
	     conn.setRequestProperty("Accept-Charset", "UTF-8");
	     conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
	             + "; boundary=" + BOUNDARY);
	     conn.addRequestProperty("appKey", "jf_golf_android");

	     StringBuilder sb = new StringBuilder();
	     /**
	      * 处理文本参数
	      */
	     params.put("token", "1-1387124436849-2147483647-8ea90ee751a255308cdd0ccb0bf46cf2");
	     for (Map.Entry<String, String> entry : params.entrySet()) {
	         sb.append(PREFIX);
	         sb.append(BOUNDARY);
	         sb.append(LINEND);
	         sb.append("Content-Disposition: form-data; name=\""
	                 + entry.getKey() + "\"" + LINEND);
	       //  sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
	       //sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
	         sb.append(LINEND);
	         sb.append(entry.getValue());
	         sb.append(LINEND);
	         
	     }

	     //上传数据
	     DataOutputStream outStream = new DataOutputStream(conn
	             .getOutputStream());
//	     outStream.writeBytes(getFormString((HashMap<?, ?>) params).toString());
	     outStream.write(sb.toString().getBytes());
	     /**
	      * 处理文件参数
	      */
	     if (files != null) {
	         for (Map.Entry<String, File> file : files.entrySet()) {
	             StringBuilder sb1 = new StringBuilder();
	             sb1.append(PREFIX);
	             sb1.append(BOUNDARY);
	             sb1.append(LINEND);
	             sb1.append("Content-Disposition: form-data; name=\""
	                     + file.getKey() + "\"; filename=\"" + file.getKey()
	                     + "\"" + LINEND);
	             sb1.append("Content-Type: application/octet-stream; charset="
	                     + CHARSET + LINEND);
	             sb1.append(LINEND);
	             outStream.write(sb1.toString().getBytes());
	             InputStream is = new FileInputStream(file.getValue());
	             byte[] buffer = new byte[1024];
	             int len = 0;
	             while ((len = is.read(buffer)) != -1) {
	                 outStream.write(buffer, 0, len);
	             }

	             is.close();
	             outStream.write(LINEND.getBytes());
	         }
	     }

	     byte[] end_data = (PREFIX + BOUNDARY + PREFIX).getBytes();
	     outStream.write(end_data);
	     outStream.flush();

	     int res = conn.getResponseCode();
	     
	     System.out.println(conn.getResponseMessage());

	     InputStream in = null;
	     StringBuilder sb2 = new StringBuilder();
	     if (res == 200) {
	         in = conn.getInputStream();
	         int ch;

	         while ((ch = in.read()) != -1) {
	             sb2.append((char) ch);
	         }
	     }
	   
	     
	     return in == null ? "wrong" + res : sb2.toString();
	 }
	 
	 
	 private static StringBuffer getFormString(HashMap<?, ?> params){
	        StringBuffer stringBuffer = new StringBuffer("");
	        Iterator<?> it = params.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry entry = (Map.Entry)it.next();
	            Object key = entry.getKey();
	            Object value = entry.getValue();
	            if (null != value) {
	                stringBuffer = stringBuffer.append(key.toString()+"=").append(value.toString()+"&");
	            }else {
	                stringBuffer = stringBuffer.append(key.toString()+"=").append("&");
	            }
	        }
	        
	        // add sign param.
//	        stringBuffer = stringBuffer.append(NetConsts.SIGN+"=").append(MD5Util.createSign(params));
	        return stringBuffer;
	    }

}
