import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import TipoConta as TC

fun main(args: Array<String>) = runBlocking {

    val contaManager = ContaManager.instance

    launch {
        contaManager
    }

    contaManager.init()
    println(contaManager.buscarConta("12345678910").toString())

}