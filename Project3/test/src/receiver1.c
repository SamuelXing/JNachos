#include "syscall.h"

int bid;
int bid1;
int bid2;
char answer[5];
char sender[7];
char message[50];
char message1[50];
char message2[50];
int rst;
int rst1;
int rst2;

int
main()
{
    sender[0] = 's';
    sender[1] = 'e';
    sender[2] = 'n';
    sender[3] = 'd';
    sender[4] = 'e';
    sender[5] = 'r';
    sender[6] = '2';
    
    bid = WaitMsg(sender, message);
    bid1 = WaitMsg(sender, message1);
    bid2 = WaitMsg(sender, message2);

    answer[0] = 'c';
    answer[1] = 'o';
    answer[2] = 'p';
    answer[3] = 'y';

    rst = SendAnswer(answer, bid);  
    rst1 = SendAnswer(answer, bid1);
    rst2 = SendAnswer(answer, bid2);
    if(rst2 == -1)
    {
        Exit(-1);
    }
    Exit(sender[5]);
}
