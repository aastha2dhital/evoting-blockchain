import { defineConfig } from "hardhat/config";
import hardhatToolboxMochaEthers from "@nomicfoundation/hardhat-toolbox-mocha-ethers";

export default defineConfig({
  plugins: [hardhatToolboxMochaEthers],
  solidity: "0.8.28",
  paths: {
    tests: {
      mocha: "./test",
    },
  },
  test: {
    mocha: {
      timeout: 20_000,
    },
  },
});