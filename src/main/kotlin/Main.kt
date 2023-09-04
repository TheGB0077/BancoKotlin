import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun main(args: Array<String>) = runBlocking {

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
        delay(1.toDuration(DurationUnit.SECONDS))
        println(conta.toString())
    }
}