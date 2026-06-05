package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GraphParser {

    // Wczytywanie współrzędnych tekstowych (ID X Y)
    public static void loadVerticesFromText(String filePath, GraphModel model) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    throw new IOException("Błąd formatu! Oczekiwano dokładnie 3 wartości, a wykryto śmieci w linii: " + line);
                }

                try {
                    int id = Integer.parseInt(parts[0]);
                    double x = Double.parseDouble(parts[1].replace(",", "."));
                    double y = Double.parseDouble(parts[2].replace(",", "."));
                    model.addVertex(new Vertex(id, x, y));
                } catch (NumberFormatException e) {
                    throw new IOException("Wykryto litery w miejscu wspolrzednych numerycznych w linii: " + line);
                }
            }
        }
    }

    // Wczytywanie krawędzi tekstowych (NAZWA V1 V2 WAGA)
    public static void loadEdgesFromText(String filePath, GraphModel model) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length != 4) {
                    throw new IOException("Błąd formatu! Oczekiwano dokładnie 4 wartości, a wykryto śmieci w linii: " + line);
                }

                try {
                    int v1_id = Integer.parseInt(parts[1]);
                    int v2_id = Integer.parseInt(parts[2]);
                    double weight = Double.parseDouble(parts[3].replace(",", "."));

                    Vertex vertex1 = model.getVertexById(v1_id);
                    Vertex vertex2 = model.getVertexById(v2_id);

                    if (vertex1 != null && vertex2 != null) {
                        model.addEdge(new Edge(vertex1, vertex2, weight));
                    } else {
                        throw new IOException("Krawędź odwołuje się do nieistniejących ID wierzchołków w linii: " + line);
                    }
                } catch (NumberFormatException e) {
                    throw new IOException("Wykryto nieprawidłowy format liczbowy podczas wczytywania krawędzi w linii: " + line);
                }
            }
        }
    }

    // Wczytywanie współrzędnych z pliku binarnego pochodzącego z C
    public static void loadVerticesFromBinary(String filePath, GraphModel model) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        ByteBuffer buffer = ByteBuffer.wrap(fileBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Format Little-Endian z Windows/C

        int vertexCount = buffer.getInt();

        for (int i = 0; i < vertexCount; i++) {
            int id = buffer.getInt();
            buffer.getInt(); // Przeskok 4 bajtów paddingu kompilatora C
            double x = buffer.getDouble();
            double y = buffer.getDouble();
            buffer.getDouble(); // Przeskok pomocniczego dx z C
            buffer.getDouble(); // Przeskok pomocniczego dy z C

            model.addVertex(new Vertex(id, x, y));
        }
    }
}

