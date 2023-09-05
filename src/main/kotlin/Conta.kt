import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

enum class TipoConta {
    CORRENTE, POUPANCA
}

abstract class Conta {
    val saldo: AtomicReference<Double> = AtomicReference(0.0)

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
        saldo.set(saldo.get() + valor)
    }

    fun isBloqueada(): Boolean {
        return bloqueada
    }

}

private class ContaCorrente(private val cpf: String, private val nomeTitular: String) : Conta() {

    private var limiteSaque:Double = 1200.0
    private var limiteCredito:Double = 2000.0
    val dividaCredEspecial:AtomicReference<Double> = AtomicReference(0.0)

    override fun sacar(valor: Double){
        if (isBloqueada()) {
            println("Conta bloqueada")
            return
        }
        if (valor > limiteSaque) {
            println("Limite de saque excedido")
            return
        }

        if (valor > saldo.get()) {
            println("Saldo insuficiente")
            println("Você gostaria de usar seu crédito especial? (S/N):")
            val resposta = readlnOrNull()
            if (resposta?.uppercase() == "S") {
                val valorRestante = valor - saldo.get()
                if (valorRestante > limiteCredito) {
                    println("Limite de crédito não é suficiente")
                    return
                }
                saldo.set(0.0)
                dividaCredEspecial.set(dividaCredEspecial.get() + valorRestante)

                return
            } else if (resposta == "N") {
                println("Operação cancelada")
                return
            }
            return
        }
        saldo.set(saldo.get() - valor)
    }

    override fun aplicarJuros() {
        if (dividaCredEspecial.get() > 0) {
            dividaCredEspecial.set(dividaCredEspecial.get() * 1.08)
        }
    }
}

private class ContaPoupanca(private val cpf: String, private val nomeTitular: String) : Conta() {

    override fun sacar(valor: Double) {
        if (isBloqueada()) {
            println("Conta bloqueada")
            return
        }
        if (valor > saldo.get()) {
            println("Saldo insuficiente")
            return
        }
        saldo.set(saldo.get() - valor)
    }

    override fun aplicarJuros() {
        saldo.set(saldo.get() * 1.0075)
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
        contaCorrente?.aplicarJuros()
        contaPoupanca?.aplicarJuros()
    }

    override fun toString(): String {
        return """ 
            |CPF: $cpf
            |Nome: $nomeTitular
            |Conta Corrente: ${contaCorrente?.numeroConta} | Saldo: ${contaCorrente?.saldo} | Dívida: ${contaCorrente?.dividaCredEspecial}
            |Conta Poupança: ${contaPoupanca?.numeroConta} | Saldo: ${contaPoupanca?.saldo}
        """.trimIndent()
    }
}