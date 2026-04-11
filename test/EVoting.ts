import { expect } from "chai";
import { network } from "hardhat";

describe("EVoting Contract", function () {
  let voting: any;
  let owner: any;
  let voter1: any;
  let voter2: any;
  let outsider: any;
  let ethers: any;

  async function createReadyElection() {
    const latestBlock = await ethers.provider.getBlock("latest");
    const now = Number(latestBlock!.timestamp);

    await voting.createElection("University President", now - 60, now + 3600);
    await voting.addCandidate(1, "Alice");
    await voting.addCandidate(1, "Bob");
    await voting.addEligibleVoters(1, [voter1.address, voter2.address]);
  }

  beforeEach(async function () {
    const connection = await network.connect();
    ethers = connection.ethers;

    [owner, voter1, voter2, outsider] = await ethers.getSigners();

    const Voting = await ethers.getContractFactory("EVoting");
    voting = await Voting.deploy();
    await voting.waitForDeployment();
  });

  it("Should create an election", async function () {
    const latestBlock = await ethers.provider.getBlock("latest");
    const now = Number(latestBlock!.timestamp);

    await expect(voting.createElection("University President", now, now + 3600))
      .to.emit(voting, "ElectionCreated")
      .withArgs(1, "University President", now, now + 3600);

    const election = await voting.getElection(1);
    expect(election.title).to.equal("University President");
    expect(election.candidateCount).to.equal(0n);
    expect(election.totalVotes).to.equal(0n);
    expect(election.isClosed).to.equal(false);
  });

  it("Should add candidates and whitelist voters", async function () {
    await createReadyElection();

    const candidate = await voting.getCandidate(1, 1);
    expect(candidate.name).to.equal("Alice");
    expect(await voting.isEligibleVoter(1, voter1.address)).to.equal(true);
    expect(await voting.isEligibleVoter(1, outsider.address)).to.equal(false);
  });

  it("Should allow an eligible checked-in voter to vote", async function () {
    await createReadyElection();
    await voting.checkInVoter(1, voter1.address);

    await expect(voting.connect(voter1).vote(1, 1))
      .to.emit(voting, "VoteCast")
      .withArgs(1, 1, voter1.address);

    const candidate = await voting.getCandidate(1, 1);
    expect(candidate.voteCount).to.equal(1n);
    expect(await voting.getTurnoutCount(1)).to.equal(1n);
  });

  it("Should prevent voting if the voter is not eligible", async function () {
    await createReadyElection();

    await expect(voting.checkInVoter(1, outsider.address)).to.be.revertedWith(
      "Voter is not eligible"
    );

    await expect(voting.connect(outsider).vote(1, 1)).to.be.revertedWith(
      "Voter is not eligible"
    );
  });

  it("Should prevent voting without check-in", async function () {
    await createReadyElection();

    await expect(voting.connect(voter1).vote(1, 1)).to.be.revertedWith(
      "Voter not checked in"
    );
  });

  it("Should prevent double voting", async function () {
    await createReadyElection();
    await voting.checkInVoter(1, voter1.address);

    await voting.connect(voter1).vote(1, 1);

    await expect(voting.connect(voter1).vote(1, 1)).to.be.revertedWith(
      "You have already voted"
    );
  });

  it("Should reject an invalid candidate", async function () {
    await createReadyElection();
    await voting.checkInVoter(1, voter1.address);

    await expect(voting.connect(voter1).vote(1, 99)).to.be.revertedWith(
      "Invalid candidate"
    );
  });

  it("Should allow the owner to close an election early", async function () {
    await createReadyElection();

    await expect(voting.closeElectionEarly(1)).to.emit(voting, "ElectionClosed");

    const election = await voting.getElection(1);
    expect(election.isClosed).to.equal(true);
    expect(await voting.hasElectionEnded(1)).to.equal(true);
  });

  it("Should prevent voting after the election is closed early", async function () {
    await createReadyElection();
    await voting.checkInVoter(1, voter1.address);
    await voting.closeElectionEarly(1);

    await expect(voting.connect(voter1).vote(1, 1)).to.be.revertedWith(
      "Election is closed"
    );
  });

  it("Should restrict admin actions to the owner", async function () {
    const latestBlock = await ethers.provider.getBlock("latest");
    const now = Number(latestBlock!.timestamp);

    await expect(
      voting.connect(voter1).createElection("Unauthorized", now, now + 3600)
    ).to.be.revertedWith("Only owner can perform this action");

    await voting.createElection("Authorized", now, now + 3600);

    await expect(
      voting.connect(voter1).addCandidate(1, "Mallory")
    ).to.be.revertedWith("Only owner can perform this action");

    await expect(
      voting.connect(voter1).addEligibleVoter(1, voter1.address)
    ).to.be.revertedWith("Only owner can perform this action");

    await expect(
      voting.connect(voter1).checkInVoter(1, voter1.address)
    ).to.be.revertedWith("Only owner can perform this action");

    await expect(voting.connect(voter1).closeElectionEarly(1)).to.be.revertedWith(
      "Only owner can perform this action"
    );
  });
});