import kotlin.concurrent.thread

fun main(args: Array<String>) {

    val contaManager = ContaManager.instance
    contaManager.init()

    thread {
        while (true) {
            Thread.sleep(1000)
            contaManager.aplicaJuros()
        }
    }

    println("Bem-vindo ao Sistema Bancário W")
    while (true) {
        print(
            """
            Funções do Cliente:
            1 - Depositar
            2 - Sacar
            3 - Pagamentos
            4 - Transferências
            5 - Extrato
            6 - Informações da conta
                
            Funções Administrativas:
            7 - Abrir Conta
            8 - Bloquear / Desbloquear Conta
            9 - Definir limite saque em conta corrente
            A - Definir limite crédito especial em conta corrente
            B - Histórico de transações de uma conta
                
            |> Escolha uma opção:
            """.trimIndent()
        )
        val opcao = readln().trimIndent().uppercase()

        when (opcao) {
            "1", "2", "3", "4", "5", "6" -> {
                print("Digite o seu CPF: ")
            }
            "7", "8", "9", "A", "B" -> {
                print("Digite o CPF do cliente: ")
            }
            else -> {
                println("Opção inválida\n")
                continue
            }
        }
        val conta = procuraCPF()
        if (conta == null) {
            println("Conta não encontrada\n")
            continue
        }

        when (opcao) {
            "1" -> {
                print("Digite o valor a ser depositado: ")
                val valor = readln().replace(oldChar = ',', newChar = '.').toDouble()
                conta.depositar(valor, TipoConta.CORRENTE)
            }
            "2" -> {
                print("Digite o valor a ser sacado: ")
                val valor = readln().replace(oldChar = ',', newChar = '.').toDouble()
                conta.sacar(valor, TipoConta.CORRENTE)
            }


            "6" -> {
                println(conta.toString())
            }
        }
        Thread.sleep(1000);
    }
}

fun procuraCPF(): PessoaFisica? {
    val cpf = readln()
    return ContaManager.instance.buscarConta(cpf)
}