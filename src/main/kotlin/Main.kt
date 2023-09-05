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
                val valor = lerValor()
                conta.depositar(valor, TipoConta.CORRENTE)
            }
            "2" -> {
                print("Digite o valor a ser sacado: ")
                val valor = lerValor()
                conta.sacar(valor, TipoConta.CORRENTE)
            }

            "3" -> {
                val divida = conta.getDividaCredEspecial() ?: 0.0
                if (divida > 0.0) {
                    print("Deseja pagar a dívida do crédito especial? (S/N): ")
                    val opcaoDivida = readln().uppercase()
                    if (opcaoDivida == "S") {
                        print("Digite o valor a ser pago: (Máximo $divida)")
                        val valor = lerValor()
                        conta.pagarDivida(valor)
                        continue
                    }
                }
                print("Digite o valor a ser pago: ")
                val valor = lerValor()
                conta.pagamento(valor)
            }

            "4" -> {
                print("Digite o número conta do destinatário: ")
                val contaDestino = readln()
                print("Digite o valor a ser transferido: ")
                val valor = lerValor()
                contaManager.buscaIndice(contaDestino)?.let {
                    conta.transferir(valor, it.first, it.second)
                    return
                }
                println("Conta não encontrada")
            }

            "5" -> {
                conta.extrato()
            }

            "6" -> {
                println(conta.toString())
            }
        }
        Thread.sleep(1000);
    }
}

private fun lerValor(): Double {
    return readln().replace(oldChar = ',', newChar = '.').toDouble()
}

fun procuraCPF(): PessoaFisica? {
    val cpf = readln()
    return ContaManager.instance.buscarConta(cpf)
}