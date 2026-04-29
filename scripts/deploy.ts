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
    network: "hardhat-local",
    rpcUrl: "http://10.0.2.2:8545"
  };

  const demoVoterWallets = {
    wallets: [
      {
        label: "Voter 1",
        privateKey:
          "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d"
      },
      {
        label: "Voter 2",
        privateKey:
          "0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a"
      },
      {
        label: "Voter 3",
        privateKey:
          "0x7c852118294e51e653712a81e05800f419141751be58f605c371e15141b007a6"
      },
      {
        label: "Voter 4",
        privateKey:
          "0x47e179ec197488593b187f80a00eb0da91f1b9d0b13f8733639f19c30a34926a"
      },
      {
        label: "Voter 5",
        privateKey:
          "0x8b3a350cf5c34c9194ca85829a2df0ec3153be0318b5e2d3348e872092edffba"
      }
    ]
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

  writeFileSync(
    path.join(assetsDir, "voter-wallets.json"),
    JSON.stringify(demoVoterWallets, null, 2),
    "utf-8"
  );

  console.log("EVoting deployed successfully.");
  console.log("Contract address:", contractAddress);
  console.log("Android asset updated: app/src/main/assets/contract-info.json");
  console.log("Android asset updated: app/src/main/assets/evoting-abi.json");
  console.log("Android asset updated: app/src/main/assets/voter-wallets.json");
  console.log("");
  console.log("Demo voter addresses:");
  console.log("Voter 1: 0x70997970C51812dc3A010C7d01b50e0d17dc79C8");
  console.log("Voter 2: 0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC");
  console.log("Voter 3: 0x90F79bf6EB2c4f870365E785982E1f101E93b906");
  console.log("Voter 4: 0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65");
  console.log("Voter 5: 0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});