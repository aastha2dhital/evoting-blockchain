import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { ethers } = await network.connect("localhost");

  const contract = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  const election = await contract.getElection(1);

  console.log("Election ID:", election[0].toString());
  console.log("Title:", election[1]);
  console.log("Start Time:", election[2].toString());
  console.log("End Time:", election[3].toString());
  console.log("Candidate Count:", election[4].toString());
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});