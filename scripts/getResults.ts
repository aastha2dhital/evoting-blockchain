import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { ethers } = await network.connect("localhost");

  const contract: any = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  const election = await contract.getElection(1);
  const candidateCount = Number(election[4]);

  console.log("Election Results for:", election[1]);
  console.log("-----------------------------");

  for (let i = 1; i <= candidateCount; i++) {
    const candidate = await contract.getCandidate(1, i);
    console.log(
      `${candidate[1]}: ${candidate[2].toString()} vote(s)`
    );
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});