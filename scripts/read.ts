import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { ethers } = await network.connect("localhost");

  const contract = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  const electionCount = await contract.electionCount();

  console.log("Total Elections:", electionCount.toString());
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});