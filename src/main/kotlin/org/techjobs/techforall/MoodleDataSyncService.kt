package org.techjobs.techforall

import org.techjobs.techforall.dto.CursoMoodleDto
import org.techjobs.techforall.dto.PontuacaoMoodleDto
import org.techjobs.techforall.dto.TempoSessaoMoodleDto
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MoodleDataSyncService(
    val moodleService: MoodleService,
    val techJobsDbService: TechJobsDbService
) {

    fun sintonizarDados(){
       sincronizarDadosDoMoodle();
    }
    fun sincronizarDadosDoMoodle() {

        val cursos = moodleService.buscarCursos();

        val pontuacoes = moodleService.buscarPontuacoes();

        val temposSessao = moodleService.buscarTemposSessao();

        inserirDadosDoMoodle(cursos, pontuacoes, temposSessao);
    }

    fun inserirDadosDoMoodle(cursos: List<CursoMoodleDto>, pontuacoes: List<PontuacaoMoodleDto>, temposSessao: List<TempoSessaoMoodleDto>) {

        techJobsDbService.cadastrarCursos(cursos);
        techJobsDbService.cadastrarPontuacoes(pontuacoes);
        techJobsDbService.cadastrarTemposSessao(temposSessao);
    }
}