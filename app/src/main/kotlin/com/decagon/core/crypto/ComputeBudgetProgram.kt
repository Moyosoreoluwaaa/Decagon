package com.decagon.core.crypto

import org.sol4k.PublicKey
import org.sol4k.instruction.Instruction
import org.sol4k.AccountMeta

object ComputeBudgetProgram {
    private val PROGRAM_ID = PublicKey("ComputeBudget111111111111111111111111111111")

    fun setComputeUnitPrice(microLamports: Long): Instruction {
        val data = ByteArray(9).apply {
            this[0] = 3
            for (i in 0..7) {
                this[i + 1] = ((microLamports shr (i * 8)) and 0xFF).toByte()
            }
        }

        return object : Instruction {
            override val data: ByteArray = data
            override val programId: PublicKey = PROGRAM_ID
            override val keys: List<AccountMeta> = emptyList()
        }
    }

    fun setComputeUnitLimit(units: Int): Instruction {
        val data = ByteArray(5).apply {
            this[0] = 2
            for (i in 0..3) {
                this[i + 1] = ((units shr (i * 8)) and 0xFF).toByte()
            }
        }

        return object : Instruction {
            override val data: ByteArray = data
            override val programId: PublicKey = PROGRAM_ID
            override val keys: List<AccountMeta> = emptyList()
        }
    }
}