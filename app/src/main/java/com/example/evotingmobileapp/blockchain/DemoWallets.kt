package com.example.evotingmobileapp.blockchain

data class DemoVoterProfile(
    val label: String,
    val address: String
)

object DemoWallets {
    val voters: List<DemoVoterProfile> = listOf(
        DemoVoterProfile(
            label = "Voter 1",
            address = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
        ),
        DemoVoterProfile(
            label = "Voter 2",
            address = "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC"
        ),
        DemoVoterProfile(
            label = "Voter 3",
            address = "0x90F79bf6EB2c4f870365E785982E1f101E93b906"
        ),
        DemoVoterProfile(
            label = "Voter 4",
            address = "0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65"
        ),
        DemoVoterProfile(
            label = "Voter 5",
            address = "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc"
        )
    )

    val defaultVoterAddress: String = voters.first().address

    val allVoterAddresses: List<String> = voters.map { it.address }

    val allVoterAddressesText: String = allVoterAddresses.joinToString(separator = "\n")
}