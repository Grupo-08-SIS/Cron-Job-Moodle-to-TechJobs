package org.techjobs.techforall

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class MoodleDataSyncConsumer(
    private val moodleDataSyncService: MoodleDataSyncService
) {

    @RabbitListener(queues = ["moodleSyncQueue"])
    fun receberMensagemDeSincronizacao(mensagem: String) {
        println("Mensagem recebida: $mensagem")
        moodleDataSyncService.sintonizarDados()
    }
}
