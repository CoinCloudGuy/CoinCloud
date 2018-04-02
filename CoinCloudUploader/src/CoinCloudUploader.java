import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/*
 * Conventions: All methods starting with '_' are deemed helper methods and not intended for general use.
 * 				'field' is a prefix for anything the user could enter text.
 * 				'dfield' is an text field just for displaying information
 * 
 */
public class CoinCloudUploader extends JFrame {
	private static final long serialVersionUID = -3015288968267047980L;
	
	//general constants
	public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE-2;//also last (valid) array index
	//program constants
	public static final String version = "0.0.2-Alpha";
	public static final String name = "CoinCloudUploader";
	public static final int ERROR = 0;
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final Color ERRORColor = new Color(255, 0, 0);
	public static final Color INFOColor = new Color(0, 0, 0);
	public static final Color WARNINGColor = new Color(155, 155, 0);
	public static final Color POINTYColor = new Color(0, 255, 0);
	//UI constants
	public static final String PRICE_PER_ADDRESS_default = "546";//in satoshi per address
	public static final int PRICE_PER_ADDRESS_default_value = 546;//in satoshi per address
	public static final String TRANSACTION_FEE_default = "1185";//in Satoshi per kB
	public static final double TRANSACTION_FEE_default_value = 1185.0d;// in Satoshi per kB
	//network constants
	public static final int TESTNET = 1;
	public static final int MAINNET = 0;
	public static int BLOCKCHAIN_Network = MAINNET;
	public static final String BLOCKCHAIN_Host = "blockchain.info";
	public static final String BLOCKCHAIN_TestNetHost = "testnet.blockchain.info";
	public static final String BLOCKCHAIN_HostTor = "blockchainbdgpzk.onion";
	public static final String URL_CheckTorConnection = "https://ipinfo.io/ip";//would normally just return the current public ip-address
	public static final int TIMEOUT_CheckTorConnection = 20;//in seconds
	public static final Proxy TORProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9150));
	
	//container
	private JPanel FileInputContainer;
	private JPanel BTCPaymentContainer;
	private JPanel BTCAddressContainer;
	private JPanel CraftContainer;
	private JPanel BroadcastContainer;
	//visible components
	private JSplitPane contentPane;
	private JTextField field_InputFilePath;
	private JTextPane dfield_AddressToPayTo;
	private JTextPane dfield_CalculatedAmountToPay;
	private JTextField field_TransactionFee;
	private JTextField field_PricePerAddress;
	private JTextPane infoBox;
	private JCheckBox chckbxUseTorCheck;
	private JCheckBox chckbxUseTorBroadcast;
	//internal data
	private byte[] fileContents;
	
	private long storedSatoshiPerAddress;//in Satoshi
	private double storedTransactionFee;//in Satoshi per kB
	private long calculatedEstamatedCost;//in Satoshi
	private long calculatedExactCost;//in Satoshi
	private ECPublicKey addressPublicKey;
	private ECPrivateKey addressPrivateKey;
	
	private LinkedList<byte[][]> unspendOutputs;
	private long sumOfAllSatoshiToSpent;
	
	private int amountOfAddressesUsed;
	
	private byte[] craftedTransaction;
	//Miscellaneous
	private ButtonGroup BTCUnits;
	private JRadioButton rdbtnBtc;
	private JRadioButton rdbtnMbtc;
	private JRadioButton rdbtnBits;
	private JRadioButton rdbtnSatoshi;
	
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
		this.fileContents = new byte[] {};//Guaranteed to never be null
		this.storedSatoshiPerAddress = -1;
		this.storedTransactionFee = -1.0;
		this.calculatedEstamatedCost = -1;
		this.calculatedExactCost = -1;
		this.sumOfAllSatoshiToSpent = -1;
		this.amountOfAddressesUsed = -1;
		this.unspendOutputs = null;
		this.craftedTransaction = null;
		setTitle(name + " " + version + (BLOCKCHAIN_Network==MAINNET ? "" : " On the Bitcoin testnet-network!"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//components
		contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JPanel actionsContainer = new JPanel();
		contentPane.setLeftComponent(actionsContainer);
		actionsContainer.setLayout(new BorderLayout(0, 0));
		
		FileInputContainer = new JPanel();
		actionsContainer.add(FileInputContainer, BorderLayout.NORTH);
		FileInputContainer.setLayout(new BorderLayout(0, 0));
		
			JLabel lblInputfile = new JLabel("Input file:");
			FileInputContainer.add(lblInputfile, BorderLayout.NORTH);
			
			field_InputFilePath = new JTextField();
			FileInputContainer.add(field_InputFilePath, BorderLayout.CENTER);
			field_InputFilePath.setColumns(10);
			
			JButton btnChooseInput = new JButton("Choose");
			btnChooseInput.addActionListener(_getChooseButtonActionListener());
			FileInputContainer.add(btnChooseInput, BorderLayout.EAST);
			
			JButton btnNext_ChooseInput = new JButton("Next");
			btnNext_ChooseInput.addActionListener(_getBtnNext_ChooseInputActionListener());
			FileInputContainer.add(btnNext_ChooseInput, BorderLayout.SOUTH);
		
		JPanel relay01 = new JPanel();
		actionsContainer.add(relay01, BorderLayout.SOUTH);
		relay01.setLayout(new BorderLayout(0, 0));
		
			BTCPaymentContainer = new JPanel();
			relay01.add(BTCPaymentContainer, BorderLayout.NORTH);
			BTCPaymentContainer.setLayout(new BorderLayout(0, 0));
		
				JLabel label = new JLabel("Amount of satoshi per generated address:");
				BTCPaymentContainer.add(label, BorderLayout.NORTH);
				
				field_PricePerAddress = new JTextField(PRICE_PER_ADDRESS_default);
				field_PricePerAddress.setColumns(10);
				BTCPaymentContainer.add(field_PricePerAddress, BorderLayout.CENTER);
				
				JButton btnDefault_PricePerAddress = new JButton("Default");
				btnDefault_PricePerAddress.addActionListener(_getBtnDefault_PricePerAddressActionListener());
				BTCPaymentContainer.add(btnDefault_PricePerAddress, BorderLayout.EAST);
				
				JPanel BTCPaymentContainerRelay01 = new JPanel();
				BTCPaymentContainer.add(BTCPaymentContainerRelay01, BorderLayout.SOUTH);
				BTCPaymentContainerRelay01.setLayout(new BorderLayout(0, 0));
		
					JLabel lblAmountSatoshiPer = new JLabel("Amount of satoshi per kB transaction size:");
					BTCPaymentContainerRelay01.add(lblAmountSatoshiPer, BorderLayout.NORTH);
					
					field_TransactionFee = new JTextField(TRANSACTION_FEE_default);
					BTCPaymentContainerRelay01.add(field_TransactionFee, BorderLayout.CENTER);
					field_TransactionFee.setColumns(10);
					
					JButton btnDefault_TransactionFee = new JButton("Default");
					btnDefault_TransactionFee.addActionListener(_getBtnDefault_TransactionFeeActionListener());
					BTCPaymentContainerRelay01.add(btnDefault_TransactionFee, BorderLayout.EAST);
					
					JButton btnNext_Payment = new JButton("Next");
					btnNext_Payment.addActionListener(_getBtnNext_PaymentActionListener());
					BTCPaymentContainerRelay01.add(btnNext_Payment, BorderLayout.SOUTH);
		
			JPanel relay02 = new JPanel();
			relay01.add(relay02, BorderLayout.CENTER);
			relay02.setLayout(new BorderLayout(0, 0));
		
				BTCAddressContainer = new JPanel();
				relay02.add(BTCAddressContainer, BorderLayout.NORTH);
				BTCAddressContainer.setLayout(new BorderLayout(0, 0));
		
					JPanel BTCAddressContainerRelay01 = new JPanel();
					BTCAddressContainer.add(BTCAddressContainerRelay01, BorderLayout.NORTH);
					BTCAddressContainerRelay01.setLayout(new BorderLayout(0, 0));
		
						JLabel lblAmountToPay = new JLabel("Amount to pay:");
						BTCAddressContainerRelay01.add(lblAmountToPay, BorderLayout.NORTH);
						
						dfield_CalculatedAmountToPay = getDField("");
						BTCAddressContainerRelay01.add(dfield_CalculatedAmountToPay, BorderLayout.CENTER);
						
						JPanel BTCUnitContainer = new JPanel();
						BTCAddressContainerRelay01.add(BTCUnitContainer, BorderLayout.SOUTH);
						
							
							BTCUnits = new ButtonGroup();
							
							JLabel lblShowPriceIn = new JLabel("Show price in: ");
							BTCUnitContainer.add(lblShowPriceIn);
							
							rdbtnBtc = new JRadioButton("BTC");
							rdbtnBtc.setSelected(true);
							rdbtnBtc.addItemListener(_getItemListener());
							BTCUnits.add(rdbtnBtc);
							BTCUnitContainer.add(rdbtnBtc);
							
							rdbtnMbtc = new JRadioButton("mBTC");
							rdbtnMbtc.addItemListener(_getItemListener());
							BTCUnits.add(rdbtnMbtc);
							BTCUnitContainer.add(rdbtnMbtc);
							
							rdbtnBits = new JRadioButton("Bits");
							rdbtnBits.addItemListener(_getItemListener());
							BTCUnits.add(rdbtnBits);
							BTCUnitContainer.add(rdbtnBits);
							
							rdbtnSatoshi = new JRadioButton("Satoshi");
							rdbtnSatoshi.addItemListener(_getItemListener());
							BTCUnits.add(rdbtnSatoshi);
							BTCUnitContainer.add(rdbtnSatoshi);
		
					JPanel BTCAddressContainerRelay02 = new JPanel();
					BTCAddressContainer.add(BTCAddressContainerRelay02, BorderLayout.CENTER);
					BTCAddressContainerRelay02.setLayout(new BorderLayout(0, 0));
		
						JLabel lblAddress = new JLabel("Address");
						BTCAddressContainerRelay02.add(lblAddress, BorderLayout.NORTH);
						
						dfield_AddressToPayTo = getDField("");
						BTCAddressContainerRelay02.add(dfield_AddressToPayTo, BorderLayout.CENTER);
						
						JButton btnBTCAddress_ViewDetais = new JButton("More...");
						btnBTCAddress_ViewDetais.addActionListener(_getBtnBTCAddress_ViewDetaisActionListener());
						BTCAddressContainerRelay02.add(btnBTCAddress_ViewDetais, BorderLayout.EAST);
						
						chckbxUseTorCheck = new JCheckBox("Use Tor to check balance"); 
						chckbxUseTorCheck.setSelected(BLOCKCHAIN_Network==MAINNET);
						BTCAddressContainerRelay02.add(chckbxUseTorCheck, BorderLayout.SOUTH);
		
					JButton btnNext_BTCAddress = new JButton("Next");
					btnNext_BTCAddress.addActionListener(_getBtnNext_BTCAddressActionListener());
					BTCAddressContainer.add(btnNext_BTCAddress, BorderLayout.SOUTH);
		
				JPanel relay03 = new JPanel();
				relay02.add(relay03, BorderLayout.CENTER);
				relay03.setLayout(new BorderLayout(0, 0));
		
					CraftContainer = new JPanel();
					relay03.add(CraftContainer, BorderLayout.NORTH);
					CraftContainer.setLayout(new BorderLayout(0, 0));
		
						JButton btnCraftTransaction = new JButton("Craft transaction");
						btnCraftTransaction.addActionListener(_getBtnCraftTransactionActionListener());
						CraftContainer.add(btnCraftTransaction, BorderLayout.NORTH);
						
						JButton btnNext_CraftTransaction = new JButton("Next");
						btnNext_CraftTransaction.addActionListener(_getBtnNext_CraftTransactionActionListener());
						CraftContainer.add(btnNext_CraftTransaction, BorderLayout.SOUTH);
		
					JPanel relay04 = new JPanel();
					relay03.add(relay04, BorderLayout.CENTER);
					relay04.setLayout(new BorderLayout(0, 0));
		
						BroadcastContainer = new JPanel();
						relay04.add(BroadcastContainer, BorderLayout.NORTH);
						BroadcastContainer.setLayout(new BorderLayout(0, 0));
		
							chckbxUseTorBroadcast = new JCheckBox("Use Tor"); 
							chckbxUseTorBroadcast.setSelected(BLOCKCHAIN_Network==MAINNET);
							BroadcastContainer.add(chckbxUseTorBroadcast, BorderLayout.EAST);
							
							JButton btnBroadcastTransaction = new JButton("Broadcast");
							btnBroadcastTransaction.addActionListener(_getBtnBroadcastTransactionActionListener());
							BroadcastContainer.add(btnBroadcastTransaction, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(300, 2));
		scrollPane.setMinimumSize(new Dimension(300, 23));
		contentPane.setRightComponent(scrollPane);
		
			JLabel lblNewLabel = new JLabel("Information");
			lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
			scrollPane.setColumnHeaderView(lblNewLabel);
			
			infoBox = new JTextPane();
			_appendToLog(">", POINTYColor);
			infoBox.setFocusable(false);
			infoBox.setBorder(new LineBorder(new Color(192, 192, 192), 2, true));
			infoBox.setEditable(false);
			scrollPane.setViewportView(infoBox);
		
			//set dimension
		pack();
		//set position
		Dimension winDim = getWindowDimension();
		setBounds(
				(winDim.width-getWidth())/2, //center x
				(winDim.height-getHeight())/2, //center y
				getWidth(),//set by pack()
				getHeight());//set by pack()
		setMinimumSize(new Dimension(getWidth(), getHeight()));
		//disable later options
		setBTCPaymentContainerEnabled(false);
		setBTCAddressContainerEnabled(false);
		setCraftContainerEnabled(false);
		setBroadcastContainerEnabled(false);
	}
//----------------------------------------------------------------------------------------------------------------------------------------

	//		Class methods.
	
//----------------------------------------------------------------------------------------------------------------------------------------

//----------------------------------------------------------------------------------------------------------------------------------------
	//Listener
	//next buttons
	private ActionListener _getBtnNext_ChooseInputActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String filePath = field_InputFilePath.getText();
				if(filePath == null) {
					log("There was no file path given.",ERROR);
					return;
				}
				if(filePath.equals("")) {
					log("The given file path is empty.", ERROR);
					return;
				}
				File file = new File(filePath);
				final int fileLength;
				try {
					fileLength = (int) file.length();
					if(fileLength==0) {
						log("The given file has been empty or could not be red.", ERROR);
						return;
					}else if(fileLength>MAX_ARRAY_LENGTH) {
						log("This program does only support files that are between (and including) 0 and " + MAX_ARRAY_LENGTH + " bytes in size. \"" + file.getAbsolutePath() + "\" of size " + fileLength + " bytes is not within these parameters." , ERROR);
						return;
					}
				}catch(Exception ex) {
					log("The program does not have permission to access file:\"" + file.getAbsolutePath() + "\".",ERROR);
					return;
				}
				try {
					try {
						fileContents = new byte[fileLength];
					}catch(OutOfMemoryError err) {
						log("The Java VM does not have enough memory to tore the file. try restarting the program after granting the Java VM more memory.", ERROR);
						fileContents = new byte[] {};
						return;
					}
					FileInputStream fis = new FileInputStream(file);
					fis.read(fileContents);
					if(fis.read() != -1) {
						log("The reported file length of " + fileLength + " did not reflect the actual file length!", ERROR);
						fileContents = new byte[] {};//clean up memory
						fis.close();//clean up memory
						return;
					}
					fis.close();
				} catch (IOException e1) {
					log("The file \"" + file.getAbsolutePath() + "\" could not be red.", ERROR);
					return;
				}
				//file is in memory, move on to next stage
				setInputContainerEnabled(false);
				setBTCPaymentContainerEnabled(true);
				BTCPaymentContainer.requestFocus();
			}
		};
	}
	private ActionListener _getBtnNext_PaymentActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//get Fee and satoshi per address
				final String pricePerAddress = field_PricePerAddress.getText();
				try {
					storedSatoshiPerAddress = Long.parseLong(pricePerAddress);
					if(storedSatoshiPerAddress < 0) {
						log("\"" + pricePerAddress + "\" is an negative integer. Please enter a positive integer.", ERROR);
						storedSatoshiPerAddress = -1;//reset to invalid
						return;
					}
				} catch (Exception ex) {
					log("\"" + pricePerAddress + "\" is not an integer. Please enter a integer!", ERROR);
					return;
				}
				
				final String transactionFee = field_TransactionFee.getText();
				try {
					storedTransactionFee = Double.parseDouble(transactionFee);
					if(storedTransactionFee < 0) {
						log("\"" + transactionFee + "\" is a negative number. Please enter a positive number.", ERROR);
						storedSatoshiPerAddress = -1;//reset to invalid
						storedTransactionFee = -1;//reset to invalid
						return;
					}
				} catch (Exception ex) {
					log("\"" + pricePerAddress + "\" is not a number. Please enter a number!", ERROR);
					storedSatoshiPerAddress = -1;//reset to invalid
					return;
				}
				int transactionlength = calculateTransactionLength(fileContents.length, 1);//1 is assumed
				calculatedEstamatedCost = (long)Math.ceil(transactionlength*(storedTransactionFee/1000)) // '/1000' conversion from kB to byte
								+ ((long)calculateAmountOfAddressesNeeded(fileContents.length))*storedSatoshiPerAddress;
				
				//create one-time use Bitcoin address.
				ECKeyPair pair = generateKeyPair();
				if(pair == null) {
					log("Your operating system does not provide the \"SHA1PRNG\" random number generator or the \"secp256k1\" elliptic curve. This is required in order to create bitcoin addresses. You will (probably) never be able to fix this, sorry.", ERROR);
					storedSatoshiPerAddress = -1;//reset to invalid
					storedTransactionFee = -1;//reset to invalid
					calculatedEstamatedCost = -1;//reset to invalid
					return;
				}
				addressPublicKey = pair.puk;
				addressPrivateKey = pair.prk;
				dfield_CalculatedAmountToPay.setText(convertSatoshiToBTC(calculatedEstamatedCost)+"");
				dfield_AddressToPayTo.setText(createBitcoinAddress(addressPublicKey, BLOCKCHAIN_Network));
				setBTCPaymentContainerEnabled(false);
				setBTCAddressContainerEnabled(true);
				BTCAddressContainer.requestFocus();
			}
		};
	}
	private ActionListener _getBtnNext_BTCAddressActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//disable nextbutton to prevent spam
				if(!(e.getSource() instanceof JButton))
					return;
				((JButton)e.getSource()).setEnabled(false);
				//get data for worker thread
				final boolean useTor = chckbxUseTorCheck.isSelected();
				final String bitcoinAddress = createBitcoinAddress(addressPublicKey, BLOCKCHAIN_Network);
				SwingWorker<LinkedList<byte[][]>, Void> worker = new SwingWorker<LinkedList<byte[][]>, Void>(){
					
					@Override //isSuccsess
					protected LinkedList<byte[][]> doInBackground() throws Exception {
						LinkedList<byte[][]> results;
						try {
							results = obtainAddressInformation(bitcoinAddress, useTor, BLOCKCHAIN_Network);
						}catch (IOException e) {
							return null;
						}
						//check for not validity
						for (byte[][] result : results) {
							if(result[0].length != 32)
								return null;
							if(result[1].length != 4)
								return null;
							if(result[3].length != 8)
								return null;
							if(result[4].length != 8)
								return null;
						}
						return results;
					}
					@Override
					protected void done() {
						try {
							LinkedList<byte[][]> results = get();
							long sumOfBTC = 0;
							if(results == null) {
								log("The retrieved blockchain data has been corrupted in transport, please try again.", ERROR);
								((JButton)e.getSource()).setEnabled(true);
								return;
							}
							if(results.isEmpty()) {
								log("The address does not have any spendable money! Transfer at least " + calculatedEstamatedCost + " Satoshi to \"" + bitcoinAddress + "\".", ERROR);
								((JButton)e.getSource()).setEnabled(true);
								return;
							}
							for (byte[][] result : results) {
								if(new BigInteger(result[4]).compareTo(BigInteger.ZERO)>0) {//read as 'if(numberOfConfirmations>0)'
									sumOfBTC += Integer.toUnsignedLong(Integer.parseUnsignedInt(switchEndianess(convertToHex(result[3])), 16));
								}else {
									log("The transaction: \"" + convertToHex(result[0]) + "\" does not have any confirmations. Try again later(in about 10 minutes).", ERROR);
									((JButton)e.getSource()).setEnabled(true);
									return;
								}
							}
							
							long realTransactionLength = calculateTransactionLength(fileContents.length, results.size());
							calculatedExactCost = (long)Math.ceil(realTransactionLength*(storedTransactionFee/1000)) // '/1000' conversion from kB to byte
									+ ((long)calculateAmountOfAddressesNeeded(fileContents.length))*storedSatoshiPerAddress;
							if(sumOfBTC>=calculatedExactCost) {
								if(sumOfBTC == calculatedExactCost) {
									log("The address has currently: " + sumOfBTC + " Satoshi. This is exactly the amount needed for this transaction.",INFO);
								}else {
									log("The address has currently: " + sumOfBTC + " Satoshi. This is more than is needed for this transaction. Note that all funds, that are now deposited on this address, will be used in this transaction. You will loose all money that is now on that address(" + sumOfBTC + " Satoshi, meaning you will pay " + (sumOfBTC-calculatedExactCost) + " more Satoshi, than the necessary " + calculatedExactCost + ". That extra money will be awarded to the miner, that mines your transaction.). Should you not want this, grab the addresses privatekey by pressing the 'More...' button on the right hand side of the Bitcoin address and transfer the money back to your original account.",WARNING);
								}
								sumOfAllSatoshiToSpent = sumOfBTC;
								unspendOutputs = results;
								setBTCAddressContainerEnabled(false);//next button would be disabled here anyway so no need to enable it here
								setCraftContainerEnabled(true);
								CraftContainer.requestFocus();
								return;
							}else {
								log("The is not enough money to pay for the transaction to be accepted.(needed: " + calculatedExactCost + " Satoshi, available: " + sumOfBTC + ").", ERROR);
								calculatedExactCost = -1;
								((JButton)e.getSource()).setEnabled(true);
								return;
							}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
							log("An error in the worker thread has occurred, please try again.", ERROR);
							//do nothing, abort handled below
						}
						//some error, reset anything
						calculatedExactCost = -1;
						((JButton)e.getSource()).setEnabled(true);
					}
					
				};
				worker.execute();
			}
		};
	}
	private ActionListener _getBtnNext_CraftTransactionActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(craftedTransaction == null) {
					log("The transaction has not been crafted yet. Please press the 'Craft transaction'-button.", ERROR);
					return;
				}
				if(craftedTransaction.length <= fileContents.length) {
					log("The crafted transaction is smaller in size than the original file contents. This is an error. Please try again, by pressing the 'Craft transaction'-button", ERROR);
					return;
				}
				long realCost = (long)Math.ceil(craftedTransaction.length*(storedTransactionFee/1000)) // '/1000' conversion from kB to byte
						+ ((long)amountOfAddressesUsed)*storedSatoshiPerAddress;
				
				if(sumOfAllSatoshiToSpent<realCost) {
					log("The final check, on whether or not the money on the would be enough for the transaction, has shown, that the current amount of " + sumOfAllSatoshiToSpent + " Satoshi is not enough to pay for the transaction(final price: " + realCost + " Satoshi).", ERROR);
					return;
				}
				setCraftContainerEnabled(false);
				setBroadcastContainerEnabled(true);
				BroadcastContainer.requestFocus();
			}
		};
	}
	//broadcast button
	private ActionListener _getBtnBroadcastTransactionActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//lock broadcast button
				if(!(e.getSource() instanceof JButton))
					return;
				((JButton)e.getSource()).setEnabled(false);
				//check transaction validness one last time
				
				//send the transaction to the blockchain.info publish server and broadcast it to the rest of the world.
				final boolean useTor = chckbxUseTorBroadcast.isSelected();
				SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>(){

					@Override
					protected Boolean doInBackground() throws Exception {
						try {
							boolean succsess = broadcastTransaction(craftedTransaction, useTor, BLOCKCHAIN_Network);
							return succsess;
						} catch (IOException e2) {
							return false;
						}
					}
					
					@Override
					protected void done() {
						try {
							if(get().booleanValue()) {//was succsessfull
								log("The transaction has been successfully broadcasted to the bitcoin network. Note that it can take up to 3 hours before your transaction is confirmed.", INFO);
								String transactionID = getTransactionID(craftedTransaction);
								log("The transaction ID is: " + transactionID, INFO);
								log("You can check whether or not the transactions is confirmed here: https://" + BLOCKCHAIN_Host + "/tx/" + transactionID, INFO);
								log("Restart the program to start over.", INFO);
								((JButton)e.getSource()).setEnabled(true);
								return;
							}else {
								log("The transaction could not be successfully broadcasted.", ERROR);
								((JButton)e.getSource()).setEnabled(true);
								return;
							}
						} catch (InterruptedException | ExecutionException ex) {
							log("The broadcast thread has been interrupted. This is not supposed to happen, please try again.", ERROR);
							((JButton)e.getSource()).setEnabled(true);
							return;
						}
					}
				};
				worker.execute();
			}
		};
	}
	
	//regular listeners
	
	private ActionListener _getBtnCraftTransactionActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final byte[] version = new byte[] {1,0,0,0};//0x01 in little endian
				final byte[] numOfInputsVarInt = convertToBytes(getVarIntFromLong(unspendOutputs.size()));
				byte[] preInput = appendBytes(new byte[][] {version, numOfInputsVarInt});
				//inputs computed later
				int amoutofAddresses = calculateAmountOfAddressesNeeded(fileContents.length);
				byte[][] outputData = new byte[amoutofAddresses][20];//20 byte packets, init value for java arrays is null (or rather 0x00 for bytes) for each element
				for (int i = 0; i < fileContents.length; i++) {
					outputData[i/20][i%20] = fileContents[i];
				}
				
				//insert all outputs
				byte[] postInput;//store result here
				byte[] numOfOutputsVarInt = convertToBytes(getVarIntFromLong(outputData.length));
				postInput = numOfOutputsVarInt;
				final byte[] outScriptPrefix = convertToBytes("76a914");//doesn't change for constant length of 20 bytes 
				final byte[] outScriptPostfix = convertToBytes("88ac");//doesn't change 
				final byte[] costPerAddress = convertToBytes(switchEndianess(ensureHexLengthOf(16, Long.toUnsignedString(storedSatoshiPerAddress, 16))));//doesn't change
				for (int i = 0; i < outputData.length; i++) {
					byte[] script = appendBytes(new byte[][] {outScriptPrefix, outputData[i], outScriptPostfix});
					byte[] scriptLengthVarInt = convertToBytes(getVarIntFromLong(script.length));
					postInput = appendBytes(new byte[][] {postInput, costPerAddress, scriptLengthVarInt, script});
				}
				outputData = new byte[][] {};//empty the outputData array since its not needed anymore and basically contains the file.
				byte[] lockTime = new byte[] {0,0,0,0};//as soon as possible (not locked)
				postInput = appendBytes(new byte[][] {postInput, lockTime});
				
				
				//create all input scripts
				//input script structure:script = OPpush 71 bytes + [signature] + OPpush 33 bytes + [compressed public key]
				
				// 21 in hex is 33 (OPpush 33 bytes)
				byte[] inputScriptPostfix = appendBytes(new byte[][] {convertToBytes("21"), getRawCompressedPublicKey(addressPublicKey)});
				
				byte[] hashTypeCode = new byte[] {1,0,0,0};//0x01 in little endian, needs to be in 4 bytes
				byte[] sequence = convertToBytes("ffffffff");//dont use RBF
				LinkedList<byte[]> inputscripts = new LinkedList<byte[]>();
				int i = 0;
				for (byte[][] unspendOutput : unspendOutputs) {
					byte[] modifiedInscriptlength = convertToBytes(getVarIntFromLong(unspendOutput[2].length));
					byte[] inputsBefore = getInputsBefore(unspendOutputs, i);
					byte[] inputsAfter = getInputsAfter(unspendOutputs, i);//              prev txID     prev tx out index   length of prev tx outscript  prev txOutscript   RBF stuff  rest of inputs   this tx outputs  hashtypecode
					byte[] combined = appendBytes(new byte[][] {preInput, inputsBefore, unspendOutput[0], unspendOutput[1],    modifiedInscriptlength,   unspendOutput[2]   ,sequence   , inputsAfter,      postInput, hashTypeCode});
					byte[] signature;
					try {
						System.out.println("Combined: " + convertToHex(combined));
						signature = getSignature(combined, addressPrivateKey);
					} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e1) {
						e1.printStackTrace();
						log("There was an error signing the transaction. It seems that your operating system does not provide ECDSA signature algorithms, this is a problem, that you can't do anything about. Sorry!", ERROR);
						return;//abort anything
					}
					System.out.println("signature length: " + signature.length);
					signature = appendBytes(new byte[][] {signature, new byte[] {hashTypeCode[0]}});
					inputscripts.add(appendBytes(new byte[][] {convertToBytes(getVarIntFromLong(signature.length)), signature, inputScriptPostfix}));
					i++;
				}
				
				
				//craft the final transaction byte[]
				byte[] finalTransaction = preInput;
				i=0;//reuse counter
				for (byte[][] unspendOutput : unspendOutputs) {
					byte[] script = inputscripts.get(i);//                          prev txID        prev tx out index     script length in varint                       script    sequence
					finalTransaction = appendBytes(new byte[][] {finalTransaction, unspendOutput[0], unspendOutput[1], convertToBytes(getVarIntFromLong(script.length)), script, sequence});
					i++;
				}
				finalTransaction = appendBytes(new byte[][] {finalTransaction, postInput});//append outputs and locktime
				craftedTransaction = finalTransaction;
				log("The transaction has been crafted!", INFO);
			}
		};
	}
	private static byte[] getInputsBefore(List<byte[][]> data, int i) {
		if(i==0)
			return new byte[] {};
		byte[] sequence = convertToBytes("ffffffff");//no RBF 
		byte[] out = new byte[] {};
		for (int j = 0; j < i; j++) {
			byte[][] sc = data.get(j);
			appendBytes(new byte[][] {out, sc[0], sc[1], new byte[] {0}, sequence});
		}
		return out;
	}
	private static byte[] getInputsAfter(List<byte[][]> data, int i) {
		if(i==data.size()-1)
			return new byte[] {};
		byte[] sequence = convertToBytes("ffffffff");//no RBF 
		byte[] out = new byte[] {};
		for (int j = i+1; j < data.size(); j++) {
			byte[][] sc = data.get(j);
			appendBytes(new byte[][] {out, sc[0], sc[1], new byte[] {0}, sequence});
		}
		return out;
	}
	private static byte[] getSignature(byte[] toSign, ECPrivateKey priv) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		Signature sig = Signature.getInstance("SHA256withECDSA");
		sig.initSign(priv);
		sig.update(sha256(toSign));
		return sig.sign();
	}
	
	private ItemListener _getItemListener() {
		return new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED && e.getSource() instanceof JRadioButton) {
					JRadioButton r = (JRadioButton)e.getSource();
					if(r == rdbtnBtc) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText(convertSatoshiToBTC(calculatedEstamatedCost)+"");
					}else if(r == rdbtnMbtc) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText(convertSatoshiTomBTC(calculatedEstamatedCost)+"");
					}else if(r == rdbtnBits) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText(convertSatoshiToBits(calculatedEstamatedCost)+"");
					}else if(r == rdbtnSatoshi) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText((calculatedEstamatedCost)+"");
					}else {
						System.err.println("Something went horrobly wrong!");
					}
				}
			}
		};
	}
	private ActionListener _getChooseButtonActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!(e.getSource() instanceof JButton))
					return;
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog((Component) e.getSource());
				File f = fc.getSelectedFile();
				if(f==null)//file chooser aborted or canceled
					return;
				if(f.isDirectory()) {
					log("The selected file is directory and not an file, choose an file!", ERROR);
					return;
				}
				if(!f.exists()) {
					log("The chosen file does not exist. Choose an file you want to be converted!", ERROR);
					return;
				}
				if(f.length() == 0) {
					log("The chosen file has no content(size: 0 bytes). Choose an file with content!", ERROR);
					return;
				}
				field_InputFilePath.setText(f.getAbsolutePath());
			}
		};
	}
	private ActionListener _getBtnDefault_TransactionFeeActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				field_TransactionFee.setText(TRANSACTION_FEE_default);
			}
		};
	}
	private ActionListener _getBtnDefault_PricePerAddressActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				field_PricePerAddress.setText(PRICE_PER_ADDRESS_default);
			}
		};
	}
	private ActionListener _getBtnBTCAddress_ViewDetaisActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(addressPrivateKey != null && addressPublicKey != null)
					openKeyViewWindow();
			}
		};
	}
