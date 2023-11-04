package demosocket;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TestCase2 {
    SocketAddress severAddr = new InetSocketAddress("192.168.1.75", 5000);
    SocketChannel socketChannel = SocketChannel.open(severAddr);

    Writer writer = Channels.newWriter(socketChannel, StandardCharsets.UTF_8);

//    make a PrintWriter and write(print) something

    PrintWriter printWriter = new PrintWriter(writer);


    public TestCase2() throws IOException {
        printWriter.println("Say hello World!");
        printWriter.flush();
    }
}
