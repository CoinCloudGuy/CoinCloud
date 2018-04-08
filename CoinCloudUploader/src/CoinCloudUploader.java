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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/*
 * Conventions: All methods starting with '_' are deemed helper methods and not intended for general use.
 * 				'field' is a prefix for anything the user could enter text.
 * 				'dfield' is an text field just for displaying information
 * 
 */
public class CoinCloudUploader extends JFrame {
	private static final long serialVersionUID = 4443304742443698016L;
	
	//general constants
	public static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE-2;//also last (valid) array index
	//program constants
	public static final String version = "0.0.5-Beta";
	public static final String name = "CoinCloudUploader";
	public static final int ERROR = 0;
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final int INSTRUCTION = 3;
	public static final Color ERRORColor = new Color(255, 0, 0);
	public static final Color INFOColor = new Color(0, 0, 0);
	public static final Color WARNINGColor = new Color(155, 155, 0);
	public static final Color INSTRUCTIONColor = new Color(155, 0, 155);
	public static final Color POINTYColor = new Color(0, 255, 0);
	//UI constants
	public static final int SPACE_BETWEEN_STAGES = 5;//in pixels
	public static final Color StageBorderColor = new Color(205, 205, 205);
	public static final Color StageBackgroundColor = new Color(220, 220, 220);
	public static final Color CurrentStageBorderColor = new Color(190, 190, 190);
	public static final Color CurrentStageBackgroundColor = new Color(247, 247, 247);
	public static final Color ControlStageBorderColor = new Color(18, 72, 80);
	public static final Color ControlStageBackgroundColor = new Color(60,167,186);

	public static final Color FieldBorderColor = new Color(0, 0, 0);
	public static final Color FieldBackgroundColor = new Color(255, 255, 255);
	
	public static final Color DFieldBorderColor = new Color(180, 180, 180);
	public static final Color DFieldBackgroundColor = new Color(200, 200, 200);
	//data constants/limits
	public static final String PRICE_PER_ADDRESS_default = "546";//in satoshi per address
	public static final int PRICE_PER_ADDRESS_default_value = 546;//in satoshi per address
	public static final String TRANSACTION_FEE_default = "1185";//in Satoshi per kB
	public static final double TRANSACTION_FEE_default_value = 1185.0d;// in Satoshi per kB
	
	//the FILESIZE and TXSIZE values have been found by experiment.
    public static final int FILESIZE_BelowOK = 43257;
    public static final int FILESIZE_AboveError = 43620;
    public static final int FILESIZE_AboveNoBroadcastPossible = 67959;//there is a bit of room there, but I don't want to test these limits. Above this can sometimes work, but this will be our cutoff.
    
    public static final int TXSIZE_BelowOK = 73702;
    public static final int TXSIZE_AboveError = 74312;
    public static final int TXSIZE_AboveNoBroadcastPossible = 116721;//there is a bit of room there, but I don't want to test these limits. Above this can sometimes work, but this will be our cutoff.
    
