#ifndef CPU_H
#define CPU_H
typedef struct alloy;

alloy* malloc_alloy();
void free_alloy_struct(alloy*);

double* malloc_2d(int, int);
void free_2d(double*, int, int);

double* malloc_3d(int, int, int);
void free_3d(double*, int, int, int);

void update_alloy(int, alloy*);
double next_position_temp(alloy*, double*, int, int);
#endif // CPU_H
