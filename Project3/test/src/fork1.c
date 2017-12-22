#include "syscall.h"

char snd[7];
char rcv[10];
char message[5];
char answer[5];
char get_msg[50];
char get_asw[50];

int x;
int bid;
int bid1;
int rst;
int rst1;

int main()
{
    message[0] = '1';
    message[1] = '2';
    message[2] = '3';
    
    answer[0] = 'c';
    answer[1] = 'o';
    answer[2] = 'p';
    answer[3] = 'y';
    
    snd[0] = 'f';
    snd[1] = 'o';
    snd[2] = 'r';
    snd[3] = 'k';
    snd[4] = '1';
    
    rcv[0] = 'c';
    rcv[1] = '_';
    rcv[2] = 'f';
    rcv[3] = 'o';
    rcv[4] = 'r';
    rcv[5] = 'k';
    rcv[6] = '1';
    
    x = Fork();
    if(x != 0)
    {
      bid = SendMsg(rcv, message);
      rst = WaitAnswer(get_asw, bid);
      if(rst == 0)
      {
         Exit(-1); 
      }
      else
      {
         Exit(114);
      }
    }
    else
    {
      bid1 = WaitMsg(snd, get_msg);
      rst1 = SendAnswer(answer, bid1);
      if(rst1 == 0)
      {
         Exit(-1);
      }
      else
      {
         Exit(114);
      }
    }
}