	//network constants
	public static final int TESTNET = 1;
	public static final int MAINNET = 0;
	public static int BLOCKCHAIN_Network = MAINNET;
	public static final String BLOCKCHAIN_Host = "blockchain.info";
	public static final String BLOCKCHAIN_TestNetHost = "testnet.blockchain.info";
	public static final String BLOCKCHAIN_HostTor = "blockchainbdgpzk.onion";
	public static final Proxy TORProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9150));
	public static final String TORProxyString = "127.0.0.1:9150";
	
	//container
	private JPanel FileInputContainer;
	private JPanel BTCPaymentContainer;
	private JPanel BTCAddressContainer;
	private JPanel CraftContainer;
	private JPanel BroadcastContainer;
	//network
	private static boolean canUseTor;
	//visible components
	private JSplitPane contentPane;
	private JTextField field_InputFilePath;
	private JTextField dfield_AddressToPayTo;
	private JTextField dfield_CalculatedAmountToPay;
	private JTextField field_TransactionFee;
	private JTextField field_PricePerAddress;
	private JTextPane infoBox;
	private JCheckBox chckbxUseTorCheck;
	private JCheckBox chckbxAddChecksumAddress;
	private JCheckBox chckbxUseTorBroadcast;
	private JButton btnViewKeys;
	private JButton btnViewTransaction;
	private JButton btnStartOver;
	//UI related
	private JPanel currentStage;
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
	
	private boolean isBroadcastDone;
	//Miscellaneous
	private ButtonGroup BTCUnits;
	private JRadioButton rdbtnBtc;
	private JRadioButton rdbtnMbtc;
	private JRadioButton rdbtnBits;
	private JRadioButton rdbtnSatoshi;
	
	public static void main(String[] args) throws MalformedURLException, IOException{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				//check if all algorithms are supplied by the operating system.
				if(!isSHA256AlgorithmWorking()) {
					JOptionPane.showMessageDialog(null, "Your operating system does not supply the \"SHA-256\"-hashing algorithm. This, however, is required for this program. There is most likely nothing that can be done about this. Sorry!", "Error: Missing algorithms", JOptionPane.ERROR_MESSAGE);
					return;
				}else if(!isSHA1PRNGAlgorithmWorking()) {
					JOptionPane.showMessageDialog(null, "Your operating system does not supply the \"SHA-1 pseudo random number generator\"-algorithm. This, however, is required for this program. There is most likely nothing that can be done about this. Sorry!", "Error: Missing algorithms", JOptionPane.ERROR_MESSAGE);
					return;
				}else if(!isECKeyAlgorithmWorking()) {
					JOptionPane.showMessageDialog(null, "Your operating system does not support elliptic curve cryptography or does not supply the \"secp256k1\"-elliptic curve. This, however, is required for this program. There is most likely nothing that can be done about this. Sorry!", "Error: Missing algorithms", JOptionPane.ERROR_MESSAGE);
					return;
				}else if(!isSignatureAlgorithmWorking()) {
					JOptionPane.showMessageDialog(null, "Your operating system does not supply the \"SHA256 with ECDSA\"-signature algorithm. This, however, is required for this program. There is most likely nothing that can be done about this. Sorry!", "Error: Missing algorithms", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//anything works, continue startup
				
				//check for an running tor proxy
				canUseTor = isTorProxyRunning();
				if(BLOCKCHAIN_Network!=MAINNET)//tor support only for mainnet.
					canUseTor = false;
				
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
		this.isBroadcastDone = false;
		setTitle(name + " " + version + (BLOCKCHAIN_Network==MAINNET ? "" : " On the Bitcoin testnet-network!"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//components
		contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JPanel actionsContainer = new JPanel();
		contentPane.setLeftComponent(actionsContainer);
		actionsContainer.setLayout(new BoxLayout(actionsContainer, BoxLayout.PAGE_AXIS));
		
		FileInputContainer = getStageContainer();
		FileInputContainer.setLayout(new BorderLayout(0, 0));
		
			JLabel lblInputfile = new JLabel("Input file:");
			FileInputContainer.add(lblInputfile, BorderLayout.NORTH);
			
			field_InputFilePath = getNormalField("");
			field_InputFilePath.setToolTipText("You must choose an file that has less than " + FILESIZE_AboveNoBroadcastPossible + " bytes! Due to technical limitations, you cannot process an larger file.");
			FileInputContainer.add(field_InputFilePath, BorderLayout.CENTER);
			
			JButton btnChooseInput = getButton("Choose");
			btnChooseInput.addActionListener(_getChooseButtonActionListener());
			FileInputContainer.add(btnChooseInput, BorderLayout.EAST);
			
			JButton btnNext_ChooseInput = getButton("Next");
			btnNext_ChooseInput.addActionListener(_getBtnNext_ChooseInputActionListener());
			FileInputContainer.add(btnNext_ChooseInput, BorderLayout.SOUTH);
		
		actionsContainer.add(FileInputContainer);	
		
		Component verticalGlue_Stage1To2 = Box.createVerticalGlue();
		verticalGlue_Stage1To2.setPreferredSize(new Dimension(0, SPACE_BETWEEN_STAGES));
		actionsContainer.add(verticalGlue_Stage1To2);
		
		BTCPaymentContainer = getStageContainer();
		BTCPaymentContainer.setLayout(new BorderLayout(0, 0));
	
			JLabel label = new JLabel("Amount of satoshi per generated address:");
			BTCPaymentContainer.add(label, BorderLayout.NORTH);
			
			field_PricePerAddress = getIntegerField(PRICE_PER_ADDRESS_default);
			BTCPaymentContainer.add(field_PricePerAddress, BorderLayout.CENTER);
			
			JButton btnDefault_PricePerAddress = getButton("Default");
			btnDefault_PricePerAddress.addActionListener(_getBtnDefault_PricePerAddressActionListener());
			BTCPaymentContainer.add(btnDefault_PricePerAddress, BorderLayout.EAST);
			
			JPanel BTCPaymentContainerRelay01 = new JPanel();
			BTCPaymentContainerRelay01.setOpaque(false);
			BTCPaymentContainer.add(BTCPaymentContainerRelay01, BorderLayout.SOUTH);
			BTCPaymentContainerRelay01.setLayout(new BorderLayout(0, 0));
	
				JLabel lblAmountSatoshiPer = new JLabel("Amount of satoshi per kB transaction size:");
				BTCPaymentContainerRelay01.add(lblAmountSatoshiPer, BorderLayout.NORTH);
				
				field_TransactionFee = getDecimalField(TRANSACTION_FEE_default);
				BTCPaymentContainerRelay01.add(field_TransactionFee, BorderLayout.CENTER);
				field_TransactionFee.setColumns(10);
				
				JButton btnDefault_TransactionFee = getButton("Default");
				btnDefault_TransactionFee.addActionListener(_getBtnDefault_TransactionFeeActionListener());
				BTCPaymentContainerRelay01.add(btnDefault_TransactionFee, BorderLayout.EAST);
				
				JPanel BTCPaymentContainerRelay02 = new JPanel();
				BTCPaymentContainerRelay02.setLayout(new BorderLayout(0,0));
				BTCPaymentContainerRelay02.setOpaque(false);
				
					chckbxAddChecksumAddress = new JCheckBox("Include file size in transaction");
					chckbxAddChecksumAddress.setOpaque(false);
					chckbxAddChecksumAddress.setSelected(true);
					BTCPaymentContainerRelay02.add(chckbxAddChecksumAddress, BorderLayout.NORTH);
					
					JButton btnNext_Payment = getButton("Next");
					btnNext_Payment.addActionListener(_getBtnNext_PaymentActionListener());
					BTCPaymentContainerRelay02.add(btnNext_Payment, BorderLayout.SOUTH);
				
				BTCPaymentContainerRelay01.add(BTCPaymentContainerRelay02, BorderLayout.SOUTH);
		
		actionsContainer.add(BTCPaymentContainer);

		Component verticalGlue_Stage2To3 = Box.createVerticalGlue();
		verticalGlue_Stage2To3.setPreferredSize(new Dimension(0, SPACE_BETWEEN_STAGES));
		actionsContainer.add(verticalGlue_Stage2To3);
		
		BTCAddressContainer = getStageContainer();
		BTCAddressContainer.setLayout(new BorderLayout(0, 0));

			JPanel BTCAddressContainerRelay01 = new JPanel();
			BTCAddressContainerRelay01.setOpaque(false);
			BTCAddressContainer.add(BTCAddressContainerRelay01, BorderLayout.NORTH);
			BTCAddressContainerRelay01.setLayout(new BorderLayout(0, 0));

				JLabel lblAmountToPay = new JLabel("Amount to pay:");
				BTCAddressContainerRelay01.add(lblAmountToPay, BorderLayout.NORTH);
				
				dfield_CalculatedAmountToPay = getDField("");
				BTCAddressContainerRelay01.add(dfield_CalculatedAmountToPay, BorderLayout.CENTER);
				
				JSpinner spinner = new JSpinner();
				spinner.setToolTipText("In how many individual transactions you've needed, to send the money to the generated address.");
				spinner.setModel(new SpinnerNumberModel(1, 0, 999, 1));
				spinner.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent arg0) {
						calculatedEstamatedCost = estimateCost(fileContents.length, (Integer)spinner.getValue(), storedTransactionFee, storedSatoshiPerAddress, chckbxAddChecksumAddress.isSelected());
						if(rdbtnBtc.isSelected()) {
							if(calculatedEstamatedCost != -1)
								dfield_CalculatedAmountToPay.setText(formatDouble(convertSatoshiToBTC(calculatedEstamatedCost)));
						}else if(rdbtnMbtc.isSelected()) {
							if(calculatedEstamatedCost != -1)
								dfield_CalculatedAmountToPay.setText(formatDouble(convertSatoshiTomBTC(calculatedEstamatedCost)));
						}else if(rdbtnBits.isSelected()) {
							if(calculatedEstamatedCost != -1)
								dfield_CalculatedAmountToPay.setText(formatDouble(convertSatoshiToBits(calculatedEstamatedCost)));
						}else if(rdbtnSatoshi.isSelected()) {
							if(calculatedEstamatedCost != -1)
								dfield_CalculatedAmountToPay.setText(formatDouble(calculatedEstamatedCost));
						}else {
							System.err.println("Something went horrobly wrong!");
						}
					}
				});
				BTCAddressContainerRelay01.add(spinner, BorderLayout.EAST);
				
				JPanel BTCUnitContainer = new JPanel();
				BTCUnitContainer.setOpaque(false);
				BTCAddressContainerRelay01.add(BTCUnitContainer, BorderLayout.SOUTH);
				
					
					BTCUnits = new ButtonGroup();
					
					JLabel lblShowPriceIn = new JLabel("Show price in: ");
					BTCUnitContainer.add(lblShowPriceIn);
					
					rdbtnBtc = new JRadioButton("BTC");
					rdbtnBtc.setOpaque(false);
					rdbtnBtc.setSelected(true);
					rdbtnBtc.addItemListener(_getItemListener());
					BTCUnits.add(rdbtnBtc);
					BTCUnitContainer.add(rdbtnBtc);
					
					rdbtnMbtc = new JRadioButton("mBTC");
					rdbtnMbtc.setOpaque(false);
					rdbtnMbtc.addItemListener(_getItemListener());
					BTCUnits.add(rdbtnMbtc);
					BTCUnitContainer.add(rdbtnMbtc);
					
					rdbtnBits = new JRadioButton("Bits");
					rdbtnBits.setOpaque(false);
					rdbtnBits.addItemListener(_getItemListener());
					BTCUnits.add(rdbtnBits);
					BTCUnitContainer.add(rdbtnBits);
					
					rdbtnSatoshi = new JRadioButton("Satoshi");
					rdbtnSatoshi.setOpaque(false);
					rdbtnSatoshi.addItemListener(_getItemListener());
					BTCUnits.add(rdbtnSatoshi);
					BTCUnitContainer.add(rdbtnSatoshi);

			JPanel BTCAddressContainerRelay02 = new JPanel();
			BTCAddressContainerRelay02.setOpaque(false);
			BTCAddressContainer.add(BTCAddressContainerRelay02, BorderLayout.CENTER);
			BTCAddressContainerRelay02.setLayout(new BorderLayout(0, 0));

				JLabel lblAddress = new JLabel("Address:");
				BTCAddressContainerRelay02.add(lblAddress, BorderLayout.NORTH);
				
				dfield_AddressToPayTo = getDField("");
				BTCAddressContainerRelay02.add(dfield_AddressToPayTo, BorderLayout.CENTER);
				
				chckbxUseTorCheck = new JCheckBox("Use Tor to check balance"); 
				chckbxUseTorCheck.setOpaque(false);
				chckbxUseTorCheck.setSelected(canUseTor);
				chckbxUseTorCheck.setEnabled(BLOCKCHAIN_Network==MAINNET);
				BTCAddressContainerRelay02.add(chckbxUseTorCheck, BorderLayout.SOUTH);

			JButton btnNext_BTCAddress = getButton("Next");
			btnNext_BTCAddress.addActionListener(_getBtnNext_BTCAddressActionListener());
			BTCAddressContainer.add(btnNext_BTCAddress, BorderLayout.SOUTH);
		
		actionsContainer.add(BTCAddressContainer);
		
		Component verticalGlue_Stage3To4 = Box.createVerticalGlue();
		verticalGlue_Stage3To4.setPreferredSize(new Dimension(0, SPACE_BETWEEN_STAGES));
		actionsContainer.add(verticalGlue_Stage3To4);
		
		CraftContainer = getStageContainer();
		CraftContainer.setLayout(new BorderLayout(0, 0));
			
			JButton btnCraftTransaction = getButton("Craft transaction");
			btnCraftTransaction.addActionListener(_getBtnCraftTransactionActionListener());
			CraftContainer.add(btnCraftTransaction, BorderLayout.NORTH);
			
			Component strutCraftContainer = Box.createVerticalStrut(10);
			CraftContainer.add(strutCraftContainer, BorderLayout.CENTER);
			
			JButton btnNext_CraftTransaction = getButton("Next");
			btnNext_CraftTransaction.addActionListener(_getBtnNext_CraftTransactionActionListener());
			CraftContainer.add(btnNext_CraftTransaction, BorderLayout.SOUTH);

		actionsContainer.add(CraftContainer);

		Component verticalGlue_Stage4To5 = Box.createVerticalGlue();
		verticalGlue_Stage4To5.setPreferredSize(new Dimension(0, SPACE_BETWEEN_STAGES));
		actionsContainer.add(verticalGlue_Stage4To5);
		
		BroadcastContainer = getStageContainer();
		BroadcastContainer.setLayout(new BorderLayout(0, 0));

			chckbxUseTorBroadcast = new JCheckBox("Use Tor"); 
			chckbxUseTorBroadcast.setOpaque(false);
			chckbxUseTorBroadcast.setSelected(canUseTor);
			chckbxUseTorBroadcast.setEnabled(BLOCKCHAIN_Network==MAINNET);
			BroadcastContainer.add(chckbxUseTorBroadcast, BorderLayout.EAST);
			
			JButton btnBroadcastTransaction = getButton("Broadcast");
			btnBroadcastTransaction.addActionListener(_getBtnBroadcastTransactionActionListener());
			BroadcastContainer.add(btnBroadcastTransaction, BorderLayout.CENTER);
			
		actionsContainer.add(BroadcastContainer);
		
		Component verticalGlue_Stage5ToControl = Box.createVerticalGlue();
		verticalGlue_Stage5ToControl.setPreferredSize(new Dimension(0, SPACE_BETWEEN_STAGES*3));
		actionsContainer.add(verticalGlue_Stage5ToControl);
		
		JPanel controlContainer = getControlStageContainer();
		controlContainer.setLayout(new BoxLayout(controlContainer, BoxLayout.X_AXIS));
			
			btnViewKeys = getButton("View keys...");
			btnViewKeys.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(addressPrivateKey != null && addressPublicKey != null)
						openKeyViewWindow();
				}
			});
			controlContainer.add(btnViewKeys);

			Component horizontalGlue_ControlStage1 = Box.createHorizontalGlue();
			horizontalGlue_ControlStage1.setPreferredSize(new Dimension(5, 0));
			controlContainer.add(horizontalGlue_ControlStage1);
			
			btnViewTransaction = getButton("View transaction...");
			btnViewTransaction.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(craftedTransaction != null)
						openTransactionViewWindow();
				}
			});
			controlContainer.add(btnViewTransaction);
			
			Component horizontalGlue_ControlStage2 = Box.createHorizontalGlue();
			horizontalGlue_ControlStage2.setPreferredSize(new Dimension(5, 0));
			controlContainer.add(horizontalGlue_ControlStage2);
			
			btnStartOver = getButton("Start over");
			btnStartOver.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fileContents = new byte[] {};//Guaranteed to never be null
					storedSatoshiPerAddress = -1;
					storedTransactionFee = -1.0;
					calculatedEstamatedCost = -1;
					calculatedExactCost = -1;
					sumOfAllSatoshiToSpent = -1;
					amountOfAddressesUsed = -1;
					unspendOutputs = null;
					craftedTransaction = null;

					spinner.setValue(1);//since here is an change event this value needs to be set, before the dfield gets reset
					field_InputFilePath.setText("");
					field_PricePerAddress.setText(PRICE_PER_ADDRESS_default);
					field_TransactionFee.setText(TRANSACTION_FEE_default);
					rdbtnBtc.setSelected(true);
					dfield_AddressToPayTo.setText("");
					dfield_CalculatedAmountToPay.setText("");
					
					btnViewKeys.setEnabled(false);
					btnViewTransaction.setEnabled(false);
					
					clearlog();
					setStageTo(FileInputContainer);
					isBroadcastDone = false;//resetting it here(after resetting the stage) allows for the first dialogue to be different if the start over button was pressed after an completed upload.
				}
			});
			controlContainer.add(btnStartOver);
			
		actionsContainer.add(controlContainer);
		
		JPanel infoBoxContainer = new JPanel();
		infoBoxContainer.setLayout(new BorderLayout());
		contentPane.setRightComponent(infoBoxContainer);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(300, 2));
		scrollPane.setMinimumSize(new Dimension(300, 23));
		scrollPane.setViewportBorder(new LineBorder(new Color(192, 192, 192), 2, true));
		infoBoxContainer.add(scrollPane, BorderLayout.CENTER);
		
			JLabel lblNewLabel = new JLabel("Information");
			lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
			scrollPane.setColumnHeaderView(lblNewLabel);
			
			infoBox = new JTextPane();
			infoBox.setEditorKit(new WrapEditorKit());
			_appendToLog(">", POINTYColor);
			infoBox.setEditable(false);
			scrollPane.setViewportView(infoBox);
		
		JButton btnClearInfoBox = getButton("Clear");
		btnClearInfoBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearlog();
			}
		});
		infoBoxContainer.add(btnClearInfoBox, BorderLayout.SOUTH);
		
		//set dimension
		pack();
		//enable the glue functionality
		//new Dimension(, MAX_VALUE) will make sure that the glue will always try to be as large as possible.
		verticalGlue_Stage1To2.setPreferredSize(new Dimension(1, Short.MAX_VALUE));
		verticalGlue_Stage2To3.setPreferredSize(new Dimension(1, Short.MAX_VALUE));
		verticalGlue_Stage3To4.setPreferredSize(new Dimension(1, Short.MAX_VALUE));
		verticalGlue_Stage4To5.setPreferredSize(new Dimension(1, Short.MAX_VALUE));
		verticalGlue_Stage5ToControl.setPreferredSize(new Dimension(1, Short.MAX_VALUE));
		//set position
		Dimension winDim = getWindowDimension();
		setLocation(
				(winDim.width-getWidth())/2, //center x
				(winDim.height-getHeight())/2); //center y
		//fix the size to at least this
		setMinimumSize(getContentPane().getSize());
		//lock the divider in the middle for this size (make it honor the horizontal glues in the Control stage)
		contentPane.getLeftComponent().setMinimumSize(contentPane.getLeftComponent().getSize());
		contentPane.getRightComponent().setMinimumSize(contentPane.getRightComponent().getSize());
		
		//disable later stages
		this.currentStage = FileInputContainer;
		setStageTo(FileInputContainer);
		btnViewKeys.setEnabled(false);
		btnViewTransaction.setEnabled(false);
	}
