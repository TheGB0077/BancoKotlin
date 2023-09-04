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

    val conta = contaManager.buscarConta("12345678910");

    conta?.depositar(100.0, TipoConta.POUPANCA)
    conta?.sacar(50.0, TipoConta.CORRENTE)

    while (true) {
        Thread.sleep(1000);
        println(conta.toString())
    }
}