// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

contract EVoting {
    address public owner;

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
        bool isClosed;
        uint256 candidateCount;
        uint256 totalVotes;
    }

    uint256 public electionCount;

    mapping(uint256 => Election) public elections;
    mapping(uint256 => mapping(uint256 => Candidate)) public candidates;
    mapping(uint256 => mapping(address => bool)) public eligibleVoters;
    mapping(uint256 => mapping(address => bool)) public checkedInVoters;
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

    event VoterWhitelisted(
        uint256 indexed electionId,
        address indexed voter
    );

    event VoterCheckedIn(
        uint256 indexed electionId,
        address indexed voter
    );

    event VoteCast(
        uint256 indexed electionId,
        uint256 indexed candidateId,
        address indexed voter
    );

    event ElectionClosed(
        uint256 indexed electionId,
        uint256 closedAt
    );

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can perform this action");
        _;
    }

    modifier electionExists(uint256 _electionId) {
        require(elections[_electionId].exists, "Election does not exist");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function createElection(
        string memory _title,
        uint256 _startTime,
        uint256 _endTime
    ) public onlyOwner {
        require(bytes(_title).length > 0, "Election title is required");
        require(_startTime < _endTime, "Start time must be before end time");

        electionCount++;

        elections[electionCount] = Election({
            id: electionCount,
            title: _title,
            startTime: _startTime,
            endTime: _endTime,
            exists: true,
            isClosed: false,
            candidateCount: 0,
            totalVotes: 0
        });

        emit ElectionCreated(electionCount, _title, _startTime, _endTime);
    }

    function addCandidate(
        uint256 _electionId,
        string memory _name
    ) public onlyOwner electionExists(_electionId) {
        require(bytes(_name).length > 0, "Candidate name is required");
        require(!elections[_electionId].isClosed, "Election is closed");

        elections[_electionId].candidateCount++;
        uint256 candidateId = elections[_electionId].candidateCount;

        candidates[_electionId][candidateId] = Candidate({
            id: candidateId,
            name: _name,
            voteCount: 0
        });

        emit CandidateAdded(_electionId, candidateId, _name);
    }

    function addEligibleVoter(
        uint256 _electionId,
        address _voter
    ) public onlyOwner electionExists(_electionId) {
        require(!elections[_electionId].isClosed, "Election is closed");
        require(_voter != address(0), "Invalid voter address");
        require(!eligibleVoters[_electionId][_voter], "Voter already eligible");

        eligibleVoters[_electionId][_voter] = true;
        emit VoterWhitelisted(_electionId, _voter);
    }

    function addEligibleVoters(
        uint256 _electionId,
        address[] memory _voters
    ) public onlyOwner electionExists(_electionId) {
        require(!elections[_electionId].isClosed, "Election is closed");
        require(_voters.length > 0, "At least one voter is required");

        for (uint256 i = 0; i < _voters.length; i++) {
            address voter = _voters[i];
            require(voter != address(0), "Invalid voter address");

            if (!eligibleVoters[_electionId][voter]) {
                eligibleVoters[_electionId][voter] = true;
                emit VoterWhitelisted(_electionId, voter);
            }
        }
    }

    function checkInVoter(
        uint256 _electionId,
        address _voter
    ) public onlyOwner electionExists(_electionId) {
        require(!elections[_electionId].isClosed, "Election is closed");
        require(block.timestamp <= elections[_electionId].endTime, "Election has ended");
        require(_voter != address(0), "Invalid voter address");
        require(eligibleVoters[_electionId][_voter], "Voter is not eligible");
        require(!checkedInVoters[_electionId][_voter], "Voter already checked in");

        checkedInVoters[_electionId][_voter] = true;
        emit VoterCheckedIn(_electionId, _voter);
    }

    function closeElectionEarly(
        uint256 _electionId
    ) public onlyOwner electionExists(_electionId) {
        Election storage election = elections[_electionId];

        require(!election.isClosed, "Election already closed");

        election.isClosed = true;
        election.endTime = block.timestamp;

        emit ElectionClosed(_electionId, block.timestamp);
    }

    function vote(
        uint256 _electionId,
        uint256 _candidateId
    ) public electionExists(_electionId) {
        Election storage election = elections[_electionId];

        require(!election.isClosed, "Election is closed");
        require(block.timestamp >= election.startTime, "Election has not started");
        require(block.timestamp <= election.endTime, "Election has ended");
        require(eligibleVoters[_electionId][msg.sender], "Voter is not eligible");
        require(checkedInVoters[_electionId][msg.sender], "Voter not checked in");
        require(!hasVoted[_electionId][msg.sender], "You have already voted");
        require(
            _candidateId > 0 && _candidateId <= election.candidateCount,
            "Invalid candidate"
        );

        hasVoted[_electionId][msg.sender] = true;
        candidates[_electionId][_candidateId].voteCount++;
        election.totalVotes++;

        emit VoteCast(_electionId, _candidateId, msg.sender);
    }

    function getElection(
        uint256 _electionId
    )
        public
        view
        electionExists(_electionId)
        returns (
            uint256 id,
            string memory title,
            uint256 startTime,
            uint256 endTime,
            uint256 candidateCount,
            uint256 totalVotes,
            bool isClosed
        )
    {
        Election memory e = elections[_electionId];
        return (
            e.id,
            e.title,
            e.startTime,
            e.endTime,
            e.candidateCount,
            e.totalVotes,
            e.isClosed
        );
    }

    function getCandidate(
        uint256 _electionId,
        uint256 _candidateId
    )
        public
        view
        electionExists(_electionId)
        returns (
            uint256 id,
            string memory name,
            uint256 voteCount
        )
    {
        require(
            _candidateId > 0 && _candidateId <= elections[_electionId].candidateCount,
            "Invalid candidate"
        );

        Candidate memory c = candidates[_electionId][_candidateId];
        return (c.id, c.name, c.voteCount);
    }

    function isEligibleVoter(
        uint256 _electionId,
        address _voter
    ) public view electionExists(_electionId) returns (bool) {
        return eligibleVoters[_electionId][_voter];
    }

    function isCheckedIn(
        uint256 _electionId,
        address _voter
    ) public view electionExists(_electionId) returns (bool) {
        return checkedInVoters[_electionId][_voter];
    }

    function getTurnoutCount(
        uint256 _electionId
    ) public view electionExists(_electionId) returns (uint256) {
        return elections[_electionId].totalVotes;
    }

    function hasElectionEnded(
        uint256 _electionId
    ) public view electionExists(_electionId) returns (bool) {
        Election memory election = elections[_electionId];
        return election.isClosed || block.timestamp > election.endTime;
    }
}