import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class View extends JFrame implements ActionListener {

    private static final long serialVersionUID = -2279311826638729930L;

    private JTextField txtAdresse;
    private JTextField txtPort;
    private JTextField txtSendLocal;
    private JTextField txtSendPath;
    private JTextField txtSendDistant;
    private JTextArea  txtInfoArea;

    JTextArea getTxtInfoArea() {
        return txtInfoArea;
    }

    private JButton btnSendFile;
    private JButton btnFindSend;
    private JMenuItem mnQuitter;
    private Client client;
    private String distant;

    View() {

        setType(Window.Type.UTILITY);
        setTitle("Client STF");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBounds(100, 100, 469, 485);

        chargerForm();
        client = new Client(this);
    }

    private void chargerForm() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        mnQuitter = new JMenuItem("Quitter");
        mnQuitter.addActionListener(this);
        menuBar.add(mnQuitter);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JTabbedPane ongletPanel = new JTabbedPane(JTabbedPane.TOP);
        ongletPanel.setBounds(6, 43, 452, 223);
        contentPane.add(ongletPanel);

        JPanel sendPanel = new JPanel();
        ongletPanel.addTab("Send", null, sendPanel, null);
        sendPanel.setLayout(null);

        JLabel lblNomLocal = new JLabel("Fichier local");
        lblNomLocal.setBounds(17, 28, 85, 14);
        sendPanel.add(lblNomLocal);

        txtSendLocal = new JTextField();
        txtSendLocal.setBounds(105, 25, 227, 20);
        txtSendLocal.setColumns(10);
        txtSendLocal.setEditable(false);
        sendPanel.add(txtSendLocal);

        JLabel lblNomFichierDistant = new JLabel("Nom fichier distant");
        lblNomFichierDistant.setBounds(17, 94, 124, 14);
        sendPanel.add(lblNomFichierDistant);

        btnFindSend = new JButton("Find");
        btnFindSend.setBounds(344, 25, 59, 20);
        btnFindSend.addActionListener(this);
        sendPanel.add(btnFindSend);

        txtSendDistant = new JTextField();
        txtSendDistant.setBounds(153, 91, 254, 20);
        sendPanel.add(txtSendDistant);
        txtSendDistant.setColumns(10);

        btnSendFile = new JButton("Send");
        btnSendFile.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
        btnSendFile.setBounds(126, 123, 171, 39);
        sendPanel.add(btnSendFile);

        JLabel lblPath = new JLabel("Path");
        lblPath.setBounds(17, 60, 85, 14);
        sendPanel.add(lblPath);

        txtSendPath = new JTextField();
        txtSendPath.setColumns(10);
        txtSendPath.setEditable(false);
        txtSendPath.setBounds(105, 57, 302, 20);
        sendPanel.add(txtSendPath);
        btnSendFile.addActionListener(this);


        JLabel lblAdresse = new JLabel("Adresse");
        lblAdresse.setBounds(31, 17, 59, 14);
        contentPane.add(lblAdresse);

        txtAdresse = new JTextField();
        txtAdresse.setBounds(102, 14, 124, 20);
        contentPane.add(txtAdresse);
        txtAdresse.setText("127.0.0.1");
        txtAdresse.setColumns(10);

        JLabel lblPort = new JLabel("Port");
        lblPort.setBounds(288, 17, 37, 14);
        contentPane.add(lblPort);

        txtPort = new JTextField();
        txtPort.setBounds(332, 14, 89, 20);
        txtPort.setEditable(false);
        txtPort.setText("69");
        txtPort.setColumns(10);
        contentPane.add(txtPort);

        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(14, 270, 436, 130);
        infoPanel.setLayout(new BorderLayout());

        txtInfoArea = new JTextArea();
        txtInfoArea.setEditable(false);
        txtInfoArea.setWrapStyleWord(true);
        txtInfoArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(txtInfoArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(txtInfoArea);
        infoPanel.add(scrollPane, BorderLayout.CENTER);

        contentPane.add(infoPanel);

        String monAdresse = "Inconnue";
        try { monAdresse = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) { e.printStackTrace(); }
        JLabel lblMonAdr = new JLabel("Mon adresse IP : "+monAdresse);
        lblMonAdr.setBounds(24, 412, 285, 16);
        contentPane.add(lblMonAdr);
    }


    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.mnQuitter) {
            System.exit(0);
        }

        if (e.getSource() == this.btnFindSend) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choisir un fichier");
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                txtSendLocal.setText(chooser.getSelectedFile().getName());
                txtSendPath.setText(chooser.getCurrentDirectory().toPath().toString());
            }
        }

        //Vérification de l'adresse IP
        if(!txtAdresse.getText().matches(
                "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))")
                || txtAdresse.getText().isEmpty())
        {
            JOptionPane.showMessageDialog(null, "L'adresse Ip n'est pas correcte");
            return;
        }

        //Vérification du port
        if(txtPort.getText().isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Le port est vide");
            return;
        }
        else {
            try{
                Integer.parseInt(txtPort.getText());
            }catch(NumberFormatException e1){
                JOptionPane.showMessageDialog(null, "Le port est invalide");
            }
        }

        //Clique sur le bouton Send
        if (e.getSource() == this.btnSendFile) {
            //Vérification du fichier local
            if(txtSendLocal.getText().isEmpty())
            {
                JOptionPane.showMessageDialog(null, "Le nom local est vide");
                return;
            }
            //Vérification du champ de renommage distant
            distant = txtSendLocal.getText();
            if(!txtSendDistant.getText().isEmpty())
                distant = txtSendDistant.getText();

            new Thread(new Runnable() {
                public void run() {
                    int Crem = client.sendFile(txtSendPath.getText(), txtSendLocal.getText(), distant, txtAdresse.getText(), Integer.parseInt(txtPort.getText()));
                    txtInfoArea.append("Crem : "+Crem+"\n\n");
                }
            }).start();

        }
    }
}
