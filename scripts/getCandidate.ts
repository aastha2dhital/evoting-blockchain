import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { ethers } = await network.connect("localhost");

  const contract = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  const candidate = await contract.getCandidate(1, 1);

  console.log("Candidate ID:", candidate[0].toString());
  console.log("Candidate Name:", candidate[1]);
  console.log("Vote Count:", candidate[2].toString());
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});