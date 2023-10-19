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

public class Client implements Runnable {
    //Stores all the clients that have been created.
    //public static ArrayList<Client> clientList = new ArrayList<Client>();

    Socket socket;
    PrintWriter pr;
    BufferedReader br;
    String username = null;


    public Client() throws UnknownHostException, IOException {
        createFrame();

        socket = new Socket("localhost", 8001);
        pr = new PrintWriter(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(System.in));


        try {
            String userResponse = null;
            while( username == null) {
                System.out.println("Enter a username.");
                for( Client c : Server.clientList) {
                    System.out.println(c.username);
                }
                System.out.println("test");
                userResponse = br.readLine();
                boolean usernameTaken = false;
                for(Client c: Server.clientList) {
                    System.out.println(c.username);
                    if( c.username.equals(userResponse)) {
                        System.out.println("This username is taken. Try a new one.");
                        usernameTaken = true;
                        break;
                    }
                }
                if(!usernameTaken) {
                    username = userResponse;
                    pr.println(username);
                    pr.flush();
                }
            } //loops until a vaild username is entered.
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(socket.isConnected()) {
            System.out.println("Hello "+ username + ", enter message to send");
            try {
                String message;
                message = br.readLine();
                pr.println(message);
                pr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket("localhost", 8001);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrintWriter pr = null;
        try {
            pr = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        br = new BufferedReader(new InputStreamReader(System.in));


        try {
            String userResponse = null;
            while( username == null) {
                System.out.println("Enter a username.");
                for( Client c : Server.clientList) {
                    System.out.println(c.username);
                }
                System.out.println("test");
                userResponse = br.readLine();
                boolean usernameTaken = false;
                for(Client c: Server.clientList) {
                    System.out.println(c.username);
                    if( c.username.equals(userResponse)) {
                        System.out.println("This username is taken. Try a new one.");
                        usernameTaken = true;
                        break;
                    }
                }
                if(!usernameTaken) {
                    username = userResponse;
                    pr.println(username);
                    pr.flush();
                }
            } //loops until a vaild username is entered.
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(socket.isConnected()) {
            System.out.println("Hello "+ username + ", enter message to send");
            try {
                String message;
                message = br.readLine();
                pr.println(message);
                pr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static void createFrame()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
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
                JTextArea textArea = new JTextArea(15, 50);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);
                textArea.setFont(Font.getFont(Font.SANS_SERIF));
                JScrollPane scroller = new JScrollPane(textArea);
                scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                JPanel inputpanel = new JPanel();
                inputpanel.setLayout(new FlowLayout());
                JTextField input = new JTextField(20);
                JButton button = new JButton("Send");
                button.addActionListener(e -> frame.setTitle(input.getText()));
                input.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(e.getKeyCode() == 10) {
                            frame.setTitle(input.getText());
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
        });
    }
}//Closes Class