//----------------------------------------------------------------------------------------------------------------------------------------

	//		Class methods.
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//swing fixes
	
	static class JTextFieldIntegersOnly extends PlainDocument {
	private static final long serialVersionUID = -651554675463378418L;

		public JTextFieldIntegersOnly() {
			super();
		}
		
		public void insertString(int offset, String  str, AttributeSet attr) throws BadLocationException {
			if(str == null) return;
			if(str.matches("-?[0-9]+")) {
				super.insertString(offset, str, attr);
			}
		}
	}
	
	static class JTextFieldNumbersOnly extends PlainDocument {
		private static final long serialVersionUID = -6156769443369928897L;
		public JTextFieldNumbersOnly() {
			super();
		}
		
		public void insertString(int offset, String  str, AttributeSet attr) throws BadLocationException {
			if(str == null) return;
			if(str.matches("-?[0-9]+")||str.contains(".")) {
				if(getSequenceCount(".", getText(0, getLength()))==0)
					super.insertString(offset, str, attr);
				else
					super.insertString(offset, str.replace(".",""), attr);//there is already an decimal point present
			}
		}
		public static int getSequenceCount(String sequence, String toSearchIn){
			int count = 0;
			int sequenceLength = sequence.length();
			for(int i = 0; i+sequenceLength<=toSearchIn.length();i++){
				if(toSearchIn.substring(i, i+sequenceLength).equals(sequence)) count++;
			}
			return count;
		}
	}
	
	//fix for weird JTextPane (infoBox) wrap behavior
    class WrapEditorKit extends StyledEditorKit {
		private static final long serialVersionUID = 8085257318132218646L;
		ViewFactory defaultFactory=new WrapColumnFactory();
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }
	
	class WrapColumnFactory implements ViewFactory  {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            // default to text display
            return new LabelView(elem);
        }
    }

    class WrapLabelView extends LabelView {

        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }
    }
	
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
				
				//check whether or not the file size will be accepted by blockchain.infos pushtx
				if(fileContents.length >= FILESIZE_BelowOK) {
					if(fileContents.length < FILESIZE_AboveError) {
						//could work in some cases, I don't know
						log("The file size of " + fileContents.length + " can result in an minor server error. Regardless whether or not it does, in this case, the broadcast will not fail it is safe to continue.", WARNING);
					}else{
						if(fileContents.length > FILESIZE_AboveNoBroadcastPossible) {
							//will not work
							log("The file size of " + fileContents.length + " will not be able to be broadcasted, please reduce the file size to at least " + FILESIZE_AboveNoBroadcastPossible + " bytes or choose another file!", ERROR);
							fileContents = new byte[] {};
							return;
						}else {
							//works but with error
							log("The file size of " + fileContents.length + " will cause an minor server error. This is not a problem, since in this case the transaction will still be broadcasted. It is safe to continue.", WARNING);
						}
					}
				}
				//file is in memory, move on to next stage
				log(fileContents.length + " Bytes of data have been loaded.", INFO);
				setStageTo(BTCPaymentContainer);
			}
		};
	}
	private ActionListener _getBtnNext_PaymentActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//get Fee and satoshi per address
				final boolean useChecksum = chckbxAddChecksumAddress.isSelected();
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
				
				calculatedEstamatedCost = estimateCost(fileContents.length, 1, storedTransactionFee, storedSatoshiPerAddress, useChecksum);//1 is assumed
				//create one-time use Bitcoin address.
				ECKeyPair pair = generateKeyPair();
				if(pair == null) {
					log("Critical error, some required algorithms are not supplied by your system. You seeing this, should be impossible... You will (probably) never be able to fix this, sorry.", ERROR);
					storedSatoshiPerAddress = -1;//reset to invalid
					storedTransactionFee = -1;//reset to invalid
					calculatedEstamatedCost = -1;//reset to invalid
					return;
				}
				addressPublicKey = pair.puk;
				addressPrivateKey = pair.prk;
				dfield_CalculatedAmountToPay.setText(formatDouble(convertSatoshiToBTC(calculatedEstamatedCost)));
				dfield_AddressToPayTo.setText(createBitcoinAddress(addressPublicKey, BLOCKCHAIN_Network));
				log("The price calculation is complete and an new Bitcoin address has been generated.", INFO);
				setStageTo(BTCAddressContainer);
				btnViewKeys.setEnabled(true);
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
				btnStartOver.setEnabled(false);
				//get data for worker thread
				final boolean useTor = chckbxUseTorCheck.isSelected();
				if(useTor) {
					//check if torproxy is running
					if(!isTorProxyRunning()) {
						log("It has been selected, that Tor should be used to check the current balance of the generated address, however you don't have an Tor-proxy running right now. Uncheck the check-box or start the proxy and try again!", ERROR);
						((JButton)e.getSource()).setEnabled(true);
						btnStartOver.setEnabled(true);
						return;
					}
				}
				final String bitcoinAddress = createBitcoinAddress(addressPublicKey, BLOCKCHAIN_Network);
				final boolean useChecksum = chckbxAddChecksumAddress.isSelected();
				log("Receiving balance information...", INFO);
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
								btnStartOver.setEnabled(true);
								return;
							}
							if(results.isEmpty()) {
								log("The address does not have any spendable money! Transfer at least " + calculatedEstamatedCost + " Satoshi to \"" + bitcoinAddress + "\".", ERROR);
								((JButton)e.getSource()).setEnabled(true);
								btnStartOver.setEnabled(true);
								return;
							}
							for (byte[][] result : results) {
								if(new BigInteger(result[4]).compareTo(BigInteger.ZERO)>0) {//read as 'if(numberOfConfirmations>0)'
									sumOfBTC += Integer.toUnsignedLong(Integer.parseUnsignedInt(switchEndianess(convertToHex(result[3])), 16));
								}else {
									log("The transaction: \"" + convertToHex(result[0]) + "\" does not have any confirmations. Try again later(in about 10 minutes).", ERROR);
									((JButton)e.getSource()).setEnabled(true);
									btnStartOver.setEnabled(true);
									return;
								}
							}
							
							long realTransactionLength = calculateTransactionLength(fileContents.length, results.size(), useChecksum);
							calculatedExactCost = (long)Math.ceil(realTransactionLength*(storedTransactionFee/1000)) // '/1000' conversion from kB to byte
									+ ((long)calculateAmountOfAddressesNeeded(fileContents.length, useChecksum))*storedSatoshiPerAddress;
							if(sumOfBTC>=calculatedExactCost) {
								if(sumOfBTC == calculatedExactCost) {
									log("The address has currently: " + sumOfBTC + " Satoshi. This is exactly the amount needed for this transaction.",INFO);
								}else {
									log("The address has currently: " + sumOfBTC + " Satoshi. This is more than is needed for this transaction. Note that all funds, that are now deposited on this address, will be used in this transaction. You will loose all money that is now on that address(" + sumOfBTC + " Satoshi, meaning you will pay " + (sumOfBTC-calculatedExactCost) + " more Satoshi, than the necessary " + calculatedExactCost + "). That extra money will be awarded to the miner, that mines your transaction. Should you not want this, grab the addresses privatekey by pressing the 'View keys...' button on the bottom of the screen and transfer the money back to your original account.",WARNING);
								}
								sumOfAllSatoshiToSpent = sumOfBTC;
								unspendOutputs = results;
								setStageTo(CraftContainer);//next button would be disabled here anyway so no need to enable it here
								btnStartOver.setEnabled(true);
								return;
							}else {
								log("The is not enough money to pay for the transaction to be accepted.(needed: " + calculatedExactCost + " Satoshi, available: " + sumOfBTC + ").", ERROR);
								calculatedExactCost = -1;
								((JButton)e.getSource()).setEnabled(true);
								btnStartOver.setEnabled(true);
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
						btnStartOver.setEnabled(true);
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
				
				if(craftedTransaction.length >= TXSIZE_BelowOK) {
					if(craftedTransaction.length < TXSIZE_AboveError) {
						//could work in some cases, I don't know
						log("The transaction size of " + craftedTransaction.length + " can result in an minor server error. Regardless whether or not it does, in this case, the broadcast will not fail. It is safe to continue.", WARNING);
					}else{
						if(craftedTransaction.length > TXSIZE_AboveNoBroadcastPossible) {
							//will not work
							log("The transaction size of " + craftedTransaction.length + " will not be able to be broadcasted. Please reduce the file size to at least " + FILESIZE_AboveNoBroadcastPossible + " bytes or choose another file. You cannot continue!", ERROR);
							return;
						}else {
							//works but with error
							log("The transaction size of " + craftedTransaction.length + " will cause an minor server error. This is not a problem, since in this case the transaction will still be broadcasted. It is safe to continue.", WARNING);
						}
					}
				}
				
				log("All financial checks are now complete. The address does have enough funds to make the transaction.", INFO);
				setStageTo(BroadcastContainer);
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
				btnStartOver.setEnabled(false);
				//send the transaction to the blockchain.info publish server and broadcast it to the rest of the world.
				//that will also detect errors in the transaction and sent an response on what went wrong, so no real need
				//to do validness checks here in code.
				final boolean useTor = chckbxUseTorBroadcast.isSelected();
				if(useTor) {
					//check if torproxy is running
					if(!isTorProxyRunning()) {
						log("It has been selected, that Tor should be used to broadcast the crafted transaction, however you don't have an Tor-proxy running right now. Uncheck the check-box or start the proxy and try again!", ERROR);
						((JButton)e.getSource()).setEnabled(true);
						btnStartOver.setEnabled(true);
						return;
					}
				}
				log("Broadcasting " + craftedTransaction.length + " bytes of transaction...",  INFO);
				SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>(){

					@Override
					protected Integer doInBackground() throws Exception {
						try {
							int succsess = broadcastTransaction(craftedTransaction, useTor, BLOCKCHAIN_Network);
							return succsess;
						} catch (IOException e2) {
							log("An network error appeared, please try again later.", ERROR);
							return BROADCAST_Failed;
						}
					}
					
					@Override
					protected void done() {
						try {
							int response = get().intValue();
							if(response == BROADCAST_Succsess) {//=isSuccsessfull
								log("The transaction has been successfully broadcasted to the bitcoin network. Note that it can take up to(or even longer than) 3 hours before your transaction gets confirmed.", INFO);
								String transactionID = getTransactionID(craftedTransaction);
								log("Your transaction ID is: " + transactionID, INFO);
								log("Make sure your write that ID down, since it is really difficult to locate your file in the blockchain, should you forget this ID.", INFO);
								log("You can check whether or not the transaction has been confirmed yet here: https://" + (BLOCKCHAIN_Network==MAINNET ? BLOCKCHAIN_Host: BLOCKCHAIN_TestNetHost) + "/tx/" + transactionID, INFO);
								setStageTo(null);
								isBroadcastDone = true;
								btnStartOver.setEnabled(true);
								return;
							}else if(response == BROADCAST_SuccsessWithMinorError){
								log("The transaction has been successfully broadcasted to the bitcoin network.", INFO);
								log("However, an minor server error has been encountered, but this will not be an issue.", WARNING);
								log("Note that it can take up to(or even longer than) 3 hours before your transaction gets confirmed.", INFO);
								String transactionID = getTransactionID(craftedTransaction);
								log("Your transaction ID is: " + transactionID, INFO);
								log("Make sure your write that ID down, since it is really difficult to locate your file in the blockchain, should you forget this ID.", INFO);
								log("You can check whether or not the transaction has been confirmed yet here: https://" + (BLOCKCHAIN_Network==MAINNET ? BLOCKCHAIN_Host: BLOCKCHAIN_TestNetHost) + "/tx/" + transactionID, INFO);
								setStageTo(null);
								isBroadcastDone = true;
								btnStartOver.setEnabled(true);
								return;
							}else {//BROADCAST_Failed is the only other possible return value possible
								log("The transaction could not be successfully broadcasted.", ERROR);
								((JButton)e.getSource()).setEnabled(true);
								btnStartOver.setEnabled(true);
								return;
							}
						} catch (InterruptedException | ExecutionException ex) {
							log("The broadcast thread has been interrupted. This is not supposed to happen, please try again.", ERROR);
							((JButton)e.getSource()).setEnabled(true);
							btnStartOver.setEnabled(true);
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
				final boolean UseChecksumAddress = chckbxAddChecksumAddress.isSelected();
				
				final byte[] version = new byte[] {1,0,0,0};//0x01 in little endian
				final byte[] numOfInputsVarInt = convertToBytes(getVarIntFromLong(unspendOutputs.size()));
				byte[] preInput = appendBytes(new byte[][] {version, numOfInputsVarInt});
				//inputs computed later
				int amoutofAddresses = calculateAmountOfAddressesNeeded(fileContents.length, false);// useChecksum=false because this will be added in manually later in the code
				byte[][] outputData = new byte[amoutofAddresses][20];//20 byte packets, init value for java arrays is null (or rather 0x00 for bytes) for each element
				for (int i = 0; i < fileContents.length; i++) {
					outputData[i/20][i%20] = fileContents[i];
				}
				final byte amountOfPaddingBytes = (byte)( (20 - (fileContents.length%20))%20 );//fileContents.length%20=number of content-bytes in the last address
				//insert all outputs
				byte[] postInput;//store result here
				byte[] numOfOutputsVarInt = convertToBytes(getVarIntFromLong(outputData.length + (UseChecksumAddress?1:0)));
				postInput = numOfOutputsVarInt;
				final byte[] outScriptPrefix = convertToBytes("76a914");//doesn't change for constant length of 20 bytes 
				final byte[] outScriptPostfix = convertToBytes("88ac");//doesn't change 
				final byte[] costPerAddress = convertToBytes(switchEndianess(ensureHexLengthOf(16, Long.toUnsignedString(storedSatoshiPerAddress, 16))));//doesn't change
				for (int i = 0; i < outputData.length; i++) {
					byte[] script = appendBytes(new byte[][] {outScriptPrefix, outputData[i], outScriptPostfix});
					byte[] scriptLengthVarInt = convertToBytes(getVarIntFromLong(script.length));
					postInput = appendBytes(new byte[][] {postInput, costPerAddress, scriptLengthVarInt, script});
				}
				
				//append the checksum address if enabled
				if(UseChecksumAddress) {
					//the checksum address is the first 19 bytes of the sha256 hash of the file contents(including the needed padding bytes) and one byte containing the number of padding bytes, that are used, in the previous address. This number must be in between and including 0 and 19 in order to be valid
					byte[] sha256_File = sha256(Arrays.copyOf(fileContents, outputData.length*20));
					byte[] cksumScript = appendBytes(new byte[][] {outScriptPrefix, Arrays.copyOf(sha256_File, 19), new byte[] {amountOfPaddingBytes}, outScriptPostfix});
					byte[] cksumscriptLengthVarInt = convertToBytes(getVarIntFromLong(cksumScript.length));
					postInput = appendBytes(new byte[][] {postInput, costPerAddress, cksumscriptLengthVarInt, cksumScript});
				}

				outputData = new byte[][] {};//empty the outputData array since its not needed anymore and basically contains the file (clean memory).
				byte[] lockTime = new byte[] {0,0,0,0};//as soon as possible (not locked)
				postInput = appendBytes(new byte[][] {postInput, lockTime});
				
				
				//create all input scripts
				//input script structure:script = OPpush ~71 bytes + [signature] + OPpush 33 bytes + [compressed public key]
				
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
					byte[] signature = getSignature(combined, addressPrivateKey);
					if(signature==null) 
						//no message since this should be impossible.
						return;//abort anything
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
				btnViewTransaction.setEnabled(true);
				log("The transaction has been crafted!", INFO);
			}
		};
	}

	
	private ItemListener _getItemListener() {
		return new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED && e.getSource() instanceof JRadioButton) {
					JRadioButton r = (JRadioButton)e.getSource();
					if(r == rdbtnBtc) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText(formatDouble(convertSatoshiToBTC(calculatedEstamatedCost)));
					}else if(r == rdbtnMbtc) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText(formatDouble(convertSatoshiTomBTC(calculatedEstamatedCost)));
					}else if(r == rdbtnBits) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText(formatDouble(convertSatoshiToBits(calculatedEstamatedCost)));
					}else if(r == rdbtnSatoshi) {
						if(calculatedEstamatedCost != -1)
							dfield_CalculatedAmountToPay.setText(formatDouble(calculatedEstamatedCost));
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
		dialouge.setTitle("Generated Keys");
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
				JTextField dfield_Address = getDField(_getAddress_View());
				addressPanel.add(dfield_Address);
				JButton btnCopyBtcAddress = getButton("Copy");
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
				JTextField dfield_PrivateKeyWIF = getDField(_getPrivateKeyWIF_View());
				privateKeyWIFPanel.add(dfield_PrivateKeyWIF);
				
				JButton btnCopyPrivateKeyWIF = getButton("Copy");
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
				JTextField dfield_PrivateKeyHex = getDField(_getPrivateKeyHex_View());
				privateKeyHexPanel.add(dfield_PrivateKeyHex);
				JButton btnCopyPrivateKeyHex = getButton("Copy");
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
				JTextField dfield_PublicKeyHex = getDField(_getPublicKeyHex_View());
				publicKeyHexPanel.add(dfield_PublicKeyHex);
				JButton btnCopyPublicKeyHex = getButton("Copy");
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
				JTextField dfield_PublicUnComKeyHex = getDField(_getUnComPublicKeyHex_View());
				publicUnComKeyHexPanel.add(dfield_PublicUnComKeyHex);
				JButton btnCopyUnComPublicKeyHex = getButton("Copy");
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
				JButton ImportButton = getButton("Import");
				ImportButton.setEnabled(!isBroadcastDone);//is the broadcast is done, importing any key is useless and thus, should not be possible.
				ImportButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						openKeyImportWindow(keyViewWindow);
					}
				});
				buttonPane.add(ImportButton);
				
				JButton okButton = getButton("OK");
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
				JButton okButton = getButton("OK");
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
					}
				});
				buttonPane.add(okButton);
				keyImportWindow.getRootPane().setDefaultButton(okButton);
				JButton cancelButton = getButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						keyImportWindow.dispose();
					}
				});
				buttonPane.add(cancelButton);
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//view transaction window
	
	private void openTransactionViewWindow() {
		JDialog dialouge = new JDialog(this);
		_setupTransactionViewWindow(dialouge);
		dialouge.pack();
		dialouge.setBounds(
				getX() + (getWidth()-dialouge.getWidth())/2, //center x within main window
				getY() + (getHeight()-dialouge.getHeight())/2, //center y within main window
				dialouge.getWidth(),//set by pack()
				dialouge.getHeight());//set by pack()
		dialouge.setAutoRequestFocus(true);
		dialouge.setTitle("Transaction");
		dialouge.setVisible(true);
	}
	private void _setupTransactionViewWindow(JDialog dialouge) {
		dialouge.getContentPane().setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		dialouge.getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			JPanel TransactionIDContainer = new JPanel();
			contentPanel.add(TransactionIDContainer);
			TransactionIDContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
				JLabel lblTransactionId = new JLabel("Transaction ID:");
				TransactionIDContainer.add(lblTransactionId);
				JTextField dfieldTxID = getDField(getTransactionID(craftedTransaction));
				TransactionIDContainer.add(dfieldTxID);
				JButton btnCopy = new JButton("Copy");
				TransactionIDContainer.add(btnCopy);
				
				Component verticalGlue = Box.createVerticalGlue();
				contentPanel.add(verticalGlue);
				JPanel TransactionHexContainer = new JPanel();
				contentPanel.add(TransactionHexContainer);
				TransactionHexContainer.setLayout(new BorderLayout(0, 0));
					JButton btnCopy_1 = new JButton("Copy");
					TransactionHexContainer.add(btnCopy_1, BorderLayout.SOUTH);
					
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setViewportBorder(new LineBorder(DFieldBorderColor, 2, false));
					scrollPane.setBorder(BorderFactory.createEmptyBorder());
					scrollPane.setPreferredSize(new Dimension(200, 300));
					TransactionHexContainer.add(scrollPane, BorderLayout.CENTER);
					JLabel lblRawTransactionhex = new JLabel("Raw transaction(hex):");
					scrollPane.setColumnHeaderView(lblRawTransactionhex);
					
					JTextArea textArea = new JTextArea(convertToHex(craftedTransaction));
					textArea.setBackground(DFieldBackgroundColor);
					textArea.setLineWrap(true);
					scrollPane.setViewportView(textArea);
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			dialouge.getContentPane().add(buttonPane, BorderLayout.SOUTH);
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						dialouge.dispose();
					}
				});
				buttonPane.add(okButton);
				
				JButton exportButton = new JButton("Export...");
				exportButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						JFileChooser fc = new JFileChooser();
						fc.setFileFilter(new FileFilter() {
							
							@Override
							public String getDescription() {
								return "*.txn";
							}
							
							@Override
							public boolean accept(File f) {
								return f.getAbsolutePath().endsWith(".txn") || f.isDirectory();
							}
						});
						int returnval = fc.showSaveDialog(exportButton);
						if(returnval != JFileChooser.APPROVE_OPTION)
							return;
						File f = fc.getSelectedFile();
						if(!f.getAbsolutePath().endsWith(".txn")) {
							f = new File(f.getAbsolutePath() + ".txn");
						}
						if(f.exists()) {
							actionPerformed(arg0);//file already exists choose another file
						}else {
							//write to file
							String toWrite = "{\n    \"hex\": \"" + convertToHex(craftedTransaction) + "\"\n}";
							FileWriter wr;
							try {
								wr = new FileWriter(f);
								wr.write(toWrite);
								wr.close();
							} catch (IOException e) {
								actionPerformed(arg0);//IO error choose another file.
							}
						}
					}
				});
				buttonPane.add(exportButton);
	}

