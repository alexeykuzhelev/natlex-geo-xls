##  «Натлекс» - Тестовое задание для backend-разработчика

 1) Добавьте REST CRUD API для разделов и геологических классов. Каждый раздел имеет структуру:
{  
	"name": "Section 1", 
	"geologicalClasses": [ 
		{ "name": "Geo Class 11", "code": "GC11" },  
		{ "name": "Geo Class 12", "code": "GC12" }, ...
	]  
}
2) Добавьте API GET /sections /by-code? code = ..., который возвращает список всех разделов, имеющих геологические классы с указанным кодом.
3) Добавить API для импорта и экспорта файлов XLS. Каждый файл XLS содержит заголовки и список разделов с геологическими классами. 

Пример:

Section name | Class 1 name | Class 1 code | Class 2 name | Class 2 code
------------ | ------------ | ------------ | ------------ | ------------ 
Section 1    | Geo Class 1  | GC1          | Geo Class 2  | GC2
Section 2    | Geo Class 2  | GC2
Section 3    | Geo Class 3  | GC3

Файлы должны обрабатываться асинхронно, результаты должны храниться в DB.
- API POST / import (file) возвращает ID (идентификатор) Async Job (асинхронной операции) и запускает импорт.
- API GET / import / {id} возвращает результат импорта по Job ID ("DONE", "IN PROGRESS", "ERROR")
- API GET / export возвращает ID Async Job и запускает экспорт.
- API GET / export / {id} возвращает результат парсинга файла по Job ID (идентификатору операции)  ("DONE", "IN PROGRESS", "ERROR")
- API GET / export / {id} / file возвращает файл по Job ID (генерирует исключение, если экспорт находится в процессе)
Требования:
● Стек технологий: Spring, Hibernate, Spring Data, Spring Boot, Gradle / Maven.
● Все данные (кроме файлов) должны быть в формате JSON.
● При экспорте и импорте используйте Apache POI для анализа.
● Должна поддерживаться Базовая авторизация.

----------------------------------------------------------------------------------------------------------------------------------------------

    Подключение к СУБД
Параметры подключения находятся в /src/main/resources/application.properties
По дефолту для PostgreSql:
- username=postgres
- password=aleks
- база данных используется стандартная: postgres


                                        Тестирование (примеры запросов через программу Postman):
			
	Данные авторизации
Создано два пользователя:
username: user     - 1-й пользователь (имеет роль "USER")
password: userpw
username: admin     - 2-й пользователь (имеет роли "USER", "ADMIN")
password: adminpw    	

Для импорта-экспорта XML заданы следующие пути:
C:\Download\jobs\import
C:\Download\jobs\export

Файл sections.xls с заголовками и списами разделов, содержащих геологические классы, лежит в папке проекта fileXLS.

1) POST-запрос - добавить REST CRUD API для разделов и геологических классов.
http://localhost:8080/api/sections
{  
	"name": "Section 1", 
	"geologicalClasses": [ 
		{ "name": "Geo Class 11", "code": "GC11" },  
		{ "name": "Geo Class 12", "code": "GC12" }
	]  
}  

2) POST-запрос для импорта данных XML из файла в базу:
http://localhost:8080/api/jobs/import

3) GET-запрос возвращает результат импорта XML в базу по Job ID: 
http://localhost:8080/api/jobs/import/1

4) GET-запрос для экспорта в XML из базы:
http://localhost:8080/api/jobs/export

5) GET-запрос для получения списка Section (разделов) из базы:
http://localhost:8080/api/sections/list-sections
http://localhost:8080/api/sections/list-sections?size=2&page=0    - с указанием номера и размера страницы

6) GET-запрос для получения списка Job (операций) из базы:
http://localhost:8080/api/jobs/list-jobs

7) GET-запрос для получения списка всех разделов, имеющих геологические классы с указанным кодом:
http://localhost:8080/api/sections/by-code?code=GC11

8) GET-запрос возвращает результат парсинга файла по Job ID: 
http://localhost:8080/api/jobs/export/3

9) GET-запрос возвращает файл по Job ID: 
http://localhost:8080/api/jobs/export/3/file







