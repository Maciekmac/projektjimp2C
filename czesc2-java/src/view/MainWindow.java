package view;

import model.GraphModel;
import model.GraphParser;
import model.Vertex;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private GraphCanvas canvas;
    private GraphModel model;

    // Elementy panelu bocznego, które muszą być aktualizowane
    private JTextField txtX, txtY;
    private JLabel lblSelectedId;
    private Vertex currentVertex = null;

    public MainWindow() {
        model = new GraphModel();
        // Przekazujemy 'this' (czyli to okno) do płótna, by mogły się komunikować
        canvas = new GraphCanvas(model, this);

        setTitle("Wizualizacja Grafu - Projekt Java Swing");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.EAST);
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("Plik");

        JMenuItem itemLoadTxt = new JMenuItem("Wczytaj Współrzędne (TXT)...");
        JMenuItem itemLoadBin = new JMenuItem("Wczytaj Współrzędne (BIN)...");
        JMenuItem itemLoadEdges = new JMenuItem("Wczytaj Krawędzie (TXT)...");

        itemLoadTxt.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    model.clear();
                    GraphParser.loadVerticesFromText(chooser.getSelectedFile().getAbsolutePath(), model);
                    JOptionPane.showMessageDialog(this, "Pomyślnie wczytano wierzchołki (TXT)!");
                    canvas.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Błąd odczytu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        itemLoadBin.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    model.clear();
                    GraphParser.loadVerticesFromBinary(chooser.getSelectedFile().getAbsolutePath(), model);
                    JOptionPane.showMessageDialog(this, "Pomyślnie wczytano zrzut pamięci z C!");
                    canvas.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Błąd odczytu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        itemLoadEdges.addActionListener(e -> {
            if (model.getVertices().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Najpierw wczytaj wierzchołki!", "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    GraphParser.loadEdgesFromText(chooser.getSelectedFile().getAbsolutePath(), model);
                    JOptionPane.showMessageDialog(this, "Krawędzie połączone poprawnie!");
                    canvas.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Błąd krawędzi: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        menuFile.add(itemLoadTxt);
        menuFile.add(itemLoadBin);
        menuFile.addSeparator();
        menuFile.add(itemLoadEdges);
        menuBar.add(menuFile);

        return menuBar;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Panel Narzędziowy"));
        panel.setPreferredSize(new Dimension(220, 0));

        JCheckBox chkLabels = new JCheckBox("Pokaż ID wierzchołków", true);
        JCheckBox chkWeights = new JCheckBox("Pokaż Wagi krawędzi", true);

        chkLabels.addActionListener(e -> canvas.setShowLabels(chkLabels.isSelected()));
        chkWeights.addActionListener(e -> canvas.setShowWeights(chkWeights.isSelected()));

        JLabel lblZoom = new JLabel("Skala (Zoom): 100%");
        JSlider sliderZoom = new JSlider(50, 200, 100);

        sliderZoom.addChangeListener(e -> {
            double factor = sliderZoom.getValue() / 100.0;
            lblZoom.setText("Skala (Zoom): " + sliderZoom.getValue() + "%");
            canvas.setZoomFactor(factor);
        });

        // --- NOWE: Pola edycji wierzchołka ---
        lblSelectedId = new JLabel("Zaznaczony wierzchołek: Brak");
        txtX = new JTextField(5);
        txtY = new JTextField(5);
        txtX.setEnabled(false); // Zablokowane, dopóki czegoś nie klikniesz
        txtY.setEnabled(false);
        txtX.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        txtY.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JButton btnSaveCoords = new JButton("Zapisz Współrzędne");

        // Akcja zapisywania z palca
        btnSaveCoords.addActionListener(e -> {
            if (currentVertex != null) {
                try {
                    currentVertex.setX(Double.parseDouble(txtX.getText().replace(",", ".")));
                    currentVertex.setY(Double.parseDouble(txtY.getText().replace(",", ".")));
                    canvas.repaint(); // Po zapisie przerysowujemy ekran
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Wprowadź poprawne liczby!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(Box.createVerticalStrut(10));
        panel.add(chkLabels);
        panel.add(chkWeights);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblZoom);
        panel.add(sliderZoom);

        // Dodajemy nowe elementy na dół panelu
        panel.add(Box.createVerticalStrut(20));
        panel.add(new JSeparator()); // Cienka linia oddzielająca
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblSelectedId);
        panel.add(new JLabel("Współrzędna X:"));
        panel.add(txtX);
        panel.add(new JLabel("Współrzędna Y:"));
        panel.add(txtY);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnSaveCoords);

        return panel;
    }

    // --- NOWA METODA: Odbiera dane od płótna ---
    public void updateFieldsForVertex(Vertex v) {
        currentVertex = v;
        lblSelectedId.setText("Zaznaczony wierzchołek ID: " + v.getId());
        txtX.setText(String.format("%.2f", v.getX()).replace(",", "."));
        txtY.setText(String.format("%.2f", v.getY()).replace(",", "."));
        txtX.setEnabled(true);
        txtY.setEnabled(true);
    }
}