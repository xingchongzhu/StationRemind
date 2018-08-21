package com.traffic.locationremind.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.item.IteratorNodeTool;
import com.traffic.locationremind.baidu.location.item.Node;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.*;

public class PathSerachUtil {
    private static final int MAXLINENUMBER = 20;
    private static final int MAXRECOMENDLINENUMBER = 8;
    static String TAG = "PathSerachUtil";
    static boolean debug = false;

    /**
     * 往图片上写入文字、图片等内容
     */
    public static void findLinedStation(LineInfo lineInfo, StationInfo start, StationInfo end, List<StationInfo> list) {
        List<StationInfo> stationInfoList = lineInfo.getStationInfoList();

        if (start == null || end == null) {
            return;
        }
        //Log.d(TAG,"起点 "+start.getCname()+" 终点 "+end.getCname()+" lineInfo.line = "+lineInfo.lineid);
        //Log.d(TAG,"  start.pm  = "+start.pm+" end.pm = "+end.pm);
        if (start.pm < end.pm) {
            int next = start.pm;
            for (StationInfo mStationInfo : stationInfoList) {
                if (next == mStationInfo.pm) {
                    int size = list.size();
                    if (size >= 1) {
                        if (list.get(size - 1).getCname().equals(mStationInfo.getCname())) {
                            list.remove(size - 1);
                        }
                    }
                    list.add(mStationInfo);
                    if (debug)
                        Log.d(TAG, mStationInfo.getCname() + "   ");
                    if (next == end.pm) {
                        break;
                    }
                    next++;
                }
            }
        } else {
            int next = start.pm;
            int size = stationInfoList.size() - 1;
            for (int i = size; i >= 0; i--) {
                StationInfo mStationInfo = stationInfoList.get(i);
                if (next == mStationInfo.pm) {
                    int size1 = list.size();
                    if (size1 >= 1) {
                        if (list.get(size1 - 1).getCname().equals(mStationInfo.getCname())) {
                            list.remove(size1 - 1);
                        }
                    }
                    list.add(mStationInfo);
                    if (debug)
                        Log.d(TAG, mStationInfo.getCname() + "   ");
                    if (next == end.pm) {
                        break;
                    }
                    next--;
                }
            }
        }
    }

