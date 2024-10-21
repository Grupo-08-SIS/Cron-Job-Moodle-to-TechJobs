package org.techjobs.techforall

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.techjobs.techforall.config.TechJobsDbConfig
import org.techjobs.techforall.dto.CursoMoodleDto
import org.techjobs.techforall.dto.PontuacaoMoodleDto
import org.techjobs.techforall.dto.TempoSessaoMoodleDto

@Service
class TechJobsDbService(
    @Qualifier("techJobsJdbcTemplate")
    private val jdbcTemplateTechJobs: JdbcTemplate
) {

    fun cadastrarCursos(cursos: List<CursoMoodleDto>) {

        cursos.forEach { curso ->
            val sqlInserirCurso = """
            INSERT INTO curso (id, nome, categoria, total_atividades, total_atividades_do_aluno)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            nome = VALUES(nome),
            categoria = VALUES(categoria),
            total_atividades = VALUES(total_atividades),
            total_atividades_do_aluno = VALUES(total_atividades_do_aluno)
        """
            jdbcTemplateTechJobs.update(
                sqlInserirCurso,
                curso.id, curso.nome, curso.categoria, curso.totalAtividades, curso.totalAtividadesDoAluno
            )
        }
    }

    fun cadastrarPontuacoes(pontuacoes: List<PontuacaoMoodleDto>) {

        pontuacoes.forEach { pontuacao ->
            val sqlInserirPontuacao = """
            INSERT INTO pontuacao (aluno_id, aluno_email, curso_id, curso_nome, data_entrega, nome_atividade, nota_atividade, nota_aluno)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            data_entrega = VALUES(data_entrega),
            nota_aluno = VALUES(nota_aluno)
        """
            jdbcTemplateTechJobs.update(
                sqlInserirPontuacao,
                pontuacao.alunoId, pontuacao.alunoEmail, pontuacao.cursoId, pontuacao.cursoNome, pontuacao.dataEntrega, pontuacao.nomeAtividade, pontuacao.notaAtividade, pontuacao.notaAluno
            )
        }
    }

    fun cadastrarTemposSessao(temposSessao: List<TempoSessaoMoodleDto>) {

        temposSessao.forEach { tempo ->
            val sqlInserirSessao = """
            INSERT INTO tempo_sessao (aluno_id, aluno_email, dia_sessao, qtd_tempo_sessao)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            qtd_tempo_sessao = VALUES(qtd_tempo_sessao)
        """
            jdbcTemplateTechJobs.update(
                sqlInserirSessao,
                tempo.alunoId, tempo.alunoEmail, tempo.diaSessao, tempo.qtdTempoSessao
            )
        }
    }
}