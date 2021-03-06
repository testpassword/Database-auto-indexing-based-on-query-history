# Автоиндексация базы данных на основе истории запросов

## Описание

### Решаемая проблема

Старые базы данных могут содержать очень сложные и непонятные запросы, скорость выполнения которых необходимо увеличить. 
Ручная оптимизации займёт много времени и напрямую зависит от опыта работы сотрудника с данной даталогической моделью.

### Цели

- Минимизировать время на оптимизацию БД пользователем 
- Максимально увеличить скорость выполнения запросов к БД

### Задачи

- Проектирование и создание тестовой базы данных 
- СОЗДАНИЕ ПРИЛОЖЕНИЯ АВТОИНДЕКСИРОВАНИЯ БАЗЫ ДАННЫХ 
- Тестирование приложения 
- Анализ полученных результатов

### Обзор аналогов

8 из 10 самых популярных (по версии [db-engines.com](https://db-engines.com/en/ranking)) СУБД подобной функциональности не имеют.

| Рейтинг | СУБД                  | Модель данных    |
|---------|-----------------------|------------------|
| _1_     | _Oracle_              | _Реляционная_    |
| 2       | MySQL                 | Реляционная      |
| _3_     | _Microsoft SQLServer_ | _Реляционная_    |
| 4       | PostgreSQL            | Реляционная      |
| 5       | MongoDB               | Документная      |
| 6       | Redis                 | Ключ-значение    |
| 7       | IBM Db2               | Реляционная      |
| 8       | Elasticsearch         | Поисковой движок |
| 9       | Microsoft Access      | Реляционная      |
| 10      | SQLite                | Реляционная      |

Только `Oracle Database` и `Microsoft SQLServer` имеют функцию автоиндексирования, но это дорогие коммерческие продукты, 
а сам процесс представляет собой «черный ящик».

Нельзя официально приобрести в России.  

**Преимущества моего решения:** 

1. Поддержка любой БД с JDBC-драйвером 
2. Контролируемые процесс (формирование отчётов)
3. Открытый исходный код

### Технологический стек

#### Kotlin
Ключевым требованием, предъявляемым к разрабатываемой программе, сформулированным в задании на выпускную квалификационную 
работу, является поддержка широкого спектра оборудования, а именно операционных систем Windows, Linux, macOS и процессорных 
архитектур arm, x86-64. Необходимо было выбрать язык программирования, который бы позволял запускать программу на всех 
перечисленных конфигурациях. Чтобы разработка проходила легче, стоило выбрать тот язык, который обладает развитым сообществом 
и подробной документацией. Я обратился к веб-сайту TIOBE, который занимается оценкой и аудитом программного обеспечения. 
Специалисты сайта поддерживают ежемесячный рейтинг популярности язык программирования, опираясь на данные которого, 
я решил начать своё исследование. Из представленного списка, наиболее подходящими кандидатами является Java. 
Тем не менее за свою более чем тридцатилетнюю историю, язык успел накопить проблем, в первую очередь связанных со слишком 
строгим следованием правилам ООП и громоздким синтаксисом. Со временем, появились другие языки, которые исправляли эти проблемы, 
а их код мог выполняться в JVM (Java Virtual Machine – среда выполнения Java-кода). Одним из таких языков является 
Kotlin, созданный в Санкт-Петербурге в 2011 году фирмой JetBrains. Имея богатый опыт разработки на Java и контакт с сообществом, 
они создали язык, который решал проблемы Java и был полностью совместим с ним. Другой причиной, почему была выбрана 
именно технология JVM, является протокол JDBC: он обеспечивает интерфейс для взаимодействия с любой SQL СУБД, для которой 
разработчики СУБД написали JDBC-драйвер, что они обычно и делают, ввиду широкого распространения Java/JVM в корпоративной разработке. 
Это позволит мне удобно добавлять поддержку новых СУБД, так как интерфейс выполнения SQL выражений будет общий, сложная 
задача будет состоять в том, чтобы научить программу формировать выражения создания индексов, специфичные для каждой СУБД.


#### React
Приложению также нужен интерфейс взаимодействия с пользователем, графический или консольный. 
Стоит отметить, что, если реализовать взаимодействие с приложением через HTTP-протокол, появится возможность независимо 
от ядра приложения (той части, которая будет отвечать за разбор истории запросов и формирование индексов), обновлять 
графический интерфейс. Или создать консольный интерфейс ввода. Или создать другой клиент на любой вкус. 
Было решено так и поступить. Клиент я решил реализовать в виде веб-приложения, как это часто делают другие авторы ПО, 
используемого в качестве инфраструктурного, к примеру, Jenkins, WildFly или pgAdmin. Выбор фронтенд-фреймворка по большей 
части дело вкуса: каждый из трёх самых популярный фрейворков (Vue, React, Angular) имеет подробную документацию, активное 
сообщество, библиотеки компонентов. С первыми двумя у меня был опыт, в обоих случаях положительный, а потому я решил 
использовать React: мне он нравится своим декларативным подходом. 

#### Redis
Так как ядро приложения будет является сервером, доступ к которому могут получить несколько клиентов в сети, или может 
быть развернуто несколько экземпляров приложения параллельно, нам нужно где-то хранить информацию, какая из баз данных 
сейчас индексируется приложением, чтобы не допустить параллельного построения одних и тех же индексов несколькими 
экземплярами приложения. Хранить достаточно лишь строку – url занятой базы данных. Для таких целей отлично подойдут 
хранилища типа «ключ-значение», самые известные из которых Redis и Memcached. Оба продукта представляют одинаковую 
функциональность, но Redis имеет чуть более удобную в использовании библиотеку для JVM, поэтому был выбран он.

### PostgreSQL
Невозможно будет проверить работу программы без существования сложной базы данных. Модель данных будет описана в
следующей части, здесь стоит определиться с СУБД, в который эта база будет создана. Для тестирования стоит выбрать ту, 
которая имеет несколько разных индексов, позволит создать сложную модель данных, а опыт работы в ней достаточен, 
чтобы в случае проблем быть уверенным, что проблемы именно в разработанном приложении, а не в неправильно сконфигурированной СУБД. 
Для меня такой является PostgreSQL.

### Архитектура

Определившись с используемыми технологиями, нужно понять, когда и как эти технологии будут между собой взаимодействовать. 
При проектировании стоит учитывать, что процесс автоиндексирования базы данных может занимать длительное время, 
поэтому передача клиенту итогового отчёта будет асинхронным процессом. 
В таком случае, наилучшим вариантов будет дать возможность клиенту самому выбрать, куда получить отчёт, 
а не дожидаться HTTP-ответа от серверной части приложения.
1. Клиент создаёт задание на автоиндексирование, предоставляя историю запросов, данные для подключения к БД и данные для получения отчёта.
2. Сервер выполняет необходимые проверки, к примеру, разбор SQL-выражений на корректность, не занята ли БД индексацией в данный момент.
3. Клиент получит ответное сообщение, в случае успеха: что его задание принято в работу, и отчёт он получит, как только работа будет завершена; в случае проблем сообщение об ошибке. Этот процесс представлен на схемах.

![BPMN-схема](product/docs/general.png)
![Компонентная схема](product/docs/arch_v3.jpeg)

Чтобы удостоверить в работе приложения, необходимо проверить его на какой-либо базе данных. 
Эта база должна быть большой по количеству записей и обладать сложной моделью данных. 
Попробуем спроектировать такую БД. Определим предметную область как Частная военная компания. 
Новые отношения (таблицы) и их атрибуты можно придумывать бесконечно, а потому, я усложнял модель, 
пока на это хватало фантазии. Описывать каждую сущность смысла нет, так как процесс исключительно творческий, 
а не технический, а потому сразу приведу полную схему и выдержку из скрипта создания этой модели.

![Даталогическая модель](product/docs/datalogical.jpg)

Главным требованием, предъявляемым к архитектуре серверной части приложения, является простота расширения, что позволит 
просто и быстро добавлять поддержку новый СУБД. Для выполнения SQL-запросов к СУБД используется интерфейс, предоставляемый JDBC: 
он не обладает информацией о том, какие индексы существует в каждой конкретной СУБД, а также о синтаксисе выражения 
добавления индекса, который может отличаться от стандартного SQL. И использовал средство языка программирования, 
которое называется перечислением (enum). Новый элемент перечисления принимает на вход названия индексов СУБД, 
выражение для создания индексов на основе стандарта SQL, переопределяет метод формирования запросов и возвращает 
множество этих запросов. Таким образом, можно выполнять запросы с помощью JDBC независимо от их (запросов) генерации, 
как бы сильно синтаксис конкретной СУБД не расходился с общим SQL-синтаксисом.

![UML-схема поддерживаемых СУБД](product/docs/supported_instances.jpg)

Алгоритм формирования индексов: программа создаёт для каждого уникального SQL-запроса из истории все возможные комбинации: 
`Таблица-поле-тип индекса`

![Алгоритм формирования индексов](product/docs/parsing_alg_horizontal.jpeg)

## Требования

```
- Java11
- PostgreSQL
- Redis
```

## Использование

Клиент и сервер разворачиваются отдельно.

### Сервер

Установите указанные переменные окружения:

| Название            | Описание                        | Пример                                         |
|---------------------|---------------------------------|------------------------------------------------|
| `EMAIL_SENDER`      | Сервер исходящих сообщений      | `test@gmail.com;test; smtp.googlemail.com:465` |
| `REDIS_CACHE_CREDS` | Адрес экземпляра Redis          | `localhost:6379`                               |
| `SERVICE_PORT`      | Порт сервиса данного приложения | `80`                                           |

Далее используйте команду:

```shell
java -jar back/target/server.jar
```

### Клиент

В файле `front/.env` укажите адрес сервера в переменную `REACT_APP_BACK_URL`. Пример: 
`REACT_APP_BACK_URL="http://localhost:80"`

Далее используйте команду:

```shell
cd front
npm start
```

Графический интерфейс клиента

![Графический интерфейс клиента](product/docs/ui_white.png)

Спецификация OpenAPI3 сервера
```yaml
openapi: 3.0.3
info:
  title: SQL-OPTIMIZER
  version: 1.1.0
components:
  schemas:
    FORMATS:
      type: string
      enum:
        - CSV
        - JSON
    CONSUMERS:
      type: string
      enum:
        - EMAIL
        - SMB
        - SFTP
        - FS
    INSTANCES:
      type: string
      enum:
        - POSTGRESQL
        - SQLSERVER
    CommonMessage:
      type: object
      properties:
        details:
          type: string
          required: true
    BenchTask:
      type: object
      properties:
        connectionUrl:
          type: string
          required: true
        queries:
          type: array
          items:
            type: string
          required: true
        consumer:
          type: string
          required: false
          default: FS
        format:
          type: string
          required: false
          default: CSV
        consumerParams:
          type: string
          required: false
          default: ''
        saveBetter:
          type: boolean
          required: false
          default: false
paths:
  /bench:
    post:
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: 'components/schemas/BenchTask'
      responses:
        200:
          description: Info about test request
          content:
            application/json:
              schema:
                $ref: 'components/schemas/CommonMessage'
        400:
          description: This database not supported yet
        404:
          description: Provided connectionUrl doesn't not match pattern
        504:
          description: Can't ping database. Please check it's availability and creds or try again later.
        500:
          description: Error while creating database server specific index for query.
        5XX:
          description: Something goes wrong
  /support/formats:
    get:
      responses:
        200:
          description: Enum with supported formats
          content:
            application/json:
              schema:
                $ref: '#components/schemas/formats'
  /support/consumers:
    get:
      responses:
        200:
          description: Enum with supported methods for saving reports
          content:
            application/json:
              schema:
                $ref: '#components/schemas/consumers'
  /support/instances:
    get:
      responses:
        200:
          description: Enum with DBMS which the program can build indexes
          content:
            application/json:
              schema:
                $ref: '#components/schemas/instances'
```

## Примеры

Примеры отчёта, сформированного программой для выражения
```postgresql
SELECT emp_id FROM employee 
    JOIN position USING (pos_id) 
    JOIN missions_emp USING (emp_id) 
    JOIN mission USING (miss_id) 
WHERE rank !~~ '' 
ORDER BY is_married DESC, end_date_and_time DESC, hiring_date DESC
LIMIT 20; 
```

| indexStatement                                                                                                  | diff     | timeTaken |
|-----------------------------------------------------------------------------------------------------------------|----------|-----------|
| CREATE INDEX Is_marriedEmployeeHASH ON employee USING HASH(is_married);                                         | 173166   | 4956042   |
| CREATE INDEX Is_marriedHiring_dateEmployeeBTREE ON employee USING BTREE(is_married, hiring_date);               | 123791   | 5005417   |
| CREATE INDEX End_date_and_timeMissionBTREE ON mission USING BTREE(end_date_and_time);                           | 78250    | 5050958   |
| CREATE INDEX Emp_idIs_marriedHiring_dateEmployeeBTREE ON employee USING BTREE(emp_id, is_married, hiring_date); | 71375    | 5057833   |
| CREATE INDEX RankPositionHASH ON position USING HASH(rank);                                                     | 45833    | 5083375   |
| CREATE INDEX Emp_idMissions_empHASH ON missions_emp USING HASH(emp_id);                                         | 7375     | 5121833   |
| CREATE INDEX End_date_and_timeMissionHASH ON mission USING HASH(end_date_and_time);                             | -37834   | 5167042   |
| CREATE INDEX Emp_idMissions_empBTREE ON missions_emp USING BTREE(emp_id);                                       | -86209   | 5215417   |
| CREATE INDEX Is_marriedEmployeeBTREE ON employee USING BTREE(is_married);                                       | -109667  | 5238875   |
| CREATE INDEX Emp_idHiring_dateEmployeeBTREE ON employee USING BTREE(emp_id, hiring_date);                       | -173792  | 5303000   |
| CREATE INDEX RankPositionBTREE ON position USING BTREE(rank);                                                   | -181834  | 5311042   |
| CREATE INDEX Hiring_dateEmployeeBTREE ON employee USING BTREE(hiring_date);                                     | -209209  | 5338417   |
| CREATE INDEX Hiring_dateEmployeeHASH ON employee USING HASH(hiring_date);                                       | -414625  | 5543833   |
| CREATE INDEX Emp_idEmployeeBTREE ON employee USING BTREE(emp_id);                                               | -665376  | 5794584   |
| CREATE INDEX Emp_idIs_marriedEmployeeBTREE ON employee USING BTREE(emp_id, is_married);                         | -674542  | 5803750   |
| CREATE INDEX Emp_idEmployeeHASH ON employee USING HASH(emp_id);                                                 | -3633667 | 8762875   |

Больше отчётов можно найти в директории `docs/reports`.