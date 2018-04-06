# CoinCloud Uploader

## What?
This is an program that lets you upload files to the Bitcoin-blockchain.
## Install.
Go into the `Releases`-folder, than choose the folder with version that you want to download (`Newest Version` will always contain the most up-to-date version) and download the runnable jar-file named `CoinCloudUploader {version} {state}(runnable).jar`. Once The download is complete, you only need to install the java `jre 8` (or higher).
## How to use?
Once you start the `CoinCloudUploader` you'll see 5 stages.

---
In the first stage, you have to enter the path to the file you want to upload. You can type the path manually or choose an path via the `Choose`-button. Due to technical limitations, the file must be less than or equal to 67,959 bytes. Once your done click the `Next`-button.

---
In the second stage you need to enter the amount of Satoshi, that will be sent to each generated Bitcoin-address. This value should not be lower than 546 Satoshi, since payments less than that are considered "dust"(or in other words spam) in the Bitcoin-network. Dust will be ignored by all nodes and miners, causing your transaction to not be accepted.

Below that is a field, in that you have to enter the transaction fee that you pay, in order to give the people that mine blocks a reason to include your transaction in their block and thus in the blockchain. This field is only used by the `Estimate BTC needed`-button and does not affect the resulting transaction in any way, since **all** funds, that are transfered to the generated address, will be consumed by the upload(with all excess money, not used (in the individual transactions) going to the miner). Once your done click the `Next`-button. 

---
In the third stage you have to pay the calculated amount of BTC, to an newly generated address. That is done, so you don't have to enter the private key to your wallet anywhere in this program. It is advised, but not necessary, that the payment is done in one single transaction(and not multiple), since more transactions increase the fee that one has to pay and thus making the calculated value inaccurate(because it assumed that only one transaction to the new address has been made). If you needed more than one, keep in mind to adjust the spinner, to the number of transactions you have used and check if the transferred amount still matches. Once you have paid the calculated amount of BTC and your transaction, to the new address and it has **at least 1 confirmation**, you can proceed, by clicking the `Next`-button. The program will than check the balance of the new address through blockchain.info, should you want to use Tor for that connection, you can check the `Use Tor to check balance`-check box.

**Note**: If you want to connect through Tor, you'll need to have an Tor-proxy or the Tor Browser Bundle running (doesn't matter which website, if any, you have open there, the Tor browser just needs to be running).

**Note also**: If you click the `View keys...`-button, an window will appear showing all information about the generated new address. It is advised to copy these values and temporarily store them somewhere, so you can get your Bitcoins back(by importing the private key into your wallet) should something fail. In that window you can also import an already existing Bitcoin address, however keep in mind: **The program will use ALL FUNDS that are stored on the imported address!**

---
In the fourth stage, you just have to press the `Craft transaction`-button and the program will craft an transaction, containing the contents of the file, that was specified in stage one. After the program is done crafting the transaction you can press the `Next`-button to progress to the last stage.

**Note**: Once the transaction is crafted, it can always be seen by clicking the `View transaction...`-button.

---
In the fifth stage, your transaction just needs to be broadcasted to the entire Bitcoin network, this is done by simply clicking the `Broadcast`-button. The program will now, using blockchain.infos 'pushtx'-feature, broadcast your transaction to the network. Should you want this connection to go through Tor, you can check the `Use Tor`-check box and an connection to the hidden service of blockchain.info through Tor will be made instead. You may be seeing a warning show up in the information panel(if your crafted transaction size happens to be greater than 74312), notifying you of an `minor server error`. This, however, is no big deal and can be ignored.

**Note**(again): If you want to connect through Tor, you'll need to have an Tor-proxy or the Tor Browser Bundle running (doesn't matter which website, if any, you have open there, the Tor browser just needs to be running).

Once you see an confirmation message appear in the information panel, take note of the transaction ID and wait for the confirmation of your transaction (you can check `https://blockchain.info/tx/{yourTransactionID}` to see whether or not it has been confirmed yet). This can, depending on the fee you chose in the second stage, take some time. Once you see at least one confirmation you are done and can now safely close the program(should you also have written down the private key, you can now also safely delete it).

You have now successfully uploaded the chosen file into the Bitcoin-blockchain and can download it, at any time, with the id of your transaction using the CoinCloudDownloader or similar tools. See `How does this work?` below if you want to create such a tool yourself.

---
General advice: **NEVER** give away your wallets private key! Anyone claiming to need that key is trying to scam you, until you have definite prove that he doesn't. Whoever knows that key owns any money on that address.
## Why is there an image in each release?
This image has an zip-archive embedded into it, containing the source-code and the runnable jar file of its version. This makes it easier to distribute the program through image boards and the such, just upload the image like any other normal image.
It is possible that the image gets cleaned in the upload process, so don't expect this to work everywhere! Just check the file size of the original and the uploaded one, if they differ, the image has been cleaned.

To open the zip-archive: Download the image and change it's file extension from `.jpg` to `.zip` and then open it with winrar or similar programs.

You can create these images on Linux with the command: '`cat {imageName}.jpg {zipName}.zip > {newImageZipName}.jpg`'.
## How does this work?
The uploaded file is essentially 'hidden' in the output scripts of an transaction.

This is achieved by cutting the input file into many 20 byte pieces (the last piece will be padded with null bytes), than converting the pieces into Pay-to-Public-Key-Hash (or P2PKH for short) addresses, that get stored along with the amount of Satoshi, that will be sent to each generated address, into an generated transaction.

This transaction is than broadcasted to the entire Bitcoin network and, after some time, integrated into the blockchain, where it can be accessed at any time through its transaction ID. You can use the `CoinCloudDownloader` to download the file from the blockchain to you computer. If you don't want to use that program, you can also do it manually by extracting the public-key-hashes from each output script and concatenate them (in order of appearance in the transaction) and store the result into an file (this is essentially what the `CoinCloudDownloader` does).