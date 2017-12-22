#include "syscall.h"

int bid;
char answer[5];
char sender[7];
char message[50];
int rst;

int
main()
{
    sender[0] = 's';
    sender[1] = 'e';
    sender[2] = 'n';
    sender[3] = 'd';
    sender[4] = 'e';
    sender[5] = 'r';

    bid = WaitMsg(sender, message);
    if(message[0] == 'a')
    {
        answer[0] = 'c';
        answer[1] = 'o';
        answer[2] = 'p';
        answer[3] = 'y';

        rst = SendAnswer(answer, bid);  // TODO: rst not used
    
        Exit(sender[5]);
    }
    else
    {
       Exit(-1);
    }
}
