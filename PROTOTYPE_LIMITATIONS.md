\# EVotingMobileApp Prototype Limitations and Security Notes



\## Purpose



EVotingMobileApp is a university Final Year Project prototype that demonstrates a decentralized mobile e-voting flow using Android, QR check-in, and blockchain smart contracts.



The project is designed for assessment, testing, and demonstration on a local Hardhat blockchain. It is not intended for real national election deployment.



\## Prototype Scope



The current version demonstrates:



\- Admin election creation

\- Candidate setup

\- Voter eligibility/whitelisting

\- QR-based voter check-in

\- Blockchain-based vote submission

\- One-vote-per-voter enforcement

\- Transaction receipt/hash display

\- Receipt/hash verification

\- Election closing

\- Results display after election close



\## Local Blockchain Limitation



The project uses a local Hardhat blockchain for testing and demonstration.



This means:



\- The blockchain node must be running before using the app.

\- The app must use the correct laptop IP address when testing on a physical phone.

\- The contract address may change after redeployment.

\- The app must be rebuilt/reinstalled after updating contract-info.json.



This setup is suitable for a prototype demo, but not for production deployment.



\## Wallet and Key Limitation



The project includes local demo wallet configuration for prototype testing.



Important notes:



\- Bundled wallet files are for local Hardhat demo/testing only.

\- They must not be used on a real public blockchain.

\- They must not be treated as production authentication.

\- A real deployment would require secure wallet management and proper external wallet signing.



\## Admin Access Limitation



The app uses a prototype admin access flow suitable for assessment/demo use.



A production system would require stronger authentication, role-based access control, secure session handling, and server-side identity verification.



\## QR Check-In Limitation



The QR check-in flow demonstrates the required polling-station attendance step.



Current limitation:



\- The QR/check-in mechanism is prototype-level.

\- It is not connected to a national ID system.

\- It does not provide advanced protection against all replay/spoofing attacks.



A stronger production version would use time-limited QR tokens, polling-officer authentication, encrypted check-in payloads, and secure voter identity verification.



\## Privacy Limitation



The project avoids unnecessary national identity integration and focuses on a prototype voting flow.



However:



\- Blockchain transactions are visible on-chain.

\- Wallet addresses may be linkable to actions.

\- This version does not implement zero-knowledge proofs or full coercion resistance.



These advanced privacy techniques are outside the scope of this version and are listed as future improvements.



\## Network Limitation



The app depends on network access to the local blockchain node.



Possible issues:



\- Phone and laptop must be on the same Wi-Fi.

\- Firewall or port issues may block connection.

\- The laptop IP address can change.

\- Hardhat node restart can reset blockchain state.



The demo run guide explains how to check and fix these issues.



\## Testing Evidence



The following have been tested:



\- Smart contract tests using npx hardhat test

\- Android build using .\\gradlew :app:assembleDebug

\- Android unit test task using .\\gradlew :app:testDebugUnitTest

\- Full physical phone flow from election creation to result viewing



More details are recorded in TESTING\_EVIDENCE.md.



\## Future Improvements



Possible future improvements include:



\- Production wallet integration

\- Stronger admin authentication

\- Time-limited QR check-in tokens

\- Better privacy through commit-reveal or zero-knowledge proof methods

\- Public testnet deployment

\- Larger-scale testing

\- More complete Android UI and instrumentation tests

\- Stronger accessibility support

\- Improved multilingual coverage



\## Summary



The current implementation is appropriate as a university prototype because it demonstrates the core required decentralized e-voting flow. The limitations above should be explained clearly during assessment to show awareness of security, privacy, scalability, and deployment risks.