//----------------------------------------------------------------------------------------------------------------------------------------
	//Public/private key window
	public void openKeyViewWindow() {
		JDialog dialouge = new JDialog(this);
		_setupKeyViewWindow(dialouge);
		dialouge.pack();
		dialouge.setBounds(
				getX() + (getWidth()-dialouge.getWidth())/2, //center x within main window
				getY() + (getHeight()-dialouge.getHeight())/2, //center y within main window
				dialouge.getWidth(),//set by pack()
				dialouge.getHeight());//set by pack()
		dialouge.setAutoRequestFocus(true);
		dialouge.setTitle("View Keys");
		dialouge.setVisible(true);
	}
	private void _setupKeyViewWindow(JDialog keyViewWindow) {
		keyViewWindow.getContentPane().setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		keyViewWindow.getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
			JPanel addressPanel = new JPanel();
			contentPanel.add(addressPanel);
				JLabel lblBtcAddress = new JLabel("BTC Address: ");
				addressPanel.add(lblBtcAddress);
				JTextPane dfield_Address = getDField(_getAddress_View());
				addressPanel.add(dfield_Address);
				JButton btnCopyBtcAddress = new JButton("Copy");
				btnCopyBtcAddress.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StringSelection selection = new StringSelection(dfield_Address.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, new ClipboardOwner() {
							@Override
							public void lostOwnership(Clipboard arg0, Transferable arg1) {
								// nothing
							}
						});
					}
				});
				addressPanel.add(btnCopyBtcAddress);
			
			JPanel privateKeyWIFPanel = new JPanel();
			contentPanel.add(privateKeyWIFPanel);
				JLabel lblPrivateKeywif = new JLabel("Private Key(WIF): ");
				privateKeyWIFPanel.add(lblPrivateKeywif);
				JTextPane dfield_PrivateKeyWIF = getDField(_getPrivateKeyWIF_View());
				privateKeyWIFPanel.add(dfield_PrivateKeyWIF);
				
				JButton btnCopyPrivateKeyWIF = new JButton("Copy");
				btnCopyPrivateKeyWIF.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StringSelection selection = new StringSelection(dfield_PrivateKeyWIF.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, new ClipboardOwner() {
							@Override
							public void lostOwnership(Clipboard arg0, Transferable arg1) {
								// nothing
							}
						});
					}
				});
				privateKeyWIFPanel.add(btnCopyPrivateKeyWIF);
				
			JPanel privateKeyHexPanel = new JPanel();
			contentPanel.add(privateKeyHexPanel);
				JLabel lblPrivateKeyhex = new JLabel("Private Key(hex): ");
				privateKeyHexPanel.add(lblPrivateKeyhex);
				JTextPane dfield_PrivateKeyHex = getDField(_getPrivateKeyHex_View());
				privateKeyHexPanel.add(dfield_PrivateKeyHex);
				JButton btnCopyPrivateKeyHex = new JButton("Copy");
				btnCopyPrivateKeyHex.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StringSelection selection = new StringSelection(dfield_PrivateKeyHex.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, new ClipboardOwner() {
							@Override
							public void lostOwnership(Clipboard arg0, Transferable arg1) {
								// nothing
							}
						});
					}
				});
				privateKeyHexPanel.add(btnCopyPrivateKeyHex);
				
			JPanel publicKeyHexPanel = new JPanel();
			contentPanel.add(publicKeyHexPanel);
				JLabel lblPublicKeyhex = new JLabel("Compressed public key(hex): ");
				publicKeyHexPanel.add(lblPublicKeyhex);
				JTextPane dfield_PublicKeyHex = getDField(_getPublicKeyHex_View());
				publicKeyHexPanel.add(dfield_PublicKeyHex);
				JButton btnCopyPublicKeyHex = new JButton("Copy");
				btnCopyPublicKeyHex.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StringSelection selection = new StringSelection(dfield_PublicKeyHex.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, new ClipboardOwner() {
							@Override
							public void lostOwnership(Clipboard arg0, Transferable arg1) {
								// nothing
							}
						});
					}
				});
				publicKeyHexPanel.add(btnCopyPublicKeyHex);
				
			JPanel publicUnComKeyHexPanel = new JPanel();
			contentPanel.add(publicUnComKeyHexPanel);
				JLabel lblPublicUnComKeyhex = new JLabel("Raw public key(hex): ");
				publicUnComKeyHexPanel.add(lblPublicUnComKeyhex);
				JTextPane dfield_PublicUnComKeyHex = getDField(_getUnComPublicKeyHex_View());
				publicUnComKeyHexPanel.add(dfield_PublicUnComKeyHex);
				JButton btnCopyUnComPublicKeyHex = new JButton("Copy");
				btnCopyUnComPublicKeyHex.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StringSelection selection = new StringSelection(dfield_PublicUnComKeyHex.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, new ClipboardOwner() {
							@Override
							public void lostOwnership(Clipboard arg0, Transferable arg1) {
								// nothing
							}
						});
					}
				});
				publicUnComKeyHexPanel.add(btnCopyUnComPublicKeyHex);
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			keyViewWindow.getContentPane().add(buttonPane, BorderLayout.SOUTH);
				JButton ImportButton = new JButton("Import");
				ImportButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						openKeyImportWindow(keyViewWindow);
					}
				});
				buttonPane.add(ImportButton);
				
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						keyViewWindow.dispose();
					}
				});
				buttonPane.add(okButton);
	}
	private String _getPrivateKeyWIF_View() {
		return createWIFPrivateKey(addressPrivateKey, BLOCKCHAIN_Network);
	}
	
	private String _getPrivateKeyHex_View() {
		return convertToHex(getRawPrivateKey(addressPrivateKey));
	}
	private String _getPublicKeyHex_View() {
		return convertToHex(getRawCompressedPublicKey(addressPublicKey));
	}
	private String _getUnComPublicKeyHex_View() {
		return convertToHex(getRawUncompressedPublicKey(addressPublicKey));
	}
	private String _getAddress_View() {
		return createBitcoinAddress(addressPublicKey, BLOCKCHAIN_Network);
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//import window
	
	public void openKeyImportWindow(JDialog daddy) {
		JDialog dialouge = new JDialog(this);
		_setupKeyImportWindow(dialouge, daddy);
		dialouge.pack();
		dialouge.setBounds(
				getX() + (getWidth()-dialouge.getWidth())/2, //center x within main window
				getY() + (getHeight()-dialouge.getHeight())/2, //center y within main window
				dialouge.getWidth() + 300,//set by pack()
				dialouge.getHeight());//set by pack()
		dialouge.setAutoRequestFocus(true);
		dialouge.setTitle("Import Keys");
		dialouge.setVisible(true);
	}
	
	private void _setupKeyImportWindow(JDialog keyImportWindow, JDialog daddy) {
		JPanel contentPanel = new JPanel();
		keyImportWindow.getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		keyImportWindow.getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			JPanel PrivateKeyPanel = new JPanel();
			PrivateKeyPanel.setLayout(new BorderLayout(2,2));
			contentPanel.add(PrivateKeyPanel);
				JLabel lblPrivateKey = new JLabel("Private key(WIF): ");
				PrivateKeyPanel.add(lblPrivateKey, BorderLayout.WEST);
				JTextField field_privatekey = new JTextField();
				PrivateKeyPanel.add(field_privatekey, BorderLayout.CENTER);
				field_privatekey.setColumns(10);
			JPanel PublicKeyPanel = new JPanel();
			PublicKeyPanel.setLayout(new BorderLayout(2,2));
			contentPanel.add(PublicKeyPanel);
				JLabel lblPublicKeyhex = new JLabel("Public key(hex): ");
				PublicKeyPanel.add(lblPublicKeyhex, BorderLayout.WEST);
				JTextField field_publickey = new JTextField();
				PublicKeyPanel.add(field_publickey, BorderLayout.CENTER);
				field_publickey.setColumns(10);
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			keyImportWindow.getContentPane().add(buttonPane, BorderLayout.SOUTH);
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						final String s_privateKey = field_privatekey.getText();
						final String s_publicKey = field_publickey.getText();
						//private key
						byte[] privateKey = decodeBase58(s_privateKey);
						//check private key validness
						if(privateKey == null) {
							log("The given private key(" + s_privateKey + ") is not in WIF format.", ERROR);
							return;
						}
						if (privateKey.length <30) {
							log("The given private key is to short in byte size.", ERROR);
							return;
						}
						byte[] given_checksum = Arrays.copyOfRange(privateKey, privateKey.length-1-3, privateKey.length);
						byte[] hash = sha256(sha256(Arrays.copyOfRange(privateKey, 0, privateKey.length-4)));
						boolean isEqual = true;
						for (int i = 0; i < given_checksum.length; i++)
							isEqual = given_checksum[i]==hash[i];
						if(!isEqual) {
							log("The given private key(" + s_privateKey + ") is not valid.", ERROR);
							return;
						}
						privateKey = Arrays.copyOfRange(privateKey, 1, privateKey.length-4);
						if(privateKey.length != 32)
							privateKey = Arrays.copyOfRange(privateKey, 0, privateKey.length-1);
						if(privateKey.length != 32) {
							log("The given private key(" + s_privateKey + ") does not have the required length of 32 bytes.", ERROR);
							return;
						}
						//public key
						byte[] publicKey = convertToBytes(s_publicKey);
						ECKeyPair pair = getKeyPair(privateKey, publicKey);
						if(pair == null) {
							log("The given keys where invalid!", ERROR);
							return;
						}
						try {
							if(isKeyPair(pair)) {
								addressPublicKey = pair.puk;
								addressPrivateKey = pair.prk;
								dfield_AddressToPayTo.setText(createBitcoinAddress(addressPublicKey, BLOCKCHAIN_Network));
								daddy.dispose();
								keyImportWindow.dispose();
								openKeyViewWindow();
							}else {
								log("The given private and public keys are not a key pair.", ERROR);
								return;
							}
						} catch (NoSuchAlgorithmException e) {
							log("Your operating system does not support the \"SHA256 with ECDSA\" signature. This is required for this entire program to work. There is nothing you can do about this, sorry.", ERROR);
							return;
						}
					}
				});
				buttonPane.add(okButton);
				keyImportWindow.getRootPane().setDefaultButton(okButton);
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						keyImportWindow.dispose();
					}
				});
				buttonPane.add(cancelButton);
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//enable/disable stuff
	
	public void setInputContainerEnabled(boolean enabled) {
		_setAllEnabled(FileInputContainer, enabled);
	}
	public void setBTCPaymentContainerEnabled(boolean enabled) {
		_setAllEnabled(BTCPaymentContainer, enabled);
	}
	public void setBTCAddressContainerEnabled(boolean enabled) {
		_setAllEnabled(BTCAddressContainer, enabled);
	}
	public void setCraftContainerEnabled(boolean enabled) {
		_setAllEnabled(CraftContainer, enabled);
	}
	public void setBroadcastContainerEnabled(boolean enabled) {
		_setAllEnabled(BroadcastContainer, enabled);
	}
	private void _setAllEnabled(JPanel p, boolean enabled) {
		Component[] coms = p.getComponents();
		for(Component c : coms) {
			if(c instanceof JPanel) 
				_setAllEnabled((JPanel)c, enabled);
			else
				c.setEnabled(enabled);
		}
	}
