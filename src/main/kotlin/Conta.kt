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

    var bloqueada: Boolean = false

    val numeroConta: String = UUID.randomUUID().toString()

    abstract fun sacar(valor: Double): Boolean

    fun depositar(valor: Double): Boolean {
        bloqueada.let {
            if (it) {
                println("Conta bloqueada")
                return false
            }
        }
        saldo.set(saldo.get() + valor)
        return true
    }

    abstract fun aplicarJuros()

    fun isBloqueada(): Boolean {
        return bloqueada
    }
}

private class ContaCorrente(private val cpf: String, private val nomeTitular: String) : Conta() {

    var limiteSaque: Double = 1200.0
    var limiteCredMax: Double = 2000.0
    var limiteCredito: Double = 2000.0
    val dividaCredEspecial: AtomicReference<Double> = AtomicReference(0.0)

    override fun sacar(valor: Double): Boolean {
        if (isBloqueada()) {
            println("Conta bloqueada")
            return false
        }
        if (valor > limiteSaque) {
            println("Limite de saque excedido")
            return false
        }

        if (valor > saldo.get()) {
            println("Saldo insuficiente")
            println("Você gostaria de usar seu crédito especial? (S/N): ")
            val resposta = readlnOrNull()
            if (resposta?.uppercase() == "S") {
                val valorRestante = valor - saldo.get()
                if (valorRestante > limiteCredito) {
                    println("Limite de crédito não é suficiente")
                    return false
                }
                saldo.set(0.0)
                dividaCredEspecial.set(dividaCredEspecial.get() + valorRestante)
                limiteCredito -= valorRestante
                return true
            } else if (resposta == "N") {
                println("Operação cancelada")
                return false
            }
            return false
        }
        saldo.set(saldo.get() - valor)
        return true
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

    override fun sacar(valor: Double): Boolean {
        if (isBloqueada()) {
            println("Conta bloqueada")
            return false
        }
        if (valor > saldo.get()) {
            println("Saldo insuficiente")
            return false
        }
        saldo.set(saldo.get() - valor)
        return true
    }

    override fun aplicarJuros() {
        saldo.set(saldo.get() * 1.0075)
    }
}

class PessoaFisica(private val cpf: String, private val nomeTitular: String, opcoes: Array<TipoConta>) {

    private var contaCorrente: ContaCorrente? = null
    private var contaPoupanca: ContaPoupanca? = null
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
        if (!isContaValida(tipoConta)) return

        var efetuado = false
        if (tipoConta == TipoConta.CORRENTE) {
            efetuado = contaCorrente!!.depositar(valor)
        } else if (tipoConta == TipoConta.POUPANCA) {
            efetuado = contaPoupanca!!.depositar(valor)
        }
        if (efetuado) transacoes.add(Pair(TipoTransacao.DEPOSITO, valor))
    }

