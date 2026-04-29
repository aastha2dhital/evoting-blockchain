import { readFileSync } from "node:fs";
import { network } from "hardhat";

async function main() {
  const { ethers } = await network.connect("localhost");

  const info = JSON.parse(
    readFileSync("app/src/main/assets/contract-info.json", "utf-8")
  );

  const contract = await ethers.getContractAt("EVoting", info.contractAddress);

  const electionId = 3;
  const voter = "0x70997970c51812dc3a010c7d01b50e0d17dc79c8";

  console.log("Contract:", info.contractAddress);
  console.log("Election ID:", electionId);
  console.log("Voter:", voter);

  console.log("isEligibleVoter:", await contract.isEligibleVoter(electionId, voter));
  console.log("isCheckedIn:", await contract.isCheckedIn(electionId, voter));
  console.log("hasVoted:", await contract.hasVoted(electionId, voter));
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
