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
        JMenuItem itemSaveTxt = new JMenuItem("Zapisz Współrzędne (TXT)...");
        JMenuItem itemExportPng = new JMenuItem("Eksportuj Graf do obrazka (PNG)...");

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

        itemSaveTxt.addActionListener(e -> {
            if (model.getVertices().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nie ma nic do zapisania!", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (java.io.PrintWriter out = new java.io.PrintWriter(chooser.getSelectedFile())) {
                    for (Vertex v : model.getVertices()) {
                        out.println(v.getId() + " " + String.format(java.util.Locale.US, "%.6f", v.getX()) + " " + String.format(java.util.Locale.US, "%.6f", v.getY()));
                    }
                    JOptionPane.showMessageDialog(this, "Pomyślnie zapisano plik TXT!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        itemExportPng.addActionListener(e -> {
            if (model.getVertices().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nie ma grafu do wyeksportowania!", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File file = chooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new java.io.File(file.getAbsolutePath() + ".png");
                    }
                    // Rysowanie niewidzialnego płótna na obrazek
                    java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(canvas.getWidth(), canvas.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2d = image.createGraphics();
                    canvas.paintAll(g2d); // zrzuca wszystko co widać na płótnie
                    g2d.dispose();
                    javax.imageio.ImageIO.write(image, "png", file);
                    JOptionPane.showMessageDialog(this, "Wyeksportowano obrazek PNG!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Błąd eksportu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        menuFile.add(itemLoadTxt);
        menuFile.add(itemLoadBin);
        menuFile.add(itemLoadEdges);
        menuFile.addSeparator(); // pozioma kreska oddzielająca
        menuFile.add(itemSaveTxt);
        menuFile.add(itemExportPng);

        menuBar.add(menuFile);

        return menuBar;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Panel Narzędziowy"));
        panel.setPreferredSize(new Dimension(260, 0));

        // Zdalne sterowanie modułem C
        JLabel lblAlgo = new JLabel("Algorytm rozmieszczania:");
        lblAlgo.setAlignmentX(Component.CENTER_ALIGNMENT);
        String[] algos = {"Fruchterman-Reingold (Siłowy)", "Tutte Embedding (Barycentryczny)"};
        JComboBox<String> comboAlgo = new JComboBox<>(algos);
        comboAlgo.setMaximumSize(new Dimension(220, 25));
        comboAlgo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnRunEngine = new JButton("Przelicz graf (Silnik C)");
        btnRunEngine.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRunEngine.setMaximumSize(new Dimension(180, 30));
        btnRunEngine.setBackground(new Color(46, 204, 113)); // Zielony akcent
        btnRunEngine.setForeground(Color.WHITE);

        btnRunEngine.addActionListener(e -> {
            try {
                String cFolderPath = "C:\\Users\\dulny\\IdeaProjects\\projektjimp2C\\czesc1-C";
                java.io.File cDir = new java.io.File(cFolderPath);

                JOptionPane.showMessageDialog(this,
                        "Proszę wskaż plik tekstowy z listą krawędzi grafu.\n" +
                                "Silnik obliczy na jego podstawie nowy układ wierzchołków.",
                        "Wybór danych wejściowych",
                        JOptionPane.INFORMATION_MESSAGE);

                JFileChooser inputChooser = new JFileChooser(cDir);
                if (inputChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    java.io.File selectedFile = inputChooser.getSelectedFile();
                    java.io.File workingFile = new java.io.File(cDir, "working_input.txt");
                    java.nio.file.Files.copy(selectedFile.toPath(), workingFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // Komenda
                    String algoFlag = (comboAlgo.getSelectedIndex() == 1) ? " -a" : "";
                    String commandStr = "./programC working_input.txt" + algoFlag;

                    ProcessBuilder pb = new ProcessBuilder("wsl", "bash", "-c", commandStr);
                    pb.directory(cDir);
                    Process process = pb.start();

                    // CZYTANIE Z KONSOLI
                    java.util.Scanner sc = new java.util.Scanner(process.getInputStream());
                    model.clear();

                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Wierzcho[^:]+?\\s+(\\d+):\\s+X=([\\d.-]+),\\s+Y=([\\d.-]+)");
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        java.util.regex.Matcher matcher = pattern.matcher(line);

                        // Tylko regex! Bez starego "split"
                        if (matcher.find()) {
                            try {
                                int id = Integer.parseInt(matcher.group(1));
                                double x = Double.parseDouble(matcher.group(2));
                                double y = Double.parseDouble(matcher.group(3));
                                model.addVertex(new Vertex(id, x, y));
                            } catch (Exception ex) {
                                System.err.println("Błąd konwersji liczb: " + line);
                            }
                        }
                    }
                    process.waitFor();

                    // Wczytanie krawędzi (z pliku)
                    GraphParser.loadEdgesFromText(workingFile.getAbsolutePath(), model);

                    canvas.centerGraph();
                    canvas.repaint();
                    JOptionPane.showMessageDialog(this, "Sukces! Graf przeliczony i wczytany.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Zgrupowanie checkboxów, żeby ich kwadraciki były idealnie w pionie
        JCheckBox chkLabels = new JCheckBox("Pokaż ID wierzchołków", true);
        JCheckBox chkWeights = new JCheckBox("Pokaż Wagi krawędzi", true);
        chkLabels.addActionListener(e -> canvas.setShowLabels(chkLabels.isSelected()));
        chkWeights.addActionListener(e -> canvas.setShowWeights(chkWeights.isSelected()));

        JPanel checksWrapper = new JPanel();
        checksWrapper.setLayout(new BoxLayout(checksWrapper, BoxLayout.Y_AXIS));
        checksWrapper.add(chkLabels);
        checksWrapper.add(chkWeights);
        checksWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Skala i suwak
        JLabel lblZoom = new JLabel("Skala (Zoom): 100%");
        lblZoom.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSlider sliderZoom = new JSlider(50, 200, 100);
        sliderZoom.setAlignmentX(Component.CENTER_ALIGNMENT);
        sliderZoom.addChangeListener(e -> {
            double factor = sliderZoom.getValue() / 100.0;
            lblZoom.setText("Skala (Zoom): " + sliderZoom.getValue() + "%");
            canvas.setZoomFactor(factor);
        });

        // Przycisk wyśrodkowania (narzucamy stały, ładny rozmiar)
        JButton btnCenter = new JButton("Wyśrodkuj graf");
        btnCenter.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCenter.setMaximumSize(new Dimension(160, 30));
        btnCenter.addActionListener(e -> {
            canvas.centerGraph();
            if (currentVertex != null) {
                updateFieldsForVertex(currentVertex);
            }
        });

        // Pola edycji wierzchołka
        lblSelectedId = new JLabel("Zaznaczony wierzchołek: Brak");
        lblSelectedId.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblX = new JLabel("Współrzędna X:");
        lblX.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtX = new JTextField(5);
        txtX.setEnabled(false);
        txtX.setMaximumSize(new Dimension(160, 25)); // Blokujemy rozciąganie na pełną szerokość
        txtX.setHorizontalAlignment(JTextField.CENTER); // Liczby wpisują się na środku pola!

        JLabel lblY = new JLabel("Współrzędna Y:");
        lblY.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtY = new JTextField(5);
        txtY.setEnabled(false);
        txtY.setMaximumSize(new Dimension(160, 25)); // Blokujemy rozciąganie na pełną szerokość
        txtY.setHorizontalAlignment(JTextField.CENTER); // Liczby wpisują się na środku pola!

        // Przycisk zapisu (narzucamy IDENTYCZNY rozmiar co przycisk wyśrodkowania)
        JButton btnSaveCoords = new JButton("Zapisz Współrzędne");
        btnSaveCoords.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSaveCoords.setMaximumSize(new Dimension(160, 30));

        btnSaveCoords.addActionListener(e -> {
            if (currentVertex != null) {
                try {
                    double newX = Double.parseDouble(txtX.getText().replace(",", "."));
                    double newY = Double.parseDouble(txtY.getText().replace(",", "."));
                    double maxX = canvas.getWidth();
                    double maxY = canvas.getHeight();

                    if (newX < 0.0 || newX > maxX || newY < 0.0 || newY > maxY) {
                        JOptionPane.showMessageDialog(this,
                                "Współrzędne wykraczają poza widoczny obszar roboczy!\n" +
                                        "Wprowadź X w przedziale [0 - " + (int)maxX + "] oraz Y w przedziale [0 - " + (int)maxY + "].",
                                "Ostrzeżenie",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    currentVertex.setX(newX);
                    currentVertex.setY(newY);
                    canvas.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Wprowadź poprawne liczby!", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Finalne układanie w panelu
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblAlgo);
        panel.add(comboAlgo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnRunEngine);

        panel.add(Box.createVerticalStrut(20));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));

        panel.add(checksWrapper);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblZoom);
        panel.add(sliderZoom);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnCenter);

        panel.add(Box.createVerticalStrut(20));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));

        panel.add(lblSelectedId);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblX);
        panel.add(txtX);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblY);
        panel.add(txtY);
        panel.add(Box.createVerticalStrut(15));
        panel.add(btnSaveCoords);

        return panel;
    }

    // Odbieranie danych od płótna
    public void updateFieldsForVertex(Vertex v) {
        currentVertex = v;
        lblSelectedId.setText("Zaznaczony wierzchołek ID: " + v.getId());
        txtX.setText(String.format("%.2f", v.getX()).replace(",", "."));
        txtY.setText(String.format("%.2f", v.getY()).replace(",", "."));
        txtX.setEnabled(true);
        txtY.setEnabled(true);
    }
}