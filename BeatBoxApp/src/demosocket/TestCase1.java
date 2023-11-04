package demosocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TestCase1 {

    /* Make a connection to the server
        "192.168.1.75" is a IP address for "localhost",
        5000 is TCP port
        you need to open a SocketChannel that connects to this address
    * */

    SocketAddress severAddr = new InetSocketAddress("192.168.1.75", 5000);
    SocketChannel socketChannel = SocketChannel.open(severAddr);

    /* create or get a reader from the connection
     * */
    Reader reader = Channels.newReader(socketChannel, StandardCharsets.UTF_8);

    /* Make a BufferReader and read
        Chain the BufferReader to the Reader (which is from our SocketChannel)
     * */

    BufferedReader bufferedReader = new BufferedReader(reader);
    String message = bufferedReader.readLine();
    
    public TestCase1() throws IOException {
    }
}
