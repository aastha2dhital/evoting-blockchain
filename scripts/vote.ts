import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { ethers } = await network.connect("localhost");

  const [, voter] = await ethers.getSigners(); // Account #1
  const contract: any = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  try {
    const tx = await contract.connect(voter).vote(1, 1);
    await tx.wait();
    console.log("Vote cast successfully.");
  } catch (error: any) {
    console.log("Voting failed: voter may have already voted or is not eligible.");
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});