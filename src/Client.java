import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

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
    private String chatContent;

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
        JTextArea textArea = new JTextArea(chatContent,15, 50);
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

        if( username == null) {
            textArea.setText(Server.getServerWelcomeMessage() + "\nEnter a username.\n");
        }else {

            textArea.setText(chatContent);
        }
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Submit input field on submit button click if not empty
                if(!input.getText().isEmpty()) {
                    usernameHandler(input, textArea);
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
                    usernameHandler(input, textArea);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });


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


    }

    private void usernameHandler(JTextField input, JTextArea textArea) {
        userResponse = input.getText();
        if(username == null) {
            boolean usernameTaken = false;
            for(ClientData c: Server.getClientList()) {
                System.out.println(c.getUsername());
                try {
                    if (c.getUsername().equals(userResponse)) {
                        textArea.setText("This username is taken. Try a new one.");
                        usernameTaken = true;
                        break;
                    }
                } catch (NullPointerException e) {
                    continue;
                }
            }
            if(!usernameTaken) {
                textArea.setText("");
                username = userResponse;
//                Server.clientList.add(new ClientData(this.socket,this.username));
                pr.println(username);
                pr.flush();
            }
        }else {
            textArea.setText(textArea.getText() + userResponse + "\n");
            pr.println(userResponse);
            pr.flush();
        }
        userResponse = "";
        input.setText("");

    }

    private String getMessage() {
        return null;
    }

}//Closes Class


