char msg[4];
char rcv[9];
char asw[50];

int bid;
int rst;

int
main()
{

    msg[0] = '1';
    msg[1] = '2';
    msg[2] = '3';

    rcv[0] = 'r';
    rcv[1] = 'e';
    rcv[2] = 'c';
    rcv[3] = 'e';
    rcv[4] = 'i';
    rcv[5] = 'v';
    rcv[6] = 'e';
    rcv[7] = 'r';


    bid = SendMsg(rcv, msg);
    rst = WaitAnswer(asw, bid);  //TODO: bid not used
    if(rst == 0)
    {
        Exit(-1);
    }
    else
    {
        Exit(rcv[7]);
    }
}
