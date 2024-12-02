package org.techjobs.techforall

import org.techjobs.techforall.dto.CursoMoodleDto
import org.techjobs.techforall.dto.PontuacaoMoodleDto
import org.techjobs.techforall.dto.TempoSessaoMoodleDto
import org.springframework.stereotype.Component
import org.techjobs.techforall.dto.CursoAlunoDto

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

        val cursosAlunos = moodleService.buscarCursosAlunos()

        val pontuacoes = moodleService.buscarPontuacoes();

        val temposSessao = moodleService.buscarTemposSessao();

        inserirDadosDoMoodle(cursos, cursosAlunos, pontuacoes, temposSessao);
    }

    fun inserirDadosDoMoodle(cursos: List<CursoMoodleDto>, cursosAlunos:List<CursoAlunoDto>, pontuacoes: List<PontuacaoMoodleDto>, temposSessao: List<TempoSessaoMoodleDto>) {

        techJobsDbService.cadastrarCursos(cursos);
        techJobsDbService.cadastrarCursosAlunos(cursosAlunos)
        techJobsDbService.cadastrarPontuacoes(pontuacoes);
        techJobsDbService.cadastrarTemposSessao(temposSessao);
    }
}