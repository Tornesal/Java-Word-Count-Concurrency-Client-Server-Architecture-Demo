    
# Distributed Word Count System (Multi-Tiered)

This project is a multi-threaded, client-server system designed to process text files, count word/line/character metrics based on a specific grammar, and manage persistent storage. It implements a logical three-tier architecture (Presentation, Processing, Data) running on a single physical machine via the JVM.

## Features

*   **Multi-Threaded Server:** Supports multiple concurrent client connections without blocking.
*   **LRU Caching Strategy:** Implements a fixed-size `LinkedHashMap` cache to keep "hot" files in memory for fast retrieval.
*   **Persistence Layer:** Custom Overlay File System that saves user files to disk and serializes global system statistics to binary (`.dat`) format to survive server restarts.
*   **Write-Through Consistency:** Updates both the Disk and Cache simultaneously to ensure data integrity.
*   **Custom Application Protocol:** A TAB-delimited, sentinel-based protocol designed to reliably transmit multi-line text streams over TCP.
*   **Strict Grammar Parsing:** Custom parsing logic that counts alphanumeric units while ignoring specific separators (`. : ; -`).


## Architecture

The system is divided into three distinct logical tiers:

1.  **Tier 1: Presentation (The Client)**
    *   **Entry Point:** `client.clientMain`
    *   **Role:** Handles user interaction via a CLI menu, reads local files, and manages the TCP session.

2.  **Tier 2: Processing (The Server)**
    *   **Entry Point:** `server.serverMain`, `server.clientHandler`
    *   **Role:** Manages thread pooling, protocol parsing, and cache management via `server.cacheHandler`.

3.  **Tier 3: Persistence (The Data Layer)**
    *   **Entry Point:** `server.dataManager`
    *   **Role:** Manages the `server_data/` directory and synchronized access to global statistics.


## Installation & Usage

### Prerequisites
*   Java Development Kit (JDK) 8 or higher.

### Compilation
Open your terminal in the project root directory:
```bash
javac server/*.java client/*.java
```

1. Start the Server

You can optionally specify the cache size as an argument (default is 5).
```bash
java server.serverMain 10
```

The server runs on port 5050 and will create a server_data folder automatically.
2. Start the Client

Open a new terminal window:
```bash
java client.clientMain
```

    
### Part 4: Protocol

## Application Protocol

Communication occurs via a stateless TCP stream using a custom text protocol.

**Header Format:**
`COMMAND` `\t` `FILENAME` `\t` `FLAGS`

**Sentinel Strategy:**
To handle multi-line files, the client sends data line-by-line and terminates the stream with a specific magic word on its own line:
`__END__`

### Supported Commands

| Command | Description | Example Request |
| :--- | :--- | :--- |
| **STORE** | Uploads a file. Returns counts. | `STORE \t notes.txt \t LWC` |
| **READ** | Downloads a file. Checks Cache then Disk. | `READ \t notes.txt \t 0` |
| **REMOVE** | Deletes file from Cache and Disk. | `REMOVE \t notes.txt \t 0` |
| **LIST** | Lists all files in persistent storage. | `LIST \t null \t 0` |
| **TOTALS** | Returns global system stats. | `TOTALS \t null \t 0` |


## Design Decisions

*   **Sentinel vs. Fixed Length:** We chose a sentinel (`__END__`) approach for data transmission. This allows the client to send variable-length multi-line text without needing to pre-calculate byte arrays, improving the user experience during manual data entry.
*   **Atomic Operations / Monitors:** The Data Layer uses synchronized primitives for global statistics to prevent race conditions when multiple clients finish processing simultaneously.
*   **Binary Metadata:** System totals are saved as `totals.dat` using `DataOutputStream`. This prevents parsing errors and distinguishes system files from user text files in the storage directory.

## Authors

*   **Brandon DeCelle** - *Design & Implementation*
*   **Course:** CSIS 3810 - Operating Systems
