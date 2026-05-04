# EVotingMobileApp Demo Run Guide



## Branch



feature/admin-otp-ui-polish



## Purpose



This file explains how to run my EVotingMobileApp demo.



This project uses an Android app and a local Hardhat blockchain. It is made for my university Final Year Project, so it is for testing and demonstration only. It is not a real production voting system.



## Before Starting



Before running the app, make sure:



\- Android Studio is installed

\- Hardhat can run on the laptop

\- the Android app builds successfully

\- the phone and laptop are on the same Wi-Fi if testing on a real phone

\- USB debugging is enabled if using a real phone

\- the app is rebuilt after changing contract-info.json



## Step 1: Start Hardhat Node



Open PowerShell in the project folder:



cd D:\\AndroidStudioProjects\\EVotingMobileApp



Run:



npx hardhat node --hostname 0.0.0.0



Keep this window open while testing the app.



For emulator testing, this can also work:



npx hardhat node



## Step 2: Deploy the Smart Contract



Open another PowerShell window in the project folder:



cd D:\\AndroidStudioProjects\\EVotingMobileApp



Run:



npx hardhat run scripts/deploy.ts



This updates the contract files used by the Android app, mainly:



\- app/src/main/assets/contract-info.json

\- app/src/main/assets/evoting-abi.json



## Step 3: Check RPC URL



Open this file:



app/src/main/assets/contract-info.json



For Android emulator testing, the RPC URL should normally be:



http://10.0.2.2:8545



For real phone testing, use the laptop Wi-Fi IP address.



To find the laptop IP address, run:



ipconfig | findstr /i "IPv4"



Example:



192.168.100.7



Then the RPC URL should look like:



http://192.168.100.7:8545



Important:



\- 127.0.0.1 does not work on a real phone

\- 10.0.2.2 is mainly for emulator testing

\- a real phone needs the laptop Wi-Fi IP address

\- rebuild and reinstall the app after changing contract-info.json



## Step 4: Check Blockchain Connection



For real phone testing, check if port 8545 is working:



Test-NetConnection 192.168.100.7 -Port 8545



Expected result:



TcpTestSucceeded : True



You can also check the Hardhat chain ID:



Invoke-RestMethod -Uri "http://192.168.100.7:8545" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","method":"eth\_chainId","params":\[],"id":1}'



Expected result:



0x7a69



This means the local Hardhat blockchain is running.



## Step 5: Build the Android App



Run:



.\\gradlew :app:assembleDebug



Expected result:



BUILD SUCCESSFUL



You can also build it from Android Studio using:



Build > Make Project



## Step 6: Run the App



Run the app from Android Studio on the emulator or phone.



If contract-info.json was changed, rebuild and reinstall the app before testing again.



## Demo Flow



The demo flow is:



1\. Admin logs in using the demo OTP.

2\. Admin creates an election.

3\. Admin opens QR check-in.

4\. Admin selects the election.

5\. Voter opens the Voter Access screen.

6\. Voter selects a demo voter.

7\. Voter shows the QR pass.

8\. Admin scans the QR pass.

9\. Admin checks in the voter.

10\. Voter goes to the voter dashboard.

11\. Voter selects the election.

12\. Voter chooses a candidate.

13\. Voter submits the vote.

14\. The app shows the transaction hash.

15\. The transaction hash can be verified.

16\. Admin closes the election.

17\. Results become visible.

18\. The winner and vote count are shown.

19\. Observer View can show turnout and results without admin controls.



## Expected Working Flow



Admin OTP -> Create election -> QR check-in -> Vote -> Receipt/hash -> Verify -> Close election -> Results -> Observer View



## QR Check-In Notes



The QR check-in is now improved.



Before, the QR mainly used a wallet address. Now the QR contains:



\- wallet address

\- issue time

\- expiry time

\- random nonce



The QR format is:



SecureVoteCheckIn|wallet=<address>|issuedAt=<millis>|expiresAt=<millis>|nonce=<random>



The admin scanner checks if:



\- the QR format is correct

\- the wallet address is valid

\- the QR has expired

\- the QR was already scanned in the current admin screen session



This is better for the prototype, but it is still not production-level security. A real system would need signed QR tokens, backend checking, and stronger replay protection.



## Demo Wallet Notes



The app uses demo wallet files for local Hardhat testing.



These wallets are only for the university demo. They should not be used on a real blockchain.



In a real app, private keys should not be stored inside the Android app. A real version should use WalletConnect, Reown, MetaMask, or another secure wallet method.



## Troubleshooting



If the phone cannot connect:



\- check that the phone and laptop are on the same Wi-Fi

\- check that the Hardhat node is still running

\- check that rpcUrl uses the laptop IP address

\- check that port 8545 is reachable

\- rebuild and reinstall the app after editing contract-info.json



If the contract does not work:



\- deploy the contract again

\- check the new contract address

\- rebuild and reinstall the app



If the laptop IP changes:



\- run ipconfig again

\- update contract-info.json

\- rebuild and reinstall the app



If the QR code is rejected:



\- the QR may have expired

\- the QR may have already been scanned

\- the QR payload may be wrong

\- refresh the QR pass from the Voter Access screen



## Prototype Notes



This app is a working prototype for my Final Year Project.



It uses a local Hardhat blockchain and demo wallet files.



The QR check-in has a time limit and nonce check, but it is still only a prototype feature.



More advanced features like zero-knowledge proofs, full privacy protection, real wallet authentication, and real identity verification are outside the current project scope.


