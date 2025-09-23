Prosty projekt w Javie modelujący „szafkę” folderów w strukturze drzewiastej.
Umożliwia dodawanie/usuwanie folderów po ścieżce, wyszukiwanie po nazwie, filtrowanie po rozmiarze według strategii oraz zliczanie wszystkich elementów w drzewie. 
Główna logika siedzi w FolderTree, a interfejs użytkowy zapewnia FileCabinet.
Struktura projektu

### `Folder.java` 
- interfejsy `Folder` i `MultiFolder` (drzewo składa się z węzłów—folderów; węzeł może mieć dzieci jeśli jest `MultiFolder`).

### `Cabinet.java`
- interfejs publiczny „szafki”: wyszukiwanie po nazwie i rozmiarze oraz zliczanie elementów.

### `Size.java` 
- enum z trzema rozmiarami: `SMALL`, `MEDIUM`, `LARGE`.

### `SizeStrategy.java`
- interfejs strategii, która ustala rozmiar węzła na podstawie listy jego dzieci.

### `FileCabinet.java`
- implementacja `Cabinet` + drzewo `FolderTree` + domyślna strategia

### `Main.java`
- Miejsce do testowania i uruchamiania aplikacji.

## Model i interfejsy
