# JavaClient4CouchDB
Beispielnutzung des Comand Line Interface (CLI) Programms:

## Daten einlesen und vorverarbeiten
Gib im CLI folgendes ein:
-fetch https://www.reddit.com/r/CatsStaringAtWalls -process

Dies kann auch einzeln erfolgen als:
1. Einlesen -fetch https://www.reddit.com/r/CatsStaringAtWalls
2. Wenn erfolgt: -use CatsStaringAtWalls -process

Beachte: fetch und process müssen nur initial angewendet werden. Anschließend sind die Daten in CouchDB hinterlegt und können mittels -use "SubredditName" verwendet werden.

## Abfragen tätigen
1. Datenbank bzw. Subreddit festlegen
-use CatsStaringAtWalls

Beachte: -use muss nur einmal ausgeführt werden anschließend wird automatisch die zuvor definierte Datenbank verwendet ist ist aber auch möglich -use "SubredditName" den folgenden Anfragen voranzustellen.

2. Mögliche Abfragen
friends person:labourgeoisie 
degreeCentralityMinMax
degreeCentrality person:labourgeoisie
bridges
help
