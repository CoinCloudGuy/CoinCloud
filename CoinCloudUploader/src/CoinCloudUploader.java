import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class CoinCloudUploader extends JFrame {
	private static final long serialVersionUID = 4082568690602702353L;
	
	private JPanel contentPane;
	private JTextField fieldFileInput;
	private JTextField fieldTxOutputFile;
	private JTextField fieldCostPerAddress;
	private JTextField fieldFee;
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

	public static final String TRANSACTION_default = "546";//in satoshi per address
	public static final int TRANSACTION_default_value = 546;//in satoshi per address
	public static final String FEE_default = "0.00001185";//in BTC per kB
	public static final double FEE_default_value = 0.00001185d;// in BTC per kB
	public static final int assumedInputLength = 187;//in bytes
	public static final int amoutDataInTx = 20;//in bytes
	public static final int assumedExtraBytes = 20;//per transaction
	
	public static final String version = "0.0.1-Beta";
	public static final String name = "CoinCloudUploader";
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CoinCloudUploader frame = new CoinCloudUploader();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public CoinCloudUploader() {
		this.startTime = System.currentTimeMillis();
		this.isBuisy = new Boolean(false);
		setTitle(name + " " + version);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblInput = new JLabel("File input:");
		panel.add(lblInput, BorderLayout.NORTH);
		
		fieldFileInput = new JTextField();
		fieldFileInput.setToolTipText("The file that will be uploaded into the bitcoin blockchain.");
		panel.add(fieldFileInput, BorderLayout.CENTER);
		fieldFileInput.setColumns(10);
		
		JButton btnChooseInput = new JButton("Choose");
		btnChooseInput.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(btnChooseInput);
				File f = fc.getSelectedFile();
				if(f==null)//file chooser aborted
					return;
				if(f.isDirectory()) {
					log("The selected file is directory and not an file, choose an file!", ERROR);
					return;
				}
				if(!f.exists()) {
					log("The chosen file does not exist. Choose an file you want to be converted!", ERROR);
					return;
				}
				fieldFileInput.setText(f.getAbsolutePath());
			}
		});
		panel.add(btnChooseInput, BorderLayout.EAST);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JLabel lblTransactionOutputFile = new JLabel("Transaction csv output file:");
		panel_2.add(lblTransactionOutputFile, BorderLayout.NORTH);
		
		fieldTxOutputFile = new JTextField();
		fieldTxOutputFile.setToolTipText("This is the file the bitcoin addresses, that need to be payed, will be written to.");
		panel_2.add(fieldTxOutputFile, BorderLayout.CENTER);
		fieldTxOutputFile.setColumns(10);
		
		JButton btnChooseOutput = new JButton("Choose");
		btnChooseOutput.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return ".csv";
					}
					
					@Override
					public boolean accept(File f) {
						if(f.isDirectory())
							return true;
						return f.getName().endsWith(".csv");
					}
				});
				fc.showOpenDialog(btnChooseOutput);
				File f = fc.getSelectedFile();
				if(f==null)//file chooser aborted
					return;
				if(f.isDirectory()) {
					log("The selected file is directory and not an file, choose a file!", ERROR);
					return;
				}
				if(!f.getName().endsWith(".csv")) {
					log("The selected file is not an .csv file. Choose an .csv file!", ERROR);
					return;
				}
				if(f.exists())
					log("The chosen file does already exist, the file WILL BE OVERWRITTEN, choose another file if you don't want this!", WARNING);
				fieldTxOutputFile.setText(f.getAbsolutePath());
			}
		});
		panel_2.add(btnChooseOutput, BorderLayout.EAST);
		
		JButton btnWriteparseToTransaction = new JButton("Write/Parse to Transaction");
		btnWriteparseToTransaction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//this ensures that the button is not being spamed, since every press creates an new thread. Only one parse at a time.
				//Necessary, because swing doesn't handle multiple threads well (and comes up with a lot of surprises if one is not careful enough). Therefore JButton.setEnabled(false) could be dangerous.
				synchronized (isBuisy) {
					if(isBuisy.booleanValue())
						return;
					else
						isBuisy = new Boolean(true);
				}
				//this ensures the the field-data is not being manipulated while the download is running
				final String s_cpa = fieldCostPerAddress.getText();
				final String s_fileInPath = fieldFileInput.getText();
				final String s_fileOutPath = fieldTxOutputFile.getText();
				//new thread keeps the UI being responsive and the log screen updating.
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						final int cpa;//cost per address in satoshi
						final File fileInPath;
						final File fileOutPath;
						try {
							cpa = Integer.parseInt(s_cpa);
						}catch(NumberFormatException | NullPointerException e) {
							log("The Satoshi per transaction field contained: \"" + s_cpa + "\". This is not an integer value.", ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						try {
							fileInPath = new File(s_fileInPath);
							if(!fileInPath.exists()) {
								log("The file input field contained: \"" + s_fileInPath + "\". This file does not exist.", ERROR);
								synchronized (isBuisy) {isBuisy = new Boolean(false);}
								return;
							}
							if(fileInPath.length() == 0) {
								log("The input file is empty.", ERROR);
								synchronized (isBuisy) {isBuisy = new Boolean(false);}
								return;
							}
						} catch (Exception e) {
							log("Could not access the input file: \"" + s_fileInPath + "\".", ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						try {
							fileOutPath = new File(s_fileOutPath);
							if(!fileOutPath.getName().endsWith(".csv")){
								log("The file output field contained: \"" + s_fileOutPath + "\". This is not an .csv file.", ERROR);
								synchronized (isBuisy) {isBuisy = new Boolean(false);}
								return;
							}
						} catch (Exception e) {
							log("The file output field contained: \"" + s_fileOutPath + "\". This is invalid.", ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						log("Reading the input file and parsing the data...", INFO);
						FileInputStream fi;
						LinkedList<String> addresses = new LinkedList<String>();
						try {
							fi = new FileInputStream(fileInPath);
							byte[] data = new byte[20];//length of would be RIPEMD160 output
							boolean hasNext = true;
							while(hasNext) {
								hasNext = fi.read(data)!=-1;
								addresses.add(craftValidAddressFromData(data));
							}
							fi.close();
						} catch (IOException e) {
							if(e instanceof FileNotFoundException)
								log("Could not find the input file: \"" + fileInPath.getAbsolutePath() + "\".", ERROR);
							else
								log("While reading the input file an IOException has occurred, maybe the program does not have permission to read?", ERROR);
							
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						log("Writing the parsed data to the .csv file...", INFO);
						FileWriter w;
						try {
							w = new FileWriter(fileOutPath);
							for(String address : addresses)
								w.write(address + "," + convertSatoshiToBTC(cpa) + (char)(0x0A));//0x0A is .csv line separator
							w.close();
						} catch (IOException e) {
							log("Could not write to the output file: \"" + fileOutPath.getAbsolutePath() + "\", maybe the program does not have permission to write?", ERROR);
							synchronized (isBuisy) {isBuisy = new Boolean(false);}
							return;
						}
						log("DONE", INFO);
						log("To finish the upload: Open electrum and under \"Tools\" activate \"Pay to many\". Than import the .csv file created by this program and choose an fee that you feel comfortable with, than press sent. If you are using another wallet: Inform yourself on how to pay multiple addresses in only one transaction.", INFO);
						synchronized (isBuisy) {isBuisy = new Boolean(false);}
					}
				});
				t.start();
			}
		});
		panel_2.add(btnWriteparseToTransaction, BorderLayout.SOUTH);
		
		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel lblCostPerAddress = new JLabel("Satoshi per address:");
		panel_3.add(lblCostPerAddress, BorderLayout.NORTH);
		
		fieldCostPerAddress = new JTextField();
		fieldCostPerAddress.setToolTipText("This is the amount of Satoshi each Bitcoin address will receive. It is advised not to change this, since any amount less than " + TRANSACTION_default + " will be dubbed as \"dust\" and dealt with as spam(ignored) on the network.");
		fieldCostPerAddress.setText(TRANSACTION_default+"");
		panel_3.add(fieldCostPerAddress, BorderLayout.SOUTH);
		fieldCostPerAddress.setColumns(10);
		
		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JLabel lblFee = new JLabel("Fee in BTC per kB transaction size:");
		panel_4.add(lblFee, BorderLayout.NORTH);
		
		fieldFee = new JTextField();
		fieldFee.setToolTipText("Only used in the estimating calculations! This is the fee that will be awarded to the miner that includes this transaction. If this value is to small the transaction will take a longer to be included into the blockchain. " + FEE_default + " is fee will include your transaction in around 2-5 hours.");
		fieldFee.setText(FEE_default+"");
		panel_4.add(fieldFee, BorderLayout.CENTER);
		fieldFee.setColumns(10);
		
		JButton btnEstimateBtcNeeded = new JButton("Estimate BTC needed");
		btnEstimateBtcNeeded.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				final int transactionCost;//in satoshi per address. 
				final String s_transactionCost = fieldCostPerAddress.getText();
				try {
					transactionCost = Integer.parseInt(s_transactionCost);
				}catch(NumberFormatException | NullPointerException e) {
					log("The Satoshi per transaction field contained: \"" + s_transactionCost + "\". This is not an integer value.", ERROR);
					return;
				}
				final double fee;//in BTC per kB 
				final String s_fee = fieldFee.getText();
				try {
					fee = Double.parseDouble(s_fee);
				}catch(NumberFormatException | NullPointerException e) {
					log("The fee field contained: \"" + s_fee + "\". This is not an decimal value.", ERROR);
					return;
				}
				final long fileLength;//in bytes
				final String filePath = fieldFileInput.getText();
				try {
					fileLength = new File(filePath).length();
					if(fileLength == 0) {
						log("The given input file(\"" + filePath + "\") is invalid or empty.", ERROR);
						return;
					}
				} catch (Exception e) {
					log("Could not read the input file: \"" + filePath + "\".", ERROR);
					return;
				}
				//very high precision, so double it is.
				double totalScriptLengthPerInput = (assumedExtraBytes+amoutDataInTx)/amoutDataInTx;//in bytes of script per byte of input
				double totalCost = (totalScriptLengthPerInput*fileLength+assumedInputLength)*fee/1000d + ((double)(transactionCost)/amoutDataInTx)*(double)(fileLength)/100000000d;//sum of data fee-cost and each address transaction
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(8);
				nf.setMinimumFractionDigits(8);
				nf.setRoundingMode(RoundingMode.UP);
				log("The estimated total BTC cost of storing " + fileLength + " bytes worth of data into transactions, is " + nf.format(totalCost) + " BTC, assuming the input script only has one input(the entire input plus the start flags of the output with only one input transaction is " + assumedInputLength + " bytes in size) and there needs to be " + assumedExtraBytes + " extra bytes for each " + amoutDataInTx + " bytes of data.", INFO);
			}
		});
		panel_4.add(btnEstimateBtcNeeded, BorderLayout.SOUTH);
		
		JButton btnClearLog = new JButton("Clear");
		btnClearLog.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearlog();
			}
		});
		contentPane.add(btnClearLog, BorderLayout.SOUTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		JLabel lblInfo = new JLabel("Info/Progress:");
		scrollPane.setColumnHeaderView(lblInfo);
		
		textPane = new JTextPane();
		appendToLog(">", POINTYColor);
		textPane.setFocusable(false);
		textPane.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
	}
	
	private static String craftValidAddressFromData(byte[] data) {//data needs to be amoutDataInTx (20 bytes) in size.
		if(data.length != amoutDataInTx)
			throw new IllegalArgumentException(data.length + " is not equal to the amount of data per transaction(" + amoutDataInTx + ")!");
		String out = "";
		byte[] toBeEncoded = new byte[amoutDataInTx + 1 + 4];//0x00 byte prefix and fist four bytes of sha256 double hash.
		byte[] toBeHashed = new byte[data.length+1];
		toBeEncoded[0] = (byte)0x00;//= version 1 = P2PKH-Method
		toBeHashed[0] = (byte)0x00;//= Version 1 = P2PKH-Method
		for (int i = 0; i < data.length; i++) {
			toBeEncoded[1+i] = data[i];//guaranteed not to out of bound
			toBeHashed[1+i] = data[i];//guaranteed not to out of bound
		}
		byte[] doubleHash = sha256(sha256(toBeHashed));
		for (int i = 0; i < 4; i++)
			toBeEncoded[1+data.length+i] = doubleHash[i];
		out = encodeBase58(toBeEncoded);
		return out;
	}
	private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

	public static final char[] numbers = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
	public static final BigInteger INT58 = BigInteger.valueOf(58);
	private static String encodeBase58(byte[] input) {
    	String out = "";
    	BigInteger integer = new BigInteger(1, input);//effectively unsigned now
    	while(integer.compareTo(BigInteger.ZERO)==1) {//zero bytes at the beginning of input will be ignored in this while loop
    		BigInteger p = integer.mod(INT58);
    		out = numbers[p.intValue()] + out;
    		integer = integer.subtract(p).divide(INT58);
    	}
    	for (int i = 0; i < input.length; i++) {//therefore they need to be dealt with here
			if(input[i]!=0)
				break;
			out = numbers[0] + out;
    	}
    	return out;
	}
	private static float convertSatoshiToBTC(int amountSatoshi) {
		return (float)(amountSatoshi)/100000000f;
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
	
}
