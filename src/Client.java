import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable {
    //Stores all the clients that have been created.
    //public static ArrayList<Client> clientList = new ArrayList<Client>();

    //Client Identification
    private String username = null;
    private String serverName = null;
    private final Socket socket;

    //Used for sending message through socket.
    private PrintWriter pr;

    //Used for receiving message through socket.
    private InputStreamReader isr;
    private BufferedReader br;

    private boolean isEditingUsername = false;
    //Chat Display Content
    private String chatContent = "";
    private String serverContent = "";

    public Client() throws UnknownHostException, IOException {
        socket = new Socket("localhost", 8001);
        pr = new PrintWriter(socket.getOutputStream());
        isr = new InputStreamReader(socket.getInputStream());
        br = new BufferedReader(isr);
    }

    public static void main(String [] args) throws IOException {
        ExecutorService clientExecutor = Executors.newSingleThreadExecutor();
        clientExecutor.execute(new Client());
    }

    @Override
    public void run()
    {

        while(serverName == null) {
            try {
                serverName = br.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

        //Enables scrolling for chat display
        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //
        //Setup for bottom entry section
        JPanel inputpanel = new JPanel();
        inputpanel.setLayout(new FlowLayout());
        inputpanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField input = new JTextField(40);
        JButton button = new JButton("Send");


        if(username == null) {
            isEditingUsername = true;
            serverContent += getServerWelcomeMessage(serverName) + "\nEnter a username.\n";
            textArea.setText(serverContent);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Submit input field on submit button click if not empty
                    if(!input.getText().isEmpty()) {
                        try {
                            submitHandler(input, textArea, frame);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
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
        }

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
        inputpanel.add(input);
        inputpanel.add(button);
        panel.add(inputpanel);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setResizable(true);
        panel.updateUI();

        //Once frame is loaded, loop here and read from messages sent to socket
        while(true) {
            try {
                String messageRecieved = br.readLine();
                if(messageRecieved.startsWith("/servername")) {
                    String newServername = messageRecieved.substring(12, messageRecieved.length());
                    serverName = newServername;
                    frame.setTitle("Welcome to " + serverName +
                            " - Socket: " + socket.getLocalPort());
                }
                else if(!messageRecieved.equals("null")) {
                    chatContent += messageRecieved + "\n\n";
                    if( isEditingUsername == false ) {
                        textArea.setText(chatContent);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }//closes run()



    private void submitHandler(JTextField input, JTextArea textArea, JFrame frame) throws IOException {
        String userResponse = input.getText();

        // Username prompting system.
        // Switches to rendering Server specific content but still stores chatContent
        if(username == null || (userResponse.startsWith("/username") && userResponse.length() >= 10) ) {
            isEditingUsername = true;
            String usernameCandidate = userResponse;
            if( username == null ) {
                usernameCandidate = userResponse;
            } else if(userResponse.startsWith("/username")) {
                usernameCandidate = userResponse.substring(10, userResponse.length());
            }
            boolean usernameTaken = false;
            for(ClientMessageHandler clientHandler : Server.getClientHandlerList()) {
                if (usernameCandidate.equals(clientHandler.getUsername())) {
                    serverContent += "\nUsername taken. Try a new one.\n";
                    textArea.setText(serverContent);
                    usernameTaken = true;
                    break;
                }
            }
            if(!usernameTaken) {
                username = usernameCandidate;
                serverContent += "\n[SERVER] You have chosen the username: " + username + "\n";
                textArea.setText(serverContent);
                pr.println("/username " + username);
                pr.flush();
                isEditingUsername = false;
            }
        }
        // Handles Normal Message Sending
        else {
            if(userResponse != null ) {
                if(userResponse.equals("/quit")) {
                    doClose(frame);
                    return;
                } else {
                    chatContent += "You: " + userResponse + "\n\n";
                    textArea.setText(chatContent);
                    pr.println(userResponse);
                    pr.flush();
                }
            }
        }
        input.setText("");
    }

    private void doClose(JFrame frame) throws IOException {
        frame.dispose();
        pr.println("/quit");
        pr.flush();
        socket.close();
    }


    public static String getServerWelcomeMessage(String serverName) {
        return "\nWelcome to the " + serverName + " Server!\n";
    }


}//Closes Class


