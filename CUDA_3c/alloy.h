#ifndef ALLOY_H
#define ALLOY_H
typedef struct {
    double const1;
    double const2;
    double const3;

    double ratio1;
    double ratio2;
    double ratio3;
} materials_def;

typedef struct {
    double* points_a; // 2d
    double* points_b; // 2d
    double* materials; // 3d

    materials_def mat_definition;

    int width;
    int height;
} alloy;

materials_def create_materials_def(double, double, double);

alloy* create_alloy(int, int, materials_def);
void initialize_materials(alloy*);
void initialize_points(alloy*, double);
void free_alloy(alloy*);

void stamp_dots(alloy*);
void stamp_pattern(alloy*);
void write_alloy_png(alloy*, const char*);

void write_random_material(materials_def, double* materials, int, int, int, int);

int offset2D(int, int, int);
int offset3D(int, int, int, int, int);
#endif // ALLOY_H
