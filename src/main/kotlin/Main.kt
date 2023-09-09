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

        val conta = if (opcao != "7") procuraCPF() else PessoaFisica("0", "0", arrayOf())
        if (conta == null) {
            println("Conta não encontrada\n")
            continue
        }

        when (opcao) {
            "1" -> {
                print("Digite o valor a ser depositado: ")
                val valor = lerValor()
                qualConta(conta)?.let { conta.depositar(valor, it) }
            }

            "2" -> {
                print("Digite o valor a ser sacado: ")
                val valor = lerValor()
                qualConta(conta)?.let { conta.sacar(valor, it) }
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
                val contaBuscada = contaManager.buscaIndice(contaDestino)
                if (contaBuscada != null) {
                    conta.transferir(valor, contaBuscada.first, contaBuscada.second)
                } else {
                    println("Conta não encontrada")
                }
            }

            "5" -> {
                conta.extrato()
            }

            "6" -> {
                println(conta.toString())
            }

            "7" -> {
                val cpf = readln()
                print("Digite o nome do cliente: ")
                val nome = readln()
                print("Digite o tipo de conta (1 - Corrente, 2 - Poupança, 3 - Corrente e Poupança): ")
                val opcoes = when (readln().toInt()) {
                    1 -> arrayOf(TipoConta.CORRENTE)
                    2 -> arrayOf(TipoConta.POUPANCA)
                    3 -> arrayOf(TipoConta.CORRENTE, TipoConta.POUPANCA)
                    else -> {
                        println("Opção inválida")
                        continue
                    }
                }
                contaManager.criarConta(cpf, nome, opcoes)
            }

            "8" -> {
                val cpf = readln()
                contaManager.bloquearConta(cpf)
            }

            "9" -> {
                print("Digite o valor do limite: ")
                val limite = readln().toDouble()
                conta.atualizarLimiteSaque(limite)
            }

            "A" -> {
                print("Digite o valor do limite: ")
                val limite = readln().toDouble()
                conta.atualizarLimiteCredito(limite)
            }

            "B" -> {
                conta.historicoTransacoes()
            }
        }
        Thread.sleep(1000)
    }
}

private fun lerValor(): Double {
    return readln().replace(oldChar = ',', newChar = '.').toDouble()
}

private fun procuraCPF(): PessoaFisica? {
    val cpf = readln()
    return ContaManager.instance.buscarConta(cpf)
}

private fun qualConta(conta: PessoaFisica): TipoConta? {
    return conta.qualConta()
}