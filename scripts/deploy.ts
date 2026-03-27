import { network } from "hardhat";

async function main() {
  const { ethers } = await network.connect();

  const evoting = await ethers.deployContract("EVoting");
  await evoting.waitForDeployment();

  console.log("EVoting contract deployed to:", await evoting.getAddress());
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});