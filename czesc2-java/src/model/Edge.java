package model;

public class Edge {
    private Vertex v1;
    private Vertex v2;
    private double weight;

    public Edge(Vertex v1, Vertex v2, double weight) {
        this.v1 = v1;
        this.v2 = v2;
        this.weight = weight;
    }

    public Vertex getV1() { return v1; }
    public Vertex getV2() { return v2; }
    public double getWeight() { return weight; }
}
