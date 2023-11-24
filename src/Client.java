import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Threaded Client class to:
 * establish connection with server,
 * manage and update Swing GUI,
 * and service client requests.
 *
 * @author Zachary Cowan
 * @version 11 /1/2023
 * Fall/2023
 */
public class Client implements Runnable {

    //Client Identification
    private String username = null;
    private String serverName = null;
    private final Socket socket;

    //Used for sending and receiving message through socket.
    private final PrintWriter pr; // Sends messages through socket output stream.
    private final BufferedReader br; // Receives messages through socket input stream.

    // Flag for when username editing process is active.
    // Used to determine which page content to render.
    private boolean isEditingUsername = false;

    //
    //Chat Display Content
    private String chatContent = ""; // Stores chat data and some server notifications
    private String serverContent = ""; // Stores server dialogs like username prompting

    /**
     * Instantiates a new Client.
     * Connects to server and stores socket object.
     * Instantiates PrintWriter to write to socket output stream.
     * Instantiates Buffered Reader to read socket input stream.
     *
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    public Client() throws IOException {
        socket = new Socket("localhost", 8001);
        pr = new PrintWriter(socket.getOutputStream());
        //Used for receiving message through socket.
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
    }

    /**
     * Client main method.
     * Instantiates a new client object and executes its run method.
     * Client Object is designed to be distributable.
     *
     * @param args main args
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    public static void main(String [] args) throws IOException {
        ExecutorService clientExecutor = Executors.newSingleThreadExecutor();
        clientExecutor.execute(new Client());
    }

    /**
     * Execution loop for thread where:
     *      GUI is created and managed, and
     *      messages are processed and sent.
     */
    @Override
    public void run()
    {

        // Receive Initial Server Name from Server.
        try {
            serverName = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //
        //Setup for main frame
        JFrame frame = new JFrame("Welcome to " + serverName +
                " - Socket: " + socket.getLocalPort());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(true);


        //
        //Setup for text area in center
        JTextArea textArea = new JTextArea("",20, 60);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFont(Font.getFont(Font.SANS_SERIF));

        //
        //Enables scrolling for chat display
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //
        //Setup for bottom entry section
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField input = new JTextField(40);
        JButton button = new JButton("Send");

        //
        // Occurs on initial run
        if(username == null) {
            isEditingUsername = true;
            serverContent += getServerWelcomeMessage(serverName) + "\nEnter a username.\n";
            textArea.setText(serverContent);
        }


        //
        // Listen for Enter key press or Submit button click.
        // If text input is not empty, process that text and clear the field.
        button.addActionListener(e -> {
            //Submit input field on submit button click if not empty
            if(!input.getText().isEmpty()) {
                try {
                    submitHandler(input, textArea, frame);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        input.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //Submit input field on enter key if not empty
                if(e.getKeyCode() == 10 && !input.getText().isEmpty()) {
                    try {
                        submitHandler(input, textArea, frame);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        //
        // Listens for frame close and handles correspondingly.
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                try {
                    doClose(frame);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        //
        //Final Composition and Rendering
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panel.add(scroller);
        panel.setAutoscrolls(true);
        inputPanel.add(input);
        inputPanel.add(button);
        panel.add(inputPanel);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setResizable(true);
        panel.updateUI();

        //
        // Message handling for messages sent from Server
        while(socket.isConnected()) {
            try {
                String messageReceived = br.readLine();

                // Process new servername and update GUI.
                if(messageReceived.startsWith("/servername")) {
                    serverName = messageReceived.substring(12);
                    frame.setTitle("Welcome to " + serverName +
                            " - Socket: " + socket.getLocalPort());
                }
                //
                else if(messageReceived.startsWith("/userIsTaken")) {
                    serverContent += "\nUsername taken. Try a new one.\n";
                    textArea.setText(serverContent);
                } else if( messageReceived.startsWith("/userIsUnique") ) {
                    username = messageReceived.substring(14);
                    serverContent += "\n[SERVER] You have chosen the username: " + username + "\n";
                    textArea.setText(serverContent);
                    pr.println("/username " + username);
                    pr.flush();
                    isEditingUsername = false;
                }

                // Process User Messages
                else {
                    chatContent += messageReceived + "\n\n";
                    if(!isEditingUsername) {
                        textArea.setText(chatContent);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }//closes run()


    /**
     * Handles cases when user submits a message via Enter key or Submit button.
     * Preprocessing for username creation / editing process.
     * Handles user quit request.
     * @param input Input where user types messages.
     * @param textArea Area where messages are rendered.
     * @param frame Main JFrame passed for frame termination.
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    private void submitHandler(JTextField input, JTextArea textArea, JFrame frame) throws IOException {
        String userResponse = input.getText();

        // Username prompting system.
        // Switches to rendering Server specific content but still stores chatContent
        if(username == null || (userResponse.startsWith("/username") && userResponse.length() >= 10) ) {
            isEditingUsername = true;
            String usernameCandidate = userResponse;
            // Takes first entry by user upon entering server as username
            if( username == null ) {
                usernameCandidate = userResponse;
            }
            // Parses /username command
            else if(userResponse.startsWith("/username")) {
                usernameCandidate = userResponse.substring(10);
            }

            // Sends request to server to check if desired username is taken
            pr.println("/checkUser " + usernameCandidate);
            pr.flush();
        }
        // Handles User Quit
        else if(userResponse.equals("/quit")) {
            doClose(frame);
            return;
        }
        // Normal Message Sending
        else {
            chatContent += "You: " + userResponse + "\n\n";
            textArea.setText(chatContent);
            pr.println(userResponse);
            pr.flush();
        }
        input.setText("");
    }

    /**
     * Method to terminate all resources when client decides to quit or frame is closed.
     * @param frame JFrame to be terminated
     * @throws IOException IOException might be thrown if socket encounters and IO error.
     */
    private void doClose(JFrame frame) throws IOException {
        frame.dispose();
        pr.println("/quit");
        pr.flush();
        pr.close();
        br.close();
        socket.close();
    }


    /**
     * Gets server welcome message.
     *
     * @param serverName the server name
     * @return the server welcome message
     */
    public static String getServerWelcomeMessage(String serverName) {
        return "\nWelcome to the " + serverName + " Server!\n";
    }


}//Closes Class


