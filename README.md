# Decentralized E-Voting Mobile Application



This project is a blockchain-based electronic voting mobile application developed for my university Final Year Project.



The main idea of this project is to show how a mobile app can connect with a blockchain smart contract so that voting can be more transparent and harder to tamper with. The app allows an admin to create an election, register voters, check in voters using a QR code, allow voters to vote, and then show the results after the election is closed.



## Project Overview



The project uses an Android mobile app with a Solidity smart contract running on a local Hardhat blockchain.



The Android app is built using Kotlin and Jetpack Compose. The blockchain side is handled using Hardhat and a Solidity smart contract.



This project is mainly made for demonstration and assessment. It is not meant to be used as a real national voting system.



## Main Features



\- Admin login using a demo OTP flow

\- Election creation by admin

\- Candidate setup

\- Eligible voter registration

\- QR-based voter check-in

\- Time-limited QR check-in with a nonce to reduce simple replay issues

\- Voting through blockchain transaction

\- One vote per voter

\- Transaction hash receipt after voting

\- Transaction hash QR scan/share support

\- Receipt verification

\- Results shown only after the election is closed

\- Winner shown with vote count

\- Read-only observer view for turnout and results

\- Candidate symbols shown in voting and results screens



## Blockchain Part



The project uses a local Hardhat blockchain for testing.



The smart contract handles the main election functions such as creating elections, registering candidates, registering voters, checking in voters, casting votes, closing elections, and reading results.



The Android app reads the deployed contract details from:



```text

app/src/main/assets/contract-info.json

```



For Android emulator testing, the RPC URL normally uses:



```text

http://10.0.2.2:8545

```



For physical phone testing, the RPC URL needs to use the laptop Wi-Fi IP address, for example:



```text

http://192.168.100.7:8545

```



## Demo Wallets



This project includes demo wallet files in the Android assets folder. These are used only for the local Hardhat blockchain demo.



The demo wallets make it easier to test the app because the app can send transactions without setting up MetaMask or WalletConnect for every test user.



These wallets are not safe for production. In a real app, private keys should never be included inside the mobile app. A real version should use a secure wallet connection such as WalletConnect, Reown, MetaMask, or another proper wallet/authentication method.



## QR Check-In



The QR check-in feature is used to check in a voter before they vote.



The QR payload now includes the voter wallet address, issue time, expiry time, and a random nonce. This makes it better than just scanning a plain wallet address.



The format used is:



```text

SecureVoteCheckIn|wallet=<address>|issuedAt=<millis>|expiresAt=<millis>|nonce=<random>

```



The admin scanner checks if the QR code is valid, expired, or already used in the current admin screen session.



This is still only a prototype security improvement. A real system should use signed QR tokens, backend validation, and stronger replay protection.



## Observer View



The app also includes a read-only observer view.



This screen can show turnout information and results after the election is closed. It does not give admin controls, so it is only for viewing public election information.



## Limitations



This project is a working academic prototype, but it still has limitations:



\- It uses a local Hardhat blockchain only.

\- Demo wallet private keys are bundled in the app for testing.

\- The admin OTP is a prototype flow.

\- QR replay protection is only session-based.

\- WalletConnect/Reown is not fully implemented as a production wallet flow.

\- It does not include national ID verification.

\- It does not include advanced privacy features like zero-knowledge proofs.

\- Nepali/multilingual support is started but not fully complete.



## How to Run



Start the Hardhat node:



```powershell

npx hardhat node

```



Deploy the smart contract:



```powershell

npx hardhat run scripts/deploy.ts

```



Build the Android app:



```powershell

.\\gradlew :app:assembleDebug

```



For emulator testing, make sure `contract-info.json` uses:



```text

http://10.0.2.2:8545

```



For physical phone testing, use the laptop Wi-Fi IP address instead.



## Demo Flow



A normal demo flow is:



1\. Admin completes demo OTP verification.

2\. Admin creates an election.

3\. Admin opens QR check-in.

4\. Voter opens the Voter Access screen.

5\. Voter shows the time-limited QR pass.

6\. Admin scans the QR pass and checks in the voter.

7\. Voter selects the election.

8\. Voter selects a candidate.

9\. Voter submits the vote.

10\. App shows the transaction hash receipt.

11\. Receipt/hash can be verified.

12\. Admin closes the election.

13\. Results and winner are shown.

14\. Observer view shows turnout and public result information.



## Final Notes



This project is focused on showing a complete working prototype of a blockchain-based voting system. It demonstrates the main flow from election creation to voting and result viewing, while also explaining the security and deployment limitations clearly.


