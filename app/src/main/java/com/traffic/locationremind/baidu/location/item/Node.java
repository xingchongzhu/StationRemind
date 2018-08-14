package com.traffic.locationremind.baidu.location.item;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joker on 2017/5/27.
 */
public class Node<T> implements Serializable {

    private Node parentNode;
    private T nodeEntity;
    private List<Node> childNodes;

    public Node (T nodeEntity){
        this.nodeEntity=nodeEntity;
    }

    public Node (){}

    public void addChildNode(Node childNode){
        childNode.setParentNode(this);
        if ( this.childNodes==null){
            this.childNodes = new ArrayList<Node>();
        }
        this.childNodes.add(childNode);
    }

    public void removeChildNode(Node childNode){
        if (this.childNodes!=null){
            this.childNodes.remove(childNode);
        }
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public T getNodeEntity() {
        return nodeEntity;
    }

    public void setNodeEntity(T nodeEntity) {
        this.nodeEntity = nodeEntity;
    }

    public void setChildNodes(List<Node> childNodes) {
        this.childNodes = childNodes;
    }

    public List<Node> getChildNodes() {
        return childNodes;
    }
}
