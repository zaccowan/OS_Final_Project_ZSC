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

    Socket socket;
    static PrintWriter pr;
    BufferedReader br;
    String username = null;
    String userResponse;


    public Client() throws UnknownHostException, IOException {

        socket = new Socket("localhost", 8001);
        pr = new PrintWriter(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(System.in));


    }

    @Override
    public void run()
    {
        //Setup for main frame
        JFrame frame = new JFrame("Welcome to " + Server.getServerName() + " !");
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

        //Setup for text area in center
        JTextArea textArea = new JTextArea("",15, 50);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(Font.getFont(Font.SANS_SERIF));


        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        if( username == null) {
            textArea.setText("Enter a username.\n");
        }

        //Setup for bottom entry section
        JPanel inputpanel = new JPanel();
        inputpanel.setLayout(new FlowLayout());
        JTextField input = new JTextField(20);
        JButton button = new JButton("Send");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!Objects.equals(input.getText(), "")) {
                    userResponse = input.getText();
                    if(username == null) {
                        boolean usernameTaken = false;
                        for(Client c: Server.clientList) {
                            System.out.println(c.username);
                            if( c.username.equals(userResponse)) {
                                textArea.setText("This username is taken. Try a new one.");
                                usernameTaken = true;
                                break;
                            }
                        }
                        if(!usernameTaken) {
                            textArea.setText("");
                            username = userResponse;
                            pr.println(username);
                            pr.flush();
                        }
                    }
                    textArea.setText(textArea.getText() + userResponse + "\n");
                    input.setText("");
                }
            }
        });
        input.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == 10 && !Objects.equals(input.getText(), "")) {
                    userResponse = input.getText();
                    if(username == null) {
                        boolean usernameTaken = false;
                        for(Client c: Server.clientList) {
                            System.out.println(c.username);
                            if( c.username.equals(userResponse)) {
                                textArea.setText("This username is taken. Try a new one.");
                                usernameTaken = true;
                                break;
                            }
                        }
                        if(!usernameTaken) {
                            textArea.setText("");
                            username = userResponse;
                            pr.println(username);
                            pr.flush();
                        }
                    }
                    textArea.setText(textArea.getText() + userResponse + "\n");
                    input.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
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
        input.requestFocus();

    }

}//Closes Class


