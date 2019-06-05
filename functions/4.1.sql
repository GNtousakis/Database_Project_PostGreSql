CREATE VIEW view4_1 AS 
	SELECT cr.course_code,c.course_title,(concat(pr1.name::text,'    ',pr1.surname::text,'  ,  ',pr2.name::text,'    ',pr2.surname::text)) AS Teachers
	FROM (SELECT course_code,amka_prof1,amka_prof2 FROM "CourseRun" WHERE serial_number=trexon_examino()) AS cr
			JOIN (SELECT course_code,course_title FROM "Course") AS c USING (course_code)
			INNER JOIN (SELECT amka,name,surname FROM "Professor") AS pr1 ON (amka_prof1=pr1.amka)
			LEFT JOIN (SELECT amka,name,surname FROM "Professor") AS pr2 ON (amka_prof2=pr2.amka)