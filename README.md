Multiplayer_Online_Game_Engine
==============================

Implementation of an online version of the Game of Nim using internet domain sockets.

There are three parts in this project:

1. Single Game 

2. Multiple Concurrent games

3. Capability to observe other games


The Game of Nim 
=========================================================
The Game of Nim is a game played between two players. 
There are M sets of objects presented to the two players. 
The object can be anything you like: chocolate, book, CD, etc. 
Assume Set j contains Nj objects, Nj>0, where j=1..M. 
The two players take turns removing objects from these sets. 
At his/her turn, a player must take one or more objects from exactly one set. 
The player who takes the last object is the winner. 

An example game is: 

set 1 2 3 

size 4 5 6 

A takes 2 from set 2 

    4 3 6 

B takes 1 from set 1 

    3 3 6 

A takes 6 from set 3 

    3 3 0 

B takes 1 from set 2 

    3 2 0 

A takes 1 from set 1 

    2 2 0 

B takes 2 from set 1 

    0 2 0 

A takes 2 from set 2 

    0 0 0 

A wins.