    public static List<Map.Entry<List<Integer>, List<StationInfo>>> getLastRecomendLines(Map<List<Integer>, List<StationInfo>> currentAllStationList) {
        List<Map.Entry<List<Integer>, List<StationInfo>>> infoIds = new ArrayList<Map.Entry<List<Integer>, List<StationInfo>>>(currentAllStationList.entrySet());

        Collections.sort(infoIds, new Comparator<Map.Entry<List<Integer>, List<StationInfo>>>() {
            /*
             * 返回负数表示：p1 小于p2，
             * 返回0 表示：p1和p2相等，
             * 返回正数表示：p1大于p2
             */
            public int compare(Map.Entry<List<Integer>, List<StationInfo>> o1,
                               Map.Entry<List<Integer>, List<StationInfo>> o2) {
                if (o1.getValue().size() < o2.getValue().size()) {
                    return -1;
                } else if (o1.getValue().size() == o2.getValue().size()) {
                    return 0;
                }
                return 1;
            }
        });
        if (infoIds.size() <= MAXRECOMENDLINENUMBER) {
            return infoIds;
        }
        List<Map.Entry<List<Integer>, List<StationInfo>>> lastLines = new ArrayList<Map.Entry<List<Integer>, List<StationInfo>>>();
        //Log.d(TAG,"---------------------no filte----------------------------------");
        //printAllRecomindLine(infoIds);//打印所有路线
        //Log.d(TAG,"---------------------filte 20 ----------------------------------");
        if (infoIds.size() > MAXLINENUMBER) {//保留前20条路线
            int n = 0;
            for (Map.Entry<List<Integer>, List<StationInfo>> entry : infoIds) {
                if (n < MAXLINENUMBER)
                    lastLines.add(entry);
                n++;
            }
            infoIds.clear();
        } else {
            lastLines.addAll(infoIds);
        }
        //printAllRecomindLine(lastLines);//打印所有路线
        //Log.d(TAG,"---------------------filte 20 ----------------------------------");
        // 对HashMap中的key 进行排序
        Collections.sort(lastLines, new Comparator<Map.Entry<List<Integer>, List<StationInfo>>>() {
            /*
             * 返回负数表示：p1 小于p2，
             * 返回0 表示：p1和p2相等，
             * 返回正数表示：p1大于p2
             */
            public int compare(Map.Entry<List<Integer>, List<StationInfo>> o1,
                               Map.Entry<List<Integer>, List<StationInfo>> o2) {
                if (o1.getKey().size() < o2.getKey().size()) {
                    return -1;
                } else if (o1.getKey().size() == o2.getKey().size()) {
                    return 0;
                }
                return 1;
            }
        });
        List<Map.Entry<List<Integer>, List<StationInfo>>> lastLinesLast = new ArrayList<Map.Entry<List<Integer>, List<StationInfo>>>();
        if (lastLines.size() > MAXRECOMENDLINENUMBER) {//最终保留推荐线路不超过5条

            int size = lastLines.size();
            lastLinesLast.add(lastLines.get(0));
            for (int i = 1; i < size; i++) {
                if (lastLines.get(i).getKey().size() == lastLinesLast.get(0).getKey().size()) {
                    lastLinesLast.add(lastLines.get(i));
                }
            }
            lastLines.removeAll(lastLinesLast);
            size = lastLines.size();

            for (int i = 0; i < size; i++) {
                Map.Entry<List<Integer>, List<StationInfo>> entry = lastLines.get(i);
                if (i < MAXRECOMENDLINENUMBER) {
                    lastLinesLast.add(entry);
                }
                Log.d(TAG, "getReminderLines getKey = " + entry.getKey() + " size = " + entry.getValue().size());
            }
        } else {
            lastLinesLast.addAll(lastLines);
        }
        List<Map.Entry<List<Integer>, List<StationInfo>>> remove = new ArrayList<Map.Entry<List<Integer>, List<StationInfo>>>();
        for (Map.Entry<List<Integer>, List<StationInfo>> entry : lastLinesLast) {
            int size = entry.getKey().size();
            for (int i = 0; i < size; i++) {
                boolean needRemove = true;
                for (StationInfo stationInfo : entry.getValue()) {
                    if (entry.getKey().get(i) == stationInfo.lineid) {
                        needRemove = false;
                        break;
                    }
                }
                if (needRemove) {

                    remove.add(entry);
                    break;
                }
            }
        }
        for (Map.Entry<List<Integer>, List<StationInfo>> entry : remove) {
            Log.d("needRemove", "need remove entry key =" + entry.getKey());
            for (Map.Entry<List<Integer>, List<StationInfo>> entry1 : lastLinesLast) {
                if (entry1.getKey() == entry.getKey()) {
                    lastLinesLast.remove(entry1);
                    break;
                }
            }
        }
        lastLines.clear();
        Collections.sort(lastLinesLast, new Comparator<Map.Entry<List<Integer>, List<StationInfo>>>() {
            /*
             * 返回负数表示：p1 小于p2，
             * 返回0 表示：p1和p2相等，
             * 返回正数表示：p1大于p2
             */
            public int compare(Map.Entry<List<Integer>, List<StationInfo>> o1,
                               Map.Entry<List<Integer>, List<StationInfo>> o2) {
                if (o1.getValue().size() < o2.getValue().size()) {
                    return -1;
                } else if (o1.getValue().size() == o2.getValue().size()) {
                    return 0;
                }
                return 1;
            }
        });
        return lastLinesLast;
    }

    public static Map<List<Integer>, List<StationInfo>> getAllLineStation(Map<Integer, LineInfo> mLineInfoList, List<List<Integer>> transferLine
            , StationInfo start, final StationInfo end) {
        Map<List<Integer>, List<StationInfo>> currentAllStationList = new HashMap<>();//正在导航线路
        //取出一条路线
        int n = 0;
        for (List<Integer> list : transferLine) {

            StationInfo startStation = null, endStation = null;
            final int size = list.size();
            //构建多叉树
            //起点
            LineInfo lineInfo = getLineInfoByLineid(mLineInfoList, list.get(0));
            startStation = getStationInfoByLineidAndName(lineInfo.getStationInfoList(), start.getCname());//再找相同站台
            Node root = new Node(startStation);//根节点
            //终点
            lineInfo = getLineInfoByLineid(mLineInfoList, list.get(size - 1));
            endStation = getStationInfoByLineidAndName(lineInfo.getStationInfoList(), end.getCname());//再找相同站台
            for (int i = 0; i < size; i++) {
                final int lined = list.get(i);
                if (size > i + 1) {
                    List<StationInfo> stationInfoList = getTwoLineCommonStation(mLineInfoList, lined, list.get(i + 1));//找到当前线路和下一条线路交汇站台
                    if (stationInfoList != null && stationInfoList.size() > 0) {
                        addChild(root, stationInfoList);
                    }
                }
            }
            List<StationInfo> stationInfoList = new ArrayList<>();
            stationInfoList.add(endStation);
            addChild(root, stationInfoList);

            //多叉树查找所有路径
            //Log.d("zxc", "----- list = " + list);
            IteratorNodeTool tool=new IteratorNodeTool();
            Stack <Node>pathstack=new Stack();
            tool.iteratorNode(root, pathstack);
            List<List<StationInfo>> all = new ArrayList<>();
            for(List<StationInfo>entry:tool.pathMap){
                all.add(getLineStation(entry,mLineInfoList)) ;
            }
            StringBuffer buffer = new StringBuffer();
            if(all != null && all.size() > 0) {
                List<StationInfo> min = all.get(0);
                for (List<StationInfo> ll : all) {
                    if(min.size() > ll.size()){
                        min = ll;
                    }
                }
                currentAllStationList.put(list, min);
            }
            //Log.d("zxc", "----- end------------");

        }
        return currentAllStationList;
    }

