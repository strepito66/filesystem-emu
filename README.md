# File system built in Java
Simulazione di un file system vero e proprio, con persistenza dei dati.
### Features
- **Login/Signup/Logout**: Fornisce le funzionalità essenziali per l'autenticazione (login), la registrazione di nuovi utenti (signup) e la disconnessione (logout) dal filesystem.
- **Password Hashing**:  Password crittografate con algoritmo SHA512 (con salting).
- **Persistenza**: Il file system viene ricaricato dal disco se viene individuato uno stato salvato
- **Shell**: È possibile interagire con il FS tramite una shell che vuole imitare l'effettiva shell UNIX.
- **Gestione dei permessi**: Sono completamente gestibili i permessi sia di file regolari che di cartelle.

### Comandi Disponibili
* `help`: Mostra la lista dei comandi disponibili.
* `login <username> <password>`: Effettua il login di un utente.
* `signup <username> <group> <password>`: Crea un nuovo utente.
* `exit`: Salva lo stato corrente ed esce dalla shell.
* `grouplist`: Elenca tutti i gruppi disponibili.
---
* `groupadd <group>`: Crea un nuovo gruppo (richiede login).
* `logout`: Effettua il logout dell'utente corrente (richiede login).
* `mkdir <name>`: Crea una nuova directory (richiede login).
* `cd <dir>`: Cambia la directory corrente (richiede login). Usa `..` per la directory padre o `cd` da solo per la root.
* `ls (-l/-r/-R)`: Elenca file e directory nella directory corrente (richiede login). `-l` per formato lungo, `-r` per ordine inverso, `-R` per listato ricorsivo.
* `touch <fileName>`: Crea un nuovo file vuoto (richiede login).
* `read <fileName>`: Legge il contenuto di un file (richiede login).
* `write <fileName> <string/bytes>`: Scrive contenuto in un file (richiede login).
* `execute <fileName>`: Esegue un file (richiede login).
* `rm (-r) <fileName/dirName>`: Rimuove un file o una directory (richiede login). Usa `-r` per le directory.
* `chmod <perms> <fileName>`: Cambia i permessi di un file usando la notazione ottale (richiede login).
### Componenti Chiave

#### File System Core
- **`FileSystem`**: La classe principale che gestisce la struttura gerarchica del file system, inclusa la directory root e la gestione degli utenti e dei gruppi.
- **`Directory`**: Rappresenta una directory all'interno del file system, contenente riferimenti a file e sottodirectory.
- **`File`**: Classe base astratta per i nodi del file system, gestisce anche i permessi per le sue sottoclassi.
   -  **`Permission`**: Nested class che processa gli ottali dei permessi e effettua controlli sui permessi.
- **`RegularFile`**: Rappresenta un file regolare con contenuto, al momento non sono eseguibili.
#### ZShell
- **`ZShell`**: Implementa l'interfaccia a riga di comando per interagire con il file system, interpretando i comandi dell'utente e invocando le operazioni appropriate sul `FileSystem`.
- Viene inizialmente creato un gruppo di default, da passare al costruttore di Zshell.
- Il primo utente del gruppo di default prende il ruolo di owner della directory **root**.
- Solo un utente può registrarsi nel gruppo di default.
#### Gestione Utenti e Gruppi
- **`User`**: Rappresenta un utente del sistema con username e password.
- **`Group`**: Rappresenta un gruppo di utenti.
#### Persistenza
- Il sistema supporta la persistenza dei dati del file system, salvando e caricando lo stato per mantenere i dati tra le sessioni.
- L'oggetto Zshell viene serializzato e salvato sul disco al comando "exit", per essere ricaricato al prossimo avvio, permettendo di continuare con gli stessi utenti/gruppi/file


### Possibili miglioramenti
- **Hashing con algoritmi migliori**: Attualmente `File` sfrutta SHA512 con salt, però algoritmi come  BCrypt.
- **Comandi aggiuntivi**: Possibili aggiunte potrebbero essere adduser, userdel, sudo, un comando per forzare il salvataggio dello stato, comandi per gestire ulteriormente i gruppi, etc.
- **Mounting**: Potrebbe essere implementato un sistema di mounting per "unire" più oggetti filesystem sotto la stessa ZShell.
- **Comandi con path**: Al momento tutti i comandi operano nella directory attiva, si potrebbe implementare un parser di path per migliorare la ZShell
- **Requisiti di Complessità/Lunghezza**: Introdurre requisiti minimi per le password.
- **Sanificazione Input (Char Escaping)**: Lo `username` viene parzialmente "sanificato" in `ZShell` (`replaceAll(",", "")`) durante il signup, ma sarebbe necessaria una validazione e sanificazione più rigorosa per prevenire potenziali problemi (es. caratteri speciali che potrebbero rompere la logica di parsing o essere usati per attacchi).
- **Error handling**: Nello stato corrente, nel caso un utente non abbia il permesso di eseguire una specifica azione, l'intera shell si chiuderà avvisando l'utente del perchè, senza però salvare lo stato.
### Esempio di Utilizzo

**1. Compila il Progetto:**

Apri un terminale nella directory principale del tuo progetto (dove si trova il file `build.gradle.kts`) ed esegui:

```sh  
# Su Linux/macOS  
./gradlew build  
```  

```bat  
# Su Windows  
gradlew build  
```  

**2. Avvia la shell:**

Nello stesso terminale (o in uno nuovo), esegui il task per avviare la shell:

```sh  
# Su Linux/macOS  
./gradlew runFS --console=plain  
```  

```bat  
# Su Windows  
gradlew runFS --console=plain
```  

Da questo terminale è adesso possibile interagire con la shell. Per vedere tutti i comandi a disposizione sfruttare il comando `help`