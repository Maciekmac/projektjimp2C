package view;

import model.Edge;
import model.GraphModel;
import model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GraphCanvas extends JPanel {
    private GraphModel model;
    private MainWindow mainWindow; // Dodajemy referencję do okna, by przesyłać mu dane
    private double zoomFactor = 1.0;
    private boolean showLabels = true;
    private boolean showWeights = true;

    private Vertex selectedVertex = null; // Pamięta, który wierzchołek kliknęliśmy

    // Konstruktor teraz przyjmuje też MainWindow
    public GraphCanvas(GraphModel model, MainWindow mainWindow) {
        this.model = model;
        this.mainWindow = mainWindow;
        setBackground(Color.WHITE);

        // --- KONTROLER MYSZY ---
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedVertex = null;
                // Szukamy, czy kliknięto w któryś wierzchołek (uwzględniając zoom)
                for (Vertex v : model.getVertices()) {
                    double vx = v.getX() * zoomFactor;
                    double vy = v.getY() * zoomFactor;
                    // Jeśli odległość myszki od środka kółka jest mniejsza niż 15 pikseli to trafiliśmy!
                    if (Math.hypot(e.getX() - vx, e.getY() - vy) <= 15) {
                        selectedVertex = v;
                        mainWindow.updateFieldsForVertex(v); // Wysyłamy dane do panelu bocznego
                        break;
                    }
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedVertex != null) {
                    // Aktualizujemy współrzędne w obiekcie Modelu
                    selectedVertex.setX(e.getX() / zoomFactor);
                    selectedVertex.setY(e.getY() / zoomFactor);
                    mainWindow.updateFieldsForVertex(selectedVertex); // Aktualizujemy tekst z boku
                    repaint(); // Natychmiast przerysowujemy ekran!
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        repaint();
    }

    public void setShowLabels(boolean showLabels) {
        this.showLabels = showLabels;
        repaint();
    }

    public void setShowWeights(boolean showWeights) {
        this.showWeights = showWeights;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Rysowanie krawędzi
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2));
        for (Edge edge : model.getEdges()) {
            int x1 = (int) (edge.getV1().getX() * zoomFactor);
            int y1 = (int) (edge.getV1().getY() * zoomFactor);
            int x2 = (int) (edge.getV2().getX() * zoomFactor);
            int y2 = (int) (edge.getV2().getY() * zoomFactor);
            g2.drawLine(x1, y1, x2, y2);

            if (showWeights) {
                g2.setColor(Color.RED);
                g2.drawString(String.format("%.1f", edge.getWeight()), (x1 + x2) / 2, (y1 + y2) / 2 - 5);
                g2.setColor(Color.GRAY);
            }
        }

        // 2. Rysowanie wierzchołków
        int radius = 10;
        for (Vertex v : model.getVertices()) {
            int cx = (int) (v.getX() * zoomFactor);
            int cy = (int) (v.getY() * zoomFactor);

            // Zmieniamy kolor, jeśli wierzchołek jest zaznaczony!
            if (v == selectedVertex) {
                g2.setColor(Color.ORANGE);
            } else {
                g2.setColor(new Color(41, 128, 185));
            }

            g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
            g2.setColor(Color.BLACK);
            g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

            if (showLabels) {
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString(String.valueOf(v.getId()), cx - 4, cy + 4);
            }
        }
    }
}