//----------------------------------------------------------------------------------------------------------------------------------------
	//enable/disable stuff
	public void setStageTo(JPanel stage) {//null disables all stages
		if (currentStage != null) {
			this.currentStage.setBorder(getStageBorder(false));
			this.currentStage.setBackground(StageBackgroundColor);
			this.currentStage.revalidate();
			this.currentStage.repaint();
		}
		this.currentStage = stage;
		_setAllEnabled(FileInputContainer, false);
		_setAllEnabled(BTCPaymentContainer, false);
		_setAllEnabled(BTCAddressContainer, false);
		_setAllEnabled(CraftContainer, false);
		_setAllEnabled(BroadcastContainer, false);
		
		if(stage!=null) {
			_setAllEnabled(stage, true);
			stage.setBorder(getStageBorder(true));
			stage.setBackground(CurrentStageBackgroundColor);
			stage.revalidate();
			stage.repaint();
			stage.requestFocus();
			//print log for the appropriate stage
			if(stage == FileInputContainer) {
				if (isBroadcastDone) {
					log("Not satisfied yet? Fine by me!", INSTRUCTION);
					log("I will guide you through the process, of uploading an file to the Bitcoin-blockchain, as many times as you want to!", INSTRUCTION);
				}else {
					log("Hi there!", INSTRUCTION);
					log("I will guide you through the process, of uploading an file to the Bitcoin-blockchain.", INSTRUCTION);
				}
				log("First you'll need to choose the file that you want to upload. Press 'Choose' or type the full file path by hand.", INSTRUCTION);
				log("Once, you're done, press 'Next' to get to stage 2.", INSTRUCTION);
			}else if(stage == BTCPaymentContainer) {
				logEmptyLine();
				log("Nice, now that the file is loaded, you need to choose how much money will be given to each address that gets generated in the upload process. Be careful what you choose, because if this value is less than " + PRICE_PER_ADDRESS_default + ", the transaction will be considered spam in the Bitcoin-network and as such, could be rejected.", INSTRUCTION);
				log("You also need to choose how large the transaction fee should be. This is the money, that will be given to the miner, that is resposible for mining your transaction. So choose an higher transaction fee, if you want your transaction to be processed in less time.", INSTRUCTION);
				log("Then you'll need to decide, whether or not to include the file size in the crafted transaction, beacuse some files cannot be altered in their size(for example encrypted files), while others can(for example .jpg images). If you are unsure, whether or not you need to include the file size, just leave the 'Include file size in transaction'-check box checked and continue.", INSTRUCTION);
				log("If you have chosen these values, press 'Next' to get to stage 3.", INSTRUCTION);
			}else if(stage == BTCAddressContainer) {
				logEmptyLine();
				log("Okay, the amount of money, that is required to upload, now is calculated.", INSTRUCTION);
				log("What you need to do now, is to supply that amount to the program. To do this, you have to send the amount shown in the 'Amount to pay'-field, to the Bitcoin address shown in the 'Address'-field. This address is randomly generated. In fact, every time you get to stage 3 there will be an entirely new address generated for you.", INSTRUCTION);
				log("Keep in mind that the required amount of money depends, in part, on the number of transactions it took you to send the money to the generated address. If you needed more than one transaction, you can tell the calculated price to include that, by changing the spinner next to the 'Amount to pay'-field, to the number of transactions you needed(up to 999 transactions are allowed).", INSTRUCTION);
				log("Should you distrust the address in the 'Address'-field, which is absolutely understandable and a good habit, you can view the private key corresponding to that address at any time, after reaching the third stage, by pressing the 'View keys'-button at the bottom of the window. You can even import your own Bitcoin address, by clicking 'Import' in the window that opens, if you press 'View keys'.", INSTRUCTION);
				log("Once the transaction to the generated address is made, you'll have to wait until your wallet tells you that the transaction is confirmed.", INSTRUCTION);
				log("After the transaction is confirmed, you can press 'Next' to get to stage 4. If the 'Next'-button is pressed, the program will check the current balance of the generated address, to do this it will make a connection over the Internet. This connection can be done through Tor, if you have an Tor-proxy or the Tor Browser Bundle currently running on your computer. Make sure the 'Use Tor to check Balance'-check box is checked, if you want the connection to go through Tor.", INSTRUCTION);
			}else if(stage == CraftContainer) {
				logEmptyLine();
				log("So, you have paid the required amount of money? Great! The most complicated part is over.", INSTRUCTION);
				log("However, please consider storing the private key somewhere. Should it come to an critical failure, from this point onwards, you will only be able to recover your money, if you still have the private key! You can view the private key, by clicking the 'View keys'-button, at the bottom of the window, at any time.", INSTRUCTION);
				log("Now that it is verified, that the generated account does have the money nessessary to upload the file, we just need craft the transaction that contains the file. To do this, simply press the 'Craft transaction'-button and the program does the rest for you.", INSTRUCTION);
				log("Once the transaction is crafted, you can view it at any time, by pressing the 'View transaction'-button.", INSTRUCTION);
				log("That's it for stage 4, press 'Next' to continue to the last stage.", INSTRUCTION);
			}else if(stage == BroadcastContainer) {
				logEmptyLine();
				log("Almost done! The only thing left to do, is to broadcast the crafted transaction to the Bitcoin network and let all know of the new transaction. To do this, just press the 'Broadcast'-button and wait for the broadcast to complete.", INSTRUCTION);
				log("If you have an Tor-proxy or the Tor Browser Bundle currently running on your computer, you can broadcast the transaction through Tor, in that way the Bitcoin-network can't see your IP and won't know it was you, who sent the transaction. To activate this, you just have to check the 'Use Tor'-check box.", INSTRUCTION);
			}
		}else {//just for dialogue
			logEmptyLine();
			log("Wonderful! Everything worked, your transaction is now out there. Now you just have to wait for confirmation. Once your transaction is confirmed your file will be in the Bitcoin-blockchain forever.", INSTRUCTION);
			log("After the confirmation you can also safely delete the private key, you may have made a backup of earlier.", INSTRUCTION);
			log("The transaction ID can be used to access the file again at any time, by using the CoinCloudDownloader or similar tools.", INSTRUCTION);
			log("If you want to upload another file to the Bitcoin-blockchain, simply click the 'Start over'-button, at the bottom of the window.", INSTRUCTION);
			log("Should you not want to upload anything else, than farewell, until you need me again!", INSTRUCTION);
			//this causes the new line point to not appear, which is okay, because this is the last line that will be printed before either the program gets shut down or reseted by the user
			synchronized (infoBox) {
				_appendToLog("Bye.", INSTRUCTIONColor);
			}
		}
	}
	private static void _setAllEnabled(JPanel p, boolean enabled) {
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
			case INSTRUCTION://special case
				_appendToLog(message + "\n", INSTRUCTIONColor);
				_appendToLog(">", POINTYColor);
				return;
			default:
				specificText = "";
				specificColor = new Color(0, 0, 0);
				break;
			}
			_appendToLog("[" + df.format(date) + "] " + specificText + message + "\n", specificColor);
			_appendToLog(">", POINTYColor);
		}
	}
	public void logEmptyLine() {
		synchronized (infoBox) {
			_appendToLog("\n>", POINTYColor);
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
    //swing-stuff
	public static JTextField getDField(String text) {
		JTextField dfield = new JTextField();
		dfield.setText(text);
		dfield.setEditable(false);
		dfield.setBackground(DFieldBackgroundColor);
		dfield.setBorder(new LineBorder(DFieldBorderColor));
		return dfield;
	}
	
	public JPanel getStageContainer() {
		JPanel out = new JPanel();
		out.setBorder(getStageBorder(false));
		out.setBackground(StageBackgroundColor);
		return out;
	}
	public JPanel getControlStageContainer() {//only called control stage because of semantics, it really is just an JPanel nothing special about this
		JPanel out = new JPanel();
		out.setBorder(new LineBorder(ControlStageBorderColor, 2, false));
		out.setBackground(ControlStageBackgroundColor);
		return out;
	}
	public static Border getStageBorder(boolean isCurrentStage) {
		if(isCurrentStage)
			return new LineBorder(CurrentStageBorderColor, 2, false);
		else
			return new LineBorder(StageBorderColor, 2, false);
	}
	public static JTextField getIntegerField(String defaultText) {
		JTextField out = new JTextField();
		out.setDocument(new JTextFieldIntegersOnly());
		out.setText(defaultText);
		return out;
	}
	public static JTextField getDecimalField(String defaultText) {
		JTextField out = new JTextField();
		out.setDocument(new JTextFieldNumbersOnly());
		out.setText(defaultText);
		return out;
	}
	public static JTextField getNormalField(String defaultText) {
		JTextField out = new JTextField();
		out.setText(defaultText);
		return out;
	}
	public static JButton getButton(String text) {
		JButton out = new JButton(text);
		return out;
	}
	private static Dimension getWindowDimension() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//Internet stuff
	public static final String BLOCKCHAIN_unspendTransactionPrefix = "/unspent?active=";

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
			URL blockchainINFO = new URL("https",BLOCKCHAIN_HostTor, BLOCKCHAIN_unspendTransactionPrefix + address);
			log("Connect to: " + "https://" + BLOCKCHAIN_HostTor + BLOCKCHAIN_unspendTransactionPrefix + address + ".", INFO);
			c = (HttpURLConnection)blockchainINFO.openConnection(TORProxy);
		}else {
			URL blockchainINFO = new URL("https", network==MAINNET ? BLOCKCHAIN_Host : BLOCKCHAIN_TestNetHost, BLOCKCHAIN_unspendTransactionPrefix + address);
			log("Connect to: " + "https://" + (network==MAINNET ? BLOCKCHAIN_Host : BLOCKCHAIN_TestNetHost) + BLOCKCHAIN_unspendTransactionPrefix + address + ".", INFO);
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
	public static final int BROADCAST_Succsess = 0;
	public static final int BROADCAST_SuccsessWithMinorError = 1;
	public static final int BROADCAST_Failed = 2;
	
	//true if and only if the transaction has been successfully broadcasted!
	public int broadcastTransaction(byte[] transaction, boolean useTor, int network) throws IOException {
		String host = useTor ? BLOCKCHAIN_HostTor : (network == MAINNET ? BLOCKCHAIN_Host : BLOCKCHAIN_TestNetHost);
		HttpURLConnection con;
		if(useTor)
			con = (HttpURLConnection) new URL("https", host, pushPrefix).openConnection(TORProxy);
		else
			con = (HttpURLConnection) new URL("https", host, pushPrefix).openConnection();
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
		log("Send 'POST' request to URL: \"" + host + pushPrefix + "\".", INFO);
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		}catch (IOException e) {
			in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		if(responseCode/100==2 && response.toString().equalsIgnoreCase("Transaction Submitted")) {
			return BROADCAST_Succsess;
		}else {
			//check if the transaction made it nonetheless
			HttpURLConnection chckCon = (HttpURLConnection)new URL("https", host, "/rawtx/" + getTransactionID(transaction)).openConnection();
			chckCon.setRequestMethod("HEAD");
			chckCon.connect();
			int code = chckCon.getResponseCode();
			chckCon.disconnect();
			if(code/100==2) {
				//did still broadcast
				return BROADCAST_SuccsessWithMinorError;
			}else {
				log("The transaction broadcast failed(response code: \"" + responseCode + "\"), due to rejection of the transaction. Reason/Error message:\"" + response.toString() + "\".", ERROR);
				return BROADCAST_Failed;
			}
		}
	}
	
	public static boolean isTorProxyRunning() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL("http://" + TORProxyString).openConnection();
			con.setRequestMethod("HEAD");
			con.connect();
			return con.getResponseCode()!=-1;//-1 means invalid HTTP response
		} catch (IOException e) {
			return false;
		}
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
	
	public static String formatDouble(double toFormat) {
		DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		df.setMaximumFractionDigits(100);
		return df.format(toFormat);
	}
//----------------------------------------------------------------------------------------------------------------------------------------
	//length calculation stuff
	
	public static long estimateCost(int fileLength, int numberOfInputs, double fee, long satoshiPerAddress, boolean useChecksum) {
		int transactionlength = calculateTransactionLength(fileLength, numberOfInputs, useChecksum);
		return (long)Math.ceil(transactionlength*(fee/1000)) // '/1000' conversion from kB to byte
						+ ((long)calculateAmountOfAddressesNeeded(fileLength, useChecksum))*satoshiPerAddress;
	}
	
	private static final int versionlength = 4;
	private static final int TxOutHashLength = 32;
	private static final int TxOutIndexLength = 4;
	private static final int standartInputScriptlength = 1+71+1+33;//[varInt of 71] + [length of signature + 1] + [varInt of 33] + [compressed EC public key]
	private static final int sequenceLength = 4;
	private static final int outValueLength = 8;
	private static final int standartOutPubScriptlength = 25;
	private static final int lockTimeLength = 4;
	public static int calculateTransactionLength(int amountOfBytes, int amountOfInputs, boolean useChecksum) {
		final int VarIntNumOfTxInLength = getVarIntFromLong(amountOfInputs).length()/2;// '/2' because hex string
		int amountOfAddressesNeeded = calculateAmountOfAddressesNeeded(amountOfBytes, useChecksum);
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
	public static int calculateAmountOfAddressesNeeded(int amountOfBytes, boolean useChecksum) {
		return (useChecksum?1:0) + ((amountOfBytes-1)/20)+1;//split in 20 byte intervals and the last one padded with null bytes, therefore divide by 20 and ceil(or add 1 because integer) the result.
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
		toBeChecksumed[0] = network==MAINNET ? (byte)0x00 : (byte)0x6F;
		for (int i = 0; i < dHashed.length; i++) 
			toBeChecksumed[i+1] = dHashed[i];
		byte[] checksum = sha256(sha256(toBeChecksumed));
		out[0] = network==MAINNET ? (byte)0x00 : (byte)0x6F;
		for (int i = 0; i < dHashed.length; i++) 
			out[i+1] = dHashed[i];
		for (int i = 0; i < 4; i++)
			out[out.length-4+i] = checksum[i];
		return encodeBase58(out);
	}
	public static String createWIFPrivateKey(ECPrivateKey priv, int network) {
		byte[] rawKey = priv.getS().toByteArray();
		byte[] toBeHashed = new byte[1+32+1];
		toBeHashed[0] = network==MAINNET ? (byte)0x80 : (byte)0xEF;
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
	public static final BigInteger ECCURVE_N = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
	private static byte[] getSignature(byte[] toSign, ECPrivateKey priv){
		try {
			Signature sig = Signature.getInstance("SHA256withECDSA");
			sig.initSign(priv);
			sig.update(sha256(toSign));
			byte[] signature = sig.sign();
			//see if s-value is greater than N/2 and if so set s to N-s. This is done to fix "Non-canonical signature: S value is unnecessarily high"
			//get s bytes
			boolean passedR = false;
			byte[] sValue = null;
			int firstSIndex = 0;
			for (int i = 0; i < signature.length; i++) {
				if(signature[i]==(byte)0x02) {
					if(passedR) {
						byte lengthS = signature[i+1];
						sValue = new byte[lengthS];
						firstSIndex = i+2;
						for (int j = 0; j < lengthS; j++)
							sValue[j] = signature[j+(i+2)];//(i+2)starting index of s-value
						break;
					}else {
						byte lengthR = signature[i+1];
						i=(i+1)+lengthR;//(i+1)=index of length byte, (i+1)+lengthR= last index of r-value 
						passedR = true;
					}
				}
			}
			BigInteger biOfS = new BigInteger(sValue);
			if(biOfS.compareTo(ECCURVE_N.divide(BigInteger.valueOf(2))) > 0) {
				sValue = ECCURVE_N.subtract(biOfS).toByteArray();
				byte[] out = appendBytes(new byte[][] {Arrays.copyOfRange(signature, 0, firstSIndex-1),new byte[] {(byte)sValue.length},sValue});
				out[1] = (byte)(out.length-2);//[1] is the total length of the signature except for the 0x30 prefix and the length itself(so the amount of bytes following the value of it), since s could have changed in size the total length could have been changed as well.
				return out;
			}else {
				return signature;
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			System.err.println("NoSuchAlgorithmException, InvalidKeyException or SignatureException, this should be impossible!");
			e.printStackTrace();
			return null;
		}
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//Transaction creation helper
	private static byte[] getInputsBefore(List<byte[][]> data, int i) {
		if(i==0)
			return new byte[] {};
		byte[] sequence = convertToBytes("ffffffff");//no RBF 
		byte[] out = new byte[] {};
		for (int j = 0; j < i; j++) {
			byte[][] sc = data.get(j);
			out = appendBytes(new byte[][] {out, sc[0], sc[1], new byte[] {0}, sequence});
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
			out = appendBytes(new byte[][] {out, sc[0], sc[1], new byte[] {0}, sequence});
		}
		return out;
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//Algorithm availability check methods
	public static boolean isSignatureAlgorithmWorking() {
		try {
			ECKeyPair pair = generateKeyPair();
			if(pair==null)
				return false;
			Signature sig = Signature.getInstance("SHA256withECDSA");
			sig.initSign(pair.prk);
			byte[] data ="Just some test data.".getBytes();
			sig.update(data);
			byte[] signature = sig.sign();
			sig = Signature.getInstance("SHA256withECDSA");
			sig.initVerify(pair.puk);
			sig.update(data);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			return false;
		}
		
	}
	public static boolean isECKeyAlgorithmWorking() {
		SecureRandom sr;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
	        keyGen.initialize(new ECGenParameterSpec("secp256k1"), sr);
	        @SuppressWarnings("unused")
	        KeyPair pair = keyGen.generateKeyPair();
	        return true;
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			return false;
		}
	}
	public static boolean isSHA1PRNGAlgorithmWorking() {
		try {
			@SuppressWarnings("unused")
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			return true;
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
	}
	
	public static boolean isSHA256AlgorithmWorking() {
		try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update("Random test data.".getBytes());
            md.digest();
			return true;
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
	}
	
//----------------------------------------------------------------------------------------------------------------------------------------
	//Crypto key related methods methods
		
	private static class ECKeyPair{
		private ECPrivateKey prk;
		private ECPublicKey puk;
		public ECKeyPair(ECPublicKey puk, ECPrivateKey prk) {
			this.puk = puk;
			this.prk = prk;
		}
	}
	public static boolean isKeyPair(ECKeyPair pair){
		try {
			byte[] data = "This is an string, that is just there to generate random data and thats about it.".getBytes();
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
		} catch (NoSuchAlgorithmException e) {
			System.err.println("NoSuchAlgorithmException, this should be impossible!");
			e.printStackTrace();
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
	        ECKeyPair ecPair = new ECKeyPair((ECPublicKey) pair.getPublic(), (ECPrivateKey)pair.getPrivate());
	        if(!isKeyPair(ecPair))//ensure they work
	        	return null;
	        return ecPair;
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			System.err.println("NoSuchAlgorithmException or InvalidAlgorithmParameterException, this should be impossible!");
			e.printStackTrace();
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
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException | InvalidParameterSpecException e) {
			System.err.println("NoSuchAlgorithmException or InvalidParameterSpecException, this should be impossible!");
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
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException | InvalidParameterSpecException e) {
			System.err.println("NoSuchAlgorithmException or InvalidParameterSpecException, this should be impossible!");
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
		String use = in;
		int length = in.length();
		if(length % 2 != 0)
			use = "0"+use;;
		for (int i = 0; i < length; i = i+2)
			out = use.substring(i, i+2)+out;
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
  			System.err.println("NoSuchAlgorithmException, this should be impossible!");
  			e.printStackTrace();
            return null;
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