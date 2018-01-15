#include <stdio.h>
#include <stdlib.h>

#include "alloy.h"
#include "gpu.h"

alloy* malloc_alloy() {
    return (alloy*) malloc(sizeof(alloy));
    /*alloy* my_alloy;
    cudaMallocManaged(&my_alloy, sizeof(alloy));

    return my_alloy;*/
}

void free_alloy_struct(alloy* my_alloy) {
    free(my_alloy);
    //cudaFree(my_alloy);
}

double* malloc_2d(int width, int height) {
    //double** array = (double**) malloc(width * sizeof(double*) + width * height *
    //        sizeof(double));

    /*double** array;
    cudaMallocManaged(&array, width * sizeof(double*) + width * height *
              sizeof(double));*/
    /*double *offs_points = (double*) &array[width];
    for (int i = 0; i < width; i++, offs_points += height) {
        array[i] = offs_points;
    }

    return array;*/
    return (double*) malloc(width * height * sizeof(double));
}

void free_2d(double* array, int height, int width) {
    free(array);
    //cudaFree(array);
}

double* malloc_3d(int width, int height, int depth) {
    //double*** array = (double***) malloc(width * sizeof(double**));
    /*double*** array;
    cudaMallocManaged(&array, width * sizeof(double**));*/
    /*for (int i = 0; i < width; i++) {
        array[i] = (double**) malloc(height * sizeof(double*));
        //cudaMallocManaged(&array[i], height * sizeof(double*));
        for (int j = 0; j < height; j++) {
            array[i][j] = (double*) malloc(depth * sizeof(double));
            //cudaMallocManaged(&array[i][j], depth * sizeof(double));
        }
    }

    return array;*/

    return (double*) malloc(width * height * depth * sizeof(double));
}

void free_3d(double* array, int width, int height, int depth) {
    /*for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
            free(array[i][j]);
            //cudaFree(array[i][j]);
        }
        free(array[i]);
        //cudaFree(array[i]);
    }
    free(array);*/
    //cudaFree(array);

    free(array);
}

__host__
void update_alloy(int turn, alloy* my_alloy) {
    double *read, *write;
    if (turn % 2 == 0) {
        read = my_alloy->points_a;
        write = my_alloy->points_b;
    } else {
        read = my_alloy->points_b;
        write = my_alloy->points_a;
    }

    int width = my_alloy->width;
    int height = my_alloy->height;
    int depth = 3;

    //int rw_size = width * sizeof(double*) + width * height * sizeof(double);
    //int mat_size = ((depth * sizeof(double) * height) + (height * sizeof(double*))) * width + width * sizeof(double**);
    int rw_size = width * height * sizeof(double);
    int mat_size = width * height * depth * sizeof(double);

    double *readGPU, *writeGPU;
    alloy* my_alloyGPU;
    double* materialGPU;

    cudaMalloc((void**) &readGPU, rw_size);
    cudaMalloc((void**) &writeGPU, rw_size); // May not need to be memcpy'ed
    cudaMalloc((void**) &my_alloyGPU, sizeof(alloy));
    cudaMalloc((void**) &materialGPU, mat_size);

    cudaMemcpy(readGPU, read, rw_size, cudaMemcpyHostToDevice);
    cudaMemcpy(writeGPU, write, rw_size, cudaMemcpyHostToDevice);
    cudaMemcpy(my_alloyGPU, my_alloy, sizeof(alloy), cudaMemcpyHostToDevice);
    cudaMemcpy(materialGPU, my_alloy->materials, mat_size, cudaMemcpyHostToDevice);

    int num_threads = 1024;
    update_section<<<1, num_threads>>>(readGPU, writeGPU, my_alloyGPU, materialGPU);

    cudaDeviceSynchronize();

    cudaMemcpy(write, writeGPU, rw_size, cudaMemcpyDeviceToHost);

    cudaFree(readGPU);
    cudaFree(writeGPU);
    cudaFree(my_alloyGPU);
    cudaFree(materialGPU);
}

__global__
void update_section(double* read, double* write, alloy* my_alloy, double* materials) {
    int index = threadIdx.x;
    int stride = blockDim.x;

    int width = my_alloy->width;
    int height = my_alloy->height;

    for (int x = index; x < width; x += stride) {
        for (int y = 0; y < height; y++) {
    int num_neighbors = 0;

    double total_temp = 0.0;
    for (int m = 0; m < 3; m++) {
        double temp_mat = 0.0;
        for (int i = x - 1; i <= x + 1; i++) {
            double temp_per = 0.0;
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && j >= 0 &&
                        i < width && j < height) {
                    //temp_per += read[i][j] * materials[i][j][m];
                    temp_per += read[offsetDEV(width, i, j)] * materials[offsetDEV(width, height, i, j, m)];

                    if (m == 0) {
                        num_neighbors += 1;
                    }
                }
            }
            temp_mat += temp_per;
        }
        if (m == 0) {
            total_temp += temp_mat * my_alloy->mat_definition.const1;
        } else if (m == 1) {
            total_temp += temp_mat * my_alloy->mat_definition.const2;
        } else if (m == 2) {
            total_temp += temp_mat * my_alloy->mat_definition.const3;
        }
    }
            //printf("%f\n", total_temp / num_neighbors);
            //write[x][y] = total_temp / num_neighbors;
            write[offsetDEV(width, x, y)] = total_temp / num_neighbors;
        }
    }
}

__device__
int offsetDEV(int width, int x, int y) {
    return (y * width) + x;
}

__device__
int offsetDEV(int width, int height, int x, int y, int z) {
    return (z * width * height) + (y * width) + x;
}
