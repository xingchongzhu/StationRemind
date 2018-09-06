/**
 * Copyright (C) 2015~2050 by foolstudio. All rights reserved.
 * 
 * ��Դ�ļ��д��벻����������������ҵ��;�����߱�������Ȩ��
 * 
 * ���ߣ�������
 * 
 * �������䣺foolstudio@qq.com
 * 
*/

package com.traffic.locationremind.common.util;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class FooFileUtil {
	private static FooFileUtil mInstance =  new FooFileUtil();
	private FooFileUtil() {}
	public static FooFileUtil getInstance() { return(mInstance); }
	
	public ArrayList<String> list(String path, final String suffix) {		
		File dir = new File(path);
		if(!dir.exists()) { return null; }
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return (filename.endsWith(suffix));
			}
		};
	
		ArrayList<String> items = new ArrayList<String>();
		
		String[] arr = dir.list(filter);
		for(int i = 0; i < arr.length; ++i) {
			items.add(arr[i]);
		}
		
		return (items);
	}

	public static void saveFile(String str) {  

	    try {  
	        File file = new File(IDef.MISSPATH+IDef.MISSPATHNAME);  
	        if (!file.exists()) {  
	            file.createNewFile();  
	        }  
	        FileOutputStream outStream = new FileOutputStream(file);  
	        outStream.write(str.getBytes());  
	        outStream.close();  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
    public static void WriteTxtFile(String strcontent,String strFilePath){
      String strContent=strcontent+"\n";
      try {
           File file = new File(strFilePath);
           if (!file.exists()) {
        	   Log.d("TestFile", "Create the file:" + strFilePath);
            	file.createNewFile();
           }else{
        	   if(ReadTxtFile(strFilePath, strcontent)){
        		   return;
        	   }
           }
           RandomAccessFile raf = new RandomAccessFile(file, "rw");
           raf.seek(file.length());
           raf.write(strContent.getBytes());
           raf.close();
      } catch (Exception e) {
           Log.e("TestFile", "Error on write File.");
          }
    }

    public static boolean ReadTxtFile(String strFilePath,String str)
    {
        String path = strFilePath;
        boolean isExist = false;
            File file = new File(path);
            if (file.isDirectory())
            {
                Log.d("TestFile", "The File doesn't not exist.");
            }
            else
            {
                try {
                    InputStream instream = new FileInputStream(file); 
                    if (instream != null) 
                    {
                        InputStreamReader inputreader = new InputStreamReader(instream);
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line;
                        while (( line = buffreader.readLine()) != null) {
                            if(line.equals(str)){
                            	isExist = true;
                            	break;
                            }
                        }                
                        instream.close();
                    }
                }
                catch (java.io.FileNotFoundException e) 
                {
                    Log.d("TestFile", "The File doesn't not exist.");
                } 
                catch (IOException e) 
                {
                     Log.d("TestFile", e.getMessage());
                }
            }
            return isExist;
    }

	public static String rightTrim(String str) {

		if (str == null) {

			return "";

		}

		int length = str.length();

		for (int i = length - 1; i >= 0; i--) {

			if (str.charAt(i) != 0x20) {

				break;

			}

			length--;

		}

		return str.substring(0, length);

	}
	public String getFilename(String url) {
		File f = new File(url);
		return (f.getName());
	}
};
