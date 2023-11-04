package beatboxfinal;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.sound.midi.ShortMessage.*;

public class BeatBox {
    private JList<String> incomingList;
    private JTextArea userMessage;
    //    we store the checkboxs in an ArrayList
    public ArrayList<JCheckBox> checkboxList;
    private Vector<String> listVector = new Vector<>();
    private HashMap<String, boolean[]> otherSeqsMap = new HashMap<>();
    private String userName;
    private int nextNum;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Sequencer sequencer;
    public Sequence sequence;
    public Track track;

    /* There are teh names of the instrucment, as a String array,
        for building teh GUi labels (on each row)
     * */
    String[] instrucmentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi=Hat", "Aucostic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};

    /* drum keys
       drum channel is like a piano
    *
    * */
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        System.out.println("Start Beatbox App" + Arrays.toString(args));

        if (args.length >= 0) {
            new BeatBox().startUp("ToMinq ");
        } else {
            System.out.println("Please provide a username as a command-line argument.");
        }
    }

    /* set up the networking I/O,
        using Socket instead of Channels
     * */
    public void startUp(String name) {
        userName = name;
        // open connection to teh server
        try {
            Socket socket = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new RemoteReader());
        } catch (Exception e) {
            System.out.println("Couldn't connect-you'll have to play alone");
        }
        setUpMidi();
        buildGui();
    }

    /* build GUI
        checkboxs component
        text area that displays incoming messages
        text field
     * */
    public void buildGui() {
        JFrame frame = new JFrame("Hiphop never dieeeeeeee !");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        /*  Empty border give us a margin between
            the edges of the panel and where the components are placed
         * */
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

//  create button start
        JButton start = new JButton("Start");
        start.addActionListener(e -> buildTractAndStart());
        buttonBox.add(start);

//  create button stop

        JButton stop = new JButton("Stop");
        stop.addActionListener(e -> sequencer.stop());
        buttonBox.add(stop);

//  create button tempo up

        /* the default tempo is 1.0
            then up/down 3% per click
        * */
        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(e -> changeTempo(1.03f));
        buttonBox.add(upTempo);

//  create button tempo down
        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(e -> changeTempo(0.97f));
        buttonBox.add(downTempo);

//  create button send message
        JButton sendIt = new JButton("Send It");
        sendIt.addActionListener(e -> sendMessageAndTracks());
        buttonBox.add(sendIt);

//  text area, for the user to tye their messages
        userMessage = new JTextArea();
        userMessage.setLineWrap(true);
        userMessage.setWrapStyleWord(true);
        JScrollPane messsageScroller = new JScrollPane(userMessage);
        buttonBox.add(messsageScroller);

//
        incomingList = new JList<>();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (String instrumentName :
                instrucmentNames) {
            JLabel instrumentLabel = new JLabel(instrumentName);
            instrumentLabel.setBorder(BorderFactory.createEmptyBorder(4, 1, 4, 1));
            nameBox.add(instrumentLabel);
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);

        JPanel mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        /* Then, we make check box, all 256 items.
            set them to false (aren't checked), and add them to the
            ArrayList AND to the GUI panel.
        * */
        checkboxList = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }
        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);
    }

    /* MIDI set up,
        for getting the Squencer, the Squence, and the Track
    * */
    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* where amazing happens
        turn checkbox state into MIDI events and add them to the Track
    * */

    public void buildTractAndStart() {
        ArrayList<Integer> trackList; //hold the instruments for each

//        make a fresh track
        sequence.deleteTrack(track);
        track = sequence.createTrack();
//      do this for each of the 16 ROWS life, Bass, Congo, etc
        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<>();
            int key = instruments[i];
            for (int j = 0; j < 16; j++) {
                JCheckBox jc = checkboxList.get(j + (16 * i));
                if (jc.isSelected()) {
                    trackList.add(key);
                } else {
                    trackList.add(null);
                }
            }
            makeTracks(trackList);
            track.add(makeEvent(CONTROL_CHANGE, 1, 127, 0, 16));
        }
        track.add(makeEvent(PROGRAM_CHANGE, 9, 1, 0, 15));

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.setTempoInBPM(120);
            sequencer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeTempo(float tempoMultiplier) {
        float tempoFactor = sequencer.getTempoFactor();
        sequencer.setTempoFactor(tempoFactor * tempoMultiplier);
    }

    public void sendMessageAndTracks() {
        boolean[] checkBoxsState = new boolean[256];
        for (int i = 0; i < 256; i++) {
            JCheckBox check = checkboxList.get(i);
            if (check.isSelected()) {
                checkBoxsState[i] = true;
            }
        }
        try {
            out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
            out.writeObject(checkBoxsState);
        } catch (IOException e) {
            System.out.println("So sorry. Could not send it to the server");
            e.printStackTrace();
        }
        userMessage.setText("");
    }

    class MyListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                String selected = incomingList.getSelectedValue();
                if (selected != null) {
                    boolean[] selectedState = otherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTractAndStart();
                }
            }
        }
    }

    public void changeSequence(boolean[] checkboxsState) {
        for (int i = 0; i < 256; i++) {
            JCheckBox check = checkboxList.get(i);
            check.setSelected(checkboxsState[i]);
        }
    }

    public void makeTracks(ArrayList<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            Integer instrumentKey = list.get(i);
            if (instrumentKey != null) {
                track.add(makeEvent(NOTE_ON, 9, instrumentKey, 100, i));
                track.add(makeEvent(NOTE_OFF, 9, instrumentKey, 100, i + 1));
            }
        }
    }

    public static MidiEvent makeEvent(int cmd, int chnl, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(cmd, chnl, one, two);
            event = new MidiEvent(msg, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    class RemoteReader implements Runnable {

        @Override
        public void run() {
            try {
                Object obj;
                while ((obj = in.readObject()) != null) {
                    System.out.println("Got an obj from server");
                    System.out.println(obj.getClass());

                    String nameToShow = (String) obj;
                    boolean[] checkboxState = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, checkboxState);

                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