    public static List<StationInfo> getLineStation(List list,Map<Integer, LineInfo> mLineInfoList){
        List<StationInfo> oneLineMap = new ArrayList<>();
        int size = list.size();
        //Log.d("zxc","----------start---------------");
        StringBuffer buf = new StringBuffer();
        for(int i = 0;i < size;i++){
            if(i+1 < size ){
                Node start = (Node)list.get(i);
                StationInfo stationInfo = (StationInfo) start.getNodeEntity();
                Node end = (Node)list.get(i+1);
                StationInfo endInfo = (StationInfo) end.getNodeEntity();
                int lineid = stationInfo.lineid;
                if(i != 0){
                    lineid = endInfo.lineid;
                }
                LineInfo lineInfo = getLineInfoByLineid(mLineInfoList, lineid);
                for(StationInfo stationInfo1:lineInfo.getStationInfoList()){
                    if(stationInfo1.getCname().equals(endInfo.getCname())){
                        endInfo = stationInfo1;
                    }
                    if(stationInfo1.getCname().equals(stationInfo.getCname())){
                        stationInfo = stationInfo1;
                    }
                }
                PathSerachUtil.findLinedStation(lineInfo,stationInfo,endInfo,oneLineMap);

            }
            /*Node start = (Node)list.get(i);
            StationInfo stationInfo = (StationInfo) start.getNodeEntity();
            buf.append(stationInfo.lineid+"  "+stationInfo.getCname()+" ->");*/
        }
        //Log.d("zxc",buf.toString());
        //Log.d("zxc","----------end--------------");
        return oneLineMap;
    }

    public static void addChild(Node node, List<StationInfo> stationInfoList) {
        if (node.getChildNodes() == null) {
            for (StationInfo stationInfo : stationInfoList) {
                Node nextNode = new Node(stationInfo);
                node.addChildNode(nextNode);
            }
        } else {
            List<Node> stationInfos = node.getChildNodes();
            for (Node node1 : stationInfos) {
                addChild(node1, stationInfoList);
            }
        }
    }

    public static StationInfo gitNearestStation(List<StationInfo> stationInfoList, StationInfo stationInfo) {
        if (stationInfoList == null || stationInfoList.size() <= 0) {
            return null;
        }
        StationInfo minStationInfo = stationInfoList.get(0);
        int min = 0;
        for (StationInfo station : stationInfoList) {
            int dis = Math.abs(station.pm - stationInfo.pm);
            if (min > dis) {
                minStationInfo = station;
            }
        }
        return minStationInfo;
    }

    public static StationInfo getStationInfoByLineidAndName(List<StationInfo> stationInfoList, String name) {
        for (StationInfo mStationInfo : stationInfoList) {//查询最站台和当前路线相同站
            if (mStationInfo.getCname().equals(name)) {
                return mStationInfo;
            }
        }
        return null;
    }

    public static List<StationInfo> getTwoLineCommonStation(Map<Integer, LineInfo> mLineInfoList, int line1, int line2) {
        List<StationInfo> stationlist = new ArrayList<>();
        LineInfo lineInfo1 = PathSerachUtil.getLineInfoByLineid(mLineInfoList, line1);
        for (StationInfo stationInfo : lineInfo1.getStationInfoList()) {
            if (stationInfo.canTransfer()) {
                int lined = PathSerachUtil.isSameLine(stationInfo, line2);
                if (lined > 0) {
                    stationlist.add(stationInfo);
                }
            }
        }
        return stationlist;
    }

