import { defineConfig } from "hardhat/config";
import hardhatToolboxMochaEthers from "@nomicfoundation/hardhat-toolbox-mocha-ethers";
import "@nomicfoundation/hardhat-ethers";

export default defineConfig({
  solidity: {
    version: "0.8.28",
  },
  plugins: [hardhatToolboxMochaEthers],
});