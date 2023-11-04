package demosocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class DailyAdviceServer {
    final private String[] adviceList = {
            "Take small botes",
            "look fat",
            "important",
            "just for today",
            "hihi",
            "ngu quá đi"
    };

    private final Random random = new Random();

    public void go() {
        try (ServerSocketChannel severChannel = ServerSocketChannel.open()) {
            severChannel.bind(new InetSocketAddress(5000));
            while (severChannel.isOpen()) {
                SocketChannel clientChannel = severChannel.accept();
                PrintWriter writer = new PrintWriter(Channels.newOutputStream(clientChannel));

                String advice = getAdvice();
                writer.println(advice);
                writer.close();
                System.out.println(advice);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getAdvice() {
        int nextAdvice = random.nextInt(adviceList.length);
        return adviceList[nextAdvice];
    }

    public static void main(String[] args) {
        new DailyAdviceServer().go();
    }
}