    public static LineInfo getLineInfoByLineid(Map<Integer, LineInfo> lineInfoList, int lineid) {
        return lineInfoList.get(lineid);
    }

    public static String printAllRecomindLine(List<Map.Entry<List<Integer>, List<StationInfo>>> lastLines) {
        StringBuffer str = new StringBuffer();
        Log.d(TAG, "------------------------devide-----------------------lastLines.size = " + lastLines.size());
        for (Map.Entry<List<Integer>, List<StationInfo>> entry : lastLines) {
            str.append(entry.getKey().toString() + ":");
            for (StationInfo stationInfo : entry.getValue()) {
                str.append(stationInfo.getCname() + "->");
            }
            str.append("\n");
            //Log.d(TAG,"key = "+entry.getKey()+" change = "+entry.getKey().size()+" stationnumber = "+entry.getValue().size()+" station = "+str.toString());
        }
        Log.d(TAG, str.toString());
        Log.d(TAG, "------------------------end-----------------------");
        return str.toString();
    }

    //查询站台是否与目标线路有相同线路
    public static int isSameLine(StationInfo start, int lined) {
        final String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
        int size = lines.length;
        for (int i = 0; i < size; i++) {//找出和终点站相同点不用换乘
            if (CommonFuction.convertToInt(lines[i], 0) == lined) {//找到相同线路站不用换乘
                return CommonFuction.convertToInt(lines[i], 0);
            }
        }
        return -1;
    }

    //查询站台是否与目标线路有相同线路
    public static int isTwoStationSameLine(StationInfo start, StationInfo end) {
        final String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
        final String lines1[] = end.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
        int size = lines.length;
        int size1 = lines1.length;
        for (int i = 0; i < size; i++) {//找出和终点站相同点不用换乘
            for (int j = 0; j < size1; j++) {
                if (CommonFuction.convertToInt(lines[i], 0) == CommonFuction.convertToInt(lines1[j], -1)) {//找到相同线路站不用换乘
                    return CommonFuction.convertToInt(lines[i], 0);
                }
            }
        }
        return -1;
    }

    public static StationInfo getNerastStation(BDLocation location, Map<Integer, LineInfo> mLineInfoList) {
        double min = Double.MAX_VALUE;
        double longitude = 0;
        double latitude = 0;
        double dis = 0;
        StationInfo nerstStationInfo = null;
        if (location != null && mLineInfoList != null) {
            for (Map.Entry<Integer, LineInfo> entry : mLineInfoList.entrySet()) {
                for (StationInfo stationInfo : entry.getValue().getStationInfoList()) {
                    longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
                    latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
                    dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
                    if (min > dis) {
                        min = dis;
                        nerstStationInfo = stationInfo;
                    }
                }
            }
        }
        return nerstStationInfo;
    }

    private static final int MINDIS = 600;

    public static StationInfo getNerastNextStation(BDLocation location, Map<Integer, LineInfo> mLineInfoList) {
        double min = Double.MAX_VALUE;
        double longitude = 0;
        double latitude = 0;
        double dis = 0;
        StationInfo nerstStationInfo = null;
        if (location != null && mLineInfoList != null) {
            for (Map.Entry<Integer, LineInfo> entry : mLineInfoList.entrySet()) {
                for (StationInfo stationInfo : entry.getValue().getStationInfoList()) {
                    longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
                    latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
                    dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
                    if (min > dis) {
                        min = dis;
                        nerstStationInfo = stationInfo;
                    }
                }
            }
        }
        Log.d("zxc", "getNerastNextStation min = " + min);
        if (MINDIS < min) {
            return null;
        }
        return nerstStationInfo;
    }

    public static boolean arriveNextStatison(BDLocation location, StationInfo stationInfo) {
        double longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
        double latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
        double dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
        if (MINDIS > dis) {
            return true;
        }
        return false;
    }

