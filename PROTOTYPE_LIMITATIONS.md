# EVotingMobileApp Prototype Limitations



## Purpose



EVotingMobileApp is my university Final Year Project prototype.



The project shows how an Android mobile app can work with a blockchain smart contract for an e-voting system. It includes admin election creation, QR check-in, voting, transaction receipts, result viewing, and an observer view.



This project is made for testing and demonstration. It is not a real production voting system.



## What the Prototype Shows



The current version shows:



\- admin login using demo OTP

\- election creation

\- candidate setup

\- eligible voter registration

\- QR-based voter check-in

\- time-limited QR pass

\- nonce check for QR replay protection in the current session

\- blockchain-based vote submission

\- one vote per voter

\- transaction hash receipt after voting

\- transaction hash QR scan/share

\- receipt/hash verification

\- election closing

\- result display after election closes

\- winner display with vote count

\- observer view for turnout and results



## Local Blockchain Limitation



The project uses a local Hardhat blockchain.



This means the blockchain must be running on the laptop before using the app.



For emulator testing, the app usually uses:



http://10.0.2.2:8545



For real phone testing, the app needs the laptop Wi-Fi IP address, for example:



http://192.168.100.7:8545



This is fine for a university demo, but it is not the same as a real deployed blockchain system.



Some limitations are:



\- Hardhat node must stay running

\- blockchain data can reset when Hardhat restarts

\- contract address can change after redeployment

\- contract-info.json must be correct

\- the app must be rebuilt after changing blockchain details



## Demo Wallet and Private Key Limitation



The app uses demo wallet files for local Hardhat testing.



These wallets are included so the app can send blockchain transactions during the demo without needing every user to set up MetaMask or WalletConnect.



This is only for testing.



In a real app:



\- private keys should not be inside the Android app

\- private keys should not be bundled inside the APK

\- demo wallets should not be used on a real public blockchain

\- users should sign transactions using a secure wallet



A real version should use WalletConnect, Reown, MetaMask, or another secure wallet method.



## Wallet Connection Limitation



The project is based on wallet-based voting, but the current version mainly uses demo wallets for local testing.



This means the real production wallet connection is not fully complete yet.



The demo wallets make the project easier to test, but they are not a production-safe login or wallet system.



## Admin Access Limitation



The app uses a demo OTP for admin login.



This is better than a simple hardcoded PIN, but it is still only for prototype use.



A real system would need stronger admin security such as:



\- real OTP verification

\- backend authentication

\- role-based access

\- session expiry

\- audit logs

\- protection against repeated login attempts



## QR Check-In Improvement



The QR check-in has been improved from the earlier version.



The QR now contains:



\- wallet address

\- issued time

\- expiry time

\- random nonce



The QR format is:



SecureVoteCheckIn|wallet=<address>|issuedAt=<millis>|expiresAt=<millis>|nonce=<random>



The admin scanner checks if:



\- the QR format is correct

\- the wallet address is valid

\- the QR has expired

\- the QR nonce was already scanned in the current admin screen session



This makes the prototype better because expired or repeated QR codes can be rejected.



## QR Check-In Limitation



The QR check-in is still not production-level security.



Current limitations are:



\- nonce checking only works while the current admin screen is open

\- used nonces are not saved in a backend database

\- QR payload is not signed

\- QR payload is not encrypted

\- there is no national ID verification

\- there is no biometric verification

\- polling officer identity is not strongly verified



A real system should use signed QR tokens, backend checking, encrypted payloads, and stronger identity verification.



## Privacy Limitation



The smart contract does not store personal voter details like name or national ID.



However, blockchain transactions and wallet addresses can still be visible.



This means there can still be privacy issues.



A more advanced version could use:



\- commit-reveal voting

\- zero-knowledge proofs

\- anonymous credentials

\- stronger voter privacy methods

\- better identity separation



These are outside the scope of this version.



## Observer View Limitation



The observer view is read-only and is used to show turnout and results.



It helps with transparency, but a real system would need to make sure no private voter information is exposed.



## Candidate Symbol Limitation



Candidate symbols are currently only used in the app UI.



They make the voting and results screens look better, but they do not change the smart contract candidate data.



A real system would need verified candidate symbols or party logos from an official election authority.



## Multilingual Limitation



The project has started Nepali/multilingual support, but it is not fully complete.



Some screens may still have hardcoded English text.



A complete version would need:



\- all text moved into string resources

\- proper Nepali translations

\- language switching

\- layout testing for longer translated text



## Network Limitation



The app depends on the laptop blockchain node.



Possible issues are:



\- phone and laptop may not be on the same Wi-Fi

\- firewall may block port 8545

\- laptop IP address may change

\- Hardhat node may stop

\- contract address may change after redeployment



The demo run guide explains how to handle these issues.



## Testing



During development, I tested:



\- Android build using .\\gradlew :app:assembleDebug

\- smart contract deployment

\- election creation

\- QR check-in

\- voting flow

\- transaction hash receipt

\- receipt verification

\- result viewing after closing election

\- observer view



More details can be added in TESTING\_EVIDENCE.md.



## Future Improvements



Future improvements could include:



\- full WalletConnect/Reown integration

\- stronger admin authentication

\- signed QR tokens

\- encrypted QR payloads

\- backend replay protection

\- public testnet deployment

\- better privacy features

\- more complete Nepali language support

\- better accessibility testing

\- more UI polish

\- proper production audit logs



## Summary



This project is a working university prototype which shows the main e-voting flow using Android and blockchain. It also shows QR check-in, transaction receipts, results, and observer transparency.


