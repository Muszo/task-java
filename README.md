# Szafka folderów — zadanie rekrutacyjne Java

Prosty projekt w Javie modelujący „szafkę” folderów w strukturze drzewiastej.
Pozwala dodawać i usuwać foldery po ścieżce, wyszukiwać węzły po nazwie, filtrować po rozmiarze zgodnie ze strategią oraz zliczać wszystkie elementy drzewa.

## Cel zadania

Zaimplementować w klasie `FileCabinet` metody:
- `Optional<Folder> findFolderByName(String name)` — zwraca dowolny element o podanej nazwie,
- `List<Folder> findFoldersBySize(String size)` — zwraca wszystkie foldery o rozmiarze `SMALL/MEDIUM/LARGE` (wejście tekstowe, case-insensitive),
- `int count()` — zwraca liczbę wszystkich obiektów (węzłów) w strukturze.


## Szybki start

W projekcie jest skonfigurowany Gradle (wrapper w repo). Komendy:

```bash
# Uruchom testy (JUnit 5)
./gradlew test

# Zbuduj projekt (kompilacja + testy)
./gradlew build

# Sprzątanie artefaktów
./gradlew clean
```

Wymagania narzędziowe:
- Gradle wrapper (`./gradlew`) – nie trzeba mieć globalnego Gradle,
- toolchain Java ustawiony na 24 (Gradle wybierze lokalny JDK 24, jeśli dostępny).

## Struktura projektu

- `src/Cabinet.java` — interfejs publiczny „szafki”: wyszukiwanie po nazwie i rozmiarze, zliczanie elementów.
- `src/FileCabinet.java` — implementacja `Cabinet`; deleguje do `FolderTree`, zarządza strategią i walidacją wejścia.
- `src/Folder.java` — interfejsy `Folder` (nazwa, rozmiar) i `MultiFolder` (lista dzieci).
- `src/FolderTree.java` — wewnętrzna struktura drzewa: dodawanie/usuwanie po ścieżce, wyszukiwanie po nazwie, filtrowanie po rozmiarze (strategia), zliczanie; pełna obsługa `MultiFolder`.
- `src/Size.java` — enum: `SMALL`, `MEDIUM`, `LARGE`.
- `src/SizeStrategy.java` — interfejs strategii: klasyfikuje rozmiar węzła na podstawie listy jego bezpośrednich dzieci.
- `src/SizeUtils.java` — narzędzia; `parse(String)` → `Optional<Size>` (case-insensitive, bezpieczne dla niepoprawnych wartości i `null`).
- `src/SomeSizeStrategy.java` — przykładowa strategia: 0→`SMALL`, 1..5→`MEDIUM`, ≥6→`LARGE`.
- `tests/SizeStrategyIntegrationTest.java` — testy integracyjne/weryfikujące zachowanie.

## API (Cabinet)

- `Optional<Folder> findFolderByName(String name)`
  - `null` lub puste drzewo → `Optional.empty()`.
  - Wyszukiwanie w głąb, zwraca pierwsze trafienie po nazwie.
- `List<Folder> findFoldersBySize(String size)`
  - `size` parsowane przez `SizeUtils.parse` (case-insensitive).
  - `null`/niepoprawna wartość → pusta lista. Puste drzewo → pusta lista.
  - Rozmiar węzła liczony przez strategię na podstawie liczby jego bezpośrednich dzieci.
- `int count()`
  - Liczba wszystkich węzłów w drzewie (liście i `MultiFolder`). O(1) – utrzymywana w `FolderTree`.

## Architektura i decyzje

- `FileCabinet` jest głównym API. Trzyma strategię (`SomeSizeStrategy` domyślnie) i opcjonalne drzewo `FolderTree`.
- `FolderTree` kapsułkuje operacje na drzewie: dodawanie/usuwanie po ścieżce, wyszukiwanie, filtrowanie, zliczanie.
- Obsługa `MultiFolder` jest rekurencyjna: dodanie `MultiFolder` dołącza też jego dzieci.
- Unikanie duplikacji: logika walidacji i parsowania jest w `FileCabinet`, a praca na drzewie – w `FolderTree`.

## Strategia rozmiaru

`SomeSizeStrategy` (domyślna) klasyfikuje rozmiar węzła wg liczby bezpośrednich dzieci:
- 0 → `SMALL`
- 1..5 → `MEDIUM`
- ≥6 → `LARGE`

Można wstrzyknąć własną strategię przez konstruktor `FileCabinet(SizeStrategy strategy)`.

## Operacje na ścieżkach

- Ścieżki rozdzielane po `/`; białe znaki i puste segmenty są ignorowane.
- `addFolder("", folder)` lub `addFolder(null, folder)` → dodanie na poziomie korzenia.
- `removeFolder("")`/`removeFolder(null)` → brak operacji.
- `addFolder(path, folder)` rzuci `IllegalArgumentException`, gdy rodzic ze ścieżki nie istnieje.


(W testach znajdują się proste implementacje pomocnicze `LeafFolder` i `MF`).

## Przypadki brzegowe i założenia

- `findFolderByName(null)` → `Optional.empty()`.
- `findFoldersBySize(null)`/`"invalid"/""` → pusta lista (bez wyjątków).
- Nazwy nie są unikalne – zwracane jest „jakiekolwiek” trafienie.
- Usunięcie węzła usuwa również całe poddrzewo.
- Dodanie `null` folderu — ignorowane.
- `removeFolder` dla nieistniejącej ścieżki — brak efektu.

## Złożoność (w przybliżeniu)

- `findFolderByName` — O(n), `findFoldersBySize` — O(n), `count` — O(1).
- `addFolder`/`removeFolder` — zależne od długości ścieżki i wielkości poddrzewa.

## Jak zweryfikować rozwiązanie

- Testy JUnit 5 znajdują się w `tests/SizeStrategyIntegrationTest.java`.
- Uruchomienie:

```bash
./gradlew test
```

Oczekiwany wynik: „BUILD SUCCESSFUL”.
