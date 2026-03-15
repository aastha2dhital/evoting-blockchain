import { expect } from "chai";
import { network } from "hardhat";

describe("EVoting Contract", function () {
  let voting: any;
  let owner: any;
  let voter1: any;
  let voter2: any;
  let ethers: any;

  beforeEach(async function () {
    const connection = await network.connect();
    ethers = connection.ethers;

    [owner, voter1, voter2] = await ethers.getSigners();

    const Voting = await ethers.getContractFactory("EVoting");
    voting = await Voting.deploy();
    await voting.waitForDeployment();
  });

  it("Should create an election", async function () {
    const now = Math.floor(Date.now() / 1000);

    await voting.createElection("University President", now, now + 3600);

    const election = await voting.getElection(1);
    expect(election.title).to.equal("University President");
  });

  it("Should add candidates", async function () {
    const now = Math.floor(Date.now() / 1000);

    await voting.createElection("Student Election", now, now + 3600);
    await voting.addCandidate(1, "Alice");
    await voting.addCandidate(1, "Bob");

    const candidate = await voting.getCandidate(1, 1);
    expect(candidate.name).to.equal("Alice");
  });

  it("Should allow checked-in voter to vote", async function () {
    const now = Math.floor(Date.now() / 1000);

    await voting.createElection("Voting Test", now, now + 3600);
    await voting.addCandidate(1, "Alice");
    await voting.checkInVoter(1, voter1.address);

    await voting.connect(voter1).vote(1, 1);

    const candidate = await voting.getCandidate(1, 1);
    expect(candidate.voteCount).to.equal(1n);
  });

  it("Should prevent double voting", async function () {
    const now = Math.floor(Date.now() / 1000);

    await voting.createElection("Voting Test", now, now + 3600);
    await voting.addCandidate(1, "Alice");
    await voting.checkInVoter(1, voter1.address);

    await voting.connect(voter1).vote(1, 1);

    await expect(
      voting.connect(voter1).vote(1, 1)
    ).to.be.revertedWith("You have already voted");
  });
});