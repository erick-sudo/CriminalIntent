package com.kcire.criminalintent

import androidx.lifecycle.ViewModel
import java.util.*

class CrimeListViewModel : ViewModel(){

    private val crimeTitles = """
        Handling Stolen Goods
        Importing and Exporting Border Controlled Precursors,
        Importing and Exporting Commercial Quantities of Border Controlled Drugs or Border Controlled Plants
        Improper Use of Motor Vehicle
        Incest
        Inclusion of False or Misleading Information in Records
        Incurring of Certain Debts; Fraudulent Conduct
        Indecent Act In the Presence of a Child Under the Age of 16
        Indecent Assault
        Indemnifying Surety
        Inducement to Be Appointed Liquidator etc. of Company
        Infanticide
        Insider Trading
        Intentionally Causing Serious Injury
        Intentionally or Recklessly Causing a Bushfire
        Intentionally Visually Capture Another Person’s Genital or Anal Region
        Interfere With Corpse of a Human Being
        Introduction of a Drug of Dependence Into the Body of Another Person
        Involving a Child in the Production of Child Abuse Material
        Kidnapping
    """.trimIndent().split("\n")

    val crimes = mutableListOf<Crime>()

    init {
        for(i in 0 until 100) {
            val crime = Crime(
                id = UUID.randomUUID(),
                title =  crimeTitles.random(), //"Crime #$i",
                date = Date(),
                isSolved = i%2==0
            )

            crimes += crime
        }
    }
}