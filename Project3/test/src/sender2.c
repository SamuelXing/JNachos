#include "syscall.h"

char msg[4];
char msg1[4];
char msg2[4];
char rcv[9];
char asw[50];
char asw1[50];
char asw2[50];

int bid;
int bid1;
int bid2;
int rst;
int rst1;
int rst2;

int
main()
{

    msg[0] = '1';
    msg[1] = '2';
    msg[2] = '3';

    msg1[0] = 'm';
    msg1[1] = 's';
    msg1[2] = 'g';

    msg2[0] = 'm';
    msg2[1] = 's';
    msg2[2] = 'j';
    
    rcv[0] = 'r';
    rcv[1] = 'e';
    rcv[2] = 'c';
    rcv[3] = 'e';
    rcv[4] = 'i';
    rcv[5] = 'v';
    rcv[6] = 'e';
    rcv[7] = 'r';
    rcv[8] = '1';


    bid = SendMsg(rcv, msg);
    bid1 = SendMsg(rcv, msg1);
    bid2 = SendMsg(rcv, msg1);
    rst = WaitAnswer(asw, bid);  
    rst1 = WaitAnswer(asw1, bid1);
    rst2 = WaitAnswer(asw2, bid2);

    if(rst != 0 && rst1 != 0 && rst2 != 0)
    {
        Exit(rcv[7]);
    }
    else
    {
        Exit(-1);
    }
}
