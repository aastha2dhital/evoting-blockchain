# Decentralized E-Voting Mobile Application

This project is a blockchain-based electronic voting mobile application developed for my university Final Year Project.

The main idea of this project is to show how a mobile app can be connected with a blockchain smart contract so that voting can be more transparent and harder to tamper with. The app allows an admin to create an election, register voters, check in voters using a QR code, allow voters to vote, and then show the results after the election is closed.

## Project Overview

The project uses an Android mobile app with a Solidity smart contract running on a local Hardhat blockchain.

The Android app is built using Kotlin and Jetpack Compose. The blockchain side is handled using Hardhat and a Solidity smart contract.

The app is mainly made for demonstration and assessment purposes. It is not meant to be used as a real national voting system.

## Main Features

- Admin login using a demo OTP flow
- Election creation by admin
- Candidate setup
- Eligible voter registration
- QR-based voter check-in
- Time-limited QR check-in with a nonce to reduce simple replay issues
- Voting through blockchain transaction
- One vote per voter
- Transaction hash receipt after voting
- Transaction hash QR scan/share support
- Receipt verification
- Results shown only after the election is closed
- Winner shown with vote count
- Read-only observer view for turnout and results
- Candidate symbols shown in voting and results screens

## Blockchain Part

The project uses a local Hardhat blockchain for testing.

The smart contract handles the main election functions such as creating elections, registering candidates, registering voters, checking in voters, casting votes, closing elections, and reading results.

The Android app reads the deployed contract details from:

```text
app/src/main/assets/contract-info.json
