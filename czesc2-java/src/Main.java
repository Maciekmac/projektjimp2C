import view.MainWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Bezpieczne uruchomienie interfejsu graficznego
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true); // Pokazujemy okno na ekranie
        });
    }
}
