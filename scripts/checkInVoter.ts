import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
const VOTER_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";

async function main() {
  const { ethers } = await network.connect("localhost");

  const contract = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  const tx = await contract.checkInVoter(1, VOTER_ADDRESS);
  await tx.wait();

  console.log("Voter checked in successfully.");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});