    fun sacar(valor: Double, tipoConta: TipoConta) {
        if (!isContaValida(tipoConta)) return

        var efetuado = false
        if (tipoConta == TipoConta.CORRENTE) {
            efetuado = contaCorrente!!.sacar(valor)
        } else if (tipoConta == TipoConta.POUPANCA) {
            efetuado = contaPoupanca!!.sacar(valor)
        }
        if (efetuado) transacoes.add(Pair(TipoTransacao.SAQUE, valor))
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

    fun transferir(valor: Double, contaDestino: PessoaFisica, tipoConta: TipoConta) {
        lateinit var contaTransacao: TipoConta

        var tentativas = 0
        while (true) {
            tentativas++
            if (tentativas > 3) {
                println("Operação Cancelada")
                return
            }
            val resposta = qualConta() ?: continue
            contaTransacao = resposta
            break
        }

        if (contaTransacao == TipoConta.CORRENTE) {
            if (valor > contaCorrente!!.saldo.get()!!) {
                println("Saldo insuficiente")
                return
            }
            contaCorrente?.sacar(valor)
            contaDestino.depositar(valor, tipoConta)
        } else if (contaTransacao == TipoConta.POUPANCA) {
            if (valor > contaPoupanca!!.saldo.get()!!) {
                println("Saldo insuficiente")
                return
            }
            contaPoupanca?.sacar(valor)
            contaDestino.depositar(valor, tipoConta)
        }
        transacoes.add(Pair(TipoTransacao.TRANSFERENCIA, valor))
    }

    fun extrato() {
        println("# Extrato #")
        for ((index, transacao) in transacoes.reversed().withIndex()) {
            if (index == transacoes.size - 15) {
                break
            }
            println("${index + 1}: ${transacao.first.descricao} - Valor: ${transacao.second}")
        }
        println("\n# Fim do extrato #\n")
    }

    fun historicoTransacoes() {
        println("# Histórico #")
        for ((index, transacao) in transacoes.reversed().withIndex()) {
            println("${index + 1}: ${transacao.first.descricao} - Valor: ${transacao.second}")
        }
        println("\n# Fim do histórico #\n")
    }

    private fun isContaValida(tipoConta: TipoConta): Boolean {
        val contas = verificaConta()

        if (contas.size == 2) return true

        if (contas.isEmpty()) {
            println("Cliente não possui nenhuma conta")
            return false
        }

        if (tipoConta == TipoConta.CORRENTE) {
            if (!contas.contains(TipoConta.CORRENTE)) {
                println("Cliente não possui conta corrente")
                return false
            }
            return true
        }
        if (tipoConta == TipoConta.POUPANCA) {
            if (!contas.contains(TipoConta.POUPANCA)) {
                println("Cliente não possui conta poupança")
                return false
            }
            return true
        }
        return false
    }

    private fun verificaConta(): Array<TipoConta> {
        val contas = mutableListOf<TipoConta>()
        if (contaCorrente != null) contas.add(TipoConta.CORRENTE)
        if (contaPoupanca != null) contas.add(TipoConta.POUPANCA)
        return contas.toTypedArray()
    }

    fun qualConta(): TipoConta? {
        val contas = verificaConta()
        if (contas.size == 2) {
            println("Qual conta deseja usar? (1 - Corrente, 2 - Poupança): ")
            val resposta = readlnOrNull()
            if (resposta == "1") return TipoConta.CORRENTE
            if (resposta == "2") return TipoConta.POUPANCA
            println("Opção inválida\n")
            return null
        }
        if (contas.isEmpty()) {
            println("Cliente não possui nenhuma conta")
            return null
        }
        if (contas.contains(TipoConta.CORRENTE)) return TipoConta.CORRENTE
        if (contas.contains(TipoConta.POUPANCA)) return TipoConta.POUPANCA
        return null
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

    fun atualizarLimiteSaque(limite: Double) {
        if (limite < 0) {
            println("Limite inválido")
            return
        }
        if (contaCorrente == null) {
            println("Cliente não possui conta corrente")
            return
        }
        contaCorrente!!.limiteSaque = limite
    }

    fun atualizarLimiteCredito(limite: Double) {
        if (limite < 0) {
            println("Limite inválido")
            return
        }

        if (contaCorrente == null) {
            println("Cliente não possui conta corrente")
            return
        }

        val diferencaLimite = contaCorrente!!.limiteCredMax - limite
        if (diferencaLimite > contaCorrente!!.limiteCredito) {
            println("Limite selecionado gera situação inválida para o cliente")
            return
        } else {
            contaCorrente!!.limiteCredito -= diferencaLimite
            contaCorrente!!.limiteCredMax = limite
        }
    }

    fun bloquearConta() {
        contaCorrente?.let {
            it.bloqueada = !it.bloqueada
            if (it.bloqueada) println("Conta corrente bloqueada")
            else println("Conta corrente desbloqueada")
        }
        contaPoupanca?.let {
            it.bloqueada = !it.bloqueada
            if (it.bloqueada) println("Conta poupança bloqueada")
            else println("Conta poupança desbloqueada")
        }
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