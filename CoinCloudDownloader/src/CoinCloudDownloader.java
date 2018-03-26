import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class CoinCloudDownloader extends JFrame {
	private static final long serialVersionUID = -7471111582156575408L;
	
	private JPanel contentPane;
	private JTextField textField_tx;
	private JTextField textField_path;
	private JTextPane textPane;
	private volatile Boolean isBuisy;
	public final long startTime;
	public static final int ERROR = 0;
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final Color ERRORColor = new Color(255, 0, 0);
	public static final Color INFOColor = new Color(0, 0, 0);
	public static final Color WARNINGColor = new Color(155, 155, 0);
	public static final Color POINTYColor = new Color(0, 255, 0);
	public static final String version = "0.0.4-Beta";
	public static final String name = "CoinCloudDownloader";
	
	public static final String BLOCKCHAIN_Host = "blockchain.info";
	public static final String BLOCKCHAIN_HostTor = "blockchainbdgpzk.onion";
	public static final String URL_CheckTorConnection = "https://ipinfo.io/ip";//would normally just return the current public ip-address
	public static final int TIMEOUT_CheckTorConnection = 20;//in seconds
	public static final Proxy TORProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9150));

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CoinCloudDownloader frame = new CoinCloudDownloader();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	public CoinCloudDownloader() {
		this.startTime = System.currentTimeMillis();
		this.isBuisy = false;
		setTitle(name + " " + version);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		textField_tx = new JTextField();
		textField_tx.setBorder(new LineBorder(new Color(171, 173, 179)));
		panel.add(textField_tx, BorderLayout.CENTER);
		textField_tx.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Transaction Name (Hash):");
		panel.add(lblNewLabel, BorderLayout.NORTH);
		
		JCheckBox chckbxUseTor = new JCheckBox("Use Tor");
		chckbxUseTor.setToolTipText("If enabled, the hidden service \"" + BLOCKCHAIN_HostTor + "\" will be used, in order to download the transaction. You need to have an Tor proxy installed and running on 127.0.0.1:9150 in order for this to work! Otherwise \"" + BLOCKCHAIN_Host + "\" will be used.");
		chckbxUseTor.setSelected(true);
		panel.add(chckbxUseTor, BorderLayout.SOUTH);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		textField_path = new JTextField();
		textField_path.setBorder(new LineBorder(new Color(171, 173, 179)));
		panel_2.add(textField_path, BorderLayout.CENTER);
		textField_path.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Output file path:");
		panel_2.add(lblNewLabel_1, BorderLayout.NORTH);
		
		JButton btnChoose = new JButton("Choose");
		btnChoose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(btnChoose);
				File f = fc.getSelectedFile();
				if(f==null)
					return;
				if(f.isDirectory()) {
					log("The selected file is directory and not an file, choose an file!", ERROR);
					return;
				}
				if(f.exists())
					log("The chosen file does already exist, the file WILL BE OVERWRITTEN, choose another file if you don't want this!", WARNING);
				textField_path.setText(f.getAbsolutePath());
			}
		});
		panel_2.add(btnChoose, BorderLayout.EAST);
		
		JButton btnNewButton = new JButton("Write to output file");
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//this ensures that the button is not being spamed, since every press creates an new thread. Only one download at a time.
				//Necessary, because swing doesn't handle multiple threads well (and comes up with a lot of surprises if one is not careful enough). Therefore JButton.setEnabled(false) could be dangerous.
				synchronized (isBuisy) {
					if(isBuisy.booleanValue())
						return;
					else
						isBuisy = new Boolean(true);
				}
				//this ensures the the field-data is not being manipulated while the download is running
				final String transaction = textField_tx.getText();
				final boolean isCheckBoxTorChecked = chckbxUseTor.isSelected();
				final String outputPath =textField_path.getText();
				//new thread keeps the UI being responsive and the log screen updating.
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						if(transaction.equals("")) {
							log("The field transaction name is empty.",ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						if(!transaction.matches("-?[0-9a-fA-F]+")) {
							log("The given transaction hash is not in hexadecimal, the transaction has to be in hexadecimal...",ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						boolean useTor = false;
						if (isCheckBoxTorChecked) {
							log("Use Tor is checked, testing proxy...", INFO);
							if(isProxyAvailable(TORProxy)) {
								useTor = true;
								log("Tor is working.", INFO);
							}else {
								log("It has been selected that Tor should be used in the connection to " + BLOCKCHAIN_Host + "(" + BLOCKCHAIN_HostTor + "), but the Tor-proxy could not achieve an Internet connection. Make sure it runs under 127.0.0.1 on port 9150!", ERROR);
								synchronized (isBuisy) {isBuisy = new Boolean(false);}
								return;
							}
							log("Receiving transmission... Since an hidden service is used, this could take some time.", INFO);
						}else {
							log("Receiving transmission...", INFO);
						}
						List<String> hex;
						try {
							hex = obtainHexFromTransaction(transaction, useTor);
						} catch (IOException e2) {
							log("There have been problems retrieving the transaction from " + (useTor? BLOCKCHAIN_HostTor : BLOCKCHAIN_Host) + "/rawtx/[...] maybe you have no Internet connection or the webside is down?",ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						log("The transaction has been received.", INFO);
						if(outputPath == "") {
							log("The path is empty!",ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						FileOutputStream fos;
						try {
							fos = new FileOutputStream(outputPath);
						} catch (FileNotFoundException e2) {
							log("The path is invalid!",ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						log("Writing data to file[" + outputPath + "]...", INFO);
						List<Byte> b = new ArrayList<Byte>();
						for (String h : hex) {
							putToBytes(h, b);
						}
						byte[] out = new byte[b.size()];
						for (int i = 0; i < out.length; i++) {
							out[i] = b.get(i);
						}
						try {
							fos.write(out);
							fos.close();
						} catch (IOException e1) {
							log("An exception while writing to the file, maybe not enough file permissions?", ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						log("DONE", INFO);
						synchronized (isBuisy) {isBuisy = new Boolean(false);}
					}
				});
				t.start();
			}
		});

		panel_2.add(btnNewButton, BorderLayout.SOUTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		panel_1.add(scrollPane, BorderLayout.CENTER);
		
		textPane = new JTextPane();
		appendToLog(">", POINTYColor);
		textPane.setFocusable(false);
		textPane.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		
		JLabel lblInfo = new JLabel("Info/Progress:");
		scrollPane.setColumnHeaderView(lblInfo);
		
		JButton btnLookhere = new JButton("Clear");
		btnLookhere.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				clearlog();
			}
		});
		panel_1.add(btnLookhere, BorderLayout.SOUTH);
	}

	public static void putToBytes(String s, List<Byte> l) {
		for(int i = 0;i< s.length()/2;i++) {
			int b = (Integer.parseInt(s.substring(2*i, (2*i)+2),16) & 0xff);
			l.add((byte)b);
		}
	}
	
	public List<String> obtainHexFromTransaction(String transaction, boolean useTor) throws IOException{
		List<String> out = new ArrayList<String>();
		URLConnection c;
		if(useTor) {
			URL blockchainINFO = new URL("https", BLOCKCHAIN_HostTor, "/rawtx/" + transaction);
			log("Connect to: " + "https://" + BLOCKCHAIN_HostTor + "/rawtx/" + transaction + ".", INFO);
			c = blockchainINFO.openConnection(TORProxy);
		}else {
			URL blockchainINFO = new URL("https", BLOCKCHAIN_Host, "/rawtx/" + transaction);
			log("Connect to: " + "https://" + BLOCKCHAIN_Host + "/rawtx/" + transaction + ".", INFO);
			c = blockchainINFO.openConnection();
		}
		c.connect();
		InputStream i = c.getInputStream();
		InputStreamReader r = new InputStreamReader(i);
		BufferedReader br = new BufferedReader(r);
		String line = "";
		boolean passedOUT = false;
		log("Download and parse...", INFO);
		while((line = br.readLine()) != null) {
			if(passedOUT) {
				if(line.contains("\"script\":")) {
					String[] split = line.split("\"");
					out.add(extractDataFromScript(split[split.length-1]));
				}
			}else {
				if(line.contains("\"out\"")) {
					passedOUT = true;
				}
			}
		}
		return out;
	}
	//since this is an early version, only the first data(the very fist pushed bytes) in the script will be returned. Maybe more 'creativity' for storage in the future?
	public static String extractDataFromScript(String script) {
		for (int i = 0; i < script.length(); i +=2) {
			int data = Integer.parseInt(script.substring(i, i+2), 16);
			if(data>=1 && data <= 75) {//next data bytes are the data
				return script.substring(i+2,(i+2) + data*2);//2 chars are 1 byte, duh!
			}else if(data==76) {//the next byte is the number of bytes of the data
				data = Integer.parseInt(script.substring(i+2, i+4), 16);
				return script.substring(i+4, (i+4) + data*2);
			}else if(data==77) {//the next two bytes are the number of bytes of the data (little endian)
				String[] hexBytes = new String[] {script.substring(i+2, i+4),script.substring(i+4, i+6)};
				data = Integer.parseInt(hexBytes[1] + hexBytes[0], 16);
				return script.substring(i+6, (i+6) + data*2);
			}else if(data==78) {//the next four bytes are the number of bytes of the data (little endian)
				String[] hexBytes = new String[] {script.substring(i+2, i+4),script.substring(i+4, i+6),script.substring(i+6, i+8),script.substring(i+8, i+10)};
				data = Integer.parseInt(hexBytes[3] + hexBytes[2] + hexBytes[1] + hexBytes[0], 16);
				return script.substring(i+10, (i+10) + data*2);
			}
		}
		return null;
	}
	public void log(String message, int type) {
		synchronized (textPane) {
			Date date = new Date(System.currentTimeMillis());
			DateFormat df = new SimpleDateFormat("HH-mm-ss");
			String specificText;
			Color specificColor;
			switch (type) {
			case ERROR:
				specificText = "ERROR: ";
				specificColor = ERRORColor;
				break;
			case INFO:
				specificText = "INFO: ";
				specificColor = INFOColor;
				break;
			case WARNING:
				specificText = "WARNING: ";
				specificColor = WARNINGColor;
				break;
			default:
				specificText = "";
				specificColor = new Color(0, 0, 0);
				break;
			}
			appendToLog("[" + df.format(date) + "] " + specificText + message + "\n", specificColor);
			appendToLog(">", POINTYColor);
		}
	}
	public void clearlog() {
		synchronized (textPane) {
			textPane.setText("");
			appendToLog(">", POINTYColor);
		}
	}
	
    private void appendToLog(String msg, Color c){//Concurrency save, because the method is used only once in an sync-block
	        StyleContext sc = StyleContext.getDefaultStyleContext();
	        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
	
	        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
	        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
	
	        int len = textPane.getDocument().getLength();
	        textPane.setCaretPosition(len);
	        textPane.setCharacterAttributes(aset, false);
	        textPane.setEditable(true);
	        textPane.replaceSelection(msg);
	        textPane.setEditable(false);
    }
    
	private static boolean isProxyAvailable(Proxy p) {
		HttpURLConnection con;
		try {
			con = (HttpURLConnection) new URL(URL_CheckTorConnection).openConnection(p);
			con.setRequestMethod("HEAD");
			con.setConnectTimeout(TIMEOUT_CheckTorConnection*1000);
			con.connect();
			return con.getResponseCode()/100 == 2;//code 2xx means OK
		} catch (IOException e) {
			return false;
		}
	}
}