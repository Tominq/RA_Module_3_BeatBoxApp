package demosocket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestCas3 {
    Socket chatSocket = new Socket("192.168.1.75", 5000);
    InputStreamReader in = new InputStreamReader(chatSocket.getInputStream());
    PrintWriter writer = new PrintWriter(chatSocket.getOutputStream());

    public TestCas3() throws IOException {
    }
}
