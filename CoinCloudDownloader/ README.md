#CoinCloud Downloader
##What?
This is an program that lets you download files, that have been embedded into the Bitcoin-blockchain.
##Install.
Go into the `Releases`-folder, than choose the folder with version that you want to download (`Newest Version` will always contain the most up-to-date version) and download the runnable jar-file named `CoinCloudDownloader {version} {state}(runnable).jar`. Once The download is complete, you only need to install the java `jre 8` (or higher).
##How to use?
Once you start the CoinCloud Downloader you'll see 2 fields.

In the first field, you have to paste the transaction ID(in hexadecimal) of the transaction that contains the file you want to download.

The second field is for the path for the output file, that will be downloaded from the blockchain. You can type the path manually or choose an path via the `Choose`-button.

**Be aware:** If you type or choose an already existing file, the program will not hesitate to override that file and thereby delete it's contents forever!

If you have done that, you can choose whether or not to use the Tor network to download the file. In order for that to work you need to have an Tor-proxy running on your machine(`localhost(127.0.0.1)` on port `9150`). If you are not sure how to do that or feel not  tech-savvy enough to set an Tor-proxy up, you can download the [Tor Browser Bundle](https://www.torproject.org/download/download-easy.html.en) and start the Tor Browser. While the Tor Browser is running(it doesn't matter whether you are visiting a site or not), the CoinCloud Downloader can(and will) use the Tor Browsers connection to the Tor-network and download through it. Should you not want to download through Tor at all, you can uncheck the `Use Tor`-checkbox and your normal Internet connection will be used for the download instead, however this will leave your IP in [their](https://blockchain.info) server logs and they will know that you where there!

Once your done with anything mentioned above you can press the `Write to output file`-button and the file will start to download.
##Why is there an image in each release?
This image has an zip-archive embedded into it, containing the source-code and the runnable jar file of the version. This makes it easier to distribute the program through image boards and the such, just upload the image like any other normal image.
It is possible that the image gets cleaned in the upload process, so don't expect this to work everywhere! Just check the file size of the original and the uploaded one, if they differ, the image has been cleaned.

To open the zip-archive: Download the image and change it's file extension from `.jpg` to `.zip` and then open it with winrar or similar programs.

You can create these images on Linux with the command `cat {imageName}.jpg {zipName}.zip > {newImageZipName}.jpg`
##In which cases does this work/How do I embed an file into the blockchain?
The embedded file must be hidden in the output scripts of an transaction.
In an standard transaction structure (so that the miner will accept the transaction and insert it into an block), you'll get to a point where the script-public-key is pushed onto the stack (usually 20 Bytes for each address) these bytes, can be freely chosen since they correspond to some address. One could now cut an file into 20 Byte slices and insert them into an big transaction, where all addresses(or rather the script-public-key of the address) are the slices of the embedded file. 
The address and the script-public-key are not one and the same! Do not try to encode the file-data into addresses, since just the script-public-key will show up in the raw transaction.

How that could look like is (transaction in raw hex format): 

`[Version]+[Input scripts...]+[Output script length]+[amount of satoshi]+00000000000019+76a914+[20 Bytes of data]+88ac+[amount of satishi#2]+...`

For more info on that, see the [bitcoinwiki](https://en.bitcoin.it/wiki/Transaction).