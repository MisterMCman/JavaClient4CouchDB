# JavaClient4CouchDB

## Wir richtige ich dieses Projekt in meinem Eclipse ein?

Diese Projekt verwendet Maven um Bibliotheken einzubinden. Um dieses Projekt auf dem eigenen Rechner zu importieren geht man am besten wie folgt vor:

1. Klonen Sie dieses Projekt über eine Installation Git oder über andere Tools wie SourceTree in Ihren Eclipse Workspace

2. Im nächsten Schritt muss das Projekt als Maven-Projekt importiert werden 

	- File > Import > Maven > Existing Maven Projects > Next
	- Wähle den Ordner in welchen das Projekt geklont wurde also z.B. `workspace/JavaClient4CouchDB`
	- Setze einen Haken beim Projekt und bestätige mit Finish
	
## Die Verwendung

Die main Methode des Programms liegt in der Klasse RedditCouch.java starte diese mittels Run. In der Console wird `Enter Command:` ausgegeben. 
Es folgt eine Beispielhafte Verwendung des CLI-Programms:

### Daten einlesen und vorverarbeiten

Gib im CLI folgendes ein:

`-fetch https://www.reddit.com/r/CatsStaringAtWalls -process`

Dies kann auch einzeln erfolgen als:

1. Einlesen `-fetch https://www.reddit.com/r/CatsStaringAtWalls`

2. Wenn erfolgt: `-use CatsStaringAtWalls -process`

Beachte: `fetch` und `process` müssen nur initial angewendet werden. Anschließend sind die Daten in CouchDB hinterlegt und können mittels `-use nameOfSubreddit` verwendet werden.

### Abfragen tätigen

#### Datenbank bzw. Subreddit festlegen

`-use CatsStaringAtWalls` 

Beachte: `-use` muss nur einmal ausgeführt werden anschließend wird automatisch die zuvor definierte Datenbank verwendet ist ist aber auch möglich `-use nameOfSubreddit` den folgenden Anfragen voranzustellen.

#### Mögliche Abfragen

`friends person:labourgeoisie`

`degreeCentralityMinMax`

`degreeCentrality person:labourgeoisie`

`bridges`

`help`
