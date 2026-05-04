# Demo Security Notes

This project is a Final Year Project prototype for a decentralized e-voting mobile application.

The main purpose of this project is to show how an Android app can work together with a blockchain smart contract for a voting system. It shows the full flow from creating an election to voting and viewing the final result.

This project is not a real production voting system. It is made for testing, learning, and university demonstration.

## What the App Demonstrates

The app demonstrates:

- admin login using a demo OTP
- creating an election
- adding candidates
- registering eligible voters
- checking in voters using QR code
- allowing checked-in voters to vote
- recording votes using blockchain transactions
- showing a transaction hash after voting
- sharing or scanning the transaction hash as a QR code
- verifying a vote transaction
- viewing results after the election is closed
- showing the winner and vote count
- observer view for public turnout and results

## Demo Wallets

The app includes demo wallet files inside the Android app assets folder, such as:

- admin-wallet.json
- voter-wallet.json
- voter-wallets.json

These wallet files are only used for local testing with the Hardhat blockchain.

I used demo wallets so that the app can send blockchain transactions during testing without needing every tester to set up MetaMask or WalletConnect.

This is useful for the demo, but it is not safe for a real production app.

In a real voting app:

- private keys should not be stored inside the Android app
- private keys should not be bundled inside the APK
- demo wallets should not be used on a real public blockchain
- users should sign transactions using a proper wallet or secure authentication method

A real version should use something like WalletConnect, Reown, MetaMask, a hardware wallet, or another secure wallet/key management system.

## Local Blockchain

This project uses a local Hardhat blockchain for testing.

For Android emulator testing, the app normally connects using:

http://10.0.2.2:8545

This is used because the Android emulator cannot directly use the laptop's localhost.

For physical phone testing, the app needs the laptop Wi-Fi IP address, for example:

http://192.168.100.7:8545

This local blockchain setup is fine for a university demo, but it is not the same as a real deployed blockchain system.

Some limitations are:

- Hardhat state can reset when the node restarts
- the contract address can change after redeploying
- the app needs the correct RPC URL
- the phone and laptop must be on the same network if testing on a real phone

## QR Check-In

The QR check-in feature has been improved.

At first, the QR check-in was mainly based on scanning a wallet address. Now the QR pass includes:

- wallet address
- issued time
- expiry time
- random nonce

The QR format is:

SecureVoteCheckIn|wallet=<address>|issuedAt=<millis>|expiresAt=<millis>|nonce=<random>

The admin scanner checks:

- if the QR format is correct
- if the wallet address is valid
- if the QR has expired
- if the same QR nonce was already scanned in the current admin screen session

This makes the QR check-in better for the prototype because expired or repeated QR codes can be rejected.

## QR Check-In Limitation

Even though the QR check-in is improved, it is still not production-level security.

Current limitations are:

- the nonce check only works while the current admin screen is open
- used nonces are not stored in a real backend database
- the QR payload is not cryptographically signed
- the QR payload is not encrypted
- there is no real national ID verification
- there is no biometric verification
- polling officer identity is not strongly verified

A real system should use signed QR tokens, backend validation, encrypted payloads, proper identity verification, and stronger replay protection.

## Admin Access Limitation

The admin login uses a demo OTP flow.

This is better than using a simple visible PIN, but it is still only for prototype/demo use.

A real system would need stronger login security such as:

- real OTP delivery
- backend verification
- role-based access control
- rate limiting
- session expiry
- audit logs

## Wallet Connection Limitation

The project is based on the idea of wallet-based voting.

However, the current app mainly uses demo wallet files for local Hardhat testing. This makes the demo easier to run, but it is not the same as a real production wallet connection.

A real version should use WalletConnect, Reown, MetaMask, or another secure wallet method so users can sign transactions safely.

## Privacy Limitation

The smart contract does not store personal voter details like name or national ID on the blockchain.

However, wallet addresses and transaction hashes are still visible on-chain. This means there can still be privacy issues.

A more advanced version could improve privacy using:

- commit-reveal voting
- zero-knowledge proofs
- stronger voter privacy methods
- anonymous credentials
- better identity separation

These features are outside the current project scope.

## Summary

This project is a working academic prototype.

It shows how blockchain can be used in a mobile voting app to make voting more transparent and auditable. At the same time, the project has clear limitations, especially around demo wallets, local blockchain setup, QR security, admin authentication, wallet connection, and privacy.

These limitations should be explained during the assessment to show that the project is a prototype and not a production-ready national voting system.