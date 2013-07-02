************************************************************************
file with basedata            : mf61_.bas
initial value random generator: 2812
************************************************************************
projects                      :  1
jobs (incl. supersource/sink ):  32
horizon                       :  236
RESOURCES
  - renewable                 :  2   R
  - nonrenewable              :  2   N
  - doubly constrained        :  0   D
************************************************************************
PROJECT INFORMATION:
pronr.  #jobs rel.date duedate tardcost  MPM-Time
    1     30      0       30       22       30
************************************************************************
PRECEDENCE RELATIONS:
jobnr.    #modes  #successors   successors
   1        1          3           2   3   4
   2        3          2          21  26
   3        3          2           6   7
   4        3          3           5  12  13
   5        3          3           6   8  15
   6        3          1          21
   7        3          3           8   9  20
   8        3          2          17  18
   9        3          3          10  14  17
  10        3          3          11  15  25
  11        3          2          19  22
  12        3          3          14  18  22
  13        3          3          16  18  20
  14        3          2          24  29
  15        3          1          16
  16        3          1          21
  17        3          1          19
  18        3          2          26  28
  19        3          2          23  26
  20        3          1          31
  21        3          1          29
  22        3          2          30  31
  23        3          1          27
  24        3          3          25  27  28
  25        3          1          30
  26        3          2          27  29
  27        3          1          31
  28        3          1          30
  29        3          1          32
  30        3          1          32
  31        3          1          32
  32        1          0        
************************************************************************
REQUESTS/DURATIONS:
jobnr. mode duration  R 1  R 2  N 1  N 2
------------------------------------------------------------------------
  1      1     0       0    0    0    0
  2      1     2       9    6    9    6
         2     4       7    4    8    3
         3    10       6    1    7    2
  3      1     3       9   10    9    6
         2     6       8   10    4    3
         3     7       6   10    2    2
  4      1     3       8    3    9    8
         2     6       7    3    4    6
         3     7       7    3    3    4
  5      1     3       6    7    7    5
         2    10       3    2    5    5
         3    10       1    4    5    3
  6      1     4       6    7    5    7
         2     5       4    4    4    6
         3     8       3    3    4    5
  7      1     8       3    9    6    9
         2     8       3   10    8    7
         3     9       3    7    5    3
  8      1     1       8   10    7    6
         2     4       4   10    6    6
         3     5       2   10    6    6
  9      1     1       8    3    6    3
         2    10       8    3    3    2
         3    10       7    3    3    3
 10      1     4      10    7    5    7
         2     7       9    5    4    6
         3    10       9    3    2    2
 11      1     1       8    5   10    5
         2     4       6    4   10    3
         3     5       4    4   10    2
 12      1     2      10    8    8    9
         2     8       9    7    7    5
         3     9       9    4    6    4
 13      1     1       7    2    8    8
         2     4       5    2    4    5
         3     7       2    1    2    1
 14      1     1       4    7    6    5
         2     3       3    4    6    3
         3     3       1    5    5    3
 15      1     4       4   10    6    9
         2     8       3    8    3    6
         3     9       3    6    3    4
 16      1     1       8    8    7    6
         2     2       4    7    6    5
         3     5       2    3    4    5
 17      1     2       9    7    8    6
         2     2       6    7    8    7
         3     4       4    5    7    3
 18      1     2       8    7   10    5
         2     3       7    6   10    3
         3     7       6    2   10    3
 19      1     1       4    7    7    7
         2     1       3    6    7    8
         3     9       1    5    7    2
 20      1     6       7    7    4    5
         2     9       3    7    3    4
         3     9       2    6    2    5
 21      1     3       8    5    8    9
         2     6       7    3    6    7
         3    10       6    2    4    6
 22      1     5       4    2    4    6
         2     7       4    2    3    5
         3     9       3    1    3    5
 23      1     1       3    6    7    8
         2     4       3    5    6    5
         3     4       2    5    4    6
 24      1     4       9    3    9    5
         2     8       9    3    7    5
         3    10       6    3    6    4
 25      1     1      10    4    7    1
         2     8       5    4    6    1
         3     9       2    3    3    1
 26      1     3       9   10   10    8
         2     5       6    9    5    7
         3     5       5    9    5    8
 27      1     8       9    3    2    5
         2     9       7    3    2    5
         3    10       5    3    2    3
 28      1     2       6    7    4    3
         2     5       6    3    4    2
         3     8       5    2    3    2
 29      1     3       6    6    7    8
         2     3       6    8    7    6
         3     9       4    4    5    5
 30      1     2       2    6    4    7
         2     8       2    6    4    6
         3     9       1    6    2    5
 31      1     1       8    9    5    7
         2     6       7    8    4    5
         3    10       6    5    1    4
 32      1     0       0    0    0    0
************************************************************************
RESOURCEAVAILABILITIES:
  R 1  R 2  N 1  N 2
   18   16  206  191
************************************************************************