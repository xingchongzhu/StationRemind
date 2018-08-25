package com.traffic.locationremind.baidu.location.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.traffic.locationremind.baidu.location.listener.SearchResultListener;
import com.traffic.locationremind.baidu.location.object.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


public class SearchPath extends AsyncTask<String, List<Integer>, List<List<Integer>>> {
    /* 临时保存路径节点的栈 */
    public Stack<Node> stack = new Stack<Node>();
    /* 存储路径的集合 */
    public ArrayList<List<Integer>> sers = new ArrayList<>();
    private static final int maxchange = 5;
    private SearchResultListener mSearchResultListener;
    private boolean stopRun = false;
    private int origin=0,  goal=0;
    private int[][] nodeRalation;
    //onPreExecute用于异步处理前的操作
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public SearchPath(SearchResultListener mSearchResultListener,int origin, int goal, int[][] nodeRalation) {
        this.mSearchResultListener = mSearchResultListener;
        this.origin = origin;
        this.goal = goal;
        this.nodeRalation = nodeRalation;
    }

    //在doInBackground方法中进行异步任务的处理.
    @Override
    protected List<List<Integer>> doInBackground(String... params) {
        return serach(origin, goal, nodeRalation);
    }

    @Override
    protected void onProgressUpdate(List<Integer>... values) {
        super.onProgressUpdate(values);
        notificationUpdate((List<Integer>)values[0]);
    }

    //onPostExecute用于UI的更新.此方法的参数为doInBackground方法返回的值.
    @Override
    protected void onPostExecute(List<List<Integer>> list) {
        super.onPostExecute(list);
        stopRun = false;
        stack.clear();
        stack = null;
        list.clear();
        AsyncTaskManager.getInstance().removeGeekRunnable(this);
    }

    public SearchPath() {

    }

    public boolean getRunState() {
        return stopRun;
    }

    public void setStopRunState(boolean state) {
        stopRun = state;
    }

    /* 判断节点是否在栈中 */
    public boolean isNodeInStack(Node node) {
        Iterator<Node> it = stack.iterator();
        while (it.hasNext()) {
            Node node1 = (Node) it.next();
            if (node == node1)
                return true;
        }
        return false;
    }

    public void notificationUpdate(List<Integer> list) {
        if (mSearchResultListener != null)
            mSearchResultListener.updateSingleResult(list);
    }

    /* 此时栈中的节点组成一条所求路径，转储并打印输出 */
    public void showAndSavePath() {
        StringBuffer str = new StringBuffer();
        List<Integer> list = new ArrayList<>();
        Object[] o = stack.toArray();
        if (o.length >= maxchange) {
            return;
        }
        for (int i = 0; i < o.length; i++) {
            Node nNode = (Node) o[i];
            list.add(nNode.getName());
            if (i < (o.length - 1)) {
                str.append(nNode.getName() + "->");
                //System.out.print(nNode.getName() + "->");
            } else {
                str.append(nNode.getName() + "->");
                System.out.print(nNode.getName());
            }
        }
        for (List<Integer> entry : sers) {
            if (entry.toString().equals(list.toString())) {
                return;
            }
        }
        Log.d("zxc01", "showAndSavePath str = " + str.toString());
        sers.add(list); /* 转储 */
        publishProgress(list);
    }

    /*
     * 寻找路径的方法
     * cNode: 当前的起始节点currentNode
     * pNode: 当前起始节点的上一节点previousNode
     * sNode: 最初的起始节点startNode
     * eNode: 终点endNode
     */
    public boolean getPaths(Node cNode, Node pNode, Node sNode, Node eNode) {
        if (stopRun) {
            return false;
        }
        Node nNode = null;
        /* 如果符合条件判断说明出现环路，不能再顺着该路径继续寻路，返回false */
        if (cNode != null && pNode != null && cNode == pNode)
            return false;

        if (cNode != null) {
            int i = 0;
            /* 起始节点入栈 */
            stack.push(cNode);
            /* 如果该起始节点就是终点，说明找到一条路径 */
            if (cNode == eNode) {
                /* 转储并打印输出该路径，返回true */
                showAndSavePath();
                return true;
            }
            /* 如果不是,继续寻路 */
            else {
                /*
                 * 从与当前起始节点cNode有连接关系的节点集中按顺序遍历得到一个节点
                 * 作为下一次递归寻路时的起始节点
                 */
                nNode = cNode.getRelationNodes().get(i);
                while (nNode != null) {
                    /*
                     * 如果nNode是最初的起始节点或者nNode就是cNode的上一节点或者nNode已经在栈中 ，
                     * 说明产生环路 ，应重新在与当前起始节点有连接关系的节点集中寻找nNode
                     */
                    if (pNode != null && (nNode == sNode || nNode == pNode || isNodeInStack(nNode))) {
                        i++;
                        if (i >= cNode.getRelationNodes().size())
                            nNode = null;
                        else
                            nNode = cNode.getRelationNodes().get(i);
                        continue;
                    }
                    /* 以nNode为新的起始节点，当前起始节点cNode为上一节点，递归调用寻路方法 */
                    if (getPaths(nNode, cNode, sNode, eNode))/* 递归调用 */ {
                        /* 如果找到一条路径，则弹出栈顶节点 */
                        stack.pop();
                    }
                    /* 继续在与cNode有连接关系的节点集中测试nNode */
                    i++;
                    if (i >= cNode.getRelationNodes().size())
                        nNode = null;
                    else
                        nNode = cNode.getRelationNodes().get(i);
                }
                /*
                 * 当遍历完所有与cNode有连接关系的节点后，
                 * 说明在以cNode为起始节点到终点的路径已经全部找到
                 */
                stack.pop();
                return false;
            }
        } else
            return false;
    }

    public void main(int[][] nodeRalation) {
        /* 定义节点数组 */
        Node[] node = new Node[nodeRalation.length];

        for (int i = 0; i < nodeRalation.length; i++) {
            node[i] = new Node();
            node[i].setName(i);
        }

        /* 定义与节点相关联的节点集合 */
        for (int i = 0; i < nodeRalation.length; i++) {
            ArrayList<Node> List = new ArrayList<Node>();

            for (int j = 0; j < nodeRalation[i].length; j++) {
                List.add(node[nodeRalation[i][j]]);
            }
            node[i].setRelationNodes(List);
            List = null;  //释放内存
        }
        /* 开始搜索所有路径 */
        getPaths(node[1], null, node[0], node[4]);
    }

    public List<List<Integer>> serach(int origin, int goal, int[][] nodeRalation) {
        Log.d("zxc01", "findpath start = " + origin + " end = " + goal);
        /* 定义节点数组 */
        Node[] node = new Node[nodeRalation.length];

        for (int i = 0; i < nodeRalation.length; i++) {
            node[i] = new Node();
            node[i].setName(i);
        }

        /* 定义与节点相关联的节点集合 */
        for (int i = 0; i < nodeRalation.length; i++) {
            ArrayList<Node> List = new ArrayList<Node>();

            for (int j = 0; j < nodeRalation[i].length; j++) {
                List.add(node[nodeRalation[i][j]]);
            }
            node[i].setRelationNodes(List);
            List = null;  //释放内存
        }
        /* 开始搜索所有路径 */
        getPaths(node[origin], node[origin - 1], node[0], node[goal]);

        return sers;
    }
}
