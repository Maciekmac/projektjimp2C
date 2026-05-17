package model;

import java.util.ArrayList;
import java.util.List;

public class GraphModel {
    private List<Vertex> vertices;
    private List<Edge> edges;

    public GraphModel() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public List<Vertex> getVertices() { return vertices; }
    public List<Edge> getEdges() { return edges; }

    public void addVertex(Vertex v) {
        vertices.add(v);
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public void clear() {
        vertices.clear();
        edges.clear();
    }

    // szuka wierzchołka po jego ID
    public Vertex getVertexById(int id) {
        for (Vertex v : vertices) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }
}
