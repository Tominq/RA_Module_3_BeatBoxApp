package chatboxversion1;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SimpleChatClientA {
    private JTextField outgoing;

    private JTextArea incoming;
    private BufferedReader reader;
    private PrintWriter writer;

    /* call the methos network
        make gui and register a listener with the sned button
    * */
    public void go() {
        setUpNetworking();
        JScrollPane scroller = createScrollLabelTextArea();
        outgoing = new JTextField(20);

        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> sendMessage());

        JPanel mainPanel = new JPanel();
        mainPanel.add(scroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendBtn);

        ExecutorService excutor = Executors.newSingleThreadExecutor();
        excutor.execute(new IncomingReader());

        JFrame frame = new JFrame("Hiphop never die");
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(400, 350);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private JScrollPane createScrollLabelTextArea() {
        incoming = new JTextArea(15, 30);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane scroller = new JScrollPane(incoming);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroller;
    }

    /*  open a socketChannel to the server
        make a PrintWriter and assign to writer instance variable
    * */

    public void setUpNetworking() {
        try {
            InetSocketAddress severAddress = new InetSocketAddress("192.168.1.75", 5000);
            SocketChannel socketChannel = SocketChannel.open(severAddress);
            reader = new BufferedReader(Channels.newReader(socketChannel, UTF_8));
            writer = new PrintWriter(Channels.newWriter(socketChannel, UTF_8));
            System.out.println("networking established");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*  get the text from the text field
     *   send it to teh server using teh writer (a PrintWriter)
     * */
    private void sendMessage() {
        writer.println(outgoing.getText());
        writer.flush();
        outgoing.setText("");
        outgoing.requestFocus();
    }

    class IncomingReader implements Runnable {

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("read" + message);
                    incoming.append(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new SimpleChatClientA().go();
    }
}
