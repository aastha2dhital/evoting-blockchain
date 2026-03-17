import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { ethers } = await network.connect("localhost");

  const contract = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  let tx;

  tx = await contract.addCandidate(1, "Alice");
  await tx.wait();

  tx = await contract.addCandidate(1, "Bob");
  await tx.wait();

  tx = await contract.addCandidate(1, "Charlie");
  await tx.wait();

  console.log("Candidates added successfully.");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});