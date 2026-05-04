# Demo Security Notes

This project is a final year university prototype for a decentralized e-voting mobile application. The main purpose of the project is to show how blockchain, smart contracts, QR check-in, and mobile voting can work together in a voting system.

The app demonstrates the main flow of the system, including:

- creating an election
- adding candidates
- registering eligible voters
- checking in voters using QR code
- allowing only checked-in voters to vote
- recording votes on the blockchain
- showing a transaction hash after voting
- verifying a vote transaction
- viewing results after the election is closed

## Demo Wallets

Some wallet files are included inside the Android app assets folder, such as:

- `admin-wallet.json`
- `voter-wallet.json`
- `voter-wallets.json`

These are only used for local testing and demonstration.

For the prototype, I used demo wallets so that the app can send blockchain transactions during testing without needing every user to set up MetaMask or WalletConnect. This makes the demo easier to run and test on a local Hardhat blockchain.

This is not how a real production voting app should handle wallets.

In a real system, private keys should never be stored inside the mobile app. Voters would sign transactions using a secure wallet such as MetaMask, WalletConnect, a hardware wallet, or another secure key management system.

## Local Blockchain

This project uses a local Hardhat blockchain for testing.

The Android emulator connects to the local blockchain using:

`http://10.0.2.2:8545`

This is used because Android emulators cannot access the computer’s `localhost` directly, so `10.0.2.2` is used instead.

## QR Check-in Limitation

The QR check-in feature in this prototype checks the registered voter wallet address and marks the voter as checked in.

This works for the prototype, but it is not fully secure for a real election system because the QR code is not time-limited.

In a real system, the QR code should use signed and time-limited tokens so that it cannot easily be reused or spoofed.

## Privacy Limitation

The smart contract does not store personal voter details such as name or national ID on the blockchain.

However, wallet addresses and transaction information are still visible on the blockchain, so there is still a possible privacy issue.

For a more advanced version, this could be improved using methods such as:

- commit-reveal voting
- zero-knowledge proofs
- stronger voter privacy methods
- secure identity verification
- time-limited QR tokens

## Summary

This project is not intended to be a full national voting system. It is a working academic prototype that shows how blockchain can be used to make voting more transparent, auditable, and difficult to tamper with.

The security limitations are understood and are included as future improvements.