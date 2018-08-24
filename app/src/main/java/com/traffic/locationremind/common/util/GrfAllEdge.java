package com.traffic.locationremind.common.util;

import android.util.Log;

import java.util.*;

/**
 * 无向无权无环图<br>
 * 寻找起点到终点的所有路径
 */
public class GrfAllEdge {
    final static String TAG = "GrfAllEdge";
    // 图的顶点总数
    private int total;
    // 各顶点基本信息
    private Integer[] nodes;
    // 图的邻接矩阵
    private int[][] matirx;
    private boolean stop = false;
    private final int MAXTRANSFERNUM = 5;//最多换乘3次,包含起点终点
    //private static GrfAllEdge grf;


    private List<List<Integer>> allLine = new ArrayList<List<Integer>>();

    public GrfAllEdge(int total, Integer[] nodes) {
        this.total = total;
        this.nodes = nodes;
        this.matirx = new int[total][total];
    }

    private void printStack(Stack<Integer> stack, int k) {
        //str.delete(0, str.length());
        if(stack == null && stack.size() <= 0){
            return ;
        }
        List<Integer> list = new ArrayList<>();
        for (Integer i : stack) {
            //str.append(this.nodes[i] + ",");
            list.add(this.nodes[i]);
        }
        allLine.add(list);//保存查询到的路线
        //str.append(this.nodes[k] + ",");
        list.add(this.nodes[k]);
        //Log.d(TAG,str.toString());
    }

    /**
     * 寻找起点到终点的所有路径
     *
     *            紧挨着栈顶的下边的元素
     * @param goal
     *            目标
     * @param stack
     */
    private void dfsStack( int goal, Stack<Integer> stack) {
        if (stack.isEmpty() || stop) {
            return;
        }

        // 访问栈顶元素，但不弹出
        int k = stack.peek().intValue();
        if (k == goal) {
            Log.d(TAG, "\n起点与终点不能相同");
            return;
        }

        // 对栈顶的邻接点依次递归调用，进行深度遍历
        for (int i = 1; i < this.total; i++) {
            // 有边，并且不在左上到右下的中心线上
            if (this.matirx[k][i] == 1 && i != k) {
                // 排除环路
                if (stack.contains(i)) {
                    // 由某顶点A，深度访问其邻接点B时，由于是无向图，所以存在B到A的路径，在环路中，我们要排除这种情况
                    // 严格的请，这种情况也是一个环
                    //if (i != uk) {
                        //Log.d(TAG, "\n有环:");
                        //this.printStack(stack, i);
                    //}
                    continue;
                }

                // 打印路径
                if (i == goal) {
                    if(stack.size() < MAXTRANSFERNUM && stack.size() >= 0) {
                        //Log.d(TAG, "\n路径:");
                        this.printStack(stack, i);
                    }
                    continue;
                }
                // 深度遍历
                stack.push(i);
                dfsStack( goal, stack);
            }
        }

        stack.pop();
    }

    public static void printMatrix(int total,int[][] matirx) {
        StringBuffer str = new StringBuffer();
        Log.d(TAG, "----------------- matrix -----------------");
        str.delete(0, str.length());
        str.append(" |");
        for (int i = 0; i < total; i++) {
            str.append(i + ",");
        }
        Log.d(TAG, str.toString());
        str.delete(0, str.length());
        for (int i = 0; i < total; i++) {
            str.append("---");
        }
        Log.d(TAG, str.toString());
        for (int i = 0; i < total; i++) {
            str.delete(0, str.length());
            //Log.d(TAG," " + this.nodes[i] + "|");
            str.append(i + "|");
            for (int j = 0; j < total; j++) {
                str.append(matirx[i][j] + ",");
                //Log.d(TAG,this.matirx[i][j] + "-");
            }
            Log.d(TAG, str.toString());
        }
        Log.d("zxc001", "----------------- matrix -----------------");
    }

    // 设置[i][i]位置处的元素值为0，0表示图中的定点i未被访问，1表示图中的定点i已被访问
    private void resetVisited() {
        for (int i = 0; i < this.total; i++) {
            this.matirx[i][i] = 0;
        }
    }

    // 初始化图数据
    private void initGrf(Map<Integer, Map<Integer, Integer>> allLineCane) {
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : allLineCane.entrySet()) {
            for (Map.Entry<Integer, Integer> value : entry.getValue().entrySet()) {
                this.matirx[entry.getKey()][value.getValue()] = 1;
            }
        }
    }

    public GrfAllEdge(int total, Integer[] nodes,int[][] matirx) {
        this.total = total;
        this.nodes = nodes;
        this.matirx = matirx;
    }

    public List<List<Integer>> serach(int origin, int goal){
        //Log.d(TAG, "\n------ 寻找起点到终点的所有路径开始 ------origin = " + origin + " goal = " + goal);
        Stack<Integer> stack = new Stack<>();
        stack.push(origin);
        Log.d(TAG, "查询 origin = "+origin+" 到 goal = "+goal);
        dfsStack(goal, stack);
        //Log.d(TAG, "\n------ 寻找起点到终点的所有路径结束 ------");
        Log.d(TAG, "总共寻找到换乘小于4条线路径总共为 grf.allLine.lenght = "+allLine.size());
        Collections.sort(allLine, new Comparator<List<Integer>>(){
            public int compare(List<Integer> p1, List<Integer> p2) {
                //按照换乘次数
                if(p1.size() > p2.size()){
                    return 1;
                }
                if(p1.size() == p2.size()){
                    return 0;
                }
                return -1;
            }
        });
        return allLine;
    }

    public static List<List<Integer>> createGraph(Integer[] nodes, Map<Integer, Map<Integer, Integer>> allLineCane, int origin, int goal) {
        GrfAllEdge grf = new GrfAllEdge(nodes.length, nodes);//从一开始加
        grf.stop = false;
        grf.allLine.clear();
        grf.resetVisited();
        grf.initGrf(allLineCane);
        //grf.printMatrix();

        //Log.d(TAG, "\n------ 寻找起点到终点的所有路径开始 ------origin = " + origin + " goal = " + goal);
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(origin);
        grf.dfsStack( goal, stack);
        //Log.d(TAG, "\n------ 寻找起点到终点的所有路径结束 ------");
        Log.d(TAG, "总共寻找到换乘小于4条线路径总共为 grf.allLine.lenght = "+grf.allLine.size());
        Collections.sort(grf.allLine, new Comparator<List<Integer>>(){
            /*
             * int compare(Person p1, Person p2) 返回一个基本类型的整型，
             * 返回负数表示：p1 小于p2，
             * 返回0 表示：p1和p2相等，
             * 返回正数表示：p1大于p2
             */
            public int compare(List<Integer> p1, List<Integer> p2) {
                //按照换乘次数
                if(p1.size() > p2.size()){
                    return 1;
                }
                if(p1.size() == p2.size()){
                    return 0;
                }
                return -1;
            }
        });
        int n =0;
        for(List<Integer> list:grf.allLine){
            Log.d(TAG, (n++)+" list = "+list);
        }
        return grf.allLine;
    }

}