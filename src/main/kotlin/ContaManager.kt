class ContaManager private constructor() {

    companion object {
        val instance = ContaManager()
    }

    private val contas = mutableMapOf<String, PessoaFisica>()
    private val indice = mutableMapOf<String, Pair<PessoaFisica, TipoConta>>()

    fun criarConta(cpf: String, nomeTitular: String, opcoes: Array<TipoConta>) {
        contas[cpf]?.let {
            println("Conta já existe")
            return
        }
        val pessoa = PessoaFisica(cpf, nomeTitular, opcoes)
        if (opcoes.contains(TipoConta.CORRENTE)) indice[pessoa.getContaCorrente()!!] = Pair(pessoa, TipoConta.CORRENTE)
        if (opcoes.contains(TipoConta.POUPANCA)) indice[pessoa.getContaPoupanca()!!] = Pair(pessoa, TipoConta.POUPANCA)
        contas[cpf] = pessoa
    }

    fun buscarConta(cpf: String): PessoaFisica? {
        return contas[cpf]
    }

    fun buscaIndice(conta: String): Pair<PessoaFisica, TipoConta>? {
        return indice[conta]
    }

    fun bloquearConta(cpf: String) {
        buscarConta(cpf)?.bloquearConta()
    }

    fun aplicaJuros() {
        contas.values.forEach {
            it.aplicarJuros()
        }
    }

    fun init() {
        val contaManager = instance

        contaManager.criarConta("12345678910", "João da Silva", arrayOf(TipoConta.CORRENTE, TipoConta.POUPANCA))
        contaManager.criarConta("12345678911", "Maria da Silva", arrayOf(TipoConta.CORRENTE))
        contaManager.criarConta("12345678912", "José da Silva", arrayOf(TipoConta.POUPANCA))
        contaManager.criarConta("12345678913", "Joana da Silva", arrayOf(TipoConta.CORRENTE, TipoConta.POUPANCA))
        contaManager.criarConta("12345678914", "Pedro da Silva", arrayOf(TipoConta.CORRENTE))
        contaManager.criarConta("12345678915", "Paula da Silva", arrayOf(TipoConta.POUPANCA))
        contaManager.criarConta("12345678916", "Joaquim da Silva", arrayOf(TipoConta.CORRENTE, TipoConta.POUPANCA))
        contaManager.criarConta("12345678917", "Ana da Silva", arrayOf(TipoConta.CORRENTE))
        contaManager.criarConta("12345678918", "Antônio da Silva", arrayOf(TipoConta.POUPANCA))
        contaManager.criarConta("12345678919", "Mariana da Silva", arrayOf(TipoConta.CORRENTE, TipoConta.POUPANCA))
        contaManager.criarConta("12345678920", "Carlos da Silva", arrayOf(TipoConta.CORRENTE))
        contaManager.criarConta("12345678921", "Carla da Silva", arrayOf(TipoConta.POUPANCA))
        contaManager.criarConta("12345678922", "Marcos da Silva", arrayOf(TipoConta.CORRENTE, TipoConta.POUPANCA))
        contaManager.criarConta("12345678923", "Márcia da Silva", arrayOf(TipoConta.CORRENTE))
    }
}


