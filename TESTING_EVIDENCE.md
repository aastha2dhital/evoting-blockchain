# EVotingMobileApp Testing Evidence

## Branch

fix/core-voter-flow-stability

## Smart Contract Tests

Command used:

npx hardhat test

Result:

10 passing

Covered contract behaviours:

- Election creation
- Candidate addition
- Voter whitelist
- QR/check-in requirement
- Eligible checked-in voter can vote
- Non-eligible voter blocked
- Voter without check-in blocked
- Double voting blocked
- Invalid candidate blocked
- Election close early
- Voting after early close blocked
- Admin-only actions protected

## Android Build Test

Command used:

.\gradlew :app:assembleDebug

Result:

BUILD SUCCESSFUL

## Physical Phone Blockchain Test

Device type:

- Physical Android phone

Blockchain node:

- Local Hardhat node
- RPC URL used by app: http://192.168.100.7:8545
- Chain ID: 31337

Contract address used:

0x5FbDB2315678afecb367f032d93F642f64180aa3

## End-to-End Manual Test Flow

The following flow was tested successfully on a physical phone:

1. Admin logged in.
2. Admin created an election successfully.
3. Polling officer/admin opened QR check-in.
4. Voter was checked in successfully.
5. Voter accessed the voter flow.
6. Voter selected the same election.
7. Voter selected a candidate.
8. Vote was submitted successfully.
9. Transaction receipt/hash was shown.
10. Receipt was verified successfully on blockchain.
11. Admin closed the election successfully.
12. Results became visible after election close.
13. Vote count was correct.

## Result

The core non-UI project flow is working:

Create election → QR check-in → Vote → Receipt/hash → Verify → Close election → Results

## Prototype Notes

This project uses a local Hardhat blockchain for assessment/demo testing.

Important limitations:

- The RPC URL may need updating if the laptop Wi-Fi IP address changes.
- The bundled wallet files are for local prototype/demo testing only.
- The QR check-in uses the prototype voter wallet/check-in flow and is not a national-scale identity system.
- Advanced privacy features such as zero-knowledge proofs are outside this version’s scope.