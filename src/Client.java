import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client implements Runnable {
    //Stores all the clients that have been created.
    //public static ArrayList<Client> clientList = new ArrayList<Client>();

    //Client Identification
    private String username = null;
    private String userResponse;
    private Socket socket;

    //Used for sending message through socket.
    private PrintWriter pr;

    //Used for recieving message through socket.
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

    @Override
    public void run()
    {
        //
        //Setup for main frame
        JFrame frame = new JFrame("Welcome to " + Server.getServerName() +
                " ! Thread: " + Thread.currentThread().threadId());
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
        while (true) {
            try {
                String messageRecieved = br.readLine();
                System.out.println(messageRecieved);

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
        userResponse = input.getText();
        if(username == null) {
            boolean usernameTaken = false;
            for(ClientData c: Server.getClientList()) {
                try {
                    if (c.getUsername().equals(userResponse)) {
                        textArea.setText("This username is taken. Try a new one.");
                        usernameTaken = true;
                        break;
                    }
                } catch (NullPointerException e) {
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
            if(userResponse != null) {
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


