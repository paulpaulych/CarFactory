# Car Factory

Создано для тренировки скиллов в Java Multithreading.

Паттерны: Producer/Consumer, MVC, Observer

Технологии: 
1. Java 15 Preview.
2. JavaFX 15

## Overview

Оригинальный текст задания я потерял, поэтому своими словами:

Приложение имитирует работу автомобильной фабрики.
Автомобиль состоит из:
1. кузова
2. двигателя
3. набора аксессуаров.
Каждую деталь поставляет поставщик(да ладно?), представленный java-тредом и помещает ее на соответствующий склад.
Работники фабрики(джава-треды) достают со складов детали и собирают машину, затем помещают ее на склад.
Дилеры(джава-треды) продают готовые автомобили.

### Доп.требования по пунктикам:
1. Поставщик кузовов один, поставщик двигателей один, а кол-во поставщиков аксессуаров задается в конфигурационном файле
2. Кол-во дилеров задается в файле
3. Вместимость каждого склада задается в конф.файле
4. Каждая роль имеет скорость работы ( 1/{время сна потока} ), которая регулируется в пользовательском интерфейсе
5. Кол-во сброщиков машин меняется динамически в зависимости от текущей загруженности склада.
    Кол-во машин на складе должно стремиться к указанному в конф.файле значению   
6. В UI можно увидеть:
    - текущее состояние складов
    - общее количество задач выполненных каждой ролью
    - количество активных задач для каждой роли

## Текущие ограничения
1. Бедноватый UI
2. Изменение скорости работы роли входит в силу после завершения текущей итерации для данной роли