    public static List<Map.Entry<List<Integer>, List<StationInfo>>> getReminderLines(StationInfo start, final StationInfo end,
                                                                                     int maxLineid, BDLocation location, Map<Integer, LineInfo> mLineInfoList, Map<Integer, Map<Integer, Integer>> allLineCane) {
        List<Map.Entry<List<Integer>, List<StationInfo>>> lastLinesLast = new ArrayList<Map.Entry<List<Integer>, List<StationInfo>>>();
        if (end == null) {
            return lastLinesLast;
        }
        List<List<Integer>> transferLine = new ArrayList<List<Integer>>();//所有换乘路线
        final Integer allLineNodes[] = new Integer[maxLineid];
        for (int i = 0; i < maxLineid; i++) {//初始化所有线路
            allLineNodes[i] = i;
        }
        if (start == null) {
            start = PathSerachUtil.getNerastStation(location, mLineInfoList);
        }
        if (start != null) {
            if (!start.canTransfer() && !end.canTransfer()) {//都不能换乘
                if (start.lineid == end.lineid) {//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(start.lineid);
                    transferLine.add(list);
                } else {
                    transferLine = GrfAllEdge.createGraph(allLineNodes, allLineCane, start.lineid, end.lineid);
                }
            } else if (start.canTransfer() && !end.canTransfer()) {
                int lined = PathSerachUtil.isSameLine(start, end.lineid);
                if (lined > 0) {//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(lined);
                    transferLine.add(list);
                } else {
                    String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    for (int i = 0; i < lines.length; i++) {
                        int startid = CommonFuction.convertToInt(lines[i], -1);
                        if (startid >= 0) {
                            List<List<Integer>> temp = GrfAllEdge.createGraph(allLineNodes, allLineCane, startid, end.lineid);
                            transferLine.addAll(temp);
                        }
                    }
                }
            } else if (!start.canTransfer() && end.canTransfer()) {
                int lined = PathSerachUtil.isSameLine(end, start.lineid);
                if (lined > 0) {//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(lined);
                    transferLine.add(list);
                } else {
                    String lines[] = end.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    for (int i = 0; i < lines.length; i++) {
                        int startid = CommonFuction.convertToInt(lines[i], -1);
                        if (startid >= 0) {
                            List<List<Integer>> temp = GrfAllEdge.createGraph(allLineNodes, allLineCane, start.lineid, startid);
                            transferLine.addAll(temp);
                        }
                    }
                }
            } else {
                int lined = PathSerachUtil.isTwoStationSameLine(start, end);
                if (lined > 0) {//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(lined);
                    transferLine.add(list);
                } else {
                    String startLines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    String endLines[] = end.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    for (int i = 0; i < startLines.length; i++) {//遍历所有情况
                        int startid = CommonFuction.convertToInt(startLines[i], -1);
                        if (startid > 0) {
                            for (int j = 0; j < endLines.length; j++) {
                                int endid = CommonFuction.convertToInt(endLines[j], -1);
                                if (endid > 0) {
                                    List<List<Integer>> temp = GrfAllEdge.createGraph(allLineNodes, allLineCane, startid, endid);
                                    transferLine.addAll(temp);
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(transferLine, new Comparator<List<Integer>>() {
            /*
             * int compare(Person p1, Person p2) 返回一个基本类型的整型，
             * 返回负数表示：p1 小于p2，
             * 返回0 表示：p1和p2相等，
             * 返回正数表示：p1大于p2
             */
            public int compare(List<Integer> p1, List<Integer> p2) {
                //按照换乘次数
                if (p1.size() > p2.size()) {
                    return 1;
                }
                if (p1.size() == p2.size()) {
                    return 0;
                }
                return -1;
            }
        });

        if (start != null || end != null) {
            //找出所有路径
            Log.d(TAG, "getReminderLines find all line = " + transferLine.size() + " start = " + start.getCname() + " end = " + end.getCname());
            lastLinesLast = PathSerachUtil.getLastRecomendLines(PathSerachUtil.getAllLineStation(mLineInfoList, transferLine, start, end));//查询最终线路
        }
        transferLine.clear();
        return lastLinesLast;
    }

    public static Map<Integer, Integer> getLineAllLined(List<StationInfo> list) {
        Map<Integer, Integer> listStr = new HashMap<Integer, Integer>();
        if (list == null && list.size() <= 0)
            return listStr;
        StringBuffer str = new StringBuffer();
        for (StationInfo stationInfo : list) {
            String lineList[] = stationInfo.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
            int size = lineList.length;
            for (int i = 0; i < size; i++) {
                int line = CommonFuction.convertToInt(lineList[i], -1);
                if (!listStr.containsKey(line) && line != stationInfo.lineid) {
                    listStr.put(line, line);
                    str.append(lineList[i] + "  ");
                }
            }
        }
        //Log.d(TAG, "getLineAllLined lineid = " + list.get(0).lineid + " all lined = " + str);
        return listStr;
    }
}
