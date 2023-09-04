import java.util.UUID

enum class TipoConta {
    CORRENTE, POUPANCA
}

abstract class Conta {
    var saldo: Double = 0.0

    private var bloqueada: Boolean = false

    val numeroConta: String = UUID.randomUUID().toString()

    abstract fun sacar(valor: Double)

    abstract fun aplicarJuros()

    fun depositar(valor: Double) {
        bloqueada.let {
            if (it) {
                println("Conta bloqueada")
                return
            }
        }
        saldo += valor
    }

    fun isBloqueada(): Boolean {
        return bloqueada
    }

}

private class ContaCorrente(private val cpf: String, private val nomeTitular: String) : Conta() {

    private var limiteSaque:Double = 1200.0
    private var limiteCredito:Double = 2000.0
    private var dividaCredEspecial:Double = 0.0

    override fun sacar(valor: Double){

        isBloqueada().let {
            if (it) {
                println("Conta bloqueada")
                return
            }
        }

        if (valor > limiteSaque) {
            println("Limite de saque excedido")
            return
        }

        if (valor > saldo) {
            println("Saldo insuficiente")
            println("Você gostaria de usar seu crédito especial? (S/N):")
            val resposta = readlnOrNull()
            if (resposta?.uppercase() == "S") {
                val valorRestante = valor - saldo
                if (valorRestante > limiteCredito) {
                    println("Limite de crédito não é suficiente")
                    return
                }
                saldo = 0.0
                dividaCredEspecial += valorRestante
                return
            } else if (resposta == "N") {
                println("Operação cancelada")
                return
            }
            return
        }
        saldo -= valor
    }

    override fun aplicarJuros() {
        if (dividaCredEspecial > 0) {
            dividaCredEspecial * 1.08
        }
    }
}

private class ContaPoupanca(private val cpf: String, private val nomeTitular: String) : Conta() {

    override fun sacar(valor: Double) {

        isBloqueada().let {
            if (it) {
                println("Conta bloqueada")
                return
            }
        }

        if (valor > saldo) {
            println("Saldo insuficiente")
            return
        }
        saldo -= valor
    }

    override fun aplicarJuros() {
        saldo *= 1.0075
    }
}

class PessoaFisica(private val cpf: String, private val nomeTitular: String, opcoes: Array<TipoConta>) {

    private var contaCorrente:ContaCorrente? = null
    private var contaPoupanca:ContaPoupanca? = null

    init {
        if (opcoes.contains(TipoConta.CORRENTE)) {
            contaCorrente = ContaCorrente(cpf, nomeTitular)
        }
        if (opcoes.contains(TipoConta.POUPANCA)) {
            contaPoupanca = ContaPoupanca(cpf, nomeTitular)
        }
    }


    fun depositar(valor: Double, tipoConta: TipoConta) {
        if (isContaInvalida(tipoConta)) return

        if (tipoConta == TipoConta.CORRENTE) {
            contaCorrente?.depositar(valor)
        } else if (tipoConta == TipoConta.POUPANCA) {
            contaPoupanca?.depositar(valor)
        }
    }

    fun sacar(valor: Double, tipoConta: TipoConta) {
        if (isContaInvalida(tipoConta)) return

        if (tipoConta == TipoConta.CORRENTE) {
            contaCorrente?.sacar(valor)
        } else if (tipoConta == TipoConta.POUPANCA) {
            contaPoupanca?.sacar(valor)
        }
    }

    private fun isContaInvalida(tipoConta: TipoConta): Boolean {
        if (contaCorrente == null && tipoConta == TipoConta.CORRENTE) {
            println("Cliente não possui conta corrente")
            return true
        }
        if (contaPoupanca == null && tipoConta == TipoConta.POUPANCA) {
            println("Cliente não possui conta poupança")
            return true
        }
        return false
    }

    fun aplicarJuros() {
        if(contaCorrente != null) contaCorrente!!.aplicarJuros()
        if(contaPoupanca != null) contaPoupanca!!.aplicarJuros()
    }


    override fun toString(): String {
        return """ 
            |CPF: $cpf
            |Nome: $nomeTitular
            |Conta Corrente: ${contaCorrente?.numeroConta} | Saldo: ${contaCorrente?.saldo}
            |Conta Poupança: ${contaPoupanca?.numeroConta} | Saldo: ${contaPoupanca?.saldo}
        """.trimIndent()
    }
}