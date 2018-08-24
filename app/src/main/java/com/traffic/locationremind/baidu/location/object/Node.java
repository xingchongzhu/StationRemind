package com.traffic.locationremind.baidu.location.object;

import java.util.ArrayList;

/* 表示一个节点以及和这个节点相连的所有节点 */
public class Node
{
    public int name ;
    public ArrayList<Node> relationNodes = new ArrayList<Node>();

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public ArrayList<Node> getRelationNodes() {
        return relationNodes;
    }

    public void setRelationNodes(ArrayList<Node> relationNodes) {
        this.relationNodes = relationNodes;
    }
}
