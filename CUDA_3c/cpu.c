#include <stdlib.h>

#include "alloy.h"
#include "cpu.h"

alloy* malloc_alloy() {
    return malloc(sizeof(alloy));
}

void free_alloy_struct(alloy* my_alloy) {
    free(my_alloy);
}

double* malloc_2d(int width, int height) {
    /*double** array = malloc(width * sizeof(double*) + width * height *
            sizeof(double));
    double *offs_points = (double*) &array[width];
    for (int i = 0; i < width; i++, offs_points += height) {
        array[i] = offs_points;
    }

    return array;*/

    return malloc(width * height * sizeof(double));
}

void free_2d(double* array, int height, int width) {
    free(array);
}

double* malloc_3d(int width, int height, int depth) {
    /*double*** array = malloc(width * sizeof(double**));
    for (int i = 0; i < width; i++) {
        array[i] = malloc(height * sizeof(double*));
        for (int j = 0; j < height; j++) {
            array[i][j] = malloc(depth * sizeof(double));
        }
    }

    return array;*/

    return malloc(width * height * depth * sizeof(double));
}

void free_3d(double* array, int width, int height, int depth) {
    /*for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
            free(array[i][j]);
        }
        free(array[i]);
    }
    free(array);*/

    return free(array);
}

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

    for (int i = 0; i < my_alloy->width; i++) {
        for (int j = 0; j < my_alloy->height; j++) {
            write[offset2D(width, i, j)] = next_position_temp(my_alloy, read, i, j);
        }
    }
}

double next_position_temp(alloy* my_alloy, double* read, int x, int y) {
    int num_neighbors = 0;

    int width = my_alloy->width;
    int height = my_alloy->height;

    double total_temp = 0.0;
    for (int m = 0; m < 3; m++) {
        double temp_mat = 0.0;
        for (int i = x - 1; i <= x + 1; i++) {
            double temp_per = 0.0;
            for (int j = y - 1; j <= y + 1; j++) {
                if (i >= 0 && j >= 0 &&
                        i < my_alloy->width && j < my_alloy->height) {
                    temp_per += read[offset2D(width, i, j)] * my_alloy->materials[offset3D(width, height, i, j, m)];

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

    return total_temp / num_neighbors;
}
