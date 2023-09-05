import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

enum class TipoConta {
    CORRENTE, POUPANCA
}

enum class TipoTransacao(val descricao: String) {
    DEPOSITO("Deposito"), SAQUE("Saque"), PAGAMENTO("Pagamento"), TRANSFERENCIA("Transferência")
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
                limiteCredito -= valorRestante
                return
            } else if (resposta == "N") {
                println("Operação cancelada")
                return
            }
            return
        }
        saldo.set(saldo.get() - valor)
    }

    fun processarDivida(valor: Double) {
        if (isBloqueada()) {
            println("Conta bloqueada")
            return
        }
        saldo.set(saldo.get()!! - valor)
        dividaCredEspecial.set(dividaCredEspecial.get()!! - valor)
        limiteCredito += valor
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
    private val transacoes = mutableListOf<Pair<TipoTransacao, Double>>()

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

    fun pagamento(valor: Double) {
        if (valor > contaCorrente?.saldo?.get()!!) {
            println("Saldo insuficiente")
            return
        }
        contaCorrente!!.depositar(-valor)
        transacoes.add(Pair(TipoTransacao.PAGAMENTO, valor))
    }

    fun pagarDivida(valor: Double) {
        if (contaCorrente!!.dividaCredEspecial.get()!! < valor) {
            println("Valor maior que a dívida")
            return
        }

        if (valor > contaCorrente!!.saldo.get()!!) {
            println("Saldo insuficiente")
            return
        }

        contaCorrente!!.processarDivida(valor)
        transacoes.add(Pair(TipoTransacao.PAGAMENTO, valor))
    }

    //TODO: Adicionar escolha de qual usar para transferir
    fun transferir(valor: Double, contaDestino: PessoaFisica, tipoConta: TipoConta) {
        if (tipoConta == TipoConta.CORRENTE) {
            if (valor > contaCorrente!!.saldo.get()!!) {
                println("Saldo insuficiente")
                return
            }
            contaCorrente?.sacar(valor)
            contaDestino.depositar(valor, TipoConta.CORRENTE)
        } else if (tipoConta == TipoConta.POUPANCA) {
            if (valor > contaPoupanca!!.saldo.get()!!) {
                println("Saldo insuficiente")
                return
            }
            contaPoupanca?.sacar(valor)
            contaDestino.depositar(valor, TipoConta.POUPANCA)
        }
        transacoes.add(Pair(TipoTransacao.TRANSFERENCIA, valor))
    }

    fun extrato() {
        println("# Extrato #")
        for ((index, transacao) in transacoes.reversed().withIndex()) {
            if (index == transacoes.size - 15) break
            println("${transacao.first.descricao} - Valor: ${transacao.second}")
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

    fun getContaCorrente(): String? {
        return contaCorrente?.numeroConta
    }

    fun getContaPoupanca(): String? {
        return contaPoupanca?.numeroConta
    }

    fun getDividaCredEspecial(): Double? {
        return contaCorrente?.dividaCredEspecial?.get()
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