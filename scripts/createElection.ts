import { network } from "hardhat";

const CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { ethers } = await network.connect("localhost");

  const contract = await ethers.getContractAt("EVoting", CONTRACT_ADDRESS);

  const now = Math.floor(Date.now() / 1000);
  const startTime = now + 60;
  const endTime = now + 3600;

  const tx = await contract.createElection(
    "Student Council Election",
    startTime,
    endTime
  );

  await tx.wait();

  console.log("Election created successfully.");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});