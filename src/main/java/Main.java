import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // здесь создайте сервер, который отвечал бы на нужные запросы
        // слушать он должен порт 8989
        // отвечать на запросы /{word} -> возвращённое значение метода search(word) в JSON-формате

        final int PORT = 8989;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Старт сервера на порту " + PORT + "...");
            //noinspection InfiniteLoopStatement
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {
                    String word = in.readLine();
                    BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
                    String gsonText = engine.search(word).toString();
                    out.println(gsonText);
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }
}