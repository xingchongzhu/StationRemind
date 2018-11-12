package com.traffic.locationremind.baidu.location.item;

import com.traffic.locationremind.common.util.CommonFuction;

import java.util.List;

public class LineSearchItem {
    public static String ID = "id";
    public static String STARTLINE = "startLine";
    public static String ENDLINE = "endLine";
    public static String LINELIST = "lineList";

    private List<Integer> lineList;
    private int startLine,endLine;

    public LineSearchItem(){

    }

    public LineSearchItem(int startLine,int endLine,List<Integer> lineList){
        this.startLine = startLine;
        this.endLine = endLine;
        this.lineList = lineList;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setLineList(List<Integer> lineList) {
        this.lineList = lineList;
    }

    public List<Integer> getLineList() {
        return lineList;
    }

    public String getLineString() {
        StringBuffer line = new StringBuffer();
        int size = lineList.size();
        for(int n = 0;n < size;n++){
            if(n == size -1) {
                line.append(lineList.get(n));
            }else{
                line.append(lineList.get(n)+CommonFuction.TRANSFER_SPLIT);
            }
        }
        return line.toString();
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }
}
