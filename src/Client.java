import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
    private final Socket socket;

    //Used for sending message through socket.
    private PrintWriter pr;

    //Used for receiving message through socket.
    private InputStreamReader isr;
    private BufferedReader br;

    //Client Chat Data
    private String chatContent = "";

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
        //
        //Setup for main frame
        JFrame frame = new JFrame("Welcome to " + Server.getServerName() +
//                " ! Thread: " + Thread.currentThread().threadId() +
                "Socket: " + socket.getLocalPort());
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
        JTextArea textArea = new JTextArea("",15, 50);
        textArea.setWrapStyleWord(true);
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
        JTextField input = new JTextField(20);
        JButton button = new JButton("Send");

        if(username == null) {
            textArea.setText(Server.getServerWelcomeMessage() + "\nEnter a username.\n");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Submit input field on submit button click if not empty
                    if(!input.getText().isEmpty()) {
                        submitHandler(input, textArea);
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
                        submitHandler(input, textArea);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
        }

        //
        //Final Composition and Rendering
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        panel.add(scroller);
        inputpanel.add(input);
        inputpanel.add(button);
        panel.add(inputpanel);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setResizable(false);

        //Once frame is loaded, loop here and read from messages sent to socket
        while(true) {
            try {
                String messageRecieved = br.readLine();
                if(!messageRecieved.equals("null")) {
                    chatContent += messageRecieved + "\n";
                    textArea.setText(chatContent);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }//closes run()



    private void submitHandler(JTextField input, JTextArea textArea){
        String userResponse = input.getText();
        if(username == null) {
            boolean usernameTaken = false;
            for(ClientMessageHandler clientHandler : Server.getClientHandlerList()) {
                //System.out.println(clientHandler.getUsername());
                if (userResponse.equals(clientHandler.getUsername())) {
                    chatContent = "Username taken. Try a new one.";
                    textArea.setText(chatContent);
                    usernameTaken = true;
                    break;
                }
            }
            if(!usernameTaken) {
                username = userResponse;
                chatContent = "You have chosen the username: " + username + "\n";
                textArea.setText(chatContent);
                pr.println(username);
                pr.flush();
            }
        }else {
            if(userResponse != null ) {
                chatContent += "You: " + userResponse + "\n";
                textArea.setText(chatContent);
                pr.println(userResponse);
                pr.flush();
            }
        }
        userResponse = "";
        input.setText("");
    }

}//Closes Class


