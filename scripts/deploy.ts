import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import path from "node:path";
import { network } from "hardhat";

async function main() {
  const { ethers } = await network.connect("localhost");

  const evoting = await ethers.deployContract("EVoting");
  await evoting.waitForDeployment();

  const contractAddress = await evoting.getAddress();
  const projectRoot = process.cwd();
  const assetsDir = path.join(projectRoot, "app", "src", "main", "assets");
  const artifactPath = path.join(
    projectRoot,
    "artifacts",
    "contracts",
    "EVoting.sol",
    "EVoting.json"
  );

  const artifact = JSON.parse(readFileSync(artifactPath, "utf-8"));

  mkdirSync(assetsDir, { recursive: true });

  const contractInfo = {
    contractAddress,
    network: "localhost",
    rpcUrl: "http://10.0.2.2:8545"
  };

  writeFileSync(
    path.join(assetsDir, "contract-info.json"),
    JSON.stringify(contractInfo, null, 2),
    "utf-8"
  );

  writeFileSync(
    path.join(assetsDir, "evoting-abi.json"),
    JSON.stringify(artifact.abi, null, 2),
    "utf-8"
  );

  console.log("EVoting deployed successfully.");
  console.log("Contract address:", contractAddress);
  console.log("Android asset updated: app/src/main/assets/contract-info.json");
  console.log("Android asset updated: app/src/main/assets/evoting-abi.json");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});