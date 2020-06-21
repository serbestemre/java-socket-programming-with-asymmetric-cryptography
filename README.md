# Java Socket Programming with Asymmetric Cryptography
Multi-threaded server text manipulation using socket programming.

This repository is created for System Programming course project. The goal of this project is to simulate a simple client-server chat application. 


# Requirements

This time your application’s server side should handle
more than one client. You may get help from lecture’s example codes for this. Each client will download the
necessary file from this website as usual: http://homes.ieu.edu.tr/eokur. Please do not forget each client must
have a file with a different name. After the download, clients will read it into a STRING instead of our
usual HashMap structure. Then, there will be a series of communication with the server

- The client will send the string first.
- The client will send the choices for casing (U/L), shifting (1-3), and coloring (R/Y), changes, one at a time
to the server. Do not forget each client will do the same things. Your server should be able to handle all
clients doing the same step.
- The server will transform the string received from a client with the help of case, shift and color threads. As
for transformations, they work same as before but now they will do the transformations on a string.
- Lastly, the server will send the modified string back to be displayed by the client that sent it. 


Transformations on the content is as follows as a reminder:
1. Make all characters in the file upper case or lower case (caseThread)
2. Encrypt the file by shifting characters by a specified amount (e.g. a→b if the shift amount is 1)
(shiftThread)
3. Color the file red or yellow. (colorThread)

Before these transformations, the program will ask the user to enter input values
1. U for upper case, L for lower case
2. The shift amounts. A value from 1 to 3
3. R for red, Y for yellow.

# What can be improved? 

Encryption and Decryption are done by hard coding in the current version of the application. It can be implemented as two separate methods.


