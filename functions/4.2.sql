CREATE VIEW view4_2 AS
	SELECT s.amka,s.name,s.surname,et_vathmos(s.amka,etos_foitisis(s.amka)-1),etos_foitisis(s.amka)
	FROM (SELECT amka,name,surname FROM "Student" WHERE amka NOT IN (SELECT amka FROM "Diploma")) AS s
	
	
CREATE OR REPLACE FUNCTION et_vathmos(amkaf integer,etosm integer)
	 RETURNS numeric AS
$$	 
DECLARE 
	mathip numeric:=0;
	mathpr numeric:=0;
	athrismaip numeric:=0;
	athrismapr numeric:=0;	 
	
	nmath numeric:=0; --plithos ipoxreotikon proigoumenou etous
	nmathp numeric:=0; --plithos ipoxreotikon proigoumenou etous pou perastikan apo foititi
BEGIN	
	SELECT SUM(r.final_grade* c.weight),SUM(c.weight),COUNT(c.course_code) INTO mathip,athrismaip,nmathp -- vriskoume to athrisma ton ipoxreotikon
	FROM "Register" r JOIN "Course" c USING (course_code)
	WHERE amka=amkaf AND register_status='pass' AND typical_year=etosm  AND course_code IN (SELECT c.course_code
																		FROM "Course" c
																		WHERE c.obligatory='true');
	SELECT COUNT(course_code) INTO nmath
	FROM  "Course"  
	WHERE  typical_year=etosm  AND course_code IN (SELECT c.course_code
													FROM "Course" c
													WHERE c.obligatory='true');
	IF (nmathp<nmath) THEN --elegxoume oti o foitits exei perasei ola ta ipoxreotika tou proigoumou etos
	RAISE EXCEPTION 'No enough lessons passed';
	END IF;
	
	SELECT SUM(pinakas.grade),SUM(pinakas.monades)  INTO mathpr,athrismapr -- vriskoume to athrisma ton ipoxreotikon kat epilogi
	FROM (SELECT (r.final_grade * c.weight) as grade, c.weight as monades
			FROM "Register" r JOIN "Course" c USING (course_code)
			WHERE (amka=amkaf) AND register_status='pass' AND typical_year=etosm AND course_code NOT IN (SELECT c.course_code
																									FROM "Course" c
																									WHERE c.obligatory='true')) AS pinakas;	
	
	RETURN round(((mathip+mathpr)/(athrismaip+athrismapr)),1);
END;
$$
LANGUAGE 'plpgsql' IMMUTABLE;		

CREATE OR REPLACE FUNCTION public.etos_foitisis(
	amkaf integer)
    RETURNS integer
    LANGUAGE 'plpgsql'

    COST 100
    IMMUTABLE 
AS $BODY$

DECLARE
	yearf integer;
	yeart integer;
BEGIN 
	SELECT EXTRACT(YEAR FROM  s.entry_date) INTO yearf
	FROM "Student" s
	WHERE amka=amkaf;
	
	SELECT EXTRACT(YEAR FROM  start_date) INTO yeart
	FROM "Semester"
	WHERE semester_status='present';
	
	RETURN(yeart-yearf+1);
END;

$BODY$;

ALTER FUNCTION public.etos_foitisis(integer)
    OWNER TO postgres;