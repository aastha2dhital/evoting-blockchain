\# EVotingMobileApp Demo Run Guide



\## Branch



fix/core-voter-flow-stability



\## Purpose



This guide explains how to run the EVotingMobileApp demo using a local Hardhat blockchain and a physical Android phone.



\## Before Starting



Make sure:



\- Laptop and phone are connected to the same Wi-Fi network.

\- Hardhat node is running.

\- Android Studio is installed.

\- Phone has USB debugging enabled.

\- The app is rebuilt and reinstalled after updating contract-info.json.



\## Step 1: Check Laptop IP Address



Run:



ipconfig | findstr /i "IPv4"



Example:



192.168.100.7



This IP is used by the phone to connect to the laptop blockchain node.



\## Step 2: Start Hardhat Node



Open a new PowerShell window and run:



cd D:\\AndroidStudioProjects\\EVotingMobileApp



npx hardhat node --hostname 0.0.0.0



Keep this PowerShell window open during the full demo.



\## Step 3: Deploy Smart Contract



In another PowerShell window, run:



cd D:\\AndroidStudioProjects\\EVotingMobileApp



npx hardhat run scripts/deploy.ts



This updates:



\- app/src/main/assets/contract-info.json

\- app/src/main/assets/evoting-abi.json



\## Step 4: Update RPC URL for Physical Phone



Open:



app/src/main/assets/contract-info.json



For physical phone testing, rpcUrl must use the laptop Wi-Fi IP.



Example:



{

&#x20; "contractAddress": "0x5FbDB2315678afecb367f032d93F642f64180aa3",

&#x20; "network": "localhost",

&#x20; "rpcUrl": "http://192.168.100.7:8545"

}



Important:



\- 127.0.0.1 does not work on a physical phone.

\- 10.0.2.2 is for emulator testing.

\- Physical phone testing needs the laptop Wi-Fi IP address.



\## Step 5: Confirm Blockchain Connection



Run:



Test-NetConnection 192.168.100.7 -Port 8545



Expected:



TcpTestSucceeded : True



Then run:



Invoke-RestMethod -Uri "http://192.168.100.7:8545" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","method":"eth\_chainId","params":\[],"id":1}'



Expected result:



0x7a69



This means Hardhat chain ID 31337 is running.



\## Step 6: Confirm Contract Exists



Replace the contract address if a new one was deployed.



Run:



Invoke-RestMethod -Uri "http://192.168.100.7:8545" -Method Post -ContentType "application/json" -Body '{"jsonrpc":"2.0","method":"eth\_getCode","params":\["0x5FbDB2315678afecb367f032d93F642f64180aa3","latest"],"id":1}'



Expected:



A long result starting with 0x6080604052



If the result is only 0x, the contract is not deployed at that address.



\## Step 7: Build Android App



Run:



.\\gradlew :app:assembleDebug



Expected:



BUILD SUCCESSFUL



Or use Android Studio:



Build > Make Project



\## Step 8: Run App on Physical Phone



Install and run the app from Android Studio.



Important:



After changing contract-info.json, rebuild and reinstall the app so the phone uses the latest blockchain configuration.



\## Step 9: Demo Flow



Use this flow:



1\. Admin login.

2\. Create election.

3\. Open QR check-in.

4\. Select created election.

5\. Check in voter.

6\. Go to voter flow.

7\. Select same election.

8\. Choose candidate.

9\. Submit vote.

10\. Confirm receipt/hash is shown.

11\. Verify receipt/hash.

12\. Return to admin/results.

13\. Close election early.

14\. Confirm results become visible.

15\. Confirm vote count is correct.



\## Expected Working Flow



Create election -> QR check-in -> Vote -> Receipt/hash -> Verify -> Close election -> Results



\## Troubleshooting



If the phone cannot connect:



\- Confirm phone and laptop are on the same Wi-Fi.

\- Confirm Hardhat node is still running.

\- Confirm rpcUrl uses laptop IP, not 127.0.0.1.

\- Confirm port 8545 is reachable.

\- Rebuild and reinstall the app after editing contract-info.json.



If contract address fails:



\- Redeploy using npx hardhat run scripts/deploy.ts.

\- Copy the new contract address into contract-info.json.

\- Check eth\_getCode again.

\- Rebuild and reinstall the app.



If laptop IP changes:



\- Run ipconfig again.

\- Update rpcUrl in contract-info.json.

\- Rebuild and reinstall the app.



\## Prototype Notes



This project uses a local Hardhat blockchain for university demo and testing.



Bundled wallet files are for local prototype testing only.



The QR check-in flow is a prototype check-in mechanism and not a national identity system.



Advanced privacy features such as zero-knowledge proofs are outside this version scope.

