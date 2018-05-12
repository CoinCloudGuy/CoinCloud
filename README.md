# CoinCloud
## What?
CoinCloud is a set of two programs, called the CoinCloudUploader and the CoinCloudDownloader, which allow you to upload to and retrieve files from the Bitcoin-blockchain. Both are free and open source, so if you think that this is a scam(which is fair, to be honest), you can check the source code or, if you don't know how to program, ask a friend that does and should you distrust the precompiled executable jar files, I provided, you can also compile the source code yourself. See below for details on that.

## Where do I get these programs?
Above this text you'll see two folders, one called `CoinCloudDownloader` and one called `CoinCloudUploader`. In there you'll find specific instructions on how to install and use the respective program, as well as their source code. You can also get both programs, as well as their source code by downloading `CoinCloudBundle.zip`. Alternatively you can also download `CoinCloudBundle.png`, which provides instructions on how to use both programs and also has them and their source code embedded inside it, in the [Cornelia-format](https://encyclopediadramatica.rs/Embedded_files#Cornelia_format)(the random colored pixels below the actual image). To extract the embedded files just follow the instructions in the pink box in the image.

If you want to get the program from another place, it can be found here:
1.  [Github](https://github.com/CoinCloudGuy/CoinCloud) - https://github.com/CoinCloudGuy/CoinCloud
2.  [Gitgud](https://gitgud.io/CoinCloudGuy/CoinCloud) - https://gitgud.io/CoinCloudGuy/CoinCloud
3.  [Git Center](http://127.0.0.1:43110/126KYGEVcPaw9u7NKA6ZxqBUgFjGxqpF15) (requires [Zeronet](https://zeronet.io/)) - http://127.0.0.1:43110/126KYGEVcPaw9u7NKA6ZxqBUgFjGxqpF15

### How do I compile from source code?
This applies to both programs:
1.  Download the source code(`.java`-file).
2.  Place the `.java`-file into an empty folder(it doesn't matter where that folder is located).
3.  Download and install the `java development kit` version 8 or greater, also called the java jdk-8(**not** to be confused, with the java runtime environment, also called jre) .
4.  Open an command-prompt/terminal window and navigate to the folder that contains the `.java`-source file. (Usually done by typing `cd [FullPathToYourFolder]`)
5.  Copy the name of the `.java`-file(including the '.java' part).
6.  Type the following into your command-prompt/terminal window: `javac`(add a space at the end), than right click in the window and press `paste`. If that is done, press enter to execute the command.
7.  You're already done. To run the program just type `java`(add a space at the end) and after that, type the name of the source-file, this time just the name without the `.java` at the end and press enter to execute the command(that will execute the program).


**Optional:**

To create an executable jar file, after you compiled the source code, follow these steps:
1.  Download and install some software that lets you create an `.zip`-archive/file (for example Winrar).
2.  Copy the name of the source `.java`-file (just the name, without '.java' at the end).
3.  Create an new folder and call it `META-INF`(all letters in caps).
4.  In that newly created folder create an file and name it `MANIFEST.MF`(all letters in caps).
5.  Open that file and put `Manifest-Version: 1.0`, `Class-Path: .` and `Main-Class:`(each in separate lines) into the file.
6.  Right after (and in the same line as) `Main-Class:`, add a space and than paste the source-file-name that you copied in step 2.
7.  Save the file.
8.  Return to the original folder(in which you've created the `META-INF`-folder).
9.  Put all files, except the source code file(the `.java` one) inside the folder, into an `.zip`-archive. You can name it whatever you want.
10.  Change the extension from `.zip` to `.jar`.
11.  Done! You can now start the program, just by double clicking the `.jar`-file.

## How does it work?
The file is cut into 20 byte slices. These are than encoded as retrieving addresses and put into an Bitcoin transaction.
  
**More technical**: This is done, such that the pushed data in the output-script, if red from top to bottom, will reconstruct the file. The last 20 bytes of the file are either the last few bytes of the file, with the remaining empty bytes (since an address must be 20 bytes in size) being filled up with zero-bytes or the first 19 bytes of the sha-256 hash, of the entire file(all 20 byte slices, including the filler bytes) and the number of filler-bytes that where used, as the 20th byte. This information can be used to regain the size of the uploaded file, since some file types cannot just be filled up with zero-bytes(encrypted file containers, for example).

## Do I need to pay something to download a file?
No, downloading a file does not cost a single penny, it is completely free.

## Do I need to pay something to upload a file?
Unfortunately yes, since the file is encoded as receiving addresses, there needs to be some money to spend to them(otherwhise they wouldn't be receiving addresses). The two reasons for the price, that has to be spend on the file upload, are: 

1.  The DOS and Spam protection in the Bitcoin network, requires any transaction to be at least 546 Satoshi or 0.00000546 BTC(this value can change in the future, if you want to be sure, you can search for **"Bitcoin Dust"**, to find the most recent value).
2.  Every transaction in the Bitcoin-network needs to have a fee. That the size of that fee depends on the transaction size (usually per KB transaction). The fee guarantees that your transaction will be added to the Bitcoin-blockchain and thus be accepted by the network. In general the higher the fee the faster your transaction gets processed. In theory your fee can be arbitrarily low, however the lower your fee the longer it takes for your transaction to be accepted(this value changes rather frequently, it is best to search for the appropriate amount(**"Bitcoin current mining fee"**) before uploading).

So in short, the money you'll need to spend, goes to the individual addresses, that make up your file and the rest will be counted as the mining fee of the transaction.