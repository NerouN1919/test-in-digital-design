# Тестовое задание
## Примечание
Операции и столбцы регистро независимые и количество пробелов не учитываются
Также добавлен дополнительный метод в main, который отображает базу данных в виде таблицы. Вывод сделан в консоль  

## Пример запросов и результат
**Пример**
```
javaSchoolStarter.execute("INSERT    VALUES 'id'=0, 'lastName'='test', 'age'=36, 'active'=false, 'cost'=3.6");
            javaSchoolStarter.execute("INSERT VALUES 'id'=10   , 'lastName'=null, 'age'=45, 'active'=true");
            javaSchoolStarter.execute("INSERT VALUES 'lastname'='Fedorov', 'age'=5,    'active'=true, 'cost'=5");
            javaSchoolStarter.execute("INSERT VALUES 'lastName' = 'Федоров' , 'id'=3, 'age'=40, 'active'=true");
            print(javaSchoolStarter.execute("select where 'active' = false and 'lastname'='test' or 'cost'=5"));
            javaSchoolStarter.execute("update VALues 'lastNAME' = 'updated', 'id' = 7 where 'active' = false and 'lastname'='test' or 'cost'=5");
            print(javaSchoolStarter.execute("selECT"));
            javaSchoolStarter.execute("Delete WHERE 'lastName'='updated'");
            print(javaSchoolStarter.execute("select"));
            javaSchoolStarter.execute("update values 'id' = null, 'lastname' = null, 'active' = null, 'cost' = null, 'age' = null");
            print(javaSchoolStarter.execute("select"));
```
**Результат**
```
| id    | lastName | age | active | cost |
+-------+----------+-----+--------+------+
|  0    |  test    |  36 |  false |  3.6 |
|  null |  Fedorov |  5  |  true  |  5.0 |
+-------+----------+-----+--------+------+
| id  | lastName | age | active | cost  |
+-----+----------+-----+--------+-------+
|  7  |  updated |  36 |  false |  3.6  |
|  10 |  null    |  45 |  true  |  null |
|  7  |  updated |  5  |  true  |  5.0  |
|  3  |  Федоров |  40 |  true  |  null |
+-----+----------+-----+--------+-------+
| id  | lastName | age | active| cost  |
+-----+----------+-----+-------+-------+
|  10 |  null    |  45 |  true |  null |
|  3  |  Федоров |  40 |  true |  null |
+-----+----------+-----+-------+-------+
database is empty
```
