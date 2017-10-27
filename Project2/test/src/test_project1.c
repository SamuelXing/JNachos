#include "syscall.h"

char matFile[] = "test/matmult";
char sortFile[] = "test/sort";

int main()
{
	int pid = Fork();
	if(pid == 0){
		Exec(sortFile);
	}
	else{
		Exec(matFile);
	}
	return 0;
}
