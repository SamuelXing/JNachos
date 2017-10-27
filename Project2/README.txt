Project 2, README

PURPOSE:
This is my work for Project2, CIS657-Operating System

COMPILE:
(a) cd JNachos_Project1/src
(b) javac jnachos/*.java jnachos/*/*.java jnachos/*/*/*.java -d ../build/classes

notes: if directory “../build/classes” does not exist, then create one.

RUN:
java jnachos/Main -x ./test/matmult,./test/sort

IDE:
IntelliJ

CHECKLIST:
Swap Space.                     DONE
    (1) Create a SWAP space, all other pages must be brought in to SWAP space on demand

Page Fault Handling.            DONE
    (1) See if there is a free page-frame in RAM
    (2) If not choose a victim page and write it back to disk if
necessary.
    (3) Bring in the requested page into RAM.
    (4) When a process terminates, its address space should be
    cleaned up (reclaim pages in RAM).

Page Replacement Algorithm.      DONE
    (1) You must implement a page replacement algorithm.


WORK I COMPLETED:
(1) Swap space: kern/JNachos.java
Swap space is created when kernel initialize.

(2) Address space: kern/AddrSpace.java
Executable is split into pages, all the pages are going to SWAP space.

(3) Page Replacement Algorithm: Kern/ExceptionHandler.java, Machine/MMU.java
Implemented LRU algorithm. Decreasing page faults times in FIFO from 28473 to 13114 times
when running "matmult" and "sort".

(4) Clean Up: kern/NachoProcess.java
When one process terminated because of exiting, its pages in RAM,
SWAP space and data structure are cleaned up.



LISENCE
BSD 3.0  



