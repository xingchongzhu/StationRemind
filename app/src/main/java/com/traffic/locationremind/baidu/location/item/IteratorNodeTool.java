package com.traffic.locationremind.baidu.location.item;

import android.util.Log;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author lei
 */
public class IteratorNodeTool {
    String TAG = "IteratorNodeTool";
    public List<List<StationInfo>> pathMap = new ArrayList();//记录所有从根节点到叶子结点的路径

    private void print(List lst) {
        Iterator it = lst.iterator();
        Log.d(TAG, "----------start---------------");
        while (it.hasNext()) {
            Node n = (Node) it.next();
            StationInfo stationInfo = (StationInfo) n.getNodeEntity();
            Log.d(TAG, stationInfo.lineid + " " + stationInfo.getCname());
        }
        Log.d(TAG, "----------end--------------");
    }

    public void iteratorNode(Node n, Stack<Node> pathstack) {
        pathstack.push(n);//入栈
        List childlist = n.getChildNodes();
        if (childlist == null)//没有孩子 说明是叶子结点
        {
            List lst = new ArrayList();
            Iterator stackIt = pathstack.iterator();
            while (stackIt.hasNext()) {
                lst.add(stackIt.next());

            }
            //print(lst);//打印路径
            pathMap.add(lst);//保存路径信息
            return;
        } else {
            Iterator it = childlist.iterator();
            while (it.hasNext()) {
                Node child = (Node) it.next();
                iteratorNode(child, pathstack);//深度优先 进入递归
                pathstack.pop();//回溯时候出栈
            }

        }

    }
}
