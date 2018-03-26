#C oinCloud Uploader
## What?
This is an program that lets you upload files to the Bitcoin-blockchain.
## Install.
Go into the `Releases`-folder, than choose the folder with version that you want to download (`Newest Version` will always contain the most up-to-date version) and download the runnable jar-file named `CoinCloudUploader {version} {state}(runnable).jar`. Once The download is complete, you only need to install the java `jre 8` (or higher).
## How to use?
Once you start the CoinCloud Uploader you'll see 4 fields.

In the first field, you have to enter the path to the file you want to upload. You can type the path manually or choose an path via the `Choose`-button.

The second field is for the amount of Satoshi that will be sent to each generated Bitcoin-address This value should not be lower than 546 Satoshi, since payments less than that are considered "dust", or in other words spam, in the Bitcoin-network. Spam will be ignored by all nodes and miners.

The third field is the transaction fee that you pay in order for miner to include your transaction in the blockchain. This field is only used by the `Estimate BTC needed`-button and does not affect the output file in any way.

The fourth field is the path to the output file, where the parsed input file will be written to. The chosen file must have an .csv extension. You can type the path manually or choose an path via the `Choose`-button.
 **Be aware**: If you type or choose an already existing .csv file, the program will not hesitate to override that file and thereby delete it's contents forever!

After you filled out all fields, you can press the `Estimate BTC needed`-button to get an estimate on how many Bitcoins the file upload will cost. This is just an estimate the real price could and in most cases will be higher than the estimated value!

Now you just need to press the `Write/Parse to Transaction`-button and the file specified in the input file field will be parsed into the specified output file.

By now you should have an .csv file that contains the parsed input file. To actually upload the input file you need to make the payments. To do this go into the `electrum` Bitcoin wallet and under "Tools" activate "Pay to many". Than import the .csv file created by the CoinCloudUploader and choose an fee that you feel comfortable with, than press sent.

If you are using an wallet, that is not electrum: Inform yourself on how to pay multiple addresses in only one transaction and follow what you find.

**Be aware:** **NEVER** give away your wallets private key! Anyone claiming to need that key is trying to scam you, until you have definite prove that he doesn't.
 
**Note:** Depending on the type of content you are uploading it can be advisable to set an Tor proxy in your wallet, since the send action will use **your IP** to publish the transaction and anyone in the Bitcoin-netowrk will see that IP.

Once your wallet confirms your transaction you have successfully uploaded the file into the Bitcoin-blockchain and can download the file at any time with the id of your transaction using the CoinCloudDownloader.
## Why is there an image in each release?
This image has an zip-archive embedded into it, containing the source-code and the runnable jar file of the version. This makes it easier to distribute the program through image boards and the such, just upload the image like any other normal image.
It is possible that the image gets cleaned in the upload process, so don't expect this to work everywhere! Just check the file size of the original and the uploaded one, if they differ, the image has been cleaned.

To open the zip-archive: Download the image and change it's file extension from `.jpg` to `.zip` and then open it with winrar or similar programs.

You can create these images on Linux with the command `cat {imageName}.jpg {zipName}.zip > {newImageZipName}.jpg`
## How does this work?
The uploaded file is essentially hidden in the output scripts of an transaction.

This is achieved by cutting the input file into many 20 byte pieces (the last piece will be padded with null bytes), than converting the pieces into Pay-to-Public-Key-Hash (or P2PKH for short) addresses, that get stored along with the amount of Satoshi, that will be sent to each generated address, into an .csv file.

This file than needs to be imported into an wallet, using an feature of that wallet, that can put payments to multiple addresses into one single transaction and respects the order of the file, where the transactions are imported from. It is important that this is the case, since the file would be stored corrupted in the blockchain and not be readable. The wallet than crafts an raw transaction converting the addresses back into the data pieces before they where turned into addresses and puts them into the output transaction format, that can be red by the `CoinCloudDownloader`. The finished raw transaction is than broadcasted to the all Bitcoin miners and eventually written into the blockchain.