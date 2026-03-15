// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract EVoting {

    address public admin;
    uint public candidateCount;

    struct Candidate {
        uint id;
        string name;
        uint voteCount;
    }

    mapping(uint => Candidate) public candidates;
    mapping(address => bool) public voters;

    constructor() {
        admin = msg.sender;
    }

    modifier onlyAdmin() {
        require(msg.sender == admin, "Only admin allowed");
        _;
    }

    function addCandidate(string memory _name) public onlyAdmin {
        candidateCount++;
        candidates[candidateCount] = Candidate(candidateCount, _name, 0);
    }

    function vote(uint _candidateId) public {
        require(!voters[msg.sender], "You already voted");
        require(_candidateId > 0 && _candidateId <= candidateCount, "Invalid candidate");

        voters[msg.sender] = true;
        candidates[_candidateId].voteCount++;
    }

    function getCandidate(uint _id) public view returns (
        uint,
        string memory,
        uint
    ) {
        Candidate memory c = candidates[_id];
        return (c.id, c.name, c.voteCount);
    }
}