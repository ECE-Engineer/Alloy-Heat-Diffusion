#ifndef GPU_H
#define GPU_H
//typedef struct alloy;

alloy* malloc_alloy();
void free_alloy_struct(alloy*);

double* malloc_2d(int, int);
void free_2d(double*, int, int);

double* malloc_3d(int, int, int);
void free_3d(double*, int, int, int);

__host__ void update_alloy(int, alloy*);
__global__ void update_section(double*, double*, alloy*, double*);

__device__ int offsetDEV(int, int, int);
__device__ int offsetDEV(int, int, int, int, int);
#endif // GPU_H
