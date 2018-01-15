#include <math.h>
#include <stdlib.h>

#include "alloy.h"
//#include "gpu.h"
#include "cpu.h"
//#include "writepng.cu"
#include "writepng.c"

materials_def create_materials_def(double const1, double const2, double const3) {
    materials_def mat_def;

    mat_def.const1 = const1;
    mat_def.const2 = const2;
    mat_def.const3 = const3;

    mat_def.ratio1 = 1.0 / 3.0;
    mat_def.ratio2 = 1.0 / 3.0;
    mat_def.ratio3 = 1.0 / 3.0;

    return mat_def;
}

alloy* create_alloy(int width, int height, materials_def mat_definition) {
    alloy* new_alloy = malloc_alloy();

    new_alloy->width = width;
    new_alloy->height = height;
    new_alloy->mat_definition = mat_definition;

    new_alloy->points_a = malloc_2d(width, height);
    new_alloy->points_b = malloc_2d(width, height);
    new_alloy->materials = malloc_3d(width, height, 3);

    initialize_materials(new_alloy);
    initialize_points(new_alloy, 0.0);

    return new_alloy;
}

void initialize_materials(alloy* my_alloy) {
    materials_def mat_def = my_alloy->mat_definition;

    int width = my_alloy->width;
    int height = my_alloy->height;

    for (int i = 0; i < my_alloy->width; i++) {
        for (int j = 0; j < my_alloy->height; j++) {
            write_random_material(mat_def, my_alloy->materials, width, height, i, j);
        }
    }
}

void initialize_points(alloy* my_alloy, double temperature) {
    int width = my_alloy->width;

    for (int i = 0; i < my_alloy->width; i++) {
        for (int j = 0; j < my_alloy->height; j++) {
            //my_alloy->points_a[i][j] = temperature;
            //my_alloy->points_b[i][j] = temperature;
            my_alloy->points_a[offset2D(width, i, j)] = temperature;
            my_alloy->points_b[offset2D(width, i, j)] = temperature;
        }
    }
}

void free_alloy(alloy* my_alloy) {
    int width = my_alloy->width;
    int height = my_alloy->height;

    free_2d(my_alloy->points_a, width, height);
    free_2d(my_alloy->points_b, width, height);
    free_3d(my_alloy->materials, width, height, 3);

    free_alloy_struct(my_alloy);
}

void stamp_dots(alloy* my_alloy) {
    //double temperature = 10000.0;
    double temperature = 10000.0;

    int width = my_alloy->width;
    int height = my_alloy->height;

    for (int a = 1; a <= 9; a += 8) {
        for (int b = 1; b <= 9; b += 8) {
            int x = a * (width / 10);
            int y = b * (width / 10);

            //my_alloy->points_a[x][y] = temperature * (a + b);
            my_alloy->points_a[offset2D(width, x, y)] = temperature * (a + b);
        }
    }
}

void stamp_pattern(alloy* my_alloy) {
    //double temperature = 7000.0;
    double temperature = 700.0;

    int width = my_alloy->width;
    int height = my_alloy->height;

    int width_brush = width / 10;
    int height_brush = height / 10;

    int boundary;
    int a = (int) (width / 2) * 0.8;
    int b = (int) (height / 2) * 0.8;
    if (a < b) {
        boundary = a;
    } else {
        boundary = b;
    }

    int num_rings = 12;
    int interval = boundary / num_rings;

    int center_x = width / 2;
    int center_y = height / 2;

    for (int r = 1; r < boundary; r++) {
        if (r % interval == 0) {
            int num_rotations = r /interval * 2;
            int rotation_deg = 360 / num_rotations;

            for (int d = 0; d < num_rotations; d++) {
                for (int i = 0; i < 6; i++) {
                    int x = center_x + r * cos(d * rotation_deg + i * 2 * M_PI / 6);
                    int y = center_y + r * sin(d * rotation_deg + i * 2 * M_PI / 6);

                    //my_alloy->points_a[x][y] = temperature;
                    my_alloy->points_a[offset2D(width, x, y)] = temperature;
                }
            }
        }
    }
}

void write_alloy_png(alloy* my_alloy, const char *path) {
    int width = my_alloy->width;
    int height = my_alloy->height;

    bitmap_t image;
    image.width = width;
    image.height = height;

    image.pixels = (pixel_t *) calloc(image.width * image.height, sizeof(pixel_t));

    int max = 256;

    for (int x = 0; x < image.width; x++) {
        for (int y = 0; y < image.height; y++) {
            pixel_t *pixel = pixel_at(&image, x, y);

            //int value = my_alloy->points_a[x][y];
            int value = my_alloy->points_a[offset2D(width, x, y)];

            int red = pix(value, max);

            if (red > 0) {
                pixel->red = pix(value, max);
            } else {
                pixel->blue = pix(-value, max);
            }
        } 
    }

    save_png_to_file(&image, path);

    free(image.pixels);
}

void write_random_material(materials_def mat_def, double* materials, int width, int height, int x, int y) {
    int var = 25;
    double r1 = (double) ((rand() % (2 * var)) - var);
    double r2 = (double) ((rand() % (2 * var)) - var);
    double r3 = (double) ((rand() % (2 * var)) - var);

    double p1 = mat_def.ratio1 * 100 + r1;
    double p2 = mat_def.ratio2 * 100 + r2;
    double p3 = mat_def.ratio3 * 100 + r3;

    if (p1 < 0.0) p1 = 0.0;
    if (p2 < 0.0) p2 = 0.0;
    if (p3 < 0.0) p3 = 0.0;

    double total = p1 + p2 + p3;

    materials[offset3D(width, height, x, y, 0)] = p1 / total;
    materials[offset3D(width, height, x, y, 1)] = p2 / total;
    materials[offset3D(width, height, x, y, 2)] = p3 / total;
}

int offset2D(int width, int x, int y) {
    return (y * width) + x;
}

int offset3D(int width, int height, int x, int y, int z) {
    return (z * width * height) + (y * width) + x;
}