//----------------------------------------------------------------------------------------------------------------------------------------
	//logging stuff
	public void log(String message, int type) {
		synchronized (infoBox) {
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
			_appendToLog("[" + df.format(date) + "] " + specificText + message + "\n", specificColor);
			_appendToLog(">", POINTYColor);
		}
	}
	public void clearlog() {
		synchronized (infoBox) {
			infoBox.setText("");
			_appendToLog(">", POINTYColor);
		}
	}
	
    private void _appendToLog(String msg, Color c){//Concurrency save, because the method is used only once in an sync-block
	        StyleContext sc = StyleContext.getDefaultStyleContext();
	        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
	
	        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
	        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
	
	        int len = infoBox.getDocument().getLength();
	        infoBox.setCaretPosition(len);
	        infoBox.setCharacterAttributes(aset, false);
	        infoBox.setEditable(true);
	        infoBox.replaceSelection(msg);
	        infoBox.setEditable(false);
    }
    

//----------------------------------------------------------------------------------------------------------------------------------------

	//		Static/helper methods.

//----------------------------------------------------------------------------------------------------------------------------------------

	public static JTextPane getDField(String text) {
		JTextPane dfield = new JTextPane();
		dfield.setText(text);
		dfield.setEditable(false);
		dfield.setBackground(new Color(200, 200, 200));
		dfield.setBorder(new LineBorder(new Color(180, 180, 180)));
		return dfield;
	}

	private static Dimension getWindowDimension() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//Internet stuff
	public static final String prefix = "/unspent?active=";

	/**
	 *[0]= transactionID (little endian) length=32
	 *<br>[1]= txoutput index (little endian) length=4
	 *<br>[2]= txoutput script (big endian) length=?
	 *<br>[3]= value satoshi (little endian) length=8
	 *<br>[4]= confirmations (big endian) length=8
	 */
	public LinkedList<byte[][]> obtainAddressInformation(String address, boolean useTor, int network) throws IOException{
		HttpURLConnection c;
		if(useTor) {
			URL blockchainINFO = new URL("https",BLOCKCHAIN_HostTor, prefix + address);
			log("Connect to: " + "https://" + BLOCKCHAIN_HostTor + prefix + address + ".", INFO);
			c = (HttpURLConnection)blockchainINFO.openConnection(TORProxy);
		}else {
			URL blockchainINFO = new URL("https", network==MAINNET ? BLOCKCHAIN_Host : BLOCKCHAIN_TestNetHost, prefix + address);
			log("Connect to: " + "https://" + (network==MAINNET ? BLOCKCHAIN_Host : BLOCKCHAIN_TestNetHost) + prefix + address + ".", INFO);
			c = (HttpURLConnection)blockchainINFO.openConnection();
		}
		c.connect();
		if(c.getResponseCode() == 500) {
			return new LinkedList<byte[][]>();//empty list, because no funds available.
		}
		InputStream i = c.getInputStream();
		InputStreamReader r = new InputStreamReader(i);
		BufferedReader br = new BufferedReader(r);
		String line = "";
		String complete = "";
		log("Download blockchain data...", INFO);
		while((line = br.readLine()) != null) {
			complete += line;
		}
		br.close();
		System.out.println("Complete: " + complete);
		log("Parsing data...", INFO);
		LinkedList<byte[][]> out = new LinkedList<byte[][]>();
		complete = complete.replace(" ", "");
		String[] cut = complete.replace("{", ";").split(";");
		
		byte[] transactionID = new byte[] {};boolean foundID = false;
		byte[] txOutIndex = new byte[] {};boolean foundOutIndex = false;
		byte[] txOutScript = new byte[] {};boolean foundOutScript = false;
		byte[] valueSatoshi = new byte[] {};boolean foundSatoshi = false;
		byte[] confirmations = new byte[] {};boolean foundConfirmations = false;
		for(String sblock : cut) {
			String[] sBlockLines = sblock.replace("}","").replace("]", "").split(",");
			for(String sBlockLine : sBlockLines) {
				if(!sblock.startsWith("\""))
					continue;
				if(sBlockLine.startsWith("\"tx_hash\"")) {
					String s_txIdHex = sBlockLine.split(":")[1].split("\"")[1]; //"tx_hash":"LolHexHashHere", <- expected
					transactionID = convertToBytes(s_txIdHex);
					foundID = true;
				}else if(sBlockLine.startsWith("\"tx_output_n\"")) {
					String s_txOutIndex = sBlockLine.split(":")[1].split(",")[0];//"tx_output_n":1337, <- expected
					int index = Integer.parseUnsignedInt(s_txOutIndex);
					txOutIndex = convertToBytes(switchEndianess(ensureHexLengthOf(8, Integer.toHexString(index))));//toHexString() returns big endian
					foundOutIndex = true;
				}else if(sBlockLine.startsWith("\"script\"")) {
					String s_txOutScript = sBlockLine.split(":")[1].split("\"")[1]; //"script":"LolHexScriptHere", <- expected
					txOutScript = convertToBytes(s_txOutScript);
					foundOutScript = true;
				}else if(sBlockLine.startsWith("\"value_hex\"")) {
					String s_txOutValue = sBlockLine.split(":")[1].split("\"")[1]; //"value_hex":"LolHexValueHere", <- exected
					valueSatoshi = convertToBytes(switchEndianess(ensureHexLengthOf(16,s_txOutValue)));//is in big endian
					foundSatoshi = true;
				}else if(sBlockLine.startsWith("\"confirmations\"")) {
					String s_confirmations = sBlockLine.replace(":", ";").split(";")[1];//"tx_output_n":1337 <- expected
					int confirms = Integer.parseUnsignedInt(s_confirmations);
					confirmations = convertToBytes(ensureHexLengthOf(16, Integer.toHexString(confirms)));//toHexString() returns big endian, but we want this
					foundConfirmations = true;
				}
			}
			if(foundID && foundOutIndex && foundOutScript && foundSatoshi && foundConfirmations)
				out.add(new byte[][] {transactionID, txOutIndex, txOutScript, valueSatoshi, confirmations});
			foundID = false;
			foundOutIndex = false;
			foundOutScript = false;
			foundSatoshi = false;
			foundConfirmations = false;
		}
		return out;
	}
	public static final String pushPrefix = "/pushtx";
	//true if and only if the transaction has been successfully broadcasted!
	public boolean broadcastTransaction(byte[] transaction, boolean useTor, int network) throws IOException {
		String host = useTor ? BLOCKCHAIN_HostTor : (network == MAINNET ? BLOCKCHAIN_Host : BLOCKCHAIN_TestNetHost);
		HttpURLConnection con = (HttpURLConnection) new URL("https", host, pushPrefix).openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//			con.setRequestProperty("X-Forwarded-For", ""); at some point
		String data = "tx=" + convertToHex(transaction);

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(data);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		log("Sending 'POST' request to URL : " + host + pushPrefix, INFO);
		log("Post parameters : " + data, INFO);
		log("Response Code : " + responseCode,INFO);

		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return responseCode/100==2 && response.toString().equalsIgnoreCase("Transaction Submitted");
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//conversion stuff
	public static double convertSatoshiToBTC(long satoshi) {
		return ((double)satoshi)/100000000d;
	}
	public static double convertSatoshiTomBTC(long satoshi) {
		return ((double)satoshi)/100000d;
	}
	public static double convertSatoshiToBits(long satoshi) {
		return ((double)satoshi)/100d;
	}
	public static long convertBTCToSatoshi(double BTC) {
		return (long) Math.ceil(BTC*100000000d);
	}
	public static long convertmBTCToSatoshi(double mBTC) {
		return (long) Math.ceil(mBTC*100000d);
	}
	public static long convertBitsCToSatoshi(double bits) {
		return (long) Math.ceil(bits*100d);
	}
//----------------------------------------------------------------------------------------------------------------------------------------
	//length calculation stuff
	private static final int versionlength = 4;
	private static final int TxOutHashLength = 32;
	private static final int TxOutIndexLength = 4;
	private static final int standartInputScriptlength = 1+71+1+33;//[varInt of 71] + [length of signature + 1] + [varInt of 33] + [compressed EC public key]
	private static final int sequenceLength = 4;
	private static final int outValueLength = 8;
	private static final int standartOutPubScriptlength = 25;
	private static final int lockTimeLength = 4;
	public static int calculateTransactionLength(int amountOfBytes, int amountOfInputs) {
		final int VarIntNumOfTxInLength = getVarIntFromLong(amountOfInputs).length()/2;// '/2' because hex string
		int amountOfAddressesNeeded = calculateAmountOfAddressesNeeded(amountOfBytes);
		final int VarIntNumOfTxOutLength = getVarIntFromLong(amountOfAddressesNeeded).length()/2;// '/2' because hex string
		final int VarIntLengthOutScriptLength = getVarIntFromLong(standartOutPubScriptlength).length()/2;// '/2' because hex string
		return versionlength 
				//input portion
				+ VarIntNumOfTxInLength + (TxOutHashLength + TxOutIndexLength + standartInputScriptlength + sequenceLength)*amountOfInputs
				//output portion
				+ VarIntNumOfTxOutLength + (outValueLength+VarIntLengthOutScriptLength+standartOutPubScriptlength)*amountOfAddressesNeeded
				//other
				+ lockTimeLength;
	}
	public static int calculateInputLength() {
		return TxOutHashLength + TxOutIndexLength + standartInputScriptlength + sequenceLength;
	}
	public static int calculateAmountOfAddressesNeeded(int amountOfBytes) {
		return (amountOfBytes/20)+1;//split in 20 byte intervals and the last one padded with null bytes, therefore divide by 20 and ceil(or add 1 because integer) the result.
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//varInt stuff
	
	//will return lower case letters
	public static String getVarIntFromLong(long l) {
		//toHexString() returns big endian varInt however is stored in little endian
		if(Long.compareUnsigned(l, 0xFD)<0) {
			return ensureHexLengthOf(2, Long.toHexString(l));
		}else if(!(Long.compareUnsigned(l, 0xFFFF)>0)) {// l<=0xFFFF
			return "fd" + switchEndianess(ensureHexLengthOf(4, Long.toHexString(l)));
		}else if(!(Long.compareUnsigned(l, 0xFFFFFFFF)>0)) {// l<=0xFFFF FFFF
			return "fe" + switchEndianess(ensureHexLengthOf(8, Long.toHexString(l)));
		}else if(!(Long.compareUnsigned(l, -1l)>0)) {// l<=0xFFFF FFFF  FFFF FFFF; largest unsigned long possible equals -1 if signed long is used
			return "ff" + switchEndianess(ensureHexLengthOf(16, Long.toHexString(l)));
		}else {
			throw new IllegalArgumentException("An long larger than than 0xffffffffffffffff? This should be impossible!");
		}
	}
	//doesn't care about case
	public static long getLongFromVarInt(String varInt) {
		String prefix = varInt.substring(0, 2);
		if(prefix.equalsIgnoreCase("ff")) {
			return Long.parseUnsignedLong(switchEndianess(varInt.substring(2, 18)), 16);
		}else if(prefix.equalsIgnoreCase("fe")) {
			return Long.parseUnsignedLong(switchEndianess(varInt.substring(2, 10)), 16);
		}else if(prefix.equalsIgnoreCase("fd")) {
			return Long.parseUnsignedLong(switchEndianess(varInt.substring(2, 6)), 16);
		}else if(prefix.length() == 2) {
			return Long.parseUnsignedLong(varInt, 16);
		}else {
			throw new IllegalArgumentException();
		}
	}

//----------------------------------------------------------------------------------------------------------------------------------------
	//Bitcoin methods
	
	public static String createBitcoinAddress(ECPublicKey pub, int network) {
		byte[] out = new byte[20+1+4];
		byte[] dHashed = ripemd160(sha256(getRawCompressedPublicKey(pub)));
		byte[] toBeChecksumed = new byte[dHashed.length+1];
		toBeChecksumed[0] = network==MAINNET ? (byte)0x00 : (byte)0x6F;//main-net
		for (int i = 0; i < dHashed.length; i++) 
			toBeChecksumed[i+1] = dHashed[i];
		byte[] checksum = sha256(sha256(toBeChecksumed));
		out[0] = network==MAINNET ? (byte)0x00 : (byte)0x6F;//main-net
		for (int i = 0; i < dHashed.length; i++) 
			out[i+1] = dHashed[i];
		for (int i = 0; i < 4; i++)
			out[out.length-4+i] = checksum[i];
		return encodeBase58(out);
	}
	public static String createWIFPrivateKey(ECPrivateKey priv, int network) {
		byte[] rawKey = priv.getS().toByteArray();
		byte[] toBeHashed = new byte[1+32+1];
		toBeHashed[0] = network==MAINNET ? (byte)0x80 : (byte)0xEF;//main-net
		for (int i = 0; i < 32; i++)
			toBeHashed[toBeHashed.length-2 -i] = rawKey[rawKey.length-1 - i];
		toBeHashed[toBeHashed.length-1] = (byte)0x01;//Corresponds to compressed key
		byte[] dhash = sha256(sha256(toBeHashed));
		byte[] out = new byte[toBeHashed.length+4];
		int i;
		for (i = 0; i < toBeHashed.length; i++)
			out[i] = toBeHashed[i];
		for (int j = 0; j < 4; j++) 
			out[i+j] = dhash[j];
		return encodeBase58(out);
	}
	public static String getTransactionID(byte[] transaction) {
		return switchEndianess(convertToHex(sha256(sha256(transaction))));
	}
//----------------------------------------------------------------------------------------------------------------------------------------
	//Crypto methods
	
	private static class ECKeyPair{
		private ECPrivateKey prk;
		private ECPublicKey puk;
		public ECKeyPair(ECPublicKey puk, ECPrivateKey prk) {
			this.puk = puk;
			this.prk = prk;
		}
	}
	public static boolean isKeyPair(ECKeyPair pair) throws NoSuchAlgorithmException {
		try {
			byte[] data = "This is an string, that is just there to generate random data and thats just it.".getBytes();
			Signature sig = Signature.getInstance("SHA256withECDSA");
			sig.initSign(pair.prk);
			sig.update(data);
			byte[] signature = sig.sign();
			sig = Signature.getInstance("SHA256withECDSA");
			sig.initVerify(pair.puk);
			sig.update(data);
			return sig.verify(signature);
		} catch (InvalidKeyException | SignatureException e) {
			return false;
		}
		
	}
	public static byte[] getRawPrivateKey(ECPrivateKey priv) {
		byte[] data = priv.getS().toByteArray();
		if(data.length == 32)
			return data;
		byte[] out = new byte[32];
		for (int i = 0; i < out.length; i++)
			out[out.length-1 -i] = data[data.length-1 -i];
		return out;
	}
	public static byte[] getRawUncompressedPublicKey(ECPublicKey pub) {
		byte[] out = new byte[65];
		out[0] = (byte)0x04;//constant for uncompressed EC key
		byte[] X = pub.getW().getAffineX().toByteArray();
		byte[] Y = pub.getW().getAffineY().toByteArray();
		int i;
		for (i = 0; i < 32 && (Y.length-1 -i) >= 0; i++)
			out[out.length-1 -i] = Y[Y.length-1 -i];
		for (int j = 0; j < 32 && (X.length-1 -j) >= 0; j++)
			out[out.length-1 -i -j] = X[X.length-1 -j];
		return out;
	}
	public static byte[] getRawCompressedPublicKey(ECPublicKey pub) {
		byte[] out = new byte[33];
		out[0] = (byte) (pub.getW().getAffineY().testBit(0) ? 0x03 : 0x02);
		byte[] val = convertToBytes(ensureHexLengthOf(64, pub.getW().getAffineX().toString(16)));
		for (int i = 0; i < val.length; i++) {
			out[i+1] = val[i];
		}
		return out;
	}
	
	public static ECKeyPair generateKeyPair() {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");//this should be random enough for an one time only key only existing for maybe 2 hours at a time.
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
	        keyGen.initialize(new ECGenParameterSpec("secp256k1"), sr);
	        KeyPair pair = keyGen.generateKeyPair();
	        return new ECKeyPair((ECPublicKey) pair.getPublic(), (ECPrivateKey)pair.getPrivate());
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			return null;
		}
	}
	
	//import
	public static ECKeyPair getKeyPair(byte[] privateKey, byte[] publicKey) {
		ECPublicKey pub = getPublicKey(publicKey);
		ECPrivateKey priv = getPrivateKey(privateKey);
		if(pub != null && priv != null)
			return new ECKeyPair(pub, priv);
		else
			return null;
	}
	
	public static ECPrivateKey getPrivateKey(byte[] in) {
		if(in.length != 32)
			throw new IllegalArgumentException();
		try {
			AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
			params.init(new ECGenParameterSpec("secp256k1"));
			ECParameterSpec spec = params.getParameterSpec(ECParameterSpec.class);
	  	    return (ECPrivateKey)KeyFactory.getInstance("EC").generatePrivate(new ECPrivateKeySpec(new BigInteger(1,in), spec));
		} catch (InvalidParameterSpecException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static ECPublicKey getPublicKey(byte[] in) throws IllegalArgumentException{
		byte [] rawPublicKey;
		if(in.length == 65) {
			rawPublicKey = in;
		}else if(in.length == 33) {
			rawPublicKey = decompressECPubkey(in);
		}else {
			throw new IllegalArgumentException();
		}
		byte[] X = new byte[32];
		byte[] Y = new byte[32];
		int i;
		for (i = 0; i < X.length; i++) {
			X[i] = rawPublicKey[i+1];
		}
		for (int j = 0; j < Y.length; j++) {
			Y[j] = rawPublicKey[i+1+j];
		}
		try {
			AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
			params.init(new ECGenParameterSpec("secp256k1"));//Bitcoin curve
			ECParameterSpec spec = params.getParameterSpec(ECParameterSpec.class);
	  	    return (ECPublicKey)KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(new BigInteger(1, X), new BigInteger(1, Y)), spec));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidParameterSpecException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static final BigInteger MODULUS =  new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);// = q or p depends on the notation one is used to
	private static final BigInteger MODULUS_EX = new BigInteger("3fffffffffffffffffffffffffffffffffffffffffffffffffffffffbfffff0c", 16);//precomputed exponent (q+1)/4

	// Given a 33-byte compressed public key, this returns a 65-byte uncompressed key.(both x and y are big endian)
	static byte[] decompressECPubkey(byte[] compressed) {
		// Check length and type indicator byte
		if (compressed.length != 33 || compressed[0] != 2 && compressed[0] != 3)
			throw new IllegalArgumentException();
		final BigInteger X = new BigInteger(1, Arrays.copyOfRange(compressed, 1, compressed.length));  // unsigned! Range (0, 2^256)
		// y²=x³+b
		final BigInteger Ysquare = X.pow(3).add(BigInteger.valueOf(7));
		// sqrt(a) = a^((q+1)/4) , only works because we are on secp256k1 curve since q%4=3
		BigInteger Y = Ysquare.modPow(MODULUS_EX, MODULUS);
		//choose the correct solution, because sqrt always 2 mathematically correct solutions
		boolean tempIsEven = !Y.testBit(0);
		boolean yShouldBeEven = compressed[0] == 2;
		final BigInteger yCoord;
		if (tempIsEven && yShouldBeEven)
			yCoord = Y;
		else
			yCoord = MODULUS.subtract(Y);
		
		byte[] out = Arrays.copyOf(compressed, 65);
		out[0] = (byte)0x04;
		
		final byte[] yCoordBytes = yCoord.toByteArray();
		for (int i = 0; i < 32 && i < yCoordBytes.length; i++)
			out[out.length-1 - i] = yCoordBytes[yCoordBytes.length-1 - i];
		return out;
	}
//----------------------------------------------------------------------------------------------------------------------------------------
	//Hex-string stuff
	private static byte[] convertToBytes(String s) {
		String use = s;
		if(use.length()%2!=0)
			use = "0"+use;
		byte[] out = new byte[use.length()/2];
		for(int i = 0;i< use.length()/2;i++) {
			int b = (Integer.parseInt(use.substring(2*i, (2*i)+2),16) & 0xff);
			out[i] = ((byte)b);
		}
		return out;
	}
	
	private static String convertToHex(byte[] bs) {
		String out = "";
		for(byte b : bs) {
			String s_byte = Integer.toHexString(((int)b)&0xFF);
			while(s_byte.length() != 2)
				s_byte = "0"+s_byte;
			out += s_byte;
		}
		return out;
	}

	//length in chars or half bytes. assumes big endian.
	private static String ensureHexLengthOf(int length, String rawHex) {
		String out = rawHex.substring(0);
		while(out.length() != length)
			out = "0"+out;
		return out;
	}
	private static String switchEndianess(String in) {//switches from big to little and from little to big
		String out = "";
		int length = in.length();
		if(length % 2 != 0)
			throw new IllegalArgumentException();
		for (int i = 0; i < length; i = i+2)
			out = in.substring(i, i+2)+out;
		return out;
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//byte-level helper
	private static byte[] appendBytes(byte[][] toAppent) {
		int sum = 0;
		for (int i = 0; i < toAppent.length; i++) {
			sum += toAppent[i].length;
		}
		byte[] out = new byte[sum];
		int index =  0;
		for (int i = 0; i < toAppent.length; i++) {
			for (int j = 0; j < toAppent[i].length; j++) {
				out[index] = toAppent[i][j];
				index++;
			}
		}
		return out;
	}
//----------------------------------------------------------------------------------------------------------------------------------------
	//Base 58-encoding
	public static final String alphabetInBase58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	public static final char[] numbers = alphabetInBase58.toCharArray();
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

    private static byte[] decodeBase58(String input) {
        BigInteger num = BigInteger.ZERO;
        for (char t : input.toCharArray()) {
            int p = alphabetInBase58.indexOf(t);
            if (p == -1)
                return null;
            num = num.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(p));
        }
        //leading zeros will be ignored in loop above, they need to taken care of here.
        int extra = 0;
        for(char c : input.toCharArray()) {
        	if(c != '0')
        		break;
        	extra++;
        }
        byte[] numBytes = convertToBytes(num.toString(16));
        byte[] result = new byte[numBytes.length+extra];
        System.arraycopy(numBytes, 0, result, result.length - numBytes.length, numBytes.length);
        return result;
    }
//----------------------------------------------------------------------------------------------------------------------------------------
	//hash stuff
  	private static byte[] sha256(byte[] data) {
          try {
              MessageDigest md = MessageDigest.getInstance("SHA-256");
              md.update(data);
              return md.digest();
          } catch (NoSuchAlgorithmException e) {
              throw new IllegalStateException(e);
          }
      }	
  	
//credit to: https://github.com/nayuki/Bitcoin-Cryptography-Library/blob/master/java/io/nayuki/bitcoin/crypto/Ripemd160.java

  	private static final int BLOCK_LEN = 64;  // In bytes
  	
  	
  	
  	/*---- Static functions ----*/
  	
  	/**
  	 * Computes and returns a 20-byte (160-bit) hash of the specified binary message.
  	 * Each call will return a new byte array object instance.
  	 * @param msg the message to compute the hash of
  	 * @return a 20-byte array representing the message's RIPEMD-160 hash
  	 * @throws NullPointerException if the message is {@code null}
  	 */
  	public static byte[] ripemd160(byte[] msg) {
  		// Compress whole message blocks
  		Objects.requireNonNull(msg);
  		int[] state = {0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476, 0xC3D2E1F0};
  		int off = msg.length / BLOCK_LEN * BLOCK_LEN;
  		compress(state, msg, off);
  		
  		// Final blocks, padding, and length
  		byte[] block = new byte[BLOCK_LEN];
  		System.arraycopy(msg, off, block, 0, msg.length - off);
  		off = msg.length % block.length;
  		block[off] = (byte)0x80;
  		off++;
  		if (off + 8 > block.length) {
  			compress(state, block, block.length);
  			Arrays.fill(block, (byte)0);
  		}
  		long len = (long)msg.length << 3;
  		for (int i = 0; i < 8; i++)
  			block[block.length - 8 + i] = (byte)(len >>> (i * 8));
  		compress(state, block, block.length);
  		
  		// Int32 array to bytes in little endian
  		byte[] result = new byte[state.length * 4];
  		for (int i = 0; i < result.length; i++)
  			result[i] = (byte)(state[i / 4] >>> (i % 4 * 8));
  		return result;
  	}
  	
  	
  	
  	/*---- Private functions ----*/
  	
  	private static void compress(int[] state, byte[] blocks, int len) {
  		if (len % BLOCK_LEN != 0)
  			throw new IllegalArgumentException();
  		for (int i = 0; i < len; i += BLOCK_LEN) {
  			
  			// Message schedule
  			int[] schedule = new int[16];
  			for (int j = 0; j < BLOCK_LEN; j++)
  				schedule[j / 4] |= (blocks[i + j] & 0xFF) << (j % 4 * 8);
  			
  			// The 80 rounds
  			int al = state[0], ar = state[0];
  			int bl = state[1], br = state[1];
  			int cl = state[2], cr = state[2];
  			int dl = state[3], dr = state[3];
  			int el = state[4], er = state[4];
  			for (int j = 0; j < 80; j++) {
  				int temp;
  				temp = Integer.rotateLeft(al + f(j, bl, cl, dl) + schedule[RL[j]] + KL[j / 16], SL[j]) + el;
  				al = el;
  				el = dl;
  				dl = Integer.rotateLeft(cl, 10);
  				cl = bl;
  				bl = temp;
  				temp = Integer.rotateLeft(ar + f(79 - j, br, cr, dr) + schedule[RR[j]] + KR[j / 16], SR[j]) + er;
  				ar = er;
  				er = dr;
  				dr = Integer.rotateLeft(cr, 10);
  				cr = br;
  				br = temp;
  			}
  			int temp = state[1] + cl + dr;
  			state[1] = state[2] + dl + er;
  			state[2] = state[3] + el + ar;
  			state[3] = state[4] + al + br;
  			state[4] = state[0] + bl + cr;
  			state[0] = temp;
  		}
  	}
  	
  	
  	private static int f(int i, int x, int y, int z) {
  		assert 0 <= i && i < 80;
  		if (i < 16) return x ^ y ^ z;
  		if (i < 32) return (x & y) | (~x & z);
  		if (i < 48) return (x | ~y) ^ z;
  		if (i < 64) return (x & z) | (y & ~z);
  		return x ^ (y | ~z);
  	}
  	
  	
  	/*---- Class constants ----*/
  	
  	private static final int[] KL = {0x00000000, 0x5A827999, 0x6ED9EBA1, 0x8F1BBCDC, 0xA953FD4E};  // Round constants for left line
  	private static final int[] KR = {0x50A28BE6, 0x5C4DD124, 0x6D703EF3, 0x7A6D76E9, 0x00000000};  // Round constants for right line
  	
  	private static final int[] RL = {  // Message schedule for left line
  		 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,
  		 7,  4, 13,  1, 10,  6, 15,  3, 12,  0,  9,  5,  2, 14, 11,  8,
  		 3, 10, 14,  4,  9, 15,  8,  1,  2,  7,  0,  6, 13, 11,  5, 12,
  		 1,  9, 11, 10,  0,  8, 12,  4, 13,  3,  7, 15, 14,  5,  6,  2,
  		 4,  0,  5,  9,  7, 12,  2, 10, 14,  1,  3,  8, 11,  6, 15, 13};
  	
  	private static final int[] RR = {  // Message schedule for right line
  		 5, 14,  7,  0,  9,  2, 11,  4, 13,  6, 15,  8,  1, 10,  3, 12,
  		 6, 11,  3,  7,  0, 13,  5, 10, 14, 15,  8, 12,  4,  9,  1,  2,
  		15,  5,  1,  3,  7, 14,  6,  9, 11,  8, 12,  2, 10,  0,  4, 13,
  		 8,  6,  4,  1,  3, 11, 15,  0,  5, 12,  2, 13,  9,  7, 10, 14,
  		12, 15, 10,  4,  1,  5,  8,  7,  6,  2, 13, 14,  0,  3,  9, 11};
  	
  	private static final int[] SL = {  // Left-rotation for left line
  		11, 14, 15, 12,  5,  8,  7,  9, 11, 13, 14, 15,  6,  7,  9,  8,
  		 7,  6,  8, 13, 11,  9,  7, 15,  7, 12, 15,  9, 11,  7, 13, 12,
  		11, 13,  6,  7, 14,  9, 13, 15, 14,  8, 13,  6,  5, 12,  7,  5,
  		11, 12, 14, 15, 14, 15,  9,  8,  9, 14,  5,  6,  8,  6,  5, 12,
  		 9, 15,  5, 11,  6,  8, 13, 12,  5, 12, 13, 14, 11,  8,  5,  6};
  	
  	private static final int[] SR = {  // Left-rotation for right line
  		 8,  9,  9, 11, 13, 15, 15,  5,  7,  7,  8, 11, 14, 14, 12,  6,
  		 9, 13, 15,  7, 12,  8,  9, 11,  7,  7, 12,  7,  6, 15, 13, 11,
  		 9,  7, 15, 11,  8,  6,  6, 14, 12, 13,  5, 14, 13, 13,  7,  5,
  		15,  5,  8, 11, 14, 14,  6, 14,  6,  9, 12,  9, 12,  5, 15,  8,
  		 8,  5, 12,  9, 12,  5, 14,  6,  8, 13,  6,  5, 15, 13, 11, 11};
}