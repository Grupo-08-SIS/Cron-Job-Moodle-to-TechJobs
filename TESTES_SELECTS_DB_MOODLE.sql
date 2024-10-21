use moodle;

 -- SELECT MUITO DAHORA PARA SABER TEMPO TOTAL DE ESTUDO -> Tentativa 1 --  
SELECT 
                aluno.id AS aluno_id, 
                aluno.email AS aluno_email, 
                FROM_UNIXTIME(sessao.timecreated) AS dia_sessao, 
                (sessao.timemodified - sessao.timecreated) / 60 AS qtd_tempo_sessao -- duração em minutos
            FROM 
                mdl_sessions sessao
            JOIN 
                mdl_user aluno ON aluno.id = sessao.userid;
           
 -- SELECT MUITO DAHORA PARA SABER TEMPO TOTAL DE ESTUDO -> Tentativa 2 --             
SELECT userid,
		FROM_UNIXTIME(timecreated) AS log_time,
        courseid,
        action
FROM 
	mdl_logstore_standard_log
WHERE 
	userId = 2
    AND FROM_UNIXTIME(timecreated) BETWEEN '2024-10-01 00:00:00' AND '2024-10-01 23:59:59'
ORDER BY
	log_time;
    
  -- SELECT MUITO DAHORA PARA SABER TEMPO TOTAL DE ESTUDO --  
    WITH UserActivity AS (

    SELECT
        userid,
        from_unixtime(timecreated) AS event_time,
        courseid,
        action,
        LEAD(from_unixtime(timecreated)) OVER (PARTITION BY userid ORDER BY timecreated) AS next_event_time
    FROM
        mdl_logstore_standard_log
    WHERE
        userid = 2
        AND from_unixtime(timecreated) BETWEEN '2024-10-01 00:00:00' AND '2024-10-01 23:59:59'
)

SELECT
    userid,
    SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, event_time, next_event_time))) AS total_study_time

FROM
    UserActivity

WHERE
    TIMESTAMPDIFF(MINUTE, event_time, next_event_time) <= 30 -- Considera-se que se o intervalo for maior que 30 minutos, o aluno parou de estudar

GROUP BY
    userid;


-- SELECT MUITO DAHORA PARA SABER TEMPO TOTAL DE ESTUDO COM DATA --

WITH UserActivity AS (
    SELECT
        userid,
        from_unixtime(timecreated) AS event_time,
        courseid,
        action,
        LEAD(from_unixtime(timecreated)) OVER (PARTITION BY userid ORDER BY timecreated) AS next_event_time
    FROM
        mdl_logstore_standard_log
    WHERE
        userid = 2
        AND from_unixtime(timecreated) BETWEEN '2024-10-01 00:00:00' AND '2024-10-01 23:59:59'
)

SELECT
    userid,
    DATE_FORMAT(event_time, '%Y-%m-%d') AS dia_log, -- Aqui adiciona o dia do log
    SEC_TO_TIME(SUM(TIMESTAMPDIFF(SECOND, event_time, next_event_time))) AS total_study_time
FROM
    UserActivity
WHERE
    TIMESTAMPDIFF(MINUTE, event_time, next_event_time) <= 30 -- Considera-se que se o intervalo for maior que 30 minutos, o aluno parou de estudar
GROUP BY
    userid, dia_log;  -- Agora o agrupamento também considera o dia do log

SELECT
    curso.id AS id_curso,
    curso.fullname AS nome_curso,
    COUNT(atividade.id) AS total_atividades,  -- Total de atividades no curso
    aluno.id AS id_aluno,
    aluno.firstname AS nome_aluno,
    aluno.lastname AS sobrenome_aluno,
    COUNT(DISTINCT nota.itemid) AS atividades_entregues  -- Quantidade de atividades que o aluno entregou
FROM
    mdl_course curso
JOIN
    mdl_grade_items atividade ON atividade.courseid = curso.id
LEFT JOIN
    mdl_enrol enrol ON enrol.courseid = curso.id  -- Junta os alunos matriculados no curso
LEFT JOIN
    mdl_user_enrolments ue ON ue.enrolid = enrol.id
LEFT JOIN
    mdl_user aluno ON aluno.id = ue.userid  -- Todos os alunos matriculados
LEFT JOIN
    mdl_grade_grades nota ON nota.itemid = atividade.id AND nota.userid = aluno.id -- Tenta associar a nota do aluno
WHERE
    atividade.itemtype = 'mod'
GROUP BY
    curso.id, curso.fullname, aluno.id, aluno.firstname, aluno.lastname
ORDER BY
    curso.id, aluno.id;



SELECT * FROM mdl_grade_grades;

SELECT * FROM  mdl_sessions;

SELECT * FROM mdl_grade_items;

SELECT * FROM mdl_assign_submission;

SELECT * FROM mdl_user_enrolments;