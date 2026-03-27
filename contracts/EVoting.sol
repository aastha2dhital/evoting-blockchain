// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract EVoting {
    address public owner;

    constructor() {
        owner = msg.sender;
    }

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can perform this action");
        _;
    }

    struct Candidate {
        uint256 id;
        string name;
        uint256 voteCount;
    }

    struct Election {
        uint256 id;
        string title;
        uint256 startTime;
        uint256 endTime;
        bool exists;
        uint256 candidateCount;
    }

    uint256 public electionCount;

    mapping(uint256 => Election) public elections;
    mapping(uint256 => mapping(uint256 => Candidate)) public candidates;

    // electionId => voter wallet => checked in or not
    mapping(uint256 => mapping(address => bool)) public checkedInVoters;

    // electionId => voter wallet => voted or not
    mapping(uint256 => mapping(address => bool)) public hasVoted;

    event ElectionCreated(
        uint256 indexed electionId,
        string title,
        uint256 startTime,
        uint256 endTime
    );

    event CandidateAdded(
        uint256 indexed electionId,
        uint256 indexed candidateId,
        string name
    );

    event VoterCheckedIn(uint256 indexed electionId, address indexed voter);

    event VoteCast(
        uint256 indexed electionId,
        uint256 indexed candidateId,
        address indexed voter
    );

    function createElection(
        string memory _title,
        uint256 _startTime,
        uint256 _endTime
    ) public onlyOwner {
        require(_startTime < _endTime, "Start time must be before end time");

        electionCount++;

        elections[electionCount] = Election({
            id: electionCount,
            title: _title,
            startTime: _startTime,
            endTime: _endTime,
            exists: true,
            candidateCount: 0
        });

        emit ElectionCreated(electionCount, _title, _startTime, _endTime);
    }

    function addCandidate(
        uint256 _electionId,
        string memory _name
    ) public onlyOwner {
        require(elections[_electionId].exists, "Election does not exist");

        elections[_electionId].candidateCount++;
        uint256 candidateId = elections[_electionId].candidateCount;

        candidates[_electionId][candidateId] = Candidate({
            id: candidateId,
            name: _name,
            voteCount: 0
        });

        emit CandidateAdded(_electionId, candidateId, _name);
    }

    function checkInVoter(
        uint256 _electionId,
        address _voter
    ) public onlyOwner {
        require(elections[_electionId].exists, "Election does not exist");
        require(!checkedInVoters[_electionId][_voter], "Voter already checked in");

        checkedInVoters[_electionId][_voter] = true;

        emit VoterCheckedIn(_electionId, _voter);
    }

    function vote(uint256 _electionId, uint256 _candidateId) public {
        Election memory election = elections[_electionId];

        require(election.exists, "Election does not exist");
        require(block.timestamp >= election.startTime, "Election has not started");
        require(block.timestamp <= election.endTime, "Election has ended");
        require(checkedInVoters[_electionId][msg.sender], "Voter not checked in");
        require(!hasVoted[_electionId][msg.sender], "You have already voted");
        require(
            _candidateId > 0 && _candidateId <= election.candidateCount,
            "Invalid candidate"
        );

        hasVoted[_electionId][msg.sender] = true;
        candidates[_electionId][_candidateId].voteCount++;

        emit VoteCast(_electionId, _candidateId, msg.sender);
    }

    function getElection(
        uint256 _electionId
    )
        public
        view
        returns (
            uint256 id,
            string memory title,
            uint256 startTime,
            uint256 endTime,
            uint256 candidateCount
        )
    {
        require(elections[_electionId].exists, "Election does not exist");

        Election memory e = elections[_electionId];
        return (e.id, e.title, e.startTime, e.endTime, e.candidateCount);
    }

    function getCandidate(
        uint256 _electionId,
        uint256 _candidateId
    )
        public
        view
        returns (
            uint256 id,
            string memory name,
            uint256 voteCount
        )
    {
        require(elections[_electionId].exists, "Election does not exist");
        require(
            _candidateId > 0 && _candidateId <= elections[_electionId].candidateCount,
            "Invalid candidate"
        );

        Candidate memory c = candidates[_electionId][_candidateId];
        return (c.id, c.name, c.voteCount);
    }
}